package com.integrationlab.shared.dto.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for system log entries.
 * 
 * <p>Represents audit and monitoring log entries for tracking
 * system activities, errors, and user actions.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemLogDTO {
    
    /**
     * Unique identifier for the log entry
     */
    private String id;
    
    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Log level (ERROR, WARN, INFO, DEBUG, TRACE)
     */
    @NotBlank(message = "Log level is required")
    @Pattern(regexp = "^(ERROR|WARN|INFO|DEBUG|TRACE)$", 
             message = "Level must be ERROR, WARN, INFO, DEBUG, or TRACE")
    private String level;
    
    /**
     * Log message
     */
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;
    
    /**
     * Additional details or stack trace
     */
    @Size(max = 5000, message = "Details cannot exceed 5000 characters")
    private String details;
    
    /**
     * Source system or service
     */
    @NotBlank(message = "Source is required")
    private String source;
    
    /**
     * ID of the source entity
     */
    private String sourceId;
    
    /**
     * Name of the source entity
     */
    private String sourceName;
    
    /**
     * Component that generated the log
     */
    private String component;
    
    /**
     * ID of the component
     */
    private String componentId;
    
    /**
     * Domain type (FLOW, ADAPTER, USER, SYSTEM)
     */
    @Pattern(regexp = "^(FLOW|ADAPTER|USER|SYSTEM|TRANSFORMATION|BUSINESS_COMPONENT)$",
             message = "Invalid domain type")
    private String domainType;
    
    /**
     * Reference ID of the domain entity
     */
    private String domainReferenceId;
    
    /**
     * User ID who triggered the action (if applicable)
     */
    private String userId;
    
    /**
     * Timestamp when the log was created in the system
     */
    private LocalDateTime createdAt;
    
    /**
     * Additional contextual data
     */
    private Map<String, Object> context;
    
    /**
     * Request/correlation ID for tracing
     */
    private String correlationId;
    
    /**
     * IP address of the client (if applicable)
     */
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^(?:[a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4}$",
             message = "Invalid IP address format")
    private String clientIp;
}