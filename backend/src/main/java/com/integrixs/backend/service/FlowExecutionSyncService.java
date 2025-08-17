package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.DataStructure;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.DataStructureRepository;
import com.integrixs.data.repository.FieldMappingRepository;
import com.integrixs.data.repository.FlowStructureRepository;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

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
    
    @Autowired
    private FieldMappingRepository fieldMappingRepository;
    
    @Autowired
    private HierarchicalXmlFieldMapper xmlFieldMapper;
    
    @Autowired
    private FlowStructureRepository flowStructureRepository;
    
    /**
     * Process a message through an integration flow
     */
    @Transactional
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
            try {
                correlationId = messageService.createMessage(flow, message, protocol);
            } catch (Exception e) {
                logger.error("Error creating message log, continuing with generated ID: {}", e.getMessage());
                correlationId = UUID.randomUUID().toString();
            }
        } else {
            // Use existing correlation ID
            try {
                correlationId = messageService.createMessage(flow, message, protocol, correlationId);
            } catch (Exception e) {
                logger.error("Error creating message log, continuing with existing ID: {}", e.getMessage());
            }
        }
        context.put("correlationId", correlationId);
        context.put("flowId", flow.getId());
        // Pass through the isEndpointFlow flag if present
        if (headers.containsKey("isEndpointFlow")) {
            context.put("isEndpointFlow", "true".equals(headers.get("isEndpointFlow")));
        }
        logger.info("Using correlation ID: {}", correlationId);
        
        try {
            // Step 1: Validate incoming message against source data structure
            messageService.logProcessingStep(correlationId, flow, 
                "Validating message against source structure", 
                "Checking message format and structure", 
                com.integrixs.data.model.SystemLog.LogLevel.INFO);
                
            String validatedMessage = message;
            if (flow.getSourceStructureId() != null) {
                DataStructure sourceStructure = dataStructureRepository.findById(flow.getSourceStructureId())
                    .orElse(null);
                if (sourceStructure != null) {
                    validatedMessage = validateMessage(message, sourceStructure, context);
                    messageService.logProcessingStep(correlationId, flow,
                        "Message validation completed",
                        "Structure: " + sourceStructure.getName() + " (" + sourceStructure.getType() + ")",
                        com.integrixs.data.model.SystemLog.LogLevel.INFO);
                }
            }
            
            // Step 2: Apply transformation if flow has mapping
            String transformedMessage = validatedMessage;
            if ("WITH_MAPPING".equals(flow.getMappingMode().toString())) {
                logger.info("Applying field mapping transformation for flow: {}", flow.getName());
                
                messageService.logProcessingStep(correlationId, flow,
                    "Applying data transformations",
                    "Mapping mode: WITH_MAPPING",
                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
                    
                // Get the flow's transformation
                if (flow.getTransformations() != null && !flow.getTransformations().isEmpty()) {
                    try {
                        // Get the transformation with the lowest execution order (for request mapping)
                        FlowTransformation transformation = flow.getTransformations().stream()
                            .min((t1, t2) -> Integer.compare(t1.getExecutionOrder(), t2.getExecutionOrder()))
                            .orElse(flow.getTransformations().get(0));
                        
                        String transformationId = transformation.getId();
                        logger.info("Using transformation: {} (ID: {}, execution order: {})", 
                            transformation.getName(), transformationId, transformation.getExecutionOrder());
                        
                        // Get field mappings for this transformation
                        List<FieldMapping> fieldMappings = fieldMappingRepository.findByTransformationId(transformationId);
                        logger.info("Found {} field mappings", fieldMappings.size());
                        
                        if (fieldMappings.isEmpty()) {
                            logger.warn("No field mappings found for transformation: {}", transformationId);
                            transformedMessage = validatedMessage;
                        } else {
                            // For field mappings, we always work with XML
                            // Get the target template from the flow structure
                            String targetTemplate = null;
                            if (flow.getTargetFlowStructureId() != null) {
                                logger.info("Target flow structure ID: {}", flow.getTargetFlowStructureId());
                                FlowStructure targetFlowStructure = flowStructureRepository.findById(flow.getTargetFlowStructureId()).orElse(null);
                                if (targetFlowStructure != null) {
                                    logger.info("Target flow structure found: {}", targetFlowStructure.getName());
                                    if (targetFlowStructure.getWsdlContent() != null) {
                                        logger.info("Target flow structure has WSDL content");
                                        // TODO: Extract sample XML from WSDL
                                        targetTemplate = null; // Let the mapper create the structure for now
                                    }
                                } else {
                                    logger.warn("Target flow structure not found!");
                                }
                            } else {
                                logger.warn("Flow has no target flow structure ID!");
                            }
                            
                            logger.debug("Field mappings count: {}", fieldMappings.size());
                            if (logger.isDebugEnabled()) {
                                for (int i = 0; i < fieldMappings.size(); i++) {
                                    FieldMapping fm = fieldMappings.get(i);
                                    logger.debug("Mapping {}: source='{}', target='{}', sourceXPath='{}', targetXPath='{}'", 
                                        i+1, fm.getSourceFields(), fm.getTargetField(), fm.getSourceXPath(), fm.getTargetXPath());
                                }
                            }
                            // Apply XML field mappings
                            transformedMessage = xmlFieldMapper.mapXmlFields(
                                validatedMessage,  // source XML
                                targetTemplate,    // target template (can be null)
                                fieldMappings,     // field mappings
                                null              // namespaces (TODO: get from flow structure)
                            );
                            
                            logger.debug("Transformed message: {}", transformedMessage);
                            logger.info("Successfully applied {} field mappings", fieldMappings.size());
                            messageService.logProcessingStep(correlationId, flow,
                                "Transformation completed successfully",
                                "Applied " + fieldMappings.size() + " field mappings",
                                com.integrixs.data.model.SystemLog.LogLevel.INFO);
                        }
                    } catch (Exception e) {
                        logger.error("Error applying XML transformation: {}", e.getMessage(), e);
                        e.printStackTrace();
                        messageService.logProcessingStep(correlationId, flow,
                            "Transformation error",
                            e.getMessage(),
                            com.integrixs.data.model.SystemLog.LogLevel.ERROR);
                        throw new RuntimeException("Failed to apply XML transformation: " + e.getMessage(), e);
                    }
                } else {
                    logger.warn("Flow has WITH_MAPPING mode but no transformations!");
                    transformedMessage = validatedMessage;
                }
            } else if ("PASS_THROUGH".equals(flow.getMappingMode().toString())) {
                logger.info("Pass-through mode - no transformation applied");
                messageService.logProcessingStep(correlationId, flow,
                    "Pass-through mode",
                    "No transformations applied - direct passthrough",
                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
                transformedMessage = validatedMessage;
            }
            
            // Step 3: Execute target adapter
            logger.info("Executing target adapter: {} (ID: {})", targetAdapter.getName(), targetAdapter.getId());
            logger.debug("Original message: {}", validatedMessage);
            logger.debug("Transformed message: {}", transformedMessage);
            if (!validatedMessage.equals(transformedMessage)) {
                logger.info("Message was transformed successfully");
            } else {
                logger.warn("Message was not transformed - original and transformed messages are identical");
            }
            
            messageService.logProcessingStep(correlationId, flow,
                "Executing target adapter: " + targetAdapter.getName(),
                "Adapter type: " + targetAdapter.getType() + ", Mode: " + targetAdapter.getMode(),
                com.integrixs.data.model.SystemLog.LogLevel.INFO);
            
            // Note: Source adapter payload logging is handled by IntegrationEndpointService for SOAP/REST flows
            // Only log here if not coming from IntegrationEndpointService (check protocol type)
            if (!"SOAP".equals(protocol) && !"REST".equals(protocol)) {
                // Log source payload for non-SOAP/REST flows (e.g., direct adapter tests)
                messageService.logAdapterPayload(correlationId, sourceAdapter, "REQUEST", validatedMessage, "INBOUND");
            }
                
            String response = adapterExecutionService.executeAdapter(targetAdapter, transformedMessage, context);
            
            // Note: Target adapter response logging is handled by IntegrationEndpointService for SOAP/REST flows
            if (response != null && !"SOAP".equals(protocol) && !"REST".equals(protocol)) {
                // Log target response for non-SOAP/REST flows (e.g., direct adapter tests)
                messageService.logAdapterPayload(correlationId, targetAdapter, "RESPONSE", response, "OUTBOUND");
            }
            
            messageService.logProcessingStep(correlationId, flow,
                "Target adapter execution completed",
                "Response received from " + targetAdapter.getName(),
                com.integrixs.data.model.SystemLog.LogLevel.INFO);
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