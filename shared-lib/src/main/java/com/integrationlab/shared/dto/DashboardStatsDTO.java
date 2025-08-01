package com.integrationlab.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard statistics.
 * 
 * <p>Provides key performance metrics and statistics for the
 * integration platform dashboard.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsDTO {
    
    /**
     * Number of currently active integration flows
     */
    @NotNull(message = "Active integrations count is required")
    @Min(value = 0, message = "Active integrations cannot be negative")
    private int activeIntegrations;
    
    /**
     * Number of messages processed today
     */
    @NotNull(message = "Messages today count is required")
    @Min(value = 0, message = "Messages count cannot be negative")
    private long messagesToday;
    
    /**
     * Success rate as a percentage (0-100)
     */
    @NotNull(message = "Success rate is required")
    @Min(value = 0, message = "Success rate cannot be less than 0")
    @Max(value = 100, message = "Success rate cannot exceed 100")
    private double successRate;
    
    /**
     * Average response time in milliseconds
     */
    @NotNull(message = "Average response time is required")
    @Min(value = 0, message = "Response time cannot be negative")
    private long avgResponseTime;
    
    /**
     * Total number of integration flows
     */
    @Min(value = 0, message = "Total flows cannot be negative")
    private Integer totalFlows;
    
    /**
     * Number of flows with errors
     */
    @Min(value = 0, message = "Error flows cannot be negative")
    private Integer errorFlows;
    
    /**
     * System uptime percentage (0-100)
     */
    @Min(value = 0, message = "Uptime cannot be less than 0")
    @Max(value = 100, message = "Uptime cannot exceed 100")
    private Double uptimePercentage;
}