package com.integrationlab.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.integrationlab.backend.exception.BusinessException;
import com.integrationlab.backend.util.XsdDependencyResolver;
import com.integrationlab.data.model.BusinessComponent;
import com.integrationlab.data.model.DataStructure;
import com.integrationlab.data.model.User;
import com.integrationlab.data.repository.BusinessComponentRepository;
import com.integrationlab.data.repository.DataStructureRepository;
import com.integrationlab.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Slf4j
@Service
public class DataStructureService {
    
    private final DataStructureRepository dataStructureRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final UserRepository userRepository;
    private final EnvironmentPermissionService environmentPermissionService;
    private final ObjectMapper objectMapper;
    private final XsdDependencyResolver xsdDependencyResolver;
    
    public DataStructureService(DataStructureRepository dataStructureRepository,
                               BusinessComponentRepository businessComponentRepository,
                               UserRepository userRepository,
                               EnvironmentPermissionService environmentPermissionService,
                               XsdDependencyResolver xsdDependencyResolver) {
        this.dataStructureRepository = dataStructureRepository;
        this.businessComponentRepository = businessComponentRepository;
        this.userRepository = userRepository;
        this.environmentPermissionService = environmentPermissionService;
        this.xsdDependencyResolver = xsdDependencyResolver;
        
        // Configure ObjectMapper to preserve field order using LinkedHashMap
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        this.objectMapper.setNodeFactory(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance);
    }
    
    /**
     * Get all data structures with filtering
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDataStructures(String type, String usage, String search, 
                                                List<String> tags, int page, int limit,
                                                String businessComponentId) {
        try {
            // Create pageable
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
            
            // Convert usage string to enum
            DataStructure.DataStructureUsage usageEnum = null;
            if (usage != null && !usage.isEmpty()) {
                try {
                    usageEnum = DataStructure.DataStructureUsage.valueOf(usage.toLowerCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid usage value: {}", usage);
                }
            }
            
            // Get filtered data structures
            Page<DataStructure> structurePage = dataStructureRepository.findWithFilters(
                type, usageEnum, businessComponentId, search, pageable
            );
            
            // Convert to response format
            List<Map<String, Object>> structures = structurePage.getContent().stream()
                .map(this::convertToMap)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("structures", structures);
            response.put("total", structurePage.getTotalElements());
            response.put("totalPages", structurePage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", limit);
            
            return response;
        } catch (Exception e) {
            log.error("Error getting data structures", e);
            throw new BusinessException("Failed to retrieve data structures: " + e.getMessage());
        }
    }
    
    /**
     * Get a specific data structure by ID
     */
    @Transactional(readOnly = true)
    public DataStructure getDataStructure(String id) {
        return dataStructureRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Data structure not found: " + id));
    }
    
