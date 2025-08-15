package com.integrationlab.backend.service;

import com.integrationlab.backend.config.EnvironmentConfig;
import com.integrationlab.backend.exception.ForbiddenException;
import com.integrationlab.backend.security.SecurityUtils;
import com.integrationlab.shared.enums.EnvironmentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing environment-based permissions.
 * Enforces restrictions based on the system environment type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentPermissionService {
    
    private final EnvironmentConfig environmentConfig;
    
    /**
     * Check if a specific action is allowed in the current environment
     * 
     * @param action The action to check
     * @throws ForbiddenException if the action is not allowed
     */
    public void checkPermission(String action) {
        if (!isActionAllowed(action)) {
            throw new ForbiddenException(environmentConfig.getFormattedRestrictionMessage());
        }
    }
    
    /**
     * Check if an action is allowed without throwing exception
     * 
     * @param action The action to check
     * @return true if allowed, false otherwise
     */
    public boolean isActionAllowed(String action) {
        // Administrators can always perform admin functions
        if (SecurityUtils.hasRole("ADMIN") && action.startsWith("admin.")) {
            return true;
        }
        
        switch (action) {
            case "flow.create":
            case "adapter.create":
            case "businessComponent.create":
            case "dataStructure.create":
            case "certificate.upload":
                return environmentConfig.canCreateFlows();
                
            case "adapter.updateConfig":
                return environmentConfig.canModifyAdapterConfig();
                
            case "flow.import":
                return environmentConfig.canImportFlows();
                
            case "flow.deploy":
            case "flow.undeploy":
                return environmentConfig.canDeployFlows();
                
            case "flow.export":
            case "view":
                return true; // Always allowed
                
            default:
                // Default to development-only for unknown actions
                return environmentConfig.isDevelopment();
        }
    }
    
    /**
     * Get environment information
     * 
     * @return Map containing environment details
     */
    public Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("type", environmentConfig.getType().name());
        info.put("displayName", environmentConfig.getType().getDisplayName());
        info.put("description", environmentConfig.getType().getDescription());
        info.put("enforceRestrictions", environmentConfig.isEnforceRestrictions());
        info.put("permissions", getPermissionSummary());
        
        // Add available environment types
        List<String> availableTypes = Arrays.stream(EnvironmentType.values())
                .map(EnvironmentType::name)
                .collect(Collectors.toList());
        info.put("availableTypes", availableTypes);
        
        return info;
    }
    
    /**
     * Get a summary of what's allowed in the current environment
     * 
     * @return Map of permissions
     */
    public Map<String, Boolean> getPermissionSummary() {
        Map<String, Boolean> permissions = new HashMap<>();
        
        permissions.put("canCreateFlows", environmentConfig.canCreateFlows());
        permissions.put("canCreateAdapters", environmentConfig.canCreateAdapters());
        permissions.put("canModifyAdapterConfig", environmentConfig.canModifyAdapterConfig());
        permissions.put("canImportFlows", environmentConfig.canImportFlows());
        permissions.put("canDeployFlows", environmentConfig.canDeployFlows());
        permissions.put("canCreateBusinessComponents", environmentConfig.canCreateBusinessComponents());
        permissions.put("canCreateDataStructures", environmentConfig.canCreateDataStructures());
        
        // Add role-specific permissions
        String currentRole = SecurityUtils.getCurrentUserRole();
        permissions.put("isAdmin", "ADMIN".equals(currentRole));
        permissions.put("canAccessAdmin", "ADMIN".equals(currentRole));
        
        return permissions;
    }
    
    /**
     * Update environment type (admin only)
     * 
     * @param newType The new environment type
     */
    public void updateEnvironmentType(EnvironmentType newType) {
        if (!SecurityUtils.hasRole("ADMIN")) {
            throw new ForbiddenException("Only administrators can change environment type");
        }
        
        log.info("Changing environment type from {} to {}", 
                environmentConfig.getType(), newType);
        
        environmentConfig.setType(newType);
        
        // Log the change
        log.warn("ENVIRONMENT CHANGED: System is now running in {} mode", 
                newType.getDisplayName());
    }
    
    /**
     * Check if UI element should be visible
     * 
     * @param element The UI element identifier
     * @return true if visible, false if should be hidden
     */
    public boolean isUIElementVisible(String element) {
        switch (element) {
            case "createFlowButton":
            case "createAdapterButton":
            case "createBusinessComponentButton":
            case "createDataStructureButton":
                return environmentConfig.isDevelopment();
                
            case "importFlowButton":
            case "deployButton":
            case "exportButton":
            case "adapterConfigButton":
                return true; // Always visible
                
            case "adminPanel":
                return SecurityUtils.hasRole("ADMIN");
                
            default:
                return true;
        }
    }
}