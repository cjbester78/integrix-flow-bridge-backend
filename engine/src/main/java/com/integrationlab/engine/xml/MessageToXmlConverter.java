package com.integrationlab.engine.xml;

import com.integrationlab.adapters.config.*;
import com.integrationlab.shared.dto.adapter.JsonXmlWrapperConfig;
import com.integrationlab.shared.dto.adapter.XmlMappingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Central service for converting messages from various adapter types to XML
 */
@Service
public class MessageToXmlConverter {
    
    private final JsonToXmlConverter jsonToXmlConverter;
    private final JdbcToXmlConverter jdbcToXmlConverter;
    private final CsvToXmlConverter csvToXmlConverter;
    
    @Autowired
    public MessageToXmlConverter(JsonToXmlConverter jsonToXmlConverter,
                                JdbcToXmlConverter jdbcToXmlConverter,
                                CsvToXmlConverter csvToXmlConverter) {
        this.jsonToXmlConverter = jsonToXmlConverter;
        this.jdbcToXmlConverter = jdbcToXmlConverter;
        this.csvToXmlConverter = csvToXmlConverter;
    }
    
    /**
     * Convert message data to XML based on adapter configuration
     * 
     * @param messageData The raw message data from the adapter
     * @param adapterConfig The adapter configuration
     * @return XML string representation of the message
     * @throws XmlConversionException if conversion fails
     */
    public String convertToXml(Object messageData, Object adapterConfig) throws XmlConversionException {
        if (adapterConfig == null) {
            throw new XmlConversionException("Adapter configuration is required");
        }
        
        // Determine adapter type and get appropriate configuration
        if (adapterConfig instanceof JdbcSenderAdapterConfig) {
            JdbcSenderAdapterConfig jdbcConfig = (JdbcSenderAdapterConfig) adapterConfig;
            return jdbcToXmlConverter.convertToXml(messageData, jdbcConfig.getXmlMappingConfig());
            
        } else if (adapterConfig instanceof FileSenderAdapterConfig) {
            FileSenderAdapterConfig fileConfig = (FileSenderAdapterConfig) adapterConfig;
            
            // Determine file type based on configuration or content
            if (isJsonContent(messageData)) {
                // If file contains JSON, use JSON converter with default wrapper
                JsonXmlWrapperConfig wrapperConfig = JsonXmlWrapperConfig.builder()
                        .rootElementName("fileData")
                        .includeXmlDeclaration(true)
                        .prettyPrint(true)
                        .build();
                return jsonToXmlConverter.convertToXml(messageData, wrapperConfig);
            } else {
                // Assume CSV or delimited text
                return csvToXmlConverter.convertToXml(messageData, fileConfig.getXmlMappingConfig());
            }
            
        } else if (adapterConfig instanceof HttpSenderAdapterConfig) {
            HttpSenderAdapterConfig httpConfig = (HttpSenderAdapterConfig) adapterConfig;
            return jsonToXmlConverter.convertToXml(messageData, httpConfig.getJsonXmlWrapperConfig());
            
        } else if (adapterConfig instanceof RestSenderAdapterConfig) {
            RestSenderAdapterConfig restConfig = (RestSenderAdapterConfig) adapterConfig;
            return jsonToXmlConverter.convertToXml(messageData, restConfig.getJsonXmlWrapperConfig());
            
        } else if (isXmlContent(messageData)) {
            // Already XML, return as-is
            return messageData.toString();
            
        } else {
            // For other adapter types, use a default conversion
            return convertWithDefaultConfig(messageData, adapterConfig);
        }
    }
    
    private boolean isJsonContent(Object data) {
        if (data instanceof String) {
            String content = ((String) data).trim();
            return (content.startsWith("{") && content.endsWith("}")) ||
                   (content.startsWith("[") && content.endsWith("]"));
        }
        return false;
    }
    
    private boolean isXmlContent(Object data) {
        if (data instanceof String) {
            String content = ((String) data).trim();
            return content.startsWith("<?xml") || content.startsWith("<");
        }
        return false;
    }
    
    private String convertWithDefaultConfig(Object messageData, Object adapterConfig) 
            throws XmlConversionException {
        // Default conversion for unknown adapter types
        String adapterType = adapterConfig.getClass().getSimpleName();
        
        if (isJsonContent(messageData)) {
            JsonXmlWrapperConfig defaultConfig = JsonXmlWrapperConfig.builder()
                    .rootElementName("message")
                    .namespaceUri("http://integrationlab.com/adapter/" + adapterType)
                    .namespacePrefix("msg")
                    .includeXmlDeclaration(true)
                    .prettyPrint(true)
                    .build();
            return jsonToXmlConverter.convertToXml(messageData, defaultConfig);
        } else {
            // Create a simple XML wrapper for non-structured data
            XmlMappingConfig defaultConfig = XmlMappingConfig.builder()
                    .rootElementName("message")
                    .rowElementName("data")
                    .includeXmlDeclaration(true)
                    .prettyPrint(true)
                    .build();
            
            // Wrap the data in a map for conversion
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("content", messageData);
            
            return jdbcToXmlConverter.convertToXml(
                java.util.Collections.singletonList(dataMap), defaultConfig);
        }
    }
    
    /**
     * Get the appropriate XML configuration for an adapter
     * 
     * @param adapterConfig The adapter configuration
     * @return The XML configuration (XmlMappingConfig or JsonXmlWrapperConfig)
     */
    public Object getXmlConfig(Object adapterConfig) {
        if (adapterConfig instanceof JdbcSenderAdapterConfig) {
            return ((JdbcSenderAdapterConfig) adapterConfig).getXmlMappingConfig();
        } else if (adapterConfig instanceof FileSenderAdapterConfig) {
            return ((FileSenderAdapterConfig) adapterConfig).getXmlMappingConfig();
        } else if (adapterConfig instanceof HttpSenderAdapterConfig) {
            return ((HttpSenderAdapterConfig) adapterConfig).getJsonXmlWrapperConfig();
        } else if (adapterConfig instanceof RestSenderAdapterConfig) {
            return ((RestSenderAdapterConfig) adapterConfig).getJsonXmlWrapperConfig();
        }
        return null;
    }
}