package com.integrationlab.adapters.core;

import java.util.Collection;

/**
 * Interface for receiver adapters that pull data from external systems.
 */
public interface ReceiverAdapter extends BaseAdapter {
    
    /**
     * Receive data from the configured external system.
     * This is typically a one-time pull operation.
     * 
     * @return AdapterResult containing the received data
     * @throws AdapterException if the receive operation fails
     */
    AdapterResult receive() throws AdapterException;
    
    /**
     * Receive data with filtering criteria.
     * 
     * @param criteria filtering or query criteria
     * @return AdapterResult containing the received data
     * @throws AdapterException if the receive operation fails
     */
    AdapterResult receive(Object criteria) throws AdapterException;
    
    /**
     * Start polling for data from the external system.
     * This sets up continuous polling based on adapter configuration.
     * 
     * @param callback callback to handle received data
     * @throws AdapterException if polling setup fails
     */
    void startPolling(AdapterCallback callback) throws AdapterException;
    
    /**
     * Stop the polling operation.
     * 
     * @throws AdapterException if stopping polling fails
     */
    void stopPolling() throws AdapterException;
    
    /**
     * Check if the adapter is currently polling.
     * 
     * @return true if actively polling, false otherwise
     */
    boolean isPolling();
    
    /**
     * Receive a batch of data items with specified limit.
     * 
     * @param maxItems maximum number of items to receive
     * @return AdapterResult containing the batch of received data
     * @throws AdapterException if the batch receive operation fails
     */
    AdapterResult receiveBatch(int maxItems) throws AdapterException;
    
    /**
     * Acknowledge processing of received data.
     * Some systems require explicit acknowledgment.
     * 
     * @param messageId identifier of the processed message
     * @throws AdapterException if acknowledgment fails
     */
    void acknowledge(String messageId) throws AdapterException;
    
    @Override
    default AdapterMode getAdapterMode() {
        return AdapterMode.RECEIVER;
    }
}