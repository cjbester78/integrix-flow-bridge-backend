package com.integrationlab.adapters.config;

/**
 * Configuration for JMS Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on producing messages to JMS queues/topics.
 */
public class JmsReceiverAdapterConfig {

    // Target Connection Details for Message Production
    private String targetConnectionFactoryClass;
    private String targetQueueClass;
    private String targetQueueManager;
    private String targetHost;
    private int targetPort;
    private String targetChannel;
    private String targetConnectionFactory; // JNDI name
    private String transportType = "CLIENT"; // CLIENT, BINDINGS
    
    // Target Authentication
    private String targetUsername;
    private String targetPassword;
    
    // Target Queue/Topic Configuration
    private String targetQueueName;
    private String targetTopicName;
    private String destinationType = "QUEUE"; // QUEUE, TOPIC
    
    // Message Production Configuration
    private String messageFormat = "TEXT"; // TEXT, BYTES, OBJECT
    private String messageEncoding = "UTF-8";
    private String deliveryMode = "PERSISTENT"; // PERSISTENT, NON_PERSISTENT
    private int priority = 4; // Default JMS priority (0-9)
    private long timeToLive = 0; // 0 = no expiration (milliseconds)
    
    // Producer Settings
    private String producerType = "SYNC"; // SYNC, ASYNC
    private boolean enableBatching = false;
    private int batchSize = 100;
    private long batchTimeoutMs = 5000; // 5 seconds
    
    // Connection Pool Settings for Producer
    private int maxConnections = 10;
    private int minConnections = 1;
    private long connectionTimeout = 30000; // 30 seconds
    private long idleTimeout = 300000; // 5 minutes
    private boolean enableConnectionPooling = true;
    
    // Transaction Settings for Message Production
    private String transactionMode = "AUTO_ACKNOWLEDGE";
    private boolean useTransactions = false;
    private long transactionTimeout = 30000; // 30 seconds
    
    // Message Properties and Headers
    private String customMessageProperties; // JSON string of custom properties
    private String customMessageHeaders; // JSON string of custom headers
    private boolean includeCorrelationId = false;
    private String correlationIdPattern; // Pattern for generating correlation IDs
    private boolean includeTimestamp = true;
    
    // Error Handling for Message Production
    private String errorHandlingStrategy = "FAIL_FAST";
    private String retryPolicy = "EXPONENTIAL_BACKOFF";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private boolean continueOnError = false;
    
    // Message Validation and Processing
    private boolean validateMessage = true;
    private String messageValidationSchema;
    private boolean logMessageContent = false;
    private String messageTransformation; // Optional message transformation logic
    
    // Performance and Monitoring
    private boolean enableMetrics = true;
    private long slowSendThresholdMs = 5000; // 5 seconds
    private int concurrentProducers = 1; // Number of concurrent producers
    private boolean enableCompressionForLargeMessages = false;
    private long compressionThresholdBytes = 10240; // 10KB
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target JMS system
    private String operationType = "SEND"; // SEND, PUBLISH, REQUEST_REPLY
    private boolean idempotent = false;
    private String idempotencyStrategy = "messageId"; // messageId, correlationId, custom
    
    // Certificate and Security
    private String certificateId;
    private String sslConfig;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId;
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public JmsReceiverAdapterConfig() {}
    
    public JmsReceiverAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getTargetConnectionFactoryClass() { return targetConnectionFactoryClass; }
    public void setTargetConnectionFactoryClass(String targetConnectionFactoryClass) { this.targetConnectionFactoryClass = targetConnectionFactoryClass; }
    
    public String getTargetQueueClass() { return targetQueueClass; }
    public void setTargetQueueClass(String targetQueueClass) { this.targetQueueClass = targetQueueClass; }
    
    public String getTargetQueueManager() { return targetQueueManager; }
    public void setTargetQueueManager(String targetQueueManager) { this.targetQueueManager = targetQueueManager; }
    
