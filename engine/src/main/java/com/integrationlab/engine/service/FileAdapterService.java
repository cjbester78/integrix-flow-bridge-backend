package com.integrationlab.engine.service;

import com.integrationlab.adapters.core.AdapterResult;
import com.integrationlab.adapters.core.AdapterException;
import com.integrationlab.adapters.config.FileSenderAdapterConfig;
import com.integrationlab.adapters.config.FileReceiverAdapterConfig;

/**
 * FileAdapterService handles file-based adapter operations using the new separated architecture.
 * Follows middleware convention: Sender = receives FROM files (inbound), Receiver = sends TO files (outbound)
 */
public interface FileAdapterService {
    
    /**
     * Read files from directory using sender adapter (inbound operation)
     */
    AdapterResult readFiles(FileSenderAdapterConfig config) throws AdapterException;
    
    /**
     * Write data to file using receiver adapter (outbound operation) 
     */
    AdapterResult writeFile(FileReceiverAdapterConfig config, Object data) throws AdapterException;
    
    /**
     * Start polling for files with callback
     */
    void startFilePolling(FileSenderAdapterConfig config, FilePollingCallback callback) throws AdapterException;
    
    /**
     * Stop file polling
     */
    void stopFilePolling(FileSenderAdapterConfig config) throws AdapterException;
    
    /**
     * Test file adapter configuration
     */
    boolean testFileAdapter(Object config) throws AdapterException;
    
    /**
     * Callback interface for file polling operations
     */
    interface FilePollingCallback {
        void onFilesFound(AdapterResult result);
        void onError(AdapterException error);
    }
}
