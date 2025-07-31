package com.integrationlab.adapters.config;

/**
 * Configuration for SFTP Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on uploading files to SFTP servers.
 */
public class SftpReceiverAdapterConfig {

    // Target SFTP Server Connection
    private String targetServerAddress;
    private String targetPort = "22"; // Default SFTP port
    private int connectionTimeout = 30000; // 30 seconds
    private int readTimeout = 60000; // 60 seconds
    
    // Target Authentication
    private String authenticationType = "password"; // password, publickey, keyboard-interactive
    private String targetUserName;
    private String targetPassword;
    private String targetPublicKeyPath;
    private String targetPrivateKeyPath;
    private String targetPassphrase;
    
    // Target Directory and File Configuration
    private String targetDirectory;
    private String targetFileName;
    private String targetFileNamePattern; // Dynamic file name pattern
    private boolean createTargetDirectory = false;
    
    // File Upload Configuration
    private String fileConstructionMode = "create"; // create, append, overwrite
    private boolean overwriteExistingFile = false;
    private String fileEncoding = "UTF-8";
    private String filePermissions = "644"; // Unix file permissions
    
    // File Placement Strategy
    private String filePlacement = "atomic"; // direct, temporary_then_move, atomic
    private String temporaryDirectory; // Temporary directory on SFTP server
    private String temporaryFileExtension = ".tmp";
    private boolean useAtomicUpload = true; // Upload to temp file then rename
    
    // Connection Pool and Performance
    private int maxConcurrentConnections = 5;
    private boolean enableConnectionPooling = true;
    private long connectionIdleTime = 300000; // 5 minutes
    private int connectionRetryAttempts = 3;
    private long connectionRetryDelayMs = 2000;
    
    // Transfer Configuration
    private String transferMode = "binary"; // binary, ascii
    private int bufferSize = 32768; // 32KB default buffer
    private boolean enableCompression = false;
    private String compressionLevel = "6"; // 1-9, 6 is default
    
    // SSH Configuration
    private String[] supportedCiphers; // Supported SSH ciphers
    private String[] supportedKeyExchanges; // Supported key exchange algorithms
    private String[] supportedMacs; // Supported MAC algorithms
    private boolean strictHostKeyChecking = true;
    private String knownHostsFile; // Path to known_hosts file
    
    // Batch Processing
    private boolean enableBatching = false;
    private int batchSize = 100; // Number of files per batch
    private long batchTimeoutMs = 30000; // 30 seconds
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    
    // Error Handling for Uploads
    private String errorHandlingStrategy = "FAIL_FAST";
    private boolean continueOnError = false;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    private boolean useExponentialBackoff = true;
    private String errorDirectory; // Local directory for files that failed to upload
    
    // File Validation and Integrity
    private boolean validateFileTransfer = true;
    private String checksumAlgorithm = "MD5"; // MD5, SHA1, SHA256
    private boolean generateChecksum = false;
    private String checksumFileExtension = ".checksum";
    private boolean compareFileSize = true;
    
    // Archival and Backup
    private boolean archiveAfterUpload = false;
    private String archiveDirectory; // Local archive directory
    private boolean createBackup = false;
    private String backupDirectory;
    private int maxBackupFiles = 5;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target SFTP system
    private String operationType = "UPLOAD"; // UPLOAD, SYNC, MIRROR
    private boolean idempotent = false;
    private String idempotencyStrategy = "filename"; // filename, checksum, size
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logUploadedFiles = true;
    private boolean logFileContent = false;
    private long slowUploadThresholdMs = 30000; // 30 seconds
    private boolean enableTransferProgressLogging = false;
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId;
    
    // Legacy compatibility
    private String configParam;
    
    // Constructors
    public SftpReceiverAdapterConfig() {}
    
    public SftpReceiverAdapterConfig(String configParam) {
        this.configParam = configParam;
    }
    
    // Essential getters and setters
    public String getTargetServerAddress() { return targetServerAddress; }
    public void setTargetServerAddress(String targetServerAddress) { this.targetServerAddress = targetServerAddress; }
    
    public String getTargetPort() { return targetPort; }
    public void setTargetPort(String targetPort) { this.targetPort = targetPort; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    
    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }
    
