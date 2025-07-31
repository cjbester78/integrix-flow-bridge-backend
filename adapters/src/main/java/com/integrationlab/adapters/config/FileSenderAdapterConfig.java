package com.integrationlab.adapters.config;

import java.util.List;
import java.util.ArrayList;

/**
 * Configuration for File Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on polling/monitoring directories to retrieve files.
 */
public class FileSenderAdapterConfig {
    
    // Source Directory Configuration
    private String sourceDirectory; // Directory to monitor/poll for files
    private String fileName; // File name pattern to match
    private String filePattern; // Regex pattern for file matching
    private String exclusionMask; // Files to exclude from processing
    
    // File Selection and Filtering
    private String sorting = "name"; // name, date, size, none
    private boolean advancedSelection = false;
    private List<FileAccessAdvancedEntry> advancedEntries = new ArrayList<>();
    private String fileFilter; // Additional filtering criteria
    private long minFileSize = 0L; // Minimum file size in bytes
    private long maxFileSize = Long.MAX_VALUE; // Maximum file size in bytes
    private int minFileAge = 0; // Minimum file age in seconds
    
    // Polling Configuration
    private Long pollingInterval = 30000L; // 30 seconds default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    private int maxFilesPerPoll = 100; // Maximum files to process per poll
    
    // Processing Configuration
    private String processingMode = "delete"; // delete, archive, move, copy
    private String archiveDirectory; // Directory for archiving processed files
    private String moveDirectory; // Directory for moving processed files
    private String backupDirectory; // Directory for backup copies
    private boolean createBackup = false;
    
    // File Handling
    private String emptyFileHandling = "ignore"; // ignore, process, error
    private String fileEncoding = "UTF-8"; // File encoding
    private boolean validateFileIntegrity = true;
    private String checksumAlgorithm = "MD5"; // MD5, SHA1, SHA256
    
    // Duplicate Detection
    private boolean enableDuplicateHandling = false;
    private String duplicateDetectionStrategy = "name"; // name, checksum, content
    private String duplicateMessageAlertThreshold = "10";
    private boolean disableChannelOnExceed = false;
    private String duplicateDirectory; // Directory for duplicate files
    
    // File Lock and Concurrency
    private boolean useFileLocking = true;
    private long fileLockTimeout = 30000L; // 30 seconds
    private String lockFileExtension = ".lock";
    private int maxConcurrentFiles = 5;
    
    // Business Context
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from files
    
    // Incremental Processing
    private String lastProcessedFile; // Last processed file name
    private long lastProcessedTimestamp; // Last processed timestamp
    private boolean resetIncrementalOnStart = false;
    
    // Error Handling
    private String errorHandlingStrategy = "FAIL_FAST";
    private String errorDirectory; // Directory for files that caused errors
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logProcessedFiles = true;
    private boolean logFileContent = false;
    private long slowProcessingThresholdMs = 10000;
    
    // Legacy compatibility
    private String filename; // For backward compatibility
    
    // Constructors
    public FileSenderAdapterConfig() {}
    
    public FileSenderAdapterConfig(String sourceDirectory, String fileName) {
        this.sourceDirectory = sourceDirectory;
        this.fileName = fileName;
    }
    
