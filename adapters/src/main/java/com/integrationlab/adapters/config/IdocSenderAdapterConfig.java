package com.integrationlab.adapters.config;

/**
 * Configuration for IDOC Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on receiving IDOCs from SAP systems.
 */
public class IdocSenderAdapterConfig {

    // SAP Source System Identification
    private String sourceSapSystemId; // SID of source system
    private String sourceSapClientNumber;
    private String sourceSapSystemNumber;
    
    // Source Connection Details
    private String sourceSapApplicationServerHost;
    private String sourceSapGatewayHost;
    private String sourceSapGatewayService;
    private String sourcePortNumber;
    private String connectionType = "TCP/IP"; // TCP/IP, Gateway
    
    // Source Authentication
    private String sourceUsername;
    private String sourcePassword;
    private String sourceLanguage = "EN"; // Default language
    
    // IDOC Configuration for Source
    private String sourceIdocType;
    private String sourceMessageType;
    private String sourceBasicType;
    private String sourceExtension;
    private String sourceReleaseVersion;
    private String sourcePartnerNumber;
    private String sourcePartnerType;
    private String sourcePartnerRole;
    
    // Processing Parameters for Inbound
    private String processingMode = "asynchronous"; // synchronous, asynchronous
    private String qualityOfService = "ExactlyOnce"; // BestEffort, ExactlyOnce, ExactlyOnceInOrder
    private String serialization = "XML"; // XML, EDI, SAP
    private String transactionalMode = "transactional"; // transactional, non-transactional
    private String allowedIdocTypes; // Comma-separated list of allowed IDoc types
    private boolean enableTidManagement = true; // Enable transaction ID management
    
    // Polling Configuration
    private long pollingInterval = 60000L; // 1 minute default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    private int maxIdocsPerPoll = 100; // Limit IDOCs per polling cycle
    private String pollingStrategy = "STATUS_BASED"; // STATUS_BASED, TIME_BASED, SEQUENCE_BASED
    
    // Connection Pool and Performance
    private int maxConnections = 10;
    private int connectionTimeout = 30000; // 30 seconds
    private int idocReceiveTimeout = 60000; // 60 seconds
    private boolean enableConnectionPooling = true;
    private long connectionIdleTime = 300000; // 5 minutes
    
    // IDOC Structure and Mapping
    private String idocStructureMapping; // Mapping configuration
    private String characterEncoding = "UTF-8";
    private boolean validateIdocStructure = true;
    private String validationSchema; // XSD or other validation schema
    
    // Source System Specific Settings
    private String sourceSystem; // Name/identifier of source SAP system
    private String sourceLogicalSystem;
    private String senderPort;
    private String senderPartnerProfile;
    
    // Error Handling for Inbound
    private String errorHandlingStrategy = "FAIL_FAST";
    private boolean continueOnError = false;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    private String errorDirectory; // Directory for failed IDOCs
    private boolean archiveFailedIdocs = true;
    
    // Status and Monitoring
    private boolean enableStatusTracking = true;
    private String statusReportingLevel = "ALL"; // ALL, ERRORS_ONLY, NONE
    private boolean enablePerformanceMetrics = true;
    private long slowProcessingThresholdMs = 10000;
    
    // File Input (for file-based IDOC processing)
    private String inputDirectory;
    private String inputFilePattern = "*.xml";
    private String processedDirectory;
    private boolean createProcessedDirectory = false;
    private boolean deleteProcessedFiles = false;
    
    // Incremental Processing
    private String lastProcessedTimestamp; // ISO timestamp of last processed IDOC
    private String lastProcessedDocNumber; // Document number of last processed IDOC
    private boolean resetIncrementalOnStart = false;
    
    // IDOC Server Configuration (for direct IDOC receiving)
    private String idocServerName; // IDOC server name registered in SAP
    private String idocServerHost; // Host where middleware IDOC server runs
    private String idocServerPort; // Port for IDOC server
    private String programId; // Program ID registered in SAP gateway
    private int maxConcurrentIdocs = 50; // Maximum concurrent IDOC processing
    
    // Business Context
    private String businessComponentId;
    private String sourceDataStructureId;
    private String operationType = "RECEIVE"; // RECEIVE, POLL, LISTEN
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public IdocSenderAdapterConfig() {}
    
    public IdocSenderAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getSourceSapSystemId() { return sourceSapSystemId; }
    public void setSourceSapSystemId(String sourceSapSystemId) { this.sourceSapSystemId = sourceSapSystemId; }
    
    public String getSourceSapClientNumber() { return sourceSapClientNumber; }
    public void setSourceSapClientNumber(String sourceSapClientNumber) { this.sourceSapClientNumber = sourceSapClientNumber; }
    
    public String getSourceSapSystemNumber() { return sourceSapSystemNumber; }
    public void setSourceSapSystemNumber(String sourceSapSystemNumber) { this.sourceSapSystemNumber = sourceSapSystemNumber; }
    
