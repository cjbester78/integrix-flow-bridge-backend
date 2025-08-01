package com.integrationlab.service;

import com.integrationlab.model.*;
import com.integrationlab.repository.*;
import com.integrationlab.shared.dto.flow.FlowTransformationDTO;
import com.integrationlab.shared.dto.mapping.FieldMappingDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a complete direct mapping flow with business components, adapters, and field mappings
     */
    public IntegrationFlow createDirectMappingFlow(DirectMappingFlowRequest request) {
        // Validate business components exist
        validateBusinessComponent(request.getSourceBusinessComponentId());
        validateBusinessComponent(request.getTargetBusinessComponentId());
        
        // Validate adapters exist
        validateAdapter(request.getSourceAdapterId());
        validateAdapter(request.getTargetAdapterId());
        
        // Create the integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        flow.setSourceAdapterId(request.getSourceAdapterId());
        flow.setTargetAdapterId(request.getTargetAdapterId());
        flow.setSourceStructureId(request.getSourceStructureId());
        flow.setTargetStructureId(request.getTargetStructureId());
        flow.setStatus(FlowStatus.DRAFT);
        flow.setCreatedBy(request.getCreatedBy());
        
        // Save additional configuration as JSON
        try {
            FlowConfiguration config = new FlowConfiguration();
            config.setSourceBusinessComponentId(request.getSourceBusinessComponentId());
            config.setTargetBusinessComponentId(request.getTargetBusinessComponentId());
            config.setFlowType("DIRECT_MAPPING");
            flow.setConfiguration(objectMapper.writeValueAsString(config));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize flow configuration", e);
        }
        
        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Create transformation if field mappings are provided
        if (request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) {
            FlowTransformationDTO transformation = new FlowTransformationDTO();
            transformation.setFlowId(savedFlow.getId());
            transformation.setType("FIELD_MAPPING");
            transformation.setConfiguration("{}");
            transformation.setExecutionOrder(1);
            transformation.setActive(true);
            
            FlowTransformationDTO savedTransformation = transformationService.save(transformation);
            
            // Save field mappings
            for (FieldMappingDTO mapping : request.getFieldMappings()) {
                mapping.setTransformationId(savedTransformation.getId());
                fieldMappingService.save(mapping);
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
        flow.setSourceAdapterId(request.getSourceAdapterId());
        flow.setTargetAdapterId(request.getTargetAdapterId());
        flow.setStatus(FlowStatus.DRAFT);
        flow.setCreatedBy(request.getCreatedBy());
        
        // Save orchestration configuration as JSON
        try {
            OrchestrationConfiguration config = new OrchestrationConfiguration();
            config.setBusinessComponentIds(request.getBusinessComponentIds());
            config.setAdapterIds(request.getAdapterIds());
            config.setOrchestrationSteps(request.getOrchestrationSteps());
            config.setFlowType("ORCHESTRATION");
            flow.setConfiguration(objectMapper.writeValueAsString(config));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize orchestration configuration", e);
        }
        
        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Create transformations for each orchestration step
        if (request.getOrchestrationSteps() != null) {
            int order = 1;
            for (OrchestrationStep step : request.getOrchestrationSteps()) {
                FlowTransformationDTO transformation = new FlowTransformationDTO();
                transformation.setFlowId(savedFlow.getId());
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
        return flowRepository.findById(flowId).map(flow -> {
            flow.setName(request.getFlowName());
            flow.setDescription(request.getDescription());
            
            if (request.getSourceAdapterId() != null) {
                validateAdapter(request.getSourceAdapterId());
                flow.setSourceAdapterId(request.getSourceAdapterId());
            }
            
            if (request.getTargetAdapterId() != null) {
                validateAdapter(request.getTargetAdapterId());
                flow.setTargetAdapterId(request.getTargetAdapterId());
            }
            
            flow.setSourceStructureId(request.getSourceStructureId());
            flow.setTargetStructureId(request.getTargetStructureId());
            
            return flowRepository.save(flow);
        });
    }

    /**
     * Get complete flow composition including all related components
     */
    public Optional<CompleteFlowComposition> getCompleteFlowComposition(String flowId) {
        return flowRepository.findById(flowId).map(flow -> {
            CompleteFlowComposition composition = new CompleteFlowComposition();
            composition.setFlow(flow);
            
            // Get business components if available in configuration
            try {
                if (flow.getConfiguration() != null) {
                    if (flow.getConfiguration().contains("DIRECT_MAPPING")) {
                        FlowConfiguration config = objectMapper.readValue(flow.getConfiguration(), FlowConfiguration.class);
                        composition.setSourceBusinessComponent(
                            businessComponentRepository.findById(config.getSourceBusinessComponentId()).orElse(null)
                        );
                        composition.setTargetBusinessComponent(
                            businessComponentRepository.findById(config.getTargetBusinessComponentId()).orElse(null)
                        );
                    }
                }
            } catch (Exception e) {
                // Configuration parsing failed, continue without business components
            }
            
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
        return flowRepository.findById(flowId).map(flow -> {
            // Delete field mappings first (cascade should handle this, but being explicit)
            List<FlowTransformation> transformations = transformationRepository.findByFlowId(flowId);
            for (FlowTransformation transformation : transformations) {
                fieldMappingRepository.deleteByTransformationId(transformation.getId());
            }
            
            // Delete transformations
            transformationRepository.deleteByFlowId(flowId);
            
            // Delete the flow
            flowRepository.delete(flow);
            
            return true;
        }).orElse(false);
    }

    private void validateBusinessComponent(String componentId) {
        if (componentId != null && !businessComponentRepository.existsById(componentId)) {
            throw new IllegalArgumentException("Business component not found: " + componentId);
        }
    }

    private void validateAdapter(String adapterId) {
        if (adapterId != null && !adapterRepository.existsById(adapterId)) {
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
        private String sourceStructureId;
        private String targetStructureId;
        private String createdBy;
        private List<FieldMappingDTO> fieldMappings;

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
        public String getSourceStructureId() { return sourceStructureId; }
        public void setSourceStructureId(String sourceStructureId) { this.sourceStructureId = sourceStructureId; }
        public String getTargetStructureId() { return targetStructureId; }
        public void setTargetStructureId(String targetStructureId) { this.targetStructureId = targetStructureId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
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
        private String sourceStructureId;
        private String targetStructureId;

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceAdapterId() { return sourceAdapterId; }
        public void setSourceAdapterId(String sourceAdapterId) { this.sourceAdapterId = sourceAdapterId; }
        public String getTargetAdapterId() { return targetAdapterId; }
        public void setTargetAdapterId(String targetAdapterId) { this.targetAdapterId = targetAdapterId; }
        public String getSourceStructureId() { return sourceStructureId; }
        public void setSourceStructureId(String sourceStructureId) { this.sourceStructureId = sourceStructureId; }
        public String getTargetStructureId() { return targetStructureId; }
        public void setTargetStructureId(String targetStructureId) { this.targetStructureId = targetStructureId; }
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
}