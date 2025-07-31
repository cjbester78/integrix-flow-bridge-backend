package com.integrationlab.shared.dto.adapter;

/**
 * DTO for AdapterConfigDTO supporting separated sender/receiver configurations.
 * Encapsulates data for transport between layers with middleware conventions.
 */
public class AdapterConfigDTO {
    private String name;
    private String type;
    private String mode; // SENDER or RECEIVER
    private String configJson;
    private String description;
    private boolean active = true;
    
    // Middleware convention fields
    private String direction; // INBOUND or OUTBOUND for clarity
    private String businessComponentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    
    /**
     * Helper method to determine if this is a sender adapter (receives FROM external systems)
     */
    public boolean isSender() {
        return "SENDER".equalsIgnoreCase(mode) || "INBOUND".equalsIgnoreCase(direction);
    }
    
    /**
     * Helper method to determine if this is a receiver adapter (sends TO external systems)
     */
    public boolean isReceiver() {
        return "RECEIVER".equalsIgnoreCase(mode) || "OUTBOUND".equalsIgnoreCase(direction);
    }
}