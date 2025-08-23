package com.integrixs.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for integration flow configurations.
 * 
 * <p>Validates that flow configuration DTOs contain valid source/target
 * adapters and proper transformation configurations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FlowConfigurationValidator.class)
@Documented
public @interface ValidFlowConfiguration {
    
    /**
     * Validation error message
     */
    String message() default "Invalid flow configuration";
    
    /**
     * Validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Additional payload
     */
    Class<? extends Payload>[] payload() default {};
}