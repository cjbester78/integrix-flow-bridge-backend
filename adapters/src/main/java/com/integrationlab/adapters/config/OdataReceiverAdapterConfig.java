package com.integrationlab.adapters.config;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for OData Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on sending/updating data to OData services.
 */
public class OdataReceiverAdapterConfig {

    // Target OData Service Configuration
    private String targetServiceEndpointUrl;
    private String targetServiceName;
    private String targetServiceVersion;
    private String targetMetadataUrl;
    private String targetBaseUrl;
    
    // Target Entity and Operation Configuration
    private String targetEntitySet;
    private String targetEntityType;
    private String operation = "POST"; // GET, POST, PUT, PATCH, DELETE
    private String customQueryParameters;
    
    // Target Authentication
    private String authenticationType = "none"; // none, basic, oauth2, saml
    
    // Basic Authentication
    private String targetUsername;
    private String targetPassword;
    
    // OAuth2 Configuration
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String scope;
    private String accessToken;
    private long tokenExpirationTime;
    private boolean autoRefreshToken = true;
    
    // SAML Authentication
    private String samlAssertion;
    private String samlIssuer;
    
    // Request Configuration
    private String requestFormat = "JSON"; // JSON, XML
    private String contentType = "application/json";
    private String acceptHeader = "application/json";
    private Map<String, String> customHeaders = new HashMap<>();
    
    // Target Connection Settings
    private int connectionTimeout = 30000; // milliseconds
    private int readTimeout = 60000; // milliseconds
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    
    // Data Operations Configuration
    private String insertOperation = "POST"; // Operation for creating new entities
    private String updateOperation = "PATCH"; // Operation for updating entities
    private String deleteOperation = "DELETE"; // Operation for deleting entities
    private String conflictResolution = "CLIENT_WINS"; // CLIENT_WINS, SERVER_WINS, MERGE
    private boolean useUpsert = false; // Use UPSERT operation when available
    
    // Batch Processing
    private boolean enableBatchProcessing = false;
    private int batchSize = 100;
    private long batchTimeoutMs = 30000; // 30 seconds
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Entity Key Handling
    private String entityKeyFields; // Comma-separated list of key fields
    private String keyMappingStrategy = "AUTO"; // AUTO, MANUAL, GUID
    private boolean generateKeys = false; // Generate keys if not provided
    private String keyGenerationPattern; // Pattern for key generation
    
    // Data Mapping and Transformation
    private String dataMapping; // Custom data mapping configuration
    private String fieldMapping; // Field-level mapping configuration
    private boolean validatePayload = true;
    private String payloadValidationSchema;
    private String dataTransformation; // Custom transformation logic
    
    // Error Handling for Operations
    private String errorHandlingStrategy = "FAIL_FAST";
    private String retryPolicy = "EXPONENTIAL_BACKOFF";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean continueOnError = false;
    private String errorLogDirectory; // Directory for error logs
    
    // Response Handling
    private String responseFormat = "JSON"; // JSON, XML
    private String responseHandling = "AUTO"; // AUTO, MANUAL
    private boolean logSuccessResponses = false;
    private boolean logErrorResponses = true;
    
    // Performance and Monitoring
    private boolean enableMetrics = true;
    private long slowOperationThresholdMs = 10000; // 10 seconds
    private boolean logRequestResponse = false;
    private int maxConcurrentRequests = 5; // Number of concurrent requests
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target OData system
    private String operationType = "CRUD"; // CRUD, BULK, STREAMING
    private boolean idempotent = false;
    private String idempotencyStrategy = "entityKey"; // entityKey, etag, custom
    
    // ETag and Concurrency
    private boolean useETags = true; // Use ETags for optimistic concurrency
    private String etagHandling = "AUTO"; // AUTO, MANUAL, IGNORE
    private String concurrencyMode = "OPTIMISTIC"; // OPTIMISTIC, PESSIMISTIC, NONE
    
    // Certificate and SSL
    private String sslConfig;
    private String certificateId;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
    private boolean verifySSL = true;
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId;
    private String targetSystemId; // Identifier of target OData system
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public OdataReceiverAdapterConfig() {}
    
