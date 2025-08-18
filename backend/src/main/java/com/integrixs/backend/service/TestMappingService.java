package com.integrixs.backend.service;

import com.integrixs.engine.transformation.FieldMappingProcessor;
import com.integrixs.shared.dto.TestFieldMappingsRequestDTO;
import com.integrixs.shared.dto.TestFieldMappingsResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for testing field mappings without deploying flows
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestMappingService {
    
    private final FieldMappingProcessor fieldMappingProcessor;
    private final DevelopmentFunctionService developmentFunctionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TestFieldMappingsResponseDTO testFieldMappings(TestFieldMappingsRequestDTO request) {
        long startTime = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        
        log.info("TestFieldMappings called with {} mappings", request.getMappings() != null ? request.getMappings().size() : 0);
        
        try {
            // Parse input XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document inputDoc = builder.parse(new ByteArrayInputStream(request.getInputXml().getBytes()));
            
            log.debug("Input XML parsed successfully, root element: {}", inputDoc.getDocumentElement().getNodeName());
            
            // Create output document preserving the input structure and order
            Document outputDoc = createOutputDocumentFromInput(inputDoc, builder);
            
            // Apply field mappings
            if (request.getMappings() != null) {
                log.info("Applying {} mappings", request.getMappings().size());
                for (TestFieldMappingsRequestDTO.TestMappingDTO mapping : request.getMappings()) {
                    try {
                        log.debug("Applying mapping: {} -> {}", mapping.getSourceFields(), mapping.getTargetField());
                        applyMapping(inputDoc, outputDoc, mapping);
                    } catch (Exception e) {
                        warnings.add("Failed to apply mapping for " + mapping.getTargetField() + ": " + e.getMessage());
                        log.warn("Failed to apply mapping", e);
                    }
                }
            } else {
                log.warn("No mappings provided in request");
            }
            
            // Convert output document to XML string
            String outputXml = documentToString(outputDoc);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return TestFieldMappingsResponseDTO.builder()
                    .success(true)
                    .outputXml(outputXml)
                    .warnings(warnings)
                    .executionTimeMs(executionTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error testing field mappings", e);
            return TestFieldMappingsResponseDTO.builder()
                    .success(false)
                    .error("Failed to test mappings: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Create output document by cloning the input structure (preserves element order)
     */
    private Document createOutputDocumentFromInput(Document inputDoc, DocumentBuilder builder) throws Exception {
        Document outputDoc = builder.newDocument();
        
        // Clone the root element structure
        Element inputRoot = inputDoc.getDocumentElement();
        Element outputRoot = (Element) outputDoc.importNode(inputRoot, false); // false = don't import children
        outputDoc.appendChild(outputRoot);
        
        // Clone the structure recursively, preserving order but clearing values
        cloneStructure(inputRoot, outputRoot, outputDoc);
        
        return outputDoc;
    }
    
    /**
     * Recursively clone element structure preserving order but not values
     */
    private void cloneStructure(Element source, Element target, Document targetDoc) {
        org.w3c.dom.NodeList children = source.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                // Create new element in target document
                Element newElement = targetDoc.createElement(childElement.getNodeName());
                target.appendChild(newElement);
                
                // Copy attributes if any
                org.w3c.dom.NamedNodeMap attributes = childElement.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attributes.item(j);
                    newElement.setAttribute(attr.getName(), attr.getValue());
                }
                
                // Recursively process children (structure only, no text content)
                cloneStructure(childElement, newElement, targetDoc);
            }
        }
    }
    
    private Document createOutputDocument(String targetStructureXml, DocumentBuilder builder, String mappingType) throws Exception {
        if (targetStructureXml != null && !targetStructureXml.isEmpty()) {
            // Parse target structure to extract the appropriate message type
            Document fullDoc = builder.parse(new ByteArrayInputStream(targetStructureXml.getBytes()));
            
            // Create a new document with only the root element structure
            Document outputDoc = builder.newDocument();
            Element root = fullDoc.getDocumentElement();
            
            // Find the appropriate message element based on mapping type
            // For request mappings, we want the request message (e.g., Credit_Token_Req_MT)
            // For response mappings, we want the response message (e.g., Credit_Token_Resp_MT)
            // For fault mappings, we want the fault message (e.g., StandardMessageFault)
            org.w3c.dom.NodeList children = root.getChildNodes();
            Element messageElement = null;
            
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    Element child = (Element) children.item(i);
                    String nodeName = child.getNodeName();
                    
                    // Select the appropriate message based on mapping type
                    if ("request".equalsIgnoreCase(mappingType) && nodeName.endsWith("_Req_MT")) {
                        messageElement = child;
                        break;
                    } else if ("response".equalsIgnoreCase(mappingType) && nodeName.endsWith("_Resp_MT")) {
                        messageElement = child;
                        break;
                    } else if ("fault".equalsIgnoreCase(mappingType) && nodeName.contains("Fault")) {
                        messageElement = child;
                        break;
                    }
                }
            }
            
            // If no specific match found, use the first element
            if (messageElement == null) {
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element) {
                        messageElement = (Element) children.item(i);
                        log.warn("Could not find specific message type for mapping type '{}', using first element: {}", 
                                mappingType, messageElement.getNodeName());
                        break;
                    }
                }
            }
            
            if (messageElement != null) {
                // Import and clone only the relevant message structure
                Element importedElement = (Element) outputDoc.importNode(messageElement, true);
                outputDoc.appendChild(importedElement);
                
                // Clear all text content (remove default "string" values)
                clearTextContent(importedElement);
            } else {
                // Fallback: create empty document
                Element fallbackRoot = outputDoc.createElement("Output");
                outputDoc.appendChild(fallbackRoot);
            }
            
            return outputDoc;
        } else {
            // Create empty document
            Document doc = builder.newDocument();
            Element root = doc.createElement("Output");
            doc.appendChild(root);
            return doc;
        }
    }
    
    /**
     * Recursively clear all text content from elements (removes default values like "string")
     * but preserves the element structure
     */
    private void clearTextContent(Element element) {
        // Get all child nodes
        org.w3c.dom.NodeList children = element.getChildNodes();
        
        // Process each child
        for (int i = children.getLength() - 1; i >= 0; i--) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                // Remove text nodes that contain default values
                String textContent = child.getTextContent();
                if (textContent != null && (textContent.trim().equals("string") || 
                    textContent.trim().equals("?") || textContent.trim().isEmpty())) {
                    element.removeChild(child);
                }
            } else if (child instanceof Element) {
                // Recursively process child elements
                clearTextContent((Element) child);
            }
        }
    }
    
    private void applyMapping(Document inputDoc, Document outputDoc, TestFieldMappingsRequestDTO.TestMappingDTO mapping) throws Exception {
        log.info("Applying mapping - sourceFields: {}, targetField: {}, sourcePaths: {}, targetPath: {}, function: {}, requiresTransformation: {}", 
                mapping.getSourceFields(), mapping.getTargetField(), mapping.getSourcePaths(), mapping.getTargetPath(), mapping.getJavaFunction(), mapping.isRequiresTransformation());
        
        // Log visual flow data if present
        if (mapping.getVisualFlowData() != null) {
            log.info("Visual flow data present with {} nodes and {} edges", 
                    mapping.getVisualFlowData().getNodes() != null ? mapping.getVisualFlowData().getNodes().size() : 0,
                    mapping.getVisualFlowData().getEdges() != null ? mapping.getVisualFlowData().getEdges().size() : 0);
        }
        
        // Skip nodeMapping functions - they are for structural mapping, not value mapping
        if ("nodeMapping".equals(mapping.getJavaFunction())) {
            log.debug("Skipping nodeMapping function");
            return;
        }
        
        // Skip if the target is the root element (to avoid adding text to root)
        String targetField = mapping.getTargetField();
        if (targetField != null && outputDoc.getDocumentElement() != null && 
            targetField.equals(outputDoc.getDocumentElement().getNodeName())) {
            log.debug("Skipping mapping to root element: {}", targetField);
            return;
        }
        
        // Extract values from source fields or paths
        List<String> sourceValues = new ArrayList<>();
        
        // First try sourcePaths if available
        if (mapping.getSourcePaths() != null && !mapping.getSourcePaths().isEmpty()) {
            log.info("Using sourcePaths: {}", mapping.getSourcePaths());
            for (String sourcePath : mapping.getSourcePaths()) {
                // Convert dot notation to XPath if needed
                String xpathQuery = sourcePath;
                if (sourcePath.contains(".") && !sourcePath.startsWith("/")) {
                    // Convert Credit_Token_Req_MT.meterSerialNumber to /Credit_Token_Req_MT/meterSerialNumber
                    xpathQuery = "/" + sourcePath.replace(".", "/");
                    log.info("Converted path '{}' to XPath '{}'", sourcePath, xpathQuery);
                }
                String value = extractValueFromXPath(inputDoc, xpathQuery);
                log.info("Extracted value from path '{}': '{}'", xpathQuery, value);
                if (value != null) {
                    sourceValues.add(value);
                }
            }
        } 
        // If no sourcePaths, try sourceFields
        else if (mapping.getSourceFields() != null && !mapping.getSourceFields().isEmpty()) {
            log.info("Using sourceFields: {}", mapping.getSourceFields());
            for (String sourceField : mapping.getSourceFields()) {
                // Skip if source field is the root element
                if (inputDoc.getDocumentElement() != null && 
                    sourceField.equals(inputDoc.getDocumentElement().getNodeName())) {
                    log.debug("Skipping extraction from root element: {}", sourceField);
                    continue;
                }
                // Try as element name
                String value = extractValueFromXPath(inputDoc, "//" + sourceField);
                log.info("Extracted value from field '{}': '{}'", sourceField, value);
                if (value != null) {
                    sourceValues.add(value);
                }
            }
        } else {
            log.warn("No source fields or paths provided in mapping");
        }
        
        // Apply transformation if needed
        String resultValue;
        log.info("Source values collected: {}", sourceValues);
        
        // If no source values were found, skip this mapping
        if (sourceValues.isEmpty()) {
            log.warn("No source values found for mapping from {} to {}", mapping.getSourceFields(), mapping.getTargetField());
            return;
        }
        
        if (mapping.isRequiresTransformation() && mapping.getJavaFunction() != null) {
            log.info("Applying transformation with function: {}", mapping.getJavaFunction());
            // Check if this is a visual flow transformation
            if ("visual_flow".equals(mapping.getJavaFunction()) && mapping.getVisualFlowData() != null) {
                try {
                    resultValue = processVisualFlow(inputDoc, mapping.getVisualFlowData());
                    log.info("Applied visual flow transformation, result: '{}'", resultValue);
                } catch (Exception e) {
                    log.warn("Failed to apply visual flow transformation: {}", e.getMessage(), e);
                    // Fall back to direct mapping
                    resultValue = sourceValues.isEmpty() ? null : String.join(" ", sourceValues);
                }
            } else {
                // Apply Java function transformation
                try {
                    // Try to execute from database first
                    String functionName = mapping.getJavaFunction();
                    if (functionName != null && !functionName.startsWith("builtin:")) {
                        try {
                            // Get function from database
                            com.integrixs.data.model.TransformationCustomFunction function = 
                                developmentFunctionService.getBuiltInFunctionByName(functionName);
                            
                            // Map source values to function parameters
                            Map<String, Object> functionInputs = new HashMap<>();
                            List<Map<String, Object>> paramDefs = objectMapper.readValue(
                                function.getParameters() != null ? function.getParameters() : "[]", 
                                List.class
                            );
                            
                            for (int i = 0; i < paramDefs.size() && i < sourceValues.size(); i++) {
                                Map<String, Object> paramDef = paramDefs.get(i);
                                String paramName = (String) paramDef.get("name");
                                functionInputs.put(paramName, sourceValues.get(i));
                            }
                            
                            DevelopmentFunctionService.FunctionTestResult testResult = 
                                developmentFunctionService.testFunction(function.getFunctionId().toString(), functionInputs);
                            
                            if (testResult.isSuccess()) {
                                resultValue = testResult.getOutput() != null ? testResult.getOutput().toString() : "";
                                log.info("Applied transformation '{}' from database, result: '{}'", functionName, resultValue);
                            } else {
                                throw new Exception(testResult.getError());
                            }
                        } catch (Exception dbExecError) {
                            log.debug("Database function execution failed, trying FieldMappingProcessor: {}", dbExecError.getMessage());
                            // Fallback to FieldMappingProcessor
                            resultValue = fieldMappingProcessor.executeFunction(
                                mapping.getJavaFunction(), 
                                sourceValues.toArray(new String[0])
                            );
                            log.info("Applied transformation '{}' via FieldMappingProcessor, result: '{}'", mapping.getJavaFunction(), resultValue);
                        }
                    } else {
                        // Use FieldMappingProcessor for builtin: prefix or other cases
                        resultValue = fieldMappingProcessor.executeFunction(
                            mapping.getJavaFunction(), 
                            sourceValues.toArray(new String[0])
                        );
                        log.info("Applied transformation '{}', result: '{}'", mapping.getJavaFunction(), resultValue);
                    }
                } catch (Exception e) {
                    log.warn("Failed to apply transformation '{}': {}", mapping.getJavaFunction(), e.getMessage());
                    // Fall back to direct mapping
                    resultValue = sourceValues.isEmpty() ? null : String.join(" ", sourceValues);
                }
            }
        } else {
            // Direct mapping (even if requiresTransformation is true but no function specified)
            if (sourceValues.size() == 1) {
                resultValue = sourceValues.get(0);
                log.info("Direct mapping (single value), result: '{}'", resultValue);
            } else {
                // Concatenate multiple values
                resultValue = String.join(" ", sourceValues);
                log.info("Direct mapping (concatenated values), result: '{}'", resultValue);
            }
        }
        
        // Set value in target document
        String targetPath = mapping.getTargetPath();
        if (targetPath == null && mapping.getTargetField() != null) {
            targetPath = "//" + mapping.getTargetField();
        }
        
        // Convert dot notation to XPath if needed
        if (targetPath != null && targetPath.contains(".") && !targetPath.startsWith("/")) {
            targetPath = "/" + targetPath.replace(".", "/");
            log.info("Converted target path to XPath: {}", targetPath);
        }
        
        log.info("Target path: {}, Result value: {}", targetPath, resultValue);
        
        if (targetPath != null && resultValue != null) {
            log.info("Setting value '{}' to path '{}'", resultValue, targetPath);
            setValueByXPath(outputDoc, targetPath, resultValue);
        } else {
            log.warn("Cannot set value - targetPath: {}, resultValue: {}", targetPath, resultValue);
        }
    }
    
    private String extractValueFromXPath(Document doc, String xpath) {
        try {
            log.debug("Extracting value from XPath: {}", xpath);
            // Handle absolute paths starting with //
            if (xpath.startsWith("//")) {
                String elementName = xpath.substring(2);
                log.debug("Looking for element: {}", elementName);
                org.w3c.dom.NodeList nodes = doc.getElementsByTagName(elementName);
                log.debug("Found {} nodes with name '{}'", nodes.getLength(), elementName);
                if (nodes.getLength() > 0) {
                    String value = nodes.item(0).getTextContent();
                    log.debug("Raw value from element '{}': '{}'", elementName, value);
                    // Don't return placeholder values
                    return (value != null && !value.equals("?")) ? value.trim() : null;
                }
                return null;
            }
            
            // Simple XPath extraction for paths like /Root/Element
            String[] parts = xpath.split("/");
            Element current = doc.getDocumentElement();
            log.debug("Root element: {}, looking for path: {}", current.getNodeName(), xpath);
            
            // Start from 1 to skip empty first element from leading /
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.isEmpty()) continue;
                
                log.debug("Looking for child element '{}' in '{}'", part, current.getNodeName());
                
                // For the first part, check if it matches the root element
                if (i == 1 && current.getNodeName().equals(part)) {
                    log.debug("First part matches root element, continuing");
                    continue;
                }
                
                // Find child element
                org.w3c.dom.NodeList children = current.getChildNodes();
                Element found = null;
                for (int j = 0; j < children.getLength(); j++) {
                    org.w3c.dom.Node child = children.item(j);
                    if (child instanceof Element && child.getNodeName().equals(part)) {
                        found = (Element) child;
                        break;
                    }
                }
                
                if (found != null) {
                    current = found;
                    log.debug("Found element '{}', continuing", part);
                } else {
                    log.debug("Element '{}' not found in path", part);
                    return null;
                }
            }
            
            String value = current.getTextContent();
            log.debug("Final element '{}' has value: '{}'", current.getNodeName(), value);
            // Don't return placeholder values
            return (value != null && !value.equals("?") && !value.trim().isEmpty()) ? value.trim() : null;
        } catch (Exception e) {
            log.debug("Failed to extract value from XPath: {}", xpath, e);
            return null;
        }
    }
    
    private void setValueByXPath(Document doc, String xpath, String value) {
        try {
            log.info("setValueByXPath called with xpath='{}', value='{}'", xpath, value);
            // Handle absolute paths starting with //
            if (xpath.startsWith("//")) {
                String elementName = xpath.substring(2);
                log.info("Looking for element '{}' in output document", elementName);
                // Find the element within the document - search from root
                Element targetElement = findElementByName(doc.getDocumentElement(), elementName);
                if (targetElement != null) {
                    log.info("Found target element '{}', setting value", elementName);
                    // Clear any existing text nodes
                    org.w3c.dom.NodeList childNodes = targetElement.getChildNodes();
                    for (int i = childNodes.getLength() - 1; i >= 0; i--) {
                        if (childNodes.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                            targetElement.removeChild(childNodes.item(i));
                        }
                    }
                    // Set the new text content
                    targetElement.appendChild(doc.createTextNode(value));
                    log.info("Successfully set value '{}' on element '{}'", value, elementName);
                } else {
                    log.warn("Element '{}' not found in output document", elementName);
                }
                return;
            }
            
            // For non-absolute paths, navigate through the structure
            String[] parts = xpath.split("/");
            Element current = doc.getDocumentElement();
            log.info("Setting value using path navigation for: {}", xpath);
            
            // Start from 1 to skip empty first element from leading /
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.isEmpty()) continue;
                
                // For the first part, check if it matches the root element
                if (i == 1 && current.getNodeName().equals(part)) {
                    log.debug("First part matches root element, continuing");
                    continue;
                }
                
                // For last part, this is where we set the value
                if (i == parts.length - 1) {
                    Element targetElement = findChildElement(current, part);
                    if (targetElement != null) {
                        log.info("Found target element '{}', setting value", part);
                        // Clear any existing text content
                        org.w3c.dom.NodeList childNodes = targetElement.getChildNodes();
                        for (int j = childNodes.getLength() - 1; j >= 0; j--) {
                            if (childNodes.item(j).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                                targetElement.removeChild(childNodes.item(j));
                            }
                        }
                        // Set the new text content
                        targetElement.appendChild(doc.createTextNode(value));
                        log.info("Value set successfully on element '{}'", part);
                    } else {
                        log.warn("Target element '{}' not found in parent '{}'", part, current.getNodeName());
                    }
                    return;
                }
                
                // Navigate to child element
                Element found = findChildElement(current, part);
                if (found != null) {
                    current = found;
                    log.debug("Navigated to element '{}'", part);
                } else {
                    log.warn("Path element '{}' not found, cannot continue", part);
                    return;
                }
            }
            
        } catch (Exception e) {
            log.debug("Failed to set value by XPath: {}", xpath, e);
        }
    }
    
    /**
     * Find a child element by name (direct children only)
     */
    private Element findChildElement(Element parent, String name) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof Element && child.getNodeName().equals(name)) {
                return (Element) child;
            }
        }
        return null;
    }
    
    /**
     * Recursively find an element by name anywhere in the document
     */
    private Element findElementByName(Element parent, String name) {
        // Check if this element matches
        if (parent.getNodeName().equals(name)) {
            return parent;
        }
        
        // Check children
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof Element) {
                Element found = findElementByName((Element) child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
    
    /**
     * Process visual flow transformation
     * This method parses the visual flow data and executes the transformation pipeline
     */
    private String processVisualFlow(Document inputDoc, TestFieldMappingsRequestDTO.VisualFlowData visualFlowData) throws Exception {
        log.debug("Processing visual flow with {} nodes and {} edges", 
                visualFlowData.getNodes().size(), visualFlowData.getEdges().size());
        
        // Create a map to store node outputs
        Map<String, String> nodeOutputs = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // First, process all source field nodes
        for (TestFieldMappingsRequestDTO.VisualFlowNode node : visualFlowData.getNodes()) {
            if ("sourceField".equals(node.getType())) {
                try {
                    // Extract field data from node
                    Map<String, Object> nodeData = objectMapper.convertValue(node.getData(), Map.class);
                    Map<String, Object> fieldData = (Map<String, Object>) nodeData.get("field");
                    String fieldPath = (String) fieldData.get("path");
                    String fieldName = (String) fieldData.get("name");
                    
                    // Extract value from input document
                    String value = extractValueFromXPath(inputDoc, "//" + (fieldName != null ? fieldName : fieldPath));
                    if (value != null) {
                        nodeOutputs.put(node.getId(), value);
                        log.debug("Source field node {} = '{}'", node.getId(), value);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process source field node {}: {}", node.getId(), e.getMessage());
                }
            } else if ("constant".equals(node.getType())) {
                try {
                    // Extract constant value
                    Map<String, Object> nodeData = objectMapper.convertValue(node.getData(), Map.class);
                    String value = (String) nodeData.get("value");
                    if (value != null) {
                        nodeOutputs.put(node.getId(), value);
                        log.debug("Constant node {} = '{}'", node.getId(), value);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process constant node {}: {}", node.getId(), e.getMessage());
                }
            }
        }
        
        // Process function nodes in topological order (simplified - assumes no cycles)
        boolean progress = true;
        while (progress) {
            progress = false;
            
            for (TestFieldMappingsRequestDTO.VisualFlowNode node : visualFlowData.getNodes()) {
                if ("function".equals(node.getType()) && !nodeOutputs.containsKey(node.getId())) {
                    try {
                        // Check if all inputs are available
                        List<String> inputValues = new ArrayList<>();
                        boolean allInputsReady = true;
                        
                        // Find edges coming into this function and sort by target handle
                        List<TestFieldMappingsRequestDTO.VisualFlowEdge> incomingEdges = new ArrayList<>();
                        for (TestFieldMappingsRequestDTO.VisualFlowEdge edge : visualFlowData.getEdges()) {
                            if (edge.getTarget().equals(node.getId())) {
                                incomingEdges.add(edge);
                            }
                        }
                        
                        // Sort edges by target handle (string1, string2, etc.)
                        incomingEdges.sort((e1, e2) -> {
                            String h1 = e1.getTargetHandle();
                            String h2 = e2.getTargetHandle();
                            if (h1 == null) h1 = "";
                            if (h2 == null) h2 = "";
                            return h1.compareTo(h2);
                        });
                        
                        // Process edges in order
                        for (TestFieldMappingsRequestDTO.VisualFlowEdge edge : incomingEdges) {
                            if (nodeOutputs.containsKey(edge.getSource())) {
                                inputValues.add(nodeOutputs.get(edge.getSource()));
                                log.debug("Added input from edge source {} (handle: {})", edge.getSource(), edge.getTargetHandle());
                            } else {
                                allInputsReady = false;
                                break;
                            }
                        }
                        
                        if (allInputsReady && !inputValues.isEmpty()) {
                            // Extract function details
                            Map<String, Object> nodeData = objectMapper.convertValue(node.getData(), Map.class);
                            Map<String, Object> functionData = (Map<String, Object>) nodeData.get("function");
                            String functionName = (String) functionData.get("name");
                            String javaCode = (String) functionData.get("javaCode");
                            
                            // Extract parameters from node data
                            Map<String, Object> parameters = (Map<String, Object>) nodeData.get("parameters");
                            log.info("Function {} has saved parameters: {}", functionName, parameters);
                            log.info("Function {} has input values from edges: {}", functionName, inputValues);
                            
                            // Extract function parameter definitions
                            List<Map<String, Object>> functionParams = (List<Map<String, Object>>) functionData.get("parameters");
                            
                            // Build final input array in the correct order based on function parameter definitions
                            List<String> finalInputs = new ArrayList<>();
                            int draggableInputIndex = 0;
                            
                            if (functionParams != null) {
                                log.info("Function {} parameter definitions: {}", functionName, functionParams);
                                
                                // Process parameters in the order they are defined
                                for (Map<String, Object> paramDef : functionParams) {
                                    String paramName = (String) paramDef.get("name");
                                    Boolean isRequired = (Boolean) paramDef.get("required");
                                    
                                    if (isDraggableParameter(paramName, isRequired, functionParams.indexOf(paramDef))) {
                                        // This is a draggable parameter - get value from input edges
                                        if (draggableInputIndex < inputValues.size()) {
                                            String value = inputValues.get(draggableInputIndex++);
                                            finalInputs.add(value);
                                            log.info("Added draggable parameter '{}' at position {}: '{}'", 
                                                    paramName, finalInputs.size() - 1, value);
                                        }
                                    } else {
                                        // This is a configurable parameter - get value from saved parameters
                                        if (parameters != null && parameters.containsKey(paramName)) {
                                            String paramValue = String.valueOf(parameters.get(paramName));
                                            if (paramValue != null && !paramValue.isEmpty() && !"null".equals(paramValue)) {
                                                finalInputs.add(paramValue);
                                                log.info("Added configurable parameter '{}' at position {}: '{}'", 
                                                        paramName, finalInputs.size() - 1, paramValue);
                                            }
                                        }
                                    }
                                }
                            } else {
                                // No parameter definitions, use inputs as is
                                finalInputs = new ArrayList<>(inputValues);
                            }
                            
                            log.info("Final inputs for function {}: {}", functionName, finalInputs);
                            
                            // Execute function using database definition
                            String result;
                            if (functionName != null) {
                                try {
                                    // Execute function from database
                                    Map<String, Object> functionInputs = new HashMap<>();
                                    
                                    // Get function from database to get parameter names
                                    com.integrixs.data.model.TransformationCustomFunction function = 
                                        developmentFunctionService.getBuiltInFunctionByName(functionName);
                                    
                                    // Parse parameter definitions
                                    List<Map<String, Object>> paramDefs = objectMapper.readValue(
                                        function.getParameters() != null ? function.getParameters() : "[]", 
                                        List.class
                                    );
                                    
                                    // Map inputs to parameter names
                                    for (int i = 0; i < paramDefs.size() && i < finalInputs.size(); i++) {
                                        Map<String, Object> paramDef = paramDefs.get(i);
                                        String paramName = (String) paramDef.get("name");
                                        functionInputs.put(paramName, finalInputs.get(i));
                                    }
                                    
                                    log.info("Executing function {} with inputs: {}", functionName, functionInputs);
                                    DevelopmentFunctionService.FunctionTestResult testResult = 
                                        developmentFunctionService.testFunction(function.getFunctionId().toString(), functionInputs);
                                    
                                    if (testResult.isSuccess()) {
                                        result = testResult.getOutput() != null ? testResult.getOutput().toString() : "";
                                        log.info("Function execution successful: {}", result);
                                    } else {
                                        log.warn("Function execution failed: {}", testResult.getError());
                                        result = String.join(" ", finalInputs);
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to execute function from database: {}", e.getMessage());
                                    // Fallback to FieldMappingProcessor
                                    result = fieldMappingProcessor.executeFunction("builtin:" + functionName, finalInputs.toArray(new String[0]));
                                }
                            } else if (javaCode != null && !javaCode.isEmpty()) {
                                // Custom JavaScript code
                                result = fieldMappingProcessor.executeFunction(javaCode, finalInputs.toArray(new String[0]));
                            } else {
                                result = String.join(" ", finalInputs);
                            }
                            
                            nodeOutputs.put(node.getId(), result);
                            log.debug("Function node {} ({}) = '{}'", node.getId(), functionName, result);
                            progress = true;
                        }
                    } catch (Exception e) {
                        log.warn("Failed to process function node {}: {}", node.getId(), e.getMessage());
                    }
                }
            }
        }
        
        // Find the final output by looking for edges connected to the target node
        String finalResult = null;
        for (TestFieldMappingsRequestDTO.VisualFlowNode node : visualFlowData.getNodes()) {
            if ("targetField".equals(node.getType())) {
                // Find edge coming into target
                for (TestFieldMappingsRequestDTO.VisualFlowEdge edge : visualFlowData.getEdges()) {
                    if (edge.getTarget().equals(node.getId())) {
                        finalResult = nodeOutputs.get(edge.getSource());
                        break;
                    }
                }
                break;
            }
        }
        
        log.debug("Visual flow final result: '{}'", finalResult);
        return finalResult;
    }
    
    /**
     * Determine if a parameter is draggable (input field) or configurable
     * This mirrors the logic in the frontend FunctionNode component
     */
    private boolean isDraggableParameter(String paramName, Boolean isRequired, int paramIndex) {
        // Parameters that are typically draggable (input fields)
        String[] draggableNames = {"string1", "string2", "text", "input", "value", "a", "b", "array", "object", "source", "target"};
        
        // If parameter name suggests it's input data, it's draggable
        for (String name : draggableNames) {
            if (paramName.toLowerCase().contains(name)) {
                return true;
            }
        }
        
        // If it's the first parameter and required, it's usually draggable
        if (paramIndex == 0 && Boolean.TRUE.equals(isRequired)) {
            return true;
        }
        
        // Parameters like delimiter, format, separator, etc. are configurable
        String[] configurableNames = {"delimiter", "format", "separator", "pattern", "start", "end", "length", "index"};
        for (String name : configurableNames) {
            if (paramName.toLowerCase().contains(name)) {
                return false;
            }
        }
        
        // Default: if required, it's draggable; if optional, it's configurable
        return Boolean.TRUE.equals(isRequired);
    }
}