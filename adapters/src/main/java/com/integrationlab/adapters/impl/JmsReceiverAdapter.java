package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.JmsReceiverAdapterConfig;

import jakarta.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.*;

/**
 * JMS Receiver Adapter implementation for JMS message publishing (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Sends messages to JMS queues/topics in external systems.
 */
public class JmsReceiverAdapter extends AbstractReceiverAdapter {
    
    private final JmsReceiverAdapterConfig config;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Destination destination;
    private ConnectionFactory connectionFactory;
    
    public JmsReceiverAdapter(JmsReceiverAdapterConfig config) {
        super(AdapterType.JMS);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing JMS receiver adapter (outbound) for destination: {}", config.getDestinationName());
        
        validateConfiguration();
        initializeJmsConnection();
        
        logger.info("JMS receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying JMS receiver adapter");
        
        try {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.stop();
                connection.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing JMS resources", e);
        }
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: JMS connection factory
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.JMS, () -> {
            try {
                if (connectionFactory == null) {
                    initializeConnectionFactory();
                }
                
                // Test creating a connection
                Connection testConnection = connectionFactory.createConnection(
                        config.getUsername(), config.getPassword());
                testConnection.close();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.JMS, 
                        "JMS Connection", "Successfully connected to JMS provider");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JMS, 
                        "JMS Connection", "Failed to connect to JMS provider: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Destination validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JMS, () -> {
            try {
                String destinationType = config.getDestinationType();
                String destinationName = config.getDestinationName();
                
                if (!"queue".equalsIgnoreCase(destinationType) && !"topic".equalsIgnoreCase(destinationType)) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.JMS, 
                            "Destination Type", "Invalid destination type: " + destinationType, null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.JMS, 
                        "Destination Config", String.format("Configured for %s: %s", destinationType, destinationName));
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JMS, 
                        "Destination Config", "Failed to validate destination: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Producer configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.JMS, () -> {
            try {
                String deliveryMode = config.isPersistent() ? "PERSISTENT" : "NON_PERSISTENT";
                String info = String.format("Delivery: %s, Priority: %d, TTL: %dms", 
                        deliveryMode, config.getPriority(), config.getTimeToLive());
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.JMS, 
                        "Producer Config", info);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.JMS, 
                        "Producer Config", "Invalid producer configuration: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.JMS, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For JMS Receiver (outbound), this method sends data TO JMS
        return sendJmsMessage(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        throw new AdapterException.OperationException(AdapterType.JMS, 
                "JMS Receiver requires message payload for publishing");
    }
    
    private AdapterResult sendJmsMessage(Object payload) throws Exception {
        try {
            Message message;
            
            // Create appropriate JMS message based on payload type
            if (payload instanceof String) {
                message = session.createTextMessage((String) payload);
            } else if (payload instanceof byte[]) {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes((byte[]) payload);
                message = bytesMessage;
            } else if (payload instanceof Map) {
                MapMessage mapMessage = session.createMapMessage();
                Map<String, Object> map = (Map<String, Object>) payload;
                
                // Check for special fields
                Object body = map.get("body");
                Map<String, Object> properties = (Map<String, Object>) map.get("properties");
                Map<String, Object> headers = (Map<String, Object>) map.get("headers");
                
                if (body != null) {
                    // Body is provided separately
                    if (body instanceof String) {
                        message = session.createTextMessage((String) body);
                    } else if (body instanceof Map) {
                        // Create map message from body
                        Map<String, Object> bodyMap = (Map<String, Object>) body;
                        for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                            mapMessage.setObject(entry.getKey(), entry.getValue());
                        }
                        message = mapMessage;
                    } else {
                        message = session.createObjectMessage((Serializable) body);
                    }
                } else {
                    // Use entire map as message content
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        mapMessage.setObject(entry.getKey(), entry.getValue());
                    }
                    message = mapMessage;
                }
                
                // Set properties if provided
                if (properties != null) {
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        message.setObjectProperty(entry.getKey(), entry.getValue());
                    }
                }
                
                // Set headers if provided
                if (headers != null) {
                    String correlationId = (String) headers.get("correlationId");
                    if (correlationId != null) {
                        message.setJMSCorrelationID(correlationId);
                    }
                    
                    String replyTo = (String) headers.get("replyTo");
                    if (replyTo != null) {
                        Destination replyToDestination = session.createQueue(replyTo);
                        message.setJMSReplyTo(replyToDestination);
                    }
                }
            } else if (payload instanceof Serializable) {
                message = session.createObjectMessage((Serializable) payload);
            } else {
                throw new AdapterException.ValidationException(AdapterType.JMS, 
                        "Unsupported payload type: " + payload.getClass().getName());
            }
            
            // Set message properties from configuration
            if (config.getMessageProperties() != null && !config.getMessageProperties().isEmpty()) {
                String[] props = config.getMessageProperties().split(",");
                for (String prop : props) {
                    String[] keyValue = prop.split("=");
                    if (keyValue.length == 2) {
                        message.setStringProperty(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
            
            // Send the message
            if (config.isPersistent()) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            
            producer.setPriority(config.getPriority());
            producer.setTimeToLive(config.getTimeToLive());
            
            producer.send(message);
            
            // Commit if transacted
            if (config.isTransacted()) {
                session.commit();
            }
            
            logger.info("JMS receiver adapter sent message with ID: {}", message.getJMSMessageID());
            
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", message.getJMSMessageID());
            result.put("timestamp", new Date(message.getJMSTimestamp()));
            result.put("destination", destination.toString());
            
            return AdapterResult.success(result, 
                    String.format("Successfully sent JMS message: %s", message.getJMSMessageID()));
                    
        } catch (Exception e) {
            // Rollback if transacted
            if (config.isTransacted() && session != null) {
                try {
                    session.rollback();
                } catch (JMSException rollbackEx) {
                    logger.warn("Failed to rollback transaction", rollbackEx);
                }
            }
            
            logger.error("Error sending JMS message", e);
            throw new AdapterException.OperationException(AdapterType.JMS, 
                    "Failed to send JMS message: " + e.getMessage(), e);
        }
    }
    
    private void initializeJmsConnection() throws Exception {
        // Initialize connection factory
        initializeConnectionFactory();
        
        // Create connection
        connection = connectionFactory.createConnection(config.getUsername(), config.getPassword());
        
        // Set client ID if configured
        if (config.getClientId() != null && !config.getClientId().isEmpty()) {
            connection.setClientID(config.getClientId());
        }
        
        // Create session
        session = connection.createSession(config.isTransacted(), config.getAcknowledgementMode());
        
        // Create destination
        if ("topic".equalsIgnoreCase(config.getDestinationType())) {
            destination = session.createTopic(config.getDestinationName());
        } else {
            destination = session.createQueue(config.getDestinationName());
        }
        
        // Create producer
        producer = session.createProducer(destination);
        
        // Start connection
        connection.start();
    }
    
    private void initializeConnectionFactory() throws Exception {
        if (config.getJndiName() != null && !config.getJndiName().isEmpty()) {
            // Look up connection factory from JNDI
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
            props.put(Context.PROVIDER_URL, config.getProviderUrl());
            
            if (config.getJndiProperties() != null) {
                // Add additional JNDI properties
                String[] jndiProps = config.getJndiProperties().split(",");
                for (String prop : jndiProps) {
                    String[] keyValue = prop.split("=");
                    if (keyValue.length == 2) {
                        props.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
            
            Context context = new InitialContext(props);
            connectionFactory = (ConnectionFactory) context.lookup(config.getJndiName());
        } else {
            // For simulation/testing, would create vendor-specific connection factory here
            throw new AdapterException.ConfigurationException(AdapterType.JMS, 
                    "JMS connection factory configuration required");
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getDestinationName() == null || config.getDestinationName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JMS, "Destination name is required");
        }
        
        if (config.getDestinationType() == null || config.getDestinationType().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.JMS, "Destination type is required");
        }
        
        // Set defaults
        if (config.getAcknowledgementMode() == 0) {
            config.setAcknowledgementMode(Session.AUTO_ACKNOWLEDGE);
        }
        
        if (config.getPriority() < 0 || config.getPriority() > 9) {
            config.setPriority(4); // Default priority
        }
        
        if (config.getTimeToLive() < 0) {
            config.setTimeToLive(0); // Never expires
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // JMS receivers typically don't poll, they push messages
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JMS Receiver (Outbound): %s %s, Persistent: %s, Priority: %d", 
                config.getDestinationType(),
                config.getDestinationName(),
                config.isPersistent() ? "Yes" : "No",
                config.getPriority());
    }
}