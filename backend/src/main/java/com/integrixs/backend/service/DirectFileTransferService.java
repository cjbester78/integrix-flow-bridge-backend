package com.integrixs.backend.service;

import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.engine.AdapterExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Service for direct file transfers without XML conversion.
 * Provides high-performance streaming for large files and preserves original format.
 */
@Service
public class DirectFileTransferService {
    
    private static final Logger logger = LoggerFactory.getLogger(DirectFileTransferService.class);
    private static final int BUFFER_SIZE = 8192; // 8KB buffer for streaming
    private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024; // 10MB
    
    private final AdapterExecutor adapterExecutor;
    private final LogService logService;
    
    public DirectFileTransferService(AdapterExecutor adapterExecutor, LogService logService) {
        this.adapterExecutor = adapterExecutor;
        this.logService = logService;
    }
    
    /**
     * Execute a direct file transfer without any conversion
     */
    public void executeDirectTransfer(IntegrationFlow flow, CommunicationAdapter sourceAdapter, 
                                     CommunicationAdapter targetAdapter) throws Exception {
        logger.info("Starting direct file transfer for flow: {}", flow.getName());
        
        try {
            // Determine if we should use streaming based on adapter types
            if (shouldUseStreaming(sourceAdapter, targetAdapter)) {
                executeStreamingTransfer(flow, sourceAdapter, targetAdapter);
            } else {
                executeBufferedTransfer(flow, sourceAdapter, targetAdapter);
            }
            
            logService.logFlowExecutionSuccess(flow, "Direct transfer", "Completed");
            logger.info("Direct file transfer completed successfully for flow: {}", flow.getName());
            
        } catch (Exception e) {
            logger.error("Direct file transfer failed for flow: {}", flow.getName(), e);
            logService.logFlowExecutionError(flow, e);
            throw e;
        }
    }
    
    /**
     * Determine if streaming should be used based on adapter types
     */
    private boolean shouldUseStreaming(CommunicationAdapter source, CommunicationAdapter target) {
        // Use streaming for file-based adapters
        String sourceType = source.getType().name();
        String targetType = target.getType().name();
        
        return (sourceType.equals("FILE") || sourceType.equals("FTP") || sourceType.equals("SFTP")) &&
               (targetType.equals("FILE") || targetType.equals("FTP") || targetType.equals("SFTP"));
    }
    
    /**
     * Execute streaming transfer for large files using NIO
     */
    private void executeStreamingTransfer(IntegrationFlow flow, CommunicationAdapter sourceAdapter,
                                        CommunicationAdapter targetAdapter) throws Exception {
        logger.info("Using streaming transfer for flow: {}", flow.getName());
        
        // Get source file info from adapter
        Object sourceDataObj = adapterExecutor.fetchDataAsObject(sourceAdapter.getId());
        Map<String, Object> sourceData = null;
        
        // Try to cast to Map if possible
        if (sourceDataObj instanceof Map) {
            sourceData = (Map<String, Object>) sourceDataObj;
        }
        
        if (sourceData != null && sourceData.containsKey("filePath")) {
            Path sourcePath = Path.of((String) sourceData.get("filePath"));
            long fileSize = Files.size(sourcePath);
            
            logger.info("Streaming file: {} (size: {} bytes)", sourcePath, fileSize);
            
            // For very large files, use memory-mapped I/O
            if (fileSize > LARGE_FILE_THRESHOLD) {
                streamLargeFile(sourcePath, targetAdapter);
            } else {
                streamSmallFile(sourcePath, targetAdapter);
            }
        } else if (sourceData != null && sourceData.containsKey("inputStream")) {
            // Handle stream-based sources
            streamFromInputStream((InputStream) sourceData.get("inputStream"), targetAdapter);
        } else {
            // Fallback to buffered transfer
            executeBufferedTransfer(flow, sourceAdapter, targetAdapter);
        }
    }
    
