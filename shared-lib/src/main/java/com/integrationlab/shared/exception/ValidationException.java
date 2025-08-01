package com.integrationlab.shared.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails for input data, configurations, or business rules.
 * 
 * <p>This exception should be used for all validation-related errors including:
 * <ul>
 *   <li>Bean validation failures</li>
 *   <li>Business rule violations</li>
 *   <li>Invalid input parameters</li>
 *   <li>Configuration validation errors</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class ValidationException extends BaseIntegrationException {
    
    /**
     * Constructs a new validation exception.
     * 
     * @param errorCode specific validation error code
     * @param message human-readable error message
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, ErrorCategory.VALIDATION, message);
    }
    
    /**
     * Constructs a new validation exception with context.
     * 
     * @param errorCode specific validation error code
     * @param message human-readable error message
     * @param context additional context (e.g., field names, invalid values)
     */
    public ValidationException(String errorCode, String message, Map<String, Object> context) {
        super(errorCode, ErrorCategory.VALIDATION, message, context);
    }
    
    /**
     * Constructs a new validation exception with cause.
     * 
     * @param errorCode specific validation error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.VALIDATION, message, cause);
    }
    
    /**
     * Creates a validation exception for a missing required field.
     * 
     * @param fieldName name of the missing field
     * @return validation exception
     */
    public static ValidationException missingRequiredField(String fieldName) {
        return new ValidationException(
            "VALIDATION_REQUIRED_FIELD",
            String.format("Required field '%s' is missing", fieldName)
        ).withContext("field", fieldName);
    }
    
    /**
     * Creates a validation exception for an invalid field value.
     * 
     * @param fieldName name of the field
     * @param value invalid value
     * @param reason reason why the value is invalid
     * @return validation exception
     */
    public static ValidationException invalidFieldValue(String fieldName, Object value, String reason) {
        return new ValidationException(
            "VALIDATION_INVALID_VALUE",
            String.format("Invalid value for field '%s': %s", fieldName, reason)
        ).withContext("field", fieldName)
         .withContext("value", value)
         .withContext("reason", reason);
    }
    
    /**
     * Creates a validation exception for a business rule violation.
     * 
     * @param rule name or description of the business rule
     * @param message detailed error message
     * @return validation exception
     */
    public static ValidationException businessRuleViolation(String rule, String message) {
        return new ValidationException(
            "VALIDATION_BUSINESS_RULE",
            message
        ).withContext("rule", rule);
    }
    
    @Override
    public int getHttpStatusCode() {
        return 400; // Bad Request
    }
    
    @Override
    public ValidationException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}