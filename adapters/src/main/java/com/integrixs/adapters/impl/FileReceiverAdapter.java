package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.FileReceiverAdapterConfig;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File Receiver Adapter implementation for file creation and writing (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Supports file creation, atomic writes, batching, backup, and validation.
 */
public class FileReceiverAdapter extends AbstractReceiverAdapter {
    
    private final FileReceiverAdapterConfig config;
    private Path targetDirectory;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();
    
    public FileReceiverAdapter(FileReceiverAdapterConfig config) {
        super(AdapterType.FILE);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing File receiver adapter (outbound) with directory: {}", config.getTargetDirectory());
        
        validateConfiguration();
        initializeDirectory();
        
        logger.info("File receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying File receiver adapter");
        
        // Flush any remaining batch data
        if (config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch (Exception e) {
                logger.warn("Error flushing batch during shutdown", e);
            }
        }
        
        batchBuffer.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Directory accessibility
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.FILE, () -> {
            try {
                if (!Files.exists(targetDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Target directory does not exist: " + targetDirectory, null);
                }
                
                if (!Files.isDirectory(targetDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Path is not a directory: " + targetDirectory, null);
                }
                
                if (!Files.isWritable(targetDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Directory is not writable: " + targetDirectory, null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                        "Directory Access", "Target directory is accessible and writable");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                        "Directory Access", "Failed to access directory: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Test file creation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FILE, () -> {
            try {
                String testFileName = "test_file_" + System.currentTimeMillis() + ".tmp";
                Path testFile = targetDirectory.resolve(testFileName);
                
                Files.write(testFile, "test content".getBytes());
                
                if (Files.exists(testFile)) {
                    Files.deleteIfExists(testFile);
                    return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                            "File Creation", "Successfully created and deleted test file");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "File Creation", "Test file was not created", null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                        "File Creation", "Failed to create test file: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Backup and temp directories validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FILE, () -> {
            try {
                validateSupportDirectories();
                return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                        "Support Directories", "All configured support directories are accessible");
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                        "Support Directories", "Support directory validation failed: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.FILE, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object payload) throws Exception {
        // For File Receiver (outbound), this method writes data TO files
        if (config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return writeToFile(payload);
        }
    }
    
    protected AdapterResult doReceive() throws Exception {
        // Default receive without criteria
        throw new AdapterException.OperationException(AdapterType.FILE, 
                "File Receiver requires data payload for file operations");
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
            
            List<Object> itemsToWrite = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();
            
            return writeBatchToFile(itemsToWrite);
        }
    }
    
    private AdapterResult writeBatchToFile(List<Object> items) throws Exception {
        String fileName = generateBatchFileName();
        Path targetFile = targetDirectory.resolve(fileName);
        
        return writeItemsToFile(targetFile, items, true);
    }
    
    private AdapterResult writeToFile(Object payload) throws Exception {
        String fileName = generateFileName(payload);
        Path targetFile = targetDirectory.resolve(fileName);
        
        return writeItemsToFile(targetFile, Arrays.asList(payload), false);
    }
    
    private AdapterResult writeItemsToFile(Path targetFile, List<Object> items, boolean isBatch) throws Exception {
        // Check if file already exists and handle accordingly
        if (Files.exists(targetFile) && !config.isOverwriteExistingFile()) {
            if ("create".equals(config.getFileConstructionMode())) {
                throw new AdapterException.ValidationException(AdapterType.FILE, 
                        "File already exists and overwrite is disabled: " + targetFile);
            }
        }
        
        // Create backup if configured
        if (config.isCreateBackup() && Files.exists(targetFile)) {
            createBackup(targetFile);
        }
        
        // Use atomic write if configured
        Path writeTarget = targetFile;
        if (config.isUseAtomicWrite()) {
            String tempFileName = targetFile.getFileName().toString() + config.getTemporaryFileExtension();
            writeTarget = config.getTemporaryDirectory() != null ? 
                    Paths.get(config.getTemporaryDirectory()).resolve(tempFileName) :
                    targetFile.resolveSibling(tempFileName);
        }
        
        // Ensure parent directories exist
        Files.createDirectories(writeTarget.getParent());
        
        try {
            long bytesWritten = 0;
            
            // Write data based on construction mode
            switch (config.getFileConstructionMode().toLowerCase()) {
                case "create":
                case "overwrite":
                    bytesWritten = writeNewFile(writeTarget, items);
                    break;
                case "append":
                    bytesWritten = appendToFile(writeTarget, items);
                    break;
                default:
                    bytesWritten = writeNewFile(writeTarget, items);
            }
            
            // Move from temporary location if atomic write
            if (config.isUseAtomicWrite() && !writeTarget.equals(targetFile)) {
                Files.move(writeTarget, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Set file permissions if configured
            setFilePermissions(targetFile);
            
            // Generate checksum if configured
            if (config.isGenerateChecksum()) {
                generateChecksumFile(targetFile);
            }
            
            // Validate file creation
            if (config.isValidateFileCreation()) {
                validateFileCreation(targetFile, bytesWritten);
            }
            
            logger.info("File receiver adapter wrote {} bytes to file: {}", bytesWritten, targetFile);
            
            String message = isBatch ? 
                    String.format("Successfully wrote batch of %d items (%d bytes) to file", items.size(), bytesWritten) :
                    String.format("Successfully wrote %d bytes to file", bytesWritten);
            
            AdapterResult result = AdapterResult.success(targetFile.toString(), message);
            result.addMetadata("fileName", targetFile.getFileName().toString());
            result.addMetadata("filePath", targetFile.toAbsolutePath().toString());
            result.addMetadata("bytesWritten", bytesWritten);
            result.addMetadata("itemCount", items.size());
            
            return result;
            
        } catch (Exception e) {
            // Clean up temporary file on error
            if (config.isUseAtomicWrite() && !writeTarget.equals(targetFile)) {
                try {
                    Files.deleteIfExists(writeTarget);
                } catch (Exception cleanupEx) {
                    logger.warn("Failed to clean up temporary file: {}", writeTarget, cleanupEx);
                }
            }
            throw e;
        }
    }
    
    private long writeNewFile(Path file, List<Object> items) throws Exception {
        long bytesWritten = 0;
        
        try (BufferedWriter writer = createWriter(file)) {
            // Write header if configured
            if (config.isIncludeHeaders() && config.getHeaderTemplate() != null) {
                String header = processTemplate(config.getHeaderTemplate(), null);
                writer.write(header);
                writer.write(getLineEnding());
                bytesWritten += header.getBytes(getCharset()).length + getLineEnding().getBytes(getCharset()).length;
            }
            
            // Write items
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String content = convertToString(item);
                
                if (content != null && !content.isEmpty() || 
                    !"skip".equals(config.getEmptyMessageHandling())) {
                    
                    writer.write(content);
                    bytesWritten += content.getBytes(getCharset()).length;
                    
                    // Add record separator except for last item
                    if (i < items.size() - 1 || config.getFooterTemplate() != null) {
                        writer.write(config.getRecordSeparator());
                        bytesWritten += config.getRecordSeparator().getBytes(getCharset()).length;
                    }
                }
            }
            
            // Write footer if configured
            if (config.getFooterTemplate() != null) {
                String footer = processTemplate(config.getFooterTemplate(), null);
                writer.write(footer);
                bytesWritten += footer.getBytes(getCharset()).length;
            }
        }
        
        return bytesWritten;
    }
    
    private long appendToFile(Path file, List<Object> items) throws Exception {
        long bytesWritten = 0;
        
        try (BufferedWriter writer = Files.newBufferedWriter(file, getCharset(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            
            for (Object item : items) {
                String content = convertToString(item);
                
                if (content != null && !content.isEmpty() || 
                    !"skip".equals(config.getEmptyMessageHandling())) {
                    
                    writer.write(content);
                    writer.write(config.getRecordSeparator());
                    bytesWritten += content.getBytes(getCharset()).length + 
                                   config.getRecordSeparator().getBytes(getCharset()).length;
                }
            }
        }
        
        return bytesWritten;
    }
    
    private BufferedWriter createWriter(Path file) throws Exception {
        if (config.isUseBufferedWriter()) {
            return Files.newBufferedWriter(file, getCharset(), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            return new BufferedWriter(Files.newBufferedWriter(file, getCharset(), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), 
                    config.getBufferSize());
        }
    }
    
    private String convertToString(Object item) throws Exception {
        if (item == null) {
            return handleEmptyMessage();
        }
        
        if (item instanceof String) {
            return (String) item;
        }
        
        if (item instanceof byte[]) {
            return new String((byte[]) item, getCharset());
        }
        
        if (item instanceof Map || item instanceof Collection) {
            // Convert to JSON or formatted string based on configuration
            return item.toString(); // Simple implementation
        }
        
        return item.toString();
    }
    
    private String handleEmptyMessage() throws Exception {
        String handling = config.getEmptyMessageHandling();
        
        switch (handling.toLowerCase()) {
            case "skip":
                return null;
            case "error":
                throw new AdapterException.ValidationException(AdapterType.FILE, 
                        "Empty message not allowed");
            case "create_empty":
            default:
                return "";
        }
    }
    
    private String generateFileName(Object payload) {
        if (config.getTargetFileName() != null && !config.getTargetFileName().isEmpty()) {
            return processTemplate(config.getTargetFileName(), payload);
        }
        
        if (config.getFileNamePattern() != null && !config.getFileNamePattern().isEmpty()) {
            return processTemplate(config.getFileNamePattern(), payload);
        }
        
        // Generate default filename
        return "file_" + System.currentTimeMillis() + ".txt";
    }
    
    private String generateBatchFileName() {
        String pattern = config.getBatchFileNamingPattern();
        if (pattern != null && !pattern.isEmpty()) {
            return processTemplate(pattern, null);
        }
        
        // Generate default batch filename
        return String.format("batch_%s_%d.txt", 
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()),
                batchCounter.incrementAndGet());
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
    
    private void createBackup(Path file) throws Exception {
        if (config.getBackupDirectory() != null) {
            Path backupDir = Paths.get(config.getBackupDirectory());
            Files.createDirectories(backupDir);
            
            String backupFileName = file.getFileName().toString() + config.getBackupFileExtension();
            Path backupFile = backupDir.resolve(backupFileName);
            
            Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Created backup: {}", backupFile);
            
            // Manage backup file count
            cleanupOldBackups(backupDir, file.getFileName().toString());
        }
    }
    
    private void cleanupOldBackups(Path backupDir, String baseFileName) throws Exception {
        String backupPattern = baseFileName + config.getBackupFileExtension();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, 
                path -> path.getFileName().toString().startsWith(backupPattern))) {
            
            List<Path> backupFiles = new ArrayList<>();
            stream.forEach(backupFiles::add);
            
            if (backupFiles.size() > config.getMaxBackupFiles()) {
                // Sort by modification time and delete oldest
                backupFiles.sort(Comparator.comparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        return null;
                    }
                }));
                
                int filesToDelete = backupFiles.size() - config.getMaxBackupFiles();
                for (int i = 0; i < filesToDelete; i++) {
                    Files.deleteIfExists(backupFiles.get(i));
                    logger.debug("Deleted old backup: {}", backupFiles.get(i));
                }
            }
        }
    }
    
    private void setFilePermissions(Path file) throws Exception {
        if (config.getFilePermissions() != null && !System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(config.getFilePermissions());
                Files.setPosixFilePermissions(file, permissions);
            } catch (Exception e) {
                logger.warn("Failed to set file permissions: {}", config.getFilePermissions(), e);
            }
        }
    }
    
    private void generateChecksumFile(Path file) throws Exception {
        String checksum = calculateChecksum(file);
        Path checksumFile = file.resolveSibling(file.getFileName() + config.getChecksumFileExtension());
        
        Files.write(checksumFile, (checksum + "  " + file.getFileName().toString()).getBytes());
        logger.debug("Generated checksum file: {}", checksumFile);
    }
    
    private String calculateChecksum(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumAlgorithm());
        
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private void validateFileCreation(Path file, long expectedBytes) throws Exception {
        if (!Files.exists(file)) {
            throw new AdapterException.ValidationException(AdapterType.FILE, 
                    "File was not created: " + file);
        }
        
        if (!Files.isRegularFile(file)) {
            throw new AdapterException.ValidationException(AdapterType.FILE, 
                    "Created path is not a regular file: " + file);
        }
        
        if (config.getMaxFileSize() != Long.MAX_VALUE && Files.size(file) > config.getMaxFileSize()) {
            throw new AdapterException.ValidationException(AdapterType.FILE, 
                    "File size exceeds maximum allowed: " + Files.size(file) + " > " + config.getMaxFileSize());
        }
    }
    
    private java.nio.charset.Charset getCharset() {
        return config.getFileEncoding() != null ? 
                java.nio.charset.Charset.forName(config.getFileEncoding()) : 
                java.nio.charset.StandardCharsets.UTF_8;
    }
    
    private String getLineEnding() {
        switch (config.getLineEnding().toLowerCase()) {
            case "unix":
                return "\n";
            case "windows":
                return "\r\n";
            case "system":
            default:
                return System.lineSeparator();
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getTargetDirectory() == null || config.getTargetDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, "Target directory is required");
        }
    }
    
    private void initializeDirectory() throws Exception {
        targetDirectory = Paths.get(config.getTargetDirectory());
        
        if (config.isCreateFileDirectory()) {
            Files.createDirectories(targetDirectory);
        }
        
        if (!Files.exists(targetDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Target directory does not exist: " + targetDirectory);
        }
        
        if (!Files.isDirectory(targetDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Target path is not a directory: " + targetDirectory);
        }
        
        if (!Files.isWritable(targetDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Target directory is not writable: " + targetDirectory);
        }
    }
    
    private void validateSupportDirectories() throws Exception {
        String[] dirs = {
            config.getBackupDirectory(),
            config.getTemporaryDirectory(),
            config.getErrorDirectory()
        };
        
        for (String dir : dirs) {
            if (dir != null && !dir.trim().isEmpty()) {
                Path path = Paths.get(dir);
                Files.createDirectories(path); // Create if doesn't exist
                
                if (!Files.isDirectory(path)) {
                    throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                            "Support path is not a directory: " + path);
                }
                
                if (!Files.isWritable(path)) {
                    throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                            "Support directory is not writable: " + path);
                }
            }
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // File receivers typically don't poll, they write files
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("File Receiver (Outbound): %s, Pattern: %s, Construction: %s, Batching: %s", 
                config.getTargetDirectory(),
                config.getTargetFileName() != null ? config.getTargetFileName() : 
                    (config.getFileNamePattern() != null ? config.getFileNamePattern() : "Generated"),
                config.getFileConstructionMode(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }
}