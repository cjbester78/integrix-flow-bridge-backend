package com.integrixs.shared.dto.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.integrixs.shared.dto.mapping.FieldMappingDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for flow transformation configuration.
 * 
 * <p>Represents a transformation step within an integration flow,
 * including field mappings, filters, enrichments, and validations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowTransformationDTO {
    
    /**
     * Unique identifier for the transformation
     */
    private String id;
    
    /**
     * Parent flow ID
     */
    @NotBlank(message = "Flow ID is required")
    private String flowId;
    
    /**
     * Type of transformation (MAPPING, FILTER, ENRICHMENT, VALIDATION, CUSTOM)
     */
    @NotBlank(message = "Transformation type is required")
    @Pattern(regexp = "^(MAPPING|FILTER|ENRICHMENT|VALIDATION|CUSTOM)$",
             message = "Type must be MAPPING, FILTER, ENRICHMENT, VALIDATION, or CUSTOM")
    private String type;
    
    /**
     * Transformation configuration in JSON format
     */
    @Size(max = 10000, message = "Configuration cannot exceed 10000 characters")
    private String configuration;
    
    /**
     * Execution order within the flow (1-based)
     */
    @NotNull(message = "Execution order is required")
    @Min(value = 1, message = "Execution order must be at least 1")
    @Max(value = 100, message = "Execution order cannot exceed 100")
    private int executionOrder;
    
    /**
     * Whether this transformation is active
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private boolean isActive = true;
    
    /**
     * Timestamp when transformation was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when transformation was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Field mappings for this transformation
     */
    @Valid
    @Builder.Default
    private List<FieldMappingDTO> fieldMappings = new ArrayList<>();
    
    /**
     * Transformation name for display
     */
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    /**
     * Description of what this transformation does
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Error handling strategy (FAIL, SKIP, DEFAULT)
     */
    @Pattern(regexp = "^(FAIL|SKIP|DEFAULT)$",
             message = "Error strategy must be FAIL, SKIP, or DEFAULT")
    @Builder.Default
    private String errorStrategy = "FAIL";
}