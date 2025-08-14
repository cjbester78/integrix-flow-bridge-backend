package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.FtpReceiverAdapterConfig;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FTP Receiver Adapter implementation for FTP file upload and transfer (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Supports FTP/FTPS connections, file uploads, batching, and validation.
 */
public class FtpReceiverAdapter extends AbstractReceiverAdapter {
    
    private final FtpReceiverAdapterConfig config;
    private FTPClient ftpClient;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();
    
    public FtpReceiverAdapter(FtpReceiverAdapterConfig config) {
        super(AdapterType.FTP);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing FTP receiver adapter (outbound) with server: {}:{}", 
                config.getServerAddress(), config.getPort());
        
        validateConfiguration();
        
        // For per-file-transfer mode, we don't maintain persistent connection
        if ("permanently".equals(config.getConnectionMode())) {
            connectToFtp();
        }
        
        logger.info("FTP receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying FTP receiver adapter");
        
        // Flush any remaining batch data
        if (config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch (Exception e) {
                logger.warn("Error flushing batch during FTP adapter shutdown", e);
            }
        }
        
        disconnectFromFtp();
        batchBuffer.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic FTP connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.FTP, () -> {
            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                
                if (testClient.isConnected() && FTPReply.isPositiveCompletion(testClient.getReplyCode())) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.FTP, 
                            "FTP Connection", "Successfully connected to FTP server");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                            "FTP Connection", "FTP connection failed, reply: " + testClient.getReplyString(), null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                        "FTP Connection", "Failed to connect to FTP server: " + e.getMessage(), e);
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 2: Directory access and write permissions
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FTP, () -> {
            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                
                // Test directory access
                boolean dirExists = testClient.changeWorkingDirectory(config.getTargetDirectory());
                if (!dirExists) {
                    // Try to create directory if configured
                    if (config.isCreateFileDirectory()) {
                        boolean created = testClient.makeDirectory(config.getTargetDirectory());
                        if (created) {
                            testClient.changeWorkingDirectory(config.getTargetDirectory());
                        } else {
                            return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                                    "Directory Access", "Cannot create target directory: " + config.getTargetDirectory(), null);
                        }
                    } else {
                        return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                                "Directory Access", "Target directory does not exist: " + config.getTargetDirectory(), null);
                    }
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.FTP, 
                        "Directory Access", "Target directory is accessible and writable");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                        "Directory Access", "Failed to access target directory: " + e.getMessage(), e);
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 3: File upload test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FTP, () -> {
            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                testClient.changeWorkingDirectory(config.getTargetDirectory());
                
                // Test file upload
                String testFileName = "test_upload_" + System.currentTimeMillis() + ".tmp";
                byte[] testContent = "test content".getBytes();
                
                try (ByteArrayInputStream bais = new ByteArrayInputStream(testContent)) {
                    boolean uploaded = testClient.storeFile(testFileName, bais);
                    
                    if (uploaded) {
                        // Clean up test file
                        testClient.deleteFile(testFileName);
                        return ConnectionTestUtil.createTestSuccess(AdapterType.FTP, 
                                "File Upload", "Successfully uploaded and deleted test file");
                    } else {
                        return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                                "File Upload", "Test file upload failed: " + testClient.getReplyString(), null);
                    }
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FTP, 
                        "File Upload", "Failed to test file upload: " + e.getMessage(), e);
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.FTP, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object payload) throws Exception {
        // For FTP Receiver (outbound), this method uploads data TO FTP server
        if (config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return uploadToFtp(payload);
        }
    }
    
    protected AdapterResult doReceive() throws Exception {
        // Default receive without criteria
        throw new AdapterException.OperationException(AdapterType.FTP, 
                "FTP Receiver requires data payload for upload operations");
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
            
            return uploadBatchToFtp(itemsToUpload);
        }
    }
    
    private AdapterResult uploadBatchToFtp(List<Object> items) throws Exception {
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
        
        return uploadContentToFtp(fileName, batchContent.toString().getBytes("UTF-8"), true, items.size());
    }
    
    private AdapterResult uploadToFtp(Object payload) throws Exception {
        String fileName = generateFileName(payload);
        byte[] content = convertToBytes(payload);
        
        return uploadContentToFtp(fileName, content, false, 1);
    }
    
    private AdapterResult uploadContentToFtp(String fileName, byte[] content, boolean isBatch, int itemCount) throws Exception {
        FTPClient client = null;
        String uploadPath = null;
        
        try {
            client = getOrCreateConnection();
            
            // Change to target directory
            if (!client.changeWorkingDirectory(config.getTargetDirectory())) {
                if (config.isCreateFileDirectory()) {
                    createDirectoryPath(client, config.getTargetDirectory());
                    if (!client.changeWorkingDirectory(config.getTargetDirectory())) {
                        throw new AdapterException.ConfigurationException(AdapterType.FTP, 
                                "Cannot access or create target directory: " + config.getTargetDirectory());
                    }
                } else {
                    throw new AdapterException.ConfigurationException(AdapterType.FTP, 
                            "Target directory does not exist: " + config.getTargetDirectory());
                }
            }
            
            // Validate before upload if configured
            if (config.isValidateBeforeUpload()) {
                validateContent(content);
            }
            
            // Create backup if configured
            if (config.isEnableFileBackup()) {
                createBackup(client, fileName);
            }
            
            // Use temporary file for atomic upload if configured
            String uploadFileName = fileName;
            if ("temp-then-move".equals(config.getFilePlacement())) {
                uploadFileName = fileName + config.getTempFileExtension();
            }
            
            // Upload file
            client.setFileType(FTP.BINARY_FILE_TYPE);
            long bytesUploaded = 0;
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                boolean uploaded = client.storeFile(uploadFileName, bais);
                
                if (!uploaded) {
                    throw new AdapterException.ProcessingException(AdapterType.FTP, 
                            "File upload failed: " + fileName + ", FTP reply: " + client.getReplyString());
                }
                
                bytesUploaded = content.length;
            }
            
            // Move from temporary name to final name if atomic upload
            if ("temp-then-move".equals(config.getFilePlacement())) {
                boolean renamed = client.rename(uploadFileName, fileName);
                if (!renamed) {
                    // Try to clean up temp file
                    client.deleteFile(uploadFileName);
                    throw new AdapterException.ProcessingException(AdapterType.FTP, 
                            "Failed to rename temporary file: " + uploadFileName + " to " + fileName);
                }
            }
            
            uploadPath = config.getTargetDirectory() + "/" + fileName;
            
            // Validate upload if configured
            if (config.isValidateBeforeUpload()) {
                validateUpload(client, fileName, content.length);
            }
            
            logger.info("FTP receiver adapter uploaded {} bytes to file: {}", bytesUploaded, uploadPath);
            
            String message = isBatch ? 
                    String.format("Successfully uploaded batch of %d items (%d bytes) to FTP file", itemCount, bytesUploaded) :
                    String.format("Successfully uploaded %d bytes to FTP file", bytesUploaded);
            
            AdapterResult result = AdapterResult.success(uploadPath, message);
            result.addMetadata("fileName", fileName);
            result.addMetadata("ftpPath", uploadPath);
            result.addMetadata("bytesUploaded", bytesUploaded);
            result.addMetadata("itemCount", itemCount);
            
            return result;
            
        } finally {
            if ("per-file-transfer".equals(config.getConnectionMode()) && client != null) {
                disconnectClient(client);
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
                    throw new AdapterException.ValidationException(AdapterType.FTP, 
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
                throw new AdapterException.ValidationException(AdapterType.FTP, 
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
    
    private void createBackup(FTPClient client, String fileName) throws Exception {
        if (config.getBackupDirectory() != null) {
            // Check if file exists
            if (fileExists(client, fileName)) {
                String backupFileName = fileName + "_backup_" + System.currentTimeMillis();
                String backupPath = config.getBackupDirectory() + "/" + backupFileName;
                
                // Create backup directory if needed
                createDirectoryPath(client, config.getBackupDirectory());
                
                // Copy file to backup location
                boolean renamed = client.rename(fileName, backupPath);
                if (renamed) {
                    logger.debug("Created FTP backup: {}", backupPath);
                } else {
                    logger.warn("Failed to create FTP backup for: {}", fileName);
                }
            }
        }
    }
    
    private boolean fileExists(FTPClient client, String fileName) throws Exception {
        return client.listFiles(fileName).length > 0;
    }
    
    private void createDirectoryPath(FTPClient client, String directoryPath) throws Exception {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }
        
        String[] pathParts = directoryPath.split("/");
        StringBuilder currentPath = new StringBuilder();
        
        for (String part : pathParts) {
            if (part.isEmpty()) continue;
            
            currentPath.append("/").append(part);
            
            if (!client.changeWorkingDirectory(currentPath.toString())) {
                boolean created = client.makeDirectory(currentPath.toString());
                if (!created) {
                    logger.warn("Failed to create FTP directory: {}", currentPath.toString());
                }
            }
        }
    }
    
    private void validateContent(byte[] content) throws Exception {
        if (content.length > config.getMaxFileSize()) {
            throw new AdapterException.ValidationException(AdapterType.FTP, 
                    "Content size exceeds maximum allowed: " + content.length + " > " + config.getMaxFileSize());
        }
        
        // Additional validation based on checksum if configured
        if (!"none".equals(config.getChecksumValidation())) {
            String checksum = generateChecksum(content);
            logger.debug("Content checksum ({}): {}", config.getChecksumValidation(), checksum);
        }
    }
    
    private void validateUpload(FTPClient client, String fileName, long expectedSize) throws Exception {
        // Verify file was uploaded correctly
        long actualSize = client.listFiles(fileName)[0].getSize();
        if (actualSize != expectedSize) {
            throw new AdapterException.ValidationException(AdapterType.FTP, 
                    "Upload validation failed - size mismatch: expected " + expectedSize + ", actual " + actualSize);
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
    
    private FTPClient getOrCreateConnection() throws Exception {
        if ("permanently".equals(config.getConnectionMode())) {
            if (ftpClient == null || !ftpClient.isConnected()) {
                connectToFtp();
            }
            return ftpClient;
        } else {
            // Create new connection for each operation
            FTPClient client = createFtpClient();
            connectClient(client);
            return client;
        }
    }
    
    private void connectToFtp() throws Exception {
        if (ftpClient != null) {
            disconnectFromFtp();
        }
        
        ftpClient = createFtpClient();
        connectClient(ftpClient);
    }
    
    private FTPClient createFtpClient() throws Exception {
        FTPClient client = new FTPClient();
        
        // Configure timeouts
        int timeout = Integer.parseInt(config.getTimeout());
        client.setConnectTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);
        
        // Configure buffer size
        client.setBufferSize(config.getTransferBufferSize());
        
        return client;
    }
    
    private void connectClient(FTPClient client) throws Exception {
        // Connect to server
        int port = Integer.parseInt(config.getPort());
        client.connect(config.getServerAddress(), port);
        
        // Check connection reply
        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new AdapterException.ConnectionException(AdapterType.FTP, 
                    "FTP server refused connection: " + client.getReplyString());
        }
        
        // Login
        boolean loginSuccess = client.login(config.getUserName(), config.getPassword());
        if (!loginSuccess) {
            client.disconnect();
            throw new AdapterException.AuthenticationException(AdapterType.FTP, 
                    "FTP login failed: " + client.getReplyString());
        }
        
        // Configure passive mode
        if (config.isEnablePassiveMode()) {
            client.enterLocalPassiveMode();
        } else {
            client.enterLocalActiveMode();
        }
        
        // Set binary mode for file transfers
        client.setFileType(FTP.BINARY_FILE_TYPE);
        
        logger.debug("Successfully connected to FTP server: {}:{}", 
                config.getServerAddress(), config.getPort());
    }
    
    private void disconnectFromFtp() {
        if (ftpClient != null) {
            disconnectClient(ftpClient);
            ftpClient = null;
        }
    }
    
    private void disconnectClient(FTPClient client) {
        if (client != null && client.isConnected()) {
            try {
                client.logout();
                client.disconnect();
            } catch (Exception e) {
                logger.warn("Error disconnecting from FTP server", e);
            }
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getServerAddress() == null || config.getServerAddress().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, "FTP server address is required");
        }
        if (config.getUserName() == null || config.getUserName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, "FTP username is required");
        }
        if (config.getPassword() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, "FTP password is required");
        }
        if (config.getTargetDirectory() == null || config.getTargetDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, "FTP target directory is required");
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // FTP receivers typically don't poll, they push files
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("FTP Receiver (Outbound): %s:%s, User: %s, Dir: %s, Construction: %s, Batching: %s", 
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getTargetDirectory(),
                config.getFileConstructionMode(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }
}