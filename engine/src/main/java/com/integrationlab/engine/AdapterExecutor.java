package com.integrationlab.engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

/**
 * AdapterExecutor - Interface for executing adapter operations
 */
public interface AdapterExecutor {
    
    /**
     * Fetch data from an adapter as a String
     */
    String fetchData(String adapterId);
    
    /**
     * Fetch data from an adapter as an Object (can be String, byte[], InputStream, etc.)
     */
    Object fetchDataAsObject(String adapterId);
    
    /**
     * Send string data to an adapter
     */
    void sendData(String adapterId, String payload);
    
    /**
     * Send string data to an adapter with context
     */
    void sendData(String adapterId, String payload, Map<String, Object> context);
    
    /**
     * Send byte array data to an adapter
     */
    void sendData(String adapterId, byte[] data);
    
    /**
     * Send object data to an adapter
     */
    void sendData(String adapterId, Object data);
    
    /**
     * Get a writable channel for streaming data to an adapter
     */
    WritableByteChannel getWritableChannel(String adapterId, Map<String, Object> config);
    
    /**
     * Get an output stream for streaming data to an adapter
     */
    OutputStream getOutputStream(String adapterId, Map<String, Object> config);
}