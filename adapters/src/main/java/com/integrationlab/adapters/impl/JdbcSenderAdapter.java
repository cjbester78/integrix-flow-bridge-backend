package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.JdbcSenderAdapterConfig;

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
 * JDBC Sender Adapter implementation for database operations.
 * Supports INSERT, UPDATE, DELETE operations with batch processing and transaction management.
 */
public class JdbcSenderAdapter extends AbstractSenderAdapter {
    
    private final JdbcSenderAdapterConfig config;
    private HikariDataSource dataSource;
    
    public JdbcSenderAdapter(JdbcSenderAdapterConfig config) {
        super(AdapterType.JDBC);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing JDBC sender adapter with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
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
                
                // Check if we can access database metadata
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
        
        // Test 3: Query execution test (if configured)
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
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.JDBC, "Payload cannot be null");
        }
        
        String query = determineQuery(headers);
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
                AdapterResult result;
                
                if (payload instanceof List) {
                    // Batch operation
                    result = executeBatchOperation(conn, query, (List<?>) payload, headers);
                } else {
                    // Single operation
                    result = executeSingleOperation(conn, query, payload, headers);
                }
                
                if (useTransactions) {
                    conn.commit();
                    logger.debug("Transaction committed successfully");
                }
                
                return result;
                
            } catch (Exception e) {
                if (useTransactions) {
                    try {
                        conn.rollback();
                        logger.debug("Transaction rolled back due to error");
                    } catch (SQLException rollbackException) {
                        logger.error("Failed to rollback transaction", rollbackException);
                    }
                }
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("JDBC send operation failed", e);
            
            if (e.getErrorCode() == 0 && "08S01".equals(e.getSQLState())) {
                throw new AdapterException.ConnectionException(AdapterType.JDBC, 
                        "Database connection lost: " + e.getMessage(), e);
            } else if ("23000".equals(e.getSQLState())) {
                throw new AdapterException.ValidationException(AdapterType.JDBC, 
                        "Data integrity constraint violation: " + e.getMessage(), e);
            } else {
                throw new AdapterException(AdapterType.JDBC, AdapterMode.SENDER, 
                        "Database operation failed: " + e.getMessage(), e);
            }
        }
    }
    
    private void validateConfiguration() throws AdapterException {
        ConnectionTestUtil.validateRequiredField(AdapterType.JDBC, "JDBC URL", config.getJdbcUrl());
        ConnectionTestUtil.validateRequiredField(AdapterType.JDBC, "Driver Class", config.getDriverClass());
        
        if (config.getUsername() == null) {
            logger.warn("No username configured for JDBC connection");
        }
        
        if (config.getInsertQuery() == null && config.getUpdateQuery() == null && config.getDeleteQuery() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, 
                    "At least one SQL query (insert, update, or delete) must be configured");
        }
        
        ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Connection Timeout", 
                config.getConnectionTimeoutSeconds(), 1, 300);
        ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Query Timeout", 
                config.getQueryTimeoutSeconds(), 1, 600);
        
        if (config.getBatchSize() != null) {
            ConnectionTestUtil.validateNumericRange(AdapterType.JDBC, "Batch Size", 
                    config.getBatchSize(), 1, 10000);
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClass());
        
        // Connection pool settings
        hikariConfig.setMinimumIdle(config.getMinPoolSize() != null ? config.getMinPoolSize() : 1);
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 10);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeoutSeconds() * 1000L);
        hikariConfig.setIdleTimeout(600000); // 10 minutes
        hikariConfig.setMaxLifetime(1800000); // 30 minutes
        
        // Connection validation
        hikariConfig.setConnectionTestQuery(getValidationQuery());
        hikariConfig.setValidationTimeout(5000);
        
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
    
    private String determineQuery(Map<String, Object> headers) {
        // Check for operation type in headers
        if (headers != null) {
            String operationType = (String) headers.get("operation");
            if ("UPDATE".equalsIgnoreCase(operationType) && config.getUpdateQuery() != null) {
                return config.getUpdateQuery();
            } else if ("DELETE".equalsIgnoreCase(operationType) && config.getDeleteQuery() != null) {
                return config.getDeleteQuery();
            }
        }
        
        // Default to insert query
        return config.getInsertQuery();
    }
    
    private AdapterResult executeSingleOperation(Connection conn, String query, Object payload, 
                                               Map<String, Object> headers) throws SQLException {
        
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
            
            // Set parameters
            setStatementParameters(stmt, payload, headers);
            
            long startTime = System.currentTimeMillis();
            int rowsAffected = stmt.executeUpdate();
            long duration = System.currentTimeMillis() - startTime;
            
            AdapterResult result = AdapterResult.success(rowsAffected, 
                    "Database operation completed successfully, " + rowsAffected + " rows affected");
            
            // Add generated keys if available
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.addMetadata("generatedKey", generatedKeys.getObject(1));
                }
            } catch (SQLException e) {
                // Ignore if generated keys not supported
            }
            
            result.addMetadata("rowsAffected", rowsAffected);
            result.addMetadata("executionTimeMs", duration);
            result.addMetadata("query", maskSensitiveQuery(query));
            
            logger.debug("Single operation completed: {} rows affected in {}ms", rowsAffected, duration);
            
            return result;
        }
    }
    
    private AdapterResult executeBatchOperation(Connection conn, String query, List<?> payloadList, 
                                              Map<String, Object> headers) throws SQLException {
        
        int batchSize = config.getBatchSize() != null ? config.getBatchSize() : 1000;
        int totalRowsAffected = 0;
        List<Object> generatedKeys = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < payloadList.size(); i++) {
                setStatementParameters(stmt, payloadList.get(i), headers);
                stmt.addBatch();
                
                // Execute batch when batch size is reached or at the end
                if ((i + 1) % batchSize == 0 || i == payloadList.size() - 1) {
                    int[] batchResults = stmt.executeBatch();
                    
                    for (int result : batchResults) {
                        if (result >= 0) {
                            totalRowsAffected += result;
                        } else if (result == Statement.SUCCESS_NO_INFO) {
                            // Operation successful but no row count
                            totalRowsAffected++;
                        }
                    }
                    
                    // Collect generated keys
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        while (keys.next()) {
                            generatedKeys.add(keys.getObject(1));
                        }
                    } catch (SQLException e) {
                        // Ignore if generated keys not supported
                    }
                    
                    stmt.clearBatch();
                    logger.debug("Batch executed: {} operations processed so far", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            AdapterResult result = AdapterResult.success(totalRowsAffected, 
                    "Batch operation completed successfully, " + totalRowsAffected + " rows affected");
            
            result.addMetadata("rowsAffected", totalRowsAffected);
            result.addMetadata("batchSize", batchSize);
            result.addMetadata("totalOperations", payloadList.size());
            result.addMetadata("executionTimeMs", duration);
            result.addMetadata("query", maskSensitiveQuery(query));
            
            if (!generatedKeys.isEmpty()) {
                result.addMetadata("generatedKeys", generatedKeys);
            }
            
            logger.debug("Batch operation completed: {} operations, {} rows affected in {}ms", 
                        payloadList.size(), totalRowsAffected, duration);
            
            return result;
        }
    }
    
    private void setStatementParameters(PreparedStatement stmt, Object payload, 
                                      Map<String, Object> headers) throws SQLException {
        
        if (payload instanceof Map) {
            // Map-based parameter setting
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) payload;
            
            ParameterMetaData paramMetaData = stmt.getParameterMetaData();
            int paramCount = paramMetaData.getParameterCount();
            
            for (int i = 1; i <= paramCount; i++) {
                String paramName = "param" + i; // Default parameter name
                
                // Try to get parameter value from payload map
                Object paramValue = paramMap.get(paramName);
                if (paramValue == null) {
                    paramValue = paramMap.get("p" + i);
                }
                if (paramValue == null) {
                    paramValue = paramMap.get(String.valueOf(i));
                }
                
                if (paramValue != null) {
                    stmt.setObject(i, paramValue);
                } else {
                    stmt.setNull(i, Types.VARCHAR);
                }
            }
            
        } else if (payload instanceof List) {
            // List-based parameter setting
            @SuppressWarnings("unchecked")
            List<Object> paramList = (List<Object>) payload;
            
            for (int i = 0; i < paramList.size(); i++) {
                stmt.setObject(i + 1, paramList.get(i));
            }
            
        } else {
            // Single parameter
            stmt.setObject(1, payload);
        }
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        
        // Mask password in JDBC URL
        return url.replaceAll("password=[^;&]*", "password=***");
    }
    
    private String maskSensitiveQuery(String query) {
        if (query == null) return null;
        
        // Return first 100 characters for logging (avoid logging sensitive data)
        return query.length() > 100 ? query.substring(0, 100) + "..." : query;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JdbcSenderAdapter{url=%s, driver=%s, pool=%d-%d, transactions=%s, active=%s}",
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getDriverClass(),
                config.getMinPoolSize() != null ? config.getMinPoolSize() : 1,
                config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 10,
                config.isUseTransactions(),
                isActive());
    }
}