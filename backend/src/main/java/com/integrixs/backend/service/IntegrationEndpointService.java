package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.DataStructure;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.DataStructureRepository;
import com.integrixs.data.repository.FlowStructureRepository;
import com.integrixs.data.repository.FlowTransformationRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that processes requests to deployed integration endpoints
 */
@Service
public class IntegrationEndpointService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationEndpointService.class);
    
    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    @Autowired
    private FlowTransformationRepository transformationRepository;
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private DataStructureRepository dataStructureRepository;
    
    @Autowired
    private FlowStructureRepository flowStructureRepository;
    
    @Autowired
    private FlowExecutionSyncService flowExecutionSyncService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageService messageService;
    
    /**
     * Process a SOAP request
     */
    public String processSoapRequest(String flowPath, String soapRequest, Map<String, String> headers) throws Exception {
        logger.info("Processing SOAP request for flow path: {}", flowPath);
        
        // Find the deployed flow with all relationships eagerly loaded
        IntegrationFlow flow = findDeployedFlow(flowPath);
        logger.info("Found deployed flow: {} (ID: {})", flow.getName(), flow.getId());
        logger.info("Flow mapping mode: {}", flow.getMappingMode());
        logger.info("Flow has {} transformations", flow.getTransformations() != null ? flow.getTransformations().size() : 0);
        
        // Generate correlation ID for this flow execution
        String correlationId = UUID.randomUUID().toString();
        
        // Get source adapter for logging
        CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
            
        // Log incoming SOAP request to source adapter with correlation ID
        messageService.logAdapterActivity(sourceAdapter,
            "SOAP request received",
            "Endpoint: /" + flowPath + "\nRequest size: " + soapRequest.length() + " bytes",
            com.integrixs.data.model.SystemLog.LogLevel.INFO,
            correlationId);
            
        // Log the incoming SOAP request payload
        logger.info("DEBUG: About to log source adapter payload - correlationId: {}, adapter: {}, payloadSize: {}", 
            correlationId, sourceAdapter.getName(), soapRequest.length());
        try {
            logger.info("DEBUG: Source adapter details - ID: {}, Type: {}, Mode: {}", 
                sourceAdapter.getId(), sourceAdapter.getType(), sourceAdapter.getMode());
            
            // Clone adapter to avoid lazy loading issues
            CommunicationAdapter clonedAdapter = new CommunicationAdapter();
            clonedAdapter.setId(sourceAdapter.getId());
            clonedAdapter.setName(sourceAdapter.getName());
            clonedAdapter.setType(sourceAdapter.getType());
            clonedAdapter.setMode(sourceAdapter.getMode());
            
            messageService.logAdapterPayload(correlationId, clonedAdapter, "REQUEST", soapRequest, "INBOUND");
            logger.info("DEBUG: Finished logging source adapter payload");
        } catch (Exception e) {
            logger.error("DEBUG: Exception caught when logging source adapter payload: ", e);
        }
        
        // Check if target adapter is also SOAP
        CommunicationAdapter targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Target adapter not found"));
        
        String messageToProcess;
        // Always extract SOAP body for field mapping to work correctly
        // The target adapter will wrap it back into SOAP if needed
        logger.info("Extracting SOAP body from request for processing");
        messageToProcess = extractSoapBody(soapRequest);
        logger.info("Extracted SOAP body: {}", messageToProcess);
        
        // Process through the flow with correlation ID
        logger.info("Processing message through flow: {} with correlation ID: {}", flow.getName(), correlationId);
        headers.put("correlationId", correlationId);
        headers.put("isEndpointFlow", "true"); // Flag to prevent duplicate response logging
        String responseBody = flowExecutionSyncService.processMessage(flow, messageToProcess, headers, "SOAP");
        logger.debug("Received response body: {}", responseBody);
        
        // Check if response is already a SOAP envelope
        String soapResponse;
        if (responseBody.contains("Envelope") && (responseBody.contains("soap:") || responseBody.contains("SOAP-ENV:"))) {
            logger.info("Response is already a SOAP envelope, using as-is");
            soapResponse = responseBody;
        } else {
            logger.info("Response is body content only, wrapping in SOAP envelope");
            soapResponse = wrapInSoapEnvelope(responseBody);
        }
        logger.debug("Final SOAP response prepared");
        
        // Log the outgoing SOAP response payload for the SOURCE adapter (it's sending the response back)
        logger.info("DEBUG: About to log source adapter response - correlationId: {}, adapter: {}, payloadSize: {}", 
            correlationId, sourceAdapter.getName(), soapResponse.length());
        
        // Clone adapter to avoid lazy loading issues
        CommunicationAdapter clonedSourceAdapter = new CommunicationAdapter();
        clonedSourceAdapter.setId(sourceAdapter.getId());
        clonedSourceAdapter.setName(sourceAdapter.getName());
        clonedSourceAdapter.setType(sourceAdapter.getType());
        clonedSourceAdapter.setMode(sourceAdapter.getMode());
        
        try {
            messageService.logAdapterPayload(correlationId, clonedSourceAdapter, "RESPONSE", soapResponse, "INBOUND");
        } catch (Exception e) {
            logger.warn("Failed to log response payload: {}", e.getMessage());
        }
        logger.info("DEBUG: Finished logging source adapter response");
        
        return soapResponse;
    }
    
    /**
     * Generate WSDL for a deployed SOAP flow
     */
    @Transactional(readOnly = true)
    public String generateWsdl(String flowPath) throws Exception {
        logger.info("Generating WSDL for flow path: {}", flowPath);
        IntegrationFlow flow = findDeployedFlow(flowPath);
        logger.info("Found flow: {} with deployment endpoint: {}", flow.getName(), flow.getDeploymentEndpoint());
        
        // Get source adapter
        CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
        
        // First check if flow has a source flow structure with WSDL
        if (flow.getSourceFlowStructureId() != null) {
            FlowStructure sourceFlowStructure = flowStructureRepository.findById(UUID.fromString(flow.getSourceFlowStructureId()))
                .orElse(null);
            
            if (sourceFlowStructure != null && sourceFlowStructure.getWsdlContent() != null) {
                logger.info("Using WSDL from flow structure: {}", sourceFlowStructure.getName());
                // Return the original WSDL with updated endpoint
                return updateWsdlEndpoint(sourceFlowStructure.getWsdlContent(), flow.getDeploymentEndpoint());
            }
        }
        
        // Legacy: Check old data structure if no flow structure
        if (flow.getSourceStructureId() != null) {
            DataStructure sourceStructure = dataStructureRepository.findById(UUID.fromString(flow.getSourceStructureId()))
                .orElse(null);
            
            if (sourceStructure != null && sourceStructure.getOriginalContent() != null) {
                logger.info("Using WSDL from legacy data structure: {}", sourceStructure.getName());
                // Return the original WSDL with updated endpoint
                return updateWsdlEndpoint(sourceStructure.getOriginalContent(), flow.getDeploymentEndpoint());
            }
        }
        
        // Fall back to adapter configuration
        String configJson = sourceAdapter.getConfiguration();
        if (configJson != null) {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            if (config.containsKey("wsdlContent")) {
                logger.info("Using WSDL from adapter configuration");
                String originalWsdl = (String) config.get("wsdlContent");
                return updateWsdlEndpoint(originalWsdl, flow.getDeploymentEndpoint());
            }
        }
        
        // Generate basic WSDL if none exists
        logger.info("Generating basic WSDL as no original WSDL found");
        return generateBasicWsdl(flow, sourceAdapter);
    }
    
    /**
     * Process a REST request
     */
    public Map<String, Object> processRestRequest(String flowPath, String method, String requestBody, 
                                                 Map<String, String> headers, Map<String, String[]> params) throws Exception {
        // Find the deployed flow
        IntegrationFlow flow = findDeployedFlow(flowPath);
        
        // Process through the flow
        String response = flowExecutionSyncService.processMessage(flow, requestBody, headers, "REST");
        
        // Parse response as JSON if possible
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            // If not JSON, return as message
            Map<String, Object> result = new HashMap<>();
            result.put("message", response);
            return result;
        }
    }
    
    private IntegrationFlow findDeployedFlow(String flowPath) throws Exception {
        logger.info("Looking for deployed flow with path containing: {}", flowPath);
        
        // Find deployed flow with transformations eagerly loaded
        Optional<IntegrationFlow> flow = flowRepository.findByDeploymentEndpointContainingAndStatus(
            flowPath, FlowStatus.DEPLOYED_ACTIVE);
        
        if (flow.isEmpty()) {
            // Log all deployed flows for debugging
            List<IntegrationFlow> allDeployedFlows = flowRepository.findByStatusAndIsActiveTrueOrderByName(FlowStatus.DEPLOYED_ACTIVE);
            logger.warn("No deployed active flow found for path: {}. Deployed flows:", flowPath);
            for (IntegrationFlow f : allDeployedFlows) {
                logger.warn("  - Flow: {} with endpoint: {}", f.getName(), f.getDeploymentEndpoint());
            }
            throw new IllegalArgumentException("No deployed active flow found for path: " + flowPath);
        }
        
        IntegrationFlow integrationFlow = flow.get();
        logger.info("Loaded flow '{}' with {} transformations", 
            integrationFlow.getName(),
            integrationFlow.getTransformations() != null ? integrationFlow.getTransformations().size() : 0);
        
        // Load field mappings for each transformation separately to avoid MultipleBagFetchException
        if (integrationFlow.getTransformations() != null && !integrationFlow.getTransformations().isEmpty()) {
            for (int i = 0; i < integrationFlow.getTransformations().size(); i++) {
                FlowTransformation transformation = integrationFlow.getTransformations().get(i);
                // Load the transformation with field mappings
                Optional<FlowTransformation> transformationWithMappings = 
                    transformationRepository.findWithFieldMappingsById(transformation.getId());
                
                if (transformationWithMappings.isPresent()) {
                    // Replace with the fully loaded transformation
                    integrationFlow.getTransformations().set(i, transformationWithMappings.get());
                    logger.info("Transformation '{}' loaded with {} field mappings", 
                        transformationWithMappings.get().getName(),
                        transformationWithMappings.get().getFieldMappings() != null ? 
                            transformationWithMappings.get().getFieldMappings().size() : 0);
                }
            }
        }
        
        return integrationFlow;
    }
    
    private String extractSoapBody(String soapRequest) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(soapRequest)));
        
        // Find the Body element
        org.w3c.dom.NodeList bodyList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
        if (bodyList.getLength() == 0) {
            // Try SOAP 1.2
            bodyList = doc.getElementsByTagNameNS("http://www.w3.org/2003/05/soap-envelope", "Body");
        }
        
        if (bodyList.getLength() > 0) {
            // Get first child of Body (the actual request)
            org.w3c.dom.Node body = bodyList.item(0);
            if (body.hasChildNodes()) {
                org.w3c.dom.Node requestNode = body.getFirstChild();
                while (requestNode != null && requestNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    requestNode = requestNode.getNextSibling();
                }
                
                if (requestNode != null) {
                    // Convert to string
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    // Don't output XML declaration
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
                    StringWriter writer = new StringWriter();
                    transformer.transform(new DOMSource(requestNode), new StreamResult(writer));
                    return writer.toString();
                }
            }
        }
        
        throw new IllegalArgumentException("No SOAP Body found in request");
    }
    
    private String wrapInSoapEnvelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "  <soap:Body>\n" +
               body +
               "  </soap:Body>\n" +
               "</soap:Envelope>";
    }
    
    private String updateWsdlEndpoint(String wsdl, String newEndpoint) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(wsdl)));
        
        // Update soap:address location
        org.w3c.dom.NodeList addressList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/soap/", "address");
        for (int i = 0; i < addressList.getLength(); i++) {
            org.w3c.dom.Element address = (org.w3c.dom.Element) addressList.item(i);
            address.setAttribute("location", newEndpoint);
        }
        
        // Convert back to string
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
    
    private String generateBasicWsdl(IntegrationFlow flow, CommunicationAdapter adapter) {
        String serviceName = flow.getName().replaceAll("[^a-zA-Z0-9]", "");
        String endpoint = flow.getDeploymentEndpoint();
        
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<definitions name=\"" + serviceName + "Service\"\n" +
               "             targetNamespace=\"http://integrixflowbridge.com/" + serviceName + "\"\n" +
               "             xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
               "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
               "             xmlns:tns=\"http://integrixflowbridge.com/" + serviceName + "\"\n" +
               "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
               "\n" +
               "  <types>\n" +
               "    <xsd:schema targetNamespace=\"http://integrixflowbridge.com/" + serviceName + "\">\n" +
               "      <xsd:element name=\"Request\" type=\"xsd:anyType\"/>\n" +
               "      <xsd:element name=\"Response\" type=\"xsd:anyType\"/>\n" +
               "    </xsd:schema>\n" +
               "  </types>\n" +
               "\n" +
               "  <message name=\"RequestMessage\">\n" +
               "    <part name=\"body\" element=\"tns:Request\"/>\n" +
               "  </message>\n" +
               "\n" +
               "  <message name=\"ResponseMessage\">\n" +
               "    <part name=\"body\" element=\"tns:Response\"/>\n" +
               "  </message>\n" +
               "\n" +
               "  <portType name=\"" + serviceName + "PortType\">\n" +
               "    <operation name=\"process\">\n" +
               "      <input message=\"tns:RequestMessage\"/>\n" +
               "      <output message=\"tns:ResponseMessage\"/>\n" +
               "    </operation>\n" +
               "  </portType>\n" +
               "\n" +
               "  <binding name=\"" + serviceName + "Binding\" type=\"tns:" + serviceName + "PortType\">\n" +
               "    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n" +
               "    <operation name=\"process\">\n" +
               "      <soap:operation soapAction=\"process\"/>\n" +
               "      <input>\n" +
               "        <soap:body use=\"literal\"/>\n" +
               "      </input>\n" +
               "      <output>\n" +
               "        <soap:body use=\"literal\"/>\n" +
               "      </output>\n" +
               "    </operation>\n" +
               "  </binding>\n" +
               "\n" +
               "  <service name=\"" + serviceName + "Service\">\n" +
               "    <port name=\"" + serviceName + "Port\" binding=\"tns:" + serviceName + "Binding\">\n" +
               "      <soap:address location=\"" + endpoint + "\"/>\n" +
               "    </port>\n" +
               "  </service>\n" +
               "</definitions>";
    }
}