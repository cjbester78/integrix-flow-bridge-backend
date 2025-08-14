package com.integrixs.shared.dto.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for mapping non-JSON data (JDBC, CSV, etc.) to XML structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlMappingConfig {
    
    /**
     * Root element name for the XML document
     * Example: "products", "customers", "orders"
     */
    private String rootElementName;
    
    /**
     * Element name for each row/record
     * Example: "product", "customer", "order"
     */
    private String rowElementName;
    
    /**
     * XML namespace URI
     * Example: "http://company.com/integration/v1"
     */
    private String namespace;
    
    /**
     * XML namespace prefix
     * Example: "int", "prod", etc.
     */
    private String namespacePrefix;
    
    /**
     * Mapping from source field names to XML element names
     * Key: Source field name (e.g., "customer_id")
     * Value: Target XML element name (e.g., "customerId")
     */
    @Builder.Default
    private Map<String, String> fieldToElementMapping = new HashMap<>();
    
    /**
     * Data type hints for fields
     * Key: Field name
     * Value: Data type (e.g., "string", "number", "boolean", "date")
     */
    @Builder.Default
    private Map<String, String> fieldDataTypes = new HashMap<>();
    
    /**
     * Whether to include XML declaration
     */
    @Builder.Default
    private boolean includeXmlDeclaration = true;
    
    /**
     * Whether to pretty print the XML
     */
    @Builder.Default
    private boolean prettyPrint = true;
    
    /**
     * Character encoding for XML
     */
    @Builder.Default
    private String encoding = "UTF-8";
}