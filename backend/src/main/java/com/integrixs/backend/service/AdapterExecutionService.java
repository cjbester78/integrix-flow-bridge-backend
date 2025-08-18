package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.shared.enums.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service that executes communication adapters
 */
@Service
public class AdapterExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterExecutionService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    /**
     * Execute an adapter with the given message
     */
    public String executeAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        logger.info("Executing adapter: {} ({})", adapter.getName(), adapter.getType());
        
        switch (adapter.getType()) {
            case SOAP:
                return executeSoapAdapter(adapter, message, context);
                
            case REST:
            case HTTP:
                return executeHttpAdapter(adapter, message, context);
                
            case FILE:
                return executeFileAdapter(adapter, message, context);
                
            case FTP:
            case SFTP:
                return executeFtpAdapter(adapter, message, context);
                
            default:
                throw new UnsupportedOperationException("Adapter type not supported: " + adapter.getType());
        }
    }
    
    private String executeSoapAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        logger.info("Executing SOAP adapter: {} (ID: {})", adapter.getName(), adapter.getId());
        
        // Get correlation ID and flow from context
        String correlationId = (String) context.get("correlationId");
        String flowId = (String) context.get("flowId");
        IntegrationFlow flow = null;
        if (flowId != null && correlationId != null) {
            flow = flowRepository.findById(UUID.fromString(flowId)).orElse(null);
        }
        
        // Log the incoming payload (what the adapter receives)
        if (correlationId != null) {
            messageService.logAdapterPayload(correlationId, adapter, "REQUEST", message, "OUTBOUND");
        }
        
        // Log adapter execution start
        if (flow != null && correlationId != null) {
            try {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP adapter starting: " + adapter.getName(),
                    "Preparing to send SOAP request",
                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } catch (Exception e) {
                logger.warn("Failed to log processing step: {}", e.getMessage());
            }
        }
        
        // Log to adapter activity log
        try {
            messageService.logAdapterActivity(adapter,
                "Processing outbound SOAP request",
                "Flow: " + (flow != null ? flow.getName() : "Unknown"),
                com.integrixs.data.model.SystemLog.LogLevel.INFO,
                correlationId);
        } catch (Exception e) {
            logger.warn("Failed to log adapter activity: {}", e.getMessage());
        }
        
        // Parse and log configuration
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        logger.info("SOAP adapter configuration keys: {}", config.keySet());
        logger.debug("SOAP adapter full configuration: {}", config);
        
        // Check for endpoint in multiple possible field names
        // For receiver adapters (sending TO external systems), check targetEndpointUrl
        // For sender adapters in POLL mode, check serviceEndpointUrl
        String endpoint = (String) config.get("targetEndpointUrl");
        if (endpoint == null) {
            endpoint = (String) config.get("serviceEndpointUrl");
        }
        if (endpoint == null) {
            endpoint = (String) config.get("endpoint");
        }
        logger.info("SOAP endpoint from config: {}", endpoint);
        
        String soapAction = (String) config.get("soapAction");
        logger.info("SOAP action from config: {}", soapAction);
        
        if (endpoint == null || endpoint.isEmpty()) {
            logger.error("SOAP endpoint not configured for adapter: {} (ID: {}). Available config keys: {}", 
                        adapter.getName(), adapter.getId(), config.keySet());
            if (flow != null && correlationId != null) {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP adapter error: " + adapter.getName(),
                    "No endpoint configured for SOAP adapter",
                    com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            }
            throw new IllegalArgumentException("SOAP endpoint not configured");
        }
        
        // Log endpoint found
        if (flow != null && correlationId != null) {
            messageService.logProcessingStep(correlationId, flow,
                "SOAP endpoint configured",
                "Endpoint: " + endpoint,
                com.integrixs.data.model.SystemLog.LogLevel.INFO);
        }
        
        // Check if message is already a SOAP envelope
        logger.info("Received message for SOAP adapter: {}", message);
        String soapRequest;
        if (message.trim().startsWith("<?xml") && message.contains("Envelope")) {
            // Message is already a complete SOAP envelope
            logger.info("Message is already a SOAP envelope, using as-is");
            soapRequest = message;
        } else if (message.trim().startsWith("<") && message.contains(":Envelope")) {
            // Message is a SOAP envelope without XML declaration
            logger.info("Message is a SOAP envelope without declaration, using as-is");
            soapRequest = message;
        } else {
            // Message is just the body content, wrap it
            logger.info("Message is body content only, wrapping in SOAP envelope");
            soapRequest = wrapInSoapEnvelope(message);
        }
        logger.info("Final SOAP request being sent: {}", soapRequest);
        
        // Make HTTP call
        HttpHeaders headers = new HttpHeaders();
        
        // Set content type based on SOAP version
        String soapVersion = (String) config.getOrDefault("soapVersion", "1.1");
        logger.info("SOAP version: {}", soapVersion);
        
        if ("1.2".equals(soapVersion)) {
            headers.setContentType(MediaType.valueOf("application/soap+xml"));
            logger.info("Using SOAP 1.2 content type: application/soap+xml");
        } else {
            // Default to SOAP 1.1
            headers.setContentType(MediaType.TEXT_XML);
            logger.info("Using SOAP 1.1 content type: text/xml");
        }
        
        if (soapAction != null && !soapAction.isEmpty()) {
            headers.add("SOAPAction", soapAction);
            logger.info("Added SOAPAction header: {}", soapAction);
        }
        
        HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
        logger.info("Making SOAP call to endpoint: {}", endpoint);
        logger.info("SOAP Request being sent: {}", soapRequest);
        
        // Log SOAP call details
        if (flow != null && correlationId != null) {
            messageService.logProcessingStep(correlationId, flow,
                "Sending SOAP request",
                "Endpoint: " + endpoint + "\nSOAP Action: " + (soapAction != null ? soapAction : "none"),
                com.integrixs.data.model.SystemLog.LogLevel.INFO);
        }
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );
            
            logger.info("SOAP call successful. Response status: {}", response.getStatusCode());
            logger.debug("SOAP response body: {}", response.getBody());
            
            // Log the response payload only if not handled by IntegrationEndpointService
            // Check if this is a SOAP endpoint flow (where IntegrationEndpointService will log the final response)
            boolean isEndpointFlow = context.get("isEndpointFlow") != null && (boolean) context.get("isEndpointFlow");
            if (correlationId != null && !isEndpointFlow) {
                messageService.logAdapterPayload(correlationId, adapter, "RESPONSE", response.getBody(), "OUTBOUND");
            }
            
            // Log successful response
            if (flow != null && correlationId != null) {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP response received",
                    "Status: " + response.getStatusCode() + "\nResponse size: " + 
                    (response.getBody() != null ? response.getBody().length() : 0) + " bytes",
                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
            }
            
            // Log to adapter activity
            messageService.logAdapterActivity(adapter,
                "SOAP request completed successfully",
                "Endpoint: " + endpoint + "\nStatus: " + response.getStatusCode(),
                com.integrixs.data.model.SystemLog.LogLevel.INFO,
                correlationId);
            
            // Extract SOAP body from response
            String extractedBody = extractSoapBody(response.getBody());
            logger.debug("Extracted SOAP body: {}", extractedBody);
            
            return extractedBody;
            
        } catch (Exception e) {
            logger.error("Error calling SOAP endpoint: {}. Error: {}", endpoint, e.getMessage(), e);
            
            // Log error
            if (flow != null && correlationId != null) {
                try {
                    messageService.logProcessingStep(correlationId, flow,
                        "SOAP call failed",
                        "Endpoint: " + endpoint + ", Error: " + e.getMessage(),
                        com.integrixs.data.model.SystemLog.LogLevel.ERROR);
                } catch (Exception logEx) {
                    logger.warn("Failed to log error step: {}", logEx.getMessage());
                }
            }
            
            // Log to adapter activity
            try {
                messageService.logAdapterActivity(adapter,
                    "SOAP request failed",
                    "Endpoint: " + endpoint + ", Error: " + e.getMessage(),
                    com.integrixs.data.model.SystemLog.LogLevel.ERROR,
                    correlationId);
            } catch (Exception logEx) {
                logger.warn("Failed to log adapter error: {}", logEx.getMessage());
            }
            
            throw new RuntimeException("SOAP call failed: " + e.getMessage(), e);
        }
    }
    
    private String executeHttpAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        String endpoint = (String) config.get("endpoint");
        String method = (String) config.getOrDefault("method", "POST");
        
        if (endpoint == null) {
            throw new IllegalArgumentException("HTTP endpoint not configured");
        }
        
        // Get correlation ID from context
        String correlationId = (String) context.get("correlationId");
        
        // Log the incoming payload (what the adapter receives)
        if (correlationId != null) {
            messageService.logAdapterPayload(correlationId, adapter, "REQUEST", message, "OUTBOUND");
        }
        
        HttpHeaders headers = new HttpHeaders();
        
        // Set content type based on configuration
        String contentType = (String) config.getOrDefault("contentType", "application/json");
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        // Add any configured headers
        Map<String, String> customHeaders = (Map<String, String>) config.get("headers");
        if (customHeaders != null) {
            customHeaders.forEach(headers::add);
        }
        
        HttpEntity<String> request = new HttpEntity<>(message, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.valueOf(method.toUpperCase()),
                request,
                String.class
            );
            
            // Log the response payload only if not handled by IntegrationEndpointService
            // Check if this is a SOAP endpoint flow (where IntegrationEndpointService will log the final response)
            boolean isEndpointFlow = context.get("isEndpointFlow") != null && (boolean) context.get("isEndpointFlow");
            if (correlationId != null && !isEndpointFlow) {
                messageService.logAdapterPayload(correlationId, adapter, "RESPONSE", response.getBody(), "OUTBOUND");
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error calling HTTP endpoint: {}", endpoint, e);
            throw new RuntimeException("HTTP call failed: " + e.getMessage(), e);
        }
    }
    
    private String executeFileAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        String directory = (String) config.get("directory");
        String filePattern = (String) config.getOrDefault("fileNamePattern", "output-{timestamp}.txt");
        
        if (directory == null) {
            throw new IllegalArgumentException("File directory not configured");
        }
        
        // Create directory if it doesn't exist
        Path dirPath = Paths.get(directory);
        Files.createDirectories(dirPath);
        
        // Generate filename
        String filename = filePattern
            .replace("{timestamp}", String.valueOf(System.currentTimeMillis()))
            .replace("{uuid}", UUID.randomUUID().toString())
            .replace("{flowId}", (String) context.get("flowId"));
        
        // Write file
        Path filePath = dirPath.resolve(filename);
        Files.write(filePath, message.getBytes());
        
        logger.info("File written to: {}", filePath);
        
        // Return success response
        return "{\"status\":\"success\",\"file\":\"" + filePath.toString() + "\"}";
    }
    
    private String executeFtpAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        // TODO: Implement FTP/SFTP adapter execution
        throw new UnsupportedOperationException("FTP/SFTP adapter execution not yet implemented");
    }
    
    private String extractSoapBody(String soapResponse) throws Exception {
        // Parse SOAP response and extract body content
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(soapResponse.getBytes()));
        
        // Find the Body element
        org.w3c.dom.NodeList bodyList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
        if (bodyList.getLength() == 0) {
            // Try SOAP 1.2
            bodyList = doc.getElementsByTagNameNS("http://www.w3.org/2003/05/soap-envelope", "Body");
        }
        
        if (bodyList.getLength() > 0) {
            // Get first child of Body
            org.w3c.dom.Node body = bodyList.item(0);
            if (body.hasChildNodes()) {
                org.w3c.dom.Node responseNode = body.getFirstChild();
                while (responseNode != null && responseNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    responseNode = responseNode.getNextSibling();
                }
                
                if (responseNode != null) {
                    // Convert to string
                    javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
                    javax.xml.transform.Transformer transformer = tf.newTransformer();
                    // Don't output XML declaration
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
                    java.io.StringWriter writer = new java.io.StringWriter();
                    transformer.transform(new javax.xml.transform.dom.DOMSource(responseNode), 
                                        new javax.xml.transform.stream.StreamResult(writer));
                    return writer.toString();
                }
            }
        }
        
        // If no body found, return the whole response
        return soapResponse;
    }
    
    private String wrapInSoapEnvelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "  <soap:Body>\n" +
               body +
               "  </soap:Body>\n" +
               "</soap:Envelope>";
    }
    
    private Map<String, Object> parseConfiguration(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, Map.class);
        } catch (Exception e) {
            logger.error("Error parsing adapter configuration: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}