    // Essential getters and setters
    public String getSourceDirectory() { return sourceDirectory; }
    public void setSourceDirectory(String sourceDirectory) { this.sourceDirectory = sourceDirectory; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePattern() { return filePattern; }
    public void setFilePattern(String filePattern) { this.filePattern = filePattern; }
    
    public String getExclusionMask() { return exclusionMask; }
    public void setExclusionMask(String exclusionMask) { this.exclusionMask = exclusionMask; }
    
    public String getSorting() { return sorting; }
    public void setSorting(String sorting) { this.sorting = sorting; }
    
    public boolean isAdvancedSelection() { return advancedSelection; }
    public void setAdvancedSelection(boolean advancedSelection) { this.advancedSelection = advancedSelection; }
    
    public List<FileAccessAdvancedEntry> getAdvancedEntries() { return advancedEntries; }
    public void setAdvancedEntries(List<FileAccessAdvancedEntry> advancedEntries) { this.advancedEntries = advancedEntries; }
    
    public String getFileFilter() { return fileFilter; }
    public void setFileFilter(String fileFilter) { this.fileFilter = fileFilter; }
    
    public long getMinFileSize() { return minFileSize; }
    public void setMinFileSize(long minFileSize) { this.minFileSize = minFileSize; }
    
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public int getMinFileAge() { return minFileAge; }
    public void setMinFileAge(int minFileAge) { this.minFileAge = minFileAge; }
    
    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public int getMaxFilesPerPoll() { return maxFilesPerPoll; }
    public void setMaxFilesPerPoll(int maxFilesPerPoll) { this.maxFilesPerPoll = maxFilesPerPoll; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public String getArchiveDirectory() { return archiveDirectory; }
    public void setArchiveDirectory(String archiveDirectory) { this.archiveDirectory = archiveDirectory; }
    
    public String getMoveDirectory() { return moveDirectory; }
    public void setMoveDirectory(String moveDirectory) { this.moveDirectory = moveDirectory; }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
    
    public boolean isCreateBackup() { return createBackup; }
    public void setCreateBackup(boolean createBackup) { this.createBackup = createBackup; }
    
    public String getEmptyFileHandling() { return emptyFileHandling; }
    public void setEmptyFileHandling(String emptyFileHandling) { this.emptyFileHandling = emptyFileHandling; }
    
    public String getFileEncoding() { return fileEncoding; }
    public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
    
    public boolean isValidateFileIntegrity() { return validateFileIntegrity; }
    public void setValidateFileIntegrity(boolean validateFileIntegrity) { this.validateFileIntegrity = validateFileIntegrity; }
    
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    
    public boolean isEnableDuplicateHandling() { return enableDuplicateHandling; }
    public void setEnableDuplicateHandling(boolean enableDuplicateHandling) { this.enableDuplicateHandling = enableDuplicateHandling; }
    
    public String getDuplicateDetectionStrategy() { return duplicateDetectionStrategy; }
    public void setDuplicateDetectionStrategy(String duplicateDetectionStrategy) { this.duplicateDetectionStrategy = duplicateDetectionStrategy; }
    
    public String getDuplicateMessageAlertThreshold() { return duplicateMessageAlertThreshold; }
    public void setDuplicateMessageAlertThreshold(String duplicateMessageAlertThreshold) { this.duplicateMessageAlertThreshold = duplicateMessageAlertThreshold; }
    
    public boolean isDisableChannelOnExceed() { return disableChannelOnExceed; }
    public void setDisableChannelOnExceed(boolean disableChannelOnExceed) { this.disableChannelOnExceed = disableChannelOnExceed; }
    
    public String getDuplicateDirectory() { return duplicateDirectory; }
    public void setDuplicateDirectory(String duplicateDirectory) { this.duplicateDirectory = duplicateDirectory; }
    
    public boolean isUseFileLocking() { return useFileLocking; }
    public void setUseFileLocking(boolean useFileLocking) { this.useFileLocking = useFileLocking; }
    
    public long getFileLockTimeout() { return fileLockTimeout; }
    public void setFileLockTimeout(long fileLockTimeout) { this.fileLockTimeout = fileLockTimeout; }
    
    public String getLockFileExtension() { return lockFileExtension; }
    public void setLockFileExtension(String lockFileExtension) { this.lockFileExtension = lockFileExtension; }
    
    public int getMaxConcurrentFiles() { return maxConcurrentFiles; }
    public void setMaxConcurrentFiles(int maxConcurrentFiles) { this.maxConcurrentFiles = maxConcurrentFiles; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
    public String getLastProcessedFile() { return lastProcessedFile; }
    public void setLastProcessedFile(String lastProcessedFile) { this.lastProcessedFile = lastProcessedFile; }
    
    public long getLastProcessedTimestamp() { return lastProcessedTimestamp; }
    public void setLastProcessedTimestamp(long lastProcessedTimestamp) { this.lastProcessedTimestamp = lastProcessedTimestamp; }
    
    public boolean isResetIncrementalOnStart() { return resetIncrementalOnStart; }
    public void setResetIncrementalOnStart(boolean resetIncrementalOnStart) { this.resetIncrementalOnStart = resetIncrementalOnStart; }
    
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
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public boolean isLogProcessedFiles() { return logProcessedFiles; }
    public void setLogProcessedFiles(boolean logProcessedFiles) { this.logProcessedFiles = logProcessedFiles; }
    
    public boolean isLogFileContent() { return logFileContent; }
    public void setLogFileContent(boolean logFileContent) { this.logFileContent = logFileContent; }
    
    public long getSlowProcessingThresholdMs() { return slowProcessingThresholdMs; }
    public void setSlowProcessingThresholdMs(long slowProcessingThresholdMs) { this.slowProcessingThresholdMs = slowProcessingThresholdMs; }
    
    // Legacy compatibility
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    @Override
    public String toString() {
        return String.format("FileSenderAdapterConfig{sourceDir='%s', pattern='%s', polling=%dms, processing='%s'}",
                sourceDirectory, fileName != null ? fileName : filePattern, pollingInterval, processingMode);
    }
    
    // Inner class for advanced file access entries
    public static class FileAccessAdvancedEntry {
        private String directory;
        private String fileName;
        private String exclusionMask;

        public FileAccessAdvancedEntry() {}

        public FileAccessAdvancedEntry(String directory, String fileName, String exclusionMask) {
            this.directory = directory;
            this.fileName = fileName;
            this.exclusionMask = exclusionMask;
        }

        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getExclusionMask() { return exclusionMask; }
        public void setExclusionMask(String exclusionMask) { this.exclusionMask = exclusionMask; }
    }
}