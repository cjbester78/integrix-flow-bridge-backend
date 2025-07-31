package com.integrationlab.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integrationlab.model.FieldMapping;
import com.integrationlab.model.FlowTransformation;
import com.integrationlab.repository.FieldMappingRepository;
import com.integrationlab.repository.FlowTransformationRepository;
import com.integrationlab.shared.dto.mapping.FieldMappingDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieldMappingService {

    @Autowired
    private FieldMappingRepository mappingRepository;
    
    @Autowired
    private FlowTransformationRepository transformationRepository;

    public List<FieldMappingDTO> getByTransformationId(String transformationId) {
        return mappingRepository.findByTransformationId(transformationId)
                .stream()
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
        dto.setSourceFields(mapping.getSourceFields());
        dto.setTargetField(mapping.getTargetField());
        dto.setJavaFunction(mapping.getJavaFunction());
        dto.setMappingRule(mapping.getMappingRule());
        dto.setInputTypes(mapping.getInputTypes());
        dto.setOutputType(mapping.getOutputType());
        dto.setDescription(mapping.getDescription());
        dto.setVersion(mapping.getVersion());
        dto.setFunctionName(mapping.getFunctionName());
        dto.setActive(mapping.isActive());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setUpdatedAt(mapping.getUpdatedAt());
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
        
        mapping.setSourceFields(dto.getSourceFields());
        mapping.setTargetField(dto.getTargetField());
        mapping.setJavaFunction(dto.getJavaFunction());
        mapping.setMappingRule(dto.getMappingRule());
        mapping.setInputTypes(dto.getInputTypes());
        mapping.setOutputType(dto.getOutputType());
        mapping.setDescription(dto.getDescription());
        mapping.setVersion(dto.getVersion());
        mapping.setFunctionName(dto.getFunctionName());
        mapping.setActive(dto.isActive());
        return mapping;
    }
}
