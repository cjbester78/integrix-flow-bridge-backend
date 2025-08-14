package com.integrixs.adapters.core;

/**
 * Callback interface for asynchronous adapter operations.
 */
public interface AdapterCallback {
    
    /**
     * Called when an adapter operation completes successfully.
     * 
     * @param result the successful result
     */
    void onSuccess(AdapterResult result);
    
    /**
     * Called when an adapter operation fails.
     * 
     * @param result the failure result containing error information
     */
    void onFailure(AdapterResult result);
    
    /**
     * Called for progress updates during long-running operations.
     * 
     * @param progress progress information (0.0 to 1.0)
     * @param message progress message
     */
    default void onProgress(double progress, String message) {
        // Default implementation does nothing
    }
    
    /**
     * Called when an operation times out.
     * 
     * @param message timeout message
     */
    default void onTimeout(String message) {
        onFailure(AdapterResult.timeout(message));
    }
}