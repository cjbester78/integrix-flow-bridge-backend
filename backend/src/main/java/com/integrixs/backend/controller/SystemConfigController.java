package com.integrixs.backend.controller;

import com.integrixs.backend.service.EnvironmentPermissionService;
import com.integrixs.backend.service.SystemConfigurationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.shared.enums.EnvironmentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

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
    private final SystemConfigurationService systemConfigurationService;
    
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
     * Get all system configurations
     */
    @GetMapping("/settings")
    @Operation(summary = "Get all system settings")
    public ResponseEntity<Map<String, String>> getSystemSettings() {
        log.debug("Getting all system settings");
        return ResponseEntity.ok(systemConfigurationService.getAllConfigurations());
    }
    
    /**
     * Get system timezone
     */
    @GetMapping("/timezone")
    @Operation(summary = "Get system timezone")
    public ResponseEntity<Map<String, String>> getSystemTimezone() {
        log.debug("Getting system timezone");
        return ResponseEntity.ok(Map.of(
            "timezone", systemConfigurationService.getSystemTimezone(),
            "dateFormat", systemConfigurationService.getDateFormat(),
            "timeFormat", systemConfigurationService.getTimeFormat(),
            "dateTimeFormat", systemConfigurationService.getDateTimeFormat()
        ));
    }
    
    /**
     * Update system timezone
     */
    @PutMapping("/timezone")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Update system timezone")
    public ResponseEntity<Map<String, String>> updateSystemTimezone(
            @RequestBody TimezoneUpdateRequest request) {
        
        log.info("Updating system timezone to: {}", request.getTimezone());
        
        try {
            String username = SecurityUtils.getCurrentUsernameStatic();
            systemConfigurationService.updateSystemTimezone(request.getTimezone(), username);
            
            return ResponseEntity.ok(Map.of(
                "timezone", systemConfigurationService.getSystemTimezone(),
                "message", "Timezone updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get available timezones
     */
    @GetMapping("/timezones")
    @Operation(summary = "Get available timezones")
    public ResponseEntity<List<Map<String, String>>> getAvailableTimezones() {
        log.debug("Getting available timezones");
        return ResponseEntity.ok(systemConfigurationService.getAvailableTimezones());
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
    
    /**
     * Request DTO for timezone update
     */
    public static class TimezoneUpdateRequest {
        private String timezone;
        
        public String getTimezone() {
            return timezone;
        }
        
        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
    }
}