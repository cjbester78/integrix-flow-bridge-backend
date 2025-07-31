package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.JdbcReceiverAdapterConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * JDBC Receiver Adapter implementation for database polling and data retrieval.
 * Supports SELECT operations with polling, pagination, and incremental data processing.
 */
public class JdbcReceiverAdapter extends AbstractReceiverAdapter {
    
    private final JdbcReceiverAdapterConfig config;
    private HikariDataSource dataSource;
    private Object lastProcessedValue; // For incremental polling
    
    public JdbcReceiverAdapter(JdbcReceiverAdapterConfig config) {
        super(AdapterType.JDBC);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing JDBC receiver adapter with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
        validateConfiguration();
        dataSource = createDataSource();
        
        logger.info("JDBC receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying JDBC receiver adapter");
        
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Comprehensive connection testing
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.JDBC, () -> {
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                            "Basic Connectivity", "Database connection established successfully");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                            "Basic Connectivity", "Connection is not valid", null);
                }
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                        "Basic Connectivity", "Failed to establish connection: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Database schema validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                String databaseName = metaData.getDatabaseProductName();
                String databaseVersion = metaData.getDatabaseProductVersion();
                
                logger.debug("Connected to {} version {}", databaseName, databaseVersion);
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                        "Database Schema", "Database accessible: " + databaseName + " " + databaseVersion);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                        "Database Schema", "Failed to access database metadata: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Query validation test
        if (config.getSelectQuery() != null && !config.getSelectQuery().trim().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Test query preparation and execution with LIMIT 0 (doesn't return data)
                    String testQuery = addLimitToQuery(config.getSelectQuery(), 0);
                    
                    try (PreparedStatement stmt = conn.prepareStatement(testQuery)) {
                        stmt.setQueryTimeout(5); // Short timeout for test
                        
                        try (ResultSet rs = stmt.executeQuery()) {
                            ResultSetMetaData rsmd = rs.getMetaData();
                            int columnCount = rsmd.getColumnCount();
                            
                            return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                                    "Query Validation", "Select query validated successfully (" + columnCount + " columns)");
                        }
                    }
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                            "Query Validation", "Invalid select query: " + e.getMessage(), e);
                }
            }));
        }
        
        // Test 4: Table accessibility test (if table name is extractable)
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                // Try to get some basic table information
                try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    int tableCount = 0;
                    while (tables.next() && tableCount < 5) { // Just count first few tables
                        tableCount++;
                    }
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                            "Table Access", "Database schema accessible, found " + tableCount + "+ tables");
                }
                
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                        "Table Access", "Failed to access table metadata: " + e.getMessage(), e);
            }
        }));
        
        // Test 5: Incremental column test (if configured)
        if (config.getIncrementalColumn() != null && !config.getIncrementalColumn().trim().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Try to validate incremental column exists in query
                    String testQuery = addLimitToQuery(config.getSelectQuery(), 1);
                    
                    try (PreparedStatement stmt = conn.prepareStatement(testQuery);
                         ResultSet rs = stmt.executeQuery()) {
                        
                        ResultSetMetaData rsmd = rs.getMetaData();
                        boolean columnFound = false;
                        
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            if (config.getIncrementalColumn().equalsIgnoreCase(rsmd.getColumnName(i))) {
                                columnFound = true;
                                break;
                            }
                        }
                        
                        if (columnFound) {
                            return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                                    "Incremental Column", "Incremental column '" + config.getIncrementalColumn() + "' found");
                        } else {
                            return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                                    "Incremental Column", "Incremental column '" + config.getIncrementalColumn() + "' not found in query results", null);
                        }
                    }
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                            "Incremental Column", "Failed to validate incremental column: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.JDBC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        String query = buildReceiveQuery(criteria);
        
        if (query == null || query.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, 
                    "No SELECT query configured for receive operation");
        }
        
        try (Connection conn = dataSource.getConnection()) {
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
                
                // Set fetch size for large result sets
                if (config.getFetchSize() != null && config.getFetchSize() > 0) {
                    stmt.setFetchSize(config.getFetchSize());
                }
                
                // Set query parameters based on criteria and incremental processing
                setReceiveQueryParameters(stmt, criteria);
                
                long startTime = System.currentTimeMillis();
                
                try (ResultSet rs = stmt.executeQuery()) {
                    
                    List<Map<String, Object>> results = new ArrayList<>();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    
                    Object newLastProcessedValue = lastProcessedValue;
                    int rowCount = 0;
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnName(i);
                            Object columnValue = rs.getObject(i);
                            row.put(columnName, columnValue);
                        }
                        
                        results.add(row);
                        rowCount++;
                        
                        // Track incremental column value
                        if (config.getIncrementalColumn() != null) {
                            Object incrementalValue = row.get(config.getIncrementalColumn());
                            if (incrementalValue != null) {
                                newLastProcessedValue = incrementalValue;
                            }
                        }
                        
                        // Check max results limit
                        if (config.getMaxResults() != null && rowCount >= config.getMaxResults()) {
                            logger.debug("Max results limit reached: {}", config.getMaxResults());
                            break;
                        }
                    }
                    
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // Update last processed value for incremental polling
                    lastProcessedValue = newLastProcessedValue;
                    
                    AdapterResult result;
                    if (results.isEmpty()) {
                        result = AdapterResult.success(null, "No new data available");
                    } else {
                        result = AdapterResult.success(results, "Retrieved " + results.size() + " records");
                    }
                    
                    result.addMetadata("rowCount", rowCount);
                    result.addMetadata("executionTimeMs", duration);
                    result.addMetadata("query", maskSensitiveQuery(query));
                    result.addMetadata("incrementalValue", lastProcessedValue);
                    
                    logger.debug("Receive operation completed: {} records retrieved in {}ms", 
                                rowCount, duration);
                    
                    return result;
                }
            }
            
        } catch (SQLException e) {
            logger.error("JDBC receive operation failed", e);
            
            if (e.getErrorCode() == 0 && "08S01".equals(e.getSQLState())) {
                throw new AdapterException.ConnectionException(AdapterType.JDBC, 
                        "Database connection lost: " + e.getMessage(), e);
            } else if ("42000".equals(e.getSQLState())) {
                throw new AdapterException.ValidationException(AdapterType.JDBC, 
                        "SQL syntax error: " + e.getMessage(), e);
            } else {
                throw new AdapterException(AdapterType.JDBC, AdapterMode.RECEIVER, 
                        "Database query failed: " + e.getMessage(), e);
            }
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 30000; // 30 seconds default
    }
    
    private void validateConfiguration() throws AdapterException {
        ConnectionTestUtil.validateRequiredField(AdapterType.JDBC, "JDBC URL", config.getJdbcUrl());
        ConnectionTestUtil.validateRequiredField(AdapterType.JDBC, "Driver Class", config.getDriverClass());
        ConnectionTestUtil.validateRequiredField(AdapterType.JDBC, "Select Query", config.getSelectQuery());
        
        if (config.getUsername() == null) {
            logger.warn("No username configured for JDBC connection");
        }
        
        ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Connection Timeout", 
                config.getConnectionTimeoutSeconds(), 1, 300);
        ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Query Timeout", 
                config.getQueryTimeoutSeconds(), 1, 600);
        
        if (config.getFetchSize() != null) {
            ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Fetch Size", 
                    config.getFetchSize(), 1, 100000);
        }
        
        if (config.getMaxResults() != null) {
            ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Max Results", 
                    config.getMaxResults(), 1, 1000000);
        }
        
        if (config.getPollingInterval() != null) {
            ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Polling Interval", 
                    config.getPollingInterval().intValue(), 1000, 3600000); // 1 second to 1 hour
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClass());
        
        // Connection pool settings - smaller pool for receivers
        hikariConfig.setMinimumIdle(config.getMinPoolSize() != null ? config.getMinPoolSize() : 1);
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 5);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeoutSeconds() * 1000L);
        hikariConfig.setIdleTimeout(600000); // 10 minutes
        hikariConfig.setMaxLifetime(1800000); // 30 minutes
        
        // Connection validation
        hikariConfig.setConnectionTestQuery(getValidationQuery());
        hikariConfig.setValidationTimeout(5000);
        
        // Read-only configuration for receivers
        hikariConfig.setReadOnly(true);
        
        // Additional properties
        if (config.getConnectionProperties() != null && !config.getConnectionProperties().isEmpty()) {
            Properties props = new Properties();
            String[] propertyPairs = config.getConnectionProperties().split(",");
            for (String pair : propertyPairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    props.setProperty(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            hikariConfig.setDataSourceProperties(props);
        }
        
        return new HikariDataSource(hikariConfig);
    }
    
    private String getValidationQuery() {
        String driverClass = config.getDriverClass().toLowerCase();
        
        if (driverClass.contains("mysql")) {
            return "SELECT 1";
        } else if (driverClass.contains("postgresql")) {
            return "SELECT 1";
        } else if (driverClass.contains("oracle")) {
            return "SELECT 1 FROM DUAL";
        } else if (driverClass.contains("sqlserver")) {
            return "SELECT 1";
        } else if (driverClass.contains("h2")) {
            return "SELECT 1";
        } else {
            return "SELECT 1"; // Default
        }
    }
    
    private String buildReceiveQuery(Object criteria) {
        String baseQuery = config.getSelectQuery();
        
        if (baseQuery == null) {
            return null;
        }
        
        StringBuilder queryBuilder = new StringBuilder(baseQuery);
        
        // Add incremental processing WHERE clause
        if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
            String incrementalCondition = String.format(" AND %s > ?", config.getIncrementalColumn());
            
            if (baseQuery.toUpperCase().contains("WHERE")) {
                queryBuilder.append(incrementalCondition);
            } else {
                queryBuilder.append(" WHERE ").append(incrementalCondition.substring(5)); // Remove " AND "
            }
        }
        
        // Add ORDER BY for incremental processing
        if (config.getIncrementalColumn() != null && !baseQuery.toUpperCase().contains("ORDER BY")) {
            queryBuilder.append(" ORDER BY ").append(config.getIncrementalColumn()).append(" ASC");
        }
        
        // Add LIMIT if specified and not already present
        if (config.getMaxResults() != null && !baseQuery.toUpperCase().contains("LIMIT")) {
            String driverClass = config.getDriverClass().toLowerCase();
            
            if (driverClass.contains("mysql") || driverClass.contains("postgresql") || driverClass.contains("h2")) {
                queryBuilder.append(" LIMIT ").append(config.getMaxResults());
            } else if (driverClass.contains("sqlserver")) {
                // SQL Server uses TOP - would need to modify the SELECT clause
                // For now, handle in application code
            } else if (driverClass.contains("oracle")) {
                // Oracle uses ROWNUM - would need more complex query modification
                // For now, handle in application code
            }
        }
        
        return queryBuilder.toString();
    }
    
    private void setReceiveQueryParameters(PreparedStatement stmt, Object criteria) throws SQLException {
        int paramIndex = 1;
        
        // Set incremental parameter if configured
        if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
            stmt.setObject(paramIndex++, lastProcessedValue);
        }
        
        // Set additional parameters from criteria
        if (criteria instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> criteriaMap = (Map<String, Object>) criteria;
            
            // This is a simplified implementation - in practice, you'd need more sophisticated
            // parameter mapping based on the query and criteria
            for (Object value : criteriaMap.values()) {
                try {
                    stmt.setObject(paramIndex++, value);
                } catch (SQLException e) {
                    // Stop if we've set all available parameters
                    break;
                }
            }
        } else if (criteria instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> criteriaList = (List<Object>) criteria;
            
            for (Object value : criteriaList) {
                try {
                    stmt.setObject(paramIndex++, value);
                } catch (SQLException e) {
                    // Stop if we've set all available parameters
                    break;
                }
            }
        }
    }
    
    private String addLimitToQuery(String query, int limit) {
        if (query == null) return null;
        
        String upperQuery = query.toUpperCase();
        if (upperQuery.contains("LIMIT")) {
            return query; // Already has limit
        }
        
        String driverClass = config.getDriverClass().toLowerCase();
        
        if (driverClass.contains("mysql") || driverClass.contains("postgresql") || driverClass.contains("h2")) {
            return query + " LIMIT " + limit;
        } else if (driverClass.contains("sqlserver")) {
            // Simple case - add TOP to SELECT
            return query.replaceFirst("(?i)SELECT", "SELECT TOP " + limit);
        } else if (driverClass.contains("oracle")) {
            // Wrap in subquery with ROWNUM
            return "SELECT * FROM (" + query + ") WHERE ROWNUM <= " + limit;
        } else {
            return query + " LIMIT " + limit; // Default
        }
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^;&]*", "password=***");
    }
    
    private String maskSensitiveQuery(String query) {
        if (query == null) return null;
        return query.length() > 100 ? query.substring(0, 100) + "..." : query;
    }
    
    /**
     * Reset the incremental processing state.
     * Useful for restarting polling from the beginning.
     */
    public void resetIncrementalState() {
        lastProcessedValue = null;
        logger.info("Incremental state reset for JDBC receiver adapter");
    }
    
    /**
     * Get the current incremental processing value.
     * 
     * @return the last processed incremental value, or null if not set
     */
    public Object getLastProcessedValue() {
        return lastProcessedValue;
    }
    
    /**
     * Set the incremental processing starting point.
     * 
     * @param value the starting value for incremental processing
     */
    public void setLastProcessedValue(Object value) {
        lastProcessedValue = value;
        logger.debug("Set incremental starting value: {}", value);
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JdbcReceiverAdapter{url=%s, driver=%s, pool=%d-%d, polling=%dms, incremental=%s, active=%s}",
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getDriverClass(),
                config.getMinPoolSize() != null ? config.getMinPoolSize() : 1,
                config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 5,
                config.getPollingInterval() != null ? config.getPollingInterval() : 30000,
                config.getIncrementalColumn() != null ? config.getIncrementalColumn() : "none",
                isActive());
    }
}