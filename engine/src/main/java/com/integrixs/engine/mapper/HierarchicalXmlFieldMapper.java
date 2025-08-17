package com.integrixs.engine.mapper;

import com.integrixs.data.model.FieldMapping;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.Arrays;

/**
 * Service for hierarchical XML field mapping supporting XPath expressions and array structures
 */
@Service
public class HierarchicalXmlFieldMapper {
    
    /**
     * Map source XML to target XML using field mappings
     * 
     * @param sourceXml Source XML document as string
     * @param targetXmlTemplate Target XML template (optional)
     * @param fieldMappings List of field mappings
     * @param namespaces Map of namespace prefixes to URIs
     * @return Mapped target XML as string
     * @throws Exception if mapping fails
     */
    public String mapXmlFields(String sourceXml, String targetXmlTemplate, 
                              List<FieldMapping> fieldMappings,
                              Map<String, String> namespaces) throws Exception {
        
        // Parse source XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document sourceDoc = builder.parse(new InputSource(new StringReader(sourceXml)));
        
        // Create or parse target document
        Document targetDoc;
        if (targetXmlTemplate != null && !targetXmlTemplate.isEmpty()) {
            targetDoc = builder.parse(new InputSource(new StringReader(targetXmlTemplate)));
        } else {
            targetDoc = builder.newDocument();
            Element root = targetDoc.createElement("mappedData");
            targetDoc.appendChild(root);
        }
        
        // Create XPath with namespace support
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (namespaces != null && !namespaces.isEmpty()) {
            xpath.setNamespaceContext(new MapNamespaceContext(namespaces));
        }
        
        // Process each field mapping
        for (FieldMapping mapping : fieldMappings) {
            if (mapping.isArrayMapping()) {
                processArrayMapping(sourceDoc, targetDoc, mapping, xpath);
            } else {
                processSimpleMapping(sourceDoc, targetDoc, mapping, xpath);
            }
        }
        
        // Convert result to string
        return documentToString(targetDoc);
    }
    
    private void processSimpleMapping(Document sourceDoc, Document targetDoc, 
                                     FieldMapping mapping, XPath xpath) throws Exception {
        
        String sourceXPath = mapping.getSourceXPath();
        String targetXPath = mapping.getTargetXPath();
        
        if (sourceXPath == null || targetXPath == null) {
            // Fall back to legacy field mapping
            processLegacyMapping(sourceDoc, targetDoc, mapping, xpath);
            return;
        }
        
        // Evaluate source XPath
        XPathExpression sourceExpr = xpath.compile(sourceXPath);
        Object sourceResult = sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);
        NodeList sourceNodes = (NodeList) sourceResult;
        