    public String getSourceSapApplicationServerHost() { return sourceSapApplicationServerHost; }
    public void setSourceSapApplicationServerHost(String sourceSapApplicationServerHost) { this.sourceSapApplicationServerHost = sourceSapApplicationServerHost; }
    
    public String getSourceSapGatewayHost() { return sourceSapGatewayHost; }
    public void setSourceSapGatewayHost(String sourceSapGatewayHost) { this.sourceSapGatewayHost = sourceSapGatewayHost; }
    
    public String getSourceSapGatewayService() { return sourceSapGatewayService; }
    public void setSourceSapGatewayService(String sourceSapGatewayService) { this.sourceSapGatewayService = sourceSapGatewayService; }
    
    public String getSourcePortNumber() { return sourcePortNumber; }
    public void setSourcePortNumber(String sourcePortNumber) { this.sourcePortNumber = sourcePortNumber; }
    
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    
    public String getSourceUsername() { return sourceUsername; }
    public void setSourceUsername(String sourceUsername) { this.sourceUsername = sourceUsername; }
    
    public String getSourcePassword() { return sourcePassword; }
    public void setSourcePassword(String sourcePassword) { this.sourcePassword = sourcePassword; }
    
    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    
    public String getSourceIdocType() { return sourceIdocType; }
    public void setSourceIdocType(String sourceIdocType) { this.sourceIdocType = sourceIdocType; }
    
    public String getSourceMessageType() { return sourceMessageType; }
    public void setSourceMessageType(String sourceMessageType) { this.sourceMessageType = sourceMessageType; }
    
    public String getSourceBasicType() { return sourceBasicType; }
    public void setSourceBasicType(String sourceBasicType) { this.sourceBasicType = sourceBasicType; }
    
    public String getSourceExtension() { return sourceExtension; }
    public void setSourceExtension(String sourceExtension) { this.sourceExtension = sourceExtension; }
    
    public String getSourceReleaseVersion() { return sourceReleaseVersion; }
    public void setSourceReleaseVersion(String sourceReleaseVersion) { this.sourceReleaseVersion = sourceReleaseVersion; }
    
    public String getSourcePartnerNumber() { return sourcePartnerNumber; }
    public void setSourcePartnerNumber(String sourcePartnerNumber) { this.sourcePartnerNumber = sourcePartnerNumber; }
    
    public String getSourcePartnerType() { return sourcePartnerType; }
    public void setSourcePartnerType(String sourcePartnerType) { this.sourcePartnerType = sourcePartnerType; }
    
