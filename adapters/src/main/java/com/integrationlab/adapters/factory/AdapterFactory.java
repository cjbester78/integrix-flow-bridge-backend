package com.integrationlab.adapters.factory;

import com.integrationlab.adapters.core.*;

/**
 * Factory interface for creating adapter instances.
 * Different factories can be implemented for different configuration sources.
 */
public interface AdapterFactory {
    
    /**
     * Create a sender adapter instance.
     * 
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured sender adapter instance
     * @throws AdapterException if adapter creation fails
     */
    SenderAdapter createSender(AdapterType adapterType, Object configuration) throws AdapterException;
    
    /**
     * Create a receiver adapter instance.
     * 
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured receiver adapter instance
     * @throws AdapterException if adapter creation fails
     */
    ReceiverAdapter createReceiver(AdapterType adapterType, Object configuration) throws AdapterException;
    
    /**
     * Check if the factory supports the given adapter type and mode.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @return true if supported, false otherwise
     */
    boolean supports(AdapterType adapterType, AdapterMode adapterMode);
    
    /**
     * Get the factory name/identifier.
     * 
     * @return factory name
     */
    String getFactoryName();
}