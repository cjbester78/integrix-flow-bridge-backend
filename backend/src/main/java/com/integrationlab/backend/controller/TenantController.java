package com.integrationlab.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TenantController {
    
    /**
     * Get current tenant (stub implementation)
     * In a multi-tenant system, this would return the current tenant context
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentTenant() {
        log.debug("Getting current tenant");
        
        // Return a default tenant with all required fields
        Map<String, Object> tenant = new HashMap<>();
        tenant.put("id", "default");
        tenant.put("name", "Default Tenant");
        tenant.put("displayName", "Default Tenant");
        tenant.put("subdomain", "default");
        tenant.put("status", "active");
        tenant.put("planId", "enterprise");
        tenant.put("userRole", "admin");
        tenant.put("primary", true);
        
        return ResponseEntity.ok(tenant);
    }
    
    /**
     * Get user's tenants (stub implementation)
     * In a multi-tenant system, this would return all tenants the user has access to
     */
    @GetMapping("/my-tenants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserTenants() {
        log.debug("Getting user tenants");
        
        // Return the default tenant in a list with all required fields
        Map<String, Object> tenant = new HashMap<>();
        tenant.put("id", "default");
        tenant.put("name", "Default Tenant");
        tenant.put("displayName", "Default Tenant");
        tenant.put("subdomain", "default");
        tenant.put("status", "active");
        tenant.put("planId", "enterprise");
        tenant.put("userRole", "admin");
        tenant.put("primary", true);
        
        return ResponseEntity.ok(Collections.singletonList(tenant));
    }
    
    /**
     * Switch to a different tenant (stub implementation)
     */
    @PostMapping("/switch/{tenantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> switchTenant(@PathVariable String tenantId) {
        log.info("Switching to tenant: {}", tenantId);
        
        // Just return success for now
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tenantId", tenantId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get tenant subscription (stub implementation)
     */
    @GetMapping("/{tenantId}/subscription")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTenantSubscription(@PathVariable String tenantId) {
        log.debug("Getting subscription for tenant: {}", tenantId);
        
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("id", 1);
        subscription.put("planId", "enterprise");
        subscription.put("planName", "Enterprise");
        subscription.put("status", "active");
        subscription.put("features", Arrays.asList("unlimited_flows", "advanced_analytics", "priority_support"));
        
        Map<String, Integer> quotas = new HashMap<>();
        quotas.put("flows", 1000);
        quotas.put("executions", 100000);
        quotas.put("messages", 1000000);
        quotas.put("users", 100);
        quotas.put("storage_gb", 100);
        subscription.put("quotas", quotas);
        
        subscription.put("daysRemaining", 365);
        
        return ResponseEntity.ok(subscription);
    }
    
    /**
     * Get tenant usage (stub implementation)
     */
    @GetMapping("/{tenantId}/usage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTenantUsage(@PathVariable String tenantId) {
        log.debug("Getting usage for tenant: {}", tenantId);
        
        Map<String, Object> usage = new HashMap<>();
        usage.put("executions", 1250);
        usage.put("messages", 15000);
        usage.put("apiCalls", 5000);
        usage.put("storageGb", 2.5);
        usage.put("users", 5);
        usage.put("flows", 12);
        
        Map<String, Integer> quotas = new HashMap<>();
        quotas.put("flows", 1000);
        quotas.put("executions", 100000);
        quotas.put("messages", 1000000);
        quotas.put("users", 100);
        quotas.put("storage_gb", 100);
        usage.put("quotas", quotas);
        
        return ResponseEntity.ok(usage);
    }
}