package com.integrixs.adapters.config;

/**
 * Configuration for JMS Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on consuming messages from JMS queues/topics.
 */
public class JmsSenderAdapterConfig {

    // Connection Details for Message Consumption
    private String connectionFactoryClass;
    private String queueClass;
    private String queueManager;
    private String host;
    private int port;
    private String channel;
    private String connectionFactory; // JNDI name
    private String initialContextFactory; // Initial context factory class
    private String providerUrl; // JNDI provider URL
    private String jndiProperties; // Additional JNDI properties
    private String transportType = "CLIENT"; // CLIENT, BINDINGS
    
    // Authentication
    private String username;
    private String password;
    
    // Source Queue/Topic Configuration
    private String sourceQueueName;
    private String sourceTopicName;
    private String destinationType = "QUEUE"; // QUEUE, TOPIC
    private String messageSelector; // SQL-like selector for filtering messages
    private boolean durableSubscription = false; // For topic subscriptions
    private String subscriptionName; // Name for durable subscriptions
    
    // Message Consumption Configuration
    private String messageFormat = "TEXT"; // TEXT, BYTES, OBJECT
    private String messageEncoding = "UTF-8";
    private String acknowledgmentMode = "AUTO_ACKNOWLEDGE"; // AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE
    
    // Consumer Settings
    private Long pollingInterval = 1000L; // 1 second default
    private int maxMessages = 100; // Maximum messages to consume per poll
    private boolean enablePolling = true;
    private String consumerType = "SYNC"; // SYNC, ASYNC
    private boolean enableBatchProcessing = false; // Enable batch message processing
    private Integer batchSize = 10; // Messages per batch
    private long receiveTimeout = 5000L; // Timeout for receiving messages (ms)
    
    // Connection Pool Settings for Consumer
    private int maxConnections = 10;
    private int minConnections = 1;
    private long connectionTimeout = 30000; // 30 seconds
    private long idleTimeout = 300000; // 5 minutes
    
    // Transaction Settings for Message Consumption
    private String transactionMode = "AUTO_ACKNOWLEDGE";
    private boolean useTransactions = false;
    private long transactionTimeout = 30000; // 30 seconds
    
    // Error Handling for Message Consumption
    private String errorHandlingStrategy = "FAIL_FAST";
    private String retryPolicy = "EXPONENTIAL_BACKOFF";
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    private String deadLetterQueue; // Queue for failed messages
    private boolean enableDeadLetterQueue = false;
    
    // Message Processing Configuration
    private boolean validateMessage = true;
    private String messageValidationSchema;
    private boolean logMessageContent = false;
    private boolean enableDuplicateDetection = false;
    private String duplicateDetectionProperty = "JMSMessageID";
    
    // Performance and Monitoring
    private boolean enableMetrics = true;
    private long slowProcessingThresholdMs = 10000; // 10 seconds
    private int concurrentConsumers = 1; // Number of concurrent consumers
    
    // Certificate and Security
    private String certificateId;
    private String sslConfig;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
    
    // Business Context
    private String businessComponentId;
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public JmsSenderAdapterConfig() {}
    
    public JmsSenderAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getConnectionFactoryClass() { return connectionFactoryClass; }
    public void setConnectionFactoryClass(String connectionFactoryClass) { this.connectionFactoryClass = connectionFactoryClass; }
    
    public String getQueueClass() { return queueClass; }
    public void setQueueClass(String queueClass) { this.queueClass = queueClass; }
    