    public String getTargetHost() { return targetHost; }
    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }
    
    public int getTargetPort() { return targetPort; }
    public void setTargetPort(int targetPort) { this.targetPort = targetPort; }
    
    public String getTargetChannel() { return targetChannel; }
    public void setTargetChannel(String targetChannel) { this.targetChannel = targetChannel; }
    
    public String getTargetConnectionFactory() { return targetConnectionFactory; }
    public void setTargetConnectionFactory(String targetConnectionFactory) { this.targetConnectionFactory = targetConnectionFactory; }
    
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    
    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }
    
    public String getTargetQueueName() { return targetQueueName; }
    public void setTargetQueueName(String targetQueueName) { this.targetQueueName = targetQueueName; }
    
    public String getTargetTopicName() { return targetTopicName; }
    public void setTargetTopicName(String targetTopicName) { this.targetTopicName = targetTopicName; }
    
    public String getDestinationType() { return destinationType; }
    public void setDestinationType(String destinationType) { this.destinationType = destinationType; }
    
    public String getMessageFormat() { return messageFormat; }
    public void setMessageFormat(String messageFormat) { this.messageFormat = messageFormat; }
    
    public String getMessageEncoding() { return messageEncoding; }
    public void setMessageEncoding(String messageEncoding) { this.messageEncoding = messageEncoding; }
    
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public long getTimeToLive() { return timeToLive; }
    public void setTimeToLive(long timeToLive) { this.timeToLive = timeToLive; }
    
    public String getProducerType() { return producerType; }
    public void setProducerType(String producerType) { this.producerType = producerType; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getMinConnections() { return minConnections; }
    public void setMinConnections(int minConnections) { this.minConnections = minConnections; }
    
    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }
    
    public boolean isUseTransactions() { return useTransactions; }
    public void setUseTransactions(boolean useTransactions) { this.useTransactions = useTransactions; }
    
    public long getTransactionTimeout() { return transactionTimeout; }
    public void setTransactionTimeout(long transactionTimeout) { this.transactionTimeout = transactionTimeout; }
    
    public String getCustomMessageProperties() { return customMessageProperties; }
    public void setCustomMessageProperties(String customMessageProperties) { this.customMessageProperties = customMessageProperties; }
    
    public String getCustomMessageHeaders() { return customMessageHeaders; }
    public void setCustomMessageHeaders(String customMessageHeaders) { this.customMessageHeaders = customMessageHeaders; }
    
    public boolean isIncludeCorrelationId() { return includeCorrelationId; }
    public void setIncludeCorrelationId(boolean includeCorrelationId) { this.includeCorrelationId = includeCorrelationId; }
    
    public String getCorrelationIdPattern() { return correlationIdPattern; }
    public void setCorrelationIdPattern(String correlationIdPattern) { this.correlationIdPattern = correlationIdPattern; }
    
    public boolean isIncludeTimestamp() { return includeTimestamp; }
    public void setIncludeTimestamp(boolean includeTimestamp) { this.includeTimestamp = includeTimestamp; }
    
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
    
    public boolean isValidateMessage() { return validateMessage; }
    public void setValidateMessage(boolean validateMessage) { this.validateMessage = validateMessage; }
    
    public String getMessageValidationSchema() { return messageValidationSchema; }
    public void setMessageValidationSchema(String messageValidationSchema) { this.messageValidationSchema = messageValidationSchema; }
    
    public boolean isLogMessageContent() { return logMessageContent; }
    public void setLogMessageContent(boolean logMessageContent) { this.logMessageContent = logMessageContent; }
    
    public String getMessageTransformation() { return messageTransformation; }
    public void setMessageTransformation(String messageTransformation) { this.messageTransformation = messageTransformation; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowSendThresholdMs() { return slowSendThresholdMs; }
    public void setSlowSendThresholdMs(long slowSendThresholdMs) { this.slowSendThresholdMs = slowSendThresholdMs; }
    
    public int getConcurrentProducers() { return concurrentProducers; }
    public void setConcurrentProducers(int concurrentProducers) { this.concurrentProducers = concurrentProducers; }
    
    public boolean isEnableCompressionForLargeMessages() { return enableCompressionForLargeMessages; }
    public void setEnableCompressionForLargeMessages(boolean enableCompressionForLargeMessages) { this.enableCompressionForLargeMessages = enableCompressionForLargeMessages; }
    
    public long getCompressionThresholdBytes() { return compressionThresholdBytes; }
    public void setCompressionThresholdBytes(long compressionThresholdBytes) { this.compressionThresholdBytes = compressionThresholdBytes; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyStrategy() { return idempotencyStrategy; }
    public void setIdempotencyStrategy(String idempotencyStrategy) { this.idempotencyStrategy = idempotencyStrategy; }
    
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    
    public String getSslConfig() { return sslConfig; }
    public void setSslConfig(String sslConfig) { this.sslConfig = sslConfig; }
    
    public String getKeystorePath() { return keystorePath; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
    
    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
    
    public String getTruststorePath() { return truststorePath; }
    public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
    
    public String getTruststorePassword() { return truststorePassword; }
    public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getHost() { return targetHost; }
    public int getPort() { return targetPort; }
    public String getQueueManager() { return targetQueueManager; }
    public String getQueueName() { return targetQueueName; }
    public String getTopicName() { return targetTopicName; }
    public String getUsername() { return targetUsername; }
    public String getPassword() { return targetPassword; }
    public String getConnectionFactory() { return targetConnectionFactory; }
    
    @Override
    public String toString() {
        return String.format("JmsReceiverAdapterConfig{targetHost='%s:%d', queueManager='%s', targetQueue='%s', delivery='%s'}",
                targetHost, targetPort, targetQueueManager, targetQueueName, deliveryMode);
    }
}