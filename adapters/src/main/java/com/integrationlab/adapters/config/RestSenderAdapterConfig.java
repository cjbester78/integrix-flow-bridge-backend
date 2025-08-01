package com.integrationlab.adapters.config;

import java.util.Map;
import com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig;

/**
 * Configuration for REST Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on connecting TO external REST APIs to poll/retrieve data.
 */
public class RestSenderAdapterConfig {
    
    // Source REST API Connection Details
    private String baseEndpointUrl; // Base URL of the source REST API
    private String resourcePath; // Specific resource path to poll
    private String endpointUrl; // Full endpoint URL (computed or provided)
    private String healthCheckPath; // Health check endpoint path
    private String apiVersion;
    private boolean enableDuplicateDetection = false; // Enable duplicate handling
    
    // Request Configuration for Data Retrieval
    private HttpMethod httpMethod = HttpMethod.GET; // Usually GET for polling
    private String acceptHeader = "application/json"; // What data format we accept
    private Map<String, String> headers;
    private String customHeaders; // String format: "key1:value1,key2:value2"
    private String userAgent = "IntegrixFlowBridge/1.0"; // User agent header
    private int connectionTimeout = 30; // seconds
    private int readTimeout = 60; // seconds
    
    // Business Context
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from source
    private String responseFormat; // Expected response format
    
    // Authentication for accessing source REST API
    private AuthenticationType authenticationType = AuthenticationType.NONE;
    private String basicUsername;
    private String basicPassword;
    private String bearerToken;
    private String apiKey;
    private String apiKeyHeaderName;
    private String jwtToken;
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
    private String certificateId;
    
    // Proxy Configuration (if needed to reach external APIs)
    private boolean useProxy = false;
    private String proxyServer;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    
    // Polling Configuration (for receiver polling scenarios)
    private Long pollingInterval = 30000L; // 30 seconds default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression
    
    // Data Processing Configuration
    private String queryParameters; // URL query parameters for filtering
    private String requestTemplate; // Template for request body if POST/PUT
    private boolean validateResponse = true;
    private String responseValidationRules;
    private boolean includeResponseHeaders = false; // Include response headers in result
    
    // Pagination and Data Limits
    private Integer pageSize; // For paginated APIs
    private Integer maxResults; // Maximum records to retrieve
    private String paginationStrategy = "OFFSET_BASED"; // OFFSET_BASED, CURSOR_BASED, PAGE_BASED
    private String nextPageParam; // Parameter name for next page
    
    // Rate Limiting and Error Handling
    private String rateLimits; // Rate limiting configuration
    private String retryPolicy; // Retry configuration
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private String errorHandlingStrategy = "FAIL_FAST";
    
    // Incremental Processing (for change tracking)
    private String lastModifiedField; // Field to track last modification
    private Object lastProcessedValue; // Last processed timestamp/ID
    private boolean resetIncrementalOnStart = false;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    
    // Flow Processing Mode
    private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
    private long asyncResponseTimeout = 30000; // Timeout for async processing (milliseconds)
    private String asyncResponseFormat = "HTTP_202"; // HTTP_202, CUSTOM_RESPONSE
    private String asyncCallbackUrl; // URL to send async response (if applicable)
    private boolean logSlowRequests = true;
    private long slowRequestThresholdMs = 5000;
    
    // JSON to XML Wrapper Configuration
    private JsonXmlWrapperConfig jsonXmlWrapperConfig;
    
    // Constructors
    public RestSenderAdapterConfig() {
        // Initialize with default JSON to XML wrapper config
        this.jsonXmlWrapperConfig = JsonXmlWrapperConfig.builder()
                .rootElementName("restApiResponse")
                .includeXmlDeclaration(true)
                .prettyPrint(true)
                .convertPropertyNames(true)
                .build();
    }
    
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
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
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
    
