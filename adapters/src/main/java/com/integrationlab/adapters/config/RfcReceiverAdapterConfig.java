package com.integrationlab.adapters.config;

/**
 * Configuration for RFC Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on calling RFC functions in target SAP systems (acting as RFC client).
 */
public class RfcReceiverAdapterConfig {

    // Target SAP System Identification
    private String targetSapSystemId; // SID of target SAP system
    private String targetSapClientNumber;
    private String targetSapSystemNumber;
    
    // Target Connection Details
    private String targetSapApplicationServerHost;
    private String targetSapGatewayHost;
    private String targetSapGatewayService;
    private String targetPortNumber;
    private String connectionType = "TCP"; // TCP, Gateway
    
    // Target Authentication
    private String targetUsername;
    private String targetPassword;
    private String targetLanguage = "EN"; // Default language
    
    // RFC Function Details for Target System
    private String targetRfcFunctionName;
    private String rfcLibraryPath; // Path to SAP JCo library
    private String inputParameterMapping; // JSON mapping for input parameters
    private String outputParameterMapping; // JSON mapping for output parameters
    private String tableParameterMapping; // JSON mapping for table parameters
    
    // Connection Pool Settings for Target
    private int maxConnections = 10;
    private int minConnections = 1;
    private long connectionTimeout = 30000; // 30 seconds
    private long idleTimeout = 300000; // 5 minutes
    private boolean enableConnectionPooling = true;
    
    // RFC Call Configuration
    private String callType = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS, TRANSACTIONAL
    private long rfcTimeout = 60000; // 60 seconds
    private boolean enableBatching = false; // Batch multiple RFC calls
    private int batchSize = 10;
    private long batchTimeoutMs = 30000; // 30 seconds
    
    // Parameter and Data Handling
    private boolean validateInputParameters = true;
    private String inputValidationSchema;
    private boolean validateOutputParameters = false;
    private String outputValidationSchema;
    private String dataTransformation; // Custom transformation logic
    
    // Transaction and Processing for Target
    private String transactionHandling = "NONE"; // NONE, COMMIT, ROLLBACK
    private boolean supportTransactionalRfc = false;
    private long transactionTimeout = 300000; // 5 minutes
    private String commitHandling = "AUTO"; // AUTO, MANUAL, NEVER
    
    // Error Handling for RFC Calls
    private String errorHandlingStrategy = "FAIL_FAST";
    private String retryPolicy = "EXPONENTIAL_BACKOFF";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean continueOnError = false;
    private String errorLogDirectory;
    
    // Response Processing
    private String responseFormat = "SAP_NATIVE"; // SAP_NATIVE, JSON, XML
    private String responseHandling = "AUTO"; // AUTO, MANUAL
    private boolean logSuccessfulCalls = false;
    private boolean logFailedCalls = true;
    private boolean logRfcParameters = false; // Security: don't log sensitive data
    
    // Performance and Monitoring
    private boolean enableMetrics = true;
    private long slowRfcThresholdMs = 10000; // 10 seconds
    private int maxConcurrentCalls = 5; // Number of concurrent RFC calls
    private boolean enablePerformanceLogging = false;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target SAP system
    private String operationType = "RFC_CALL"; // RFC_CALL, RFC_BATCH, RFC_TRANSACTION
    private boolean idempotent = false;
    private String idempotencyStrategy = "parameters"; // parameters, timestamp, custom
    
    // RFC Function Metadata
    private String functionMetadata; // RFC function interface metadata
    private boolean autoDiscoverMetadata = true;
    private String metadataCacheTimeout = "3600"; // 1 hour cache timeout
    
    // Security and SSL (for secure RFC)
    private String sslConfig;
    private String certificateId;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId;
    private String targetSystemId; // Identifier of target SAP system
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public RfcReceiverAdapterConfig() {}
    
    public RfcReceiverAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getTargetSapSystemId() { return targetSapSystemId; }
    public void setTargetSapSystemId(String targetSapSystemId) { this.targetSapSystemId = targetSapSystemId; }
    
