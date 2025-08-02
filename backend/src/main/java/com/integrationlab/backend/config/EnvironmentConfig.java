package com.integrationlab.backend.config;

import com.integrationlab.shared.enums.EnvironmentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for system environment type.
 * Controls available functionality based on deployment environment.
 */
@Configuration
@ConfigurationProperties(prefix = "system.environment")
@Getter
@Setter
public class EnvironmentConfig {
    
    /**
     * The environment type of the system
     */
    private EnvironmentType type = EnvironmentType.DEVELOPMENT;
    
    /**
     * Whether to enforce environment restrictions
     */
    private boolean enforceRestrictions = true;
    
    /**
     * Custom message to display for restricted actions
     */
    private String restrictionMessage = "This action is not allowed in %s environment";
    
    @PostConstruct
    public void init() {
        System.out.println("System Environment: " + type.getDisplayName());
        System.out.println("Enforce Restrictions: " + enforceRestrictions);
    }
    
    /**
     * Check if the system is in development mode
     */
    public boolean isDevelopment() {
        return type == EnvironmentType.DEVELOPMENT;
    }
    
    /**
     * Check if the system is in QA mode
     */
    public boolean isQualityAssurance() {
        return type == EnvironmentType.QUALITY_ASSURANCE;
    }
    
    /**
     * Check if the system is in production mode
     */
    public boolean isProduction() {
        return type == EnvironmentType.PRODUCTION;
    }
    
    /**
     * Check if creating flows is allowed
     */
    public boolean canCreateFlows() {
        return !enforceRestrictions || isDevelopment();
    }
    
    /**
     * Check if creating adapters is allowed
     */
    public boolean canCreateAdapters() {
        return !enforceRestrictions || isDevelopment();
    }
    
    /**
     * Check if modifying adapter configuration is allowed
     */
    public boolean canModifyAdapterConfig() {
        return true; // Allowed in all environments
    }
    
    /**
     * Check if importing flows is allowed
     */
    public boolean canImportFlows() {
        return true; // Allowed in all environments
    }
    
    /**
     * Check if deploying flows is allowed
     */
    public boolean canDeployFlows() {
        return true; // Allowed in all environments
    }
    
    /**
     * Check if creating business components is allowed
     */
    public boolean canCreateBusinessComponents() {
        return !enforceRestrictions || isDevelopment();
    }
    
    /**
     * Check if creating data structures is allowed
     */
    public boolean canCreateDataStructures() {
        return !enforceRestrictions || isDevelopment();
    }
    
    /**
     * Get formatted restriction message
     */
    public String getFormattedRestrictionMessage() {
        return String.format(restrictionMessage, type.getDisplayName());
    }
}