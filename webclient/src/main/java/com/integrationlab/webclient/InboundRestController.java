package com.integrationlab.webclient;

import com.integrationlab.adapters.core.AdapterResult;
import com.integrationlab.adapters.core.ReceiverAdapter;
import com.integrationlab.adapters.factory.AdapterFactoryManager;
import com.integrationlab.adapters.core.AdapterType;
import com.integrationlab.adapters.config.HttpReceiverAdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Inbound REST Controller for receiving HTTP requests from external systems.
 * Routes incoming requests through the adapter framework for processing.
 */
@RestController
@RequestMapping("/api/inbound")
public class InboundRestController {

    private static final Logger logger = LoggerFactory.getLogger(InboundRestController.class);
    private final AdapterFactoryManager adapterFactory;

    public InboundRestController() {
        this.adapterFactory = AdapterFactoryManager.getInstance();
    }

    /**
     * Generic webhook endpoint for receiving inbound HTTP POST requests
     */
    @PostMapping("/webhook/{adapterId}")
    public ResponseEntity<String> receiveWebhook(
            @PathVariable String adapterId,
            @RequestBody String payload,
            HttpServletRequest request) {
        
        logger.info("Received inbound webhook for adapter: {}", adapterId);
        
        try {
            // Create HTTP receiver adapter configuration
            HttpReceiverAdapterConfig config = createHttpConfig(request, adapterId);
            
            // Create and initialize adapter
            ReceiverAdapter adapter = adapterFactory.createReceiver(AdapterType.HTTP, config);
            adapter.initialize();
            
            try {
                // Process the inbound message
                AdapterResult result = adapter.receive(payload);
                
                if (result.isSuccess()) {
                    logger.info("Successfully processed inbound message for adapter: {}", adapterId);
                    return ResponseEntity.ok("Message processed successfully");
                } else {
                    logger.error("Failed to process inbound message for adapter {}: {}", 
                            adapterId, result.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to process message: " + result.getMessage());
                }
            } finally {
                adapter.destroy();
            }
            
        } catch (Exception e) {
            logger.error("Error processing inbound webhook for adapter: {}", adapterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Generic GET endpoint for inbound requests
     */
    @GetMapping("/data/{adapterId}")
    public ResponseEntity<String> receiveGetRequest(
            @PathVariable String adapterId,
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        
        logger.info("Received inbound GET request for adapter: {}", adapterId);
        
        try {
            // Create HTTP receiver adapter configuration
            HttpReceiverAdapterConfig config = createHttpConfig(request, adapterId);
            
            // Create and initialize adapter
            ReceiverAdapter adapter = adapterFactory.createReceiver(AdapterType.HTTP, config);
            adapter.initialize();
            
            try {
                // Process the inbound request with parameters
                AdapterResult result = adapter.receive(params);
                
                if (result.isSuccess()) {
                    logger.info("Successfully processed inbound GET request for adapter: {}", adapterId);
                    return ResponseEntity.ok(result.getData() != null ? result.getData().toString() : "Success");
                } else {
                    logger.error("Failed to process inbound GET request for adapter {}: {}", 
                            adapterId, result.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to process request: " + result.getMessage());
                }
            } finally {
                adapter.destroy();
            }
            
        } catch (Exception e) {
            logger.error("Error processing inbound GET request for adapter: {}", adapterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Inbound webclient is healthy");
    }

    /**
     * Create HTTP receiver adapter configuration from request
     */
    private HttpReceiverAdapterConfig createHttpConfig(HttpServletRequest request, String adapterId) {
        HttpReceiverAdapterConfig config = new HttpReceiverAdapterConfig();
        
        // Set basic configuration
        config.setTargetEndpointUrl(request.getRequestURL().toString());
        config.setConnectionTimeout(30);
        config.setReadTimeout(30);
        
        // Extract and set headers as JSON string
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        // Convert headers map to JSON string (simple implementation)
        StringBuilder headerJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!first) headerJson.append(",");
            headerJson.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        headerJson.append("}");
        config.setRequestHeaders(headerJson.toString());
        
        return config;
    }
}