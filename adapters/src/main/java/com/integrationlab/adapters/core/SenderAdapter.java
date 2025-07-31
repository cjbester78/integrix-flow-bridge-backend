package com.integrationlab.adapters.core;

/**
 * Interface for sender adapters.
 * 
 * IMPORTANT: This project uses REVERSED middleware terminology:
 * - Sender Adapter = Receives data FROM external systems (inbound/receiver in traditional terms)
 * - Receiver Adapter = Sends data TO external systems (outbound/sender in traditional terms)
 * 
 * However, the current interface methods need to be reviewed for consistency with this convention.
 */
public interface SenderAdapter extends BaseAdapter {
    
    /**
     * Send data to the configured external system.
     * 
     * @param payload the data to send
     * @return AdapterResult containing the result of the send operation
     * @throws AdapterException if the send operation fails
     */
    AdapterResult send(Object payload) throws AdapterException;
    
    /**
     * Send data with custom headers or metadata.
     * 
     * @param payload the data to send
     * @param headers custom headers or metadata
     * @return AdapterResult containing the result of the send operation
     * @throws AdapterException if the send operation fails
     */
    AdapterResult send(Object payload, java.util.Map<String, Object> headers) throws AdapterException;
    
    /**
     * Send data asynchronously to the configured external system.
     * 
     * @param payload the data to send
     * @param callback callback to handle the result
     * @throws AdapterException if the async send setup fails
     */
    void sendAsync(Object payload, AdapterCallback callback) throws AdapterException;
    
    /**
     * Send a batch of data items.
     * 
     * @param payloads collection of data items to send
     * @return AdapterResult containing the batch operation results
     * @throws AdapterException if the batch send operation fails
     */
    AdapterResult sendBatch(java.util.Collection<Object> payloads) throws AdapterException;
    
    @Override
    default AdapterMode getAdapterMode() {
        return AdapterMode.SENDER;
    }
}