        if (sourceNodes.getLength() > 0) {
            Node sourceNode = sourceNodes.item(0);
            String value = getNodeValue(sourceNode);
            
            // Apply transformation if defined
            if (mapping.getJavaFunction() != null) {
                value = applyTransformation(value, mapping.getJavaFunction());
            }
            
            // Set value at target XPath
            setValueAtXPath(targetDoc, targetXPath, value, xpath);
        }
    }
    
    private void processArrayMapping(Document sourceDoc, Document targetDoc,
                                    FieldMapping mapping, XPath xpath) throws Exception {
        
        String arrayContextPath = mapping.getArrayContextPath();
        String sourceXPath = mapping.getSourceXPath();
        String targetXPath = mapping.getTargetXPath();
        
        if (arrayContextPath == null) {
            throw new IllegalArgumentException("Array context path is required for array mapping");
        }
        
        // Get all items in the array
        XPathExpression arrayExpr = xpath.compile(arrayContextPath);
        NodeList arrayNodes = (NodeList) arrayExpr.evaluate(sourceDoc, XPathConstants.NODESET);
        
        // Process each array item
        for (int i = 0; i < arrayNodes.getLength(); i++) {
            Node arrayNode = arrayNodes.item(i);
            
            // Evaluate source XPath relative to array item
            XPathExpression sourceExpr = xpath.compile(sourceXPath);
            Object sourceResult = sourceExpr.evaluate(arrayNode, XPathConstants.NODESET);
            NodeList sourceNodes = (NodeList) sourceResult;
            
            if (sourceNodes.getLength() > 0) {
                String value = getNodeValue(sourceNodes.item(0));
                
                // Apply transformation if defined
                if (mapping.getJavaFunction() != null) {
                    value = applyTransformation(value, mapping.getJavaFunction());
                }
                
                // Create target path with array index
                String indexedTargetPath = targetXPath.replace("[*]", "[" + (i + 1) + "]");
                
                // Ensure parent structure exists
                ensurePathExists(targetDoc, indexedTargetPath, xpath);
                
                // Set value at target XPath
                setValueAtXPath(targetDoc, indexedTargetPath, value, xpath);
            }
        }
    }
    
    private void processLegacyMapping(Document sourceDoc, Document targetDoc,
                                     FieldMapping mapping, XPath xpath) throws Exception {
        
        // Handle legacy field-based mapping
        String sourceFieldsStr = mapping.getSourceFields();
        String targetField = mapping.getTargetField();
        
        if (sourceFieldsStr == null || sourceFieldsStr.isEmpty() || targetField == null) {
            return;
        }
        
        // Check if sourceFields is a literal value or a field reference
        String value;
        if (sourceFieldsStr.startsWith("//") || sourceFieldsStr.contains("/")) {
            // It's an XPath expression
            XPathExpression sourceExpr = xpath.compile(sourceFieldsStr);
            NodeList sourceNodes = (NodeList) sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);
            
            if (sourceNodes.getLength() > 0) {
                value = getNodeValue(sourceNodes.item(0));
            } else {
                // No nodes found, skip this mapping
                return;
            }
        } else {
            // It's a literal value (like "12")
            value = sourceFieldsStr;
        }
        
        // Handle targetField - ensure it's a proper XPath
        String targetXPath = targetField;
        if (!targetXPath.startsWith("/")) {
            targetXPath = "//" + targetField;
        }
        
        // Apply transformation if defined
        if (mapping.getJavaFunction() != null) {
            value = applyTransformation(value, mapping.getJavaFunction());
        }
        
        // Set value at target XPath
        setValueAtXPath(targetDoc, targetXPath, value, xpath);
    }
    
    private String getNodeValue(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return node.getTextContent();
        } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return node.getNodeValue();
        }
        return node.getNodeValue();
    }
    
    private void setValueAtXPath(Document doc, String xpath, String value, XPath xpathEval) 
            throws Exception {
        
        // First try to find existing node
        XPathExpression expr = xpathEval.compile(xpath);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        
        if (nodes.getLength() > 0) {
            // Update existing node
            Node node = nodes.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                node.setTextContent(value);
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                ((Attr) node).setValue(value);
            }
        } else {
            // Create new node structure
            createNodeFromXPath(doc, xpath, value);
        }
    }
    
    private void ensurePathExists(Document doc, String xpath, XPath xpathEval) throws Exception {
        // Remove the last element to get parent path
        int lastSlash = xpath.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentPath = xpath.substring(0, lastSlash);
            
            // Check if parent exists
            XPathExpression expr = xpathEval.compile(parentPath);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            
            if (nodes.getLength() == 0) {
                // Create parent structure
                createNodeFromXPath(doc, parentPath, null);
            }
        }
    }
    
    private void createNodeFromXPath(Document doc, String xpath, String value) {
        // Parse XPath and create node structure
        String[] parts = xpath.split("/");
        Node currentNode = doc.getDocumentElement();
        
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            
            // Handle array indices
            String elementName = part;
            int arrayIndex = -1;
            if (part.contains("[") && part.contains("]")) {
                elementName = part.substring(0, part.indexOf('['));
                String indexStr = part.substring(part.indexOf('[') + 1, part.indexOf(']'));
                try {
                    arrayIndex = Integer.parseInt(indexStr) - 1; // XPath uses 1-based indexing
                } catch (NumberFormatException e) {
                    // Ignore invalid indices
                }
            }
            
            // Find or create child element
            NodeList children = currentNode.getChildNodes();
            Element targetElement = null;
            int currentIndex = 0;
            
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE && 
                    child.getNodeName().equals(elementName)) {
                    if (arrayIndex < 0 || currentIndex == arrayIndex) {
                        targetElement = (Element) child;
                        break;
                    }
                    currentIndex++;
                }
            }
            
            if (targetElement == null) {
                // Create new element
                targetElement = doc.createElement(elementName);
                currentNode.appendChild(targetElement);
            }
            
            currentNode = targetElement;
        }
        
        // Set value on final node
        if (value != null && currentNode.getNodeType() == Node.ELEMENT_NODE) {
            currentNode.setTextContent(value);
        }
    }
    
    private String applyTransformation(String value, String javaFunction) {
        // TODO: Implement JavaScript/Java function execution
        // For now, return the value as-is
        return value;
    }
    
    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return writer.toString();
    }
    
    /**
     * Simple namespace context implementation using a map
     */
    private static class MapNamespaceContext implements NamespaceContext {
        private final Map<String, String> prefixToUri;
        private final Map<String, String> uriToPrefix;
        
        public MapNamespaceContext(Map<String, String> namespaces) {
            this.prefixToUri = new HashMap<>(namespaces);
            this.uriToPrefix = new HashMap<>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                uriToPrefix.put(entry.getValue(), entry.getKey());
            }
        }
        
        @Override
        public String getNamespaceURI(String prefix) {
            return prefixToUri.get(prefix);
        }
        
        @Override
        public String getPrefix(String namespaceURI) {
            return uriToPrefix.get(namespaceURI);
        }
        
        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            if (prefix == null) {
                return Collections.emptyIterator();
            }
            return Collections.singletonList(prefix).iterator();
        }
    }
}