    public String getSourcePartnerRole() { return sourcePartnerRole; }
    public void setSourcePartnerRole(String sourcePartnerRole) { this.sourcePartnerRole = sourcePartnerRole; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public String getQualityOfService() { return qualityOfService; }
    public void setQualityOfService(String qualityOfService) { this.qualityOfService = qualityOfService; }
    
    public String getSerialization() { return serialization; }
    public void setSerialization(String serialization) { this.serialization = serialization; }
    
    public String getTransactionalMode() { return transactionalMode; }
    public void setTransactionalMode(String transactionalMode) { this.transactionalMode = transactionalMode; }
    
    public long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public int getMaxIdocsPerPoll() { return maxIdocsPerPoll; }
    public void setMaxIdocsPerPoll(int maxIdocsPerPoll) { this.maxIdocsPerPoll = maxIdocsPerPoll; }
    
    public String getPollingStrategy() { return pollingStrategy; }
    public void setPollingStrategy(String pollingStrategy) { this.pollingStrategy = pollingStrategy; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getIdocReceiveTimeout() { return idocReceiveTimeout; }
    public void setIdocReceiveTimeout(int idocReceiveTimeout) { this.idocReceiveTimeout = idocReceiveTimeout; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public long getConnectionIdleTime() { return connectionIdleTime; }
    public void setConnectionIdleTime(long connectionIdleTime) { this.connectionIdleTime = connectionIdleTime; }
    
    public String getIdocStructureMapping() { return idocStructureMapping; }
    public void setIdocStructureMapping(String idocStructureMapping) { this.idocStructureMapping = idocStructureMapping; }
    
    public String getCharacterEncoding() { return characterEncoding; }
    public void setCharacterEncoding(String characterEncoding) { this.characterEncoding = characterEncoding; }
    
    public boolean isValidateIdocStructure() { return validateIdocStructure; }
    public void setValidateIdocStructure(boolean validateIdocStructure) { this.validateIdocStructure = validateIdocStructure; }
    
    public String getValidationSchema() { return validationSchema; }
    public void setValidationSchema(String validationSchema) { this.validationSchema = validationSchema; }
    
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    
    public String getSourceLogicalSystem() { return sourceLogicalSystem; }
    public void setSourceLogicalSystem(String sourceLogicalSystem) { this.sourceLogicalSystem = sourceLogicalSystem; }
    
    public String getSenderPort() { return senderPort; }
    public void setSenderPort(String senderPort) { this.senderPort = senderPort; }
    
    public String getSenderPartnerProfile() { return senderPartnerProfile; }
    public void setSenderPartnerProfile(String senderPartnerProfile) { this.senderPartnerProfile = senderPartnerProfile; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public String getErrorDirectory() { return errorDirectory; }
    public void setErrorDirectory(String errorDirectory) { this.errorDirectory = errorDirectory; }
    
    public boolean isArchiveFailedIdocs() { return archiveFailedIdocs; }
    public void setArchiveFailedIdocs(boolean archiveFailedIdocs) { this.archiveFailedIdocs = archiveFailedIdocs; }
    
    public boolean isEnableStatusTracking() { return enableStatusTracking; }
    public void setEnableStatusTracking(boolean enableStatusTracking) { this.enableStatusTracking = enableStatusTracking; }
    
    public String getStatusReportingLevel() { return statusReportingLevel; }
    public void setStatusReportingLevel(String statusReportingLevel) { this.statusReportingLevel = statusReportingLevel; }
    
    public boolean isEnablePerformanceMetrics() { return enablePerformanceMetrics; }
    public void setEnablePerformanceMetrics(boolean enablePerformanceMetrics) { this.enablePerformanceMetrics = enablePerformanceMetrics; }
    
    public long getSlowProcessingThresholdMs() { return slowProcessingThresholdMs; }
    public void setSlowProcessingThresholdMs(long slowProcessingThresholdMs) { this.slowProcessingThresholdMs = slowProcessingThresholdMs; }
    
    public String getInputDirectory() { return inputDirectory; }
    public void setInputDirectory(String inputDirectory) { this.inputDirectory = inputDirectory; }
    
    public String getInputFilePattern() { return inputFilePattern; }
    public void setInputFilePattern(String inputFilePattern) { this.inputFilePattern = inputFilePattern; }
    
    public String getProcessedDirectory() { return processedDirectory; }
    public void setProcessedDirectory(String processedDirectory) { this.processedDirectory = processedDirectory; }
    
    public boolean isCreateProcessedDirectory() { return createProcessedDirectory; }
    public void setCreateProcessedDirectory(boolean createProcessedDirectory) { this.createProcessedDirectory = createProcessedDirectory; }
    
    public boolean isDeleteProcessedFiles() { return deleteProcessedFiles; }
    public void setDeleteProcessedFiles(boolean deleteProcessedFiles) { this.deleteProcessedFiles = deleteProcessedFiles; }
    
    public String getLastProcessedTimestamp() { return lastProcessedTimestamp; }
    public void setLastProcessedTimestamp(String lastProcessedTimestamp) { this.lastProcessedTimestamp = lastProcessedTimestamp; }
    
    public String getLastProcessedDocNumber() { return lastProcessedDocNumber; }
    public void setLastProcessedDocNumber(String lastProcessedDocNumber) { this.lastProcessedDocNumber = lastProcessedDocNumber; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
    public String getIdocServerName() { return idocServerName; }
    public void setIdocServerName(String idocServerName) { this.idocServerName = idocServerName; }
    
    public String getIdocServerHost() { return idocServerHost; }
    public void setIdocServerHost(String idocServerHost) { this.idocServerHost = idocServerHost; }
    
    public String getIdocServerPort() { return idocServerPort; }
    public void setIdocServerPort(String idocServerPort) { this.idocServerPort = idocServerPort; }
    
    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }
    
    public int getMaxConcurrentIdocs() { return maxConcurrentIdocs; }
    public void setMaxConcurrentIdocs(int maxConcurrentIdocs) { this.maxConcurrentIdocs = maxConcurrentIdocs; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getSourceDataStructureId() { return sourceDataStructureId; }
    public void setSourceDataStructureId(String sourceDataStructureId) { this.sourceDataStructureId = sourceDataStructureId; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getSapSystemId() { return sourceSapSystemId; }
    public String getSapClientNumber() { return sourceSapClientNumber; }
    public String getSapSystemNumber() { return sourceSapSystemNumber; }
    public String getSapApplicationServerHost() { return sourceSapApplicationServerHost; }
    public String getSapGatewayHost() { return sourceSapGatewayHost; }
    public String getSapGatewayService() { return sourceSapGatewayService; }
    public String getPortNumber() { return sourcePortNumber; }
    public String getUsername() { return sourceUsername; }
    public String getPassword() { return sourcePassword; }
    public String getLanguage() { return sourceLanguage; }
    public String getIdocType() { return sourceIdocType; }
    public String getMessageType() { return sourceMessageType; }
    public String getBasicType() { return sourceBasicType; }
    
    // Additional methods needed by adapter
    public String getGatewayHost() { return sourceSapGatewayHost; }
    public String getGatewayService() { return sourceSapGatewayService; }
    public String getAllowedIdocTypes() { return allowedIdocTypes; }
    public boolean isEnableTidManagement() { return enableTidManagement; }
    
    @Override
    public String toString() {
        return String.format("IdocSenderAdapterConfig{sourceSystem='%s', idocType='%s', messageType='%s', polling=%dms}",
                sourceSapSystemId, sourceIdocType, sourceMessageType, pollingInterval);
    }
}