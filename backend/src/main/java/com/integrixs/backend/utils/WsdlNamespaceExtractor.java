package com.integrixs.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to extract namespace information from WSDL content
 */
public class WsdlNamespaceExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(WsdlNamespaceExtractor.class);
    
    /**
     * Extract namespaces from WSDL content
     * @param wsdlContent The WSDL XML content
     * @return Map of namespace prefixes to URIs
     */
    public static Map<String, String> extractNamespaces(String wsdlContent) {
        Map<String, String> namespaces = new HashMap<>();
        
        if (wsdlContent == null || wsdlContent.trim().isEmpty()) {
            logger.debug("No WSDL content provided");
            return namespaces;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(wsdlContent)));
            
            Element root = doc.getDocumentElement();
            
            // Extract target namespace
            String targetNamespace = root.getAttribute("targetNamespace");
            if (targetNamespace != null && !targetNamespace.isEmpty()) {
                namespaces.put("tns", targetNamespace);
                logger.debug("Found target namespace: {}", targetNamespace);
            }
            
            // Extract all namespace declarations from root element
            NamedNodeMap attributes = root.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String name = attr.getNodeName();
                String value = attr.getNodeValue();
                
                if (name.startsWith("xmlns:")) {
                    String prefix = name.substring(6);
                    namespaces.put(prefix, value);
                    logger.debug("Found namespace: {} = {}", prefix, value);
                } else if (name.equals("xmlns")) {
                    namespaces.put("", value);
                    logger.debug("Found default namespace: {}", value);
                }
            }
            
            // Add common SOAP namespaces if not present
            if (!namespaces.containsKey("soapenv")) {
                namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
            }
            if (!namespaces.containsKey("soap")) {
                namespaces.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
            }
            
        } catch (Exception e) {
            logger.error("Error extracting namespaces from WSDL: {}", e.getMessage(), e);
        }
        
        return namespaces;
    }
    
    /**
     * Extract the primary service namespace from WSDL (targetNamespace)
     * This is the namespace that should be used for service operations
     * @param wsdlContent The WSDL XML content
     * @return The service namespace info (prefix and URI)
     */
    public static Map<String, String> extractServiceNamespace(String wsdlContent) {
        Map<String, String> serviceNamespace = new HashMap<>();
        
        if (wsdlContent == null || wsdlContent.trim().isEmpty()) {
            logger.debug("No WSDL content provided");
            return serviceNamespace;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(wsdlContent)));
            
            Element root = doc.getDocumentElement();
            
            // Get the targetNamespace - this is the service namespace
            String targetNamespace = root.getAttribute("targetNamespace");
            if (targetNamespace != null && !targetNamespace.isEmpty()) {
                // Find the prefix for this namespace
                NamedNodeMap attributes = root.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    
                    if (name.startsWith("xmlns:") && value.equals(targetNamespace)) {
                        String prefix = name.substring(6);
                        serviceNamespace.put("prefix", prefix);
                        serviceNamespace.put("uri", targetNamespace);
                        logger.debug("Found service namespace: {} = {}", prefix, targetNamespace);
                        break;
                    }
                }
                
                // If no prefix found, it might be using tns by convention
                if (!serviceNamespace.containsKey("prefix")) {
                    serviceNamespace.put("prefix", "tns");
                    serviceNamespace.put("uri", targetNamespace);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting service namespace from WSDL: {}", e.getMessage(), e);
        }
        
        return serviceNamespace;
    }
    
    /**
     * Extract namespace mapping for a specific operation
     * @param wsdlContent The WSDL XML content
     * @param operationName The operation name (e.g., "CelsiusToFahrenheit")
     * @return Map containing source and target namespace info
     */
    public static Map<String, Map<String, String>> extractOperationNamespaces(String wsdlContent, String operationName) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, String> sourceNamespaces = new HashMap<>();
        Map<String, String> targetNamespaces = new HashMap<>();
        
        // First get all namespaces from WSDL
        Map<String, String> allNamespaces = extractNamespaces(wsdlContent);
        
        // For SOAP operations, the target namespace is usually the one defined in the WSDL
        String targetNamespace = allNamespaces.get("tns");
        if (targetNamespace != null) {
            // Find the appropriate prefix for the target namespace
            String targetPrefix = null;
            for (Map.Entry<String, String> entry : allNamespaces.entrySet()) {
                if (entry.getValue().equals(targetNamespace) && !entry.getKey().equals("tns")) {
                    targetPrefix = entry.getKey();
                    break;
                }
            }
            
            // If no specific prefix found, use a default one
            if (targetPrefix == null) {
                targetPrefix = "ns";
            }
            
            targetNamespaces.put(targetPrefix, targetNamespace);
            targetNamespaces.putAll(allNamespaces);
        } else {
            // Fallback to all namespaces
            targetNamespaces.putAll(allNamespaces);
        }
        
        // Source namespaces typically come from the incoming request
        // For now, we'll include common namespaces
        sourceNamespaces.putAll(allNamespaces);
        
        result.put("source", sourceNamespaces);
        result.put("target", targetNamespaces);
        
        logger.info("Extracted namespaces for operation '{}': source={}, target={}", 
                   operationName, sourceNamespaces.size(), targetNamespaces.size());
        
        return result;
    }
}