package com.integrixs.backend.controller;

import com.integrixs.backend.service.IntegrationEndpointService;
import com.integrixs.backend.service.MessageService;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller that handles requests to deployed integration endpoints
 */
@RestController
public class IntegrationEndpointController {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationEndpointController.class);
    
    @Autowired
    private IntegrationEndpointService endpointService;
    
    @Autowired
    private SystemLogRepository systemLogRepository;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    /**
     * Handle SOAP requests
     */
    @PostMapping(value = "/soap/{flowPath}", 
                 consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "application/soap+xml"},
                 produces = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "application/soap+xml"})
    public ResponseEntity<String> handleSoapRequest(
            @PathVariable String flowPath,
            @RequestBody String soapRequest,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {
        
        logger.info("Received SOAP request for flow: {}", flowPath);
        
        try {
            String response = endpointService.processSoapRequest(flowPath, soapRequest, headers);
            
            // Determine response content type based on request
            MediaType responseContentType = MediaType.TEXT_XML;
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/soap+xml")) {
                responseContentType = MediaType.valueOf("application/soap+xml");
            }
            
            return ResponseEntity.ok()
                    .contentType(responseContentType)
                    .body(response);
        } catch (Exception e) {
            logger.error("Error processing SOAP request for flow: {}", flowPath, e);
            
            // Use same content type for fault response
            MediaType faultContentType = MediaType.TEXT_XML;
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/soap+xml")) {
                faultContentType = MediaType.valueOf("application/soap+xml");
            }
            
            return ResponseEntity.status(500)
                    .contentType(faultContentType)
                    .body(generateSoapFault(e.getMessage()));
        }
    }
    
    /**
     * Handle WSDL requests
     */
    @GetMapping(value = "/soap/{flowPath}", 
                produces = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<String> getWsdl(
            @PathVariable String flowPath,
            @RequestParam(required = false) String wsdl,
            HttpServletRequest request) {
        
        // Check if this is a WSDL request (parameter exists, regardless of value)
        if (request.getParameterMap().containsKey("wsdl")) {
            logger.info("WSDL request for flow: {}", flowPath);
            try {
                String wsdlContent = endpointService.generateWsdl(flowPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(wsdlContent);
            } catch (Exception e) {
                logger.error("Error generating WSDL for flow: {}", flowPath, e);
                return ResponseEntity.notFound().build();
            }
        }
        
        // If not a WSDL request, return method not allowed
        return ResponseEntity.status(405).build();
    }
    
    /**
     * Handle REST/HTTP requests
     */
    @RequestMapping(value = "/api/integration/{flowPath}/**", 
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
                             RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> handleRestRequest(
            @PathVariable String flowPath,
            @RequestBody(required = false) String requestBody,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {
        
        logger.info("Received {} request for flow: {}", request.getMethod(), flowPath);
        
        try {
            Map<String, Object> response = endpointService.processRestRequest(
                flowPath, 
                request.getMethod(), 
                requestBody, 
                headers,
                request.getParameterMap()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing REST request for flow: {}", flowPath, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    private String generateSoapFault(String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "  <soap:Body>\n" +
               "    <soap:Fault>\n" +
               "      <faultcode>Server</faultcode>\n" +
               "      <faultstring>" + message + "</faultstring>\n" +
               "    </soap:Fault>\n" +
               "  </soap:Body>\n" +
               "</soap:Envelope>";
    }
    
    /**
     * Test endpoint to check deployed flows
     */
    @GetMapping("/api/test-deployed-flows")
    public ResponseEntity<?> testDeployedFlows() {
        logger.info("Checking deployed flows");
        
        try {
            List<IntegrationFlow> deployedFlows = flowRepository.findByStatusAndIsActiveTrueOrderByName(FlowStatus.DEPLOYED_ACTIVE);
            List<Map<String, Object>> flowInfo = new ArrayList<>();
            
            for (IntegrationFlow flow : deployedFlows) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", flow.getId().toString());
                info.put("name", flow.getName());
                info.put("status", flow.getStatus().toString());
                info.put("deploymentEndpoint", flow.getDeploymentEndpoint());
                info.put("deployedAt", flow.getDeployedAt());
                info.put("active", flow.isActive());
                flowInfo.add(info);
            }
            
            return ResponseEntity.ok(Map.of(
                "count", deployedFlows.size(),
                "flows", flowInfo
            ));
            
        } catch (Exception e) {
            logger.error("Test deployed flows failed", e);
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Test endpoint to verify payload storage
     */
    @GetMapping("/api/test-payload-storage")
    public ResponseEntity<Map<String, Object>> testPayloadStorage() {
        logger.info("Testing payload storage directly");
        
        try {
            // Query current count
            long countBefore = systemLogRepository.count();
            long payloadCountBefore = systemLogRepository.findByCategoryOrderByTimestampDesc("ADAPTER_PAYLOAD").size();
            
            logger.info("Count before: total={}, payloads={}", countBefore, payloadCountBefore);
            
            // Try to save directly
            SystemLog testLog = new SystemLog();
            testLog.setTimestamp(java.time.LocalDateTime.now());
            testLog.setCreatedAt(java.time.LocalDateTime.now());
            testLog.setLevel(SystemLog.LogLevel.INFO);
            testLog.setMessage("TEST PAYLOAD");
            testLog.setCategory("ADAPTER_PAYLOAD");
            testLog.setCorrelationId("TEST-" + java.util.UUID.randomUUID().toString());
            testLog.setSource("TEST");
            testLog.setDetails("Test payload content");
            
            SystemLog saved = systemLogRepository.save(testLog);
            systemLogRepository.flush();
            
            // Query after
            long countAfter = systemLogRepository.count();
            long payloadCountAfter = systemLogRepository.findByCategoryOrderByTimestampDesc("ADAPTER_PAYLOAD").size();
            
            logger.info("Count after: total={}, payloads={}", countAfter, payloadCountAfter);
            
            return ResponseEntity.ok(Map.of(
                "saved", true,
                "id", saved.getId(),
                "countBefore", countBefore,
                "countAfter", countAfter,
                "payloadCountBefore", payloadCountBefore,
                "payloadCountAfter", payloadCountAfter
            ));
            
        } catch (Exception e) {
            logger.error("Test storage failed", e);
            return ResponseEntity.ok(Map.of(
                "saved", false,
                "error", e.getMessage()
            ));
        }
    }
}