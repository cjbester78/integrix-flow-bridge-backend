package com.integrationlab.engine.xml;

import com.integrationlab.shared.dto.adapter.XmlMappingConfig;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts JDBC ResultSet data to XML format
 */
@Service
public class JdbcToXmlConverter implements XmlConversionService {
    
    @Override
    public String convertToXml(Object data, Object config) throws XmlConversionException {
        if (!(config instanceof XmlMappingConfig)) {
            throw new XmlConversionException("Configuration must be of type XmlMappingConfig");
        }
        
        XmlMappingConfig mappingConfig = (XmlMappingConfig) config;
        
        try {
            List<Map<String, Object>> rows;
            
            if (data instanceof ResultSet) {
                rows = convertResultSetToList((ResultSet) data);
            } else if (data instanceof List) {
                rows = (List<Map<String, Object>>) data;
            } else {
                throw new XmlConversionException("Data must be a ResultSet or List<Map<String, Object>>");
            }
            
            return createXmlDocument(rows, mappingConfig);
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert JDBC data to XML", e);
        }
    }
    
    private List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }
        
        return rows;
    }
    
    private String createXmlDocument(List<Map<String, Object>> rows, XmlMappingConfig config) 
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Create root element
        Element rootElement = createRootElement(doc, config);
        doc.appendChild(rootElement);
        
        // Process each row
        for (Map<String, Object> row : rows) {
            Element rowElement = createRowElement(doc, config);
            rootElement.appendChild(rowElement);
            
            // Process each field in the row
            for (Map.Entry<String, Object> field : row.entrySet()) {
                String fieldName = field.getKey();
                Object fieldValue = field.getValue();
                
                // Get mapped element name
                String elementName = config.getFieldToElementMapping().getOrDefault(fieldName, fieldName);
                
                Element fieldElement = createElement(doc, elementName, config);
                
                // Set value with appropriate type handling
                if (fieldValue != null) {
                    String textValue = convertValueToString(fieldValue, fieldName, config);
                    fieldElement.setTextContent(textValue);
                    
                    // Add type attribute if configured
                    String dataType = config.getFieldDataTypes().get(fieldName);
                    if (dataType != null) {
                        fieldElement.setAttribute("type", dataType);
                    }
                }
                
                rowElement.appendChild(fieldElement);
            }
        }
        
        return transformDocumentToString(doc, config);
    }
    
    private Element createRootElement(Document doc, XmlMappingConfig config) {
        Element rootElement;
        
        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            rootElement = doc.createElementNS(config.getNamespace(), 
                config.getNamespacePrefix() != null 
                    ? config.getNamespacePrefix() + ":" + config.getRootElementName()
                    : config.getRootElementName());
            
            // Add namespace declaration
            if (config.getNamespacePrefix() != null) {
                rootElement.setAttribute("xmlns:" + config.getNamespacePrefix(), 
                    config.getNamespace());
            } else {
                rootElement.setAttribute("xmlns", config.getNamespace());
            }
        } else {
            rootElement = doc.createElement(config.getRootElementName());
        }
        
        return rootElement;
    }
    
    private Element createRowElement(Document doc, XmlMappingConfig config) {
        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            return doc.createElementNS(config.getNamespace(), 
                config.getNamespacePrefix() != null 
                    ? config.getNamespacePrefix() + ":" + config.getRowElementName()
                    : config.getRowElementName());
        } else {
            return doc.createElement(config.getRowElementName());
        }
    }
    
    private Element createElement(Document doc, String name, XmlMappingConfig config) {
        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            return doc.createElementNS(config.getNamespace(), 
                config.getNamespacePrefix() != null 
                    ? config.getNamespacePrefix() + ":" + name
                    : name);
        } else {
            return doc.createElement(name);
        }
    }
    
    private String convertValueToString(Object value, String fieldName, XmlMappingConfig config) {
        if (value == null) {
            return "";
        }
        
        // Check if there's a specific data type configured
        String dataType = config.getFieldDataTypes().get(fieldName);
        
        if (dataType != null) {
            switch (dataType.toLowerCase()) {
                case "date":
                    // Format date as ISO 8601
                    if (value instanceof java.sql.Date) {
                        return value.toString();
                    } else if (value instanceof java.sql.Timestamp) {
                        return ((java.sql.Timestamp) value).toInstant().toString();
                    }
                    break;
                case "boolean":
                    return Boolean.toString(Boolean.parseBoolean(value.toString()));
                case "number":
                case "integer":
                case "decimal":
                    return value.toString();
                default:
                    return value.toString();
            }
        }
        
        return value.toString();
    }
    
    private String transformDocumentToString(Document doc, XmlMappingConfig config) 
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
        return ResultSet.class.isAssignableFrom(dataType) || 
               List.class.isAssignableFrom(dataType);
    }
}