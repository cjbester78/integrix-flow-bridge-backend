package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.AuditTrailService;
import com.integrationlab.data.model.AuditTrail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for audit trail operations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/audit-trail")
@Tag(name = "Audit Trail", description = "Audit trail management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditTrailController {
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    /**
     * Get audit history for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "Get audit history for entity", description = "Retrieve audit trail for a specific entity")
    public ResponseEntity<Page<AuditTrail>> getEntityAuditHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            Pageable pageable) {
        Page<AuditTrail> auditHistory = auditTrailService.getEntityAuditHistory(entityType, entityId, pageable);
        return ResponseEntity.ok(auditHistory);
    }
    
    /**
     * Get audit history by user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit history by user", description = "Retrieve all audit entries for a specific user")
    public ResponseEntity<Page<AuditTrail>> getUserAuditHistory(
            @PathVariable String userId,
            Pageable pageable) {
        Page<AuditTrail> auditHistory = auditTrailService.getUserAuditHistory(userId, pageable);
        return ResponseEntity.ok(auditHistory);
    }
    
    /**
     * Search audit trail with filters
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search audit trail", description = "Search audit entries with various filters")
    public ResponseEntity<Page<AuditTrail>> searchAuditTrail(
            @Parameter(description = "Entity type filter")
            @RequestParam(required = false) String entityType,
            
            @Parameter(description = "Action type filter")
            @RequestParam(required = false) AuditTrail.AuditAction action,
            
            @Parameter(description = "User ID filter")
            @RequestParam(required = false) String userId,
            
            @Parameter(description = "Start date filter")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "End date filter")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            Pageable pageable) {
        
        Page<AuditTrail> results = auditTrailService.searchAuditTrail(
            entityType, action, userId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(results);
    }
}