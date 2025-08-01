package com.integrationlab.adapters.config;

import java.util.Map;
import java.util.HashMap;
import com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig;

/**
 * Configuration for HTTP Sender Adapter (Backend). In middleware terminology,
 * sender adapters receive data FROM external source systems. This configuration
 * focuses on how the middleware receives HTTP requests from external systems.
 */
public class HttpSenderAdapterConfig {

	// Connection Details - where external systems send requests TO the middleware
	private String endpointUrl; // The URL where 3rd party sends requests
	private HttpMethod httpMethod = HttpMethod.POST; // Default method for receiving requests
	private String contentType = "application/json";
	private String apiVersion;
	private String rateLimits; // Limits on calls per time period

	// SSL/TLS Configuration for HTTPS endpoints
	private String sslConfig;
	private String sslKeyStorePath;
	private String sslKeyStorePassword;
	private String sslTrustStorePath;
	private String sslTrustStorePassword;

	// Authentication - how external systems authenticate TO the middleware
	private AuthenticationType authenticationType = AuthenticationType.NONE;
	private String basicUsername;
	private String basicPassword;
	private String bearerToken;
	private String apiKey;
	private String apiKeyHeaderName; // Header name for API key
	private String jwtToken;
	private String jwtAlgorithm = "HS256";
	private String oauthAccessToken; // OAuth2 access token

	// Data Handling
	private String businessComponentId;
	private String requestPayloadFormat; // Expected format from source system
	private String responseFormat; // Format to respond back to source system
	private String customHeaders; // Additional headers to expect/send
	private Map<String, String> headers = new HashMap<>(); // Headers as Map

	// Error Handling & Response
	private String errorHandling; // Expected error codes and their meanings
	private String retryPolicy; // Instructions on retries or idempotency

	// Processing Configuration
	private int connectionTimeout = 30; // Seconds to wait for connections
	private int readTimeout = 60; // Seconds to wait for data
	private boolean validateIncomingData = true;
	private String requestValidationSchema; // Schema to validate incoming requests
	
	// Flow Processing Mode
	private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
	private long asyncResponseTimeout = 30000; // Timeout for async processing (milliseconds)
	private String asyncResponseFormat = "HTTP_202"; // HTTP_202, CUSTOM_RESPONSE
	private String asyncCallbackUrl; // URL to send async response (if applicable)

	// JSON to XML Wrapper Configuration
	private JsonXmlWrapperConfig jsonXmlWrapperConfig;

	// Constructors
	public HttpSenderAdapterConfig() {
		// Initialize with default JSON to XML wrapper config
		this.jsonXmlWrapperConfig = JsonXmlWrapperConfig.builder()
				.rootElementName("httpMessage")
				.includeXmlDeclaration(true)
				.prettyPrint(true)
				.convertPropertyNames(true)
				.build();
	}

	// Getters and Setters
	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getRateLimits() {
		return rateLimits;
	}

	public void setRateLimits(String rateLimits) {
		this.rateLimits = rateLimits;
	}

	public String getSslConfig() {
		return sslConfig;
	}

	public void setSslConfig(String sslConfig) {
		this.sslConfig = sslConfig;
	}

	public String getSslKeyStorePath() {
		return sslKeyStorePath;
	}

	public void setSslKeyStorePath(String sslKeyStorePath) {
		this.sslKeyStorePath = sslKeyStorePath;
	}

	public String getSslKeyStorePassword() {
		return sslKeyStorePassword;
	}

	public void setSslKeyStorePassword(String sslKeyStorePassword) {
		this.sslKeyStorePassword = sslKeyStorePassword;
	}

	public String getSslTrustStorePath() {
		return sslTrustStorePath;
	}

	public void setSslTrustStorePath(String sslTrustStorePath) {
		this.sslTrustStorePath = sslTrustStorePath;
	}

	public String getSslTrustStorePassword() {
		return sslTrustStorePassword;
	}

	public void setSslTrustStorePassword(String sslTrustStorePassword) {
		this.sslTrustStorePassword = sslTrustStorePassword;
	}

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public String getBasicUsername() {
		return basicUsername;
	}

