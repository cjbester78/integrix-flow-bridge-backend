package com.integrixs.adapters.config;

import java.util.List;
import java.util.ArrayList;

/**
 * Configuration for SFTP Sender Adapter (Backend).
 * In middleware terminology, sender adapters receive data FROM external source systems.
 * This configuration focuses on connecting TO SFTP servers to poll/retrieve files.
 */
public class SftpSenderAdapterConfig {
    
    // Source SFTP Server Connection Details
    private String serverAddress; // SFTP server hostname or IP
    private String port = "22"; // Default SFTP port
    private String timeout = "30000"; // Connection timeout in milliseconds
    private String authenticationType = "password"; // password, publickey, keyboard-interactive
    private String userName;
    private String password;
    private String publicKey; // Path to public key file
    private String privateKey; // Path to private key file
    private String passphrase; // Passphrase for private key
    private String hostKeyVerification = "strict"; // strict, relaxed, disabled
    private String knownHostsFile; // Path to known hosts file
    
    // Connection Management
    private String maxConcurrentConnections = "5";
    private String retryConnection = "3";
    private String retryConnectionDelay = "5000";
    private String connectionMode = "per-file-transfer"; // permanently, per-file-transfer
    private boolean enableConnectionPooling = false;
    private long connectionPoolTimeout = 30000L;
    
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
    
    // SSH Specific Settings
    private String sshCompression = "none"; // none, zlib, zlib@openssh.com
    private String preferredAuthentications = "password,publickey"; // Order of auth methods to try
    private boolean strictHostKeyChecking = true;
    private String cipherSuites; // Comma-separated list of preferred ciphers
    private String macAlgorithms; // Comma-separated list of preferred MAC algorithms
    private String kexAlgorithms; // Comma-separated list of preferred key exchange algorithms
    
    // File Transfer Settings
    private String transferMode = "binary"; // binary, ascii
    private int transferBufferSize = 32768; // Buffer size for file transfers
    private boolean preserveFileTimestamp = false;
    private boolean preserveFilePermissions = false;
    private String filePermissions = "644"; // Default file permissions
    
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
    private boolean logSSHDebug = false;
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
    public SftpSenderAdapterConfig() {}
    
    public SftpSenderAdapterConfig(String serverAddress, String userName, String password) {
        this.serverAddress = serverAddress;
        this.userName = userName;
        this.password = password;
    }
    
    public SftpSenderAdapterConfig(String serverAddress, String userName, String privateKey, String passphrase) {
        this.serverAddress = serverAddress;
        this.userName = userName;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.authenticationType = "publickey";
    }
    
    // Getters and Setters
    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
    
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    
    public String getTimeout() { return timeout; }
    public void setTimeout(String timeout) { this.timeout = timeout; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    
    public String getPassphrase() { return passphrase; }
    public void setPassphrase(String passphrase) { this.passphrase = passphrase; }
    
    public String getHostKeyVerification() { return hostKeyVerification; }
    public void setHostKeyVerification(String hostKeyVerification) { this.hostKeyVerification = hostKeyVerification; }
    
    public String getKnownHostsFile() { return knownHostsFile; }
    public void setKnownHostsFile(String knownHostsFile) { this.knownHostsFile = knownHostsFile; }
    
    public String getMaxConcurrentConnections() { return maxConcurrentConnections; }
    public void setMaxConcurrentConnections(String maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }
    
    public String getRetryConnection() { return retryConnection; }
    public void setRetryConnection(String retryConnection) { this.retryConnection = retryConnection; }
    
    public String getRetryConnectionDelay() { return retryConnectionDelay; }
    public void setRetryConnectionDelay(String retryConnectionDelay) { this.retryConnectionDelay = retryConnectionDelay; }
    
    public String getConnectionMode() { return connectionMode; }
    public void setConnectionMode(String connectionMode) { this.connectionMode = connectionMode; }
    
    public boolean isEnableConnectionPooling() { return enableConnectionPooling; }
    public void setEnableConnectionPooling(boolean enableConnectionPooling) { this.enableConnectionPooling = enableConnectionPooling; }
    
    public long getConnectionPoolTimeout() { return connectionPoolTimeout; }
    public void setConnectionPoolTimeout(long connectionPoolTimeout) { this.connectionPoolTimeout = connectionPoolTimeout; }
    
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
    
    public String getSshCompression() { return sshCompression; }
    public void setSshCompression(String sshCompression) { this.sshCompression = sshCompression; }
    
    public String getPreferredAuthentications() { return preferredAuthentications; }
    public void setPreferredAuthentications(String preferredAuthentications) { this.preferredAuthentications = preferredAuthentications; }
    
    public boolean isStrictHostKeyChecking() { return strictHostKeyChecking; }
    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) { this.strictHostKeyChecking = strictHostKeyChecking; }
    
    public String getCipherSuites() { return cipherSuites; }
    public void setCipherSuites(String cipherSuites) { this.cipherSuites = cipherSuites; }
    
    public String getMacAlgorithms() { return macAlgorithms; }
    public void setMacAlgorithms(String macAlgorithms) { this.macAlgorithms = macAlgorithms; }
    
    public String getKexAlgorithms() { return kexAlgorithms; }
    public void setKexAlgorithms(String kexAlgorithms) { this.kexAlgorithms = kexAlgorithms; }
    
    public String getTransferMode() { return transferMode; }
    public void setTransferMode(String transferMode) { this.transferMode = transferMode; }
    
    public int getTransferBufferSize() { return transferBufferSize; }
    public void setTransferBufferSize(int transferBufferSize) { this.transferBufferSize = transferBufferSize; }
    
    public boolean isPreserveFileTimestamp() { return preserveFileTimestamp; }
    public void setPreserveFileTimestamp(boolean preserveFileTimestamp) { this.preserveFileTimestamp = preserveFileTimestamp; }
    
    public boolean isPreserveFilePermissions() { return preserveFilePermissions; }
    public void setPreserveFilePermissions(boolean preserveFilePermissions) { this.preserveFilePermissions = preserveFilePermissions; }
    
    public String getFilePermissions() { return filePermissions; }
    public void setFilePermissions(String filePermissions) { this.filePermissions = filePermissions; }
    
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
    
    public boolean isLogSSHDebug() { return logSSHDebug; }
    public void setLogSSHDebug(boolean logSSHDebug) { this.logSSHDebug = logSSHDebug; }
    
    public long getSlowOperationThresholdMs() { return slowOperationThresholdMs; }
    public void setSlowOperationThresholdMs(long slowOperationThresholdMs) { this.slowOperationThresholdMs = slowOperationThresholdMs; }
    
    // Legacy compatibility
    public String getConfigParam() { return configParam; }
    public void setConfigParam(String configParam) { this.configParam = configParam; }
    
    @Override
    public String toString() {
        return String.format("SftpSenderAdapterConfig{server='%s', user='%s', source='%s', polling=%sms, auth='%s'}",
                serverAddress, userName, sourceDirectory, pollingInterval, authenticationType);
    }
}