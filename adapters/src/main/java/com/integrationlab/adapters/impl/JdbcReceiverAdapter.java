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
import java.util.Collection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * JDBC Receiver Adapter implementation for database operations (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Supports INSERT, UPDATE, DELETE operations with batch processing and transaction management.
 */
public class JdbcReceiverAdapter extends AbstractReceiverAdapter {
    
    private final JdbcReceiverAdapterConfig config;
    private HikariDataSource dataSource;
    
    public JdbcReceiverAdapter(JdbcReceiverAdapterConfig config) {
        super(AdapterType.JDBC);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing JDBC receiver adapter (outbound) with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
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
        
        // Test 3: INSERT Query validation test (if configured)
        if (config.getInsertQuery() != null && !config.getInsertQuery().trim().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Test query preparation (doesn't execute)
                    try (PreparedStatement stmt = conn.prepareStatement(config.getInsertQuery())) {
                        ParameterMetaData paramMetaData = stmt.getParameterMetaData();
                        int paramCount = paramMetaData.getParameterCount();
                        
                        return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                                "Query Validation", "Insert query validated successfully (" + paramCount + " parameters)");
                    }
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                            "Query Validation", "Invalid insert query: " + e.getMessage(), e);
                }
            }));
        }
        
        // Test 4: Transaction support test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
            try (Connection conn = dataSource.getConnection()) {
                boolean autoCommit = conn.getAutoCommit();
                boolean supportsTransactions = conn.getMetaData().supportsTransactions();
                
                String message = String.format("Transaction support: %s, Auto-commit: %s", 
                                              supportsTransactions, autoCommit);
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                        "Transaction Support", message);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                        "Transaction Support", "Failed to check transaction support: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.JDBC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For JDBC Receiver (outbound), this method would be used to send data TO database
        // The criteria parameter contains the data to be inserted/updated
        return insertOrUpdateData(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        // Default receive without criteria
        throw new AdapterException.OperationException(AdapterType.JDBC, 
                "JDBC Receiver requires data payload for database operations");
    }
    
    private AdapterResult insertOrUpdateData(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.JDBC, "Payload cannot be null");
        }
        
        String query = determineQuery(null);
        if (query == null || query.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, 
                    "No SQL query configured for operation");
        }
        
        try (Connection conn = dataSource.getConnection()) {
            
            // Handle transactions
            boolean useTransactions = config.isUseTransactions();
            if (useTransactions) {
                conn.setAutoCommit(false);
            }
            
            try {
                int rowsAffected;
                
                if (payload instanceof Collection) {
                    // Batch processing
                    Collection<?> dataCollection = (Collection<?>) payload;
                    rowsAffected = executeBatch(conn, query, dataCollection);
                } else if (payload instanceof Map) {
                    // Single record
                    Map<String, Object> dataMap = (Map<String, Object>) payload;
                    rowsAffected = executeSingle(conn, query, dataMap);
                } else {
                    throw new AdapterException.ValidationException(AdapterType.JDBC, 
                            "Payload must be a Map or Collection of Maps");
                }
                
                if (useTransactions) {
                    conn.commit();
                }
                
                logger.info("JDBC receiver adapter processed {} rows", rowsAffected);
                
                return AdapterResult.success(rowsAffected, 
                        String.format("Successfully processed %d database records", rowsAffected));
                        
            } catch (Exception e) {
                if (useTransactions) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.warn("Failed to rollback transaction", rollbackEx);
                    }
                }
                throw e;
            }
        }
    }
    
    private int executeSingle(Connection conn, String query, Map<String, Object> data) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
            
            // Set parameters from data map
            setStatementParameters(stmt, data);
            
            return stmt.executeUpdate();
        }
    }
    
    private int executeBatch(Connection conn, String query, Collection<?> dataCollection) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
            
            int batchSize = config.getBatchSize() != null ? config.getBatchSize() : 100;
            int currentBatchSize = 0;
            int totalRowsAffected = 0;
            
            for (Object item : dataCollection) {
                if (!(item instanceof Map)) {
                    throw new SQLException("Batch items must be Maps");
                }
                
                Map<String, Object> data = (Map<String, Object>) item;
                setStatementParameters(stmt, data);
                stmt.addBatch();
                currentBatchSize++;
                
                if (currentBatchSize >= batchSize) {
                    int[] batchResults = stmt.executeBatch();
                    totalRowsAffected += sumBatchResults(batchResults);
                    currentBatchSize = 0;
                }
            }
            
            // Execute remaining batch
            if (currentBatchSize > 0) {
                int[] batchResults = stmt.executeBatch();
                totalRowsAffected += sumBatchResults(batchResults);
            }
            
            return totalRowsAffected;
        }
    }
    
    private void setStatementParameters(PreparedStatement stmt, Map<String, Object> data) throws SQLException {
        // This is a simplified parameter setting - in production, you'd need proper parameter mapping
        ParameterMetaData paramMetaData = stmt.getParameterMetaData();
        int paramCount = paramMetaData.getParameterCount();
        
        int paramIndex = 1;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (paramIndex > paramCount) break;
            stmt.setObject(paramIndex++, entry.getValue());
        }
    }
    
    private int sumBatchResults(int[] batchResults) {
        int total = 0;
        for (int result : batchResults) {
            if (result >= 0) {
                total += result;
            }
        }
        return total;
    }
    
    private String determineQuery(Map<String, Object> headers) {
        // Priority: headers override, then config defaults
        if (headers != null) {
            String headerQuery = (String) headers.get("sql.query");
            if (headerQuery != null && !headerQuery.trim().isEmpty()) {
                return headerQuery;
            }
            
            String operation = (String) headers.get("sql.operation");
            if ("UPDATE".equalsIgnoreCase(operation) && config.getUpdateQuery() != null) {
                return config.getUpdateQuery();
            } else if ("DELETE".equalsIgnoreCase(operation) && config.getDeleteQuery() != null) {
                return config.getDeleteQuery();
            }
        }
        
        // Default to INSERT query
        return config.getInsertQuery();
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "JDBC URL is required");
        }
        if (config.getDriverClass() == null || config.getDriverClass().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "JDBC driver class is required");
        }
        if (config.getInsertQuery() == null || config.getInsertQuery().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "INSERT query is required for JDBC receiver adapter");
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setDriverClassName(config.getDriverClass());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Connection pool settings
        hikariConfig.setMinimumIdle(config.getMinPoolSize());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeoutSeconds() * 1000L);
        
        // Connection properties
        if (config.getConnectionProperties() != null && !config.getConnectionProperties().trim().isEmpty()) {
            Properties props = parseConnectionProperties(config.getConnectionProperties());
            hikariConfig.setDataSourceProperties(props);
        }
        
        return new HikariDataSource(hikariConfig);
    }
    
    private Properties parseConnectionProperties(String connectionProperties) {
        Properties props = new Properties();
        String[] pairs = connectionProperties.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                props.setProperty(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return props;
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^;&]*", "password=***");
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // JDBC receivers typically don't poll, they write data
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JDBC Receiver (Outbound): %s, Insert Query: %s, Batch Size: %d", 
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getInsertQuery() != null ? config.getInsertQuery().substring(0, Math.min(50, config.getInsertQuery().length())) + "..." : "Not configured",
                config.getBatchSize() != null ? config.getBatchSize() : 0);
    }
}