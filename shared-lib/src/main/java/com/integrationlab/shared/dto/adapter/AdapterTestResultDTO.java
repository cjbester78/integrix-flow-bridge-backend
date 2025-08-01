package com.integrationlab.shared.dto.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for adapter test results.
 * 
 * <p>Contains the outcome of an adapter connectivity test including
 * success status, error messages, and diagnostic information.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterTestResultDTO {
    
    /**
     * Name or type of adapter that was tested
     */
    @NotBlank(message = "Adapter name is required")
    private String adapter;
    
    /**
     * Whether the test was successful
     */
    @NotNull(message = "Success status is required")
    private boolean success;
    
    /**
     * Result message or error description
     */
    @NotBlank(message = "Message is required")
    private String message;
    
    /**
     * Timestamp of when the test was performed
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Response time in milliseconds (optional)
     */
    private Long responseTimeMs;
    
    /**
     * Additional diagnostic information (optional)
     */
    private Map<String, Object> diagnostics;
}
