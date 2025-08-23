package com.integrixs.adapters.core;

/**
 * Base interface for all adapters.
 * Defines core operations that every adapter must support.
 */
public interface BaseAdapter {
    
    /**
     * Get the adapter type (HTTP, JDBC, etc.)
     */
    AdapterType getAdapterType();
    
    /**
     * Get the adapter mode (SENDER or RECEIVER)
     */
    AdapterMode getAdapterMode();
    
    /**
     * Test the adapter connection and configuration.
     * This method should validate connectivity without sending actual data.
     * 
     * @return AdapterResult containing test results
     */
    AdapterResult testConnection();
    
    /**
     * Initialize the adapter with its configuration.
     * This method should prepare the adapter for operation.
     * 
     * @throws AdapterException if initialization fails
     */
    void initialize() throws AdapterException;
    
    /**
     * Cleanup and release resources when adapter is no longer needed.
     * 
     * @throws AdapterException if cleanup fails
     */
    void destroy() throws AdapterException;
    
    /**
     * Check if the adapter is currently active and ready for operation.
     * 
     * @return true if adapter is active, false otherwise
     */
    boolean isActive();
    
    /**
     * Get adapter configuration summary for monitoring/debugging.
     * Should not expose sensitive information like passwords.
     * 
     * @return configuration summary
     */
    String getConfigurationSummary();
}