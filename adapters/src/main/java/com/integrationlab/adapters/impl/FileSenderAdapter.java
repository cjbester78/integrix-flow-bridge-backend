package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.FileSenderAdapterConfig;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * File Sender Adapter implementation for file system monitoring and processing (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports directory polling, file filtering, duplicate detection, and incremental processing.
 */
public class FileSenderAdapter extends AbstractSenderAdapter {
    
    private final FileSenderAdapterConfig config;
    private final Map<String, String> processedFiles = new ConcurrentHashMap<>();
    private Pattern filePattern;
    private Pattern exclusionPattern;
    private Path sourceDirectory;
    
    public FileSenderAdapter(FileSenderAdapterConfig config) {
        super(AdapterType.FILE);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing File sender adapter (inbound) with directory: {}", config.getSourceDirectory());
        
        validateConfiguration();
        initializeDirectory();
        initializePatterns();
        
        logger.info("File sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying File sender adapter");
        processedFiles.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Directory accessibility
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.FILE, () -> {
            try {
                if (!Files.exists(sourceDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Source directory does not exist: " + sourceDirectory, null);
                }
                
                if (!Files.isDirectory(sourceDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Path is not a directory: " + sourceDirectory, null);
                }
                
                if (!Files.isReadable(sourceDirectory)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Directory Access", "Directory is not readable: " + sourceDirectory, null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                        "Directory Access", "Source directory is accessible and readable");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                        "Directory Access", "Failed to access directory: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: File pattern validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FILE, () -> {
            try {
                long fileCount = Files.list(sourceDirectory)
                        .filter(Files::isRegularFile)
                        .filter(this::matchesFilePattern)
                        .count();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                        "Pattern Matching", "Found " + fileCount + " files matching configured patterns");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                        "Pattern Matching", "Failed to scan directory: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Processing directories validation
        if (config.getArchiveDirectory() != null || config.getMoveDirectory() != null || 
            config.getBackupDirectory() != null || config.getErrorDirectory() != null) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.FILE, () -> {
                try {
                    validateProcessingDirectories();
                    return ConnectionTestUtil.createTestSuccess(AdapterType.FILE, 
                            "Processing Directories", "All configured processing directories are accessible");
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.FILE, 
                            "Processing Directories", "Processing directory validation failed: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.FILE, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For File Sender (inbound), "send" means polling/retrieving files FROM directory
        return pollForFiles();
    }
    
    private AdapterResult pollForFiles() throws Exception {
        List<Map<String, Object>> processedFiles = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory, this::matchesFilePattern)) {
            List<Path> availableFiles = new ArrayList<>();
            stream.forEach(availableFiles::add);
            
            // Sort files based on configuration
            sortFiles(availableFiles);
            
            // Apply file limits
            int maxFiles = Math.min(availableFiles.size(), config.getMaxFilesPerPoll());
            
            for (int i = 0; i < maxFiles; i++) {
                Path file = availableFiles.get(i);
                
                try {
                    if (shouldProcessFile(file)) {
                        Map<String, Object> fileData = processFile(file);
                        if (fileData != null) {
                            processedFiles.add(fileData);
                            handlePostProcessing(file);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing file: {}", file, e);
                    handleFileError(file, e);
                    
                    if (!config.isContinueOnError()) {
                        throw new AdapterException.ProcessingException(AdapterType.FILE, 
                                "File processing failed: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        logger.info("File sender adapter polled {} files from directory", processedFiles.size());
        
        return AdapterResult.success(processedFiles, 
                String.format("Retrieved %d files from directory", processedFiles.size()));
    }
    
    private Map<String, Object> processFile(Path file) throws Exception {
        if (!Files.exists(file)) {
            return null; // File may have been processed by another instance
        }
        
        // Check file age
        if (config.getMinFileAge() > 0) {
            long fileAge = System.currentTimeMillis() - Files.getLastModifiedTime(file).toMillis();
            if (fileAge < config.getMinFileAge() * 1000L) {
                logger.debug("File {} is too young, skipping", file);
                return null;
            }
        }
        
        // Acquire file lock if configured
        if (config.isUseFileLocking()) {
            if (!acquireFileLock(file)) {
                logger.debug("Could not acquire lock for file {}, skipping", file);
                return null;
            }
        }
        
        try {
            // Handle empty files
            long fileSize = Files.size(file);
            if (fileSize == 0) {
                return handleEmptyFile(file);
            }
            
            // Size validation
            if (fileSize < config.getMinFileSize() || fileSize > config.getMaxFileSize()) {
                logger.debug("File {} size {} is outside configured range, skipping", file, fileSize);
                return null;
            }
            
            // Read file content
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("fileName", file.getFileName().toString());
            fileData.put("filePath", file.toAbsolutePath().toString());
            fileData.put("fileSize", fileSize);
            fileData.put("lastModified", Files.getLastModifiedTime(file).toInstant());
            
            // Read content based on configuration
            if (config.isLogFileContent()) {
                String content = Files.readString(file, 
                        config.getFileEncoding() != null ? 
                                java.nio.charset.Charset.forName(config.getFileEncoding()) : 
                                java.nio.charset.StandardCharsets.UTF_8);
                fileData.put("content", content);
            } else {
                // For large files, just read as byte array or provide stream
                byte[] content = Files.readAllBytes(file);
                fileData.put("content", content);
            }
            
            // Generate checksum if configured
            if (config.isValidateFileIntegrity()) {
                String checksum = generateChecksum(file);
                fileData.put("checksum", checksum);
                
                // Check for duplicates
                if (config.isEnableDuplicateHandling()) {
                    if (isDuplicate(file, checksum)) {
                        handleDuplicateFile(file);
                        return null;
                    }
                }
            }
            
            // Mark as processed
            this.processedFiles.put(file.toString(), 
                    config.isValidateFileIntegrity() ? (String) fileData.get("checksum") : 
                            String.valueOf(System.currentTimeMillis()));
            
            return fileData;
            
        } finally {
            if (config.isUseFileLocking()) {
                releaseFileLock(file);
            }
        }
    }
    
    private void handlePostProcessing(Path file) throws Exception {
        String processingMode = config.getProcessingMode();
        
        switch (processingMode.toLowerCase()) {
            case "delete":
                Files.deleteIfExists(file);
                logger.debug("Deleted processed file: {}", file);
                break;
                
            case "archive":
                if (config.getArchiveDirectory() != null) {
                    Path archiveDir = Paths.get(config.getArchiveDirectory());
                    Files.createDirectories(archiveDir);
                    Path targetPath = archiveDir.resolve(file.getFileName());
                    Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Archived file to: {}", targetPath);
                }
                break;
                
            case "move":
                if (config.getMoveDirectory() != null) {
                    Path moveDir = Paths.get(config.getMoveDirectory());
                    Files.createDirectories(moveDir);
                    Path targetPath = moveDir.resolve(file.getFileName());
                    Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Moved file to: {}", targetPath);
                }
                break;
                
            case "copy":
                if (config.getBackupDirectory() != null) {
                    Path backupDir = Paths.get(config.getBackupDirectory());
                    Files.createDirectories(backupDir);
                    Path targetPath = backupDir.resolve(file.getFileName());
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Copied file to: {}", targetPath);
                }
                break;
                
            default:
                logger.debug("No post-processing configured for file: {}", file);
        }
    }
    
    private boolean shouldProcessFile(Path file) throws Exception {
        // Basic file checks
        if (!Files.isRegularFile(file)) {
            return false;
        }
        
        // Check if already processed (incremental processing)
        if (processedFiles.containsKey(file.toString())) {
            return false;
        }
        
        // Check exclusion patterns
        if (exclusionPattern != null && exclusionPattern.matcher(file.getFileName().toString()).matches()) {
            return false;
        }
        
        return true;
    }
    
    private boolean matchesFilePattern(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        
        String fileName = file.getFileName().toString();
        
        // Simple file name match
        if (config.getFileName() != null && !config.getFileName().isEmpty()) {
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
        
        // Regex pattern match
        if (filePattern != null) {
            return filePattern.matcher(fileName).matches();
        }
        
        return true; // If no pattern specified, match all files
    }
    
    private void sortFiles(List<Path> files) throws Exception {
        String sorting = config.getSorting();
        if (sorting == null || "none".equals(sorting)) {
            return;
        }
        
        switch (sorting.toLowerCase()) {
            case "name":
                files.sort(Comparator.comparing(path -> path.getFileName().toString()));
                break;
            case "date":
                files.sort(Comparator.comparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        return null;
                    }
                }));
                break;
            case "size":
                files.sort(Comparator.comparing(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0L;
                    }
                }));
                break;
        }
    }
    
    private Map<String, Object> handleEmptyFile(Path file) throws Exception {
        String handling = config.getEmptyFileHandling();
        
        switch (handling.toLowerCase()) {
            case "ignore":
                logger.debug("Ignoring empty file: {}", file);
                return null;
            case "error":
                throw new AdapterException.ValidationException(AdapterType.FILE, 
                        "Empty file not allowed: " + file);
            case "process":
            default:
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("fileName", file.getFileName().toString());
                fileData.put("filePath", file.toAbsolutePath().toString());
                fileData.put("fileSize", 0L);
                fileData.put("lastModified", Files.getLastModifiedTime(file).toInstant());
                fileData.put("content", "");
                return fileData;
        }
    }
    
    private String generateChecksum(Path file) throws Exception {
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
    
    private boolean isDuplicate(Path file, String checksum) {
        String strategy = config.getDuplicateDetectionStrategy();
        
        switch (strategy.toLowerCase()) {
            case "name":
                return processedFiles.containsKey(file.getFileName().toString());
            case "checksum":
                return processedFiles.containsValue(checksum);
            case "content":
            default:
                return processedFiles.containsValue(checksum);
        }
    }
    
    private void handleDuplicateFile(Path file) throws Exception {
        if (config.getDuplicateDirectory() != null) {
            Path duplicateDir = Paths.get(config.getDuplicateDirectory());
            Files.createDirectories(duplicateDir);
            Path targetPath = duplicateDir.resolve(file.getFileName());
            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Moved duplicate file to: {}", targetPath);
        }
    }
    
    private void handleFileError(Path file, Exception error) throws Exception {
        if (config.getErrorDirectory() != null) {
            try {
                Path errorDir = Paths.get(config.getErrorDirectory());
                Files.createDirectories(errorDir);
                Path targetPath = errorDir.resolve(file.getFileName());
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Moved error file to: {}", targetPath);
            } catch (Exception e) {
                logger.warn("Failed to move error file: {}", file, e);
            }
        }
    }
    
    private boolean acquireFileLock(Path file) {
        // Simple file locking by creating .lock file
        try {
            Path lockFile = file.resolveSibling(file.getFileName() + config.getLockFileExtension());
            if (Files.exists(lockFile)) {
                // Check if lock is stale
                long lockAge = System.currentTimeMillis() - Files.getLastModifiedTime(lockFile).toMillis();
                if (lockAge < config.getFileLockTimeout()) {
                    return false; // Active lock
                }
                // Remove stale lock
                Files.deleteIfExists(lockFile);
            }
            
            // Create lock file
            Files.createFile(lockFile);
            return true;
            
        } catch (Exception e) {
            logger.warn("Failed to acquire file lock for: {}", file, e);
            return false;
        }
    }
    
    private void releaseFileLock(Path file) {
        try {
            Path lockFile = file.resolveSibling(file.getFileName() + config.getLockFileExtension());
            Files.deleteIfExists(lockFile);
        } catch (Exception e) {
            logger.warn("Failed to release file lock for: {}", file, e);
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getSourceDirectory() == null || config.getSourceDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, "Source directory is required");
        }
    }
    
    private void initializeDirectory() throws Exception {
        sourceDirectory = Paths.get(config.getSourceDirectory());
        
        if (!Files.exists(sourceDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Source directory does not exist: " + sourceDirectory);
        }
        
        if (!Files.isDirectory(sourceDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Source path is not a directory: " + sourceDirectory);
        }
        
        if (!Files.isReadable(sourceDirectory)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "Source directory is not readable: " + sourceDirectory);
        }
    }
    
    private void initializePatterns() throws Exception {
        // Initialize file pattern
        if (config.getFilePattern() != null && !config.getFilePattern().trim().isEmpty()) {
            try {
                filePattern = Pattern.compile(config.getFilePattern());
            } catch (Exception e) {
                throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                        "Invalid file pattern: " + config.getFilePattern(), e);
            }
        }
        
        // Initialize exclusion pattern
        if (config.getExclusionMask() != null && !config.getExclusionMask().trim().isEmpty()) {
            try {
                exclusionPattern = Pattern.compile(config.getExclusionMask());
            } catch (Exception e) {
                throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                        "Invalid exclusion pattern: " + config.getExclusionMask(), e);
            }
        }
    }
    
    private void validateProcessingDirectories() throws Exception {
        String[] dirs = {
            config.getArchiveDirectory(),
            config.getMoveDirectory(), 
            config.getBackupDirectory(),
            config.getErrorDirectory(),
            config.getDuplicateDirectory()
        };
        
        for (String dir : dirs) {
            if (dir != null && !dir.trim().isEmpty()) {
                Path path = Paths.get(dir);
                Files.createDirectories(path); // Create if doesn't exist
                
                if (!Files.isDirectory(path)) {
                    throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                            "Processing path is not a directory: " + path);
                }
                
                if (!Files.isWritable(path)) {
                    throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                            "Processing directory is not writable: " + path);
                }
            }
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("File Sender (Inbound): %s, Pattern: %s, Polling: %dms, Processing: %s", 
                config.getSourceDirectory(),
                config.getFileName() != null ? config.getFileName() : 
                    (config.getFilePattern() != null ? config.getFilePattern() : "All files"),
                config.getPollingInterval() != null ? config.getPollingInterval() : 0,
                config.getProcessingMode());
    }
}