package com.integrationlab.adapters.config;

/**
 * Configuration for FTP Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on connecting TO FTP servers to upload/send files.
 */
public class FtpReceiverAdapterConfig {
    
    // Target FTP Server Connection Details
    private String serverAddress; // FTP server hostname or IP
    private String port = "21"; // Default FTP port
    private String timeout = "30000"; // Connection timeout in milliseconds
    private String connectionSecurity = "plain-ftp"; // explicit-ftps, implicit-ftps, plain-ftp
    private String userName;
    private String password;
    private String connectionMode = "per-file-transfer"; // permanently, per-file-transfer
    
    // File Upload Configuration
    private String targetDirectory = "/"; // Target directory to upload files
    private String targetFileName; // Target file name pattern
    private String fileConstructionMode = "create"; // create, append, replace
    private boolean overwriteExistingFile = false;
    private boolean createFileDirectory = false;
    private String filePlacement = "direct"; // direct, temp-then-move
    private String tempFileExtension = ".tmp"; // Extension for temporary files
    
    // Message/Content Handling
    private String emptyMessageHandling = "process"; // process, ignore, error
    private String fileEncoding = "UTF-8";
    private String contentTransformation; // Optional content transformation rules
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    
    // Batch Processing
    private Integer batchSize; // Number of files to upload in batch
    private boolean enableBatching = false;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Connection Pool and Performance
    private String maxConcurrentConnections = "5";
    private boolean useConnectionPool = false; // FTP typically doesn't use pools
    private boolean enablePassiveMode = true; // Use passive mode for firewalls
    private int transferBufferSize = 8192; // Buffer size for file transfers
    
    // File Management
    private boolean validateBeforeUpload = true;
    private String checksumValidation = "none"; // none, MD5, SHA1, SHA256
    private long maxFileSize = 100 * 1024 * 1024; // 100MB default
    private boolean enableFileBackup = false;
    private String backupDirectory; // Directory to backup files before upload
    
    // Error Handling and Retry
    private String errorHandlingStrategy = "FAIL_FAST";
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    private String[] retryableErrors = {"timeout", "connection", "temporary"};
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "UPLOAD"; // UPLOAD, SYNC, MIRROR
    private boolean idempotent = false; // Whether operations are idempotent
    private String idempotencyStrategy = "filename"; // filename, checksum, content
    
    // File Naming and Organization
    private String fileNamingPattern; // Pattern for generating file names
    private boolean includeTimestamp = false;
    private String timestampFormat = "yyyyMMdd_HHmmss";
    private boolean preserveDirectoryStructure = false;
    private String directoryNamingPattern;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logFileOperations = true;
    private boolean logFileContent = false;
    private long slowOperationThresholdMs = 30000;
    
    // Legacy compatibility
    private String configParam; // For backward compatibility
    
    // Constructors
    public FtpReceiverAdapterConfig() {}
    
    public FtpReceiverAdapterConfig(String serverAddress, String userName, String password, String targetDirectory) {
        this.serverAddress = serverAddress;
        this.userName = userName;
        this.password = password;
        this.targetDirectory = targetDirectory;
    }
    
    // Getters and Setters
    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
    
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    
    public String getTimeout() { return timeout; }
    public void setTimeout(String timeout) { this.timeout = timeout; }
    
