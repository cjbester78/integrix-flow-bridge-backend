package com.integrationlab.adapters.config;

import java.util.Map;

/**
 * Configuration for SOAP Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on calling SOAP services to send/push data.
 */
public class SoapReceiverAdapterConfig {
    
    // Target SOAP Service Connection Details
    private String serviceEndpointUrl; // Target SOAP service endpoint URL
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
    private String targetDataStructureId; // Expected data structure for target
    private String requestPayloadFormat; // Format for request payload
    
    // Flow Processing Mode
    private String processingMode = "SYNCHRONOUS"; // SYNCHRONOUS, ASYNCHRONOUS
    private long responseTimeout = 30000; // Timeout for synchronous response (milliseconds)
    private boolean waitForResponse = true; // Whether to wait for response in sync mode
    private String asyncCorrelationId; // Correlation ID for async processing
    
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
    
    // Data Sending Configuration
    private boolean validateRequest = true;
    private String requestValidationRules;
    private boolean validateResponse = true;
    private String responseValidationRules;
    
    // Batch Processing
    private Integer batchSize; // Number of records to send in one request
    private boolean enableBatching = false;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Response Processing
    private String responseMessage; // Expected response format
    private String responseFormat;
    private String responseHandling; // How to handle responses
    
    // Fault and Error Handling
    private String faultHandling = "propagate"; // propagate, ignore, transform
    private String errorHandling = "FAIL_FAST";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean continueOnError = false;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "CREATE"; // CREATE, UPDATE, DELETE, UPSERT
    private boolean idempotent = false; // Whether requests are idempotent
    private String idempotencyKey; // Header name for idempotency key
    
    // Rate Limiting
    private long requestIntervalMs = 0; // Minimum interval between requests
    private int maxConcurrentRequests = 10;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logSlowRequests = true;
    private long slowRequestThresholdMs = 5000;
    private boolean logRequestPayload = false; // Log actual data being sent
    private boolean logResponsePayload = false; // Log actual response data
    
    // Constructors
    public SoapReceiverAdapterConfig() {}
    
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
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public String getRequestPayloadFormat() { return requestPayloadFormat; }
    public void setRequestPayloadFormat(String requestPayloadFormat) { this.requestPayloadFormat = requestPayloadFormat; }
    
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
    
    public boolean isValidateRequest() { return validateRequest; }
    public void setValidateRequest(boolean validateRequest) { this.validateRequest = validateRequest; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public String getResponseHandling() { return responseHandling; }
    public void setResponseHandling(String responseHandling) { this.responseHandling = responseHandling; }
    
    public String getFaultHandling() { return faultHandling; }
    public void setFaultHandling(String faultHandling) { this.faultHandling = faultHandling; }
    
    public String getErrorHandling() { return errorHandling; }
    public void setErrorHandling(String errorHandling) { this.errorHandling = errorHandling; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
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
        return String.format("SoapReceiverAdapterConfig{endpoint='%s', operation='%s', auth='%s', batching=%s}",
                getEffectiveEndpoint(), operationName, authenticationType, enableBatching);
    }
    
    public String getEffectiveEndpoint() {
        return serviceEndpointUrl != null ? serviceEndpointUrl : endpointUrl;
    }
}