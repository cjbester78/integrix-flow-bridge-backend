package com.integrixs.engine.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.engine.AdapterExecutor;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.repository.CommunicationAdapterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.HashMap;

@Component
/**
 * AdapterExecutorImpl routes adapter executions using the new factory pattern.
 * Uses AdapterFactoryManager to create appropriate sender/receiver adapters dynamically.
 */
public class AdapterExecutorImpl implements AdapterExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AdapterExecutorImpl.class);
    
    private final AdapterFactoryManager adapterFactory;
    
    public AdapterExecutorImpl() {
        this.adapterFactory = AdapterFactoryManager.getInstance();
    }

    @Autowired private CommunicationAdapterRepository adapterRepository;


    @Override
    public String fetchData(String adapterId) {
        CommunicationAdapter adapter = getAdapter(adapterId);
        
        try {
            // For fetching data, we use sender adapters (which receive FROM external systems in middleware terminology)
            com.integrixs.adapters.core.AdapterType adapterType = mapToAdapterType(adapter.getType());
            
            // Get configuration from the adapter - this would need to be properly mapped
            Object configuration = buildAdapterConfiguration(adapter);
            
            SenderAdapter senderAdapter = adapterFactory.createSender(adapterType, configuration);
            senderAdapter.initialize();
            
            try {
                AdapterResult result = senderAdapter.send(null, null); // Fetching doesn't need payload
                
                if (result.isSuccess()) {
                    return result.getData() != null ? result.getData().toString() : "";
                } else {
                    logger.error("Failed to fetch data from adapter {}: {}", adapterId, result.getMessage());
                    throw new RuntimeException("Fetch failed: " + result.getMessage());
                }
                
            } finally {
                senderAdapter.destroy();
            }
            
        } catch (AdapterException e) {
            logger.error("Error fetching data from adapter {}", adapterId, e);
            throw new RuntimeException("Adapter error: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendData(String adapterId, String payload) {
        sendData(adapterId, payload, new HashMap<>());
    }
    
    @Override
    public void sendData(String adapterId, String payload, Map<String, Object> context) {
        CommunicationAdapter adapter = getAdapter(adapterId);
        
        try {
            // For sending data, we use receiver adapters (which send TO external systems in middleware terminology)
            com.integrixs.adapters.core.AdapterType adapterType = mapToAdapterType(adapter.getType());
            
            // Get configuration from the adapter - this would need to be properly mapped
            Object configuration = buildAdapterConfiguration(adapter);
            
            ReceiverAdapter receiverAdapter = adapterFactory.createReceiver(adapterType, configuration);
            receiverAdapter.initialize();
            
            try {
                AdapterResult result = receiverAdapter.receive(payload); // In middleware terminology, receiver "receives" the data to send out
                
                if (!result.isSuccess()) {
                    logger.error("Failed to send data via adapter {}: {}", adapterId, result.getMessage());
                    throw new RuntimeException("Send failed: " + result.getMessage());
                }
                
            } finally {
                receiverAdapter.destroy();
            }
            
        } catch (AdapterException e) {
            logger.error("Error sending data via adapter {}", adapterId, e);
            throw new RuntimeException("Adapter error: " + e.getMessage(), e);
        }
    }

    private CommunicationAdapter getAdapter(String adapterId) {
        return adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));
    }
    
    
    private Object buildAdapterConfiguration(CommunicationAdapter adapter) {
        // This is a placeholder - in a real implementation, you'd map the CommunicationAdapter
        // properties to the appropriate configuration class (HttpAdapterConfig, JdbcAdapterConfig, etc.)
        // For now, we'll return a basic configuration or delegate to the service classes
        
        logger.warn("buildAdapterConfiguration not fully implemented - using service fallback for adapter type: {}", adapter.getType());
        
        // For now, fall back to the existing service pattern until configurations are properly mapped
        return switch (adapter.getType()) {
            case FILE -> new Object(); // Placeholder - would create FileAdapterConfig
            case FTP -> new Object(); // Placeholder - would create FtpAdapterConfig  
            case SFTP -> new Object(); // Placeholder - would create SftpAdapterConfig
            case HTTP -> new Object(); // Placeholder - would create HttpAdapterConfig
            case REST -> new Object(); // Placeholder - would create RestAdapterConfig
            case SOAP -> new Object(); // Placeholder - would create SoapAdapterConfig
            case JMS -> new Object(); // Placeholder - would create JmsSenderAdapterConfig or JmsReceiverAdapterConfig
            case ODATA -> new Object(); // Placeholder - would create OdataSenderAdapterConfig or OdataReceiverAdapterConfig
            case IDOC -> new Object(); // Placeholder - would create IdocSenderAdapterConfig or IdocReceiverAdapterConfig
            case RFC -> new Object(); // Placeholder - would create RfcSenderAdapterConfig or RfcReceiverAdapterConfig
            case JDBC -> new Object(); // Placeholder - would create JdbcAdapterConfig
            case MAIL -> new Object(); // Placeholder - would create MailAdapterConfig
            default -> throw new IllegalArgumentException("Unsupported adapter type: " + adapter.getType());
        };
    }
    
    private com.integrixs.adapters.core.AdapterType mapToAdapterType(com.integrixs.shared.enums.AdapterType sharedType) {
        return switch (sharedType) {
            case FILE -> com.integrixs.adapters.core.AdapterType.FILE;
            case FTP -> com.integrixs.adapters.core.AdapterType.FTP;
            case SFTP -> com.integrixs.adapters.core.AdapterType.SFTP;
            case HTTP -> com.integrixs.adapters.core.AdapterType.HTTP;
            case REST -> com.integrixs.adapters.core.AdapterType.REST;
            case SOAP -> com.integrixs.adapters.core.AdapterType.SOAP;
            case JMS -> com.integrixs.adapters.core.AdapterType.JMS;
            case ODATA -> com.integrixs.adapters.core.AdapterType.ODATA;
            case IDOC -> com.integrixs.adapters.core.AdapterType.IDOC;
            case RFC -> com.integrixs.adapters.core.AdapterType.RFC;
            case JDBC -> com.integrixs.adapters.core.AdapterType.JDBC;
            case MAIL -> com.integrixs.adapters.core.AdapterType.MAIL;
        };
    }

    @Override
    public Object fetchDataAsObject(String adapterId) {
        // For now, return the string data wrapped in object
        // In a real implementation, this would return the raw data (byte[], InputStream, etc.)
        return fetchData(adapterId);
    }

    @Override
    public void sendData(String adapterId, byte[] data) {
        // Convert byte array to string for now
        sendData(adapterId, new String(data));
    }

    @Override
    public void sendData(String adapterId, Object data) {
        // Convert object to string for now
        if (data instanceof byte[]) {
            sendData(adapterId, (byte[]) data);
        } else if (data instanceof String) {
            sendData(adapterId, (String) data);
        } else {
            sendData(adapterId, data.toString());
        }
    }

    @Override
    public WritableByteChannel getWritableChannel(String adapterId, Map<String, Object> config) {
        // This would need to be implemented based on the adapter type
        // For now, throw unsupported operation
        throw new UnsupportedOperationException("WritableByteChannel not yet implemented for adapter: " + adapterId);
    }

    @Override
    public OutputStream getOutputStream(String adapterId, Map<String, Object> config) {
        // This would need to be implemented based on the adapter type
        // For now, return a ByteArrayOutputStream that sends data on close
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                sendData(adapterId, this.toByteArray());
            }
        };
    }
}
