package com.integrixs.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for REST API errors.
 * 
 * <p>This class provides a consistent error response format across all API endpoints,
 * making it easier for clients to handle errors uniformly.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error category or type
     */
    private String error;
    
    /**
     * Specific error code for programmatic handling
     */
    private String errorCode;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * Additional context information about the error
     */
    private Map<String, Object> context;
    
    /**
     * Unique trace ID for error tracking (optional)
     */
    private String traceId;
}