package com.integrationlab.shared.exception;

/**
 * Categories of errors in the integration platform.
 * 
 * <p>Used to group related errors for filtering, monitoring, and handling.
 * Each category represents a major functional area of the application.</p>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public enum ErrorCategory {
    
    /**
     * Validation errors for input data, configurations, or business rules.
     */
    VALIDATION("Validation Error"),
    
    /**
     * Authentication and authorization related errors.
     */
    SECURITY("Security Error"),
    
    /**
     * Adapter connection and communication errors.
     */
    ADAPTER("Adapter Error"),
    
    /**
     * Flow execution and orchestration errors.
     */
    FLOW("Flow Error"),
    
    /**
     * Data transformation and mapping errors.
     */
    TRANSFORMATION("Transformation Error"),
    
    /**
     * Configuration and setup errors.
     */
    CONFIGURATION("Configuration Error"),
    
    /**
     * Database and persistence layer errors.
     */
    DATA_ACCESS("Data Access Error"),
    
    /**
     * External system integration errors.
     */
    INTEGRATION("Integration Error"),
    
    /**
     * System-level errors (resources, internal failures).
     */
    SYSTEM("System Error");
    
    private final String displayName;
    
    ErrorCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}