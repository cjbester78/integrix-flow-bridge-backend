package com.integrixs.adapters.config;

import java.util.Map;

/**
 * Configuration for HTTP Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on how the middleware makes HTTP calls to external systems.
 */
public class HttpReceiverAdapterConfig {
    
    // Target Connection Details - where the middleware sends requests TO
    private String targetEndpointUrl; // Full URL of the 3rd party API endpoint
    private HttpMethod httpMethod = HttpMethod.POST; // Method to use when calling target
    private String contentType = "application/json";
    private int connectionTimeout = 30; // Max time to wait to establish connection (seconds)
    private int readTimeout = 60; // Max time to wait for response (seconds)
    
    // Proxy Configuration for outbound calls
    private boolean useProxy = false;
    private String proxyServer;
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyType = "HTTP"; // HTTP, SOCKS
    
    // SSL/TLS Configuration for HTTPS calls
    private boolean verifySSL = true;
    private String clientCertificatePath;
    private String clientCertificatePassword;
    private String trustStorePath;
    private String trustStorePassword;
    
    // Authentication - how the middleware authenticates TO the target system
    private AuthenticationType authenticationType = AuthenticationType.NONE;
    private String basicUsername;
    private String basicPassword;
    private String bearerToken;
    private String apiKey;
    private String apiKeyHeaderName = "X-API-Key";
    private String oauthAccessToken;
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthTokenUrl;
    private String jwtToken;
    
    // Request Configuration
    private String requestHeaders; // Additional headers to send
    private String userAgent = "Integrix-Flow-Bridge/1.0";
    private String apiVersion; // API version header
    private boolean followRedirects = true;
    private int maxRedirects = 5;
    
    // Retry & Error Handling for outbound calls
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 1000;
    private boolean useExponentialBackoff = true;
    private String[] retryableErrorCodes = {"500", "502", "503", "504"};
    
    // Response Handling
    private String expectedResponseFormat = "application/json";
    private boolean validateResponse = true;
    private String responseValidationSchema;
    private long maxResponseSize = 10 * 1024 * 1024; // 10MB default
    
    // Business Configuration
    private String businessComponentId;
    private String targetSystem; // Name/identifier of target system
    private String operationType; // CREATE, UPDATE, DELETE, NOTIFY, etc.
    
    // Flow Processing Mode
    private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
    private long responseTimeout = 30000; // Timeout for synchronous response (milliseconds)
    private boolean waitForResponse = true; // Whether to wait for response in sync mode
    private String asyncCorrelationId; // Correlation ID for async processing
    
    // Constructors
    public HttpReceiverAdapterConfig() {}
    
    // Getters and Setters
    public String getTargetEndpointUrl() { return targetEndpointUrl; }
    public void setTargetEndpointUrl(String targetEndpointUrl) { this.targetEndpointUrl = targetEndpointUrl; }
    
    public HttpMethod getHttpMethod() { return httpMethod; }
    public void setHttpMethod(HttpMethod httpMethod) { this.httpMethod = httpMethod; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
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
    
    public String getProxyType() { return proxyType; }
    public void setProxyType(String proxyType) { this.proxyType = proxyType; }
    
    public boolean isVerifySSL() { return verifySSL; }
    public void setVerifySSL(boolean verifySSL) { this.verifySSL = verifySSL; }
    
    public String getClientCertificatePath() { return clientCertificatePath; }
    public void setClientCertificatePath(String clientCertificatePath) { this.clientCertificatePath = clientCertificatePath; }
    
    public String getClientCertificatePassword() { return clientCertificatePassword; }
    public void setClientCertificatePassword(String clientCertificatePassword) { this.clientCertificatePassword = clientCertificatePassword; }
    
    public String getTrustStorePath() { return trustStorePath; }
    public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }
    
    public String getTrustStorePassword() { return trustStorePassword; }
    public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
    
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
    
    public String getOauthAccessToken() { return oauthAccessToken; }
    public void setOauthAccessToken(String oauthAccessToken) { this.oauthAccessToken = oauthAccessToken; }
    
    public String getOauthClientId() { return oauthClientId; }
    public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }
    
    public String getOauthClientSecret() { return oauthClientSecret; }
    public void setOauthClientSecret(String oauthClientSecret) { this.oauthClientSecret = oauthClientSecret; }
    
    public String getOauthTokenUrl() { return oauthTokenUrl; }
    public void setOauthTokenUrl(String oauthTokenUrl) { this.oauthTokenUrl = oauthTokenUrl; }
    
    // Convenience methods for adapter compatibility
    public String getClientId() { return oauthClientId; }
    public String getClientSecret() { return oauthClientSecret; }
    public String getEndpointUrl() { return targetEndpointUrl; }
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    
    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }
    
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public boolean isFollowRedirects() { return followRedirects; }
    public void setFollowRedirects(boolean followRedirects) { this.followRedirects = followRedirects; }
    
    public int getMaxRedirects() { return maxRedirects; }
    public void setMaxRedirects(int maxRedirects) { this.maxRedirects = maxRedirects; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isUseExponentialBackoff() { return useExponentialBackoff; }
    public void setUseExponentialBackoff(boolean useExponentialBackoff) { this.useExponentialBackoff = useExponentialBackoff; }
    
    public String[] getRetryableErrorCodes() { return retryableErrorCodes; }
    public void setRetryableErrorCodes(String[] retryableErrorCodes) { this.retryableErrorCodes = retryableErrorCodes; }
    
    public String getExpectedResponseFormat() { return expectedResponseFormat; }
    public void setExpectedResponseFormat(String expectedResponseFormat) { this.expectedResponseFormat = expectedResponseFormat; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public String getResponseValidationSchema() { return responseValidationSchema; }
    public void setResponseValidationSchema(String responseValidationSchema) { this.responseValidationSchema = responseValidationSchema; }
    
    public long getMaxResponseSize() { return maxResponseSize; }
    public void setMaxResponseSize(long maxResponseSize) { this.maxResponseSize = maxResponseSize; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public long getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(long responseTimeout) { this.responseTimeout = responseTimeout; }
    
    public boolean isWaitForResponse() { return waitForResponse; }
    public void setWaitForResponse(boolean waitForResponse) { this.waitForResponse = waitForResponse; }
    
    public String getAsyncCorrelationId() { return asyncCorrelationId; }
    public void setAsyncCorrelationId(String asyncCorrelationId) { this.asyncCorrelationId = asyncCorrelationId; }
    
    @Override
    public String toString() {
        return String.format("HttpReceiverAdapterConfig{targetUrl='%s', method='%s', contentType='%s', auth='%s', proxy='%s'}",
                targetEndpointUrl, httpMethod, contentType, authenticationType, useProxy ? proxyServer + ":" + proxyPort : "none");
    }

	public void setEndpointUrl(Object object) {
		// TODO Auto-generated method stub
		
	}
}