package com.integrationlab.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for API versioning.
 * 
 * <p>Configures URL-based versioning strategy for REST APIs.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {
    
    /**
     * API version prefix.
     */
    public static final String API_VERSION_PREFIX = "/api/v";
    
    /**
     * Current API version.
     */
    public static final String CURRENT_VERSION = "1";
    
    /**
     * Default API prefix (points to current version).
     */
    public static final String DEFAULT_API_PREFIX = "/api";
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Configure versioning for controllers
        configurer.addPathPrefix(API_VERSION_PREFIX + CURRENT_VERSION, 
            clazz -> clazz.isAnnotationPresent(ApiV1.class));
    }
    
    /**
     * Marker annotation for API version 1 controllers.
     */
    public @interface ApiV1 {
    }
    
    /**
     * Marker annotation for API version 2 controllers (future use).
     */
    public @interface ApiV2 {
    }
}