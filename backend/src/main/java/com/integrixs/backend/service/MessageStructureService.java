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
                .sourceType("INTERNAL")
                .isEditable(true)
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
        
        // Check if structure is editable
        if (!messageStructure.getIsEditable()) {
            throw new RuntimeException("Cannot edit external message structure. This structure is read-only.");
        }
        
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
                    .sourceType(entity.getSourceType())
                    .isEditable(entity.getIsEditable())
                    .isActive(entity.getIsActive())
                    .importMetadata(entity.getImportMetadata() != null ?
                            objectMapper.readValue(entity.getImportMetadata(), new TypeReference<Map<String, Object>>() {}) : null)
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
        log.info("=== Starting XSD validation for {} files ===", files.size());
        List<XsdValidationResult> results = new ArrayList<>();
        Map<String, String> fileContents = new HashMap<>();
        
        // First, read all files
        log.info("Step 1: Reading all files into memory");
        for (MultipartFile file : files) {
            log.info("  Reading file: {}", file.getOriginalFilename());
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                fileContents.put(file.getOriginalFilename(), content);
                log.info("  ✓ Successfully read file: {} ({} bytes)", file.getOriginalFilename(), content.length());
            } catch (Exception e) {
                log.error("  ✗ Failed to read file: {}", file.getOriginalFilename(), e);
                results.add(XsdValidationResult.builder()
                        .fileName(file.getOriginalFilename())
                        .valid(false)
                        .errors(Arrays.asList("Failed to read file: " + e.getMessage()))
                        .build());
            }
        }
        
        log.info("Successfully read {} files into memory", fileContents.size());
        
        // Then validate each file and check dependencies
        log.info("Step 2: Validating each file and checking dependencies");
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            log.info("  Validating file: {}", fileName);
            
            if (!fileContents.containsKey(fileName)) {
                log.info("  → Skipping (already reported as error)");
                continue; // Already reported as error
            }
            
            XsdValidationResult result = XsdValidationResult.builder()
                    .fileName(fileName)
                    .valid(true)
                    .errors(new ArrayList<>())
                    .dependencies(new ArrayList<>())
                    .resolvedDependencies(new ArrayList<>())
                    .missingDependencies(new ArrayList<>())
                    .build();
            
            try {
                String content = fileContents.get(fileName);
                log.info("  → Parsing XSD content ({} chars)", content.length());
                
                // Parse XSD to check validity and extract dependencies
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false); // Don't validate against DTD
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                
                // Set custom error handler to capture parsing errors
                builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                    @Override
                    public void warning(org.xml.sax.SAXParseException e) {
                        log.warn("  → XML Warning: {}", e.getMessage());
                    }
                    
                    @Override
                    public void error(org.xml.sax.SAXParseException e) {
                        log.error("  → XML Error: {}", e.getMessage());
                        result.setValid(false);
                        result.getErrors().add("XML Error: " + e.getMessage());
                    }
                    
                    @Override
                    public void fatalError(org.xml.sax.SAXParseException e) {
                        log.error("  → XML Fatal Error: {}", e.getMessage());
                        result.setValid(false);
                        result.getErrors().add("XML Fatal Error: " + e.getMessage());
                    }
                });
                
                Document doc = builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
                log.info("  → Successfully parsed XML document");
                
                // Extract imports and includes
                NodeList imports = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
                NodeList includes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
                
                log.info("  → Found {} imports and {} includes", imports.getLength(), includes.getLength());
                
                List<String> dependencies = new ArrayList<>();
                
                for (int i = 0; i < imports.getLength(); i++) {
                    var schemaLocationAttr = imports.item(i).getAttributes().getNamedItem("schemaLocation");
                    if (schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        log.info("    → Import schemaLocation: {}", schemaLocation);
                        if (schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                            log.info("    → Added as dependency: {}", schemaLocation);
                        }
                    }
                }
                
                for (int i = 0; i < includes.getLength(); i++) {
                    var schemaLocationAttr = includes.item(i).getAttributes().getNamedItem("schemaLocation");
                    if (schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        log.info("    → Include schemaLocation: {}", schemaLocation);
                        if (schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                            log.info("    → Added as dependency: {}", schemaLocation);
                        }
                    }
                }
                
                result.setDependencies(dependencies);
                log.info("  → Total dependencies found: {}", dependencies.size());
                
                // Check which dependencies are resolved in the current batch or already exist
                log.info("  → Checking dependency resolution...");
                for (String dep : dependencies) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    String depStructureName = depFileName.replace(".xsd", "");
                    log.info("    → Checking dependency: {} (file: {}, structure: {})", dep, depFileName, depStructureName);
                    
                    if (fileContents.containsKey(depFileName)) {
                        // Dependency is in current batch
                        result.getResolvedDependencies().add(dep);
                        log.info("      ✓ Found in current batch");
                    } else if (messageStructureRepository.existsByNameAndIsActiveTrue(depStructureName)) {
                        // Dependency already exists in database
                        result.getResolvedDependencies().add(dep);
                        log.info("      ✓ Found in database");
                    } else {
                        // Dependency is truly missing
                        result.getMissingDependencies().add(dep);
                        log.info("      ✗ NOT FOUND - marked as missing");
                    }
                }
                
                // Only mark as invalid if there are truly missing dependencies
                if (!result.getMissingDependencies().isEmpty()) {
                    result.setValid(false);
                    String errorMsg = "Missing dependencies not found in batch or database: " + String.join(", ", result.getMissingDependencies());
                    result.getErrors().add(errorMsg);
                    log.error("  ✗ Validation FAILED: {}", errorMsg);
                } else {
                    log.info("  ✓ Validation PASSED");
                }
                
            } catch (Exception e) {
                result.setValid(false);
                result.getErrors().add("XML parsing error: " + e.getMessage());
                log.error("  ✗ Error validating XSD: {}", e.getMessage(), e);
            }
            
            results.add(result);
            log.info("  → Validation result: valid={}, errors={}, dependencies={}, resolved={}, missing={}", 
                     result.isValid(), 
                     result.getErrors().size(), 
                     result.getDependencies().size(),
                     result.getResolvedDependencies().size(),
                     result.getMissingDependencies().size());
        }
        
        log.info("=== XSD validation completed. Total results: {} ===", results.size());
        return results;
    }
    
    @Transactional
    public List<XsdImportResult> importXsdFiles(List<MultipartFile> files, String businessComponentId, User currentUser) {
        log.info("=== Starting XSD import for {} files with business component: {} ===", files.size(), businessComponentId);
        List<XsdImportResult> results = new ArrayList<>();
        
        // Get business component
        BusinessComponent businessComponent = businessComponentRepository.findById(businessComponentId)
                .orElseThrow(() -> new RuntimeException("Business component not found: " + businessComponentId));
        
        // First validate all files
        List<XsdValidationResult> validationResults = validateXsdFiles(files);
        
        // Group files by validation status
        Map<String, XsdValidationResult> validationMap = validationResults.stream()
                .collect(Collectors.toMap(XsdValidationResult::getFileName, v -> v));
        
        // Log validation summary
        long validCount = validationResults.stream().filter(XsdValidationResult::isValid).count();
        long invalidCount = validationResults.size() - validCount;
        log.info("Validation summary: {} valid, {} invalid", validCount, invalidCount);
        
        // Import valid files in dependency order
        Set<String> imported = new HashSet<>();
        boolean progress = true;
        int iteration = 0;
        
        log.info("Starting dependency-ordered import...");
        while (progress) {
            progress = false;
            iteration++;
            log.info("Import iteration #{}", iteration);
            
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                
                if (imported.contains(fileName)) {
                    log.debug("  → {} already imported, skipping", fileName);
                    continue;
                }
                
                XsdValidationResult validation = validationMap.get(fileName);
                if (validation == null || !validation.isValid()) {
                    log.debug("  → {} is invalid, skipping", fileName);
                    continue;
                }
                
                // Check if all dependencies are imported or already exist
                boolean allDepsAvailable = true;
                for (String dep : validation.getDependencies()) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    String depStructureName = depFileName.replace(".xsd", "");
                    
                    // Check if dependency is already imported in this batch or exists in DB
                    if (!imported.contains(depFileName) && 
                        !messageStructureRepository.existsByNameAndIsActiveTrue(depStructureName)) {
                        allDepsAvailable = false;
                        break;
                    }
                }
                
                if (!allDepsAvailable) {
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
                        // Create message structure
                        Map<String, Object> importMetadata = new HashMap<>();
                        importMetadata.put("originalFileName", fileName);
                        importMetadata.put("importedAt", new Date());
                        importMetadata.put("importedBy", currentUser.getUsername());
                        
                        MessageStructure messageStructure = MessageStructure.builder()
                                .name(structureName)
                                .description("Imported from " + fileName)
                                .xsdContent(content)
                                .sourceType("EXTERNAL")
                                .isEditable(false)
                                .businessComponent(businessComponent)
                                .metadata(serializeToJson(Map.of("importedFrom", fileName, "importedAt", new Date())))
                                .importMetadata(serializeToJson(importMetadata))
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