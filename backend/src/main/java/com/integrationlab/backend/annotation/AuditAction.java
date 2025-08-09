package com.integrationlab.backend.annotation;

import com.integrationlab.data.model.AuditTrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that perform custom actions and should be audited.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
    /**
     * The type of entity being operated on
     */
    String entityType();
    
    /**
     * The action being performed
     */
    AuditTrail.AuditAction action();
}