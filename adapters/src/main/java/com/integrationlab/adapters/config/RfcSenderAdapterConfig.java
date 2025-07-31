package com.integrationlab.adapters.config;

/**
 * Configuration for RFC Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on receiving RFC calls from SAP systems (acting as RFC server).
 */
public class RfcSenderAdapterConfig {

    // RFC Server Configuration (Middleware acts as RFC Server)
    private String serverName; // RFC server name registered in SAP
    private String serverHost; // Host where middleware RFC server runs
    private String serverPort; // Port for RFC server
    private String gatewayHost; // SAP gateway host
    private String gatewayService; // SAP gateway service
    private String programId; // Program ID registered in SAP gateway
    
    // SAP System Connection (for registration)
    private String sapSystemId; // SID of SAP system
    private String sapClientNumber;
    private String sapSystemNumber;
    private String sapApplicationServerHost;
    
    // Authentication for RFC Server
    private String username;
    private String password;
    private String language = "EN"; // Default language
    
    // RFC Function Configuration
    private String[] allowedRfcFunctions; // Array of allowed RFC function names
    private String rfcLibraryPath; // Path to SAP JCo library
    private boolean enableFunctionFiltering = true; // Only allow specified functions
    private String defaultFunctionModule; // Default function if not specified
    
    // RFC Server Settings
    private int maxConnections = 10; // Maximum concurrent RFC connections
    private int connectionTimeout = 30000; // 30 seconds
    private long idleTimeout = 300000; // 5 minutes
    private boolean enableConnectionPooling = true;
    private int workerThreads = 5; // Number of worker threads for processing
    
    // RFC Parameter Handling
    private String inputParameterMapping; // JSON mapping for input parameters
    private String outputParameterMapping; // JSON mapping for output parameters
    private String tableParameterMapping; // JSON mapping for table parameters
    private boolean validateParameters = true;
    private String parameterValidationSchema;
    
    // Transaction and Processing
    private String transactionHandling = "NONE"; // NONE, COMMIT, ROLLBACK
    private boolean supportTransactionalRfc = false;
    private long transactionTimeout = 300000; // 5 minutes
    
    // Error Handling for RFC Processing
    private String errorHandlingStrategy = "RFC_EXCEPTION";
    private String retryPolicy = "NONE"; // RFC calls are typically not retried by server
    private boolean logRfcCalls = true;
    private boolean logRfcParameters = false; // Security: don't log sensitive data
    private String errorLogDirectory;
    
    // Security and Authorization
    private String[] authorizedUsers; // Array of authorized SAP users
    private String[] authorizedSystems; // Array of authorized SAP systems
    private boolean enableUserAuthorization = false;
    private boolean enableSystemAuthorization = false;
    private String securityPolicy = "PERMISSIVE"; // PERMISSIVE, STRICT
    
    // Monitoring and Performance
    private boolean enableMetrics = true;
    private long slowRfcThresholdMs = 10000; // 10 seconds
    private boolean enablePerformanceLogging = false;
    private int maxConcurrentCalls = 50; // Maximum concurrent RFC calls
    
    // RFC Response Configuration
    private String responseFormat = "SAP_NATIVE"; // SAP_NATIVE, JSON, XML
    private boolean includeMetadata = false;
    private String responseEncoding = "UTF-8";
    
    // Business Context
    private String businessComponentId;
    private String sourceSystemId; // Identifier of source SAP system
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public RfcSenderAdapterConfig() {}
    
    public RfcSenderAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    
    public String getServerHost() { return serverHost; }
    public void setServerHost(String serverHost) { this.serverHost = serverHost; }
    
    public String getServerPort() { return serverPort; }
    public void setServerPort(String serverPort) { this.serverPort = serverPort; }
    
    public String getGatewayHost() { return gatewayHost; }
    public void setGatewayHost(String gatewayHost) { this.gatewayHost = gatewayHost; }
    
