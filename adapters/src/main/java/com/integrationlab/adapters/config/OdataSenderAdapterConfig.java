package com.integrationlab.adapters.config;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for OData Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on polling/querying OData services for data changes.
 */
public class OdataSenderAdapterConfig {

    // Source OData Service Configuration
    private String sourceServiceEndpointUrl;
    private String sourceServiceName;
    private String sourceServiceVersion;
    private String sourceMetadataUrl;
    private String sourceBaseUrl;
    
    // Source Entity and Query Configuration
    private String sourceEntitySet;
    private String sourceEntityType;
    private String queryOptions; // $filter, $select, $expand, etc.
    private String customQueryParameters;
    private String filterCriteria; // Polling filter criteria
    private String sortOrder; // Default sorting for polling
    
    // Polling Configuration
    private Long pollingInterval = 60000L; // 1 minute default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    private boolean enableDeltaQueries = true; // Track changes using $deltatoken
    private String deltaTokenStorage; // How to store delta tokens
    private int maxRecordsPerPoll = 1000; // Limit records per polling cycle
    
    // Source Authentication
    private String authenticationType = "none"; // none, basic, oauth2, saml
    
    // Basic Authentication
    private String sourceUsername;
    private String sourcePassword;
    
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
    private String acceptHeader = "application/json";
    private Map<String, String> customHeaders = new HashMap<>();
    
    // Connection Settings
    private int connectionTimeout = 30000; // milliseconds
    private int readTimeout = 60000; // milliseconds
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    
    // Response Processing
    private String responseFormat = "JSON"; // JSON, XML
    private String responseHandling = "AUTO"; // AUTO, MANUAL
    private String dataMapping; // Custom data mapping configuration
    private boolean validateResponse = true;
    private String responseValidationSchema;
    
    // Incremental Processing
    private String lastPolledTimestamp; // ISO timestamp of last successful poll
    private String lastDeltaToken; // OData delta token for change tracking
    private boolean resetIncrementalOnStart = false;
    private String timestampField; // Field to use for incremental processing
    
    // Error Handling for Polling
    private String errorHandlingStrategy = "FAIL_FAST";
    private String retryPolicy = "EXPONENTIAL_BACKOFF";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    private boolean continueOnError = false;
    private String errorLogDirectory; // Directory for error logs
    
    // Performance and Monitoring
    private boolean enableMetrics = true;
    private long slowQueryThresholdMs = 30000; // 30 seconds
    private boolean logRequestResponse = false;
    private int pageSize = 100; // OData $top parameter
    private boolean enablePaging = true;
    
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
    private String sourceSystemId; // Identifier of source OData system
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public OdataSenderAdapterConfig() {}
    
    public OdataSenderAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getSourceServiceEndpointUrl() { return sourceServiceEndpointUrl; }
    public void setSourceServiceEndpointUrl(String sourceServiceEndpointUrl) { this.sourceServiceEndpointUrl = sourceServiceEndpointUrl; }
    
    public String getSourceServiceName() { return sourceServiceName; }
    public void setSourceServiceName(String sourceServiceName) { this.sourceServiceName = sourceServiceName; }
    
    public String getSourceServiceVersion() { return sourceServiceVersion; }
    public void setSourceServiceVersion(String sourceServiceVersion) { this.sourceServiceVersion = sourceServiceVersion; }
    
    public String getSourceMetadataUrl() { return sourceMetadataUrl; }
    public void setSourceMetadataUrl(String sourceMetadataUrl) { this.sourceMetadataUrl = sourceMetadataUrl; }
    
    public String getSourceBaseUrl() { return sourceBaseUrl; }
    public void setSourceBaseUrl(String sourceBaseUrl) { this.sourceBaseUrl = sourceBaseUrl; }
    
    public String getSourceEntitySet() { return sourceEntitySet; }
    public void setSourceEntitySet(String sourceEntitySet) { this.sourceEntitySet = sourceEntitySet; }
    
