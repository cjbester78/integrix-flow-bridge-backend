package com.integrationlab.adapters.config;

import java.util.List;
import java.util.ArrayList;

/**
 * Configuration for FTP Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on connecting TO FTP servers to poll/retrieve files.
 */
public class FtpSenderAdapterConfig {
    
    // Source FTP Server Connection Details
    private String serverAddress; // FTP server hostname or IP
    private String port = "21"; // Default FTP port
    private String timeout = "30000"; // Connection timeout in milliseconds
    private String connectionSecurity = "plain-ftp"; // explicit-ftps, implicit-ftps, plain-ftp
    private String userName;
    private String password;
    private String connectionMode = "per-file-transfer"; // permanently, per-file-transfer
    
    // File Retrieval Configuration
    private String sourceDirectory = "/"; // Source directory to monitor
    private String fileName = "*"; // File name pattern to retrieve
    private String exclusionMask; // Pattern for files to exclude
    private String sorting = "none"; // none, name, size, date
    private boolean advancedSelection = false;
    private List<FileAccessAdvancedEntry> advancedEntries = new ArrayList<>();
    
    // Polling Configuration
    private String pollingInterval = "30000"; // 30 seconds default
    private boolean enablePolling = true;
    private String pollingSchedule; // Cron expression for scheduled polling
    
    // File Processing Configuration
    private String processingMode = "delete"; // delete, archive, move
    private String archiveDirectory; // Directory to archive processed files
    private String processedDirectory; // Directory to move processed files
    private String emptyFileHandling = "process"; // process, ignore, error
    private boolean enableDuplicateHandling = false;
    private String duplicateMessageAlertThreshold = "100";
    private boolean disableChannelOnExceed = false;
    
    // Business Context
    private String businessComponentId;
    private String dataStructureId; // Expected data structure from files
    
    // Connection Pool and Performance
    private String maxConcurrentConnections = "5";
    private boolean useConnectionPool = false; // FTP typically doesn't use pools
    private boolean enablePassiveMode = true; // Use passive mode for firewalls
    
    // Error Handling and Retry
    private String errorHandlingStrategy = "FAIL_FAST";
    private boolean continueOnError = false;
    private int maxErrorThreshold = 10;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 5000;
    
    // File Processing Options
    private String fileEncoding = "UTF-8";
    private boolean validateFileIntegrity = false;
    private String checksumAlgorithm = "MD5"; // MD5, SHA1, SHA256
    private long maxFileSize = 100 * 1024 * 1024; // 100MB default
    private long minFileAge = 0; // Minimum file age before processing (ms)
    
    // Monitoring and Logging
    private boolean enableMetrics = true;
    private boolean logFileOperations = true;
    private boolean logFileContent = false;
    private long slowOperationThresholdMs = 30000;
    
    // Legacy compatibility
    private String configParam; // For backward compatibility
    
    // Inner class for advanced file selection
    public static class FileAccessAdvancedEntry {
        private String pattern;
        private String operation; // include, exclude
        private String condition; // size, date, name
        
        public FileAccessAdvancedEntry() {}
        
        public FileAccessAdvancedEntry(String pattern, String operation, String condition) {
            this.pattern = pattern;
            this.operation = operation;
            this.condition = condition;
        }
        
        // Getters and setters
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
    }
    
    // Constructors
    public FtpSenderAdapterConfig() {}
    
    public FtpSenderAdapterConfig(String serverAddress, String userName, String password) {
        this.serverAddress = serverAddress;
        this.userName = userName;
        this.password = password;
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
    
    public String getSourceDirectory() { return sourceDirectory; }
    public void setSourceDirectory(String sourceDirectory) { this.sourceDirectory = sourceDirectory; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getExclusionMask() { return exclusionMask; }
    public void setExclusionMask(String exclusionMask) { this.exclusionMask = exclusionMask; }
    
    public String getSorting() { return sorting; }
    public void setSorting(String sorting) { this.sorting = sorting; }
    
    public boolean isAdvancedSelection() { return advancedSelection; }
    public void setAdvancedSelection(boolean advancedSelection) { this.advancedSelection = advancedSelection; }
    
    public List<FileAccessAdvancedEntry> getAdvancedEntries() { return advancedEntries; }
    public void setAdvancedEntries(List<FileAccessAdvancedEntry> advancedEntries) { this.advancedEntries = advancedEntries; }
    
    public String getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(String pollingInterval) { this.pollingInterval = pollingInterval; }
    
    public boolean isEnablePolling() { return enablePolling; }
    public void setEnablePolling(boolean enablePolling) { this.enablePolling = enablePolling; }
    
    public String getPollingSchedule() { return pollingSchedule; }
    public void setPollingSchedule(String pollingSchedule) { this.pollingSchedule = pollingSchedule; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public String getArchiveDirectory() { return archiveDirectory; }
    public void setArchiveDirectory(String archiveDirectory) { this.archiveDirectory = archiveDirectory; }
    
    public String getProcessedDirectory() { return processedDirectory; }
    public void setProcessedDirectory(String processedDirectory) { this.processedDirectory = processedDirectory; }
    
    public String getEmptyFileHandling() { return emptyFileHandling; }
    public void setEmptyFileHandling(String emptyFileHandling) { this.emptyFileHandling = emptyFileHandling; }
    
    public boolean isEnableDuplicateHandling() { return enableDuplicateHandling; }
    public void setEnableDuplicateHandling(boolean enableDuplicateHandling) { this.enableDuplicateHandling = enableDuplicateHandling; }
    
    public String getDuplicateMessageAlertThreshold() { return duplicateMessageAlertThreshold; }
    public void setDuplicateMessageAlertThreshold(String duplicateMessageAlertThreshold) { this.duplicateMessageAlertThreshold = duplicateMessageAlertThreshold; }
    
    public boolean isDisableChannelOnExceed() { return disableChannelOnExceed; }
    public void setDisableChannelOnExceed(boolean disableChannelOnExceed) { this.disableChannelOnExceed = disableChannelOnExceed; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    public String getDataStructureId() { return dataStructureId; }
    public void setDataStructureId(String dataStructureId) { this.dataStructureId = dataStructureId; }
    
    public String getMaxConcurrentConnections() { return maxConcurrentConnections; }
    public void setMaxConcurrentConnections(String maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }
    
    public boolean isUseConnectionPool() { return useConnectionPool; }
    public void setUseConnectionPool(boolean useConnectionPool) { this.useConnectionPool = useConnectionPool; }
    
    public boolean isEnablePassiveMode() { return enablePassiveMode; }
    public void setEnablePassiveMode(boolean enablePassiveMode) { this.enablePassiveMode = enablePassiveMode; }
    
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
    
    public String getFileEncoding() { return fileEncoding; }
    public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
    
    public boolean isValidateFileIntegrity() { return validateFileIntegrity; }
    public void setValidateFileIntegrity(boolean validateFileIntegrity) { this.validateFileIntegrity = validateFileIntegrity; }
    
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public long getMinFileAge() { return minFileAge; }
    public void setMinFileAge(long minFileAge) { this.minFileAge = minFileAge; }
    
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
        return String.format("FtpSenderAdapterConfig{server='%s', user='%s', source='%s', polling=%sms, processing='%s'}",
                serverAddress, userName, sourceDirectory, pollingInterval, processingMode);
    }
}