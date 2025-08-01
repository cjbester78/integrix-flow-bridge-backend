package com.integrationlab.adapters.config;

import java.util.Map;

/**
 * Configuration for REST Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on connecting TO external REST APIs to push/send data.
 */
public class RestReceiverAdapterConfig {
    
    // Target REST API Connection Details
    private String baseEndpointUrl; // Base URL of the target REST API
    private String resourcePath; // Specific resource path for sending data
    private String endpointUrl; // Full endpoint URL (computed or provided)
    private String apiVersion;
    
    // Request Configuration for Data Sending
    private HttpMethod httpMethod = HttpMethod.POST; // Usually POST/PUT for sending data
    private String contentType = "application/json"; // What data format we send
    private String acceptHeader = "application/json"; // What response format we expect
    private Map<String, String> headers;
    private String customHeaders; // String format: "key1:value1,key2:value2"
    private int connectionTimeout = 30; // seconds
    private int readTimeout = 60; // seconds
    private int timeoutMillis = 30000; // milliseconds (for backward compatibility)
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    private String requestPayloadFormat; // Format for request payload
    private String responseFormat; // Expected response format
    
    // Flow Processing Mode
    private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
    private long responseTimeout = 30000; // Timeout for synchronous response (milliseconds)
    private boolean waitForResponse = true; // Whether to wait for response in sync mode
    private String asyncCorrelationId; // Correlation ID for async processing
    
    // Authentication for accessing target REST API
    private AuthenticationType authenticationType = AuthenticationType.NONE;
    private String basicUsername;
    private String basicPassword;
    private String bearerToken;
    private String apiKey;
    private String apiKeyName;
    private String apiKeyValue;
    private String apiKeyHeaderName;
    private String jwtToken;
    private String jwtAlgorithm;
    private String oauthAccessToken;
    private String oauthTokenUrl;
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthScope;
    
    // SSL Configuration for HTTPS endpoints
    private String sslKeyStorePath;
    private String sslKeyStorePassword;
    private String sslTrustStorePath;
    private String sslTrustStorePassword;
    private String sslConfig;
    private String certificateId;
    
    // Proxy Configuration (if needed to reach external APIs)
    private boolean useProxy = false;
    private String proxyServer;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    
    // Data Sending Configuration
    private String requestHeaders; // Additional headers for requests
    private String requestTemplate; // Template for request body transformation
    private boolean validateRequest = true;
    private String requestValidationRules;
    private boolean validateResponse = true;
    private String responseValidationRules;
    
    // Batch Processing
    private Integer batchSize; // Number of records to send in one request
    private boolean enableBatching = false;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Error Handling and Response Processing
    private String responseHandling; // How to handle responses
    private String errorHandling; // Error handling strategy
    private String retryPolicy; // Retry configuration
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean continueOnError = false;
    private String errorHandlingStrategy = "FAIL_FAST";
    
    // Rate Limiting
    private String rateLimits; // Rate limiting configuration
    private long requestIntervalMs = 0; // Minimum interval between requests
    private int maxConcurrentRequests = 10;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "CREATE"; // CREATE, UPDATE, DELETE, UPSERT
    private boolean idempotent = false; // Whether requests are idempotent
    private String idempotencyKey; // Header name for idempotency key
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logSlowRequests = true;
    private long slowRequestThresholdMs = 5000;
    private boolean logRequestPayload = false; // Log actual data being sent
    private boolean logResponsePayload = false; // Log actual response data
    
    // Constructors
    public RestReceiverAdapterConfig() {}
    
    // Getters and Setters
    public String getBaseEndpointUrl() { return baseEndpointUrl; }
    public void setBaseEndpointUrl(String baseEndpointUrl) { this.baseEndpointUrl = baseEndpointUrl; }
    
