package com.integrixs.shared.enums;

/**
 * Represents the source type of a message structure
 */
public enum SourceType {
    /**
     * Internal message structures created within the system
     */
    INTERNAL("Internal", "Created within the system"),
    
    /**
     * External message structures imported from third parties
     */
    EXTERNAL("External", "Imported from external sources");
    
    private final String displayName;
    private final String description;
    
    SourceType(String displayName, String description) {
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