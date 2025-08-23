package com.integrixs.engine.xml;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts XML data to SQL statements for JDBC adapters
 */
@Service
public class XmlToSqlConverter {
    
    /**
     * Configuration for SQL generation
     */
    public static class SqlGenerationConfig {
        private String tableName;
        private String operation; // INSERT, UPDATE, DELETE, SELECT
        private String whereClause;
        private Map<String, String> fieldMappings; // XML path to DB column mapping
        private boolean generateBatch;
        
        // Getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getWhereClause() { return whereClause; }
        public void setWhereClause(String whereClause) { this.whereClause = whereClause; }
        
        public Map<String, String> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(Map<String, String> fieldMappings) { this.fieldMappings = fieldMappings; }
        
        public boolean isGenerateBatch() { return generateBatch; }
        public void setGenerateBatch(boolean generateBatch) { this.generateBatch = generateBatch; }
    }
    
    /**
     * Convert XML to SQL statement(s)
     */
    public List<String> convertToSql(String xmlContent, SqlGenerationConfig config) throws XmlConversionException {
        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            
            Element rootElement = doc.getDocumentElement();
            List<String> sqlStatements = new ArrayList<>();
            
            // Check if root contains multiple records
            List<Element> recordElements = getRecordElements(rootElement);
            
            for (Element record : recordElements) {
                String sql = generateSqlForRecord(record, config);
                if (sql != null && !sql.isEmpty()) {
                    sqlStatements.add(sql);
                }
            }
            
            return sqlStatements;
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert XML to SQL", e);
        }
    }
    
    /**
     * Get record elements from root
     */
    private List<Element> getRecordElements(Element root) {
        List<Element> records = new ArrayList<>();
        
        // Check if root itself is a record or contains records
        NodeList children = root.getChildNodes();
        boolean hasOnlyElements = true;
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                // If all children have the same tag name, treat them as records
                if (records.isEmpty() || records.get(0).getTagName().equals(childElement.getTagName())) {
                    records.add(childElement);
                } else {
                    hasOnlyElements = false;
                    break;
                }
            }
        }
        
        // If root has mixed content or single record, treat root as the record
        if (!hasOnlyElements || records.size() <= 1) {
            records.clear();
            records.add(root);
        }
        
        return records;
    }
    
    /**
     * Generate SQL for a single record
     */
    private String generateSqlForRecord(Element record, SqlGenerationConfig config) {
        Map<String, Object> values = extractValues(record, config.getFieldMappings());
        
        if (values.isEmpty()) {
            return null;
        }
        
        switch (config.getOperation().toUpperCase()) {
            case "INSERT":
                return generateInsertStatement(config.getTableName(), values);
            case "UPDATE":
                return generateUpdateStatement(config.getTableName(), values, config.getWhereClause());
            case "DELETE":
                return generateDeleteStatement(config.getTableName(), config.getWhereClause());
            case "SELECT":
                return generateSelectStatement(config.getTableName(), values.keySet(), config.getWhereClause());
            default:
                throw new IllegalArgumentException("Unsupported operation: " + config.getOperation());
        }
    }
    
    /**
     * Extract values from XML element based on field mappings
     */
    private Map<String, Object> extractValues(Element element, Map<String, String> fieldMappings) {
        Map<String, Object> values = new HashMap<>();
        
        if (fieldMappings == null || fieldMappings.isEmpty()) {
            // Auto-map all child elements
            extractAllValues(element, "", values);
        } else {
            // Use explicit mappings
            for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
                String xmlPath = mapping.getKey();
                String dbColumn = mapping.getValue();
                Object value = getValueByPath(element, xmlPath);
                if (value != null) {
                    values.put(dbColumn, value);
                }
            }
        }
        
        return values;
    }
    
    /**
     * Extract all values from element recursively
     */
    private void extractAllValues(Element element, String prefix, Map<String, Object> values) {
        // Process attributes
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if (!attr.getNodeName().startsWith("xmlns")) {
                String key = prefix + "@" + attr.getNodeName();
                values.put(key.replaceAll("[^a-zA-Z0-9_]", "_"), attr.getNodeValue());
            }
        }
        
        // Process child elements
        NodeList children = element.getChildNodes();
        Map<String, Integer> elementCounts = new HashMap<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String childName = childElement.getTagName();
                
                if (!hasChildElements(childElement)) {
                    // Leaf element - get text content
                    String key = prefix + childName;
                    values.put(key.replaceAll("[^a-zA-Z0-9_]", "_"), childElement.getTextContent().trim());
                }
            }
        }
    }
    
    /**
     * Get value by XML path
     */
    private Object getValueByPath(Element element, String path) {
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
                } else {
                    return null;
                }
            }
        }
        
        // Return text content of final node
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
     * Generate INSERT statement
     */
    private String generateInsertStatement(String tableName, Map<String, Object> values) {
        List<String> columns = new ArrayList<>(values.keySet());
        List<String> placeholders = values.values().stream()
            .map(this::formatValue)
            .collect(Collectors.toList());
        
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
            tableName,
            String.join(", ", columns),
            String.join(", ", placeholders));
    }
    
    /**
     * Generate UPDATE statement
     */
    private String generateUpdateStatement(String tableName, Map<String, Object> values, String whereClause) {
        List<String> setClauses = values.entrySet().stream()
            .map(e -> e.getKey() + " = " + formatValue(e.getValue()))
            .collect(Collectors.toList());
        
        String sql = String.format("UPDATE %s SET %s", tableName, String.join(", ", setClauses));
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        
        return sql;
    }
    
    /**
     * Generate DELETE statement
     */
    private String generateDeleteStatement(String tableName, String whereClause) {
        String sql = "DELETE FROM " + tableName;
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        
        return sql;
    }
    
    /**
     * Generate SELECT statement
     */
    private String generateSelectStatement(String tableName, java.util.Set<String> columns, String whereClause) {
        String sql = String.format("SELECT %s FROM %s",
            columns.isEmpty() ? "*" : String.join(", ", columns),
            tableName);
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        
        return sql;
    }
    
    /**
     * Format value for SQL
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        
        String strValue = value.toString();
        
        // Check if numeric
        try {
            Double.parseDouble(strValue);
            return strValue;
        } catch (NumberFormatException e) {
            // Not numeric, treat as string
        }
        
        // Check if boolean
        if ("true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)) {
            return strValue.toUpperCase();
        }
        
        // Escape quotes and wrap in quotes
        return "'" + strValue.replace("'", "''") + "'";
    }
}