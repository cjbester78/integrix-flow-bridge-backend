package com.integrationlab.shared.dto.flow;

import com.integrationlab.shared.validation.ValidFlowConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a new integration flow.
 * 
 * <p>Contains all required information to create a new flow including
 * source/target adapters and transformation configurations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidFlowConfiguration
public class FlowCreateRequestDTO {
    
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
     * Flow configuration in JSON format
     */
    @Size(max = 10000, message = "Configuration cannot exceed 10000 characters")
    private String configuration;
    
    /**
     * Username of the user creating the flow
     */
    @NotBlank(message = "Created by is required")
    private String createdBy;
    
    /**
     * List of transformations to apply in this flow
     */
    @Valid
    @Builder.Default
    private List<FlowTransformationDTO> transformations = new ArrayList<>();
    
    /**
     * Initial status of the flow (defaults to INACTIVE)
     */
    @Builder.Default
    private String status = "INACTIVE";
    
    /**
     * Whether to activate the flow immediately after creation
     */
    @Builder.Default
    private boolean activateOnCreation = false;
}