    /**
     * Get a specific data structure by ID as Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDataStructureAsMap(String id) {
        DataStructure dataStructure = getDataStructure(id);
        return convertToMap(dataStructure);
    }
    
    /**
     * Create a new data structure
     */
    @Transactional
    public DataStructure createDataStructure(Map<String, Object> structureData, String userId) {
        environmentPermissionService.checkPermission("dataStructure.create");
        
        try {
            // Validate required fields
            String name = (String) structureData.get("name");
            if (name == null || name.trim().isEmpty()) {
                throw new BusinessException("Name is required");
            }
            
            // Check if name already exists
            if (dataStructureRepository.findByName(name).isPresent()) {
                throw new BusinessException("Data structure with name '" + name + "' already exists");
            }
            
            // Get the type and original content
            String type = (String) structureData.getOrDefault("type", "json");
            String originalContent = (String) structureData.get("originalContent");
            String originalFormat = (String) structureData.get("originalFormat");
            
            // If no original format specified, derive from type
            if (originalFormat == null) {
                originalFormat = deriveFormatFromType(type);
            }
            
            // Extract dependencies for XSD/WSDL types
            XsdDependencyResolver.DependencyMetadata dependencyMetadata = null;
            if ("xsd".equalsIgnoreCase(type) && originalContent != null) {
                dependencyMetadata = xsdDependencyResolver.extractDependencies(originalContent);
                xsdDependencyResolver.resolveDependencies(dependencyMetadata, null);
                log.info("Extracted {} imports and {} includes from XSD", 
                    dependencyMetadata.getImports().size(), 
                    dependencyMetadata.getIncludes().size());
            } else if ("wsdl".equalsIgnoreCase(type) && originalContent != null) {
                dependencyMetadata = xsdDependencyResolver.extractWsdlDependencies(originalContent);
                xsdDependencyResolver.resolveDependencies(dependencyMetadata, null);
                log.info("Extracted {} imports from WSDL", dependencyMetadata.getImports().size());
            }
            
            // Get the structure object
            Object structureObj = structureData.get("structure");
            log.info("Structure type: {}, structureObj class: {}", type, structureObj != null ? structureObj.getClass().getName() : "null");
            
            // If structure is a string, parse it first
            if (structureObj instanceof String) {
                try {
                    structureObj = objectMapper.readValue((String) structureObj, new TypeReference<java.util.LinkedHashMap<String, Object>>() {});
                    log.info("Parsed structure from string");
                } catch (Exception e) {
                    log.warn("Failed to parse structure string: {}", e.getMessage());
                }
            }
            
            // If this is a WSDL type and we have original content, reorder fields
            if ("wsdl".equalsIgnoreCase(type) && originalContent != null && structureObj instanceof Map) {
                log.info("Reordering WSDL structure fields based on original content");
                log.debug("Original structure: {}", structureObj);
                structureObj = reorderStructureFromWsdl((Map<String, Object>) structureObj, originalContent);
                log.debug("Reordered structure: {}", structureObj);
            } else {
                log.info("Not reordering: type={}, hasOriginalContent={}, isMap={}", 
                    type, originalContent != null, structureObj instanceof Map);
            }
            
            // Prepare metadata with dependencies
            Map<String, Object> metadata = structureData.get("metadata") != null ? 
                (Map<String, Object>) structureData.get("metadata") : new HashMap<>();
            
            // Add dependency information to metadata if present
            if (dependencyMetadata != null) {
                metadata.put("dependencies", dependencyMetadata);
            }
            
            // Process resolved dependency files from frontend
            List<Map<String, Object>> resolvedFiles = null;
            if (metadata.containsKey("resolvedFiles")) {
                resolvedFiles = (List<Map<String, Object>>) metadata.get("resolvedFiles");
                
                // Save each dependency file as a separate data structure
                List<String> savedDependencyIds = new ArrayList<>();
                for (Map<String, Object> depFile : resolvedFiles) {
                    String depName = (String) depFile.get("name");
                    String depType = (String) depFile.get("type");
                    String depContent = (String) depFile.get("content");
                    
                    // Skip if this is the primary file
                    if (depName.equals(name) || (depContent != null && depContent.equals(originalContent))) {
                        continue;
                    }
                    
                    // Check if a structure with this name already exists
                    Optional<DataStructure> existing = dataStructureRepository.findByName(depName);
                    if (!existing.isPresent()) {
                        // Create a new data structure for the dependency
                        Map<String, Object> depStructureData = new HashMap<>();
                        depStructureData.put("name", depName);
                        depStructureData.put("type", depType);
                        depStructureData.put("description", "Dependency of " + name);
                        depStructureData.put("usage", structureData.get("usage"));
                        depStructureData.put("originalContent", depContent);
                        depStructureData.put("originalFormat", "xml");
                        depStructureData.put("businessComponentId", structureData.get("businessComponentId"));
                        
                        // Parse structure from content
                        Object depStructureObj = null;
                        if ("xsd".equalsIgnoreCase(depType)) {
                            depStructureObj = Map.of("message", "XSD dependency");
                        } else if ("wsdl".equalsIgnoreCase(depType)) {
                            depStructureObj = Map.of("message", "WSDL dependency");
                        }
                        depStructureData.put("structure", depStructureObj);
                        
                        try {
                            DataStructure depStructure = createDataStructure(depStructureData, userId);
                            savedDependencyIds.add(depStructure.getId());
                            log.info("Saved dependency structure: {} with ID: {}", depName, depStructure.getId());
                        } catch (Exception e) {
                            log.warn("Failed to save dependency structure {}: {}", depName, e.getMessage());
                        }
                    } else {
                        savedDependencyIds.add(existing.get().getId());
                        log.info("Dependency structure already exists: {} with ID: {}", depName, existing.get().getId());
                    }
                }
                
                // Update metadata with saved dependency IDs
                metadata.put("savedDependencyIds", savedDependencyIds);
                
                // Update dependency metadata with resolved structure IDs
                if (dependencyMetadata != null) {
                    xsdDependencyResolver.resolveDependencies(dependencyMetadata, null);
                }
            }
            
            // Build the data structure
            DataStructure dataStructure = DataStructure.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .type(type)
                .description((String) structureData.get("description"))
                .usage(parseUsage((String) structureData.get("usage")))
                .structure(convertToJson(structureObj))
                .originalContent(originalContent)
                .originalFormat(originalFormat)
                .namespace(convertToJson(structureData.get("namespace")))
                .metadata(convertToJson(metadata))
                .tags(convertToJson(structureData.get("tags")))
                .version(1)
                .isActive(true)
                .build();
            
            // Set business component if provided
            String businessComponentId = (String) structureData.get("businessComponentId");
            if (businessComponentId != null) {
                BusinessComponent businessComponent = businessComponentRepository.findById(businessComponentId)
                    .orElseThrow(() -> new BusinessException("Business component not found: " + businessComponentId));
                dataStructure.setBusinessComponent(businessComponent);
            }
            
            // Set created by - try to find by username first, then by ID
            User user = userRepository.findByUsername(userId);
            if (user == null) {
                // Try finding by ID if username lookup failed
                user = userRepository.findById(userId).orElse(null);
            }
            
            if (user != null) {
                dataStructure.setCreatedBy(user);
            } else {
                log.warn("User not found for ID/username: {}, proceeding without setting createdBy", userId);
            }
            
            // Save and return
            DataStructure saved = dataStructureRepository.save(dataStructure);
            log.info("Created data structure: {} by user: {}", saved.getName(), userId);
            
            return saved;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating data structure", e);
            throw new BusinessException("Failed to create data structure: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing data structure
     */
    @Transactional
    public DataStructure updateDataStructure(String id, Map<String, Object> updates) {
        environmentPermissionService.checkPermission("dataStructure.update");
        
        try {
            DataStructure dataStructure = getDataStructure(id);
            
            // Update fields if provided
            if (updates.containsKey("name")) {
                String newName = (String) updates.get("name");
                // Check if new name already exists (excluding current structure)
                if (dataStructureRepository.existsByNameAndIdNot(newName, id)) {
                    throw new BusinessException("Data structure with name '" + newName + "' already exists");
                }
                dataStructure.setName(newName);
            }
            
            if (updates.containsKey("type")) {
                dataStructure.setType((String) updates.get("type"));
            }
            
            if (updates.containsKey("description")) {
                dataStructure.setDescription((String) updates.get("description"));
            }
            
            if (updates.containsKey("usage")) {
                dataStructure.setUsage(parseUsage((String) updates.get("usage")));
            }
            
            if (updates.containsKey("structure")) {
                dataStructure.setStructure(convertToJson(updates.get("structure")));
            }
            
            if (updates.containsKey("originalContent")) {
                dataStructure.setOriginalContent((String) updates.get("originalContent"));
            }
            
            if (updates.containsKey("originalFormat")) {
                dataStructure.setOriginalFormat((String) updates.get("originalFormat"));
            }
            
            if (updates.containsKey("namespace")) {
                dataStructure.setNamespace(convertToJson(updates.get("namespace")));
            }
            
            if (updates.containsKey("metadata")) {
                dataStructure.setMetadata(convertToJson(updates.get("metadata")));
            }
            
            if (updates.containsKey("tags")) {
                dataStructure.setTags(convertToJson(updates.get("tags")));
            }
            
            if (updates.containsKey("isActive")) {
                dataStructure.setIsActive((Boolean) updates.get("isActive"));
            }
            
            // Increment version
            dataStructure.setVersion(dataStructure.getVersion() + 1);
            
            // Save and return
            DataStructure saved = dataStructureRepository.save(dataStructure);
            log.info("Updated data structure: {}", saved.getName());
            
            return saved;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating data structure", e);
            throw new BusinessException("Failed to update data structure: " + e.getMessage());
        }
    }
    
    /**
     * Delete a data structure
     */
    @Transactional
    public void deleteDataStructure(String id) {
        environmentPermissionService.checkPermission("dataStructure.delete");
        
        try {
            if (!dataStructureRepository.existsById(id)) {
                throw new BusinessException("Data structure not found: " + id);
            }
            
            dataStructureRepository.deleteById(id);
            log.info("Deleted data structure: {}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting data structure", e);
            throw new BusinessException("Failed to delete data structure: " + e.getMessage());
        }
    }
    
    /**
     * Convert DataStructure entity to Map for API response
     */
    private Map<String, Object> convertToMap(DataStructure dataStructure) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dataStructure.getId());
        map.put("name", dataStructure.getName());
        map.put("type", dataStructure.getType());
        map.put("description", dataStructure.getDescription());
        map.put("usage", dataStructure.getUsage().name());
        map.put("structure", parseJsonString(dataStructure.getStructure()));
        map.put("originalContent", dataStructure.getOriginalContent());
        map.put("originalFormat", dataStructure.getOriginalFormat());
        map.put("namespace", parseJsonString(dataStructure.getNamespace()));
        map.put("metadata", parseJsonString(dataStructure.getMetadata()));
        map.put("tags", parseJsonArray(dataStructure.getTags()));
        map.put("version", dataStructure.getVersion());
        map.put("isActive", dataStructure.getIsActive());
        map.put("createdAt", dataStructure.getCreatedAt().toString());
        map.put("updatedAt", dataStructure.getUpdatedAt().toString());
        
        if (dataStructure.getBusinessComponent() != null) {
            map.put("businessComponentId", dataStructure.getBusinessComponent().getId());
            map.put("businessComponentName", dataStructure.getBusinessComponent().getName());
        }
        
        if (dataStructure.getCreatedBy() != null) {
            map.put("createdBy", dataStructure.getCreatedBy().getUsername());
        }
        
        return map;
    }
    
    /**
     * Parse usage string to enum
     */
    private DataStructure.DataStructureUsage parseUsage(String usage) {
        if (usage == null || usage.isEmpty()) {
            return DataStructure.DataStructureUsage.both;
        }
        try {
            return DataStructure.DataStructureUsage.valueOf(usage.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid usage value: " + usage + ". Must be one of: source, target, both");
        }
    }
    
    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON: {}", e.getMessage());
            throw new BusinessException("Failed to convert to JSON: " + e.getMessage());
        }
    }
    
