package com.integrationlab.adapters.config;

import java.util.Map;

/**
 * Configuration for SOAP Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on calling SOAP services to retrieve/poll data.
 */
public class SoapSenderAdapterConfig {
    
    // Source SOAP Service Connection Details
    private String serviceEndpointUrl; // SOAP service endpoint URL
    private String wsdlUrl; // WSDL location
    private String endpointUrl; // For backward compatibility
    private String soapAction; // SOAPAction header value
    private String soapVersion = "1.1"; // 1.1 or 1.2
    
    // Service Operation Configuration
    private String serviceName;
    private String portName;
    private String operationName;
    private String namespace;
    
    // Request Configuration
    private String requestMessage; // SOAP request template
    private String requestTemplate; // Request body template
    private String contentType; // text/xml or application/soap+xml
    private Map<String, String> headers;
    private String customHeaders;
    
    // Business Context
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from source
    
    // Authentication & Security
    private String authenticationType = "none"; // none, basic, ws-security, oauth
    private String basicUsername;
    private String basicPassword;
    
    // WS-Security Configuration
    private String wsSecurityPolicyType;
    private String wsSecurityUsername;
    private String wsSecurityPassword;
    private String passwordType = "digest"; // digest or plaintext
    private String securityPolicy; // usernametoken, timestamp, sign, encrypt
    private String wsAddressing = "disabled"; // disabled, optional, required
    private String timestampTTL = "300"; // seconds
    private String securityAlgorithm; // aes128, aes256, 3des, rsa15, rsa-oaep
    
    // Certificate and Key Management
    private String keystoreAlias;
    private String keystorePassword;
    private String certificateAlias;
    private String verifyServerCertificate = "true";
    private String customPolicy;
    private String securityTokenReference; // binarySecurityToken, keyIdentifier, x509Data
    private String sslKeyStorePath;
    private String sslKeyStorePassword;
    private String sslTrustStorePath;
    private String sslTrustStorePassword;
    
    // OAuth Configuration
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthTokenUrl;
    private String oauthScope;
    private String oauthAccessToken;
    
    // Connection and Timeout Settings
    private int timeout = 30; // seconds
    private int timeoutMillis = 30000; // milliseconds
    private int connectionTimeout = 30; // seconds
    private int readTimeout = 60; // seconds
    
    // Polling Configuration (for sender polling scenarios)
    private Long pollingInterval = 30000L; // 30 seconds default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression
    
    // Response Processing
    private String responseMessage; // Expected response format
    private String responseFormat;
    private boolean validateResponse = true;
    private String responseValidationRules;
    
    // Fault and Error Handling
    private String faultHandling = "propagate"; // propagate, ignore, transform
    private String errorHandling = "FAIL_FAST";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    
    // Data Processing
    private String queryParameters; // Parameters for service calls
    private Integer maxResults; // Maximum records to retrieve
    private String lastProcessedValue; // For incremental processing
    private boolean resetIncrementalOnStart = false;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logSlowRequests = true;
    private long slowRequestThresholdMs = 5000;
    
    // Flow Processing Mode
    private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
    private long asyncResponseTimeout = 30000; // Timeout for async processing (milliseconds)
    private String asyncResponseFormat = "HTTP_202"; // HTTP_202, CUSTOM_RESPONSE
    private String asyncCallbackUrl; // URL to send async response (if applicable)
    
    // Constructors
    public SoapSenderAdapterConfig() {}
    
    // Essential getters and setters
    public String getServiceEndpointUrl() { return serviceEndpointUrl; }
    public void setServiceEndpointUrl(String serviceEndpointUrl) { this.serviceEndpointUrl = serviceEndpointUrl; }
    
    public String getWsdlUrl() { return wsdlUrl; }
    public void setWsdlUrl(String wsdlUrl) { this.wsdlUrl = wsdlUrl; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getSoapAction() { return soapAction; }
    public void setSoapAction(String soapAction) { this.soapAction = soapAction; }
    
    public String getSoapVersion() { return soapVersion; }
    public void setSoapVersion(String soapVersion) { this.soapVersion = soapVersion; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getPortName() { return portName; }
    public void setPortName(String portName) { this.portName = portName; }
    
    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
    
    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    
    public String getRequestMessage() { return requestMessage; }
    public void setRequestMessage(String requestMessage) { this.requestMessage = requestMessage; }
    
    public String getRequestTemplate() { return requestTemplate; }
    public void setRequestTemplate(String requestTemplate) { this.requestTemplate = requestTemplate; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public String getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(String customHeaders) { this.customHeaders = customHeaders; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public String getBasicUsername() { return basicUsername; }
    public void setBasicUsername(String basicUsername) { this.basicUsername = basicUsername; }
    
    public String getBasicPassword() { return basicPassword; }
    public void setBasicPassword(String basicPassword) { this.basicPassword = basicPassword; }
    
    public String getWsSecurityPolicyType() { return wsSecurityPolicyType; }
    public void setWsSecurityPolicyType(String wsSecurityPolicyType) { this.wsSecurityPolicyType = wsSecurityPolicyType; }
    
    public String getWsSecurityUsername() { return wsSecurityUsername; }
    public void setWsSecurityUsername(String wsSecurityUsername) { this.wsSecurityUsername = wsSecurityUsername; }
    
    public String getWsSecurityPassword() { return wsSecurityPassword; }
    public void setWsSecurityPassword(String wsSecurityPassword) { this.wsSecurityPassword = wsSecurityPassword; }
    
    public String getPasswordType() { return passwordType; }
    public void setPasswordType(String passwordType) { this.passwordType = passwordType; }
    
    public String getSecurityPolicy() { return securityPolicy; }
    public void setSecurityPolicy(String securityPolicy) { this.securityPolicy = securityPolicy; }
    
    public String getWsAddressing() { return wsAddressing; }
    public void setWsAddressing(String wsAddressing) { this.wsAddressing = wsAddressing; }
    
    public String getTimestampTTL() { return timestampTTL; }
    public void setTimestampTTL(String timestampTTL) { this.timestampTTL = timestampTTL; }
    
    public String getSecurityAlgorithm() { return securityAlgorithm; }
    public void setSecurityAlgorithm(String securityAlgorithm) { this.securityAlgorithm = securityAlgorithm; }
    
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    
    public int getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public String getFaultHandling() { return faultHandling; }
    public void setFaultHandling(String faultHandling) { this.faultHandling = faultHandling; }
    
    public String getErrorHandling() { return errorHandling; }
    public void setErrorHandling(String errorHandling) { this.errorHandling = errorHandling; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public long getAsyncResponseTimeout() { return asyncResponseTimeout; }
    public void setAsyncResponseTimeout(long asyncResponseTimeout) { this.asyncResponseTimeout = asyncResponseTimeout; }
    
    public String getAsyncResponseFormat() { return asyncResponseFormat; }
    public void setAsyncResponseFormat(String asyncResponseFormat) { this.asyncResponseFormat = asyncResponseFormat; }
    
    public String getAsyncCallbackUrl() { return asyncCallbackUrl; }
    public void setAsyncCallbackUrl(String asyncCallbackUrl) { this.asyncCallbackUrl = asyncCallbackUrl; }
    
    @Override
    public String toString() {
        return String.format("SoapSenderAdapterConfig{endpoint='%s', operation='%s', auth='%s', polling=%dms}",
                getEffectiveEndpoint(), operationName, authenticationType, pollingInterval);
    }
    
    public String getEffectiveEndpoint() {
        return serviceEndpointUrl != null ? serviceEndpointUrl : endpointUrl;
    }
}