package com.integrixs.engine.service;

import com.integrixs.adapters.core.BaseAdapter;
import com.integrixs.adapters.core.ReceiverAdapter;
import com.integrixs.adapters.core.SenderAdapter;
import com.integrixs.adapters.factory.AdapterFactory;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.MappingMode;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.FieldMappingRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.data.repository.FlowStructureRepository;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowStructureNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main engine for processing messages through integration flows
 */
@Service
public class MessageProcessingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessingEngine.class);
    
    private final MessageRoutingService messageRoutingService;
    private final HierarchicalXmlFieldMapper xmlFieldMapper;
    private final AdapterFactory adapterFactory;
    private final IntegrationFlowRepository flowRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final CommunicationAdapterRepository adapterRepository;
    private final FlowStructureRepository flowStructureRepository;
    
    @Autowired
    public MessageProcessingEngine(
            MessageRoutingService messageRoutingService,
            HierarchicalXmlFieldMapper xmlFieldMapper,
            AdapterFactory adapterFactory,
            IntegrationFlowRepository flowRepository,
            FieldMappingRepository fieldMappingRepository,
            CommunicationAdapterRepository adapterRepository,
            FlowStructureRepository flowStructureRepository) {
        this.messageRoutingService = messageRoutingService;
        this.xmlFieldMapper = xmlFieldMapper;
        this.adapterFactory = adapterFactory;
        this.flowRepository = flowRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.adapterRepository = adapterRepository;
        this.flowStructureRepository = flowStructureRepository;
    }
    
    /**
     * Process a message through an integration flow
     * 
     * @param flowId The integration flow ID
     * @param incomingMessage The message received from sender adapter
     * @return Processing result
     */
    public CompletableFuture<ProcessingResult> processMessage(String flowId, Object incomingMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting message processing for flow: {}", flowId);
                
                // Load flow configuration
                IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
                
                // Get adapter configurations
                var sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
                var targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Target adapter not found"));
                
                // Route message based on mapping mode
                Object processedMessage = messageRoutingService.processMessage(
                    incomingMessage, flow, sourceAdapter.getConfiguration());
                
                // Apply field mappings if required
                if (flow.getMappingMode() == MappingMode.WITH_MAPPING) {
                    processedMessage = applyFieldMappings(processedMessage, flow);
                }
                
                // Send to target adapter
                sendToTargetAdapter(processedMessage, targetAdapter, flow);
                
                logger.info("Message processing completed successfully for flow: {}", flowId);
                return new ProcessingResult(true, "Message processed successfully", processedMessage);
                
            } catch (Exception e) {
                logger.error("Error processing message for flow: {}", flowId, e);
                return new ProcessingResult(false, "Processing failed: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Apply field mappings to the message
     */
    private Object applyFieldMappings(Object message, IntegrationFlow flow) throws Exception {
        logger.debug("Applying field mappings for flow: {}", flow.getName());
        
        // Get field mappings for this flow
        List<FieldMapping> mappings = fieldMappingRepository.findByTransformationFlowIdAndIsActiveTrueOrderByTransformationExecutionOrder(flow.getId());
        
        if (mappings.isEmpty()) {
            logger.warn("No field mappings found for flow: {}", flow.getName());
            return message;
        }
        
        // Apply XML field mappings
        if (message instanceof String) {
            String xmlMessage = (String) message;
            Map<String, String> namespaces = extractNamespaces(flow);
            
            return xmlFieldMapper.mapXmlFields(xmlMessage, null, mappings, namespaces);
        }
        
        return message;
    }
    
    /**
     * Send processed message to target adapter
     */
    private void sendToTargetAdapter(Object message, com.integrixs.data.model.CommunicationAdapter targetAdapter, 
                                    IntegrationFlow flow) throws Exception {
        logger.debug("Sending message to target adapter: {}", targetAdapter.getName());
        
        // Create receiver adapter instance (outbound - sends TO external systems)
        ReceiverAdapter receiver = adapterFactory.createReceiver(
            convertAdapterType(targetAdapter.getType()), 
            targetAdapter.getConfiguration()
        );
        
        // Initialize and receive (which actually sends data TO external system)
        receiver.initialize();
        receiver.receive(message);
        
        logger.debug("Message sent successfully to target adapter");
    }
    
    /**
     * Extract namespaces from flow configuration
     */
    private Map<String, String> extractNamespaces(IntegrationFlow flow) {
        Map<String, String> namespaces = new HashMap<>();
        
        try {
            // Extract namespaces from source flow structure
            if (flow.getSourceFlowStructureId() != null) {
                flowStructureRepository.findById(flow.getSourceFlowStructureId())
                    .ifPresent(structure -> extractNamespacesFromStructure(structure, namespaces));
            }
            
            // Extract namespaces from target flow structure (merge with source)
            if (flow.getTargetFlowStructureId() != null) {
                flowStructureRepository.findById(flow.getTargetFlowStructureId())
                    .ifPresent(structure -> extractNamespacesFromStructure(structure, namespaces));
            }
            
            logger.debug("Extracted {} namespaces for flow {}", namespaces.size(), flow.getId());
            
        } catch (Exception e) {
            logger.error("Error extracting namespaces for flow {}: {}", flow.getId(), e.getMessage(), e);
        }
        
        return namespaces;
    }
    
    /**
     * Extract namespaces from a flow structure and add to the map
     */
    private void extractNamespacesFromStructure(FlowStructure structure, Map<String, String> namespaces) {
        if (structure.getNamespaces() != null) {
            for (FlowStructureNamespace ns : structure.getNamespaces()) {
                String prefix = ns.getPrefix();
                String uri = ns.getUri();
                
                // Handle default namespace (null or empty prefix)
                if (prefix == null || prefix.trim().isEmpty()) {
                    namespaces.put("", uri);
                } else {
                    namespaces.put(prefix, uri);
                }
                
                logger.trace("Added namespace: {} = {}", prefix, uri);
            }
        }
    }
    
    /**
     * Result of message processing
     */
    public static class ProcessingResult {
        private final boolean success;
        private final String message;
        private final Object processedData;
        
        public ProcessingResult(boolean success, String message, Object processedData) {
            this.success = success;
            this.message = message;
            this.processedData = processedData;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getProcessedData() { return processedData; }
    }
    
    /**
     * Convert from shared enum to core adapter enum
     */
    private com.integrixs.adapters.core.AdapterType convertAdapterType(com.integrixs.shared.enums.AdapterType sharedType) {
        return com.integrixs.adapters.core.AdapterType.valueOf(sharedType.name());
    }
}