package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.SftpSenderAdapterConfig;

import com.jcraft.jsch.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SFTP Sender Adapter implementation for SFTP file polling and retrieval (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports SFTP connections, file polling, pattern matching, and SSH authentication.
 */
public class SftpSenderAdapter extends AbstractSenderAdapter {
    
    private final SftpSenderAdapterConfig config;
    private final Map<String, String> processedFiles = new ConcurrentHashMap<>();
    private Pattern filePattern;
    private Pattern exclusionPattern;
    private Session sshSession;
    private ChannelSftp sftpChannel;
    
    public SftpSenderAdapter(SftpSenderAdapterConfig config) {
        super(AdapterType.SFTP);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing SFTP sender adapter (inbound) with server: {}:{}", 
                config.getServerAddress(), config.getPort());
        
        validateConfiguration();
        initializePatterns();
        
        // For per-file-transfer mode, we don't maintain persistent connection
        if ("permanently".equals(config.getConnectionMode())) {
            connectToSftp();
        }
        
        logger.info("SFTP sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying SFTP sender adapter");
        
        disconnectFromSftp();
        processedFiles.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic SFTP connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.SFTP, () -> {
            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                
                if (testSession.isConnected() && testChannel.isConnected()) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.SFTP, 
                            "SFTP Connection", "Successfully connected to SFTP server");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                            "SFTP Connection", "SFTP connection failed", null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                        "SFTP Connection", "Failed to connect to SFTP server: " + e.getMessage(), e);
            } finally {
                if (testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if (testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));
        
        // Test 2: Directory access and permissions
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SFTP, () -> {
            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                
                // Test directory access
                testChannel.cd(config.getSourceDirectory());
                
                // Test file listing
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = testChannel.ls(".");
                int fileCount = files != null ? files.size() : 0;
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.SFTP, 
                        "Directory Access", "Source directory accessible, contains " + fileCount + " items");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                        "Directory Access", "Failed to access source directory: " + e.getMessage(), e);
            } finally {
                if (testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if (testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));
        
        // Test 3: Pattern matching test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SFTP, () -> {
            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                testChannel.cd(config.getSourceDirectory());
                
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = testChannel.ls(".");
                long matchingFiles = files != null ? 
                        files.stream()
                                .filter(entry -> !entry.getAttrs().isDir())
                                .filter(entry -> matchesFilePattern(entry.getFilename()))
                                .count() : 0;
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.SFTP, 
                        "Pattern Matching", "Found " + matchingFiles + " files matching configured patterns");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                        "Pattern Matching", "Failed to test pattern matching: " + e.getMessage(), e);
            } finally {
                if (testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if (testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.SFTP, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For SFTP Sender (inbound), "send" means polling/retrieving files FROM SFTP server
        return pollForFiles();
    }
    
    private AdapterResult pollForFiles() throws Exception {
        List<Map<String, Object>> processedFiles = new ArrayList<>();
        Session session = null;
        ChannelSftp channel = null;
        
        try {
            // Get or create connection
            if ("permanently".equals(config.getConnectionMode())) {
                session = sshSession;
                channel = sftpChannel;
                if (session == null || !session.isConnected() || channel == null || !channel.isConnected()) {
                    connectToSftp();
                    session = sshSession;
                    channel = sftpChannel;
                }
            } else {
                session = createSession();
                session.connect();
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
            }
            
            // Change to source directory
            channel.cd(config.getSourceDirectory());
            
            // List files
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = channel.ls(".");
            if (files == null) {
                logger.warn("No files returned from SFTP server directory listing");
                return AdapterResult.success(Collections.emptyList(), "No files found in directory");
            }
            
            // Filter and sort files
            List<ChannelSftp.LsEntry> eligibleFiles = files.stream()
                    .filter(entry -> !entry.getAttrs().isDir())
                    .filter(entry -> !".".equals(entry.getFilename()) && !"..".equals(entry.getFilename()))
                    .filter(entry -> matchesFilePattern(entry.getFilename()))
                    .filter(this::shouldProcessFile)
                    .collect(Collectors.toList());
            
            sortFiles(eligibleFiles);
            
            // Process files
            for (ChannelSftp.LsEntry entry : eligibleFiles) {
                try {
                    if (shouldProcessFile(entry)) {
                        Map<String, Object> fileData = processFile(channel, entry);
                        if (fileData != null) {
                            processedFiles.add(fileData);
                            handlePostProcessing(channel, entry);
                            
                            // Mark as processed
                            this.processedFiles.put(entry.getFilename(), String.valueOf(System.currentTimeMillis()));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing SFTP file: {}", entry.getFilename(), e);
                    
                    if (!config.isContinueOnError()) {
                        throw new AdapterException.ProcessingException(AdapterType.SFTP, 
                                "SFTP file processing failed for " + entry.getFilename() + ": " + e.getMessage(), e);
                    }
                }
            }
            
        } finally {
            if ("per-file-transfer".equals(config.getConnectionMode())) {
                if (channel != null && channel.isConnected()) {
                    channel.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
        
        logger.info("SFTP sender adapter polled {} files from server", processedFiles.size());
        
        return AdapterResult.success(processedFiles, 
                String.format("Retrieved %d files from SFTP server", processedFiles.size()));
    }
    
    private Map<String, Object> processFile(ChannelSftp channel, ChannelSftp.LsEntry entry) throws Exception {
        SftpATTRS attrs = entry.getAttrs();
        
        // Check file age
        if (config.getMinFileAge() > 0) {
            long fileAge = System.currentTimeMillis() - (attrs.getMTime() * 1000L);
            if (fileAge < config.getMinFileAge()) {
                logger.debug("SFTP file {} is too young, skipping", entry.getFilename());
                return null;
            }
        }
        
        // Size validation
        long fileSize = attrs.getSize();
        if (fileSize > config.getMaxFileSize()) {
            logger.debug("SFTP file {} size {} exceeds maximum {}, skipping", 
                    entry.getFilename(), fileSize, config.getMaxFileSize());
            return null;
        }
        
        // Handle empty files
        if (fileSize == 0) {
            return handleEmptyFile(entry);
        }
        
        // Download file content
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", entry.getFilename());
        fileData.put("fileSize", fileSize);
        fileData.put("lastModified", new Date(attrs.getMTime() * 1000L));
        fileData.put("sftpPath", config.getSourceDirectory() + "/" + entry.getFilename());
        fileData.put("permissions", attrs.getPermissionsString());
        
        // Download file content
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            channel.get(entry.getFilename(), baos);
            
            byte[] content = baos.toByteArray();
            
            if (config.isLogFileContent()) {
                // Convert to string for logging
                String contentStr = new String(content, 
                        config.getFileEncoding() != null ? 
                                config.getFileEncoding() : "UTF-8");
                fileData.put("content", contentStr);
            } else {
                fileData.put("content", content);
            }
            
            // Generate checksum if configured
            if (config.isValidateFileIntegrity()) {
                String checksum = generateChecksum(content);
                fileData.put("checksum", checksum);
                
                // Check for duplicates
                if (config.isEnableDuplicateHandling()) {
                    if (isDuplicate(entry.getFilename(), checksum)) {
                        logger.debug("SFTP file {} is a duplicate, skipping", entry.getFilename());
                        return null;
                    }
                }
            }
        }
        
        return fileData;
    }
    
    private void handlePostProcessing(ChannelSftp channel, ChannelSftp.LsEntry entry) throws Exception {
        String processingMode = config.getProcessingMode();
        String fileName = entry.getFilename();
        
        switch (processingMode.toLowerCase()) {
            case "delete":
                channel.rm(fileName);
                logger.debug("Deleted processed SFTP file: {}", fileName);
                break;
                
            case "archive":
                if (config.getArchiveDirectory() != null) {
                    String archivePath = config.getArchiveDirectory() + "/" + fileName;
                    channel.rename(fileName, archivePath);
                    logger.debug("Archived SFTP file to: {}", archivePath);
                }
                break;
                
            case "move":
                if (config.getProcessedDirectory() != null) {
                    String movePath = config.getProcessedDirectory() + "/" + fileName;
                    channel.rename(fileName, movePath);
                    logger.debug("Moved SFTP file to: {}", movePath);
                }
                break;
                
            default:
                logger.debug("No post-processing configured for SFTP file: {}", fileName);
        }
    }
    
    private boolean shouldProcessFile(ChannelSftp.LsEntry entry) {
        String fileName = entry.getFilename();
        
        // Check if already processed
        if (processedFiles.containsKey(fileName)) {
            return false;
        }
        
        // Check exclusion patterns
        if (exclusionPattern != null && exclusionPattern.matcher(fileName).matches()) {
            return false;
        }
        
        return true;
    }
    
    private boolean matchesFilePattern(String fileName) {
        // Simple file name match
        if (config.getFileName() != null && !config.getFileName().isEmpty() && !"*".equals(config.getFileName())) {
            if (config.getFileName().contains("*") || config.getFileName().contains("?")) {
                // Simple glob pattern
                String regex = config.getFileName()
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".");
                return fileName.matches(regex);
            } else {
                return fileName.equals(config.getFileName());
            }
        }
        
        // If no specific pattern, match all files
        return true;
    }
    
    private void sortFiles(List<ChannelSftp.LsEntry> files) {
        String sorting = config.getSorting();
        if (sorting == null || "none".equals(sorting)) {
            return;
        }
        
        switch (sorting.toLowerCase()) {
            case "name":
                files.sort(Comparator.comparing(ChannelSftp.LsEntry::getFilename));
                break;
            case "date":
                files.sort(Comparator.comparing(entry -> entry.getAttrs().getMTime()));
                break;
            case "size":
                files.sort(Comparator.comparing(entry -> entry.getAttrs().getSize()));
                break;
        }
    }
    
    private Map<String, Object> handleEmptyFile(ChannelSftp.LsEntry entry) throws Exception {
        String handling = config.getEmptyFileHandling();
        
        switch (handling.toLowerCase()) {
            case "ignore":
                logger.debug("Ignoring empty SFTP file: {}", entry.getFilename());
                return null;
            case "error":
                throw new AdapterException.ValidationException(AdapterType.SFTP, 
                        "Empty file not allowed: " + entry.getFilename());
            case "process":
            default:
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("fileName", entry.getFilename());
                fileData.put("fileSize", 0L);
                fileData.put("lastModified", new Date(entry.getAttrs().getMTime() * 1000L));
                fileData.put("sftpPath", config.getSourceDirectory() + "/" + entry.getFilename());
                fileData.put("content", "");
                return fileData;
        }
    }
    
    private String generateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumAlgorithm());
        digest.update(content);
        
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private boolean isDuplicate(String fileName, String checksum) {
        // Simple duplicate detection based on checksum
        return processedFiles.containsValue(checksum);
    }
    
    private void connectToSftp() throws Exception {
        if (sshSession != null) {
            disconnectFromSftp();
        }
        
        sshSession = createSession();
        sshSession.connect();
        
        sftpChannel = (ChannelSftp) sshSession.openChannel("sftp");
        sftpChannel.connect();
    }
    
    private Session createSession() throws Exception {
        JSch jsch = new JSch();
        
        // Configure SSH settings
        if (config.isLogSSHDebug()) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                @Override
                public boolean isEnabled(int level) {
                    return true;
                }
                
                @Override
                public void log(int level, String message) {
                    logger.debug("SSH: {}", message);
                }
            });
        }
        
        // Add private key if configured
        if ("publickey".equals(config.getAuthenticationType()) || 
            config.getPreferredAuthentications().contains("publickey")) {
            if (config.getPrivateKey() != null) {
                if (config.getPassphrase() != null) {
                    jsch.addIdentity(config.getPrivateKey(), config.getPassphrase());
                } else {
                    jsch.addIdentity(config.getPrivateKey());
                }
            }
        }
        
        // Configure known hosts
        if (config.getKnownHostsFile() != null) {
            jsch.setKnownHosts(config.getKnownHostsFile());
        }
        
        // Create session
        int port = Integer.parseInt(config.getPort());
        Session session = jsch.getSession(config.getUserName(), config.getServerAddress(), port);
        
        // Set password if using password authentication
        if ("password".equals(config.getAuthenticationType()) && config.getPassword() != null) {
            session.setPassword(config.getPassword());
        }
        
        // Configure session properties
        Properties sessionConfig = new Properties();
        
        // Host key verification
        switch (config.getHostKeyVerification().toLowerCase()) {
            case "strict":
                sessionConfig.put("StrictHostKeyChecking", "yes");
                break;
            case "relaxed":
                sessionConfig.put("StrictHostKeyChecking", "ask");
                break;
            case "disabled":
                sessionConfig.put("StrictHostKeyChecking", "no");
                break;
        }
        
        // Compression
        if (!"none".equals(config.getSshCompression())) {
            sessionConfig.put("compression.s2c", config.getSshCompression());
            sessionConfig.put("compression.c2s", config.getSshCompression());
        }
        
        // Preferred authentications
        if (config.getPreferredAuthentications() != null) {
            sessionConfig.put("PreferredAuthentications", config.getPreferredAuthentications());
        }
        
        // Cipher suites
        if (config.getCipherSuites() != null) {
            sessionConfig.put("cipher.s2c", config.getCipherSuites());
            sessionConfig.put("cipher.c2s", config.getCipherSuites());
        }
        
        // MAC algorithms
        if (config.getMacAlgorithms() != null) {
            sessionConfig.put("mac.s2c", config.getMacAlgorithms());
            sessionConfig.put("mac.c2s", config.getMacAlgorithms());
        }
        
        // Key exchange algorithms
        if (config.getKexAlgorithms() != null) {
            sessionConfig.put("kex", config.getKexAlgorithms());
        }
        
        session.setConfig(sessionConfig);
        
        // Set timeout
        int timeout = Integer.parseInt(config.getTimeout());
        session.setTimeout(timeout);
        
        return session;
    }
    
    private void disconnectFromSftp() {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            sftpChannel = null;
        }
        
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
            sshSession = null;
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getServerAddress() == null || config.getServerAddress().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "SFTP server address is required");
        }
        if (config.getUserName() == null || config.getUserName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "SFTP username is required");
        }
        
        // Validate authentication configuration
        if ("password".equals(config.getAuthenticationType()) && config.getPassword() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "Password is required for password authentication");
        }
        
        if ("publickey".equals(config.getAuthenticationType()) && config.getPrivateKey() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "Private key is required for public key authentication");
        }
        
        if (config.getSourceDirectory() == null || config.getSourceDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "SFTP source directory is required");
        }
    }
    
    private void initializePatterns() throws Exception {
        // Initialize exclusion pattern if configured
        if (config.getExclusionMask() != null && !config.getExclusionMask().trim().isEmpty()) {
            try {
                exclusionPattern = Pattern.compile(config.getExclusionMask());
            } catch (Exception e) {
                throw new AdapterException.ConfigurationException(AdapterType.SFTP, 
                        "Invalid exclusion pattern: " + config.getExclusionMask(), e);
            }
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("SFTP Sender (Inbound): %s:%s, User: %s, Dir: %s, Auth: %s, Polling: %sms, Processing: %s", 
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getSourceDirectory(),
                config.getAuthenticationType(),
                config.getPollingInterval(),
                config.getProcessingMode());
    }
}