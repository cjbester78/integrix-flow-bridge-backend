package com.integrixs.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring backend availability.
 * This endpoint is used by the frontend to detect when the backend is ready.
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    /**
     * Simple health check endpoint
     * Returns 200 OK when the backend is ready to serve requests
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Integrix Flow Bridge Backend");
        
        log.debug("Health check requested - Status: UP");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Liveness probe endpoint
     * Used for container orchestration platforms
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "alive");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Readiness probe endpoint
     * Can be extended to check database connectivity, etc.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        Map<String, String> response = new HashMap<>();
        
        // In the future, we can add checks for:
        // - Database connectivity
        // - Cache availability
        // - External service dependencies
        
        response.put("status", "ready");
        return ResponseEntity.ok(response);
    }
}