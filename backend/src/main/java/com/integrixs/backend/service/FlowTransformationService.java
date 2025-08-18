package com.integrixs.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.FlowTransformationRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlowTransformationService {

    @Autowired
    private FlowTransformationRepository transformationRepository;
    
    @Autowired
    private IntegrationFlowRepository flowRepository;

    public List<FlowTransformationDTO> getByFlowId(String flowId) {
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flowId);
        System.out.println("Found " + transformations.size() + " transformations for flow " + flowId);
        
        return transformations.stream()
                .map(transformation -> {
                    FlowTransformationDTO dto = toDTO(transformation);
                    System.out.println("Transformation: id=" + dto.getId() + ", name=" + dto.getName() + ", type=" + dto.getType());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public FlowTransformationDTO save(FlowTransformationDTO transformationDTO) {
        FlowTransformation transformation = fromDTO(transformationDTO);
        return toDTO(transformationRepository.save(transformation));
    }

    public Optional<FlowTransformationDTO> getById(String id) {
        return transformationRepository.findById(UUID.fromString(id)).map(this::toDTO);
    }

    public void delete(String id) {
        transformationRepository.deleteById(UUID.fromString(id));
    }

    private FlowTransformationDTO toDTO(FlowTransformation transformation) {
        FlowTransformationDTO dto = new FlowTransformationDTO();
        dto.setId(transformation.getId().toString());
        dto.setFlowId(transformation.getFlow() != null ? transformation.getFlow().getId().toString() : null);
        dto.setType(transformation.getType().toString());
        dto.setName(transformation.getName()); // Include the name field
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
        if (dto.getId() != null) {
            transformation.setId(UUID.fromString(dto.getId()));
        }
        
        // Set flow if flowId is provided
        Optional<IntegrationFlow> flow = Optional.empty();
        if (dto.getFlowId() != null) {
            flow = flowRepository.findById(UUID.fromString(dto.getFlowId()));
            flow.ifPresent(transformation::setFlow);
        }
        
        transformation.setType(FlowTransformation.TransformationType.valueOf(dto.getType()));
        transformation.setName(dto.getName()); // Set the name field
        transformation.setConfiguration(dto.getConfiguration());
        transformation.setExecutionOrder(dto.getExecutionOrder());
        transformation.setActive(dto.isActive());
        
        // TODO: Set audit fields properly - need to resolve User vs String inconsistency
        // For now, the AuditEntityListener will handle setting timestamps
        
        return transformation;
    }
}
