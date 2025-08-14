package com.integrixs.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.DataStructure;
import com.integrixs.data.repository.DataStructureRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class XsdDependencyResolver {
    
    private final DataStructureRepository dataStructureRepository;
    private final ObjectMapper objectMapper;
    
    @Data
    public static class ImportInfo {
        private String namespace;
        private String schemaLocation;
        private String dataStructureId;
        private String dataStructureName;
    }
    
    @Data
    public static class DependencyMetadata {
        private List<ImportInfo> imports = new ArrayList<>();
        private List<ImportInfo> includes = new ArrayList<>();
        private Map<String, String> namespaceMapping = new HashMap<>();
    }
    
    /**
     * Extract import and include dependencies from XSD content
     */
    public DependencyMetadata extractDependencies(String xsdContent) {
        DependencyMetadata metadata = new DependencyMetadata();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xsdContent)));
            
            // Extract imports
            NodeList imports = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
            for (int i = 0; i < imports.getLength(); i++) {
                Element importElement = (Element) imports.item(i);
                ImportInfo importInfo = new ImportInfo();
                importInfo.setNamespace(importElement.getAttribute("namespace"));
                importInfo.setSchemaLocation(importElement.getAttribute("schemaLocation"));
                metadata.getImports().add(importInfo);
            }
            
            // Extract includes
            NodeList includes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
            for (int i = 0; i < includes.getLength(); i++) {
                Element includeElement = (Element) includes.item(i);
                ImportInfo includeInfo = new ImportInfo();
                includeInfo.setSchemaLocation(includeElement.getAttribute("schemaLocation"));
                metadata.getIncludes().add(includeInfo);
            }
            
            // Extract namespace declarations
            Element root = doc.getDocumentElement();
            for (int i = 0; i < root.getAttributes().getLength(); i++) {
                String attrName = root.getAttributes().item(i).getNodeName();
                String attrValue = root.getAttributes().item(i).getNodeValue();
                if (attrName.startsWith("xmlns:")) {
                    String prefix = attrName.substring(6);
                    metadata.getNamespaceMapping().put(prefix, attrValue);
                }
            }
            
        } catch (Exception e) {
            log.error("Error extracting XSD dependencies: {}", e.getMessage());
        }
        
        return metadata;
    }
    
    /**
     * Extract import dependencies from WSDL content
     */
    public DependencyMetadata extractWsdlDependencies(String wsdlContent) {
        DependencyMetadata metadata = new DependencyMetadata();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(wsdlContent)));
            
            // Find schema elements within types section
            NodeList schemas = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
            
            for (int s = 0; s < schemas.getLength(); s++) {
                Element schema = (Element) schemas.item(s);
                
                // Extract imports from within schema
                NodeList imports = schema.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
                for (int i = 0; i < imports.getLength(); i++) {
                    Element importElement = (Element) imports.item(i);
                    ImportInfo importInfo = new ImportInfo();
                    importInfo.setNamespace(importElement.getAttribute("namespace"));
                    importInfo.setSchemaLocation(importElement.getAttribute("schemaLocation"));
                    metadata.getImports().add(importInfo);
                }
                
                // Extract includes from within schema
                NodeList includes = schema.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
                for (int i = 0; i < includes.getLength(); i++) {
                    Element includeElement = (Element) includes.item(i);
                    ImportInfo includeInfo = new ImportInfo();
                    includeInfo.setSchemaLocation(includeElement.getAttribute("schemaLocation"));
                    metadata.getIncludes().add(includeInfo);
                }
            }
            
            // Also check for WSDL imports
            NodeList wsdlImports = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
            for (int i = 0; i < wsdlImports.getLength(); i++) {
                Element importElement = (Element) wsdlImports.item(i);
                ImportInfo importInfo = new ImportInfo();
                importInfo.setNamespace(importElement.getAttribute("namespace"));
                importInfo.setSchemaLocation(importElement.getAttribute("location"));
                metadata.getImports().add(importInfo);
            }
            
        } catch (Exception e) {
            log.error("Error extracting WSDL dependencies: {}", e.getMessage());
        }
        
        return metadata;
    }
    
    /**
     * Resolve dependencies by matching namespace or filename
     */
    public void resolveDependencies(DependencyMetadata metadata, String currentStructureId) {
        // Resolve imports
        for (ImportInfo importInfo : metadata.getImports()) {
            DataStructure resolved = findMatchingStructure(importInfo.getNamespace(), importInfo.getSchemaLocation());
            if (resolved != null) {
                importInfo.setDataStructureId(resolved.getId());
                importInfo.setDataStructureName(resolved.getName());
            }
        }
        
        // Resolve includes
        for (ImportInfo includeInfo : metadata.getIncludes()) {
            DataStructure resolved = findMatchingStructure(null, includeInfo.getSchemaLocation());
            if (resolved != null) {
                includeInfo.setDataStructureId(resolved.getId());
                includeInfo.setDataStructureName(resolved.getName());
            }
        }
    }
    
    /**
     * Find matching data structure by namespace or filename
     */
    private DataStructure findMatchingStructure(String namespace, String schemaLocation) {
        // First try to match by namespace
        if (namespace != null && !namespace.isEmpty()) {
            List<DataStructure> structures = dataStructureRepository.findByTypeIn(Arrays.asList("xsd", "wsdl"));
            for (DataStructure structure : structures) {
                try {
                    if (structure.getNamespace() != null) {
                        Map<String, Object> nsData = objectMapper.readValue(structure.getNamespace(), Map.class);
                        String structureNamespace = (String) nsData.get("uri");
                        if (namespace.equals(structureNamespace)) {
                            return structure;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error parsing namespace for structure {}: {}", structure.getId(), e.getMessage());
                }
            }
        }
        
        // Try to match by filename from schemaLocation
        if (schemaLocation != null && !schemaLocation.isEmpty()) {
            String filename = extractFilename(schemaLocation);
            if (filename != null) {
                // Try exact name match
                Optional<DataStructure> exactMatch = dataStructureRepository.findByName(filename);
                if (exactMatch.isPresent()) {
                    return exactMatch.get();
                }
                
                // Try without extension
                String nameWithoutExt = filename.replaceAll("\\.(xsd|wsdl)$", "");
                Optional<DataStructure> withoutExtMatch = dataStructureRepository.findByName(nameWithoutExt);
                if (withoutExtMatch.isPresent()) {
                    return withoutExtMatch.get();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract filename from path
     */
    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        // Remove any query parameters
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        
        // Extract filename from path
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        
        return path;
    }
    
    /**
     * Create an EntityResolver that resolves imports from database
     */
    public EntityResolver createEntityResolver(String currentStructureId) {
        return new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                log.debug("Resolving entity - publicId: {}, systemId: {}", publicId, systemId);
                
                // Try to find the data structure by system ID
                DataStructure resolved = findMatchingStructure(publicId, systemId);
                if (resolved != null && resolved.getOriginalContent() != null) {
                    log.info("Resolved import {} to data structure: {}", systemId, resolved.getName());
                    return new InputSource(new StringReader(resolved.getOriginalContent()));
                }
                
                // Return null to use default resolution
                return null;
            }
        };
    }
    
    /**
     * Update XSD content to replace relative paths with data structure references
     */
    public String updateImportPaths(String xsdContent, DependencyMetadata metadata) {
        String updatedContent = xsdContent;
        
        // Update import schemaLocations
        for (ImportInfo importInfo : metadata.getImports()) {
            if (importInfo.getDataStructureId() != null && importInfo.getSchemaLocation() != null) {
                // Replace the schemaLocation with a reference to the data structure
                String oldPattern = "schemaLocation=\"" + Pattern.quote(importInfo.getSchemaLocation()) + "\"";
                String newLocation = "schemaLocation=\"datastructure:" + importInfo.getDataStructureId() + "\"";
                updatedContent = updatedContent.replaceAll(oldPattern, newLocation);
            }
        }
        
        // Update include schemaLocations
        for (ImportInfo includeInfo : metadata.getIncludes()) {
            if (includeInfo.getDataStructureId() != null && includeInfo.getSchemaLocation() != null) {
                String oldPattern = "schemaLocation=\"" + Pattern.quote(includeInfo.getSchemaLocation()) + "\"";
                String newLocation = "schemaLocation=\"datastructure:" + includeInfo.getDataStructureId() + "\"";
                updatedContent = updatedContent.replaceAll(oldPattern, newLocation);
            }
        }
        
        return updatedContent;
    }
}