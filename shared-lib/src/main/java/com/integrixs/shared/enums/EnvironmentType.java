package com.integrixs.shared.enums;

/**
 * Enum representing the environment type of the system.
 * Controls available functionality based on the deployment environment.
 */
public enum EnvironmentType {
    /**
     * Development environment - all functionality enabled
     */
    DEVELOPMENT("Development", "All functionality enabled for development"),
    
    /**
     * Quality Assurance environment - limited edit functionality
     */
    QUALITY_ASSURANCE("Quality Assurance", "Limited to adapter configuration, import/export, and deployment"),
    
    /**
     * Production environment - minimal edit functionality
     */
    PRODUCTION("Production", "Limited to adapter configuration, import/export, and deployment");
    
    private final String displayName;
    private final String description;
    
    EnvironmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}