    public String getTargetSapClientNumber() { return targetSapClientNumber; }
    public void setTargetSapClientNumber(String targetSapClientNumber) { this.targetSapClientNumber = targetSapClientNumber; }
    
    public String getTargetSapSystemNumber() { return targetSapSystemNumber; }
    public void setTargetSapSystemNumber(String targetSapSystemNumber) { this.targetSapSystemNumber = targetSapSystemNumber; }
    
    public String getTargetSapApplicationServerHost() { return targetSapApplicationServerHost; }
    public void setTargetSapApplicationServerHost(String targetSapApplicationServerHost) { this.targetSapApplicationServerHost = targetSapApplicationServerHost; }
    
    public String getTargetSapGatewayHost() { return targetSapGatewayHost; }
    public void setTargetSapGatewayHost(String targetSapGatewayHost) { this.targetSapGatewayHost = targetSapGatewayHost; }
    
    public String getTargetSapGatewayService() { return targetSapGatewayService; }
    public void setTargetSapGatewayService(String targetSapGatewayService) { this.targetSapGatewayService = targetSapGatewayService; }
    
    public String getTargetPortNumber() { return targetPortNumber; }
    public void setTargetPortNumber(String targetPortNumber) { this.targetPortNumber = targetPortNumber; }
    
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    
    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }
    
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    
    public String getTargetRfcFunctionName() { return targetRfcFunctionName; }
    public void setTargetRfcFunctionName(String targetRfcFunctionName) { this.targetRfcFunctionName = targetRfcFunctionName; }
    
    public String getRfcLibraryPath() { return rfcLibraryPath; }
    public void setRfcLibraryPath(String rfcLibraryPath) { this.rfcLibraryPath = rfcLibraryPath; }
    
    public String getInputParameterMapping() { return inputParameterMapping; }
    public void setInputParameterMapping(String inputParameterMapping) { this.inputParameterMapping = inputParameterMapping; }
    
    public String getOutputParameterMapping() { return outputParameterMapping; }
    public void setOutputParameterMapping(String outputParameterMapping) { this.outputParameterMapping = outputParameterMapping; }
    
    public String getTableParameterMapping() { return tableParameterMapping; }
    public void setTableParameterMapping(String tableParameterMapping) { this.tableParameterMapping = tableParameterMapping; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getMinConnections() { return minConnections; }
    public void setMinConnections(int minConnections) { this.minConnections = minConnections; }
    
    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    
    public long getRfcTimeout() { return rfcTimeout; }
    public void setRfcTimeout(long rfcTimeout) { this.rfcTimeout = rfcTimeout; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public boolean isValidateInputParameters() { return validateInputParameters; }
    public void setValidateInputParameters(boolean validateInputParameters) { this.validateInputParameters = validateInputParameters; }
    
    public String getInputValidationSchema() { return inputValidationSchema; }
    public void setInputValidationSchema(String inputValidationSchema) { this.inputValidationSchema = inputValidationSchema; }
    
    public boolean isValidateOutputParameters() { return validateOutputParameters; }
    public void setValidateOutputParameters(boolean validateOutputParameters) { this.validateOutputParameters = validateOutputParameters; }
    
    public String getOutputValidationSchema() { return outputValidationSchema; }
    public void setOutputValidationSchema(String outputValidationSchema) { this.outputValidationSchema = outputValidationSchema; }
    
    public String getDataTransformation() { return dataTransformation; }
    public void setDataTransformation(String dataTransformation) { this.dataTransformation = dataTransformation; }
    
    public String getTransactionHandling() { return transactionHandling; }
    public void setTransactionHandling(String transactionHandling) { this.transactionHandling = transactionHandling; }
    
    public boolean isSupportTransactionalRfc() { return supportTransactionalRfc; }
    public void setSupportTransactionalRfc(boolean supportTransactionalRfc) { this.supportTransactionalRfc = supportTransactionalRfc; }
    
    public long getTransactionTimeout() { return transactionTimeout; }
    public void setTransactionTimeout(long transactionTimeout) { this.transactionTimeout = transactionTimeout; }
    
    public String getCommitHandling() { return commitHandling; }
    public void setCommitHandling(String commitHandling) { this.commitHandling = commitHandling; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public String getErrorLogDirectory() { return errorLogDirectory; }
    public void setErrorLogDirectory(String errorLogDirectory) { this.errorLogDirectory = errorLogDirectory; }
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public String getResponseHandling() { return responseHandling; }
    public void setResponseHandling(String responseHandling) { this.responseHandling = responseHandling; }
    
    public boolean isLogSuccessfulCalls() { return logSuccessfulCalls; }
    public void setLogSuccessfulCalls(boolean logSuccessfulCalls) { this.logSuccessfulCalls = logSuccessfulCalls; }
    
    public boolean isLogFailedCalls() { return logFailedCalls; }
    public void setLogFailedCalls(boolean logFailedCalls) { this.logFailedCalls = logFailedCalls; }
    
    public boolean isLogRfcParameters() { return logRfcParameters; }
    public void setLogRfcParameters(boolean logRfcParameters) { this.logRfcParameters = logRfcParameters; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowRfcThresholdMs() { return slowRfcThresholdMs; }
    public void setSlowRfcThresholdMs(long slowRfcThresholdMs) { this.slowRfcThresholdMs = slowRfcThresholdMs; }
    
    public int getMaxConcurrentCalls() { return maxConcurrentCalls; }
    public void setMaxConcurrentCalls(int maxConcurrentCalls) { this.maxConcurrentCalls = maxConcurrentCalls; }
    
    public boolean isEnablePerformanceLogging() { return enablePerformanceLogging; }
    public void setEnablePerformanceLogging(boolean enablePerformanceLogging) { this.enablePerformanceLogging = enablePerformanceLogging; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyStrategy() { return idempotencyStrategy; }
    public void setIdempotencyStrategy(String idempotencyStrategy) { this.idempotencyStrategy = idempotencyStrategy; }
    
    public String getFunctionMetadata() { return functionMetadata; }
    public void setFunctionMetadata(String functionMetadata) { this.functionMetadata = functionMetadata; }
    
    public boolean isAutoDiscoverMetadata() { return autoDiscoverMetadata; }
    public void setAutoDiscoverMetadata(boolean autoDiscoverMetadata) { this.autoDiscoverMetadata = autoDiscoverMetadata; }
    
    public String getMetadataCacheTimeout() { return metadataCacheTimeout; }
    public void setMetadataCacheTimeout(String metadataCacheTimeout) { this.metadataCacheTimeout = metadataCacheTimeout; }
    
    public String getSslConfig() { return sslConfig; }
    public void setSslConfig(String sslConfig) { this.sslConfig = sslConfig; }
    
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    
    public String getKeystorePath() { return keystorePath; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
    
    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
    
    public String getTruststorePath() { return truststorePath; }
    public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
    
    public String getTruststorePassword() { return truststorePassword; }
    public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public String getTargetSystemId() { return targetSystemId; }
    public void setTargetSystemId(String targetSystemId) { this.targetSystemId = targetSystemId; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getSapSystemId() { return targetSapSystemId; }
    public String getSapClientNumber() { return targetSapClientNumber; }
    public String getSapSystemNumber() { return targetSapSystemNumber; }
    public String getSapApplicationServerHost() { return targetSapApplicationServerHost; }
    public String getSapGatewayHost() { return targetSapGatewayHost; }
    public String getSapGatewayService() { return targetSapGatewayService; }
    public String getPortNumber() { return targetPortNumber; }
    public String getUsername() { return targetUsername; }
    public String getPassword() { return targetPassword; }
    public String getLanguage() { return targetLanguage; }
    public String getRfcFunctionName() { return targetRfcFunctionName; }
    public String getInputParameters() { return inputParameterMapping; }
    public String getOutputParameters() { return outputParameterMapping; }
    public String getTableParameters() { return tableParameterMapping; }
    public String getErrorHandling() { return errorHandlingStrategy; }
    
    @Override
    public String toString() {
        return String.format("RfcReceiverAdapterConfig{targetSystem='%s', function='%s', callType='%s', pooling=%s}",
                targetSapSystemId, targetRfcFunctionName, callType, enableConnectionPooling);
    }
}