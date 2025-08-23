package com.integrixs.adapters.config;

/**
 * Configuration for JDBC Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on connecting TO target databases to insert/update data.
 */
public class JdbcReceiverAdapterConfig {
    
    // Target Database Connection Details
    private String jdbcUrl; // Connection URL to target database
    private String driverClass;
    private String username;
    private String password;
    private String databaseType; // MYSQL, POSTGRESQL, ORACLE, SQLSERVER, etc.
    
    // Connection Pool Settings for target database
    private Integer minPoolSize = 2;
    private Integer maxPoolSize = 10; // Larger pool for writing operations
    private int connectionTimeoutSeconds = 30;
    private String connectionProperties; // Additional JDBC properties
    
    // Data Writing Configuration
    private String insertQuery; // SQL for inserting new records
    private String updateQuery; // SQL for updating existing records
    private String deleteQuery; // SQL for deleting records
    private String upsertQuery; // SQL for insert-or-update operations
    private int queryTimeoutSeconds = 300; // 5 minutes default
    
    // Data Reading Configuration (for compatibility with receiver implementation)
    private String selectQuery; // SQL for reading/polling data
    private String incrementalColumn; // Column for incremental processing
    private Integer fetchSize; // Fetch size for result sets
    private Integer maxResults; // Maximum results per query
    private Long pollingInterval; // Polling interval in milliseconds
    
    // Batch Processing
    private Integer batchSize = 1000; // Number of records to batch together
    private boolean enableBatching = true;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Transaction Configuration
    private boolean useTransactions = true;
    private String transactionIsolationLevel = "READ_COMMITTED";
    private boolean autoCommit = false; // Use explicit commits for better control
    private long transactionTimeoutMs = 300000; // 5 minutes
    
    // Data Mapping & Transformation
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    private String dataMapping; // Field mapping configuration
    private boolean validateData = true;
    private String dataValidationRules;
    
    // Conflict Resolution & Error Handling
    private String conflictResolutionStrategy = "FAIL"; // FAIL, SKIP, OVERWRITE, MERGE
    private String duplicateKeyHandling = "ERROR"; // ERROR, IGNORE, UPDATE
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10; // Max errors before stopping
    private String errorHandlingStrategy = "FAIL_FAST"; // FAIL_FAST, SKIP_ERRORS, LOG_AND_CONTINUE
    
    // Performance & Optimization
    private boolean useConnectionPooling = true;
    private boolean enableStatementCaching = true;
    private int statementCacheSize = 100;
    private boolean analyzePerformance = true;
    private long slowQueryThresholdMs = 5000; // 5 seconds
    
    // Retry Configuration
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean useExponentialBackoff = true;
    private String[] retryableErrorCodes = {"08S01", "08003", "08006"}; // Connection errors
    
    // Audit & Logging
    private boolean enableAuditLogging = true;
    private String auditTableName; // Optional audit table
    private boolean logDataChanges = false; // Log actual data being written
    private boolean enableMetrics = true;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "INSERT"; // INSERT, UPDATE, DELETE, UPSERT
    private String targetSchema; // Database schema to write to
    private String targetTable; // Primary table for operations
    
    // Constructors
    public JdbcReceiverAdapterConfig() {}
    
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
    
    public String getInsertQuery() { return insertQuery; }
    public void setInsertQuery(String insertQuery) { this.insertQuery = insertQuery; }
    
    public String getUpdateQuery() { return updateQuery; }
    public void setUpdateQuery(String updateQuery) { this.updateQuery = updateQuery; }
    
    public String getDeleteQuery() { return deleteQuery; }
    public void setDeleteQuery(String deleteQuery) { this.deleteQuery = deleteQuery; }
    
