package com.integrationlab.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for adapter configurations.
 * 
 * <p>Validates that adapter configuration DTOs contain all required
 * fields based on the adapter type and ensures configuration consistency.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdapterConfigValidator.class)
@Documented
public @interface ValidAdapterConfig {
    
    /**
     * Validation error message
     */
    String message() default "Invalid adapter configuration";
    
    /**
     * Validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Additional payload
     */
    Class<? extends Payload>[] payload() default {};
}