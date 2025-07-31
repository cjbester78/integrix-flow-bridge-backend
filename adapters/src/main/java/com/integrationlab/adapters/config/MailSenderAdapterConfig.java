package com.integrationlab.adapters.config;

/**
 * Configuration for Mail Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on polling mail servers (IMAP/POP3) to retrieve emails.
 */
public class MailSenderAdapterConfig {
    
    // Mail Server Connection (Source - IMAP/POP3)
    private String mailServerHost; // IMAP/POP3 server hostname
    private String mailServerPort = "993"; // Default IMAPS port
    private String mailProtocol = "IMAP"; // IMAP, POP3
    private String mailUsername;
    private String mailPassword;
    private boolean useSSLTLS = true;
    private String connectionTimeout = "30000"; // 30 seconds
    private String readTimeout = "60000"; // 60 seconds
    
    // Folder and Mailbox Configuration
    private String folderName = "INBOX"; // Folder to monitor
    private String[] additionalFolders; // Additional folders to monitor
    private boolean monitorSubfolders = false;
    
    // Polling Configuration
    private String pollingInterval = "30000"; // 30 seconds
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    private String maxMessages = "100"; // Maximum messages per poll
    
    // Message Selection and Filtering
    private String searchCriteria; // Search criteria for filtering messages
    private boolean fetchUnreadOnly = true;
    private boolean fetchFromToday = false;
    private String dateFromFilter; // Date from filter (ISO format)
    private String dateToFilter; // Date to filter (ISO format)
    private String subjectFilter; // Subject filter pattern
    private String fromAddressFilter; // From address filter pattern
    private String[] requiredAttachments; // Required attachment patterns
    
    // Message Processing
    private String contentHandling = "both"; // text, html, both
    private String mailEncoding = "UTF-8";
    private boolean includeAttachments = true;
    private String attachmentDirectory; // Directory to save attachments
    private boolean deleteAfterFetch = false;
    private boolean markAsRead = true;
    private String processedFolder; // Folder to move processed messages
    
    // Business Context
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from emails
    
    // Incremental Processing
    private String lastProcessedMessageId; // Last processed message ID
    private long lastProcessedTimestamp; // Last processed timestamp
    private boolean resetIncrementalOnStart = false;
    
    // Error Handling
    private String errorHandlingStrategy = "FAIL_FAST";
    private String errorFolder; // Folder for messages that caused errors
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    
    // Security and Authentication
    private String authenticationMethod = "password"; // password, oauth2
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthRefreshToken;
    private String oauthAccessToken;
    
    // Connection Pool and Performance
    private int maxConnections = 5;
    private long connectionPoolTimeout = 30000L;
    private boolean useConnectionPool = true;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logProcessedMessages = true;
    private boolean logMessageContent = false;
    private long slowProcessingThresholdMs = 10000;
    
    // Legacy compatibility
    private String configParam; // For backward compatibility
    
    // Constructors
    public MailSenderAdapterConfig() {}
    
    public MailSenderAdapterConfig(String mailServerHost, String mailUsername, String mailPassword) {
        this.mailServerHost = mailServerHost;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }
    
    // Essential getters and setters
    public String getMailServerHost() { return mailServerHost; }
    public void setMailServerHost(String mailServerHost) { this.mailServerHost = mailServerHost; }
    
    public String getMailServerPort() { return mailServerPort; }
    public void setMailServerPort(String mailServerPort) { this.mailServerPort = mailServerPort; }
    
    public String getMailProtocol() { return mailProtocol; }
    public void setMailProtocol(String mailProtocol) { this.mailProtocol = mailProtocol; }
    
    public String getMailUsername() { return mailUsername; }
    public void setMailUsername(String mailUsername) { this.mailUsername = mailUsername; }
    
    public String getMailPassword() { return mailPassword; }
    public void setMailPassword(String mailPassword) { this.mailPassword = mailPassword; }
    
    public boolean isUseSSLTLS() { return useSSLTLS; }
    public void setUseSSLTLS(boolean useSSLTLS) { this.useSSLTLS = useSSLTLS; }
    
