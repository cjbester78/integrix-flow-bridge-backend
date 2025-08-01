package com.integrationlab.engine.service;

import com.integrationlab.engine.xml.MessageToXmlConverter;
import com.integrationlab.engine.xml.XmlConversionException;
import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.model.MappingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service responsible for routing messages through the integration flow
 * Handles both mapped and pass-through modes
 */
@Service
public class MessageRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageRoutingService.class);
    
    private final MessageToXmlConverter messageToXmlConverter;
    
    @Autowired
    public MessageRoutingService(MessageToXmlConverter messageToXmlConverter) {
        this.messageToXmlConverter = messageToXmlConverter;
    }
    
    /**
     * Process message based on flow configuration
     * 
     * @param messageData The incoming message data
     * @param flow The integration flow configuration
     * @param senderAdapterConfig The sender adapter configuration
     * @return Processed message ready for receiver adapter
     */
    public Object processMessage(Object messageData, IntegrationFlow flow, Object senderAdapterConfig) {
        
        if (flow.getMappingMode() == MappingMode.PASS_THROUGH) {
            logger.info("Processing message in PASS_THROUGH mode for flow: {}", flow.getName());
            return handlePassThrough(messageData, flow);
        } else {
            logger.info("Processing message in WITH_MAPPING mode for flow: {}", flow.getName());
            return handleWithMapping(messageData, flow, senderAdapterConfig);
        }
    }
    
    /**
     * Handle pass-through mode - no conversion or mapping
     */
    private Object handlePassThrough(Object messageData, IntegrationFlow flow) {
        logger.debug("Pass-through mode: Forwarding message without conversion");
        
        // Log message type and size for monitoring
        if (messageData != null) {
            logger.debug("Message type: {}", messageData.getClass().getSimpleName());
            if (messageData instanceof String) {
                logger.debug("Message size: {} characters", ((String) messageData).length());
            } else if (messageData instanceof byte[]) {
                logger.debug("Message size: {} bytes", ((byte[]) messageData).length);
            }
        }
        
        // Return data as-is for direct transfer to receiver
        return messageData;
    }
    
    /**
     * Handle with-mapping mode - convert to XML and prepare for mapping
     */
    private Object handleWithMapping(Object messageData, IntegrationFlow flow, Object senderAdapterConfig) {
        try {
            logger.debug("Converting message to XML for mapping");
            
            // Convert to XML using appropriate converter
            String xmlData = messageToXmlConverter.convertToXml(messageData, senderAdapterConfig);
            
            logger.debug("Message converted to XML successfully");
            return xmlData;
            
        } catch (XmlConversionException e) {
            logger.error("Failed to convert message to XML for flow: {}", flow.getName(), e);
            throw new RuntimeException("XML conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a flow requires mapping
     */
    public boolean requiresMapping(IntegrationFlow flow) {
        return flow.getMappingMode() == MappingMode.WITH_MAPPING;
    }
    
    /**
     * Check if a flow is in pass-through mode
     */
    public boolean isPassThrough(IntegrationFlow flow) {
        return flow.getMappingMode() == MappingMode.PASS_THROUGH;
    }
}