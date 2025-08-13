package com.integrationlab.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.data.model.CommunicationAdapter;
import com.integrationlab.data.model.DataStructure;
import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.data.repository.CommunicationAdapterRepository;
import com.integrationlab.data.repository.DataStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that handles synchronous flow execution for real-time request/response processing.
 * Used for API integrations where an immediate response is required (SOAP, REST endpoints).
 */
@Service
public class FlowExecutionSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionSyncService.class);
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private DataStructureRepository dataStructureRepository;
    
    @Autowired
    private TransformationExecutionService transformationService;
    
    @Autowired
    private AdapterExecutionService adapterExecutionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageService messageService;
    
    /**
     * Process a message through an integration flow
     */
    public String processMessage(IntegrationFlow flow, String message, Map<String, String> headers, String protocol) throws Exception {
        logger.info("Processing message through flow: {} with protocol: {}", flow.getName(), protocol);
        
        // Get source and target adapters
        logger.info("Loading source adapter: {}", flow.getSourceAdapterId());
        CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
        logger.info("Source adapter: {} (Type: {}, Mode: {})", sourceAdapter.getName(), sourceAdapter.getType(), sourceAdapter.getMode());
        
        logger.info("Loading target adapter: {}", flow.getTargetAdapterId());
        CommunicationAdapter targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Target adapter not found"));
        logger.info("Target adapter: {} (Type: {}, Mode: {})", targetAdapter.getName(), targetAdapter.getType(), targetAdapter.getMode());
        
        // Track processing context
        Map<String, Object> context = new HashMap<>();
        context.put("flowId", flow.getId());
        context.put("flowName", flow.getName());
        context.put("protocol", protocol);
        context.put("headers", headers);
        
        // Get or create correlation ID
        String correlationId = headers.get("correlationId");
        if (correlationId == null) {
            // Create new correlation ID if not provided
            correlationId = messageService.createMessage(flow, message, protocol);
        } else {
            // Use existing correlation ID
            correlationId = messageService.createMessage(flow, message, protocol, correlationId);
        }
        context.put("correlationId", correlationId);
        logger.info("Using correlation ID: {}", correlationId);
        
        try {
            // Step 1: Validate incoming message against source data structure
            messageService.logProcessingStep(correlationId, flow, 
                "Validating message against source structure", 
                "Checking message format and structure", 
                com.integrationlab.data.model.SystemLog.LogLevel.INFO);
                
            String validatedMessage = message;
            if (flow.getSourceStructureId() != null) {
                DataStructure sourceStructure = dataStructureRepository.findById(flow.getSourceStructureId())
                    .orElse(null);
                if (sourceStructure != null) {
                    validatedMessage = validateMessage(message, sourceStructure, context);
                    messageService.logProcessingStep(correlationId, flow,
                        "Message validation completed",
                        "Structure: " + sourceStructure.getName() + " (" + sourceStructure.getType() + ")",
                        com.integrationlab.data.model.SystemLog.LogLevel.INFO);
                }
            }
            
            // Step 2: Apply transformation if flow has mapping
            String transformedMessage = validatedMessage;
            if ("WITH_MAPPING".equals(flow.getMappingMode().toString())) {
                logger.info("Applying transformation for flow: {}", flow.getName());
                messageService.logProcessingStep(correlationId, flow,
                    "Applying data transformations",
                    "Mapping mode: WITH_MAPPING",
                    com.integrationlab.data.model.SystemLog.LogLevel.INFO);
                // TODO: Apply transformations using flow.getTransformations()
                transformedMessage = validatedMessage; // For now, pass through
            } else if ("PASS_THROUGH".equals(flow.getMappingMode().toString())) {
                logger.info("Pass-through mode - no transformation applied");
                messageService.logProcessingStep(correlationId, flow,
                    "Pass-through mode",
                    "No transformations applied - direct passthrough",
                    com.integrationlab.data.model.SystemLog.LogLevel.INFO);
                transformedMessage = validatedMessage;
            }
            
            // Step 3: Execute target adapter
            logger.info("Executing target adapter: {} (ID: {})", targetAdapter.getName(), targetAdapter.getId());
            messageService.logProcessingStep(correlationId, flow,
                "Executing target adapter: " + targetAdapter.getName(),
                "Adapter type: " + targetAdapter.getType() + ", Mode: " + targetAdapter.getMode(),
                com.integrationlab.data.model.SystemLog.LogLevel.INFO);
            
            // Log the incoming message payload for the source adapter
            messageService.logAdapterPayload(correlationId, sourceAdapter, "REQUEST", validatedMessage, "INBOUND");
                
            String response = adapterExecutionService.executeAdapter(targetAdapter, transformedMessage, context);
            
            // Log the response payload from the target adapter
            if (response != null) {
                messageService.logAdapterPayload(correlationId, targetAdapter, "RESPONSE", response, "OUTBOUND");
            }
            
            messageService.logProcessingStep(correlationId, flow,
                "Target adapter execution completed",
                "Response received from " + targetAdapter.getName(),
                com.integrationlab.data.model.SystemLog.LogLevel.INFO);
            logger.info("Target adapter execution completed");
            
            // Step 4: Process response if needed
            // TODO: Handle response transformations if needed
            
            logger.info("Successfully processed message through flow: {}", flow.getName());
            
            // Update message status to completed
            messageService.updateMessageStatus(correlationId, "COMPLETED", response);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing message through flow: {} (ID: {}). Error: {}", 
                        flow.getName(), flow.getId(), e.getMessage(), e);
            
            // Log the root cause if it's different
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            if (rootCause != e) {
                logger.error("Root cause: {}", rootCause.getMessage(), rootCause);
            }
            
            // Update message status to failed
            messageService.updateMessageStatus(correlationId, "FAILED", e.getMessage());
            
            throw new RuntimeException("Flow processing failed: " + e.getMessage(), e);
        }
    }
    
    private String validateMessage(String message, DataStructure structure, Map<String, Object> context) throws Exception {
        // TODO: Implement actual validation based on structure type
        logger.debug("Validating message against structure: {}", structure.getName());
        
        switch (structure.getType().toLowerCase()) {
            case "xml":
            case "xsd":
                // Basic XML validation
                return validateXmlMessage(message, structure);
                
            case "json":
                // Basic JSON validation
                return validateJsonMessage(message, structure);
                
            case "delimited":
            case "fixed_width":
                // Flat file validation
                return validateFlatFileMessage(message, structure);
                
            default:
                return message;
        }
    }
    
    private String validateXmlMessage(String message, DataStructure structure) throws Exception {
        // For now, just check if it's valid XML
        try {
            javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(message.getBytes()));
            return message;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid XML message: " + e.getMessage());
        }
    }
    
    private String validateJsonMessage(String message, DataStructure structure) throws Exception {
        // For now, just check if it's valid JSON
        try {
            objectMapper.readTree(message);
            return message;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON message: " + e.getMessage());
        }
    }
    
    private String validateFlatFileMessage(String message, DataStructure structure) throws Exception {
        // TODO: Implement flat file validation
        return message;
    }
}