    /**
     * Reorder structure fields based on original WSDL content
     */
    private Map<String, Object> reorderStructureFromWsdl(Map<String, Object> structure, String originalContent) {
        if (originalContent == null || !originalContent.contains("<xsd:element") || structure == null) {
            return structure;
        }
        
        Map<String, Object> reorderedStructure = new java.util.LinkedHashMap<>();
        
        // For each top-level key in the structure
        for (Map.Entry<String, Object> entry : structure.entrySet()) {
            String typeName = entry.getKey();
            Object typeValue = entry.getValue();
            
            if (typeValue instanceof Map) {
                // This is a complex type - reorder its fields
                Map<String, Object> fields = (Map<String, Object>) typeValue;
                Map<String, Object> reorderedFields = new java.util.LinkedHashMap<>();
                
                // Extract field order from WSDL for this type
                List<String> fieldOrder = extractFieldOrderFromWsdl(originalContent, typeName);
                log.info("Extracted field order for type {}: {}", typeName, fieldOrder);
                
                // First add fields in the order they appear in WSDL
                for (String fieldName : fieldOrder) {
                    if (fields.containsKey(fieldName)) {
                        reorderedFields.put(fieldName, fields.get(fieldName));
                    }
                }
                
                // Then add any remaining fields not found in WSDL
                for (Map.Entry<String, Object> fieldEntry : fields.entrySet()) {
                    if (!reorderedFields.containsKey(fieldEntry.getKey())) {
                        reorderedFields.put(fieldEntry.getKey(), fieldEntry.getValue());
                    }
                }
                
                reorderedStructure.put(typeName, reorderedFields);
            } else {
                // Not a complex type, just copy it
                reorderedStructure.put(typeName, typeValue);
            }
        }
        
        return reorderedStructure;
    }
    
