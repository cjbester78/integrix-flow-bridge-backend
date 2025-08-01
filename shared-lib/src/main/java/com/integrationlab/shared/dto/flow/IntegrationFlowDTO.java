package com.integrationlab.shared.dto.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for integration flow information.
 * 
 * <p>Represents a complete integration flow including source and target
 * adapters, data transformations, and execution statistics.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationFlowDTO {
    
    /**
     * Unique identifier for the flow
     */
    private String id;
    
    /**
     * Name of the integration flow
     */
    @NotBlank(message = "Flow name is required")
    @Size(min = 3, max = 100, message = "Flow name must be between 3 and 100 characters")
    private String name;
    
    /**
     * Description of the flow's purpose
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Source adapter ID (sender - receives data FROM external systems)
     */
    @NotBlank(message = "Source adapter is required")
    private String sourceAdapterId;
    
    /**
     * Target adapter ID (receiver - sends data TO external systems)
     */
    @NotBlank(message = "Target adapter is required")
    private String targetAdapterId;
    
    /**
     * Source data structure ID
     */
    private String sourceStructureId;
    
    /**
     * Target data structure ID
     */
    private String targetStructureId;
    
    /**
     * Current flow status (ACTIVE, INACTIVE, ERROR, etc.)
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE|ERROR|SUSPENDED)$", 
             message = "Status must be ACTIVE, INACTIVE, ERROR, or SUSPENDED")
    private String status;
    
    /**
     * Flow configuration in JSON format
     */
    private String configuration;
    
    /**
     * Whether the flow is currently active
     */
    @NotNull(message = "Active status is required")
    private boolean isActive;
    
    /**
     * Timestamp when the flow was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the flow was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Username of the user who created the flow
     */
    private String createdBy;
    
    /**
     * Timestamp of last execution
     */
    private LocalDateTime lastExecutionAt;
    
    /**
     * Total number of executions
     */
    @Min(value = 0, message = "Execution count cannot be negative")
    private int executionCount;
    
    /**
     * Number of successful executions
     */
    @Min(value = 0, message = "Success count cannot be negative")
    private int successCount;
    
    /**
     * Number of failed executions
     */
    @Min(value = 0, message = "Error count cannot be negative")
    private int errorCount;
    
    /**
     * List of transformations applied in this flow
     */
    @Builder.Default
    private List<FlowTransformationDTO> transformations = new ArrayList<>();
}