    public OdataReceiverAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getTargetServiceEndpointUrl() { return targetServiceEndpointUrl; }
    public void setTargetServiceEndpointUrl(String targetServiceEndpointUrl) { this.targetServiceEndpointUrl = targetServiceEndpointUrl; }
    
    public String getTargetServiceName() { return targetServiceName; }
    public void setTargetServiceName(String targetServiceName) { this.targetServiceName = targetServiceName; }
    
    public String getTargetServiceVersion() { return targetServiceVersion; }
    public void setTargetServiceVersion(String targetServiceVersion) { this.targetServiceVersion = targetServiceVersion; }
    
    public String getTargetMetadataUrl() { return targetMetadataUrl; }
    public void setTargetMetadataUrl(String targetMetadataUrl) { this.targetMetadataUrl = targetMetadataUrl; }
    
    public String getTargetBaseUrl() { return targetBaseUrl; }
    public void setTargetBaseUrl(String targetBaseUrl) { this.targetBaseUrl = targetBaseUrl; }
    
    public String getTargetEntitySet() { return targetEntitySet; }
    public void setTargetEntitySet(String targetEntitySet) { this.targetEntitySet = targetEntitySet; }
    
    public String getTargetEntityType() { return targetEntityType; }
    public void setTargetEntityType(String targetEntityType) { this.targetEntityType = targetEntityType; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getCustomQueryParameters() { return customQueryParameters; }
    public void setCustomQueryParameters(String customQueryParameters) { this.customQueryParameters = customQueryParameters; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    
    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    
    public String getTokenUrl() { return tokenUrl; }
    public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }
    
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public long getTokenExpirationTime() { return tokenExpirationTime; }
    public void setTokenExpirationTime(long tokenExpirationTime) { this.tokenExpirationTime = tokenExpirationTime; }
    
    public boolean isAutoRefreshToken() { return autoRefreshToken; }
    public void setAutoRefreshToken(boolean autoRefreshToken) { this.autoRefreshToken = autoRefreshToken; }
    
    public String getSamlAssertion() { return samlAssertion; }
    public void setSamlAssertion(String samlAssertion) { this.samlAssertion = samlAssertion; }
    
    public String getSamlIssuer() { return samlIssuer; }
    public void setSamlIssuer(String samlIssuer) { this.samlIssuer = samlIssuer; }
    
    public String getRequestFormat() { return requestFormat; }
    public void setRequestFormat(String requestFormat) { this.requestFormat = requestFormat; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getAcceptHeader() { return acceptHeader; }
    public void setAcceptHeader(String acceptHeader) { this.acceptHeader = acceptHeader; }
    
    public Map<String, String> getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(Map<String, String> customHeaders) { this.customHeaders = customHeaders; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
    public String getProxyHost() { return proxyHost; }
    public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }
    
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
    
    public String getProxyUsername() { return proxyUsername; }
    public void setProxyUsername(String proxyUsername) { this.proxyUsername = proxyUsername; }
    
    public String getProxyPassword() { return proxyPassword; }
    public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }
    
    public String getInsertOperation() { return insertOperation; }
    public void setInsertOperation(String insertOperation) { this.insertOperation = insertOperation; }
    
    public String getUpdateOperation() { return updateOperation; }
    public void setUpdateOperation(String updateOperation) { this.updateOperation = updateOperation; }
    
    public String getDeleteOperation() { return deleteOperation; }
    public void setDeleteOperation(String deleteOperation) { this.deleteOperation = deleteOperation; }
    
    public String getConflictResolution() { return conflictResolution; }
    public void setConflictResolution(String conflictResolution) { this.conflictResolution = conflictResolution; }
    
    public boolean isUseUpsert() { return useUpsert; }
    public void setUseUpsert(boolean useUpsert) { this.useUpsert = useUpsert; }
    
    public boolean isEnableBatchProcessing() { return enableBatchProcessing; }
    public void setEnableBatchProcessing(boolean enableBatchProcessing) { this.enableBatchProcessing = enableBatchProcessing; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public String getEntityKeyFields() { return entityKeyFields; }
    public void setEntityKeyFields(String entityKeyFields) { this.entityKeyFields = entityKeyFields; }
    
    public String getKeyMappingStrategy() { return keyMappingStrategy; }
    public void setKeyMappingStrategy(String keyMappingStrategy) { this.keyMappingStrategy = keyMappingStrategy; }
    
    public boolean isGenerateKeys() { return generateKeys; }
    public void setGenerateKeys(boolean generateKeys) { this.generateKeys = generateKeys; }
    
    public String getKeyGenerationPattern() { return keyGenerationPattern; }
    public void setKeyGenerationPattern(String keyGenerationPattern) { this.keyGenerationPattern = keyGenerationPattern; }
    
    public String getDataMapping() { return dataMapping; }
    public void setDataMapping(String dataMapping) { this.dataMapping = dataMapping; }
    
    public String getFieldMapping() { return fieldMapping; }
    public void setFieldMapping(String fieldMapping) { this.fieldMapping = fieldMapping; }
    
    public boolean isValidatePayload() { return validatePayload; }
    public void setValidatePayload(boolean validatePayload) { this.validatePayload = validatePayload; }
    
    public String getPayloadValidationSchema() { return payloadValidationSchema; }
    public void setPayloadValidationSchema(String payloadValidationSchema) { this.payloadValidationSchema = payloadValidationSchema; }
    
    public String getDataTransformation() { return dataTransformation; }
    public void setDataTransformation(String dataTransformation) { this.dataTransformation = dataTransformation; }
    
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
    
    public boolean isLogSuccessResponses() { return logSuccessResponses; }
    public void setLogSuccessResponses(boolean logSuccessResponses) { this.logSuccessResponses = logSuccessResponses; }
    
    public boolean isLogErrorResponses() { return logErrorResponses; }
    public void setLogErrorResponses(boolean logErrorResponses) { this.logErrorResponses = logErrorResponses; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowOperationThresholdMs() { return slowOperationThresholdMs; }
    public void setSlowOperationThresholdMs(long slowOperationThresholdMs) { this.slowOperationThresholdMs = slowOperationThresholdMs; }
    
    public boolean isLogRequestResponse() { return logRequestResponse; }
    public void setLogRequestResponse(boolean logRequestResponse) { this.logRequestResponse = logRequestResponse; }
    
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyStrategy() { return idempotencyStrategy; }
    public void setIdempotencyStrategy(String idempotencyStrategy) { this.idempotencyStrategy = idempotencyStrategy; }
    
    public boolean isUseETags() { return useETags; }
    public void setUseETags(boolean useETags) { this.useETags = useETags; }
    
    public String getEtagHandling() { return etagHandling; }
    public void setEtagHandling(String etagHandling) { this.etagHandling = etagHandling; }
    
    public String getConcurrencyMode() { return concurrencyMode; }
    public void setConcurrencyMode(String concurrencyMode) { this.concurrencyMode = concurrencyMode; }
    
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
    
    public boolean isVerifySSL() { return verifySSL; }
    public void setVerifySSL(boolean verifySSL) { this.verifySSL = verifySSL; }
    
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
    public String getServiceEndpointUrl() { return targetServiceEndpointUrl; }
    public String getServiceName() { return targetServiceName; }
    public String getEntitySet() { return targetEntitySet; }
    public String getEntityType() { return targetEntityType; }
    public String getUsername() { return targetUsername; }
    public String getPassword() { return targetPassword; }
    public String getBatchProcessing() { return String.valueOf(enableBatchProcessing); }
    public String getProxySettings() { return proxyHost != null ? proxyHost + ":" + proxyPort : null; }
    
    @Override
    public String toString() {
        return String.format("OdataReceiverAdapterConfig{targetEndpoint='%s', entitySet='%s', operation='%s', auth='%s'}",
                targetServiceEndpointUrl, targetEntitySet, operation, authenticationType);
    }
}