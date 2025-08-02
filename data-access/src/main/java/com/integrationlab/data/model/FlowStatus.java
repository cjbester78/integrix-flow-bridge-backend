package com.integrationlab.data.model;

/**
 * Status of an integration flow
 */
public enum FlowStatus {
    DRAFT("Draft"),
    ACTIVE("Active"), 
    INACTIVE("Inactive"),
    ERROR("Error"),
    DEVELOPED_INACTIVE("Developed - Inactive"), // Undeployed
    DEPLOYED_ACTIVE("Deployed - Active"); // Deployed
    
    private final String displayName;
    
    FlowStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isDeployed() {
        return this == DEPLOYED_ACTIVE;
    }
    
    public boolean isDevelopment() {
        return this == DRAFT || this == DEVELOPED_INACTIVE;
    }
}