    public String getSourceEntityType() { return sourceEntityType; }
    public void setSourceEntityType(String sourceEntityType) { this.sourceEntityType = sourceEntityType; }
    
    public String getQueryOptions() { return queryOptions; }
    public void setQueryOptions(String queryOptions) { this.queryOptions = queryOptions; }
    
    public String getCustomQueryParameters() { return customQueryParameters; }
    public void setCustomQueryParameters(String customQueryParameters) { this.customQueryParameters = customQueryParameters; }
    
    public String getFilterCriteria() { return filterCriteria; }
    public void setFilterCriteria(String filterCriteria) { this.filterCriteria = filterCriteria; }
    
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public boolean isEnableDeltaQueries() { return enableDeltaQueries; }
    public void setEnableDeltaQueries(boolean enableDeltaQueries) { this.enableDeltaQueries = enableDeltaQueries; }
    
    public String getDeltaTokenStorage() { return deltaTokenStorage; }
    public void setDeltaTokenStorage(String deltaTokenStorage) { this.deltaTokenStorage = deltaTokenStorage; }
    
    public int getMaxRecordsPerPoll() { return maxRecordsPerPoll; }
    public void setMaxRecordsPerPoll(int maxRecordsPerPoll) { this.maxRecordsPerPoll = maxRecordsPerPoll; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public String getSourceUsername() { return sourceUsername; }
    public void setSourceUsername(String sourceUsername) { this.sourceUsername = sourceUsername; }
    
    public String getSourcePassword() { return sourcePassword; }
    public void setSourcePassword(String sourcePassword) { this.sourcePassword = sourcePassword; }
    
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
    
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
    
    public String getResponseHandling() { return responseHandling; }
    public void setResponseHandling(String responseHandling) { this.responseHandling = responseHandling; }
    
    public String getDataMapping() { return dataMapping; }
    public void setDataMapping(String dataMapping) { this.dataMapping = dataMapping; }
    
    public boolean isValidateResponse() { return validateResponse; }
    public void setValidateResponse(boolean validateResponse) { this.validateResponse = validateResponse; }
    
    public String getResponseValidationSchema() { return responseValidationSchema; }
    public void setResponseValidationSchema(String responseValidationSchema) { this.responseValidationSchema = responseValidationSchema; }
    
    public String getLastPolledTimestamp() { return lastPolledTimestamp; }
    public void setLastPolledTimestamp(String lastPolledTimestamp) { this.lastPolledTimestamp = lastPolledTimestamp; }
    
    public String getLastDeltaToken() { return lastDeltaToken; }
    public void setLastDeltaToken(String lastDeltaToken) { this.lastDeltaToken = lastDeltaToken; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
    public String getTimestampField() { return timestampField; }
    public void setTimestampField(String timestampField) { this.timestampField = timestampField; }
    
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
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    
    public boolean isLogRequestResponse() { return logRequestResponse; }
    public void setLogRequestResponse(boolean logRequestResponse) { this.logRequestResponse = logRequestResponse; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    
    public boolean isEnablePaging() { return enablePaging; }
    public void setEnablePaging(boolean enablePaging) { this.enablePaging = enablePaging; }
    
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
    
    public String getSourceSystemId() { return sourceSystemId; }
    public void setSourceSystemId(String sourceSystemId) { this.sourceSystemId = sourceSystemId; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getServiceEndpointUrl() { return sourceServiceEndpointUrl; }
    public String getServiceName() { return sourceServiceName; }
    public String getEntitySet() { return sourceEntitySet; }
    public String getEntityType() { return sourceEntityType; }
    public String getUsername() { return sourceUsername; }
    public String getPassword() { return sourcePassword; }
    public String getDeltaQueries() { return String.valueOf(enableDeltaQueries); }
    
    @Override
    public String toString() {
        return String.format("OdataSenderAdapterConfig{sourceEndpoint='%s', entitySet='%s', polling=%dms, auth='%s'}",
                sourceServiceEndpointUrl, sourceEntitySet, pollingInterval, authenticationType);
    }
}