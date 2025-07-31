package com.integrationlab.adapters.config;

/**
 * Configuration for File Receiver Adapter (Frontend).
 * In middleware terminology, receiver adapters send data TO external target systems.
 * This configuration focuses on writing/creating files in target directories.
 */
public class FileReceiverAdapterConfig {
    
    // Target Directory Configuration
    private String targetDirectory; // Directory where files will be created
    private String targetFileName; // Target file name pattern
    private String fileNamePattern; // Dynamic file name pattern with variables
    private boolean createFileDirectory = false; // Create directory if it doesn't exist
    
    // File Construction and Writing
    private String fileConstructionMode = "create"; // create, append, overwrite
    private boolean overwriteExistingFile = false;
    private String fileEncoding = "UTF-8";
    private String fileFormat; // Format of output files
    private String lineEnding = "system"; // system, unix, windows
    
    // File Placement and Organization
    private String filePlacement = "direct"; // direct, temporary_then_move, atomic
    private String temporaryDirectory; // Temporary directory for staging
    private String temporaryFileExtension = ".tmp";
    private boolean useAtomicWrite = true; // Write to temp file then rename
    
    // Content Handling
    private String emptyMessageHandling = "create_empty"; // create_empty, skip, error
    private boolean includeHeaders = false;
    private String headerTemplate; // Template for file headers
    private String footerTemplate; // Template for file footers
    private String recordSeparator = "\n"; // Separator between records
    
    // Business Context
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    
    // Batch Processing
    private Integer batchSize; // Number of records per file
    private boolean enableBatching = false;
    private long batchTimeoutMs = 30000; // 30 seconds - flush batch if timeout reached
    private String batchStrategy = "SIZE_BASED"; // SIZE_BASED, TIME_BASED, MIXED
    private String batchFileNamingPattern; // Pattern for batch file names
    
    // File Permissions and Security
    private String filePermissions = "644"; // Unix file permissions
    private String fileOwner; // File owner (if supported)
    private String fileGroup; // File group (if supported)
    private boolean createSecureFile = false; // Create with restricted permissions
    
    // Archival and Backup
    private boolean createBackup = false;
    private String backupDirectory;
    private String backupFileExtension = ".bak";
    private int maxBackupFiles = 5; // Maximum number of backup files to keep
    
    // File Validation
    private boolean validateFileCreation = true;
    private String checksumAlgorithm = "MD5"; // MD5, SHA1, SHA256
    private boolean generateChecksum = false;
    private String checksumFileExtension = ".checksum";
    
    // Concurrency and Performance
    private String maxConcurrentConnections = "10";
    private int maxConcurrentWrites = 5;
    private int bufferSize = 8192; // Buffer size for file writing
    private boolean useBufferedWriter = true;
    private long maxFileSize = Long.MAX_VALUE; // Maximum file size in bytes
    
    // Error Handling
    private String errorHandlingStrategy = "FAIL_FAST";
    private String errorDirectory; // Directory for files that caused errors
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 2000;
    
    // File Rotation
    private boolean enableFileRotation = false;
    private String rotationStrategy = "SIZE"; // SIZE, TIME, COUNT
    private long rotationThresholdSize = 100 * 1024 * 1024; // 100MB
    private String rotationTimePattern = "yyyy-MM-dd-HH"; // Hourly rotation
    private int maxRotationFiles = 10;
    
    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType = "CREATE"; // CREATE, UPDATE, DELETE, APPEND
    private boolean idempotent = false; // Whether operations are idempotent
    private String idempotencyStrategy = "filename"; // filename, checksum, content
    
    // File Locking
    private boolean useFileLocking = true;
    private long fileLockTimeout = 30000L; // 30 seconds
    private String lockFileExtension = ".lock";
    private String lockingStrategy = "exclusive"; // exclusive, advisory
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logCreatedFiles = true;
    private boolean logFileContent = false;
    private long slowWriteThresholdMs = 5000;
    
    // Legacy compatibility
    private String filename; // For backward compatibility
    
    // Constructors
    public FileReceiverAdapterConfig() {}
    
    public FileReceiverAdapterConfig(String targetDirectory, String targetFileName) {
        this.targetDirectory = targetDirectory;
        this.targetFileName = targetFileName;
    }
    