    public String getQueueManager() { return queueManager; }
    public void setQueueManager(String queueManager) { this.queueManager = queueManager; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getConnectionFactory() { return connectionFactory; }
    public void setConnectionFactory(String connectionFactory) { this.connectionFactory = connectionFactory; }
    
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getSourceQueueName() { return sourceQueueName; }
    public void setSourceQueueName(String sourceQueueName) { this.sourceQueueName = sourceQueueName; }
    
    public String getSourceTopicName() { return sourceTopicName; }
    public void setSourceTopicName(String sourceTopicName) { this.sourceTopicName = sourceTopicName; }
    
    public String getDestinationType() { return destinationType; }
    public void setDestinationType(String destinationType) { this.destinationType = destinationType; }
    
    public String getMessageSelector() { return messageSelector; }
    public void setMessageSelector(String messageSelector) { this.messageSelector = messageSelector; }
    
    public String getMessageFormat() { return messageFormat; }
    public void setMessageFormat(String messageFormat) { this.messageFormat = messageFormat; }
    
    public String getMessageEncoding() { return messageEncoding; }
    public void setMessageEncoding(String messageEncoding) { this.messageEncoding = messageEncoding; }
    
    public String getAcknowledgmentMode() { return acknowledgmentMode; }
    public void setAcknowledgmentMode(String acknowledgmentMode) { this.acknowledgmentMode = acknowledgmentMode; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public int getMaxMessages() { return maxMessages; }
    public void setMaxMessages(int maxMessages) { this.maxMessages = maxMessages; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getConsumerType() { return consumerType; }
    public void setConsumerType(String consumerType) { this.consumerType = consumerType; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getMinConnections() { return minConnections; }
    public void setMinConnections(int minConnections) { this.minConnections = minConnections; }
    
    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
    
    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }
    
    public boolean isUseTransactions() { return useTransactions; }
    public void setUseTransactions(boolean useTransactions) { this.useTransactions = useTransactions; }
    
    public long getTransactionTimeout() { return transactionTimeout; }
    public void setTransactionTimeout(long transactionTimeout) { this.transactionTimeout = transactionTimeout; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public String getDeadLetterQueue() { return deadLetterQueue; }
    public void setDeadLetterQueue(String deadLetterQueue) { this.deadLetterQueue = deadLetterQueue; }
    
    public boolean isEnableDeadLetterQueue() { return enableDeadLetterQueue; }
    public void setEnableDeadLetterQueue(boolean enableDeadLetterQueue) { this.enableDeadLetterQueue = enableDeadLetterQueue; }
    
    public boolean isValidateMessage() { return validateMessage; }
    public void setValidateMessage(boolean validateMessage) { this.validateMessage = validateMessage; }
    
    public String getMessageValidationSchema() { return messageValidationSchema; }
    public void setMessageValidationSchema(String messageValidationSchema) { this.messageValidationSchema = messageValidationSchema; }
    
    public boolean isLogMessageContent() { return logMessageContent; }
    public void setLogMessageContent(boolean logMessageContent) { this.logMessageContent = logMessageContent; }
    
    public boolean isEnableDuplicateDetection() { return enableDuplicateDetection; }
    public void setEnableDuplicateDetection(boolean enableDuplicateDetection) { this.enableDuplicateDetection = enableDuplicateDetection; }
    
    public String getDuplicateDetectionProperty() { return duplicateDetectionProperty; }
    public void setDuplicateDetectionProperty(String duplicateDetectionProperty) { this.duplicateDetectionProperty = duplicateDetectionProperty; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getSlowProcessingThresholdMs() { return slowProcessingThresholdMs; }
    public void setSlowProcessingThresholdMs(long slowProcessingThresholdMs) { this.slowProcessingThresholdMs = slowProcessingThresholdMs; }
    
    public int getConcurrentConsumers() { return concurrentConsumers; }
    public void setConcurrentConsumers(int concurrentConsumers) { this.concurrentConsumers = concurrentConsumers; }
    
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
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getQueueName() { return sourceQueueName; }
    public String getTopicName() { return sourceTopicName; }
    public String getPollingIntervalStr() { return pollingInterval != null ? pollingInterval.toString() : null; }
    public String getMaxMessagesStr() { return String.valueOf(maxMessages); }
    
    // Additional methods needed by adapter
    public String getDestinationName() { 
        return sourceQueueName != null ? sourceQueueName : sourceTopicName; 
    }
    public boolean isEnableBatchReceive() { return enableBatchProcessing; }
    public Integer getBatchSize() { return batchSize; }
    public long getReceiveTimeout() { return receiveTimeout; }
    public int getAcknowledgementMode() {
        switch (acknowledgmentMode.toUpperCase()) {
            case "AUTO_ACKNOWLEDGE": return 1;
            case "CLIENT_ACKNOWLEDGE": return 2;
            case "DUPS_OK_ACKNOWLEDGE": return 3;
            default: return 1;
        }
    }
    public String getClientId() { return "JmsSender-" + System.currentTimeMillis(); }
    public boolean isTransacted() { return useTransactions; }
    public boolean isDurableSubscription() { return durableSubscription; }
    public String getSubscriptionName() { return subscriptionName; }
    public String getJndiName() { return connectionFactory; }
    public String getInitialContextFactory() { return initialContextFactory; }
    public String getProviderUrl() { return providerUrl; }
    public String getJndiProperties() { return jndiProperties; }
    public void setAcknowledgementMode(int mode) {
        switch (mode) {
            case 1: acknowledgmentMode = "AUTO_ACKNOWLEDGE"; break;
            case 2: acknowledgmentMode = "CLIENT_ACKNOWLEDGE"; break;
            case 3: acknowledgmentMode = "DUPS_OK_ACKNOWLEDGE"; break;
            default: acknowledgmentMode = "AUTO_ACKNOWLEDGE";
        }
    }
    public void setReceiveTimeout(int timeout) { this.receiveTimeout = timeout; }
    
    @Override
    public String toString() {
        return String.format("JmsSenderAdapterConfig{host='%s:%d', queueManager='%s', sourceQueue='%s', transport='%s'}",
                host, port, queueManager, sourceQueueName, transportType);
    }
}