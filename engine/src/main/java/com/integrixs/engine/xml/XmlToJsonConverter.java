package com.integrixs.engine.xml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts XML data back to JSON format
 */
@Service
public class XmlToJsonConverter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convert XML string to JSON
     * @param xmlContent The XML content to convert
     * @param removeRootElement Whether to remove the root element wrapper
     * @return JSON string
     */
    public String convertToJson(String xmlContent, boolean removeRootElement) throws XmlConversionException {
        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            
            // Convert to JSON
            JsonNode jsonNode = convertElementToJson(doc.getDocumentElement());
            
            // Remove root element if requested
            if (removeRootElement && jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                if (objectNode.size() == 1) {
                    String rootKey = objectNode.fieldNames().next();
                    jsonNode = objectNode.get(rootKey);
                }
            }
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert XML to JSON", e);
        }
    }
    
    /**
     * Convert XML element to JsonNode
     */
    private JsonNode convertElementToJson(Element element) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        JsonNode contentNode = processElement(element);
        rootNode.set(element.getTagName(), contentNode);
        return rootNode;
    }
    
    /**
     * Process an XML element and its children
     */
    private JsonNode processElement(Element element) {
        // Check if element has only text content
        if (!hasChildElements(element) && element.getAttributes().getLength() == 0) {
            String textContent = element.getTextContent().trim();
            if (textContent.isEmpty()) {
                return objectMapper.nullNode();
            }
            // Try to parse as number or boolean
            return parseValue(textContent);
        }
        
        ObjectNode objectNode = objectMapper.createObjectNode();
        
        // Process attributes
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if (!attr.getNodeName().startsWith("xmlns")) {
                objectNode.set("@" + attr.getNodeName(), parseValue(attr.getNodeValue()));
            }
        }
        
        // Process child elements
        Map<String, ArrayNode> childArrays = new HashMap<>();
        NodeList children = element.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String childName = childElement.getTagName();
                
                // Check if this is an array element
                if (countElements(element, childName) > 1) {
                    // Handle as array
                    ArrayNode arrayNode = childArrays.computeIfAbsent(childName, 
                        k -> objectMapper.createArrayNode());
                    arrayNode.add(processElement(childElement));
                } else {
                    // Handle as single element
                    objectNode.set(childName, processElement(childElement));
                }
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty() && objectNode.size() > 0) {
                    // Mixed content - add as _text field
                    objectNode.set("_text", parseValue(text));
                } else if (!text.isEmpty() && objectNode.size() == 0) {
                    // Pure text content
                    return parseValue(text);
                }
            }
        }
        
        // Add arrays to object
        childArrays.forEach(objectNode::set);
        
        return objectNode;
    }
    
    /**
     * Check if element has child elements
     */
    private boolean hasChildElements(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Count elements with given name
     */
    private int countElements(Element parent, String elementName) {
        NodeList children = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && 
                child.getNodeName().equals(elementName)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Parse string value to appropriate JSON type
     */
    private JsonNode parseValue(String value) {
        // Try boolean
        if ("true".equalsIgnoreCase(value)) {
            return objectMapper.getNodeFactory().booleanNode(true);
        }
        if ("false".equalsIgnoreCase(value)) {
            return objectMapper.getNodeFactory().booleanNode(false);
        }
        
        // Try number
        try {
            if (value.contains(".")) {
                return objectMapper.getNodeFactory().numberNode(Double.parseDouble(value));
            } else {
                return objectMapper.getNodeFactory().numberNode(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as string
        }
        
        // Default to string
        return objectMapper.getNodeFactory().textNode(value);
    }
}