package com.integrationlab.engine.xml;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * Converts XML data to fixed-length format
 */
@Service
public class XmlToFixedLengthConverter {
    
    /**
     * Configuration for fixed-length generation
     */
    public static class FixedLengthConfig {
        private Map<String, Integer> fieldLengths; // Field name to length mapping
        private String padCharacter = " ";
        private String lineTerminator = "\n";
        private Map<String, String> fieldMappings; // XML path to field name mapping
        private List<String> fieldOrder; // Order of fields in output
        private PadDirection padDirection = PadDirection.RIGHT;
        
        public enum PadDirection {
            LEFT, RIGHT
        }
        
        // Builder pattern
        public static FixedLengthConfig builder() {
            return new FixedLengthConfig();
        }
        
        public FixedLengthConfig fieldLengths(Map<String, Integer> fieldLengths) {
            this.fieldLengths = fieldLengths;
            return this;
        }
        
        public FixedLengthConfig padCharacter(String padCharacter) {
            this.padCharacter = padCharacter;
            return this;
        }
        
        public FixedLengthConfig lineTerminator(String lineTerminator) {
            this.lineTerminator = lineTerminator;
            return this;
        }
        
        public FixedLengthConfig fieldMappings(Map<String, String> fieldMappings) {
            this.fieldMappings = fieldMappings;
            return this;
        }
        
        public FixedLengthConfig fieldOrder(List<String> fieldOrder) {
            this.fieldOrder = fieldOrder;
            return this;
        }
        
        public FixedLengthConfig padDirection(PadDirection padDirection) {
            this.padDirection = padDirection;
            return this;
        }
        
        // Getters
        public Map<String, Integer> getFieldLengths() { return fieldLengths; }
        public String getPadCharacter() { return padCharacter; }
        public String getLineTerminator() { return lineTerminator; }
        public Map<String, String> getFieldMappings() { return fieldMappings; }
        public List<String> getFieldOrder() { return fieldOrder; }
        public PadDirection getPadDirection() { return padDirection; }
    }
    
    /**
     * Convert XML to fixed-length format
     */
    public String convertToFixedLength(String xmlContent, FixedLengthConfig config) throws XmlConversionException {
        try {
            if (config.getFieldLengths() == null || config.getFieldLengths().isEmpty()) {
                throw new XmlConversionException("Field lengths must be specified for fixed-length format");
            }
            
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            
            Element rootElement = doc.getDocumentElement();
            List<Map<String, String>> records = extractRecords(rootElement, config);
            
            if (records.isEmpty()) {
                return "";
            }
            
            // Generate fixed-length output
            StringWriter output = new StringWriter();
            
            // Determine field order
            List<String> fields = determineFieldOrder(config);
            
            // Write each record
            for (Map<String, String> record : records) {
                String line = generateFixedLengthLine(record, fields, config);
                output.write(line);
                output.write(config.getLineTerminator());
            }
            
            return output.toString();
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert XML to fixed-length format", e);
        }
    }
    
    /**
     * Extract records from XML
     */
    private List<Map<String, String>> extractRecords(Element root, FixedLengthConfig config) {
        List<Map<String, String>> records = new ArrayList<>();
        
        // Find record elements
        List<Element> recordElements = findRecordElements(root);
        
        for (Element recordElement : recordElements) {
            Map<String, String> record = extractRecord(recordElement, config);
            if (!record.isEmpty()) {
                records.add(record);
            }
        }
        
        return records;
    }
    
