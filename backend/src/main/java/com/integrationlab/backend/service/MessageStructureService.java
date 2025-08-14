package com.integrationlab.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.data.model.MessageStructure;
import com.integrationlab.data.model.User;
import com.integrationlab.data.repository.BusinessComponentRepository;
import com.integrationlab.data.repository.MessageStructureRepository;
import com.integrationlab.shared.dto.structure.MessageStructureCreateRequestDTO;
import com.integrationlab.shared.dto.structure.MessageStructureDTO;
import com.integrationlab.shared.dto.business.BusinessComponentDTO;
import com.integrationlab.shared.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageStructureService {
    
    private final MessageStructureRepository messageStructureRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public MessageStructureDTO create(MessageStructureCreateRequestDTO request, User currentUser) {
        log.info("Creating message structure: {}", request.getName());
        
        // Check if name already exists for business component
        if (messageStructureRepository.existsByNameAndBusinessComponentIdAndIsActiveTrue(
                request.getName(), request.getBusinessComponentId())) {
            throw new RuntimeException("Message structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        MessageStructure messageStructure = MessageStructure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .xsdContent(request.getXsdContent())
                .namespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null)
                .metadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null)
                .tags(request.getTags() != null ? serializeToJson(request.getTags()) : null)
                .businessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                        .orElseThrow(() -> new RuntimeException("Business component not found")))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();
        
        messageStructure = messageStructureRepository.save(messageStructure);
        return toDTO(messageStructure);
    }
    
    @Transactional
    public MessageStructureDTO update(String id, MessageStructureCreateRequestDTO request, User currentUser) {
        log.info("Updating message structure: {}", id);
        
        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Message structure not found"));
        
        // Check if name is being changed and already exists
        if (!messageStructure.getName().equals(request.getName()) &&
                messageStructureRepository.existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(
                        request.getName(), request.getBusinessComponentId(), id)) {
            throw new RuntimeException("Message structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        messageStructure.setName(request.getName());
        messageStructure.setDescription(request.getDescription());
        messageStructure.setXsdContent(request.getXsdContent());
        messageStructure.setNamespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null);
        messageStructure.setMetadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null);
        messageStructure.setTags(request.getTags() != null ? serializeToJson(request.getTags()) : null);
        messageStructure.setBusinessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                .orElseThrow(() -> new RuntimeException("Business component not found")));
        messageStructure.setUpdatedBy(currentUser);
        messageStructure.setVersion(messageStructure.getVersion() + 1);
        
        messageStructure = messageStructureRepository.save(messageStructure);
        return toDTO(messageStructure);
    }
    
    @Transactional(readOnly = true)
    public MessageStructureDTO findById(String id) {
        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Message structure not found"));
        return toDTO(messageStructure);
    }
    
    @Transactional(readOnly = true)
    public Page<MessageStructureDTO> findAll(String businessComponentId, String search, Pageable pageable) {
        Page<MessageStructure> page = messageStructureRepository.findAllWithFilters(
                businessComponentId, search, pageable);
        return page.map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<MessageStructureDTO> findByBusinessComponent(String businessComponentId) {
        return messageStructureRepository.findByBusinessComponentId(businessComponentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void delete(String id) {
        log.info("Deleting message structure: {}", id);
        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Message structure not found"));
        
        messageStructure.setIsActive(false);
        messageStructureRepository.save(messageStructure);
    }
    
    private MessageStructureDTO toDTO(MessageStructure entity) {
        try {
            return MessageStructureDTO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .xsdContent(entity.getXsdContent())
                    .namespace(entity.getNamespace() != null ? 
                            objectMapper.readValue(entity.getNamespace(), new TypeReference<Map<String, Object>>() {}) : null)
                    .metadata(entity.getMetadata() != null ? 
                            objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {}) : null)
                    .tags(entity.getTags() != null ? 
                            objectMapper.readValue(entity.getTags(), new TypeReference<Set<String>>() {}) : null)
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .businessComponent(toBusinessComponentDTO(entity.getBusinessComponent()))
                    .createdBy(toUserDTO(entity.getCreatedBy()))
                    .updatedBy(toUserDTO(entity.getUpdatedBy()))
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting MessageStructure to DTO", e);
            throw new RuntimeException("Error converting MessageStructure to DTO", e);
        }
    }
    
    private BusinessComponentDTO toBusinessComponentDTO(com.integrationlab.data.model.BusinessComponent entity) {
        if (entity == null) return null;
        return BusinessComponentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
    
    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
    
    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializing to JSON", e);
            throw new RuntimeException("Error serializing to JSON", e);
        }
    }
}