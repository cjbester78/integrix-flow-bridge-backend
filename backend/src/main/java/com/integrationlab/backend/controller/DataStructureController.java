package com.integrationlab.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.backend.service.DataStructureService;
import com.integrationlab.backend.security.SecurityUtils;
import com.integrationlab.data.model.DataStructure;
import com.integrationlab.engine.xml.JsonToXmlConverter;
import com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/structures")
@CrossOrigin(origins = "*")
public class DataStructureController {
    
    private final DataStructureService dataStructureService;
    private final SecurityUtils securityUtils;
    private final JsonToXmlConverter jsonToXmlConverter;
    private final ObjectMapper objectMapper;
    
    public DataStructureController(DataStructureService dataStructureService, 
                                 SecurityUtils securityUtils,
                                 JsonToXmlConverter jsonToXmlConverter) {
        this.dataStructureService = dataStructureService;
        this.securityUtils = securityUtils;
        this.jsonToXmlConverter = jsonToXmlConverter;
        
        // Configure ObjectMapper to preserve field order using LinkedHashMap
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        this.objectMapper.setNodeFactory(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<Map<String, Object>> getStructures(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String usage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) String businessComponentId) {
        
        try {
            log.info("Getting data structures with filters - type: {}, usage: {}, search: {}", type, usage, search);
            
            Map<String, Object> response = dataStructureService.getDataStructures(
                type, usage, search, tags, page, limit, businessComponentId
            );
            
            log.info("Returning {} structures", response.get("total"));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting data structures", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<?> getStructure(@PathVariable String id) {
        try {
            log.info("Getting data structure by id: {}", id);
            
            Map<String, Object> dataStructure = dataStructureService.getDataStructureAsMap(id);
            return ResponseEntity.ok(dataStructure);
            
        } catch (Exception e) {
            log.error("Error getting data structure", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<?> createStructure(@RequestBody Map<String, Object> structure) {
        try {
            log.info("Creating new data structure");
            
            String userId = securityUtils.getCurrentUserId();
            DataStructure created = dataStructureService.createDataStructure(structure, userId);
            
            // Convert to map to avoid lazy loading issues
            Map<String, Object> response = dataStructureService.getDataStructureAsMap(created.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating data structure", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<?> updateStructure(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        try {
            log.info("Updating data structure: {}", id);
            
            DataStructure updated = dataStructureService.updateDataStructure(id, updates);
            
            // Convert to map to avoid lazy loading issues
            Map<String, Object> response = dataStructureService.getDataStructureAsMap(updated.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating data structure", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<?> deleteStructure(@PathVariable String id) {
        try {
            log.info("Deleting data structure: {}", id);
            
            dataStructureService.deleteDataStructure(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting data structure", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/convert-to-xml")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<?> convertStructureToXml(
            @PathVariable String id,
            @RequestBody(required = false) JsonXmlWrapperConfig config) {
        try {
            log.info("Converting data structure {} to XML", id);
            
            // Get the data structure
            DataStructure dataStructure = dataStructureService.getDataStructure(id);
            
            // If original content is available and is XML format
            if (dataStructure.getOriginalContent() != null && 
                "xml".equalsIgnoreCase(dataStructure.getOriginalFormat())) {
                log.info("Processing original XML content for structure: {}", id);
                
                String xmlContent = dataStructure.getOriginalContent();
                
                // For WSDL types, extract just the schema elements
                if ("wsdl".equalsIgnoreCase(dataStructure.getType()) && xmlContent.contains("<wsdl:types>")) {
                    xmlContent = extractSchemaFromWsdl(xmlContent);
                }
                
                Map<String, Object> response = Map.of(
                    "structureId", id,
                    "structureName", dataStructure.getName(),
                    "xmlContent", xmlContent,
                    "config", config != null ? config : Map.of("source", "original")
                );
                
                return ResponseEntity.ok(response);
            }
            
            // Use default config if not provided
            if (config == null) {
                config = JsonXmlWrapperConfig.builder()
                    .rootElementName(dataStructure.getName().replaceAll("\\s+", ""))
                    .includeXmlDeclaration(true)
                    .prettyPrint(true)
                    .encoding("UTF-8")
                    .convertPropertyNames(true)
                    .preserveNullValues(false)
                    .arrayElementNames(new HashMap<>())
                    .additionalNamespaces(new HashMap<>())
                    .build();
            }
            
            // Log the structure for debugging
            log.info("Converting structure - ID: {}, Name: {}, Type: {}", 
                dataStructure.getId(), dataStructure.getName(), dataStructure.getType());
            log.info("Structure content (first 500 chars): {}", 
                dataStructure.getStructure().substring(0, Math.min(500, dataStructure.getStructure().length())));
            
            // Special logging for WSDL structures
            if ("wsdl".equalsIgnoreCase(dataStructure.getType())) {
                log.info("WSDL structure detected - analyzing format...");
                try {
                    // Try to parse as JSON to see structure
                    Object parsed = objectMapper.readValue(dataStructure.getStructure(), Object.class);
                    log.info("WSDL structure parsed successfully. Type: {}", parsed.getClass().getName());
                    log.info("WSDL structure content: {}", objectMapper.writeValueAsString(parsed));
                } catch (Exception e) {
                    log.error("Failed to parse WSDL structure as JSON: {}", e.getMessage());
                }
            }
            
            // Parse the structure JSON string first
            Object structureObject;
            
            try {
                // Use TypeReference to ensure LinkedHashMap is used to preserve field order
                structureObject = objectMapper.readValue(dataStructure.getStructure(), 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.LinkedHashMap<String, Object>>() {});
            } catch (Exception parseEx) {
                log.error("Failed to parse structure JSON: {}", parseEx.getMessage());
                throw new IllegalArgumentException("Invalid structure format: " + parseEx.getMessage());
            }
            
            // For WSDL structures, we might need to handle them differently
            String xmlContent;
            if ("wsdl".equalsIgnoreCase(dataStructure.getType())) {
                // WSDL structures might already be in a special format
                // Try to convert, but if it fails, create a simple structure
                try {
                    xmlContent = jsonToXmlConverter.convertToXml(structureObject, config);
                } catch (Exception wsdlEx) {
                    log.warn("Failed to convert WSDL structure directly, creating wrapper: {}", wsdlEx.getMessage());
                    // Create a simple wrapper structure using LinkedHashMap to preserve order
                    Map<String, Object> wrapper = new java.util.LinkedHashMap<>();
                    wrapper.put("structure", structureObject);
                    xmlContent = jsonToXmlConverter.convertToXml(wrapper, config);
                }
            } else {
                // Convert the structure (JSON) to XML
                xmlContent = jsonToXmlConverter.convertToXml(structureObject, config);
            }
            
            Map<String, Object> response = Map.of(
                "structureId", id,
                "structureName", dataStructure.getName(),
                "xmlContent", xmlContent,
                "config", config
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting data structure to XML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Failed to convert JSON to XML",
                    "details", e.getMessage()
                ));
        }
    }
    
    /**
     * Extract schema elements from WSDL for field mapping
     */
    private String extractSchemaFromWsdl(String wsdl) {
        try {
            // Extract just the element definitions from the WSDL
            StringBuilder result = new StringBuilder();
            result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            result.append("<root xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
            
            // Extract all message element definitions
            int startPos = 0;
            while ((startPos = wsdl.indexOf("<xsd:element name=\"", startPos)) != -1) {
                int endPos = wsdl.indexOf("</xsd:element>", startPos);
                if (endPos == -1) {
                    // Try to find self-closing element
                    endPos = wsdl.indexOf("/>", startPos);
                    if (endPos == -1) break;
                    endPos += 2;
                } else {
                    endPos += "</xsd:element>".length();
                }
                
                String elementDef = wsdl.substring(startPos, endPos);
                
                // Only include message type elements (MT)
                if (elementDef.contains("_MT\"")) {
                    // Extract the type name
                    int typeStart = elementDef.indexOf("type=\"");
                    if (typeStart != -1) {
                        typeStart += 6;
                        int typeEnd = elementDef.indexOf("\"", typeStart);
                        if (typeEnd != -1) {
                            String typeName = elementDef.substring(typeStart, typeEnd);
                            // Remove namespace prefix if present
                            if (typeName.contains(":")) {
                                typeName = typeName.substring(typeName.indexOf(":") + 1);
                            }
                            
                            // Find the complex type definition
                            String complexTypeDef = extractComplexType(wsdl, typeName);
                            if (complexTypeDef != null) {
                                result.append("  ").append(complexTypeDef).append("\n");
                            }
                        }
                    }
                }
                
                startPos = endPos;
            }
            
            result.append("</root>");
            return result.toString();
            
        } catch (Exception e) {
            log.error("Error extracting schema from WSDL: {}", e.getMessage());
            // Return original if extraction fails
            return wsdl;
        }
    }
    
    /**
     * Extract a complex type definition from WSDL
     */
    private String extractComplexType(String wsdl, String typeName) {
        try {
            String searchPattern = "<xsd:complexType name=\"" + typeName + "\"";
            int startPos = wsdl.indexOf(searchPattern);
            if (startPos == -1) {
                // Try with xs: prefix
                searchPattern = "<xs:complexType name=\"" + typeName + "\"";
                startPos = wsdl.indexOf(searchPattern);
            }
            
            if (startPos == -1) return null;
            
            // Find the end of this complex type
            int endPos = wsdl.indexOf("</xsd:complexType>", startPos);
            if (endPos == -1) {
                endPos = wsdl.indexOf("</xs:complexType>", startPos);
            }
            if (endPos == -1) return null;
            
            endPos += "</xsd:complexType>".length();
            
            String complexType = wsdl.substring(startPos, endPos);
            
            // Create a wrapper element with the message type name
            String elementName = typeName.replace("_DT", "_MT");
            StringBuilder wrapped = new StringBuilder();
            wrapped.append("<").append(elementName).append(">\n");
            
            // Extract just the sequence elements
            int seqStart = complexType.indexOf("<xsd:sequence>");
            if (seqStart == -1) {
                seqStart = complexType.indexOf("<xs:sequence>");
            }
            
            if (seqStart != -1) {
                int seqEnd = complexType.indexOf("</xsd:sequence>", seqStart);
                if (seqEnd == -1) {
                    seqEnd = complexType.indexOf("</xs:sequence>", seqStart);
                }
                
                if (seqEnd != -1) {
                    // Extract elements from sequence
                    String sequence = complexType.substring(seqStart, seqEnd + "</xsd:sequence>".length());
                    
                    // Extract each element and create simple XML elements
                    int elemStart = 0;
                    while ((elemStart = sequence.indexOf("<xsd:element", elemStart)) != -1) {
                        int nameStart = sequence.indexOf("name=\"", elemStart);
                        if (nameStart != -1) {
                            nameStart += 6;
                            int nameEnd = sequence.indexOf("\"", nameStart);
                            if (nameEnd != -1) {
                                String fieldName = sequence.substring(nameStart, nameEnd);
                                wrapped.append("    <").append(fieldName).append(">string</").append(fieldName).append(">\n");
                            }
                        }
                        elemStart = sequence.indexOf(">", elemStart) + 1;
                    }
                }
            }
            
            wrapped.append("</").append(elementName).append(">");
            return wrapped.toString();
            
        } catch (Exception e) {
            log.error("Error extracting complex type {}: {}", typeName, e.getMessage());
            return null;
        }
    }
}