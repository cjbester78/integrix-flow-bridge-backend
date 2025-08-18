package com.integrixs.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.repository.FieldMappingRepository;
import com.integrixs.data.repository.FlowTransformationRepository;
import com.integrixs.shared.dto.FieldMappingDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieldMappingService {

    @Autowired
    private FieldMappingRepository mappingRepository;
    
    @Autowired
    private FlowTransformationRepository transformationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<FieldMappingDTO> getByTransformationId(String transformationId) {
        return mappingRepository.findByTransformationId(transformationId)
                .stream()
                .sorted((a, b) -> {
                    // Sort by mappingOrder, then by id as fallback
                    int orderCompare = Integer.compare(
                        a.getMappingOrder() != null ? a.getMappingOrder() : 0,
                        b.getMappingOrder() != null ? b.getMappingOrder() : 0
                    );
                    if (orderCompare != 0) {
                        return orderCompare;
                    }
                    return a.getId().compareTo(b.getId());
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FieldMappingDTO save(FieldMappingDTO mappingDTO) {
        FieldMapping mapping = fromDTO(mappingDTO);
        return toDTO(mappingRepository.save(mapping));
    }

    public Optional<FieldMappingDTO> getById(String id) {
        return mappingRepository.findById(id).map(this::toDTO);
    }

    public void delete(String id) {
        mappingRepository.deleteById(id);
    }

    private FieldMappingDTO toDTO(FieldMapping mapping) {
        FieldMappingDTO dto = new FieldMappingDTO();
        dto.setId(mapping.getId());
        dto.setTransformationId(mapping.getTransformation() != null ? mapping.getTransformation().getId() : null);
        // Convert sourceFields from JSON string to List<String>
        dto.setSourceFields(mapping.getSourceFieldsList());
        dto.setTargetField(mapping.getTargetField());
        dto.setJavaFunction(mapping.getJavaFunction());
        dto.setMappingRule(mapping.getMappingRule());
        dto.setInputTypes(mapping.getInputTypes());
        dto.setOutputType(mapping.getOutputType());
        dto.setDescription(mapping.getDescription());
        dto.setVersion(mapping.getVersion());
        dto.setFunctionName(mapping.getFunctionName());
        dto.setActive(mapping.isActive());
        dto.setArrayMapping(mapping.isArrayMapping());
        dto.setArrayContextPath(mapping.getArrayContextPath());
        dto.setSourceXPath(mapping.getSourceXPath());
        dto.setTargetXPath(mapping.getTargetXPath());
        dto.setMappingOrder(mapping.getMappingOrder());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setUpdatedAt(mapping.getUpdatedAt());
        
        // Note: visualFlowData and functionNode are not in the entity, 
        // they are frontend-specific fields that will be null
        dto.setVisualFlowData(null);
        dto.setFunctionNode(null);
        
        return dto;
    }

    private FieldMapping fromDTO(FieldMappingDTO dto) {
        FieldMapping mapping = new FieldMapping();
        mapping.setId(dto.getId());
        
        // Set transformation if transformationId is provided
        if (dto.getTransformationId() != null) {
            Optional<FlowTransformation> transformation = transformationRepository.findById(dto.getTransformationId());
            transformation.ifPresent(mapping::setTransformation);
        }
        
        // Convert sourceFields from List<String> to JSON string
        mapping.setSourceFieldsList(dto.getSourceFields());
        mapping.setTargetField(dto.getTargetField());
        mapping.setJavaFunction(dto.getJavaFunction());
        mapping.setMappingRule(dto.getMappingRule());
        mapping.setInputTypes(dto.getInputTypes());
        mapping.setOutputType(dto.getOutputType());
        mapping.setDescription(dto.getDescription());
        mapping.setVersion(dto.getVersion());
        mapping.setFunctionName(dto.getFunctionName());
        mapping.setActive(dto.isActive());
        mapping.setArrayMapping(dto.isArrayMapping());
        mapping.setArrayContextPath(dto.getArrayContextPath());
        mapping.setSourceXPath(dto.getSourceXPath());
        mapping.setTargetXPath(dto.getTargetXPath());
        mapping.setMappingOrder(dto.getMappingOrder());
        
        // Note: visualFlowData and functionNode are frontend-specific fields
        // not stored in the entity
        
        return mapping;
    }
}
