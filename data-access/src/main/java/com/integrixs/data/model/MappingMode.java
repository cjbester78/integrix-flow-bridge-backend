package com.integrixs.data.model;

/**
 * Enum defining the mapping mode for integration flows
 */
public enum MappingMode {
    /**
     * Standard mode - Convert to XML and apply field mappings
     */
    WITH_MAPPING("With Mapping"),
    
    /**
     * Pass-through mode - Direct message transfer without conversion or mapping
     */
    PASS_THROUGH("Pass Through");
    
    private final String displayName;
    
    MappingMode(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}