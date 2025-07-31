package com.integrationlab.adapters.config;

/**
 * Configuration for JDBC Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on connecting TO source databases to poll/retrieve data.
 */
public class JdbcSenderAdapterConfig {
    
    // Source Database Connection Details
    private String jdbcUrl; // Connection URL to source database
    private String driverClass;
    private String username;
    private String password;
    private String databaseType; // MYSQL, POSTGRESQL, ORACLE, SQLSERVER, etc.
    
    // Connection Pool Settings for source database
    private Integer minPoolSize = 1;
    private Integer maxPoolSize = 5; // Smaller pool for polling
    private int connectionTimeoutSeconds = 30;
    private String connectionProperties; // Additional JDBC properties
    
    // Data Retrieval Configuration
    private String selectQuery; // Main query to retrieve data from source
    private String countQuery; // Optional query to count available records
    
    // Data Modification Configuration (for write operations)
    private String insertQuery; // INSERT statement for creating records
    private String updateQuery; // UPDATE statement for modifying records
    private String deleteQuery; // DELETE statement for removing records
    private Integer batchSize; // Batch size for bulk operations
    private boolean useTransactions = true; // Use transactions for data modifications
    private Integer fetchSize = 1000; // Number of rows to fetch at once
    private Integer maxResults; // Maximum number of results to return
    private int queryTimeoutSeconds = 300; // 5 minutes default
    
    // Incremental Processing (for polling scenarios)
    private String incrementalColumn; // Column to track incremental changes (timestamp, ID, etc.)
    private Object lastProcessedValue; // Last processed value for incremental polling
    private boolean resetIncrementalOnStart = false;
    
    // Polling Configuration
    private Long pollingInterval = 30000L; // 30 seconds default polling interval
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    
    // Data Processing
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from source
    private boolean validateData = true;
    private String dataValidationRules;
    
    // Transaction Configuration
    private String transactionIsolationLevel = "READ_COMMITTED";
    private boolean readOnly = true; // Source connections are typically read-only
    private boolean autoCommit = true;
    
    // Error Handling & Retry
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    private boolean skipErrorRows = false; // Continue processing on row errors
    private String errorHandlingStrategy = "FAIL_FAST"; // FAIL_FAST, SKIP_ERRORS, LOG_AND_CONTINUE
    
    // Monitoring & Logging
    private boolean logSlowQueries = true;
    private long slowQueryThresholdMs = 10000; // 10 seconds
    private boolean enableMetrics = true;
    
    // Constructors
    public JdbcSenderAdapterConfig() {}
    
    // Getters and Setters
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    
    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
    
    public Integer getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(Integer minPoolSize) { this.minPoolSize = minPoolSize; }
    
    public Integer getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(Integer maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    
    public int getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) { this.connectionTimeoutSeconds = connectionTimeoutSeconds; }
    
    public String getConnectionProperties() { return connectionProperties; }
    public void setConnectionProperties(String connectionProperties) { this.connectionProperties = connectionProperties; }
    
    public String getSelectQuery() { return selectQuery; }
    public void setSelectQuery(String selectQuery) { this.selectQuery = selectQuery; }
    
    public String getCountQuery() { return countQuery; }
    public void setCountQuery(String countQuery) { this.countQuery = countQuery; }
    
    public String getInsertQuery() { return insertQuery; }
    public void setInsertQuery(String insertQuery) { this.insertQuery = insertQuery; }
    
    public String getUpdateQuery() { return updateQuery; }
    public void setUpdateQuery(String updateQuery) { this.updateQuery = updateQuery; }
    
    public String getDeleteQuery() { return deleteQuery; }
    public void setDeleteQuery(String deleteQuery) { this.deleteQuery = deleteQuery; }
    
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    
    public boolean isUseTransactions() { return useTransactions; }
    public void setUseTransactions(boolean useTransactions) { this.useTransactions = useTransactions; }
    
    public Integer getFetchSize() { return fetchSize; }
    public void setFetchSize(Integer fetchSize) { this.fetchSize = fetchSize; }
    
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
    
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) { this.queryTimeoutSeconds = queryTimeoutSeconds; }
    
    public String getIncrementalColumn() { return incrementalColumn; }
    public void setIncrementalColumn(String incrementalColumn) { this.incrementalColumn = incrementalColumn; }
    
    public Object getLastProcessedValue() { return lastProcessedValue; }
    public void setLastProcessedValue(Object lastProcessedValue) { this.lastProcessedValue = lastProcessedValue; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
    public boolean isValidateData() { return validateData; }
    public void setValidateData(boolean validateData) { this.validateData = validateData; }
    
    public String getDataValidationRules() { return dataValidationRules; }
    public void setDataValidationRules(String dataValidationRules) { this.dataValidationRules = dataValidationRules; }
    
    public String getTransactionIsolationLevel() { return transactionIsolationLevel; }
    public void setTransactionIsolationLevel(String transactionIsolationLevel) { this.transactionIsolationLevel = transactionIsolationLevel; }
    
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
    
    public boolean isAutoCommit() { return autoCommit; }
    public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isSkipErrorRows() { return skipErrorRows; }
    public void setSkipErrorRows(boolean skipErrorRows) { this.skipErrorRows = skipErrorRows; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public boolean isLogSlowQueries() { return logSlowQueries; }
    public void setLogSlowQueries(boolean logSlowQueries) { this.logSlowQueries = logSlowQueries; }
    
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    @Override
    public String toString() {
        return String.format("JdbcSenderAdapterConfig{url='%s', driver='%s', user='%s', polling=%dms, incremental='%s'}",
                jdbcUrl != null ? jdbcUrl.replaceAll("password=[^;&]*", "password=***") : null,
                driverClass, username, pollingInterval, incrementalColumn);
    }
}