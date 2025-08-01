package com.integrationlab.shared.dto.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for wrapping JSON data in XML structure with namespace support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonXmlWrapperConfig {
    
    /**
     * Root element name for the XML document
     * Example: "OrderMessage", "CustomerData", "ProductCatalog"
     */
    private String rootElementName;
    
    /**
     * XML namespace URI
     * Example: "http://company.com/orders/v1"
     */
    private String namespaceUri;
    
    /**
     * XML namespace prefix
     * Example: "ord", "cust", "prod"
     */
    private String namespacePrefix;
    
    /**
     * Whether to include XML declaration
     */
    @Builder.Default
    private boolean includeXmlDeclaration = true;
    
    /**
     * Custom element names for JSON arrays
     * Key: JSON array field path (e.g., "orders.items")
     * Value: Custom wrapper element name (e.g., "orderItem")
     */
    @Builder.Default
    private Map<String, String> arrayElementNames = new HashMap<>();
    
    /**
     * Additional namespaces to declare
     * Key: Namespace prefix
     * Value: Namespace URI
     */
    @Builder.Default
    private Map<String, String> additionalNamespaces = new HashMap<>();
    
    /**
     * Whether to preserve JSON null values as empty XML elements
     */
    @Builder.Default
    private boolean preserveNullValues = false;
    
    /**
     * Whether to convert JSON property names to XML-friendly names
     * (e.g., "customer_id" to "customerId")
     */
    @Builder.Default
    private boolean convertPropertyNames = true;
    
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