package com.integrationlab.adapters.config;

/**
 * Configuration for Mail Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on sending emails via SMTP servers.
 */
public class MailReceiverAdapterConfig {
    
    // SMTP Server Connection (Target)
    private String smtpServerHost; // SMTP server hostname
    private String smtpServerPort = "587"; // Default SMTP port (587 for TLS, 465 for SSL, 25 for plain)
    private boolean smtpUseSSLTLS = true;
    private String smtpUsername;
    private String smtpPassword;
    private String connectionTimeout = "30000"; // 30 seconds
    private String readTimeout = "60000"; // 60 seconds
    
    // Message Composition and Addressing
    private String fromAddress; // Sender email address
    private String fromName; // Sender display name
    private String replyToAddress; // Reply-to address
    private String toAddresses; // Primary recipients (comma-separated)
    private String ccAddresses; // CC recipients (comma-separated)
    private String bccAddresses; // BCC recipients (comma-separated)
    
    // Message Content
    private String emailSubject; // Email subject template
    private String emailBody; // Email body template
    private String emailBodyTemplate; // Body template with variables
    private String emailEncoding = "UTF-8";
    private String contentType = "text/html"; // text/plain, text/html, multipart/mixed
    private String charset = "UTF-8";
    
    // Attachments
    private String emailAttachments; // Attachment paths (comma-separated)
    private String attachmentDirectory; // Directory for attachment files
    private boolean includeAttachments = false;
    private String attachmentNamingPattern; // Pattern for attachment names
    private long maxAttachmentSize = 25 * 1024 * 1024; // 25MB default
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    
    // Batch Processing
    private Integer batchSize; // Number of emails to send in batch
    private boolean enableBatching = false;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Template Processing
    private boolean enableTemplating = true;
    private String templateEngine = "simple"; // simple, velocity, freemarker
    private String subjectTemplate; // Subject template with variables
    private String bodyTemplate; // Body template with variables
    
    // Message Priority and Headers
    private String messagePriority = "normal"; // high, normal, low
    private String customHeaders; // Custom headers (format: "key1:value1,key2:value2")
    private String messageId; // Custom message ID pattern
    private boolean requestDeliveryReceipt = false;
    private boolean requestReadReceipt = false;
    
    // Authentication and Security
    private String authenticationMethod = "password"; // password, oauth2
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthRefreshToken;
    private String oauthAccessToken;
    private boolean enableStartTLS = true;
    
    // Rate Limiting and Throttling
    private long sendIntervalMs = 0; // Minimum interval between emails
    private int maxEmailsPerMinute = 60; // Rate limiting
    private int maxEmailsPerHour = 1000; // Rate limiting
    private int maxConcurrentSends = 5; // Concurrent sending limit
    
    // Error Handling and Retry
    private String errorHandlingStrategy = "FAIL_FAST";
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private String[] retryableErrors = {"timeout", "connection", "temporary"};
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "SEND"; // SEND, FORWARD, REPLY
    private boolean idempotent = false; // Whether operations are idempotent
    private String idempotencyStrategy = "messageId"; // messageId, checksum, content
    
    // Connection Pool and Performance
    private int maxConnections = 5;
    private long connectionPoolTimeout = 30000L;
    private boolean useConnectionPool = true;
    private boolean keepAlive = true;
    
    // Delivery Tracking
    private boolean enableDeliveryTracking = false;
    private String deliveryTrackingId; // Tracking ID pattern
    private String bounceHandlingAddress; // Address for bounce handling
    private String unsubscribeLink; // Unsubscribe link template
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logSentEmails = true;
    private boolean logEmailContent = false;
    private boolean enableDebug = false; // Enable SMTP debug logging
    private long slowSendThresholdMs = 5000;
    
    // Legacy compatibility
    private String configParam; // For backward compatibility
    
    // Constructors
    public MailReceiverAdapterConfig() {}
    
    public MailReceiverAdapterConfig(String smtpServerHost, String smtpUsername, String smtpPassword, String fromAddress) {
        this.smtpServerHost = smtpServerHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.fromAddress = fromAddress;
    }
    
    // Essential getters and setters
    public String getSmtpServerHost() { return smtpServerHost; }
    public void setSmtpServerHost(String smtpServerHost) { this.smtpServerHost = smtpServerHost; }
    
    public String getSmtpServerPort() { return smtpServerPort; }
    public void setSmtpServerPort(String smtpServerPort) { this.smtpServerPort = smtpServerPort; }
    
