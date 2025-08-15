package com.integrixs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.dto.XsdImportResult;
import com.integrixs.backend.dto.XsdValidationResult;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.model.User;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.data.repository.MessageStructureRepository;
import com.integrixs.shared.dto.structure.MessageStructureCreateRequestDTO;
import com.integrixs.shared.dto.structure.MessageStructureDTO;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    
    public List<XsdValidationResult> validateXsdFiles(List<MultipartFile> files) {
        List<XsdValidationResult> results = new ArrayList<>();
        Map<String, String> fileContents = new HashMap<>();
        
        // First, read all files
        for (MultipartFile file : files) {
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                fileContents.put(file.getOriginalFilename(), content);
            } catch (Exception e) {
                results.add(XsdValidationResult.builder()
                        .fileName(file.getOriginalFilename())
                        .valid(false)
                        .errors(Arrays.asList("Failed to read file: " + e.getMessage()))
                        .build());
            }
        }
        
        // Then validate each file and check dependencies
        for (MultipartFile file : files) {
            if (!fileContents.containsKey(file.getOriginalFilename())) {
                continue; // Already reported as error
            }
            
            XsdValidationResult result = XsdValidationResult.builder()
                    .fileName(file.getOriginalFilename())
                    .valid(true)
                    .errors(new ArrayList<>())
                    .dependencies(new ArrayList<>())
                    .resolvedDependencies(new ArrayList<>())
                    .missingDependencies(new ArrayList<>())
                    .build();
            
            try {
                String content = fileContents.get(file.getOriginalFilename());
                
                // Parse XSD to check validity and extract dependencies
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
                
                // Extract imports and includes
                NodeList imports = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
                NodeList includes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
                
                List<String> dependencies = new ArrayList<>();
                
                for (int i = 0; i < imports.getLength(); i++) {
                    var schemaLocationAttr = imports.item(i).getAttributes().getNamedItem("schemaLocation");
                    if (schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        if (schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                        }
                    }
                }
                
                for (int i = 0; i < includes.getLength(); i++) {
                    var schemaLocationAttr = includes.item(i).getAttributes().getNamedItem("schemaLocation");
                    if (schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        if (schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                        }
                    }
                }
                
                result.setDependencies(dependencies);
                
                // Check which dependencies are resolved in the current batch
                for (String dep : dependencies) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    if (fileContents.containsKey(depFileName)) {
                        result.getResolvedDependencies().add(dep);
                    } else {
                        result.getMissingDependencies().add(dep);
                        result.setValid(false);
                        result.getErrors().add("Missing dependency: " + dep);
                    }
                }
                
            } catch (Exception e) {
                result.setValid(false);
                result.getErrors().add("XML parsing error: " + e.getMessage());
            }
            
            results.add(result);
        }
        
        return results;
    }
    
    @Transactional
    public List<XsdImportResult> importXsdFiles(List<MultipartFile> files, User currentUser) {
        List<XsdImportResult> results = new ArrayList<>();
        
        // First validate all files
        List<XsdValidationResult> validationResults = validateXsdFiles(files);
        
        // Group files by validation status
        Map<String, XsdValidationResult> validationMap = validationResults.stream()
                .collect(Collectors.toMap(XsdValidationResult::getFileName, v -> v));
        
        // Import valid files in dependency order
        Set<String> imported = new HashSet<>();
        boolean progress = true;
        
        while (progress) {
            progress = false;
            
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                
                if (imported.contains(fileName)) {
                    continue;
                }
                
                XsdValidationResult validation = validationMap.get(fileName);
                if (validation == null || !validation.isValid()) {
                    continue;
                }
                
                // Check if all dependencies are imported
                boolean allDepsImported = true;
                for (String dep : validation.getResolvedDependencies()) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    if (!imported.contains(depFileName)) {
                        allDepsImported = false;
                        break;
                    }
                }
                
                if (!allDepsImported) {
                    continue;
                }
                
                // Import this file
                try {
                    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                    String structureName = fileName.replace(".xsd", "");
                    
                    // Check if already exists
                    if (messageStructureRepository.existsByNameAndIsActiveTrue(structureName)) {
                        results.add(XsdImportResult.builder()
                                .fileName(fileName)
                                .structureName(structureName)
                                .success(false)
                                .message("Message structure with this name already exists")
                                .build());
                    } else {
                        // Get the first active business component (or create a default one)
                        BusinessComponent businessComponent = businessComponentRepository.findAll().stream()
                                .filter(bc -> bc.getIsActive() != null && bc.getIsActive())
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No active business component found"));
                        
                        // Create message structure
                        MessageStructure messageStructure = MessageStructure.builder()
                                .name(structureName)
                                .description("Imported from " + fileName)
                                .xsdContent(content)
                                .businessComponent(businessComponent)
                                .metadata(serializeToJson(Map.of("importedFrom", fileName, "importedAt", new Date())))
                                .createdBy(currentUser)
                                .updatedBy(currentUser)
                                .build();
                        
                        messageStructureRepository.save(messageStructure);
                        
                        results.add(XsdImportResult.builder()
                                .fileName(fileName)
                                .structureName(structureName)
                                .success(true)
                                .build());
                        
                        imported.add(fileName);
                        progress = true;
                    }
                } catch (Exception e) {
                    results.add(XsdImportResult.builder()
                            .fileName(fileName)
                            .success(false)
                            .message("Import failed: " + e.getMessage())
                            .build());
                }
            }
        }
        
        // Report files that couldn't be imported
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (!imported.contains(fileName) && !results.stream().anyMatch(r -> r.getFileName().equals(fileName))) {
                XsdValidationResult validation = validationMap.get(fileName);
                String message = validation != null && !validation.isValid() 
                        ? "Validation failed: " + String.join(", ", validation.getErrors())
                        : "Could not import due to unresolved dependencies";
                        
                results.add(XsdImportResult.builder()
                        .fileName(fileName)
                        .success(false)
                        .message(message)
                        .build());
            }
        }
        
        return results;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class XsdValidationResult {
        private String fileName;
        private boolean valid;
        private List<String> errors;
        private List<String> dependencies;
        private List<String> resolvedDependencies;
        private List<String> missingDependencies;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class XsdImportResult {
        private String fileName;
        private String structureName;
        private boolean success;
        private String message;
    }
}