    public String getApiKeyHeaderName() { return apiKeyHeaderName; }
    public void setApiKeyHeaderName(String apiKeyHeaderName) { this.apiKeyHeaderName = apiKeyHeaderName; }
    
    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }
    
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
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public String getQueryParameters() { return queryParameters; }
    public void setQueryParameters(String queryParameters) { this.queryParameters = queryParameters; }
    
    public String getRequestTemplate() { return requestTemplate; }
    public void setRequestTemplate(String requestTemplate) { this.requestTemplate = requestTemplate; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public String getResponseValidationRules() { return responseValidationRules; }
    public void setResponseValidationRules(String responseValidationRules) { this.responseValidationRules = responseValidationRules; }
    
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
    
    public String getPaginationStrategy() { return paginationStrategy; }
    public void setPaginationStrategy(String paginationStrategy) { this.paginationStrategy = paginationStrategy; }
    
    public String getNextPageParam() { return nextPageParam; }
    public void setNextPageParam(String nextPageParam) { this.nextPageParam = nextPageParam; }
    
    public String getRateLimits() { return rateLimits; }
    public void setRateLimits(String rateLimits) { this.rateLimits = rateLimits; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getLastModifiedField() { return lastModifiedField; }
    public void setLastModifiedField(String lastModifiedField) { this.lastModifiedField = lastModifiedField; }
    
    public Object getLastProcessedValue() { return lastProcessedValue; }
    public void setLastProcessedValue(Object lastProcessedValue) { this.lastProcessedValue = lastProcessedValue; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogSlowRequests() { return logSlowRequests; }
    public void setLogSlowRequests(boolean logSlowRequests) { this.logSlowRequests = logSlowRequests; }
    
    public long getSlowRequestThresholdMs() { return slowRequestThresholdMs; }
    public void setSlowRequestThresholdMs(long slowRequestThresholdMs) { this.slowRequestThresholdMs = slowRequestThresholdMs; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public long getAsyncResponseTimeout() { return asyncResponseTimeout; }
    public void setAsyncResponseTimeout(long asyncResponseTimeout) { this.asyncResponseTimeout = asyncResponseTimeout; }
    
    public String getAsyncResponseFormat() { return asyncResponseFormat; }
    public void setAsyncResponseFormat(String asyncResponseFormat) { this.asyncResponseFormat = asyncResponseFormat; }
    
    public String getAsyncCallbackUrl() { return asyncCallbackUrl; }
    public void setAsyncCallbackUrl(String asyncCallbackUrl) { this.asyncCallbackUrl = asyncCallbackUrl; }
    
    public JsonXmlWrapperConfig getJsonXmlWrapperConfig() { return jsonXmlWrapperConfig; }
    public void setJsonXmlWrapperConfig(JsonXmlWrapperConfig jsonXmlWrapperConfig) { this.jsonXmlWrapperConfig = jsonXmlWrapperConfig; }
    
    // Additional methods needed by adapter
    public String getBaseUrl() { return baseEndpointUrl; }
    public String getHealthCheckEndpoint() { return healthCheckPath; }
    public String getPollingEndpoint() { return resourcePath; }
    public boolean isEnableDuplicateHandling() { return enableDuplicateDetection; }
    public boolean isIncludeResponseHeaders() { return includeResponseHeaders; }
    public String getContentType() { return "application/json"; } // Default content type
    public String getAcceptType() { return acceptHeader; }
    public String getUsername() { return basicUsername; }
    public String getPassword() { return basicPassword; }
    public String getApiKeyHeader() { return apiKeyHeaderName != null ? apiKeyHeaderName : "X-API-Key"; }
    public String getUserAgent() { return userAgent; }
    
    @Override
    public String toString() {
        return String.format("RestSenderAdapterConfig{endpoint='%s', method=%s, auth=%s, polling=%dms, pageSize=%s}",
                getEffectiveEndpoint(), httpMethod, authenticationType, pollingInterval, pageSize);
    }
    
    public String getEffectiveEndpoint() {
        if (endpointUrl != null) return endpointUrl;
        if (baseEndpointUrl != null && resourcePath != null) {
            return baseEndpointUrl + (baseEndpointUrl.endsWith("/") ? "" : "/") + resourcePath;
        }
        return baseEndpointUrl;
    }
}