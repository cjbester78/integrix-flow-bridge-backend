package com.integrationlab.shared.dto.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object for Adapter Configuration.
 * 
 * <p>This DTO represents the configuration for communication adapters in the integration platform.
 * It follows the REVERSED middleware convention where:</p>
 * <ul>
 *   <li><b>SENDER</b> adapters receive data FROM external systems (but are considered OUTBOUND)</li>
 *   <li><b>RECEIVER</b> adapters send data TO external systems (but are considered INBOUND)</li>
 * </ul>
 * 
 * <p>The adapter configuration includes both the adapter metadata and its specific
 * configuration stored as JSON for flexibility across different adapter types.</p>
 * 
 * @author Integration Team
 * @since 1.0.0
 * @see com.integrationlab.adapters.core.AdapterMode
 * @see com.integrationlab.adapters.core.AdapterType
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterConfigDTO {
    
    /**
     * Unique identifier for the adapter.
     */
    private String id;
    
    /**
     * Name of the adapter configuration.
     * Should be unique within a business component.
     */
    private String name;
    
    /**
     * Type of the adapter (HTTP, JDBC, FILE, etc.).
     * @see com.integrationlab.shared.enums.AdapterType
     */
    private String type;
    
    /**
     * Adapter mode determining data flow direction (reversed convention).
     * SENDER = receives FROM external systems (but is OUTBOUND)
     * RECEIVER = sends TO external systems (but is INBOUND)
     */
    private String mode;
    
    /**
     * JSON string containing adapter-specific configuration.
     * Structure varies based on adapter type.
     */
    private String configJson;
    
    /**
     * Human-readable description of the adapter's purpose.
     */
    private String description;
    
    /**
     * Indicates if the adapter is currently active.
     * Inactive adapters are not available for flow execution.
     */
    @Builder.Default
    private boolean active = true;
    
    /**
     * Alternative direction indicator for clarity (reversed convention).
     * OUTBOUND = SENDER mode (receives from external)
     * INBOUND = RECEIVER mode (sends to external)
     * @deprecated Use {@link #mode} instead for consistency
     */
    private String direction;
    
    /**
     * ID of the business component this adapter belongs to.
     */
    private String businessComponentId;
    
    /**
     * Name of the business component this adapter belongs to.
     * Used for display purposes in the UI.
     */
    private String businessComponentName;

    /**
     * Determines if this adapter receives data FROM external systems.
     * Due to reversed convention, SENDER mode = OUTBOUND direction.
     * 
     * @return true if adapter is in SENDER mode or OUTBOUND direction
     */
    @JsonIgnore
    public boolean isSender() {
        return "SENDER".equalsIgnoreCase(mode) || "OUTBOUND".equalsIgnoreCase(direction);
    }
    
    /**
     * Determines if this adapter sends data TO external systems.
     * Due to reversed convention, RECEIVER mode = INBOUND direction.
     * 
     * @return true if adapter is in RECEIVER mode or INBOUND direction
     */
    @JsonIgnore
    public boolean isReceiver() {
        return "RECEIVER".equalsIgnoreCase(mode) || "INBOUND".equalsIgnoreCase(direction);
    }
}