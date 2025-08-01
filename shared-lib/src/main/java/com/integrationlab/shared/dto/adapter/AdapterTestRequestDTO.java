package com.integrationlab.shared.dto.adapter;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adapter test requests.
 * 
 * <p>Used to test adapter connectivity and functionality before
 * deploying to production flows.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterTestRequestDTO {
    
    /**
     * Type of adapter to test (HTTP, JDBC, SOAP, etc.)
     */
    @NotBlank(message = "Adapter type is required")
    private String adapterType;
    
    /**
     * Test payload to send through the adapter
     */
    @NotBlank(message = "Test payload is required")
    private String payload;
}