    // Essential getters and setters
    public String getTargetDirectory() { return targetDirectory; }
    public void setTargetDirectory(String targetDirectory) { this.targetDirectory = targetDirectory; }
    
    public String getTargetFileName() { return targetFileName; }
    public void setTargetFileName(String targetFileName) { this.targetFileName = targetFileName; }
    
    public String getFileNamePattern() { return fileNamePattern; }
    public void setFileNamePattern(String fileNamePattern) { this.fileNamePattern = fileNamePattern; }
    
    public boolean isCreateFileDirectory() { return createFileDirectory; }
    public void setCreateFileDirectory(boolean createFileDirectory) { this.createFileDirectory = createFileDirectory; }
    
    public String getFileConstructionMode() { return fileConstructionMode; }
    public void setFileConstructionMode(String fileConstructionMode) { this.fileConstructionMode = fileConstructionMode; }
    
    public boolean isOverwriteExistingFile() { return overwriteExistingFile; }
    public void setOverwriteExistingFile(boolean overwriteExistingFile) { this.overwriteExistingFile = overwriteExistingFile; }
    
    public String getFileEncoding() { return fileEncoding; }
    public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
    
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    
    public String getLineEnding() { return lineEnding; }
    public void setLineEnding(String lineEnding) { this.lineEnding = lineEnding; }
    
    public String getFilePlacement() { return filePlacement; }
    public void setFilePlacement(String filePlacement) { this.filePlacement = filePlacement; }
    
    public String getTemporaryDirectory() { return temporaryDirectory; }
    public void setTemporaryDirectory(String temporaryDirectory) { this.temporaryDirectory = temporaryDirectory; }
    
    public String getTemporaryFileExtension() { return temporaryFileExtension; }
    public void setTemporaryFileExtension(String temporaryFileExtension) { this.temporaryFileExtension = temporaryFileExtension; }
    
    public boolean isUseAtomicWrite() { return useAtomicWrite; }
    public void setUseAtomicWrite(boolean useAtomicWrite) { this.useAtomicWrite = useAtomicWrite; }
    
    public String getEmptyMessageHandling() { return emptyMessageHandling; }
    public void setEmptyMessageHandling(String emptyMessageHandling) { this.emptyMessageHandling = emptyMessageHandling; }
    
    public boolean isIncludeHeaders() { return includeHeaders; }
    public void setIncludeHeaders(boolean includeHeaders) { this.includeHeaders = includeHeaders; }
    
    public String getHeaderTemplate() { return headerTemplate; }
    public void setHeaderTemplate(String headerTemplate) { this.headerTemplate = headerTemplate; }
    
    public String getFooterTemplate() { return footerTemplate; }
    public void setFooterTemplate(String footerTemplate) { this.footerTemplate = footerTemplate; }
    
    public String getRecordSeparator() { return recordSeparator; }
    public void setRecordSeparator(String recordSeparator) { this.recordSeparator = recordSeparator; }
    
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
    
    public String getBatchFileNamingPattern() { return batchFileNamingPattern; }
    public void setBatchFileNamingPattern(String batchFileNamingPattern) { this.batchFileNamingPattern = batchFileNamingPattern; }
    
    public String getFilePermissions() { return filePermissions; }
    public void setFilePermissions(String filePermissions) { this.filePermissions = filePermissions; }
    
    public String getFileOwner() { return fileOwner; }
    public void setFileOwner(String fileOwner) { this.fileOwner = fileOwner; }
    
    public String getFileGroup() { return fileGroup; }
    public void setFileGroup(String fileGroup) { this.fileGroup = fileGroup; }
    
    public boolean isCreateSecureFile() { return createSecureFile; }
    public void setCreateSecureFile(boolean createSecureFile) { this.createSecureFile = createSecureFile; }
    
    public boolean isCreateBackup() { return createBackup; }
    public void setCreateBackup(boolean createBackup) { this.createBackup = createBackup; }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
    
    public String getBackupFileExtension() { return backupFileExtension; }
    public void setBackupFileExtension(String backupFileExtension) { this.backupFileExtension = backupFileExtension; }
    
    public int getMaxBackupFiles() { return maxBackupFiles; }
    public void setMaxBackupFiles(int maxBackupFiles) { this.maxBackupFiles = maxBackupFiles; }
    