    public String getGatewayService() { return gatewayService; }
    public void setGatewayService(String gatewayService) { this.gatewayService = gatewayService; }
    
    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }
    
    public String getSapSystemId() { return sapSystemId; }
    public void setSapSystemId(String sapSystemId) { this.sapSystemId = sapSystemId; }
    
    public String getSapClientNumber() { return sapClientNumber; }
    public void setSapClientNumber(String sapClientNumber) { this.sapClientNumber = sapClientNumber; }
    
    public String getSapSystemNumber() { return sapSystemNumber; }
    public void setSapSystemNumber(String sapSystemNumber) { this.sapSystemNumber = sapSystemNumber; }
    
    public String getSapApplicationServerHost() { return sapApplicationServerHost; }
    public void setSapApplicationServerHost(String sapApplicationServerHost) { this.sapApplicationServerHost = sapApplicationServerHost; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String[] getAllowedRfcFunctions() { return allowedRfcFunctions; }
    public void setAllowedRfcFunctions(String[] allowedRfcFunctions) { this.allowedRfcFunctions = allowedRfcFunctions; }
    
    public String getRfcLibraryPath() { return rfcLibraryPath; }
    public void setRfcLibraryPath(String rfcLibraryPath) { this.rfcLibraryPath = rfcLibraryPath; }
    
    public boolean isEnableFunctionFiltering() { return enableFunctionFiltering; }
    public void setEnableFunctionFiltering(boolean enableFunctionFiltering) { this.enableFunctionFiltering = enableFunctionFiltering; }
    
    public String getDefaultFunctionModule() { return defaultFunctionModule; }
    public void setDefaultFunctionModule(String defaultFunctionModule) { this.defaultFunctionModule = defaultFunctionModule; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public int getWorkerThreads() { return workerThreads; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
    
    public String getInputParameterMapping() { return inputParameterMapping; }
    public void setInputParameterMapping(String inputParameterMapping) { this.inputParameterMapping = inputParameterMapping; }
    
    public String getOutputParameterMapping() { return outputParameterMapping; }
    public void setOutputParameterMapping(String outputParameterMapping) { this.outputParameterMapping = outputParameterMapping; }
    
    public String getTableParameterMapping() { return tableParameterMapping; }
    public void setTableParameterMapping(String tableParameterMapping) { this.tableParameterMapping = tableParameterMapping; }
    
    public boolean isValidateParameters() { return validateParameters; }
    public void setValidateParameters(boolean validateParameters) { this.validateParameters = validateParameters; }
    
    public String getParameterValidationSchema() { return parameterValidationSchema; }
    public void setParameterValidationSchema(String parameterValidationSchema) { this.parameterValidationSchema = parameterValidationSchema; }
    
    public String getTransactionHandling() { return transactionHandling; }
    public void setTransactionHandling(String transactionHandling) { this.transactionHandling = transactionHandling; }
    
    public boolean isSupportTransactionalRfc() { return supportTransactionalRfc; }
    public void setSupportTransactionalRfc(boolean supportTransactionalRfc) { this.supportTransactionalRfc = supportTransactionalRfc; }
    
    public long getTransactionTimeout() { return transactionTimeout; }
    public void setTransactionTimeout(long transactionTimeout) { this.transactionTimeout = transactionTimeout; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public boolean isLogRfcCalls() { return logRfcCalls; }
    public void setLogRfcCalls(boolean logRfcCalls) { this.logRfcCalls = logRfcCalls; }
    
    public boolean isLogRfcParameters() { return logRfcParameters; }
    public void setLogRfcParameters(boolean logRfcParameters) { this.logRfcParameters = logRfcParameters; }
    
    public String getErrorLogDirectory() { return errorLogDirectory; }
    public void setErrorLogDirectory(String errorLogDirectory) { this.errorLogDirectory = errorLogDirectory; }
    
    public String[] getAuthorizedUsers() { return authorizedUsers; }
    public void setAuthorizedUsers(String[] authorizedUsers) { this.authorizedUsers = authorizedUsers; }
    
    public String[] getAuthorizedSystems() { return authorizedSystems; }
    public void setAuthorizedSystems(String[] authorizedSystems) { this.authorizedSystems = authorizedSystems; }
    
    public boolean isEnableUserAuthorization() { return enableUserAuthorization; }
    public void setEnableUserAuthorization(boolean enableUserAuthorization) { this.enableUserAuthorization = enableUserAuthorization; }
    
    public boolean isEnableSystemAuthorization() { return enableSystemAuthorization; }
    public void setEnableSystemAuthorization(boolean enableSystemAuthorization) { this.enableSystemAuthorization = enableSystemAuthorization; }
    
    public String getSecurityPolicy() { return securityPolicy; }
    public void setSecurityPolicy(String securityPolicy) { this.securityPolicy = securityPolicy; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowRfcThresholdMs() { return slowRfcThresholdMs; }
    public void setSlowRfcThresholdMs(long slowRfcThresholdMs) { this.slowRfcThresholdMs = slowRfcThresholdMs; }
    
    public boolean isEnablePerformanceLogging() { return enablePerformanceLogging; }
    public void setEnablePerformanceLogging(boolean enablePerformanceLogging) { this.enablePerformanceLogging = enablePerformanceLogging; }
    
    public int getMaxConcurrentCalls() { return maxConcurrentCalls; }
    public void setMaxConcurrentCalls(int maxConcurrentCalls) { this.maxConcurrentCalls = maxConcurrentCalls; }
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public boolean isIncludeMetadata() { return includeMetadata; }
    public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }
    
    public String getResponseEncoding() { return responseEncoding; }
    public void setResponseEncoding(String responseEncoding) { this.responseEncoding = responseEncoding; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getSourceSystemId() { return sourceSystemId; }
    public void setSourceSystemId(String sourceSystemId) { this.sourceSystemId = sourceSystemId; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getRfcFunctionName() { 
        return allowedRfcFunctions != null && allowedRfcFunctions.length > 0 ? allowedRfcFunctions[0] : null; 
    }
    public String getInputParameters() { return inputParameterMapping; }
    public String getOutputParameters() { return outputParameterMapping; }
    public String getTableParameters() { return tableParameterMapping; }
    public String getPortNumber() { return serverPort; }
    public String getConnectionType() { return "RFC_SERVER"; }
    
    @Override
    public String toString() {
        return String.format("RfcSenderAdapterConfig{serverName='%s', programId='%s', sapSystem='%s', maxConnections=%d}",
                serverName, programId, sapSystemId, maxConnections);
    }
}