    /**
     * Stream large files using NIO channels
     */
    private void streamLargeFile(Path sourcePath, CommunicationAdapter targetAdapter) throws Exception {
        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ)) {
            
            // Prepare target for streaming
            Map<String, Object> targetConfig = Map.of(
                "streamingMode", true,
                "expectedSize", sourceChannel.size()
            );
            
            WritableByteChannel targetChannel = adapterExecutor.getWritableChannel(targetAdapter.getId(), targetConfig);
            
            // Transfer data using zero-copy when possible
            long position = 0;
            long size = sourceChannel.size();
            while (position < size) {
                long transferred = sourceChannel.transferTo(position, size - position, targetChannel);
                position += transferred;
                
                // Log progress for very large files
                if (size > 100 * 1024 * 1024) { // 100MB
                    int progress = (int) ((position * 100) / size);
                    logger.debug("Transfer progress: {}%", progress);
                }
            }
            
            targetChannel.close();
        }
    }
    
    /**
     * Stream smaller files using buffered approach
     */
    private void streamSmallFile(Path sourcePath, CommunicationAdapter targetAdapter) throws Exception {
        byte[] content = Files.readAllBytes(sourcePath);
        adapterExecutor.sendData(targetAdapter.getId(), content);
    }
    
    /**
     * Stream from an InputStream source
     */
    private void streamFromInputStream(InputStream source, CommunicationAdapter targetAdapter) throws Exception {
        try (InputStream input = source) {
            Map<String, Object> targetConfig = Map.of("streamingMode", true);
            
            try (OutputStream output = adapterExecutor.getOutputStream(targetAdapter.getId(), targetConfig)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    if (totalBytes % (1024 * 1024) == 0) { // Log every MB
                        logger.debug("Streamed {} MB", totalBytes / (1024 * 1024));
                    }
                }
                
                output.flush();
                logger.info("Streamed total of {} bytes", totalBytes);
            }
        }
    }
    
    /**
     * Execute buffered transfer for non-file adapters
     */
    private void executeBufferedTransfer(IntegrationFlow flow, CommunicationAdapter sourceAdapter,
                                       CommunicationAdapter targetAdapter) throws Exception {
        logger.info("Using buffered transfer for flow: {}", flow.getName());
        
        // Fetch data as-is without conversion
        Object rawData = adapterExecutor.fetchData(sourceAdapter.getId());
        
        // Detect and preserve encoding
        String encoding = detectEncoding(rawData);
        logger.debug("Detected encoding: {}", encoding);
        
        // Send data preserving original format
        if (rawData instanceof byte[]) {
            adapterExecutor.sendData(targetAdapter.getId(), (Object) rawData);
        } else if (rawData instanceof InputStream) {
            try (InputStream is = (InputStream) rawData) {
                byte[] data = is.readAllBytes();
                adapterExecutor.sendData(targetAdapter.getId(), data);
            }
        } else if (rawData instanceof String) {
            // Preserve string encoding
            byte[] data = ((String) rawData).getBytes(Charset.forName(encoding));
            adapterExecutor.sendData(targetAdapter.getId(), data);
        } else {
            // For other types, send as object
            adapterExecutor.sendData(targetAdapter.getId(), rawData);
        }
    }
    
    /**
     * Detect character encoding of the data
     */
    private String detectEncoding(Object data) {
        // Simple encoding detection - can be enhanced with libraries like juniversalchardet
        if (data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            if (bytes.length >= 3) {
                // Check for BOM
                if (bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    return "UTF-8";
                }
                if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
                    return "UTF-16LE";
                }
                if (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
                    return "UTF-16BE";
                }
            }
        }
        
        // Default to UTF-8
        return "UTF-8";
    }
    
    /**
     * Check if a file is binary (should not be converted to XML)
     */
    public boolean isBinaryFile(Object data) {
        if (data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            // Check first 512 bytes for binary content
            int checkLength = Math.min(512, bytes.length);
            int nonTextChars = 0;
            
            for (int i = 0; i < checkLength; i++) {
                byte b = bytes[i];
                // Count non-printable characters (excluding common whitespace)
                if (b < 0x20 && b != 0x09 && b != 0x0A && b != 0x0D) {
                    nonTextChars++;
                }
            }
            
            // If more than 30% non-text characters, consider it binary
            return (nonTextChars * 100.0 / checkLength) > 30;
        }
        
        return false;
    }
    
    /**
     * Detect file type by content signature (magic numbers)
     */
    public String detectFileType(byte[] data) {
        if (data == null || data.length < 4) {
            return "unknown";
        }
        
        // Check common file signatures
        if (data[0] == (byte) 0x25 && data[1] == (byte) 0x50 && 
            data[2] == (byte) 0x44 && data[3] == (byte) 0x46) {
            return "PDF";
        }
        
        if (data[0] == (byte) 0x50 && data[1] == (byte) 0x4B && 
            (data[2] == (byte) 0x03 || data[2] == (byte) 0x05 || data[2] == (byte) 0x07)) {
            return "ZIP";
        }
        
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "JPEG";
        }
        
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && 
            data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return "PNG";
        }
        
        if (data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && 
            data[2] == (byte) 0x46 && data[3] == (byte) 0x38) {
            return "GIF";
        }
        
        // Check for text-based formats
        String sample = new String(data, 0, Math.min(100, data.length), Charset.forName("UTF-8"));
        if (sample.trim().startsWith("<?xml")) {
            return "XML";
        }
        if (sample.trim().startsWith("{") || sample.trim().startsWith("[")) {
            return "JSON";
        }
        
        return "unknown";
    }
}