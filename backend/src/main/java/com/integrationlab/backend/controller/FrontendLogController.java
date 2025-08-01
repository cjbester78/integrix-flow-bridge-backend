package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.SystemLogService;
import com.integrationlab.shared.dto.log.FrontendLogBatchRequest;
import com.integrationlab.shared.dto.log.FrontendLogEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for handling frontend log submissions.
 * Receives logs from the frontend application for centralized logging and monitoring.
 */
@Slf4j
@RestController
@RequestMapping("/api/system/logs")
@Tag(name = "Frontend Logging", description = "Endpoints for frontend application logging")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FrontendLogController {

    private final SystemLogService systemLogService;

    /**
     * Receive a batch of frontend logs.
     *
     * @param request The batch of log entries
     * @param httpRequest The HTTP request for additional context
     * @return Response indicating success
     */
    @PostMapping("/batch")
    @Operation(summary = "Submit frontend logs", description = "Submit a batch of logs from the frontend application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitLogs(
            @Valid @RequestBody FrontendLogBatchRequest request,
            HttpServletRequest httpRequest) {
        
        log.debug("Received {} frontend log entries", request.getLogs().size());
        
        // Extract additional context from HTTP request
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Process each log entry
        for (FrontendLogEntry entry : request.getLogs()) {
            try {
                // Enrich log entry with server-side context
                entry.setClientIp(clientIp);
                if (entry.getUserAgent() == null) {
                    entry.setUserAgent(userAgent);
                }
                if (entry.getServerReceivedAt() == null) {
                    entry.setServerReceivedAt(LocalDateTime.now());
                }
                
                // Save to system log
                systemLogService.logFrontendEvent(entry);
                
            } catch (Exception e) {
                log.error("Failed to process frontend log entry: {}", e.getMessage(), e);
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "processed", request.getLogs().size(),
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Submit a single critical log entry (for fatal errors).
     *
     * @param entry The log entry
     * @param httpRequest The HTTP request for additional context
     * @return Response indicating success
     */
    @PostMapping("/critical")
    @Operation(summary = "Submit critical frontend log", description = "Submit a critical/fatal log from the frontend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitCriticalLog(
            @Valid @RequestBody FrontendLogEntry entry,
            HttpServletRequest httpRequest) {
        
        log.warn("Received critical frontend log: {}", entry.getMessage());
        
        // Enrich with context
        entry.setClientIp(getClientIpAddress(httpRequest));
        entry.setUserAgent(httpRequest.getHeader("User-Agent"));
        entry.setServerReceivedAt(LocalDateTime.now());
        
        // Process immediately
        systemLogService.logFrontendEvent(entry);
        
        // Send alerts for critical errors
        if ("FATAL".equals(entry.getLevel()) || "ERROR".equals(entry.getLevel())) {
            systemLogService.sendErrorAlert(entry);
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Endpoint for navigator.sendBeacon (used during page unload).
     * This endpoint accepts both JSON and form data.
     *
     * @param request The batch of log entries
     * @return Response indicating success
     */
    @PostMapping(value = "/beacon", consumes = {"application/json", "application/x-www-form-urlencoded"})
    @Operation(summary = "Submit logs via beacon", description = "Submit logs using navigator.sendBeacon during page unload")
    public ResponseEntity<Void> submitBeacon(@RequestBody(required = false) String request) {
        
        try {
            // Parse the beacon data
            if (request != null && !request.isEmpty()) {
                log.debug("Received beacon log data: {}", request.length() > 100 ? 
                    request.substring(0, 100) + "..." : request);
                
                // Process the beacon data asynchronously
                systemLogService.processBeaconLogs(request);
            }
        } catch (Exception e) {
            log.error("Failed to process beacon logs: {}", e.getMessage());
        }
        
        // Always return 200 OK for beacon requests
        return ResponseEntity.ok().build();
    }

    /**
     * Get frontend logging configuration.
     *
     * @return Logging configuration for the frontend
     */
    @GetMapping("/config")
    @Operation(summary = "Get frontend logging config", description = "Get configuration for frontend logging")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getLoggingConfig() {
        return ResponseEntity.ok(Map.of(
            "enabled", true,
            "level", systemLogService.getFrontendLogLevel(),
            "batchSize", 50,
            "flushInterval", 5000,
            "retryAttempts", 3,
            "categories", systemLogService.getEnabledCategories()
        ));
    }

    /**
     * Extract client IP address from request.
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}