package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.JdbcSenderAdapterConfig;

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
 * JDBC Sender Adapter implementation for database polling and data retrieval (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports SELECT operations with polling, pagination, and incremental data processing.
 */
public class JdbcSenderAdapter extends AbstractSenderAdapter {
    
    private final JdbcSenderAdapterConfig config;
    private HikariDataSource dataSource;
    private Object lastProcessedValue; // For incremental polling
    
    public JdbcSenderAdapter(JdbcSenderAdapterConfig config) {
        super(AdapterType.JDBC);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing JDBC sender adapter (inbound) with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
        validateConfiguration();
        dataSource = createDataSource();
        
        logger.info("JDBC sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying JDBC sender adapter");
        
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
        
        // Test 3: SELECT Query validation test
        if (config.getSelectQuery() != null && !config.getSelectQuery().trim().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JDBC, () -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Test query preparation and execution with LIMIT 0 (doesn't return data)
                    String testQuery = addLimitToQuery(config.getSelectQuery(), 0);
                    
                    try (PreparedStatement stmt = conn.prepareStatement(testQuery)) {
                        stmt.setQueryTimeout(5); // Short timeout for test
                        stmt.executeQuery(); // Execute but don't fetch results
                        
                        return ConnectionTestUtil.createTestSuccess(AdapterType.JDBC, 
                                "Query Validation", "SELECT query validated successfully");
                    }
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JDBC, 
                            "Query Validation", "Invalid SELECT query: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.JDBC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For JDBC Sender (inbound), "send" means polling/retrieving data FROM database
        return pollForData();
    }
    
    private AdapterResult pollForData() throws Exception {
        if (config.getSelectQuery() == null || config.getSelectQuery().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "SELECT query not configured");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setReadOnly(config.isReadOnly());
            conn.setAutoCommit(config.isAutoCommit());
            
            String query = buildIncrementalQuery();
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
                
                if (config.getFetchSize() != null) {
                    stmt.setFetchSize(config.getFetchSize());
                }
                
                if (config.getMaxResults() != null) {
                    stmt.setMaxRows(config.getMaxResults());
                }
                
                // Set incremental parameter if configured
                if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
                    stmt.setObject(1, lastProcessedValue);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        results.add(row);
                        
                        // Update last processed value for incremental polling
                        if (config.getIncrementalColumn() != null) {
                            Object incrementalValue = row.get(config.getIncrementalColumn());
                            if (incrementalValue != null) {
                                lastProcessedValue = incrementalValue;
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("JDBC sender adapter polled {} records from database", results.size());
        
        return AdapterResult.success(results, 
                String.format("Retrieved %d records from database", results.size()));
    }
    
    private String buildIncrementalQuery() {
        String baseQuery = config.getSelectQuery();
        
        if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
            // Add WHERE clause for incremental processing
            if (baseQuery.toUpperCase().contains("WHERE")) {
                baseQuery += " AND " + config.getIncrementalColumn() + " > ?";
            } else {
                baseQuery += " WHERE " + config.getIncrementalColumn() + " > ?";
            }
        }
        
        // Add ORDER BY for incremental column
        if (config.getIncrementalColumn() != null) {
            if (!baseQuery.toUpperCase().contains("ORDER BY")) {
                baseQuery += " ORDER BY " + config.getIncrementalColumn() + " ASC";
            }
        }
        
        return baseQuery;
    }
    
    private String addLimitToQuery(String query, int limit) {
        // Simple LIMIT addition - this would need to be database-specific in production
        return query + " LIMIT " + limit;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "JDBC URL is required");
        }
        if (config.getDriverClass() == null || config.getDriverClass().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "JDBC driver class is required");
        }
        if (config.getSelectQuery() == null || config.getSelectQuery().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, "SELECT query is required for JDBC sender adapter");
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
    public String getConfigurationSummary() {
        return String.format("JDBC Sender (Inbound): %s, Query: %s, Polling: %dms", 
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getSelectQuery() != null ? config.getSelectQuery().substring(0, Math.min(50, config.getSelectQuery().length())) + "..." : "Not configured",
                config.getPollingInterval() != null ? config.getPollingInterval() : 0);
    }
}