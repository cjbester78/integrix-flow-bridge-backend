package com.integrationlab.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integrationlab.data.model.FlowTransformation;
import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.data.repository.FlowTransformationRepository;
import com.integrationlab.data.repository.IntegrationFlowRepository;
import com.integrationlab.shared.dto.flow.FlowTransformationDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlowTransformationService {

    @Autowired
    private FlowTransformationRepository transformationRepository;
    
    @Autowired
    private IntegrationFlowRepository flowRepository;

    public List<FlowTransformationDTO> getByFlowId(String flowId) {
        return transformationRepository.findByFlowId(flowId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FlowTransformationDTO save(FlowTransformationDTO transformationDTO) {
        FlowTransformation transformation = fromDTO(transformationDTO);
        return toDTO(transformationRepository.save(transformation));
    }

    public Optional<FlowTransformationDTO> getById(String id) {
        return transformationRepository.findById(id).map(this::toDTO);
    }

    public void delete(String id) {
        transformationRepository.deleteById(id);
    }

    private FlowTransformationDTO toDTO(FlowTransformation transformation) {
        FlowTransformationDTO dto = new FlowTransformationDTO();
        dto.setId(transformation.getId());
        dto.setFlowId(transformation.getFlow() != null ? transformation.getFlow().getId() : null);
        dto.setType(transformation.getType().toString());
        dto.setConfiguration(transformation.getConfiguration());
        dto.setExecutionOrder(transformation.getExecutionOrder());
        dto.setActive(transformation.isActive());
        dto.setCreatedAt(transformation.getCreatedAt());
        dto.setUpdatedAt(transformation.getUpdatedAt());
        // Note: fieldMappings would be populated separately if needed
        return dto;
    }

    private FlowTransformation fromDTO(FlowTransformationDTO dto) {
        FlowTransformation transformation = new FlowTransformation();
        transformation.setId(dto.getId());
        
        // Set flow if flowId is provided
        if (dto.getFlowId() != null) {
            Optional<IntegrationFlow> flow = flowRepository.findById(dto.getFlowId());
            flow.ifPresent(transformation::setFlow);
        }
        
        transformation.setType(FlowTransformation.TransformationType.valueOf(dto.getType()));
        transformation.setConfiguration(dto.getConfiguration());
        transformation.setExecutionOrder(dto.getExecutionOrder());
        transformation.setActive(dto.isActive());
        return transformation;
    }
}
