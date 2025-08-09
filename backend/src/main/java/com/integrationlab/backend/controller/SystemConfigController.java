package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.EnvironmentPermissionService;
import com.integrationlab.shared.enums.EnvironmentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for system configuration.
 */
@Slf4j
@RestController
@RequestMapping("/api/system/config")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "System configuration management")
public class SystemConfigController {
    
    private final EnvironmentPermissionService environmentPermissionService;
    
    /**
     * Get current environment configuration
     * 
     * @return Environment information
     */
    @GetMapping("/environment")
    @Operation(summary = "Get environment configuration")
    public ResponseEntity<Map<String, Object>> getEnvironmentConfig() {
        log.debug("Getting environment configuration");
        return ResponseEntity.ok(environmentPermissionService.getEnvironmentInfo());
    }
    
    /**
     * Get current permissions based on environment and role
     * 
     * @return Permission summary
     */
    @GetMapping("/permissions")
    @Operation(summary = "Get current user permissions")
    public ResponseEntity<Map<String, Boolean>> getPermissions() {
        log.debug("Getting permission summary");
        return ResponseEntity.ok(environmentPermissionService.getPermissionSummary());
    }
    
    /**
     * Update environment type (admin only)
     * 
     * @param request Environment update request
     * @return Updated environment info
     */
    @PutMapping("/environment")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Update environment type")
    public ResponseEntity<Map<String, Object>> updateEnvironmentType(
            @RequestBody EnvironmentUpdateRequest request) {
        
        log.info("Updating environment type to: {}", request.getEnvironmentType());
        
        try {
            EnvironmentType newType = EnvironmentType.valueOf(request.getEnvironmentType());
            environmentPermissionService.updateEnvironmentType(newType);
            
            return ResponseEntity.ok(environmentPermissionService.getEnvironmentInfo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid environment type: " + request.getEnvironmentType()
            ));
        }
    }
    
    /**
     * Check if a specific action is allowed
     * 
     * @param action The action to check
     * @return Permission status
     */
    @GetMapping("/check-permission")
    @Operation(summary = "Check if an action is allowed")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @RequestParam String action) {
        
        boolean allowed = environmentPermissionService.isActionAllowed(action);
        
        return ResponseEntity.ok(Map.of(
            "action", action,
            "allowed", allowed,
            "environment", environmentPermissionService.getEnvironmentInfo().get("type")
        ));
    }
    
    /**
     * Check UI element visibility
     * 
     * @param element The UI element identifier
     * @return Visibility status
     */
    @GetMapping("/ui-visibility")
    @Operation(summary = "Check UI element visibility")
    public ResponseEntity<Map<String, Object>> checkUIVisibility(
            @RequestParam String element) {
        
        boolean visible = environmentPermissionService.isUIElementVisible(element);
        
        return ResponseEntity.ok(Map.of(
            "element", element,
            "visible", visible
        ));
    }
    
    /**
     * Request DTO for environment update
     */
    public static class EnvironmentUpdateRequest {
        private String environmentType;
        
        public String getEnvironmentType() {
            return environmentType;
        }
        
        public void setEnvironmentType(String environmentType) {
            this.environmentType = environmentType;
        }
    }
}