    /**
     * Extract field order from WSDL for a given type
     */
    private List<String> extractFieldOrderFromWsdl(String wsdl, String typeName) {
        List<String> fieldOrder = new java.util.ArrayList<>();
        
        try {
            // Find the complex type definition
            // Try to find by the message type name first (e.g., Credit_Token_Req_MT)
            String typePattern = "name=\"" + typeName + "\"";
            int typeStart = wsdl.indexOf(typePattern);
            
            // If not found, try to find the corresponding data type (e.g., Credit_Token_Req_DT)
            if (typeStart == -1 && typeName.endsWith("_MT")) {
                String dataTypeName = typeName.substring(0, typeName.length() - 3) + "_DT";
                typePattern = "name=\"" + dataTypeName + "\"";
                typeStart = wsdl.indexOf(typePattern);
                log.debug("Trying data type name: {}", dataTypeName);
            }
            
            if (typeStart == -1) {
                log.warn("Type {} not found in WSDL", typeName);
                return fieldOrder;
            }
            
            // Find the sequence section for this type
            int sequenceStart = wsdl.indexOf("<xsd:sequence>", typeStart);
            if (sequenceStart == -1) {
                sequenceStart = wsdl.indexOf("<xs:sequence>", typeStart);
            }
            if (sequenceStart == -1) {
                return fieldOrder;
            }
            
            int sequenceEnd = wsdl.indexOf("</xsd:sequence>", sequenceStart);
            if (sequenceEnd == -1) {
                sequenceEnd = wsdl.indexOf("</xs:sequence>", sequenceStart);
            }
            if (sequenceEnd == -1) {
                return fieldOrder;
            }
            
            // Extract elements within the sequence
            String sequence = wsdl.substring(sequenceStart, sequenceEnd);
            
            // Find all element names
            java.util.regex.Pattern elementPattern = java.util.regex.Pattern.compile(
                "<x[s]?d:element\\s+name=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = elementPattern.matcher(sequence);
            
            while (matcher.find()) {
                fieldOrder.add(matcher.group(1));
            }
            
        } catch (Exception e) {
            log.warn("Error extracting field order from WSDL: {}", e.getMessage());
        }
        
        return fieldOrder;
    }
    
    /**
     * Parse JSON string to object
     */
    private Object parseJsonString(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            // Use TypeReference to ensure LinkedHashMap is used to preserve field order
            return objectMapper.readValue(json, new TypeReference<java.util.LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON, returning as string: {}", e.getMessage());
            return json;
        }
    }
    
    /**
     * Parse JSON array string to List
     */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON array, returning empty list: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Derive the original format from the structure type
     */
    private String deriveFormatFromType(String type) {
        if (type == null) {
            return null;
        }
        
        return switch (type.toLowerCase()) {
            case "json" -> "json";
            case "xml", "xsd", "wsdl" -> "xml";
            case "csv" -> "csv";
            case "custom" -> "custom";
            default -> null;
        };
    }
    
    /**
     * Get resolved XSD/WSDL content with dependencies
     */
    @Transactional(readOnly = true)
    public String getResolvedContent(String id) {
        DataStructure dataStructure = getDataStructure(id);
        
        if (dataStructure.getOriginalContent() == null) {
            return null;
        }
        
        // For XSD/WSDL types, return content with resolved dependencies
        if ("xsd".equalsIgnoreCase(dataStructure.getType()) || 
            "wsdl".equalsIgnoreCase(dataStructure.getType())) {
            
            // Parse metadata to get dependency information
            if (dataStructure.getMetadata() != null) {
                try {
                    Map<String, Object> metadata = objectMapper.readValue(
                        dataStructure.getMetadata(), 
                        new TypeReference<Map<String, Object>>() {}
                    );
                    
                    if (metadata.containsKey("dependencies")) {
                        // Dependencies exist, return content with custom entity resolver info
                        // The actual resolution will happen during parsing
                        return dataStructure.getOriginalContent();
                    }
                } catch (Exception e) {
                    log.warn("Error parsing metadata for structure {}: {}", id, e.getMessage());
                }
            }
        }
        
        return dataStructure.getOriginalContent();
    }
    
    /**
     * Check and report missing dependencies for a data structure
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkDependencies(String id) {
        DataStructure dataStructure = getDataStructure(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", dataStructure.getName());
        result.put("hasDependencies", false);
        result.put("missingDependencies", new ArrayList<>());
        result.put("resolvedDependencies", new ArrayList<>());
        
        if (dataStructure.getMetadata() != null) {
            try {
                Map<String, Object> metadata = objectMapper.readValue(
                    dataStructure.getMetadata(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                
                if (metadata.containsKey("dependencies")) {
                    result.put("hasDependencies", true);
                    
                    XsdDependencyResolver.DependencyMetadata deps = 
                        objectMapper.convertValue(
                            metadata.get("dependencies"), 
                            XsdDependencyResolver.DependencyMetadata.class
                        );
                    
                    // Check imports
                    for (XsdDependencyResolver.ImportInfo importInfo : deps.getImports()) {
                        Map<String, String> depInfo = new HashMap<>();
                        depInfo.put("namespace", importInfo.getNamespace());
                        depInfo.put("schemaLocation", importInfo.getSchemaLocation());
                        depInfo.put("type", "import");
                        
                        if (importInfo.getDataStructureId() != null) {
                            depInfo.put("dataStructureId", importInfo.getDataStructureId());
                            depInfo.put("dataStructureName", importInfo.getDataStructureName());
                            ((List<Map<String, String>>) result.get("resolvedDependencies")).add(depInfo);
                        } else {
                            ((List<Map<String, String>>) result.get("missingDependencies")).add(depInfo);
                        }
                    }
                    
                    // Check includes
                    for (XsdDependencyResolver.ImportInfo includeInfo : deps.getIncludes()) {
                        Map<String, String> depInfo = new HashMap<>();
                        depInfo.put("schemaLocation", includeInfo.getSchemaLocation());
                        depInfo.put("type", "include");
                        
                        if (includeInfo.getDataStructureId() != null) {
                            depInfo.put("dataStructureId", includeInfo.getDataStructureId());
                            depInfo.put("dataStructureName", includeInfo.getDataStructureName());
                            ((List<Map<String, String>>) result.get("resolvedDependencies")).add(depInfo);
                        } else {
                            ((List<Map<String, String>>) result.get("missingDependencies")).add(depInfo);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error checking dependencies for structure {}: {}", id, e.getMessage());
            }
        }
        
        return result;
    }
}