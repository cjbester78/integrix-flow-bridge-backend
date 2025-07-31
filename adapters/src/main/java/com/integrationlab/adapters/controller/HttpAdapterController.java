package com.integrationlab.adapters.controller;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.HttpSenderAdapterConfig;
import com.integrationlab.adapters.factory.AdapterFactoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller to handle inbound HTTP requests routed to the HTTP Sender Adapter.
 * In middleware terminology, sender adapters receive data FROM external systems.
 */
@RestController
@RequestMapping("/api/http-adapter")
public class HttpAdapterController {

    private static final Logger logger = LoggerFactory.getLogger(HttpAdapterController.class);

    private final SenderAdapter httpSenderAdapter;
    private final AdapterFactoryManager factoryManager;

    /**
     * Constructor injecting HttpSenderAdapterConfig and initializing HTTP Sender Adapter.
     */
    public HttpAdapterController(HttpSenderAdapterConfig config) {
        this.factoryManager = AdapterFactoryManager.getInstance();
        try {
            BaseAdapter adapter = factoryManager.createAndInitialize(
                    AdapterType.HTTP, AdapterMode.SENDER, config);
            this.httpSenderAdapter = (SenderAdapter) adapter;
            logger.info("HTTP Sender Adapter initialized for controller");
        } catch (AdapterException e) {
            logger.error("Failed to initialize HTTP Sender Adapter", e);
            throw new RuntimeException("Failed to initialize HTTP Sender Adapter", e);
        }
    }

    /**
     * Inbound POST endpoint to receive JSON payloads.
     * This endpoint acts as a webhook/receiver for external systems to send data to the middleware.
     * 
     * @param payload Raw JSON payload as String
     * @return ResponseEntity with response JSON or error message
     */
    @PostMapping("/receive")
    public ResponseEntity<String> receivePayload(@RequestBody String payload) {
        try {
            logger.debug("Received inbound HTTP payload of length: {}", payload != null ? payload.length() : 0);
            
            // Use the sender adapter to process the inbound request
            // In middleware terminology, sender adapters receive data FROM external systems
            Map<String, Object> headers = new HashMap<>();
            headers.put("requestMethod", "POST");
            headers.put("contentType", "application/json");
            
            AdapterResult result = httpSenderAdapter.send(payload, headers);
            
            if (result.isSuccess()) {
                String responsePayload = result.getData() != null ? result.getData().toString() : "OK";
                logger.debug("Successfully processed inbound payload");
                return ResponseEntity.ok(responsePayload);
            } else {
                logger.warn("Failed to process inbound payload: {}", result.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process payload: " + result.getMessage());
            }
            
        } catch (AdapterException e) {
            logger.error("Adapter error processing inbound HTTP payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Adapter error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing inbound HTTP payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process inbound payload: " + e.getMessage());
        }
    }
    
    /**
     * Connection test endpoint for health checks.
     * 
     * @return ResponseEntity with connection status
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        try {
            AdapterResult result = httpSenderAdapter.testConnection();
            
            if (result.isSuccess()) {
                return ResponseEntity.ok("Connection test successful: " + result.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Connection test failed: " + result.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error testing HTTP adapter connection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Connection test error: " + e.getMessage());
        }
    }

    // Add additional endpoints (GET, PUT, DELETE) here if needed
}
