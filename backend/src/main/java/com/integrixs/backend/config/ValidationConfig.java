package com.integrixs.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Configuration for method-level validation.
 * 
 * <p>Enables validation of method parameters and return values
 * using Bean Validation annotations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
public class ValidationConfig {
    
    /**
     * Enables method-level validation for @Validated annotated classes.
     * 
     * @return method validation post processor
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}