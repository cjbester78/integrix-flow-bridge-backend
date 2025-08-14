package com.integrixs.engine.xml;

import com.integrixs.shared.dto.adapter.XmlMappingConfig;
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

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts CSV data to XML format
 */
@Service
public class CsvToXmlConverter implements XmlConversionService {
    
    @Override
    public String convertToXml(Object data, Object config) throws XmlConversionException {
        if (!(config instanceof XmlMappingConfig)) {
            throw new XmlConversionException("Configuration must be of type XmlMappingConfig");
        }
        
        XmlMappingConfig mappingConfig = (XmlMappingConfig) config;
        
        try {
            String csvData;
            if (data instanceof String) {
                csvData = (String) data;
            } else {
                throw new XmlConversionException("Data must be a String containing CSV data");
            }
            
            List<Map<String, String>> rows = parseCsv(csvData);
            return createXmlDocument(rows, mappingConfig);
            
        } catch (Exception e) {
            throw new XmlConversionException("Failed to convert CSV to XML", e);
        }
    }
    
    private List<Map<String, String>> parseCsv(String csvData) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return rows;
        }
        
        String[] headers = parseCsvLine(headerLine);
        
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = parseCsvLine(line);
            Map<String, String> row = new HashMap<>();
            
            for (int i = 0; i < headers.length && i < values.length; i++) {
                row.put(headers[i].trim(), values[i].trim());
            }
            
            rows.add(row);
        }
        
        return rows;
    }
    
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(ch);
            }
        }
        
        values.add(currentValue.toString());
        return values.toArray(new String[0]);
    }
    
    private String createXmlDocument(List<Map<String, String>> rows, XmlMappingConfig config) 
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Create root element
        Element rootElement = createRootElement(doc, config);
        doc.appendChild(rootElement);
        
        // Process each row
        for (Map<String, String> row : rows) {
            Element rowElement = createRowElement(doc, config);
            rootElement.appendChild(rowElement);
            
            // Process each field in the row
            for (Map.Entry<String, String> field : row.entrySet()) {
                String fieldName = field.getKey();
                String fieldValue = field.getValue();
                
                // Get mapped element name
                String elementName = config.getFieldToElementMapping().getOrDefault(fieldName, fieldName);
                
                Element fieldElement = createElement(doc, elementName, config);
                
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fieldElement.setTextContent(fieldValue);
                    
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
        // Clean element name to be XML-compliant
        name = cleanElementName(name);
        
        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            return doc.createElementNS(config.getNamespace(), 
                config.getNamespacePrefix() != null 
                    ? config.getNamespacePrefix() + ":" + name
                    : name);
        } else {
            return doc.createElement(name);
        }
    }
    
    private String cleanElementName(String name) {
        // Replace spaces and special characters with underscores
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_")
                  .replaceAll("^[0-9]", "_$0"); // Ensure it doesn't start with a number
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
        return String.class.isAssignableFrom(dataType);
    }
}