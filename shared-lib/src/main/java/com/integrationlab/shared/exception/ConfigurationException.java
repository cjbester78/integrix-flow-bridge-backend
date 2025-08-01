package com.integrationlab.shared.exception;

import java.util.Map;

/**
 * Exception thrown when configuration errors occur.
 * 
 * <p>This exception covers configuration-related errors including:
 * <ul>
 *   <li>Invalid configuration values</li>
 *   <li>Missing required configuration</li>
 *   <li>Configuration parsing errors</li>
 *   <li>Environment setup issues</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class ConfigurationException extends BaseIntegrationException {
    
    /**
     * Constructs a new configuration exception.
     * 
     * @param errorCode specific configuration error code
     * @param message human-readable error message
     */
    public ConfigurationException(String errorCode, String message) {
        super(errorCode, ErrorCategory.CONFIGURATION, message);
    }
    
    /**
     * Constructs a new configuration exception with cause.
     * 
     * @param errorCode specific configuration error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public ConfigurationException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.CONFIGURATION, message, cause);
    }
    
    /**
     * Constructs a new configuration exception with context.
     * 
     * @param errorCode specific configuration error code
     * @param message human-readable error message
     * @param context additional context information
     */
    public ConfigurationException(String errorCode, String message, Map<String, Object> context) {
        super(errorCode, ErrorCategory.CONFIGURATION, message, context);
    }
    
    /**
     * Creates an exception for missing required configuration.
     * 
     * @param configKey the missing configuration key
     * @param component the component requiring the configuration
     * @return configuration exception
     */
    public static ConfigurationException missingRequired(String configKey, String component) {
        return new ConfigurationException(
            "CONFIG_MISSING_REQUIRED",
            String.format("Required configuration '%s' is missing for %s", configKey, component)
        ).withContext("configKey", configKey)
         .withContext("component", component);
    }
    
    /**
     * Creates an exception for invalid configuration value.
     * 
     * @param configKey the configuration key
     * @param value the invalid value
     * @param expectedFormat expected format or constraint
     * @return configuration exception
     */
    public static ConfigurationException invalidValue(String configKey, Object value, 
                                                    String expectedFormat) {
        return new ConfigurationException(
            "CONFIG_INVALID_VALUE",
            String.format("Invalid value for configuration '%s': expected %s", 
                        configKey, expectedFormat)
        ).withContext("configKey", configKey)
         .withContext("value", value)
         .withContext("expectedFormat", expectedFormat);
    }
    
    /**
     * Creates an exception for configuration parsing errors.
     * 
     * @param source configuration source (file, environment, etc.)
     * @param parseError specific parsing error
     * @param cause underlying parsing exception
     * @return configuration exception
     */
    public static ConfigurationException parsingFailed(String source, String parseError, 
                                                     Throwable cause) {
        return new ConfigurationException(
            "CONFIG_PARSE_ERROR",
            String.format("Failed to parse configuration from %s: %s", source, parseError),
            cause
        ).withContext("source", source)
         .withContext("parseError", parseError);
    }
    
    /**
     * Creates an exception for environment configuration issues.
     * 
     * @param environment the environment name
     * @param issue specific issue description
     * @return configuration exception
     */
    public static ConfigurationException environmentIssue(String environment, String issue) {
        return new ConfigurationException(
            "CONFIG_ENVIRONMENT_ISSUE",
            String.format("Environment configuration issue in '%s': %s", environment, issue)
        ).withContext("environment", environment)
         .withContext("issue", issue);
    }
    
    @Override
    public int getHttpStatusCode() {
        return 500; // Internal Server Error - configuration issues are server-side
    }
    
    @Override
    public ConfigurationException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}