    public boolean isSmtpUseSSLTLS() { return smtpUseSSLTLS; }
    public void setSmtpUseSSLTLS(boolean smtpUseSSLTLS) { this.smtpUseSSLTLS = smtpUseSSLTLS; }
    
    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }
    
    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    
    public String getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(String connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public String getReadTimeout() { return readTimeout; }
    public void setReadTimeout(String readTimeout) { this.readTimeout = readTimeout; }
    
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
    
    public String getReplyToAddress() { return replyToAddress; }
    public void setReplyToAddress(String replyToAddress) { this.replyToAddress = replyToAddress; }
    
    public String getToAddresses() { return toAddresses; }
    public void setToAddresses(String toAddresses) { this.toAddresses = toAddresses; }
    
    public String getCcAddresses() { return ccAddresses; }
    public void setCcAddresses(String ccAddresses) { this.ccAddresses = ccAddresses; }
    
    public String getBccAddresses() { return bccAddresses; }
    public void setBccAddresses(String bccAddresses) { this.bccAddresses = bccAddresses; }
    
    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }
    
    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }
    
    public String getEmailBodyTemplate() { return emailBodyTemplate; }
    public void setEmailBodyTemplate(String emailBodyTemplate) { this.emailBodyTemplate = emailBodyTemplate; }
    
    public String getEmailEncoding() { return emailEncoding; }
    public void setEmailEncoding(String emailEncoding) { this.emailEncoding = emailEncoding; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }
    
    public String getEmailAttachments() { return emailAttachments; }
    public void setEmailAttachments(String emailAttachments) { this.emailAttachments = emailAttachments; }
    
    public String getAttachmentDirectory() { return attachmentDirectory; }
    public void setAttachmentDirectory(String attachmentDirectory) { this.attachmentDirectory = attachmentDirectory; }
    
    public boolean isIncludeAttachments() { return includeAttachments; }
    public void setIncludeAttachments(boolean includeAttachments) { this.includeAttachments = includeAttachments; }
    
    public String getAttachmentNamingPattern() { return attachmentNamingPattern; }
    public void setAttachmentNamingPattern(String attachmentNamingPattern) { this.attachmentNamingPattern = attachmentNamingPattern; }
    
    public long getMaxAttachmentSize() { return maxAttachmentSize; }
    public void setMaxAttachmentSize(long maxAttachmentSize) { this.maxAttachmentSize = maxAttachmentSize; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public boolean isEnableTemplating() { return enableTemplating; }
    public void setEnableTemplating(boolean enableTemplating) { this.enableTemplating = enableTemplating; }
    
    public String getTemplateEngine() { return templateEngine; }
    public void setTemplateEngine(String templateEngine) { this.templateEngine = templateEngine; }
    
    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }
    
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    
    public String getMessagePriority() { return messagePriority; }
    public void setMessagePriority(String messagePriority) { this.messagePriority = messagePriority; }
    
    public String getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(String customHeaders) { this.customHeaders = customHeaders; }
    
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public boolean isRequestDeliveryReceipt() { return requestDeliveryReceipt; }
    public void setRequestDeliveryReceipt(boolean requestDeliveryReceipt) { this.requestDeliveryReceipt = requestDeliveryReceipt; }
    
    public boolean isRequestReadReceipt() { return requestReadReceipt; }
    public void setRequestReadReceipt(boolean requestReadReceipt) { this.requestReadReceipt = requestReadReceipt; }
    
    public String getAuthenticationMethod() { return authenticationMethod; }
    public void setAuthenticationMethod(String authenticationMethod) { this.authenticationMethod = authenticationMethod; }
    
    public boolean isEnableStartTLS() { return enableStartTLS; }
    public void setEnableStartTLS(boolean enableStartTLS) { this.enableStartTLS = enableStartTLS; }
    
    public long getSendIntervalMs() { return sendIntervalMs; }
    public void setSendIntervalMs(long sendIntervalMs) { this.sendIntervalMs = sendIntervalMs; }
    
    public int getMaxEmailsPerMinute() { return maxEmailsPerMinute; }
    public void setMaxEmailsPerMinute(int maxEmailsPerMinute) { this.maxEmailsPerMinute = maxEmailsPerMinute; }
    
    public int getMaxEmailsPerHour() { return maxEmailsPerHour; }
    public void setMaxEmailsPerHour(int maxEmailsPerHour) { this.maxEmailsPerHour = maxEmailsPerHour; }
    
    public int getMaxConcurrentSends() { return maxConcurrentSends; }
    public void setMaxConcurrentSends(int maxConcurrentSends) { this.maxConcurrentSends = maxConcurrentSends; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxErrorThreshold() { return maxErrorThreshold; }
    public void setMaxErrorThreshold(int maxErrorThreshold) { this.maxErrorThreshold = maxErrorThreshold; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public String[] getRetryableErrors() { return retryableErrors; }
    public void setRetryableErrors(String[] retryableErrors) { this.retryableErrors = retryableErrors; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyStrategy() { return idempotencyStrategy; }
    public void setIdempotencyStrategy(String idempotencyStrategy) { this.idempotencyStrategy = idempotencyStrategy; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogSentEmails() { return logSentEmails; }
    public void setLogSentEmails(boolean logSentEmails) { this.logSentEmails = logSentEmails; }
    
    public boolean isLogEmailContent() { return logEmailContent; }
    public void setLogEmailContent(boolean logEmailContent) { this.logEmailContent = logEmailContent; }
    
    public long getSlowSendThresholdMs() { return slowSendThresholdMs; }
    public void setSlowSendThresholdMs(long slowSendThresholdMs) { this.slowSendThresholdMs = slowSendThresholdMs; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Additional methods needed by adapter
    public String getEmailTemplate() { return emailBodyTemplate; }
    public String getMailEncoding() { return emailEncoding; }
    public String getToAddress() { return toAddresses; }
    public String getCcAddress() { return ccAddresses; }
    public String getBccAddress() { return bccAddresses; }
    public boolean isUseSSLTLS() { return smtpUseSSLTLS; }
    public boolean isEnableDebug() { return enableDebug; }
    
    @Override
    public String toString() {
        return String.format("MailReceiverAdapterConfig{server='%s', from='%s', batching=%s, operation='%s'}",
                smtpServerHost, fromAddress, enableBatching, operationType);
    }
}