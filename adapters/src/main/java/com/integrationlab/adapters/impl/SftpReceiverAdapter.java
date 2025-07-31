package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.SftpReceiverAdapterConfig;

import com.jcraft.jsch.*;
import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SFTP Receiver Adapter implementation for SFTP file upload and transfer (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Supports SFTP connections, file uploads, batching, and SSH authentication.
 */
public class SftpReceiverAdapter extends AbstractReceiverAdapter {
    
    private final SftpReceiverAdapterConfig config;
    private Session sshSession;
    private ChannelSftp sftpChannel;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();
    
    public SftpReceiverAdapter(SftpReceiverAdapterConfig config) {
        super(AdapterType.SFTP);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing SFTP receiver adapter (outbound) with server: {}:{}", 
                config.getServerAddress(), config.getPort());
        
        validateConfiguration();
        
        // For per-file-transfer mode, we don't maintain persistent connection
        if ("permanently".equals(config.getConnectionMode())) {
            connectToSftp();
        }
        
        logger.info("SFTP receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying SFTP receiver adapter");
        
        // Flush any remaining batch data
        if (config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch (Exception e) {
                logger.warn("Error flushing batch during SFTP adapter shutdown", e);
            }
        }
        
        disconnectFromSftp();
        batchBuffer.clear();
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
        
        // Test 2: Directory access and write permissions
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SFTP, () -> {
            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                
                // Test directory access
                try {
                    testChannel.cd(config.getTargetDirectory());
                } catch (SftpException e) {
                    // Try to create directory if configured
                    if (config.isCreateFileDirectory()) {
                        createDirectoryPath(testChannel, config.getTargetDirectory());
                        testChannel.cd(config.getTargetDirectory());
                    } else {
                        return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                                "Directory Access", "Target directory does not exist: " + config.getTargetDirectory(), e);
                    }
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.SFTP, 
                        "Directory Access", "Target directory is accessible and writable");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                        "Directory Access", "Failed to access target directory: " + e.getMessage(), e);
            } finally {
                if (testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if (testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));
        
        // Test 3: File upload test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SFTP, () -> {
            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                testChannel.cd(config.getTargetDirectory());
                
                // Test file upload
                String testFileName = "test_upload_" + System.currentTimeMillis() + ".tmp";
                byte[] testContent = "test content".getBytes();
                
                try (ByteArrayInputStream bais = new ByteArrayInputStream(testContent)) {
                    testChannel.put(bais, testFileName);
                    
                    // Verify file exists
                    @SuppressWarnings("unchecked")
                    Vector<ChannelSftp.LsEntry> files = testChannel.ls(testFileName);
                    if (files != null && !files.isEmpty()) {
                        // Clean up test file
                        testChannel.rm(testFileName);
                        return ConnectionTestUtil.createTestSuccess(AdapterType.SFTP, 
                                "File Upload", "Successfully uploaded and deleted test file");
                    } else {
                        return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                                "File Upload", "Test file upload failed - file not found after upload", null);
                    }
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SFTP, 
                        "File Upload", "Failed to test file upload: " + e.getMessage(), e);
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
    protected AdapterResult doReceive(Object payload) throws Exception {
        // For SFTP Receiver (outbound), this method uploads data TO SFTP server
        if (config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return uploadToSftp(payload);
        }
    }
    
    @Override
    protected AdapterResult doReceive() throws Exception {
        // Default receive without criteria
        throw new AdapterException.OperationException(AdapterType.SFTP, 
                "SFTP Receiver requires data payload for upload operations");
    }
    
    private AdapterResult addToBatch(Object payload) throws Exception {
        synchronized (batchBuffer) {
            batchBuffer.add(payload);
            
            boolean shouldFlush = false;
            
            // Check size-based flushing
            if ("SIZE_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                if (config.getBatchSize() != null && batchBuffer.size() >= config.getBatchSize()) {
                    shouldFlush = true;
                }
            }
            
            // Check time-based flushing
            if ("TIME_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                long timeSinceLastFlush = System.currentTimeMillis() - lastBatchFlush;
                if (timeSinceLastFlush >= config.getBatchTimeoutMs()) {
                    shouldFlush = true;
                }
            }
            
            if (shouldFlush) {
                return flushBatch();
            } else {
                return AdapterResult.success(null, 
                        String.format("Added to batch (%d/%d items)", 
                                batchBuffer.size(), 
                                config.getBatchSize() != null ? config.getBatchSize() : "unlimited"));
            }
        }
    }
    
    private AdapterResult flushBatch() throws Exception {
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) {
                return AdapterResult.success(null, "No items in batch to flush");
            }
            
            List<Object> itemsToUpload = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();
            
            return uploadBatchToSftp(itemsToUpload);
        }
    }
    
    private AdapterResult uploadBatchToSftp(List<Object> items) throws Exception {
        String fileName = generateBatchFileName();
        
        // Combine all items into single file content
        StringBuilder batchContent = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String itemContent = convertToString(items.get(i));
            batchContent.append(itemContent);
            if (i < items.size() - 1) {
                batchContent.append(System.lineSeparator());
            }
        }
        
        return uploadContentToSftp(fileName, batchContent.toString().getBytes("UTF-8"), true, items.size());
    }
    
    private AdapterResult uploadToSftp(Object payload) throws Exception {
        String fileName = generateFileName(payload);
        byte[] content = convertToBytes(payload);
        
        return uploadContentToSftp(fileName, content, false, 1);
    }
    
    private AdapterResult uploadContentToSftp(String fileName, byte[] content, boolean isBatch, int itemCount) throws Exception {
        Session session = null;
        ChannelSftp channel = null;
        String uploadPath = null;
        
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
            
            // Change to target directory
            try {
                channel.cd(config.getTargetDirectory());
            } catch (SftpException e) {
                if (config.isCreateFileDirectory()) {
                    createDirectoryPath(channel, config.getTargetDirectory());
                    channel.cd(config.getTargetDirectory());
                } else {
                    throw new AdapterException.ConfigurationException(AdapterType.SFTP, 
                            "Target directory does not exist: " + config.getTargetDirectory(), e);
                }
            }
            
            // Validate before upload if configured
            if (config.isValidateBeforeUpload()) {
                validateContent(content);
            }
            
            // Create backup if configured
            if (config.isEnableFileBackup()) {
                createBackup(channel, fileName);
            }
            
            // Use temporary file for atomic upload if configured
            String uploadFileName = fileName;
            if ("temp-then-move".equals(config.getFilePlacement())) {
                uploadFileName = fileName + config.getTempFileExtension();
            }
            
            // Upload file
            long bytesUploaded = 0;
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                channel.put(bais, uploadFileName);
                bytesUploaded = content.length;
            }
            
            // Move from temporary name to final name if atomic upload
            if ("temp-then-move".equals(config.getFilePlacement())) {
                try {
                    channel.rename(uploadFileName, fileName);
                } catch (SftpException e) {
                    // Try to clean up temp file
                    try {
                        channel.rm(uploadFileName);
                    } catch (Exception ignored) {}
                    throw new AdapterException.ProcessingException(AdapterType.SFTP, 
                            "Failed to rename temporary file: " + uploadFileName + " to " + fileName, e);
                }
            }
            
            // Set file permissions if configured
            if (config.getFilePermissions() != null) {
                try {
                    int permissions = Integer.parseInt(config.getFilePermissions(), 8);
                    channel.chmod(permissions, fileName);
                } catch (Exception e) {
                    logger.warn("Failed to set file permissions: {}", config.getFilePermissions(), e);
                }
            }
            
            uploadPath = config.getTargetDirectory() + "/" + fileName;
            
            // Validate upload if configured
            if (config.isValidateBeforeUpload()) {
                validateUpload(channel, fileName, content.length);
            }
            
            logger.info("SFTP receiver adapter uploaded {} bytes to file: {}", bytesUploaded, uploadPath);
            
            String message = isBatch ? 
                    String.format("Successfully uploaded batch of %d items (%d bytes) to SFTP file", itemCount, bytesUploaded) :
                    String.format("Successfully uploaded %d bytes to SFTP file", bytesUploaded);
            
            AdapterResult result = AdapterResult.success(uploadPath, message);
            result.addMetadata("fileName", fileName);
            result.addMetadata("sftpPath", uploadPath);
            result.addMetadata("bytesUploaded", bytesUploaded);
            result.addMetadata("itemCount", itemCount);
            
            return result;
            
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
    }
    
    private byte[] convertToBytes(Object payload) throws Exception {
        if (payload == null) {
            return handleEmptyMessage();
        }
        
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        }
        
        if (payload instanceof String) {
            return ((String) payload).getBytes(
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }
        
        if (payload instanceof Map || payload instanceof Collection) {
            // Convert to JSON or formatted string
            String jsonStr = payload.toString(); // Simple implementation
            return jsonStr.getBytes(
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }
        
        return payload.toString().getBytes(
                config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
    }
    
    private String convertToString(Object payload) throws Exception {
        if (payload == null) {
            String emptyHandling = config.getEmptyMessageHandling();
            switch (emptyHandling.toLowerCase()) {
                case "ignore":
                    return "";
                case "error":
                    throw new AdapterException.ValidationException(AdapterType.SFTP, 
                            "Empty message not allowed");
                case "process":
                default:
                    return "";
            }
        }
        
        if (payload instanceof String) {
            return (String) payload;
        }
        
        if (payload instanceof byte[]) {
            return new String((byte[]) payload, 
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }
        
        return payload.toString();
    }
    
    private byte[] handleEmptyMessage() throws Exception {
        String handling = config.getEmptyMessageHandling();
        
        switch (handling.toLowerCase()) {
            case "ignore":
                return new byte[0];
            case "error":
                throw new AdapterException.ValidationException(AdapterType.SFTP, 
                        "Empty message not allowed");
            case "process":
            default:
                return new byte[0];
        }
    }
    
    private String generateFileName(Object payload) {
        if (config.getTargetFileName() != null && !config.getTargetFileName().isEmpty()) {
            return processTemplate(config.getTargetFileName(), payload);
        }
        
        if (config.getFileNamingPattern() != null && !config.getFileNamingPattern().isEmpty()) {
            return processTemplate(config.getFileNamingPattern(), payload);
        }
        
        // Generate default filename
        String baseName = "file_" + System.currentTimeMillis();
        
        if (config.isIncludeTimestamp()) {
            String timestamp = DateTimeFormatter.ofPattern(config.getTimestampFormat()).format(LocalDateTime.now());
            baseName = "file_" + timestamp;
        }
        
        return baseName + ".txt";
    }
    
    private String generateBatchFileName() {
        String baseName = String.format("batch_%s_%d", 
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()),
                batchCounter.incrementAndGet());
        
        return baseName + ".txt";
    }
    
    private String processTemplate(String template, Object payload) {
        String result = template;
        
        // Replace common placeholders
        result = result.replace("${timestamp}", String.valueOf(System.currentTimeMillis()));
        result = result.replace("${datetime}", DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()));
        result = result.replace("${date}", DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));
        result = result.replace("${time}", DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now()));
        
        // Replace payload-specific placeholders if payload is available
        if (payload instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) payload;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
        }
        
        return result;
    }
    
    private void createBackup(ChannelSftp channel, String fileName) throws Exception {
        if (config.getBackupDirectory() != null) {
            // Check if file exists
            try {
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = channel.ls(fileName);
                if (files != null && !files.isEmpty()) {
                    String backupFileName = fileName + "_backup_" + System.currentTimeMillis();
                    String backupPath = config.getBackupDirectory() + "/" + backupFileName;
                    
                    // Create backup directory if needed
                    createDirectoryPath(channel, config.getBackupDirectory());
                    
                    // Copy file to backup location
                    channel.rename(fileName, backupPath);
                    logger.debug("Created SFTP backup: {}", backupPath);
                }
            } catch (SftpException e) {
                // File doesn't exist, no backup needed
                logger.debug("No backup needed, file doesn't exist: {}", fileName);
            }
        }
    }
    
    private void createDirectoryPath(ChannelSftp channel, String directoryPath) throws Exception {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }
        
        String[] pathParts = directoryPath.split("/");
        StringBuilder currentPath = new StringBuilder();
        
        for (String part : pathParts) {
            if (part.isEmpty()) continue;
            
            currentPath.append("/").append(part);
            
            try {
                channel.cd(currentPath.toString());
            } catch (SftpException e) {
                try {
                    channel.mkdir(currentPath.toString());
                    logger.debug("Created SFTP directory: {}", currentPath.toString());
                } catch (SftpException mkdirEx) {
                    logger.warn("Failed to create SFTP directory: {}", currentPath.toString(), mkdirEx);
                }
            }
        }
    }
    
    private void validateContent(byte[] content) throws Exception {
        if (content.length > config.getMaxFileSize()) {
            throw new AdapterException.ValidationException(AdapterType.SFTP, 
                    "Content size exceeds maximum allowed: " + content.length + " > " + config.getMaxFileSize());
        }
        
        // Additional validation based on checksum if configured
        if (!"none".equals(config.getChecksumValidation())) {
            String checksum = generateChecksum(content);
            logger.debug("Content checksum ({}): {}", config.getChecksumValidation(), checksum);
        }
    }
    
    private void validateUpload(ChannelSftp channel, String fileName, long expectedSize) throws Exception {
        try {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = channel.ls(fileName);
            if (files == null || files.isEmpty()) {
                throw new AdapterException.ValidationException(AdapterType.SFTP, 
                        "Upload validation failed - file not found after upload: " + fileName);
            }
            
            long actualSize = files.get(0).getAttrs().getSize();
            if (actualSize != expectedSize) {
                throw new AdapterException.ValidationException(AdapterType.SFTP, 
                        "Upload validation failed - size mismatch: expected " + expectedSize + ", actual " + actualSize);
            }
        } catch (SftpException e) {
            throw new AdapterException.ValidationException(AdapterType.SFTP, 
                    "Upload validation failed - cannot verify file: " + fileName, e);
        }
    }
    
    private String generateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumValidation().toUpperCase());
        digest.update(content);
        
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
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
        
        if (config.getTargetDirectory() == null || config.getTargetDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, "SFTP target directory is required");
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("SFTP Receiver (Outbound): %s:%s, User: %s, Dir: %s, Auth: %s, Construction: %s, Batching: %s", 
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getTargetDirectory(),
                config.getAuthenticationType(),
                config.getFileConstructionMode(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }
}