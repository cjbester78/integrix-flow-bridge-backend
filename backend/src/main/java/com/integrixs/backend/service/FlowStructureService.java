package com.integrixs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowStructureMessage;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.model.User;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.data.repository.FlowStructureMessageRepository;
import com.integrixs.data.repository.FlowStructureRepository;
import com.integrixs.data.repository.MessageStructureRepository;
import com.integrixs.shared.dto.structure.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowStructureService {
    
    private final FlowStructureRepository flowStructureRepository;
    private final MessageStructureRepository messageStructureRepository;
    private final FlowStructureMessageRepository flowStructureMessageRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final EnvironmentPermissionService environmentPermissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public FlowStructureDTO create(FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Creating flow structure: {}", request.getName());
        
        // Check environment permissions
        environmentPermissionService.checkPermission("dataStructure.create");
        
        // Check if name already exists for business component
        if (flowStructureRepository.existsByNameAndBusinessComponentIdAndIsActiveTrue(
                request.getName(), request.getBusinessComponentId())) {
            throw new RuntimeException("Flow structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        FlowStructure flowStructure = FlowStructure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .processingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()))
                .direction(FlowStructure.Direction.valueOf(request.getDirection().name()))
                .namespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null)
                .metadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null)
                .tags(request.getTags() != null ? serializeToJson(request.getTags()) : null)
                .businessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                        .orElseThrow(() -> new RuntimeException("Business component not found")))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();
        
        flowStructure = flowStructureRepository.save(flowStructure);
        
        // Create flow structure messages
        if (request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
        }
        
        // Generate WSDL
        generateWsdl(flowStructure);
        
        return toDTO(flowStructure);
    }
    
    @Transactional
    public FlowStructureDTO update(String id, FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Updating flow structure: {}", id);
        
        // Check environment permissions
        environmentPermissionService.checkPermission("dataStructure.create");
        
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        
        // Check if name is being changed and already exists
        if (!flowStructure.getName().equals(request.getName()) &&
                flowStructureRepository.existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(
                        request.getName(), request.getBusinessComponentId(), id)) {
            throw new RuntimeException("Flow structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        flowStructure.setName(request.getName());
        flowStructure.setDescription(request.getDescription());
        flowStructure.setProcessingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()));
        flowStructure.setDirection(FlowStructure.Direction.valueOf(request.getDirection().name()));
        flowStructure.setNamespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null);
        flowStructure.setMetadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null);
        flowStructure.setTags(request.getTags() != null ? serializeToJson(request.getTags()) : null);
        flowStructure.setBusinessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                .orElseThrow(() -> new RuntimeException("Business component not found")));
        flowStructure.setUpdatedBy(currentUser);
        flowStructure.setVersion(flowStructure.getVersion() + 1);
        
        // Update flow structure messages
        flowStructureMessageRepository.deleteByFlowStructureId(id);
        if (request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
        }
        
        // Regenerate WSDL
        generateWsdl(flowStructure);
        
        flowStructure = flowStructureRepository.save(flowStructure);
        return toDTO(flowStructure);
    }
    
    @Transactional(readOnly = true)
    public FlowStructureDTO findById(String id) {
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        return toDTO(flowStructure);
    }
    
    @Transactional(readOnly = true)
    public Page<FlowStructureDTO> findAll(String businessComponentId, 
                                         FlowStructureDTO.ProcessingMode processingMode,
                                         FlowStructureDTO.Direction direction,
                                         String search, 
                                         Pageable pageable) {
        FlowStructure.ProcessingMode mode = processingMode != null ? 
                FlowStructure.ProcessingMode.valueOf(processingMode.name()) : null;
        FlowStructure.Direction dir = direction != null ? 
                FlowStructure.Direction.valueOf(direction.name()) : null;
                
        Page<FlowStructure> page = flowStructureRepository.findAllWithFilters(
                businessComponentId, mode, dir, search, pageable);
        return page.map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<FlowStructureDTO> findByBusinessComponent(String businessComponentId) {
        return flowStructureRepository.findByBusinessComponentId(businessComponentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void delete(String id) {
        log.info("Deleting flow structure: {}", id);
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        
        flowStructure.setIsActive(false);
        flowStructureRepository.save(flowStructure);
    }
    
    private void createFlowStructureMessages(FlowStructure flowStructure, 
                                           Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds) {
        for (Map.Entry<FlowStructureMessageDTO.MessageType, String> entry : messageStructureIds.entrySet()) {
            MessageStructure messageStructure = messageStructureRepository.findById(entry.getValue())
                    .orElseThrow(() -> new RuntimeException("Message structure not found: " + entry.getValue()));
            
            FlowStructureMessage flowMessage = FlowStructureMessage.builder()
                    .flowStructure(flowStructure)
                    .messageType(FlowStructureMessage.MessageType.valueOf(entry.getKey().name()))
                    .messageStructure(messageStructure)
                    .build();
            
            flowStructureMessageRepository.save(flowMessage);
        }
    }
    
    private void generateWsdl(FlowStructure flowStructure) {
        // TODO: Implement WSDL generation based on processing mode and direction
        // For now, set a placeholder
        flowStructure.setWsdlContent("<!-- WSDL generation pending implementation -->");
    }
    
    private FlowStructureDTO toDTO(FlowStructure entity) {
        try {
            Set<FlowStructureMessageDTO> messages = entity.getFlowStructureMessages() != null ?
                    entity.getFlowStructureMessages().stream()
                            .map(this::toFlowStructureMessageDTO)
                            .collect(Collectors.toSet()) : new HashSet<>();
            
            return FlowStructureDTO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .processingMode(FlowStructureDTO.ProcessingMode.valueOf(entity.getProcessingMode().name()))
                    .direction(FlowStructureDTO.Direction.valueOf(entity.getDirection().name()))
                    .wsdlContent(entity.getWsdlContent())
                    .namespace(entity.getNamespace() != null ? 
                            objectMapper.readValue(entity.getNamespace(), new TypeReference<Map<String, Object>>() {}) : null)
                    .metadata(entity.getMetadata() != null ? 
                            objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {}) : null)
                    .tags(entity.getTags() != null ? 
                            objectMapper.readValue(entity.getTags(), new TypeReference<Set<String>>() {}) : null)
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .businessComponent(toBusinessComponentDTO(entity.getBusinessComponent()))
                    .flowStructureMessages(messages)
                    .createdBy(toUserDTO(entity.getCreatedBy()))
                    .updatedBy(toUserDTO(entity.getUpdatedBy()))
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting FlowStructure to DTO", e);
            throw new RuntimeException("Error converting FlowStructure to DTO", e);
        }
    }
    
    private FlowStructureMessageDTO toFlowStructureMessageDTO(FlowStructureMessage entity) {
        return FlowStructureMessageDTO.builder()
                .flowStructureId(entity.getFlowStructure().getId())
                .messageType(FlowStructureMessageDTO.MessageType.valueOf(entity.getMessageType().name()))
                .messageStructure(toMessageStructureDTO(entity.getMessageStructure()))
                .build();
    }
    
    private MessageStructureDTO toMessageStructureDTO(MessageStructure entity) {
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
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting MessageStructure to DTO", e);
            throw new RuntimeException("Error converting MessageStructure to DTO", e);
        }
    }
    
    private BusinessComponentDTO toBusinessComponentDTO(com.integrixs.data.model.BusinessComponent entity) {
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