    public boolean isValidateFileCreation() { return validateFileCreation; }
    public void setValidateFileCreation(boolean validateFileCreation) { this.validateFileCreation = validateFileCreation; }
    
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    
    public boolean isGenerateChecksum() { return generateChecksum; }
    public void setGenerateChecksum(boolean generateChecksum) { this.generateChecksum = generateChecksum; }
    
    public String getChecksumFileExtension() { return checksumFileExtension; }
    public void setChecksumFileExtension(String checksumFileExtension) { this.checksumFileExtension = checksumFileExtension; }
    
    public String getMaxConcurrentConnections() { return maxConcurrentConnections; }
    public void setMaxConcurrentConnections(String maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }
    
    public int getMaxConcurrentWrites() { return maxConcurrentWrites; }
    public void setMaxConcurrentWrites(int maxConcurrentWrites) { this.maxConcurrentWrites = maxConcurrentWrites; }
    
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
    
    public boolean isUseBufferedWriter() { return useBufferedWriter; }
    public void setUseBufferedWriter(boolean useBufferedWriter) { this.useBufferedWriter = useBufferedWriter; }
    
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }
    
    public String getErrorDirectory() { return errorDirectory; }
    public void setErrorDirectory(String errorDirectory) { this.errorDirectory = errorDirectory; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    public int getMaxErrorThreshold() { return maxErrorThreshold; }
    public void setMaxErrorThreshold(int maxErrorThreshold) { this.maxErrorThreshold = maxErrorThreshold; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public boolean isEnableFileRotation() { return enableFileRotation; }
    public void setEnableFileRotation(boolean enableFileRotation) { this.enableFileRotation = enableFileRotation; }
    
    public String getRotationStrategy() { return rotationStrategy; }
    public void setRotationStrategy(String rotationStrategy) { this.rotationStrategy = rotationStrategy; }
    
    public long getRotationThresholdSize() { return rotationThresholdSize; }
    public void setRotationThresholdSize(long rotationThresholdSize) { this.rotationThresholdSize = rotationThresholdSize; }
    
    public String getRotationTimePattern() { return rotationTimePattern; }
    public void setRotationTimePattern(String rotationTimePattern) { this.rotationTimePattern = rotationTimePattern; }
    
    public int getMaxRotationFiles() { return maxRotationFiles; }
    public void setMaxRotationFiles(int maxRotationFiles) { this.maxRotationFiles = maxRotationFiles; }
    
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }
    
    public String getIdempotencyStrategy() { return idempotencyStrategy; }
    public void setIdempotencyStrategy(String idempotencyStrategy) { this.idempotencyStrategy = idempotencyStrategy; }
    
    public boolean isUseFileLocking() { return useFileLocking; }
    public void setUseFileLocking(boolean useFileLocking) { this.useFileLocking = useFileLocking; }
    
    public long getFileLockTimeout() { return fileLockTimeout; }
    public void setFileLockTimeout(long fileLockTimeout) { this.fileLockTimeout = fileLockTimeout; }
    
    public String getLockFileExtension() { return lockFileExtension; }
    public void setLockFileExtension(String lockFileExtension) { this.lockFileExtension = lockFileExtension; }
    
    public String getLockingStrategy() { return lockingStrategy; }
    public void setLockingStrategy(String lockingStrategy) { this.lockingStrategy = lockingStrategy; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogCreatedFiles() { return logCreatedFiles; }
    public void setLogCreatedFiles(boolean logCreatedFiles) { this.logCreatedFiles = logCreatedFiles; }
    
    public boolean isLogFileContent() { return logFileContent; }
    public void setLogFileContent(boolean logFileContent) { this.logFileContent = logFileContent; }
    
    public long getSlowWriteThresholdMs() { return slowWriteThresholdMs; }
    public void setSlowWriteThresholdMs(long slowWriteThresholdMs) { this.slowWriteThresholdMs = slowWriteThresholdMs; }
    
    // Legacy compatibility
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    // Backward compatibility with old method names
    public String getEmptyMessageHandling_receiver() { return emptyMessageHandling; }
    public void setEmptyMessageHandling_receiver(String emptyMessageHandling) { this.emptyMessageHandling = emptyMessageHandling; }
    
    @Override
    public String toString() {
        return String.format("FileReceiverAdapterConfig{targetDir='%s', fileName='%s', construction='%s', batching=%s}",
                targetDirectory, targetFileName, fileConstructionMode, enableBatching);
    }
}