	public void setBasicUsername(String basicUsername) {
		this.basicUsername = basicUsername;
	}

	public String getBasicPassword() {
		return basicPassword;
	}

	public void setBasicPassword(String basicPassword) {
		this.basicPassword = basicPassword;
	}

	public String getBearerToken() {
		return bearerToken;
	}

	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiKeyHeaderName() {
		return apiKeyHeaderName;
	}

	public void setApiKeyHeaderName(String apiKeyHeaderName) {
		this.apiKeyHeaderName = apiKeyHeaderName;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	public String getJwtAlgorithm() {
		return jwtAlgorithm;
	}

	public void setJwtAlgorithm(String jwtAlgorithm) {
		this.jwtAlgorithm = jwtAlgorithm;
	}

	public String getOauthAccessToken() {
		return oauthAccessToken;
	}

	public void setOauthAccessToken(String oauthAccessToken) {
		this.oauthAccessToken = oauthAccessToken;
	}

	public String getBusinessComponentId() {
		return businessComponentId;
	}

	public void setBusinessComponentId(String businessComponentId) {
		this.businessComponentId = businessComponentId;
	}

	public String getRequestPayloadFormat() {
		return requestPayloadFormat;
	}

	public void setRequestPayloadFormat(String requestPayloadFormat) {
		this.requestPayloadFormat = requestPayloadFormat;
	}

	public String getResponseFormat() {
		return responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	public String getCustomHeaders() {
		return customHeaders;
	}

	public void setCustomHeaders(String customHeaders) {
		this.customHeaders = customHeaders;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getErrorHandling() {
		return errorHandling;
	}

	public void setErrorHandling(String errorHandling) {
		this.errorHandling = errorHandling;
	}

	public String getRetryPolicy() {
		return retryPolicy;
	}

	public void setRetryPolicy(String retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	// Convenience method for adapter compatibility - converts seconds to
	// milliseconds
	public long getTimeoutMillis() {
		return readTimeout * 1000L;
	}
	public void setTimeoutMillis(int readTimeout) {
		this.readTimeout = readTimeout;
		
	}

	public boolean isValidateIncomingData() {
		return validateIncomingData;
	}

	public void setValidateIncomingData(boolean validateIncomingData) {
		this.validateIncomingData = validateIncomingData;
	}

	public String getRequestValidationSchema() {
		return requestValidationSchema;
	}

	public void setRequestValidationSchema(String requestValidationSchema) {
		this.requestValidationSchema = requestValidationSchema;
	}

	public String getProcessingMode() {
		return processingMode;
	}

	public void setProcessingMode(String processingMode) {
		this.processingMode = processingMode;
	}

	public long getAsyncResponseTimeout() {
		return asyncResponseTimeout;
	}

	public void setAsyncResponseTimeout(long asyncResponseTimeout) {
		this.asyncResponseTimeout = asyncResponseTimeout;
	}

	public String getAsyncResponseFormat() {
		return asyncResponseFormat;
	}

	public void setAsyncResponseFormat(String asyncResponseFormat) {
		this.asyncResponseFormat = asyncResponseFormat;
	}

	public String getAsyncCallbackUrl() {
		return asyncCallbackUrl;
	}

	public void setAsyncCallbackUrl(String asyncCallbackUrl) {
		this.asyncCallbackUrl = asyncCallbackUrl;
	}

	public JsonXmlWrapperConfig getJsonXmlWrapperConfig() {
		return jsonXmlWrapperConfig;
	}

	public void setJsonXmlWrapperConfig(JsonXmlWrapperConfig jsonXmlWrapperConfig) {
		this.jsonXmlWrapperConfig = jsonXmlWrapperConfig;
	}

	@Override
	public String toString() {
		return String.format("HttpSenderAdapterConfig{endpointUrl='%s', method='%s', contentType='%s', auth='%s'}",
				endpointUrl, httpMethod, contentType, authenticationType);
	}


}