    public String getConnectionSecurity() { return connectionSecurity; }
    public void setConnectionSecurity(String connectionSecurity) { this.connectionSecurity = connectionSecurity; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getConnectionMode() { return connectionMode; }
    public void setConnectionMode(String connectionMode) { this.connectionMode = connectionMode; }
    
    public String getTargetDirectory() { return targetDirectory; }
    public void setTargetDirectory(String targetDirectory) { this.targetDirectory = targetDirectory; }
    
    public String getTargetFileName() { return targetFileName; }
    public void setTargetFileName(String targetFileName) { this.targetFileName = targetFileName; }
    
    public String getFileConstructionMode() { return fileConstructionMode; }
    public void setFileConstructionMode(String fileConstructionMode) { this.fileConstructionMode = fileConstructionMode; }
    
    public boolean isOverwriteExistingFile() { return overwriteExistingFile; }
    public void setOverwriteExistingFile(boolean overwriteExistingFile) { this.overwriteExistingFile = overwriteExistingFile; }
    
    public boolean isCreateFileDirectory() { return createFileDirectory; }
    public void setCreateFileDirectory(boolean createFileDirectory) { this.createFileDirectory = createFileDirectory; }
    
    public String getFilePlacement() { return filePlacement; }
    public void setFilePlacement(String filePlacement) { this.filePlacement = filePlacement; }
    
    public String getTempFileExtension() { return tempFileExtension; }
    public void setTempFileExtension(String tempFileExtension) { this.tempFileExtension = tempFileExtension; }
    
    public String getEmptyMessageHandling() { return emptyMessageHandling; }
    public void setEmptyMessageHandling(String emptyMessageHandling) { this.emptyMessageHandling = emptyMessageHandling; }
    
    public String getFileEncoding() { return fileEncoding; }
    public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
    
    public String getContentTransformation() { return contentTransformation; }
    public void setContentTransformation(String contentTransformation) { this.contentTransformation = contentTransformation; }
    
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
    
    public String getMaxConcurrentConnections() { return maxConcurrentConnections; }
    public void setMaxConcurrentConnections(String maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }
    
    public boolean isUseConnectionPool() { return useConnectionPool; }
    public void setUseConnectionPool(boolean useConnectionPool) { this.useConnectionPool = useConnectionPool; }
    
    public boolean isEnablePassiveMode() { return enablePassiveMode; }
    public void setEnablePassiveMode(boolean enablePassiveMode) { this.enablePassiveMode = enablePassiveMode; }
    
    public int getTransferBufferSize() { return transferBufferSize; }
    public void setTransferBufferSize(int transferBufferSize) { this.transferBufferSize = transferBufferSize; }
    
    public boolean isValidateBeforeUpload() { return validateBeforeUpload; }
    public void setValidateBeforeUpload(boolean validateBeforeUpload) { this.validateBeforeUpload = validateBeforeUpload; }
    
    public String getChecksumValidation() { return checksumValidation; }
    public void setChecksumValidation(String checksumValidation) { this.checksumValidation = checksumValidation; }
    
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public boolean isEnableFileBackup() { return enableFileBackup; }
    public void setEnableFileBackup(boolean enableFileBackup) { this.enableFileBackup = enableFileBackup; }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
    
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
    
    public String getFileNamingPattern() { return fileNamingPattern; }
    public void setFileNamingPattern(String fileNamingPattern) { this.fileNamingPattern = fileNamingPattern; }
    
    public boolean isIncludeTimestamp() { return includeTimestamp; }
    public void setIncludeTimestamp(boolean includeTimestamp) { this.includeTimestamp = includeTimestamp; }
    
    public String getTimestampFormat() { return timestampFormat; }
    public void setTimestampFormat(String timestampFormat) { this.timestampFormat = timestampFormat; }
    
    public boolean isPreserveDirectoryStructure() { return preserveDirectoryStructure; }
    public void setPreserveDirectoryStructure(boolean preserveDirectoryStructure) { this.preserveDirectoryStructure = preserveDirectoryStructure; }
    
    public String getDirectoryNamingPattern() { return directoryNamingPattern; }
    public void setDirectoryNamingPattern(String directoryNamingPattern) { this.directoryNamingPattern = directoryNamingPattern; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogFileOperations() { return logFileOperations; }
    public void setLogFileOperations(boolean logFileOperations) { this.logFileOperations = logFileOperations; }
    
    public boolean isLogFileContent() { return logFileContent; }
    public void setLogFileContent(boolean logFileContent) { this.logFileContent = logFileContent; }
    
    public long getSlowOperationThresholdMs() { return slowOperationThresholdMs; }
    public void setSlowOperationThresholdMs(long slowOperationThresholdMs) { this.slowOperationThresholdMs = slowOperationThresholdMs; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    @Override
    public String toString() {
        return String.format("FtpReceiverAdapterConfig{server='%s', user='%s', target='%s', operation='%s', batching=%s}",
                serverAddress, userName, targetDirectory, operationType, enableBatching);
    }
}