    public String getResourcePath() { return resourcePath; }
    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    
    public HttpMethod getHttpMethod() { return httpMethod; }
    public void setHttpMethod(HttpMethod httpMethod) { this.httpMethod = httpMethod; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getAcceptHeader() { return acceptHeader; }
    public void setAcceptHeader(String acceptHeader) { this.acceptHeader = acceptHeader; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public String getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(String customHeaders) { this.customHeaders = customHeaders; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
    public int getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public String getRequestPayloadFormat() { return requestPayloadFormat; }
    public void setRequestPayloadFormat(String requestPayloadFormat) { this.requestPayloadFormat = requestPayloadFormat; }
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public AuthenticationType getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(AuthenticationType authenticationType) { this.authenticationType = authenticationType; }
    
    public String getBasicUsername() { return basicUsername; }
    public void setBasicUsername(String basicUsername) { this.basicUsername = basicUsername; }
    
    public String getBasicPassword() { return basicPassword; }
    public void setBasicPassword(String basicPassword) { this.basicPassword = basicPassword; }
    
    public String getBearerToken() { return bearerToken; }
    public void setBearerToken(String bearerToken) { this.bearerToken = bearerToken; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getApiKeyName() { return apiKeyName; }
    public void setApiKeyName(String apiKeyName) { this.apiKeyName = apiKeyName; }
    
    public String getApiKeyValue() { return apiKeyValue; }
    public void setApiKeyValue(String apiKeyValue) { this.apiKeyValue = apiKeyValue; }
    
    public String getApiKeyHeaderName() { return apiKeyHeaderName; }
    public void setApiKeyHeaderName(String apiKeyHeaderName) { this.apiKeyHeaderName = apiKeyHeaderName; }
    
    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }
    
    public String getJwtAlgorithm() { return jwtAlgorithm; }
    public void setJwtAlgorithm(String jwtAlgorithm) { this.jwtAlgorithm = jwtAlgorithm; }
    
    public String getOauthAccessToken() { return oauthAccessToken; }
    public void setOauthAccessToken(String oauthAccessToken) { this.oauthAccessToken = oauthAccessToken; }
    
    public String getOauthTokenUrl() { return oauthTokenUrl; }
    public void setOauthTokenUrl(String oauthTokenUrl) { this.oauthTokenUrl = oauthTokenUrl; }
    
    public String getOauthClientId() { return oauthClientId; }
    public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }
    
    public String getOauthClientSecret() { return oauthClientSecret; }
    public void setOauthClientSecret(String oauthClientSecret) { this.oauthClientSecret = oauthClientSecret; }
    
    public String getOauthScope() { return oauthScope; }
    public void setOauthScope(String oauthScope) { this.oauthScope = oauthScope; }
    
    public String getSslKeyStorePath() { return sslKeyStorePath; }
    public void setSslKeyStorePath(String sslKeyStorePath) { this.sslKeyStorePath = sslKeyStorePath; }
    
    public String getSslKeyStorePassword() { return sslKeyStorePassword; }
    public void setSslKeyStorePassword(String sslKeyStorePassword) { this.sslKeyStorePassword = sslKeyStorePassword; }
    
    public String getSslTrustStorePath() { return sslTrustStorePath; }
    public void setSslTrustStorePath(String sslTrustStorePath) { this.sslTrustStorePath = sslTrustStorePath; }
    
    public String getSslTrustStorePassword() { return sslTrustStorePassword; }
    public void setSslTrustStorePassword(String sslTrustStorePassword) { this.sslTrustStorePassword = sslTrustStorePassword; }
    
    public String getSslConfig() { return sslConfig; }
    public void setSslConfig(String sslConfig) { this.sslConfig = sslConfig; }
    
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    
    public boolean isUseProxy() { return useProxy; }
    public void setUseProxy(boolean useProxy) { this.useProxy = useProxy; }
    
    public String getProxyServer() { return proxyServer; }
    public void setProxyServer(String proxyServer) { this.proxyServer = proxyServer; }
    
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
    
    public String getProxyUsername() { return proxyUsername; }
    public void setProxyUsername(String proxyUsername) { this.proxyUsername = proxyUsername; }
    
    public String getProxyPassword() { return proxyPassword; }
    public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }
    
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    
    public String getRequestTemplate() { return requestTemplate; }
    public void setRequestTemplate(String requestTemplate) { this.requestTemplate = requestTemplate; }
    
    public boolean isValidateRequest() { return validateRequest; }
    public void setValidateRequest(boolean validateRequest) { this.validateRequest = validateRequest; }
    
    public String getRequestValidationRules() { return requestValidationRules; }
    public void setRequestValidationRules(String requestValidationRules) { this.requestValidationRules = requestValidationRules; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public String getResponseValidationRules() { return responseValidationRules; }
    public void setResponseValidationRules(String responseValidationRules) { this.responseValidationRules = responseValidationRules; }
    
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public String getResponseHandling() { return responseHandling; }
    public void setResponseHandling(String responseHandling) { this.responseHandling = responseHandling; }
    
    public String getErrorHandling() { return errorHandling; }
    public void setErrorHandling(String errorHandling) { this.errorHandling = errorHandling; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getRateLimits() { return rateLimits; }
    public void setRateLimits(String rateLimits) { this.rateLimits = rateLimits; }
    
    public long getRequestIntervalMs() { return requestIntervalMs; }
    public void setRequestIntervalMs(long requestIntervalMs) { this.requestIntervalMs = requestIntervalMs; }
    
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogSlowRequests() { return logSlowRequests; }
    public void setLogSlowRequests(boolean logSlowRequests) { this.logSlowRequests = logSlowRequests; }
    
    public long getSlowRequestThresholdMs() { return slowRequestThresholdMs; }
    public void setSlowRequestThresholdMs(long slowRequestThresholdMs) { this.slowRequestThresholdMs = slowRequestThresholdMs; }
    
    public boolean isLogRequestPayload() { return logRequestPayload; }
    public void setLogRequestPayload(boolean logRequestPayload) { this.logRequestPayload = logRequestPayload; }
    
    public boolean isLogResponsePayload() { return logResponsePayload; }
    public void setLogResponsePayload(boolean logResponsePayload) { this.logResponsePayload = logResponsePayload; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public long getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(long responseTimeout) { this.responseTimeout = responseTimeout; }
    
    public boolean isWaitForResponse() { return waitForResponse; }
    public void setWaitForResponse(boolean waitForResponse) { this.waitForResponse = waitForResponse; }
    
    public String getAsyncCorrelationId() { return asyncCorrelationId; }
    public void setAsyncCorrelationId(String asyncCorrelationId) { this.asyncCorrelationId = asyncCorrelationId; }
    
    // Additional methods needed by adapter
    public String getBaseUrl() { return baseEndpointUrl; }
    public String getTargetEndpoint() { return resourcePath; }
    public String getHealthCheckEndpoint() { return "/health"; }
    public String getQueryParameters() { return ""; }
    public boolean isIncludeResponseHeaders() { return false; }
    public String getAcceptType() { return acceptHeader; }
    public String getUsername() { return basicUsername; }
    public String getPassword() { return basicPassword; }
    public String getApiKeyHeader() { return apiKeyHeaderName != null ? apiKeyHeaderName : "X-API-Key"; }
    public String getUserAgent() { return "IntegrixFlowBridge/1.0"; }
    
    @Override
    public String toString() {
        return String.format("RestReceiverAdapterConfig{endpoint='%s', method=%s, auth=%s, batching=%s, operation='%s'}",
                getEffectiveEndpoint(), httpMethod, authenticationType, enableBatching, operationType);
    }
    
    public String getEffectiveEndpoint() {
        if (endpointUrl != null) return endpointUrl;
        if (baseEndpointUrl != null && resourcePath != null) {
            return baseEndpointUrl + (baseEndpointUrl.endsWith("/") ? "" : "/") + resourcePath;
        }
        return baseEndpointUrl;
    }
}