package com.integrixs.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.util.*;

/**
 * Utility class to extract operation information from WSDL and build dynamic SOAP structures
 */
public class WsdlOperationExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(WsdlOperationExtractor.class);
    
    /**
     * Extract operation information from WSDL
     */
    public static OperationInfo extractOperationInfo(String wsdlContent, String operationName) {
        if (wsdlContent == null || wsdlContent.trim().isEmpty()) {
            logger.warn("No WSDL content provided");
            return null;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(wsdlContent)));
            
            // Extract namespaces first
            Map<String, String> namespaces = WsdlNamespaceExtractor.extractNamespaces(wsdlContent);
            
            // Setup XPath with namespace context
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new MapNamespaceContext(namespaces));
            
            // Get target namespace
            String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
            
            // Find operation element structure from types section
            String elementName = findOperationElement(doc, xpath, operationName, namespaces);
            if (elementName == null) {
                elementName = operationName; // Fallback to operation name
            }
            
            // Determine appropriate prefix for target namespace
            String targetPrefix = findPrefixForNamespace(namespaces, targetNamespace);
            
            OperationInfo info = new OperationInfo();
            info.operationName = operationName;
            info.elementName = elementName;
            info.targetNamespace = targetNamespace;
            info.targetPrefix = targetPrefix;
            info.namespaces = namespaces;
            
            logger.info("Extracted operation info: operation={}, element={}, namespace={}:{}", 
                       operationName, elementName, targetPrefix, targetNamespace);
            
            return info;
            
        } catch (Exception e) {
            logger.error("Error extracting operation info from WSDL: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Find the element name for an operation from WSDL types section
     */
    private static String findOperationElement(Document doc, XPath xpath, String operationName, 
                                             Map<String, String> namespaces) {
        try {
            // Look for element with name matching operation
            String query = "//xsd:element[@name='" + operationName + "']";
            XPathExpression expr = xpath.compile(query);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            
            if (nodes.getLength() > 0) {
                return operationName;
            }
            
            // Also check for operation in portType
            query = "//wsdl:operation[@name='" + operationName + "']";
            expr = xpath.compile(query);
            nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            
            if (nodes.getLength() > 0) {
                // Get input message
                Node operation = nodes.item(0);
                NodeList inputs = ((Element) operation).getElementsByTagNameNS("*", "input");
                if (inputs.getLength() > 0) {
                    String messageName = ((Element) inputs.item(0)).getAttribute("message");
                    // Remove prefix if present
                    if (messageName.contains(":")) {
                        messageName = messageName.substring(messageName.indexOf(":") + 1);
                    }
                    
                    // Find message element
                    query = "//wsdl:message[@name='" + messageName + "']/wsdl:part";
                    expr = xpath.compile(query);
                    NodeList parts = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    
                    if (parts.getLength() > 0) {
                        String element = ((Element) parts.item(0)).getAttribute("element");
                        if (element != null && !element.isEmpty()) {
                            // Remove prefix if present
                            if (element.contains(":")) {
                                element = element.substring(element.indexOf(":") + 1);
                            }
                            return element;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error finding operation element: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find the prefix for a given namespace URI
     */
    private static String findPrefixForNamespace(Map<String, String> namespaces, String namespaceUri) {
        // First look for exact match (excluding tns)
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            if (entry.getValue().equals(namespaceUri) && !entry.getKey().equals("tns")) {
                return entry.getKey();
            }
        }
        
        // If not found, create a sensible prefix
        if (namespaceUri.contains("w3schools")) {
            return "tem";
        } else if (namespaceUri.contains("weather")) {
            return "weat";
        } else if (namespaceUri.contains("integrix")) {
            return "int";
        }
        
        // Default
        return "ns";
    }
    
    /**
     * Create a dynamic SOAP envelope based on WSDL operation info
     */
    public static Document createSoapEnvelope(OperationInfo operationInfo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Create SOAP envelope
        Element envelope = doc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
        envelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        envelope.setAttribute("xmlns:" + operationInfo.targetPrefix, operationInfo.targetNamespace);
        
        // Add any additional namespaces
        for (Map.Entry<String, String> ns : operationInfo.namespaces.entrySet()) {
            if (!ns.getKey().equals("soapenv") && !ns.getKey().equals(operationInfo.targetPrefix) 
                && !ns.getKey().equals("tns") && !ns.getKey().isEmpty()) {
                envelope.setAttribute("xmlns:" + ns.getKey(), ns.getValue());
            }
        }
        
        // Create SOAP body
        Element body = doc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Body");
        
        // Create operation element
        Element operation = doc.createElementNS(operationInfo.targetNamespace, 
                                              operationInfo.targetPrefix + ":" + operationInfo.elementName);
        
        body.appendChild(operation);
        envelope.appendChild(body);
        doc.appendChild(envelope);
        
        return doc;
    }
    
    /**
     * Container for operation information
     */
    public static class OperationInfo {
        public String operationName;
        public String elementName;
        public String targetNamespace;
        public String targetPrefix;
        public Map<String, String> namespaces = new HashMap<>();
    }
    
    /**
     * Simple namespace context implementation
     */
    private static class MapNamespaceContext implements NamespaceContext {
        private final Map<String, String> namespaces;
        
        public MapNamespaceContext(Map<String, String> namespaces) {
            this.namespaces = new HashMap<>(namespaces);
            // Add default namespaces
            this.namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
            this.namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema");
            this.namespaces.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
        }
        
        @Override
        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }
        
        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }
        
        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    prefixes.add(entry.getKey());
                }
            }
            return prefixes.iterator();
        }
    }
}