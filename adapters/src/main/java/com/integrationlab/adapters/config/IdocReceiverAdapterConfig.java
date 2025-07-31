package com.integrationlab.adapters.config;

/**
 * Configuration for IDOC Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on sending IDOCs to SAP systems.
 */
public class IdocReceiverAdapterConfig {

    // SAP Target System Identification
    private String targetSapSystemId; // SID of target system
    private String targetSapClientNumber;
    private String targetSapSystemNumber;
    
    // Target Connection Details
    private String targetSapApplicationServerHost;
    private String targetSapGatewayHost;
    private String targetSapGatewayService;
    private String targetPortNumber;
    private String connectionType = "TCP/IP"; // TCP/IP, Gateway
    
    // Target Authentication
    private String targetUsername;
    private String targetPassword;
    private String targetLanguage = "EN"; // Default language
    
    // IDOC Configuration for Target
    private String targetIdocType;
    private String targetMessageType;
    private String targetBasicType;
    private String targetExtension;
    private String targetReleaseVersion;
    private String targetPartnerNumber;
    private String targetPartnerType;
    private String targetPartnerRole;
    
    // Processing Parameters for Outbound
    private String processingMode = "asynchronous"; // synchronous, asynchronous
    private String qualityOfService = "ExactlyOnce"; // BestEffort, ExactlyOnce, ExactlyOnceInOrder
    private String serialization = "XML"; // XML, EDI, SAP
    private String transactionalMode = "transactional"; // transactional, non-transactional
    
    // Connection Pool and Performance
    private int maxConnections = 10;
    private int connectionTimeout = 30000; // 30 seconds
    private int idocSendTimeout = 60000; // 60 seconds
    private boolean enableConnectionPooling = true;
    private long connectionIdleTime = 300000; // 5 minutes
    
    // IDOC Structure and Mapping
    private String idocStructureMapping; // Mapping configuration
    private String characterEncoding = "UTF-8";
    private boolean validateIdocStructure = true;
    private String validationSchema; // XSD or other validation schema
    
    // Target System Specific Settings
    private String targetSystem; // Name/identifier of target SAP system
    private String targetLogicalSystem;
    private String receiverPort;
    private String receiverPartnerProfile;
    
    // Error Handling for Outbound
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
    
    // File Output (for file-based IDOC processing)
    private String outputDirectory;
    private String outputFileNamePattern;
    private String outputArchiveDirectory;
    private boolean createOutputDirectory = false;
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId;
    private String operationType = "SEND"; // SEND, FORWARD, ROUTE
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public IdocReceiverAdapterConfig() {}
    
    public IdocReceiverAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getTargetSapSystemId() { return targetSapSystemId; }
    public void setTargetSapSystemId(String targetSapSystemId) { this.targetSapSystemId = targetSapSystemId; }
    
    public String getTargetSapClientNumber() { return targetSapClientNumber; }
    public void setTargetSapClientNumber(String targetSapClientNumber) { this.targetSapClientNumber = targetSapClientNumber; }
    
    public String getTargetSapSystemNumber() { return targetSapSystemNumber; }
    public void setTargetSapSystemNumber(String targetSapSystemNumber) { this.targetSapSystemNumber = targetSapSystemNumber; }
    
    public String getTargetSapApplicationServerHost() { return targetSapApplicationServerHost; }
    public void setTargetSapApplicationServerHost(String targetSapApplicationServerHost) { this.targetSapApplicationServerHost = targetSapApplicationServerHost; }
    
    public String getTargetSapGatewayHost() { return targetSapGatewayHost; }
    public void setTargetSapGatewayHost(String targetSapGatewayHost) { this.targetSapGatewayHost = targetSapGatewayHost; }
    
    public String getTargetSapGatewayService() { return targetSapGatewayService; }
    public void setTargetSapGatewayService(String targetSapGatewayService) { this.targetSapGatewayService = targetSapGatewayService; }
    
    public String getTargetPortNumber() { return targetPortNumber; }
    public void setTargetPortNumber(String targetPortNumber) { this.targetPortNumber = targetPortNumber; }
    
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    
    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }
    
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    
    public String getTargetIdocType() { return targetIdocType; }
    public void setTargetIdocType(String targetIdocType) { this.targetIdocType = targetIdocType; }
    
    public String getTargetMessageType() { return targetMessageType; }
    public void setTargetMessageType(String targetMessageType) { this.targetMessageType = targetMessageType; }
    
    public String getTargetBasicType() { return targetBasicType; }
    public void setTargetBasicType(String targetBasicType) { this.targetBasicType = targetBasicType; }
    
    public String getTargetExtension() { return targetExtension; }
    public void setTargetExtension(String targetExtension) { this.targetExtension = targetExtension; }
    
    public String getTargetReleaseVersion() { return targetReleaseVersion; }
    public void setTargetReleaseVersion(String targetReleaseVersion) { this.targetReleaseVersion = targetReleaseVersion; }
    
    public String getTargetPartnerNumber() { return targetPartnerNumber; }
    public void setTargetPartnerNumber(String targetPartnerNumber) { this.targetPartnerNumber = targetPartnerNumber; }
    
    public String getTargetPartnerType() { return targetPartnerType; }
    public void setTargetPartnerType(String targetPartnerType) { this.targetPartnerType = targetPartnerType; }
    
    public String getTargetPartnerRole() { return targetPartnerRole; }
    public void setTargetPartnerRole(String targetPartnerRole) { this.targetPartnerRole = targetPartnerRole; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public String getQualityOfService() { return qualityOfService; }
    public void setQualityOfService(String qualityOfService) { this.qualityOfService = qualityOfService; }
    
    public String getSerialization() { return serialization; }
    public void setSerialization(String serialization) { this.serialization = serialization; }
    
    public String getTransactionalMode() { return transactionalMode; }
    public void setTransactionalMode(String transactionalMode) { this.transactionalMode = transactionalMode; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getIdocSendTimeout() { return idocSendTimeout; }
    public void setIdocSendTimeout(int idocSendTimeout) { this.idocSendTimeout = idocSendTimeout; }
    
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
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getTargetLogicalSystem() { return targetLogicalSystem; }
    public void setTargetLogicalSystem(String targetLogicalSystem) { this.targetLogicalSystem = targetLogicalSystem; }
    
    public String getReceiverPort() { return receiverPort; }
    public void setReceiverPort(String receiverPort) { this.receiverPort = receiverPort; }
    
    public String getReceiverPartnerProfile() { return receiverPartnerProfile; }
    public void setReceiverPartnerProfile(String receiverPartnerProfile) { this.receiverPartnerProfile = receiverPartnerProfile; }
    
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
    
    public String getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
    
    public String getOutputFileNamePattern() { return outputFileNamePattern; }
    public void setOutputFileNamePattern(String outputFileNamePattern) { this.outputFileNamePattern = outputFileNamePattern; }
    
    public String getOutputArchiveDirectory() { return outputArchiveDirectory; }
    public void setOutputArchiveDirectory(String outputArchiveDirectory) { this.outputArchiveDirectory = outputArchiveDirectory; }
    
    public boolean isCreateOutputDirectory() { return createOutputDirectory; }
    public void setCreateOutputDirectory(boolean createOutputDirectory) { this.createOutputDirectory = createOutputDirectory; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    @Override
    public String toString() {
        return String.format("IdocReceiverAdapterConfig{targetSystem='%s', idocType='%s', messageType='%s', processing='%s'}",
                targetSapSystemId, targetIdocType, targetMessageType, processingMode);
    }
}