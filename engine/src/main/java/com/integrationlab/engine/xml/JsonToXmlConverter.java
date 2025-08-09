package com.integrationlab.engine.xml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts JSON data to XML format with namespace support
 */
@Service
public class JsonToXmlConverter implements XmlConversionService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String ensureValidXmlElementName(String name) {
        // Handle null or empty names
        if (name == null || name.trim().isEmpty()) {
            return "_element";
        }
        
        String cleaned = name.trim();
        
        // XML element names cannot start with "xml" (case-insensitive)
        if (cleaned.toLowerCase().startsWith("xml")) {
            cleaned = "_" + cleaned;
        }
        
        // XML element names cannot start with numbers, dots, hyphens, or other invalid chars
        if (!cleaned.matches("^[a-zA-Z_].*")) {
            cleaned = "_" + cleaned;
        }
        
        // Replace invalid characters with underscores
        // Valid chars are letters (including Unicode), digits, hyphens, underscores, and periods
        // But hyphens and periods cannot be at the start
        // First normalize any special characters
        cleaned = cleaned.replaceAll("…", "_"); // Replace ellipsis
        cleaned = cleaned.replaceAll("\\[\\]", ""); // Remove array notation
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{N}_.-]", "_"); // Use Unicode categories
        
        // Ensure not empty after cleaning
        if (cleaned.isEmpty()) {
            cleaned = "_element";
        }
        
        return cleaned;
    }
    
    @Override
    public String convertToXml(Object data, Object config) throws XmlConversionException {
        if (!(config instanceof JsonXmlWrapperConfig)) {
            throw new XmlConversionException("Configuration must be of type JsonXmlWrapperConfig");
        }
        
        JsonXmlWrapperConfig wrapperConfig = (JsonXmlWrapperConfig) config;
        
        try {
            // Convert input to JsonNode
            JsonNode jsonNode;
            if (data instanceof String) {
                jsonNode = objectMapper.readTree((String) data);
            } else if (data instanceof JsonNode) {
                jsonNode = (JsonNode) data;
            } else {
                // Configure ObjectMapper to preserve order if data is LinkedHashMap
                ObjectMapper orderPreservingMapper = new ObjectMapper();
                orderPreservingMapper.configure(com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
                jsonNode = orderPreservingMapper.valueToTree(data);
            }
            
            // Create XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            // Create root element with namespace
            Element rootElement;
            String rootName = ensureValidXmlElementName(wrapperConfig.getRootElementName());
            
            if (wrapperConfig.getNamespaceUri() != null && !wrapperConfig.getNamespaceUri().isEmpty()) {
                rootElement = doc.createElementNS(wrapperConfig.getNamespaceUri(), 
                    wrapperConfig.getNamespacePrefix() != null 
                        ? wrapperConfig.getNamespacePrefix() + ":" + rootName
                        : rootName);
                
                // Add namespace declaration
                if (wrapperConfig.getNamespacePrefix() != null) {
                    rootElement.setAttribute("xmlns:" + wrapperConfig.getNamespacePrefix(), 
                        wrapperConfig.getNamespaceUri());
                } else {
                    rootElement.setAttribute("xmlns", wrapperConfig.getNamespaceUri());
                }
            } else {
                rootElement = doc.createElement(rootName);
            }
            
            // Add additional namespaces
            if (wrapperConfig.getAdditionalNamespaces() != null) {
                for (Map.Entry<String, String> ns : wrapperConfig.getAdditionalNamespaces().entrySet()) {
                    rootElement.setAttribute("xmlns:" + ns.getKey(), ns.getValue());
                }
            }
            
            doc.appendChild(rootElement);
            
            // Convert JSON to XML elements
            processJsonNode(doc, rootElement, jsonNode, wrapperConfig, "");
            
            // Transform to string
            return transformDocumentToString(doc, wrapperConfig);
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert JSON to XML", e);
        }
    }
    
    private void processJsonNode(Document doc, Element parentElement, JsonNode node, 
                                JsonXmlWrapperConfig config, String path) {
        if (node.isObject()) {
            processObjectNode(doc, parentElement, (ObjectNode) node, config, path);
        } else if (node.isArray()) {
            processArrayNode(doc, parentElement, (ArrayNode) node, config, path);
        } else if (node.isNull() && !config.isPreserveNullValues()) {
            // Skip null values unless configured to preserve them
        } else {
            // Primitive value - set as text content
            parentElement.setTextContent(node.asText());
        }
    }
    
    private void processObjectNode(Document doc, Element parentElement, ObjectNode objectNode,
                                  JsonXmlWrapperConfig config, String path) {
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            
            // Log problematic field names for debugging
            if (fieldName.contains("…") || fieldName.contains("Credit_Token")) {
                System.out.println("DEBUG: Processing field name: " + fieldName);
            }
            
            // Convert property name if configured
            if (config.isConvertPropertyNames()) {
                fieldName = convertPropertyName(fieldName);
            }
            
            String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
            
            if (fieldValue.isArray()) {
                processArrayNode(doc, parentElement, (ArrayNode) fieldValue, config, currentPath);
            } else {
                Element element = createElement(doc, fieldName, config);
                parentElement.appendChild(element);
                processJsonNode(doc, element, fieldValue, config, currentPath);
            }
        }
    }
    
    private void processArrayNode(Document doc, Element parentElement, ArrayNode arrayNode,
                                 JsonXmlWrapperConfig config, String path) {
        // Get custom array element name if configured
        String elementName = null;
        if (config.getArrayElementNames() != null) {
            elementName = config.getArrayElementNames().get(path);
        }
        
        if (elementName == null) {
            // Use singular form of the array field name
            String lastSegment = path.substring(path.lastIndexOf('.') + 1);
            elementName = singularize(lastSegment);
        }
        
        // Ensure the element name is valid
        elementName = ensureValidXmlElementName(elementName);
        
        for (JsonNode item : arrayNode) {
            Element element = createElement(doc, elementName, config);
            parentElement.appendChild(element);
            processJsonNode(doc, element, item, config, path);
        }
    }
    
    private Element createElement(Document doc, String name, JsonXmlWrapperConfig config) {
        // Ensure the name is valid for XML
        String validName = ensureValidXmlElementName(name);
        
        if (config.getNamespaceUri() != null && !config.getNamespaceUri().isEmpty()) {
            return doc.createElementNS(config.getNamespaceUri(), 
                config.getNamespacePrefix() != null 
                    ? config.getNamespacePrefix() + ":" + validName
                    : validName);
        } else {
            return doc.createElement(validName);
        }
    }
    
    private String convertPropertyName(String name) {
        // First ensure it's a valid XML element name
        String cleaned = ensureValidXmlElementName(name);
        
        // Convert snake_case or kebab-case to camelCase if needed
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (char c : cleaned.toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                result.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                result.append(c);
            }
        }
        
        String finalName = result.toString();
        
        // Ensure the result is still valid after conversion
        return ensureValidXmlElementName(finalName);
    }
    
    private String singularize(String plural) {
        // Simple singularization logic
        if (plural.endsWith("ies")) {
            return plural.substring(0, plural.length() - 3) + "y";
        } else if (plural.endsWith("es")) {
            return plural.substring(0, plural.length() - 2);
        } else if (plural.endsWith("s") && !plural.endsWith("ss")) {
            return plural.substring(0, plural.length() - 1);
        }
        return plural;
    }
    
    private String transformDocumentToString(Document doc, JsonXmlWrapperConfig config) 
            throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        // Set output properties
        if (config.isIncludeXmlDeclaration()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        } else {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        
        transformer.setOutputProperty(OutputKeys.ENCODING, config.getEncoding());
        
        if (config.isPrettyPrint()) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return writer.toString();
    }
    
    @Override
    public boolean supports(Class<?> dataType) {
        return String.class.isAssignableFrom(dataType) || 
               JsonNode.class.isAssignableFrom(dataType) ||
               Map.class.isAssignableFrom(dataType);
    }
}