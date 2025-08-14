package com.integrixs.shared.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO representing a single frontend log entry.
 * Contains all information about events, errors, and user actions from the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendLogEntry {
    
    /**
     * Log level (DEBUG, INFO, WARN, ERROR, FATAL)
     */
    @NotNull
    private String level;
    
    /**
     * Log category (AUTH, API, VALIDATION, USER_ACTION, etc.)
     */
    @NotNull
    private String category;
    
    /**
     * Log message
     */
    @NotBlank
    private String message;
    
    /**
     * Additional details as key-value pairs
     */
    private Map<String, Object> details;
    
    /**
     * Error object if applicable
     */
    private Object error;
    
    /**
     * Stack trace for errors
     */
    private String stackTrace;
    
    /**
     * User agent string
     */
    private String userAgent;
    
    /**
     * Current URL when log was created
     */
    private String url;
    
    /**
     * Frontend timestamp when log was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    /**
     * User ID if authenticated
     */
    private String userId;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Correlation ID for tracing requests
     */
    private String correlationId;
    
    // Server-side enriched fields
    
    /**
     * Client IP address (added by server)
     */
    private String clientIp;
    
    /**
     * Server timestamp when log was received
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime serverReceivedAt;
}