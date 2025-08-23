package com.integrixs.backend.aspect;

import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.AuditTrail;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for automatic audit logging of service operations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Aspect
@Component
@ConditionalOnProperty(name = "audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditAspect {
    
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired(required = false)
    private AuditTrailService auditTrailService;
    
    private final ThreadLocal<Map<String, Object>> entityCache = new ThreadLocal<>();
    
    /**
     * Capture entity state before update operations
     */
    @Before("@annotation(com.integrixs.backend.annotation.AuditUpdate) && args(id,..)")
    public void captureBeforeUpdate(JoinPoint joinPoint, String id) {
        try {
            String serviceName = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            // Try to get the current entity state
            if (methodName.startsWith("update")) {
                String entityType = extractEntityType(serviceName);
                
                // Store the ID for later use
                Map<String, Object> cache = new HashMap<>();
                cache.put("entityId", id);
                cache.put("entityType", entityType);
                entityCache.set(cache);
                
                log.debug("Captured entity ID {} for audit", id);
            }
        } catch (Exception e) {
            log.error("Failed to capture entity state before update", e);
        }
    }
    
    /**
     * Log CREATE operations
     */
    @AfterReturning(
        pointcut = "@annotation(com.integrixs.backend.annotation.AuditCreate)",
        returning = "result"
    )
    public void auditCreate(JoinPoint joinPoint, Object result) {
        try {
            if (result != null && auditTrailService != null) {
                String serviceName = joinPoint.getTarget().getClass().getSimpleName();
                String entityType = extractEntityType(serviceName);
                String entityId = extractEntityId(result);
                
                auditTrailService.logCreate(entityType, entityId, result);
            }
        } catch (Exception e) {
            log.error("Failed to audit CREATE operation", e);
        }
    }
    
    /**
     * Log UPDATE operations
     */
    @AfterReturning(
        pointcut = "@annotation(com.integrixs.backend.annotation.AuditUpdate)",
        returning = "result"
    )
    public void auditUpdate(JoinPoint joinPoint, Object result) {
        try {
            Map<String, Object> cache = entityCache.get();
            if (cache != null && result != null && auditTrailService != null) {
                String entityType = (String) cache.get("entityType");
                String entityId = (String) cache.get("entityId");
                Object oldEntity = cache.get("oldEntity");
                
                auditTrailService.logUpdate(entityType, entityId, oldEntity, result);
            }
        } catch (Exception e) {
            log.error("Failed to audit UPDATE operation", e);
        } finally {
            entityCache.remove();
        }
    }
    
    /**
     * Log DELETE operations
     */
    @Before("@annotation(com.integrixs.backend.annotation.AuditDelete) && args(id,..)")
    public void auditDelete(JoinPoint joinPoint, String id) {
        try {
            if (auditTrailService != null) {
                String serviceName = joinPoint.getTarget().getClass().getSimpleName();
                String entityType = extractEntityType(serviceName);
                
                // For delete, we might not have the full entity, so just log the ID
                Map<String, Object> details = new HashMap<>();
                details.put("deletedId", id);
                
                auditTrailService.logAction(entityType, id, AuditTrail.AuditAction.DELETE, details);
            }
        } catch (Exception e) {
            log.error("Failed to audit DELETE operation", e);
        }
    }
    
    /**
     * Log custom actions
     */
    @AfterReturning(
        pointcut = "@annotation(auditAction)",
        returning = "result"
    )
    public void auditAction(JoinPoint joinPoint, com.integrixs.backend.annotation.AuditAction auditAction, Object result) {
        try {
            if (auditTrailService != null) {
                String entityType = auditAction.entityType();
                AuditTrail.AuditAction action = auditAction.action();
                
                String entityId = "";
                if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof String) {
                    entityId = (String) joinPoint.getArgs()[0];
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("method", joinPoint.getSignature().getName());
                details.put("result", result != null ? "success" : "failed");
                
                auditTrailService.logAction(entityType, entityId, action, details);
            }
        } catch (Exception e) {
            log.error("Failed to audit custom action", e);
        }
    }
    
    /**
     * Extract entity type from service name
     */
    private String extractEntityType(String serviceName) {
        // Remove "Service" suffix and convert to entity name
        return serviceName.replace("Service", "");
    }
    
    /**
     * Extract entity ID from result object
     */
    private String extractEntityId(Object entity) {
        try {
            // Use reflection to get the ID
            return (String) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            log.warn("Could not extract entity ID", e);
            return "unknown";
        }
    }
}