    public String getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(String connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public String getReadTimeout() { return readTimeout; }
    public void setReadTimeout(String readTimeout) { this.readTimeout = readTimeout; }
    
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
    
    public String[] getAdditionalFolders() { return additionalFolders; }
    public void setAdditionalFolders(String[] additionalFolders) { this.additionalFolders = additionalFolders; }
    
    public boolean isMonitorSubfolders() { return monitorSubfolders; }
    public void setMonitorSubfolders(boolean monitorSubfolders) { this.monitorSubfolders = monitorSubfolders; }
    
    public String getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(String pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public String getMaxMessages() { return maxMessages; }
    public void setMaxMessages(String maxMessages) { this.maxMessages = maxMessages; }
    
    public String getSearchCriteria() { return searchCriteria; }
    public void setSearchCriteria(String searchCriteria) { this.searchCriteria = searchCriteria; }
    
    public boolean isFetchUnreadOnly() { return fetchUnreadOnly; }
    public void setFetchUnreadOnly(boolean fetchUnreadOnly) { this.fetchUnreadOnly = fetchUnreadOnly; }
    
    public boolean isFetchFromToday() { return fetchFromToday; }
    public void setFetchFromToday(boolean fetchFromToday) { this.fetchFromToday = fetchFromToday; }
    
    public String getDateFromFilter() { return dateFromFilter; }
    public void setDateFromFilter(String dateFromFilter) { this.dateFromFilter = dateFromFilter; }
    
    public String getDateToFilter() { return dateToFilter; }
    public void setDateToFilter(String dateToFilter) { this.dateToFilter = dateToFilter; }
    
    public String getSubjectFilter() { return subjectFilter; }
    public void setSubjectFilter(String subjectFilter) { this.subjectFilter = subjectFilter; }
    
    public String getFromAddressFilter() { return fromAddressFilter; }
    public void setFromAddressFilter(String fromAddressFilter) { this.fromAddressFilter = fromAddressFilter; }
    
    public String[] getRequiredAttachments() { return requiredAttachments; }
    public void setRequiredAttachments(String[] requiredAttachments) { this.requiredAttachments = requiredAttachments; }
    
    public String getContentHandling() { return contentHandling; }
    public void setContentHandling(String contentHandling) { this.contentHandling = contentHandling; }
    
    public String getMailEncoding() { return mailEncoding; }
    public void setMailEncoding(String mailEncoding) { this.mailEncoding = mailEncoding; }
    
    public boolean isIncludeAttachments() { return includeAttachments; }
    public void setIncludeAttachments(boolean includeAttachments) { this.includeAttachments = includeAttachments; }
    
    public String getAttachmentDirectory() { return attachmentDirectory; }
    public void setAttachmentDirectory(String attachmentDirectory) { this.attachmentDirectory = attachmentDirectory; }
    
    public boolean isDeleteAfterFetch() { return deleteAfterFetch; }
    public void setDeleteAfterFetch(boolean deleteAfterFetch) { this.deleteAfterFetch = deleteAfterFetch; }
    
    public boolean isMarkAsRead() { return markAsRead; }
    public void setMarkAsRead(boolean markAsRead) { this.markAsRead = markAsRead; }
    
    public String getProcessedFolder() { return processedFolder; }
    public void setProcessedFolder(String processedFolder) { this.processedFolder = processedFolder; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
    public String getLastProcessedMessageId() { return lastProcessedMessageId; }
    public void setLastProcessedMessageId(String lastProcessedMessageId) { this.lastProcessedMessageId = lastProcessedMessageId; }
    
    public long getLastProcessedTimestamp() { return lastProcessedTimestamp; }
    public void setLastProcessedTimestamp(long lastProcessedTimestamp) { this.lastProcessedTimestamp = lastProcessedTimestamp; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getErrorFolder() { return errorFolder; }
    public void setErrorFolder(String errorFolder) { this.errorFolder = errorFolder; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxErrorThreshold() { return maxErrorThreshold; }
    public void setMaxErrorThreshold(int maxErrorThreshold) { this.maxErrorThreshold = maxErrorThreshold; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public String getAuthenticationMethod() { return authenticationMethod; }
    public void setAuthenticationMethod(String authenticationMethod) { this.authenticationMethod = authenticationMethod; }
    
    public String getOauthClientId() { return oauthClientId; }
    public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }
    
    public String getOauthClientSecret() { return oauthClientSecret; }
    public void setOauthClientSecret(String oauthClientSecret) { this.oauthClientSecret = oauthClientSecret; }
    
    public String getOauthRefreshToken() { return oauthRefreshToken; }
    public void setOauthRefreshToken(String oauthRefreshToken) { this.oauthRefreshToken = oauthRefreshToken; }
    
    public String getOauthAccessToken() { return oauthAccessToken; }
    public void setOauthAccessToken(String oauthAccessToken) { this.oauthAccessToken = oauthAccessToken; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public long getConnectionPoolTimeout() { return connectionPoolTimeout; }
    public void setConnectionPoolTimeout(long connectionPoolTimeout) { this.connectionPoolTimeout = connectionPoolTimeout; }
    
    public boolean isUseConnectionPool() { return useConnectionPool; }
    public void setUseConnectionPool(boolean useConnectionPool) { this.useConnectionPool = useConnectionPool; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogProcessedMessages() { return logProcessedMessages; }
    public void setLogProcessedMessages(boolean logProcessedMessages) { this.logProcessedMessages = logProcessedMessages; }
    
    public boolean isLogMessageContent() { return logMessageContent; }
    public void setLogMessageContent(boolean logMessageContent) { this.logMessageContent = logMessageContent; }
    
    public long getSlowProcessingThresholdMs() { return slowProcessingThresholdMs; }
    public void setSlowProcessingThresholdMs(long slowProcessingThresholdMs) { this.slowProcessingThresholdMs = slowProcessingThresholdMs; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    @Override
    public String toString() {
        return String.format("MailSenderAdapterConfig{server='%s', protocol='%s', folder='%s', polling=%sms}",
                mailServerHost, mailProtocol, folderName, pollingInterval);
    }
}