    public String getTargetPublicKeyPath() { return targetPublicKeyPath; }
    public void setTargetPublicKeyPath(String targetPublicKeyPath) { this.targetPublicKeyPath = targetPublicKeyPath; }
    
    public String getTargetPrivateKeyPath() { return targetPrivateKeyPath; }
    public void setTargetPrivateKeyPath(String targetPrivateKeyPath) { this.targetPrivateKeyPath = targetPrivateKeyPath; }
    
    public String getTargetPassphrase() { return targetPassphrase; }
    public void setTargetPassphrase(String targetPassphrase) { this.targetPassphrase = targetPassphrase; }
    
    public String getTargetDirectory() { return targetDirectory; }
    public void setTargetDirectory(String targetDirectory) { this.targetDirectory = targetDirectory; }
    
    public String getTargetFileName() { return targetFileName; }
    public void setTargetFileName(String targetFileName) { this.targetFileName = targetFileName; }
    
    public String getTargetFileNamePattern() { return targetFileNamePattern; }
    public void setTargetFileNamePattern(String targetFileNamePattern) { this.targetFileNamePattern = targetFileNamePattern; }
    
    public boolean isCreateTargetDirectory() { return createTargetDirectory; }
    public void setCreateTargetDirectory(boolean createTargetDirectory) { this.createTargetDirectory = createTargetDirectory; }
    
    public String getFileConstructionMode() { return fileConstructionMode; }
    public void setFileConstructionMode(String fileConstructionMode) { this.fileConstructionMode = fileConstructionMode; }
    
    public boolean isOverwriteExistingFile() { return overwriteExistingFile; }
    public void setOverwriteExistingFile(boolean overwriteExistingFile) { this.overwriteExistingFile = overwriteExistingFile; }
    
    public String getFileEncoding() { return fileEncoding; }
    public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
    
    public String getFilePermissions() { return filePermissions; }
    public void setFilePermissions(String filePermissions) { this.filePermissions = filePermissions; }
    
    public String getFilePlacement() { return filePlacement; }
    public void setFilePlacement(String filePlacement) { this.filePlacement = filePlacement; }
    
    public String getTemporaryDirectory() { return temporaryDirectory; }
    public void setTemporaryDirectory(String temporaryDirectory) { this.temporaryDirectory = temporaryDirectory; }
    
    public String getTemporaryFileExtension() { return temporaryFileExtension; }
    public void setTemporaryFileExtension(String temporaryFileExtension) { this.temporaryFileExtension = temporaryFileExtension; }
    
    public boolean isUseAtomicUpload() { return useAtomicUpload; }
    public void setUseAtomicUpload(boolean useAtomicUpload) { this.useAtomicUpload = useAtomicUpload; }
    
    public int getMaxConcurrentConnections() { return maxConcurrentConnections; }
    public void setMaxConcurrentConnections(int maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public long getConnectionIdleTime() { return connectionIdleTime; }
    public void setConnectionIdleTime(long connectionIdleTime) { this.connectionIdleTime = connectionIdleTime; }
    
    public int getConnectionRetryAttempts() { return connectionRetryAttempts; }
    public void setConnectionRetryAttempts(int connectionRetryAttempts) { this.connectionRetryAttempts = connectionRetryAttempts; }
    
    public long getConnectionRetryDelayMs() { return connectionRetryDelayMs; }
    public void setConnectionRetryDelayMs(long connectionRetryDelayMs) { this.connectionRetryDelayMs = connectionRetryDelayMs; }
    
    public String getTransferMode() { return transferMode; }
    public void setTransferMode(String transferMode) { this.transferMode = transferMode; }
    
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
    
    public boolean isEnableCompression() { return enableCompression; }
    public void setEnableCompression(boolean enableCompression) { this.enableCompression = enableCompression; }
    
    public String getCompressionLevel() { return compressionLevel; }
    public void setCompressionLevel(String compressionLevel) { this.compressionLevel = compressionLevel; }
    
    public String[] getSupportedCiphers() { return supportedCiphers; }
    public void setSupportedCiphers(String[] supportedCiphers) { this.supportedCiphers = supportedCiphers; }
    
    public String[] getSupportedKeyExchanges() { return supportedKeyExchanges; }
    public void setSupportedKeyExchanges(String[] supportedKeyExchanges) { this.supportedKeyExchanges = supportedKeyExchanges; }
    
    public String[] getSupportedMacs() { return supportedMacs; }
    public void setSupportedMacs(String[] supportedMacs) { this.supportedMacs = supportedMacs; }
    
    public boolean isStrictHostKeyChecking() { return strictHostKeyChecking; }
    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) { this.strictHostKeyChecking = strictHostKeyChecking; }
    