    public String getUpsertQuery() { return upsertQuery; }
    public void setUpsertQuery(String upsertQuery) { this.upsertQuery = upsertQuery; }
    
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) { this.queryTimeoutSeconds = queryTimeoutSeconds; }
    
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public boolean isUseTransactions() { return useTransactions; }
    public void setUseTransactions(boolean useTransactions) { this.useTransactions = useTransactions; }
    
    public String getTransactionIsolationLevel() { return transactionIsolationLevel; }
    public void setTransactionIsolationLevel(String transactionIsolationLevel) { this.transactionIsolationLevel = transactionIsolationLevel; }
    
    public boolean isAutoCommit() { return autoCommit; }
    public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }
    
    public long getTransactionTimeoutMs() { return transactionTimeoutMs; }
    public void setTransactionTimeoutMs(long transactionTimeoutMs) { this.transactionTimeoutMs = transactionTimeoutMs; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public String getDataMapping() { return dataMapping; }
    public void setDataMapping(String dataMapping) { this.dataMapping = dataMapping; }
    
    public boolean isValidateData() { return validateData; }
    public void setValidateData(boolean validateData) { this.validateData = validateData; }
    
    public String getDataValidationRules() { return dataValidationRules; }
    public void setDataValidationRules(String dataValidationRules) { this.dataValidationRules = dataValidationRules; }
    
    public String getConflictResolutionStrategy() { return conflictResolutionStrategy; }
    public void setConflictResolutionStrategy(String conflictResolutionStrategy) { this.conflictResolutionStrategy = conflictResolutionStrategy; }
    
    public String getDuplicateKeyHandling() { return duplicateKeyHandling; }
    public void setDuplicateKeyHandling(String duplicateKeyHandling) { this.duplicateKeyHandling = duplicateKeyHandling; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxErrorThreshold() { return maxErrorThreshold; }
    public void setMaxErrorThreshold(int maxErrorThreshold) { this.maxErrorThreshold = maxErrorThreshold; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public boolean isUseConnectionPooling() { return useConnectionPooling; }
    public void setUseConnectionPooling(boolean useConnectionPooling) { this.useConnectionPooling = useConnectionPooling; }
    
    public boolean isEnableStatementCaching() { return enableStatementCaching; }
    public void setEnableStatementCaching(boolean enableStatementCaching) { this.enableStatementCaching = enableStatementCaching; }
    
    public int getStatementCacheSize() { return statementCacheSize; }
    public void setStatementCacheSize(int statementCacheSize) { this.statementCacheSize = statementCacheSize; }
    
    public boolean isAnalyzePerformance() { return analyzePerformance; }
    public void setAnalyzePerformance(boolean analyzePerformance) { this.analyzePerformance = analyzePerformance; }
    
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isUseExponentialBackoff() { return useExponentialBackoff; }
    public void setUseExponentialBackoff(boolean useExponentialBackoff) { this.useExponentialBackoff = useExponentialBackoff; }
    
    public String[] getRetryableErrorCodes() { return retryableErrorCodes; }
    public void setRetryableErrorCodes(String[] retryableErrorCodes) { this.retryableErrorCodes = retryableErrorCodes; }
    
    public boolean isEnableAuditLogging() { return enableAuditLogging; }
    public void setEnableAuditLogging(boolean enableAuditLogging) { this.enableAuditLogging = enableAuditLogging; }
    
    public String getAuditTableName() { return auditTableName; }
    public void setAuditTableName(String auditTableName) { this.auditTableName = auditTableName; }
    
    public boolean isLogDataChanges() { return logDataChanges; }
    public void setLogDataChanges(boolean logDataChanges) { this.logDataChanges = logDataChanges; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getTargetSchema() { return targetSchema; }
    public void setTargetSchema(String targetSchema) { this.targetSchema = targetSchema; }
    
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    
    // Getters and setters for reading configuration (compatibility methods)
    public String getSelectQuery() { return selectQuery; }
    public void setSelectQuery(String selectQuery) { this.selectQuery = selectQuery; }
    
    public String getIncrementalColumn() { return incrementalColumn; }
    public void setIncrementalColumn(String incrementalColumn) { this.incrementalColumn = incrementalColumn; }
    
    public Integer getFetchSize() { return fetchSize; }
    public void setFetchSize(Integer fetchSize) { this.fetchSize = fetchSize; }
    
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    @Override
    public String toString() {
        return String.format("JdbcReceiverAdapterConfig{url='%s', driver='%s', user='%s', operation='%s', batching=%s, transactions=%s}",
                jdbcUrl != null ? jdbcUrl.replaceAll("password=[^;&]*", "password=***") : null,
                driverClass, username, operationType, enableBatching, useTransactions);
    }
}