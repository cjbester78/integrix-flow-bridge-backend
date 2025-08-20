package com.integrixs.backend.service;

import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import com.integrixs.shared.dto.FieldMappingDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FlowCompositionService {

    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    @Autowired
    private BusinessComponentRepository businessComponentRepository;
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private FlowTransformationRepository transformationRepository;
    
    @Autowired
    private FieldMappingRepository fieldMappingRepository;
    
    @Autowired
    private FlowTransformationService transformationService;
    
    @Autowired
    private FieldMappingService fieldMappingService;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a complete direct mapping flow with business components, adapters, and field mappings
     */
    public IntegrationFlow createDirectMappingFlow(DirectMappingFlowRequest request) {
        // Check if flow name already exists
        if (flowRepository.existsByName(request.getFlowName())) {
            throw new IllegalArgumentException("A flow with the name '" + request.getFlowName() + "' already exists");
        }
        
        // Validate business components exist (only if provided)
        if (request.getSourceBusinessComponentId() != null) {
            validateBusinessComponent(request.getSourceBusinessComponentId());
        }
        if (request.getTargetBusinessComponentId() != null) {
            validateBusinessComponent(request.getTargetBusinessComponentId());
        }
        
        // Validate adapters exist
        validateAdapter(request.getSourceAdapterId());
        validateAdapter(request.getTargetAdapterId());
        
        // Create the integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        // Convert String adapter IDs to UUID
        if (request.getSourceAdapterId() != null) {
            flow.setSourceAdapterId(UUID.fromString(request.getSourceAdapterId()));
        }
        if (request.getTargetAdapterId() != null) {
            flow.setTargetAdapterId(UUID.fromString(request.getTargetAdapterId()));
        }
        // Convert String structure IDs to UUID
        if (request.getSourceFlowStructureId() != null) {
            flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
        }
        if (request.getTargetFlowStructureId() != null) {
            flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
        }
        // Deprecated fields - no longer used
        // Source and target structures are now handled through flow structures
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        // Load user from createdBy string
        if (request.getCreatedBy() != null) {
            User createdByUser = userRepository.findById(UUID.fromString(request.getCreatedBy()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getCreatedBy()));
            flow.setCreatedBy(createdByUser);
        }
        
        // Use mapping mode from request if provided, otherwise determine based on field mappings
        if (request.getMappingMode() != null) {
            flow.setMappingMode(MappingMode.valueOf(request.getMappingMode()));
        } else {
            // Fallback to determining based on field mappings
            boolean hasMappings = (request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) ||
                                 (request.getAdditionalMappings() != null && !request.getAdditionalMappings().isEmpty());
            flow.setMappingMode(hasMappings ? MappingMode.WITH_MAPPING : MappingMode.PASS_THROUGH);
        }
        
        flow.setSkipXmlConversion(request.isSkipXmlConversion());
        
        // Set the source business component as the primary business component
        if (request.getSourceBusinessComponentId() != null) {
            BusinessComponent businessComponent = businessComponentRepository.findById(UUID.fromString(request.getSourceBusinessComponentId())).orElse(null);
            if (businessComponent != null) {
                flow.setBusinessComponent(businessComponent);
            }
        }
        
        // Set flow type
        flow.setFlowType(FlowType.DIRECT_MAPPING);
        
        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Create transformation if field mappings are provided
        if (request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) {
            FlowTransformationDTO transformation = new FlowTransformationDTO();
            transformation.setFlowId(savedFlow.getId().toString());
            transformation.setType("FIELD_MAPPING");
            transformation.setName(request.getRequestMappingName() != null ? request.getRequestMappingName() : "Request Mapping");
            // Build configuration with mapping type and WSDL operations
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("mappingType", "request");
            if (request.getSourceWsdlOperation() != null) {
                configMap.put("sourceWsdlOperation", request.getSourceWsdlOperation());
            }
            if (request.getTargetWsdlOperation() != null) {
                configMap.put("targetWsdlOperation", request.getTargetWsdlOperation());
            }
            try {
                transformation.setConfiguration(objectMapper.writeValueAsString(configMap));
            } catch (JsonProcessingException e) {
                transformation.setConfiguration("{\"mappingType\":\"request\"}");
            }
            transformation.setExecutionOrder(1);
            transformation.setActive(true);
            
            FlowTransformationDTO savedTransformation = transformationService.save(transformation);
            
            // Save field mappings
            int mappingOrder = 1;
            for (FieldMappingDTO mapping : request.getFieldMappings()) {
                mapping.setTransformationId(savedTransformation.getId().toString());
                mapping.setMappingOrder(mappingOrder++);
                fieldMappingService.save(mapping);
            }
        }
        
        // Handle additional mappings for synchronous flows (response, fault mappings)
        if (request.getAdditionalMappings() != null && !request.getAdditionalMappings().isEmpty()) {
            int order = 2;
            for (AdditionalMapping additionalMapping : request.getAdditionalMappings()) {
                if (additionalMapping.getFieldMappings() != null && !additionalMapping.getFieldMappings().isEmpty()) {
                    FlowTransformationDTO transformation = new FlowTransformationDTO();
                    transformation.setFlowId(savedFlow.getId().toString());
                    transformation.setType("FIELD_MAPPING");
                    transformation.setName(additionalMapping.getName()); // Save the user-entered name
                    
                    // Determine message type based on order and flow mode
                    String messageType;
                    if (savedFlow.getMappingMode() == MappingMode.PASS_THROUGH) {
                        // Async mode: second mapping is fault
                        messageType = "fault";
                    } else {
                        // Sync mode: second is response, third is fault
                        messageType = order == 2 ? "response" : "fault";
                    }
                    
                    // Store mapping type and WSDL operations in configuration
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> configMap = new HashMap<>();
                        configMap.put("mappingType", messageType);
                        if (additionalMapping.getSourceWsdlOperation() != null) {
                            configMap.put("sourceWsdlOperation", additionalMapping.getSourceWsdlOperation());
                        }
                        if (additionalMapping.getTargetWsdlOperation() != null) {
                            configMap.put("targetWsdlOperation", additionalMapping.getTargetWsdlOperation());
                        }
                        String config = mapper.writeValueAsString(configMap);
                        transformation.setConfiguration(config);
                    } catch (Exception e) {
                        transformation.setConfiguration("{\"mappingType\":\"" + messageType + "\"}");
                    }
                    
                    transformation.setExecutionOrder(order++);
                    transformation.setActive(true);
                    
                    FlowTransformationDTO savedTransformation = transformationService.save(transformation);
                    
                    // Save field mappings for this additional mapping
                    int fieldMappingOrder = 1;
                    for (FieldMappingDTO mapping : additionalMapping.getFieldMappings()) {
                        mapping.setTransformationId(savedTransformation.getId().toString());
                        mapping.setMappingOrder(fieldMappingOrder++);
                        fieldMappingService.save(mapping);
                    }
                }
            }
        }
        
        return savedFlow;
    }

    /**
     * Create a complete orchestrated flow with multiple steps and routing
     */
    public IntegrationFlow createOrchestrationFlow(OrchestrationFlowRequest request) {
        // Validate business components
        for (String componentId : request.getBusinessComponentIds()) {
            validateBusinessComponent(componentId);
        }
        
        // Validate adapters
        for (String adapterId : request.getAdapterIds()) {
            validateAdapter(adapterId);
        }
        
        // Create the integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        // Convert String adapter IDs to UUID
        if (request.getSourceAdapterId() != null) {
            flow.setSourceAdapterId(UUID.fromString(request.getSourceAdapterId()));
        }
        if (request.getTargetAdapterId() != null) {
            flow.setTargetAdapterId(UUID.fromString(request.getTargetAdapterId()));
        }
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        // Load user from createdBy string
        if (request.getCreatedBy() != null) {
            User createdByUser = userRepository.findById(UUID.fromString(request.getCreatedBy()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getCreatedBy()));
            flow.setCreatedBy(createdByUser);
        }
        
        // Set flow type to orchestration
        flow.setFlowType(FlowType.ORCHESTRATION);
        
        // TODO: Store orchestration steps in a proper table structure instead of JSON
        
        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Create transformations for each orchestration step
        if (request.getOrchestrationSteps() != null) {
            int order = 1;
            for (OrchestrationStep step : request.getOrchestrationSteps()) {
                FlowTransformationDTO transformation = new FlowTransformationDTO();
                transformation.setFlowId(savedFlow.getId().toString());
                transformation.setType(step.getType());
                try {
					transformation.setConfiguration(objectMapper.writeValueAsString(step.getConfiguration()));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                transformation.setExecutionOrder(order++);
                transformation.setActive(true);
                
                transformationService.save(transformation);
            }
        }
        
        return savedFlow;
    }

    /**
     * Update an existing flow with new configuration
     */
    public Optional<IntegrationFlow> updateFlowComposition(String flowId, UpdateFlowRequest request) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            flow.setName(request.getFlowName());
            flow.setDescription(request.getDescription());
            
            if (request.getSourceAdapterId() != null) {
                validateAdapter(request.getSourceAdapterId());
                flow.setSourceAdapterId(UUID.fromString(request.getSourceAdapterId()));
            }
            
            if (request.getTargetAdapterId() != null) {
                validateAdapter(request.getTargetAdapterId());
                flow.setTargetAdapterId(UUID.fromString(request.getTargetAdapterId()));
            }
            
            // Convert String structure IDs to UUID
            if (request.getSourceFlowStructureId() != null) {
                flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
            }
            if (request.getTargetFlowStructureId() != null) {
                flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
            }
            // Deprecated fields - no longer used
            // Source and target structures are now handled through flow structures
            
            return flowRepository.save(flow);
        });
    }

    /**
     * Get complete flow composition including all related components
     */
    public Optional<CompleteFlowComposition> getCompleteFlowComposition(String flowId) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            CompleteFlowComposition composition = new CompleteFlowComposition();
            composition.setFlow(flow);
            
            // Business components are now stored directly in the flow
            // No need to parse JSON configuration anymore
            
            // Get adapters
            composition.setSourceAdapter(adapterRepository.findById(flow.getSourceAdapterId()).orElse(null));
            composition.setTargetAdapter(adapterRepository.findById(flow.getTargetAdapterId()).orElse(null));
            
            // Get transformations
            composition.setTransformations(transformationService.getByFlowId(flowId));
            
            return composition;
        });
    }

    /**
     * Delete a complete flow and all its related components
     */
    public boolean deleteFlowComposition(String flowId) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            // Delete field mappings first (cascade should handle this, but being explicit)
            List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
            for (FlowTransformation transformation : transformations) {
                fieldMappingRepository.deleteByTransformationId(transformation.getId());
            }
            
            // Delete transformations
            transformationRepository.deleteByFlowId(flow.getId());
            
            // Delete the flow
            flowRepository.delete(flow);
            
            return true;
        }).orElse(false);
    }

    private void validateBusinessComponent(String componentId) {
        if (componentId != null && !businessComponentRepository.existsById(UUID.fromString(componentId))) {
            throw new IllegalArgumentException("Business component not found: " + componentId);
        }
    }

    private void validateAdapter(String adapterId) {
        if (adapterId != null && !adapterRepository.existsById(UUID.fromString(adapterId))) {
            throw new IllegalArgumentException("Communication adapter not found: " + adapterId);
        }
    }

    // DTOs for request/response
    public static class DirectMappingFlowRequest {
        private String flowName;
        private String description;
        private String sourceBusinessComponentId;
        private String targetBusinessComponentId;
        private String sourceAdapterId;
        private String targetAdapterId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;
        private String createdBy;
        private String requestMappingName;
        private List<FieldMappingDTO> fieldMappings;
        private List<AdditionalMapping> additionalMappings;
        private boolean skipXmlConversion;
        private String mappingMode; // Add mappingMode field
        private String sourceWsdlOperation; // Selected WSDL operation for source
        private String targetWsdlOperation; // Selected WSDL operation for target

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceBusinessComponentId() { return sourceBusinessComponentId; }
        public void setSourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; }
        public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
        public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
        public String getSourceAdapterId() { return sourceAdapterId; }
        public void setSourceAdapterId(String sourceAdapterId) { this.sourceAdapterId = sourceAdapterId; }
        public String getTargetAdapterId() { return targetAdapterId; }
        public void setTargetAdapterId(String targetAdapterId) { this.targetAdapterId = targetAdapterId; }
        public String getSourceFlowStructureId() { return sourceFlowStructureId; }
        public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
        public String getTargetFlowStructureId() { return targetFlowStructureId; }
        public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getRequestMappingName() { return requestMappingName; }
        public void setRequestMappingName(String requestMappingName) { this.requestMappingName = requestMappingName; }
        public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
        public List<AdditionalMapping> getAdditionalMappings() { return additionalMappings; }
        public void setAdditionalMappings(List<AdditionalMapping> additionalMappings) { this.additionalMappings = additionalMappings; }
        public boolean isSkipXmlConversion() { return skipXmlConversion; }
        public void setSkipXmlConversion(boolean skipXmlConversion) { this.skipXmlConversion = skipXmlConversion; }
        public String getMappingMode() { return mappingMode; }
        public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
        public String getSourceWsdlOperation() { return sourceWsdlOperation; }
        public void setSourceWsdlOperation(String sourceWsdlOperation) { this.sourceWsdlOperation = sourceWsdlOperation; }
        public String getTargetWsdlOperation() { return targetWsdlOperation; }
        public void setTargetWsdlOperation(String targetWsdlOperation) { this.targetWsdlOperation = targetWsdlOperation; }
    }

    public static class OrchestrationFlowRequest {
        private String flowName;
        private String description;
        private String sourceAdapterId;
        private String targetAdapterId;
        private String createdBy;
        private List<String> businessComponentIds;
        private List<String> adapterIds;
        private List<OrchestrationStep> orchestrationSteps;

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceAdapterId() { return sourceAdapterId; }
        public void setSourceAdapterId(String sourceAdapterId) { this.sourceAdapterId = sourceAdapterId; }
        public String getTargetAdapterId() { return targetAdapterId; }
        public void setTargetAdapterId(String targetAdapterId) { this.targetAdapterId = targetAdapterId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public List<String> getBusinessComponentIds() { return businessComponentIds; }
        public void setBusinessComponentIds(List<String> businessComponentIds) { this.businessComponentIds = businessComponentIds; }
        public List<String> getAdapterIds() { return adapterIds; }
        public void setAdapterIds(List<String> adapterIds) { this.adapterIds = adapterIds; }
        public List<OrchestrationStep> getOrchestrationSteps() { return orchestrationSteps; }
        public void setOrchestrationSteps(List<OrchestrationStep> orchestrationSteps) { this.orchestrationSteps = orchestrationSteps; }
    }

    public static class UpdateFlowRequest {
        private String flowName;
        private String description;
        private String sourceAdapterId;
        private String targetAdapterId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceAdapterId() { return sourceAdapterId; }
        public void setSourceAdapterId(String sourceAdapterId) { this.sourceAdapterId = sourceAdapterId; }
        public String getTargetAdapterId() { return targetAdapterId; }
        public void setTargetAdapterId(String targetAdapterId) { this.targetAdapterId = targetAdapterId; }
        public String getSourceFlowStructureId() { return sourceFlowStructureId; }
        public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
        public String getTargetFlowStructureId() { return targetFlowStructureId; }
        public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
    }

    public static class CompleteFlowComposition {
        private IntegrationFlow flow;
        private BusinessComponent sourceBusinessComponent;
        private BusinessComponent targetBusinessComponent;
        private CommunicationAdapter sourceAdapter;
        private CommunicationAdapter targetAdapter;
        private List<FlowTransformationDTO> transformations;

        // Getters and setters
        public IntegrationFlow getFlow() { return flow; }
        public void setFlow(IntegrationFlow flow) { this.flow = flow; }
        public BusinessComponent getSourceBusinessComponent() { return sourceBusinessComponent; }
        public void setSourceBusinessComponent(BusinessComponent sourceBusinessComponent) { this.sourceBusinessComponent = sourceBusinessComponent; }
        public BusinessComponent getTargetBusinessComponent() { return targetBusinessComponent; }
        public void setTargetBusinessComponent(BusinessComponent targetBusinessComponent) { this.targetBusinessComponent = targetBusinessComponent; }
        public CommunicationAdapter getSourceAdapter() { return sourceAdapter; }
        public void setSourceAdapter(CommunicationAdapter sourceAdapter) { this.sourceAdapter = sourceAdapter; }
        public CommunicationAdapter getTargetAdapter() { return targetAdapter; }
        public void setTargetAdapter(CommunicationAdapter targetAdapter) { this.targetAdapter = targetAdapter; }
        public List<FlowTransformationDTO> getTransformations() { return transformations; }
        public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }
    }

    // Configuration classes
    public static class FlowConfiguration {
        private String sourceBusinessComponentId;
        private String targetBusinessComponentId;
        private String flowType;

        public String getSourceBusinessComponentId() { return sourceBusinessComponentId; }
        public void setSourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; }
        public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
        public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String flowType) { this.flowType = flowType; }
    }

    public static class OrchestrationConfiguration {
        private List<String> businessComponentIds;
        private List<String> adapterIds;
        private List<OrchestrationStep> orchestrationSteps;
        private String flowType;

        public List<String> getBusinessComponentIds() { return businessComponentIds; }
        public void setBusinessComponentIds(List<String> businessComponentIds) { this.businessComponentIds = businessComponentIds; }
        public List<String> getAdapterIds() { return adapterIds; }
        public void setAdapterIds(List<String> adapterIds) { this.adapterIds = adapterIds; }
        public List<OrchestrationStep> getOrchestrationSteps() { return orchestrationSteps; }
        public void setOrchestrationSteps(List<OrchestrationStep> orchestrationSteps) { this.orchestrationSteps = orchestrationSteps; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String flowType) { this.flowType = flowType; }
    }

    public static class OrchestrationStep {
        private String type;
        private Object configuration;
        private int order;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getConfiguration() { return configuration; }
        public void setConfiguration(Object configuration) { this.configuration = configuration; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }
    
    public static class AdditionalMapping {
        private String name;
        private List<FieldMappingDTO> fieldMappings;
        private String sourceWsdlOperation; // Selected WSDL operation for source
        private String targetWsdlOperation; // Selected WSDL operation for target
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
        public String getSourceWsdlOperation() { return sourceWsdlOperation; }
        public void setSourceWsdlOperation(String sourceWsdlOperation) { this.sourceWsdlOperation = sourceWsdlOperation; }
        public String getTargetWsdlOperation() { return targetWsdlOperation; }
        public void setTargetWsdlOperation(String targetWsdlOperation) { this.targetWsdlOperation = targetWsdlOperation; }
    }
}