    public String getKnownHostsFile() { return knownHostsFile; }
    public void setKnownHostsFile(String knownHostsFile) { this.knownHostsFile = knownHostsFile; }
    
    public boolean isEnableBatching() { return enableBatching; }
    public void setEnableBatching(boolean enableBatching) { this.enableBatching = enableBatching; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }
    
    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isUseExponentialBackoff() { return useExponentialBackoff; }
    public void setUseExponentialBackoff(boolean useExponentialBackoff) { this.useExponentialBackoff = useExponentialBackoff; }
    
    public String getErrorDirectory() { return errorDirectory; }
    public void setErrorDirectory(String errorDirectory) { this.errorDirectory = errorDirectory; }
    
    public boolean isValidateFileTransfer() { return validateFileTransfer; }
    public void setValidateFileTransfer(boolean validateFileTransfer) { this.validateFileTransfer = validateFileTransfer; }
    
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    
    public boolean isGenerateChecksum() { return generateChecksum; }
    public void setGenerateChecksum(boolean generateChecksum) { this.generateChecksum = generateChecksum; }
    
    public String getChecksumFileExtension() { return checksumFileExtension; }
    public void setChecksumFileExtension(String checksumFileExtension) { this.checksumFileExtension = checksumFileExtension; }
    
    public boolean isCompareFileSize() { return compareFileSize; }
    public void setCompareFileSize(boolean compareFileSize) { this.compareFileSize = compareFileSize; }
    
    public boolean isArchiveAfterUpload() { return archiveAfterUpload; }
    public void setArchiveAfterUpload(boolean archiveAfterUpload) { this.archiveAfterUpload = archiveAfterUpload; }
    
    public String getArchiveDirectory() { return archiveDirectory; }
    public void setArchiveDirectory(String archiveDirectory) { this.archiveDirectory = archiveDirectory; }
    
    public boolean isCreateBackup() { return createBackup; }
    public void setCreateBackup(boolean createBackup) { this.createBackup = createBackup; }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
    
    public int getMaxBackupFiles() { return maxBackupFiles; }
    public void setMaxBackupFiles(int maxBackupFiles) { this.maxBackupFiles = maxBackupFiles; }
    
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
    
    public boolean isLogUploadedFiles() { return logUploadedFiles; }
    public void setLogUploadedFiles(boolean logUploadedFiles) { this.logUploadedFiles = logUploadedFiles; }
    
    public boolean isLogFileContent() { return logFileContent; }
    public void setLogFileContent(boolean logFileContent) { this.logFileContent = logFileContent; }
    
    public long getSlowUploadThresholdMs() { return slowUploadThresholdMs; }
    public void setSlowUploadThresholdMs(long slowUploadThresholdMs) { this.slowUploadThresholdMs = slowUploadThresholdMs; }
    
    public boolean isEnableTransferProgressLogging() { return enableTransferProgressLogging; }
    public void setEnableTransferProgressLogging(boolean enableTransferProgressLogging) { this.enableTransferProgressLogging = enableTransferProgressLogging; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    // Backward compatibility methods
    public String getServerAddress() { return targetServerAddress; }
    public String getPort() { return targetPort; }
    public String getUserName() { return targetUserName; }
    public String getPassword() { return targetPassword; }
    public String getPrivateKey() { return targetPrivateKeyPath; }
    public String getPublicKey() { return targetPublicKeyPath; }
    public String getPassphrase() { return targetPassphrase; }
    
    @Override
    public String toString() {
        return String.format("SftpReceiverAdapterConfig{targetServer='%s:%s', targetDir='%s', fileName='%s', auth='%s'}",
                targetServerAddress, targetPort, targetDirectory, targetFileName, authenticationType);
    }
}