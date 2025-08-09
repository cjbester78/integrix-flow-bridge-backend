package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;
import com.integrationlab.engine.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to handle format conversions between different adapter types
 */
@Service
public class FormatConversionService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private JsonToXmlConverter jsonToXmlConverter;
    
    @Autowired
    private XmlToJsonConverter xmlToJsonConverter;
    
    @Autowired
    private XmlToSqlConverter xmlToSqlConverter;
    
    @Autowired
    private XmlToCsvConverter xmlToCsvConverter;
    
    @Autowired
    private XmlToFixedLengthConverter xmlToFixedLengthConverter;
    
    /**
     * Convert data to XML format based on source adapter type
     */
    public String convertToXml(Object data, CommunicationAdapter sourceAdapter) throws XmlConversionException {
        String adapterType = sourceAdapter.getType().name();
        
        switch (adapterType) {
            case "HTTP_REST":
            case "HTTP":
            case "REST":
                // Assume REST APIs return JSON
                return jsonToXmlConverter.convertToXml(data, createJsonXmlConfig(sourceAdapter));
                
            case "SOAP":
            case "SOAP_WS":
                // Already XML
                return data.toString();
                
            case "JDBC":
                // JDBC results need special handling
                throw new XmlConversionException("JDBC to XML conversion requires JdbcToXmlConverter");
                
            case "FILE":
            case "FTP":
            case "SFTP":
                // Depends on file format - for now assume it's already handled
                return data.toString();
                
            default:
                // Try JSON conversion as default
                return jsonToXmlConverter.convertToXml(data, createJsonXmlConfig(sourceAdapter));
        }
    }
    
    /**
     * Convert XML back to target format based on target adapter type
     */
    public Object convertFromXml(String xmlContent, CommunicationAdapter targetAdapter, Map<String, Object> conversionConfig) 
            throws XmlConversionException {
        String adapterType = targetAdapter.getType().name();
        
        switch (adapterType) {
            case "HTTP_REST":
            case "HTTP":
            case "REST":
                // Convert to JSON for REST APIs
                boolean removeRoot = shouldRemoveRootElement(targetAdapter);
                return xmlToJsonConverter.convertToJson(xmlContent, removeRoot);
                
            case "SOAP":
            case "SOAP_WS":
                // Keep as XML
                return xmlContent;
                
            case "JDBC":
                // Convert to SQL statements
                return convertToSql(xmlContent, targetAdapter, conversionConfig);
                
            case "FILE":
            case "FTP":
            case "SFTP":
                // Check file format from adapter config
                String fileFormat = getFileFormat(targetAdapter);
                if ("CSV".equalsIgnoreCase(fileFormat)) {
                    return convertToCsv(xmlContent, conversionConfig);
                } else if ("FIXED".equalsIgnoreCase(fileFormat)) {
                    return convertToFixedLength(xmlContent, conversionConfig);
                } else if ("JSON".equalsIgnoreCase(fileFormat)) {
                    return xmlToJsonConverter.convertToJson(xmlContent, true);
                } else {
                    // Default to XML
                    return xmlContent;
                }
                
            case "JMS":
            case "KAFKA":
                // Message queues typically accept XML or JSON
                String messageFormat = getMessageFormat(targetAdapter);
                if ("JSON".equalsIgnoreCase(messageFormat)) {
                    return xmlToJsonConverter.convertToJson(xmlContent, true);
                } else {
                    return xmlContent;
                }
                
            default:
                // Default to XML
                return xmlContent;
        }
    }
    
    /**
     * Create JSON to XML configuration from adapter settings
     */
    private com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig createJsonXmlConfig(CommunicationAdapter adapter) {
        Map<String, Object> adapterConfig = parseConfiguration(adapter.getConfiguration());
        Map<String, Object> xmlConfig = new HashMap<>();
        
        // Extract XML conversion config if present
        if (adapterConfig != null && adapterConfig.containsKey("xmlConversion")) {
            Object xmlConversionObj = adapterConfig.get("xmlConversion");
            if (xmlConversionObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> xmlConversionMap = (Map<String, Object>) xmlConversionObj;
                xmlConfig = xmlConversionMap;
            }
        }
        
        // Build configuration with defaults and overrides from adapter config
        com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig.JsonXmlWrapperConfigBuilder builder = 
            com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig.builder()
                .rootElementName((String) xmlConfig.getOrDefault("rootElementName", "Message"))
                .includeXmlDeclaration((Boolean) xmlConfig.getOrDefault("includeXmlDeclaration", true))
                .prettyPrint((Boolean) xmlConfig.getOrDefault("prettyPrint", true))
                .encoding((String) xmlConfig.getOrDefault("encoding", "UTF-8"))
                .convertPropertyNames(true)
                .preserveNullValues(false);
        
        // Add namespace if configured
        if (xmlConfig.containsKey("targetNamespace")) {
            builder.namespaceUri((String) xmlConfig.get("targetNamespace"));
        }
        if (xmlConfig.containsKey("namespacePrefix")) {
            builder.namespacePrefix((String) xmlConfig.get("namespacePrefix"));
        }
        
        return builder.build();
    }
    
    /**
     * Convert XML to SQL
     */
    private List<String> convertToSql(String xmlContent, CommunicationAdapter adapter, Map<String, Object> config) 
            throws XmlConversionException {
        XmlToSqlConverter.SqlGenerationConfig sqlConfig = new XmlToSqlConverter.SqlGenerationConfig();
        
        // Get configuration from adapter or conversion config
        sqlConfig.setTableName(getConfigValue(config, "tableName", adapter.getName()));
        sqlConfig.setOperation(getConfigValue(config, "operation", "INSERT"));
        sqlConfig.setWhereClause(getConfigValue(config, "whereClause", null));
        sqlConfig.setGenerateBatch(Boolean.parseBoolean(getConfigValue(config, "generateBatch", "false")));
        
        // Field mappings if provided
        @SuppressWarnings("unchecked")
        Map<String, String> fieldMappings = (Map<String, String>) config.get("fieldMappings");
        sqlConfig.setFieldMappings(fieldMappings);
        
        return xmlToSqlConverter.convertToSql(xmlContent, sqlConfig);
    }
    
    /**
     * Convert XML to CSV
     */
    private String convertToCsv(String xmlContent, Map<String, Object> config) throws XmlConversionException {
        XmlToCsvConverter.CsvGenerationConfig csvConfig = XmlToCsvConverter.CsvGenerationConfig.builder()
            .delimiter(getConfigValue(config, "delimiter", ","))
            .includeHeaders(Boolean.parseBoolean(getConfigValue(config, "includeHeaders", "true")))
            .quoteAllFields(Boolean.parseBoolean(getConfigValue(config, "quoteAllFields", "false")));
        
        // Field mappings if provided
        @SuppressWarnings("unchecked")
        Map<String, String> fieldMappings = (Map<String, String>) config.get("fieldMappings");
        if (fieldMappings != null) {
            csvConfig.fieldMappings(fieldMappings);
        }
        
        // Column order if provided
        @SuppressWarnings("unchecked")
        List<String> columnOrder = (List<String>) config.get("columnOrder");
        if (columnOrder != null) {
            csvConfig.columnOrder(columnOrder);
        }
        
        return xmlToCsvConverter.convertToCsv(xmlContent, csvConfig);
    }
    
    /**
     * Convert XML to fixed-length format
     */
    private String convertToFixedLength(String xmlContent, Map<String, Object> config) throws XmlConversionException {
        XmlToFixedLengthConverter.FixedLengthConfig fixedConfig = XmlToFixedLengthConverter.FixedLengthConfig.builder()
            .padCharacter(getConfigValue(config, "padCharacter", " "))
            .lineTerminator(getConfigValue(config, "lineTerminator", "\n"));
        
        // Field lengths (required for fixed-length format)
        @SuppressWarnings("unchecked")
        Map<String, Integer> fieldLengths = (Map<String, Integer>) config.get("fieldLengths");
        if (fieldLengths != null) {
            fixedConfig.fieldLengths(fieldLengths);
        } else {
            throw new XmlConversionException("Field lengths must be specified for fixed-length format");
        }
        
        // Field mappings if provided
        @SuppressWarnings("unchecked")
        Map<String, String> fieldMappings = (Map<String, String>) config.get("fieldMappings");
        if (fieldMappings != null) {
            fixedConfig.fieldMappings(fieldMappings);
        }
        
        // Field order if provided
        @SuppressWarnings("unchecked")
        List<String> fieldOrder = (List<String>) config.get("fieldOrder");
        if (fieldOrder != null) {
            fixedConfig.fieldOrder(fieldOrder);
        }
        
        // Pad direction
        String padDirection = getConfigValue(config, "padDirection", "RIGHT");
        if ("LEFT".equalsIgnoreCase(padDirection)) {
            fixedConfig.padDirection(XmlToFixedLengthConverter.FixedLengthConfig.PadDirection.LEFT);
        }
        
        return xmlToFixedLengthConverter.convertToFixedLength(xmlContent, fixedConfig);
    }
    
    /**
     * Get file format from adapter configuration
     */
    private String getFileFormat(CommunicationAdapter adapter) {
        // Check adapter configuration for file format
        // This would typically come from adapter-specific configuration
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        if (config != null) {
            Object format = config.get("fileFormat");
            if (format != null) {
                return format.toString();
            }
        }
        
        // Try to infer from adapter name
        String name = adapter.getName().toLowerCase();
        if (name.contains("csv")) {
            return "CSV";
        } else if (name.contains("json")) {
            return "JSON";
        } else if (name.contains("xml")) {
            return "XML";
        }
        
        return "XML"; // Default
    }
    
    /**
     * Get message format from adapter configuration
     */
    private String getMessageFormat(CommunicationAdapter adapter) {
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        if (config != null) {
            Object format = config.get("messageFormat");
            if (format != null) {
                return format.toString();
            }
        }
        return "XML"; // Default
    }
    
    /**
     * Parse JSON configuration string to Map
     */
    private Map<String, Object> parseConfiguration(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // Log error and return empty map
            return new HashMap<>();
        }
    }
    
    /**
     * Get configuration value with fallback
     */
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config != null && config.containsKey(key)) {
            Object value = config.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }
    
    /**
     * Check if root element should be removed based on adapter configuration
     */
    private boolean shouldRemoveRootElement(CommunicationAdapter adapter) {
        Map<String, Object> adapterConfig = parseConfiguration(adapter.getConfiguration());
        
        if (adapterConfig != null && adapterConfig.containsKey("xmlConversion")) {
            Object xmlConversionObj = adapterConfig.get("xmlConversion");
            if (xmlConversionObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> xmlConfig = (Map<String, Object>) xmlConversionObj;
                Object removeRoot = xmlConfig.get("removeRootElement");
                if (removeRoot instanceof Boolean) {
                    return (Boolean) removeRoot;
                }
            }
        }
        
        // Default to true for backward compatibility
        return true;
    }
}