    /**
     * Find record elements in XML
     */
    private List<Element> findRecordElements(Element root) {
        List<Element> records = new ArrayList<>();
        
        // Check if root contains repeating elements
        Map<String, List<Element>> childrenByTag = new HashMap<>();
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                childrenByTag.computeIfAbsent(tagName, k -> new ArrayList<>()).add(childElement);
            }
        }
        
        // Find the repeating element (likely the record element)
        for (Map.Entry<String, List<Element>> entry : childrenByTag.entrySet()) {
            if (entry.getValue().size() > 1) {
                records.addAll(entry.getValue());
                break;
            }
        }
        
        // If no repeating elements, treat root as single record
        if (records.isEmpty()) {
            records.add(root);
        }
        
        return records;
    }
    
    /**
     * Extract a single record from XML element
     */
    private Map<String, String> extractRecord(Element element, FixedLengthConfig config) {
        Map<String, String> record = new HashMap<>();
        
        if (config.getFieldMappings() != null && !config.getFieldMappings().isEmpty()) {
            // Use explicit field mappings
            for (Map.Entry<String, String> mapping : config.getFieldMappings().entrySet()) {
                String xmlPath = mapping.getKey();
                String fieldName = mapping.getValue();
                String value = getValueByPath(element, xmlPath);
                if (value != null) {
                    record.put(fieldName, value);
                }
            }
        } else {
            // Auto-extract all leaf values
            extractLeafValues(element, "", record);
        }
        
        return record;
    }
    
    /**
     * Extract all leaf values from element
     */
    private void extractLeafValues(Element element, String prefix, Map<String, String> values) {
        NodeList children = element.getChildNodes();
        boolean hasElementChildren = false;
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasElementChildren = true;
                Element childElement = (Element) child;
                String childName = childElement.getTagName();
                
                if (!hasChildElements(childElement)) {
                    // Leaf element
                    values.put(childName, childElement.getTextContent().trim());
                } else {
                    // Recurse
                    String childPrefix = prefix.isEmpty() ? childName : prefix + "." + childName;
                    extractLeafValues(childElement, childPrefix, values);
                }
            }
        }
        
        // If no element children and has text content, add text value
        if (!hasElementChildren) {
            String text = element.getTextContent().trim();
            if (!text.isEmpty()) {
                values.put(prefix.isEmpty() ? element.getTagName() : prefix, text);
            }
        }
    }
    
    /**
     * Get value by XML path
     */
    private String getValueByPath(Element element, String path) {
        String[] parts = path.split("\\.");
        Node current = element;
        
        for (String part : parts) {
            if (part.startsWith("@")) {
                // Attribute
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    return ((Element) current).getAttribute(part.substring(1));
                }
                return null;
            } else {
                // Element
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList children = current.getChildNodes();
                    boolean found = false;
                    for (int i = 0; i < children.getLength(); i++) {
                        Node child = children.item(i);
                        if (child.getNodeType() == Node.ELEMENT_NODE && 
                            child.getNodeName().equals(part)) {
                            current = child;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return null;
                    }
                }
            }
        }
        
        return current.getTextContent().trim();
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
     * Determine field order for output
     */
    private List<String> determineFieldOrder(FixedLengthConfig config) {
        if (config.getFieldOrder() != null && !config.getFieldOrder().isEmpty()) {
            return config.getFieldOrder();
        }
        
        // Use the order from field lengths map
        return new ArrayList<>(config.getFieldLengths().keySet());
    }
    
    /**
     * Generate a fixed-length line
     */
    private String generateFixedLengthLine(Map<String, String> record, List<String> fields, FixedLengthConfig config) {
        StringBuilder line = new StringBuilder();
        
        for (String field : fields) {
            Integer length = config.getFieldLengths().get(field);
            if (length == null) {
                continue; // Skip fields without defined length
            }
            
            String value = record.getOrDefault(field, "");
            String paddedValue = padField(value, length, config.getPadCharacter(), config.getPadDirection());
            line.append(paddedValue);
        }
        
        return line.toString();
    }
    
    /**
     * Pad a field to the specified length
     */
    private String padField(String value, int length, String padChar, FixedLengthConfig.PadDirection direction) {
        if (value.length() > length) {
            // Truncate if too long
            return value.substring(0, length);
        }
        
        if (value.length() == length) {
            return value;
        }
        
        // Calculate padding needed
        int padCount = length - value.length();
        String padding = padChar.repeat(padCount);
        
        return direction == FixedLengthConfig.PadDirection.LEFT ? 
            padding + value : 
            value + padding;
    }
}