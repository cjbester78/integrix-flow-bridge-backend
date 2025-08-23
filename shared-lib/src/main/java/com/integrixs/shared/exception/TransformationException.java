package com.integrixs.shared.exception;

/**
 * Exception thrown when data transformation operations fail.
 * 
 * <p>This exception covers transformation-related errors including:
 * <ul>
 *   <li>Field mapping errors</li>
 *   <li>Data type conversion failures</li>
 *   <li>Script execution errors</li>
 *   <li>Template processing failures</li>
 *   <li>Schema validation errors</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class TransformationException extends BaseIntegrationException {
    
    /**
     * Constructs a new transformation exception.
     * 
     * @param errorCode specific transformation error code
     * @param message human-readable error message
     */
    public TransformationException(String errorCode, String message) {
        super(errorCode, ErrorCategory.TRANSFORMATION, message);
    }
    
    /**
     * Constructs a new transformation exception with cause.
     * 
     * @param errorCode specific transformation error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public TransformationException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.TRANSFORMATION, message, cause);
    }
    
    /**
     * Creates an exception for field mapping failure.
     * 
     * @param sourceField source field name
     * @param targetField target field name
     * @param reason failure reason
     * @return transformation exception
     */
    public static TransformationException fieldMappingFailed(String sourceField, String targetField, 
                                                           String reason) {
        return new TransformationException(
            "TRANSFORM_FIELD_MAPPING_FAILED",
            String.format("Failed to map field '%s' to '%s': %s", sourceField, targetField, reason)
        ).withContext("sourceField", sourceField)
         .withContext("targetField", targetField)
         .withContext("reason", reason);
    }
    
    /**
     * Creates an exception for type conversion failure.
     * 
     * @param value the value that failed to convert
     * @param sourceType source data type
     * @param targetType target data type
     * @param cause underlying conversion error
     * @return transformation exception
     */
    public static TransformationException typeConversionFailed(Object value, String sourceType, 
                                                             String targetType, Throwable cause) {
        return new TransformationException(
            "TRANSFORM_TYPE_CONVERSION_FAILED",
            String.format("Cannot convert value from %s to %s", sourceType, targetType),
            cause
        ).withContext("value", value)
         .withContext("sourceType", sourceType)
         .withContext("targetType", targetType);
    }
    
    /**
     * Creates an exception for script execution failure.
     * 
     * @param scriptId ID or name of the script
     * @param scriptLanguage language of the script (JavaScript, Python, etc.)
     * @param errorMessage script error message
     * @param lineNumber line number where error occurred
     * @return transformation exception
     */
    public static TransformationException scriptExecutionFailed(String scriptId, String scriptLanguage,
                                                              String errorMessage, Integer lineNumber) {
        TransformationException ex = new TransformationException(
            "TRANSFORM_SCRIPT_EXECUTION_FAILED",
            String.format("Script '%s' failed: %s", scriptId, errorMessage)
        ).withContext("scriptId", scriptId)
         .withContext("scriptLanguage", scriptLanguage)
         .withContext("errorMessage", errorMessage);
        
        if (lineNumber != null) {
            ex.withContext("lineNumber", lineNumber);
        }
        
        return ex;
    }
    
    /**
     * Creates an exception for template processing failure.
     * 
     * @param templateId ID or name of the template
     * @param templateEngine template engine used (Velocity, Freemarker, etc.)
     * @param reason failure reason
     * @return transformation exception
     */
    public static TransformationException templateProcessingFailed(String templateId, 
                                                                 String templateEngine, 
                                                                 String reason) {
        return new TransformationException(
            "TRANSFORM_TEMPLATE_PROCESSING_FAILED",
            String.format("Failed to process template '%s': %s", templateId, reason)
        ).withContext("templateId", templateId)
         .withContext("templateEngine", templateEngine)
         .withContext("reason", reason);
    }
    
    /**
     * Creates an exception for schema validation failure.
     * 
     * @param schemaType type of schema (JSON Schema, XSD, etc.)
     * @param validationErrors list of validation errors
     * @return transformation exception
     */
    public static TransformationException schemaValidationFailed(String schemaType, 
                                                               String validationErrors) {
        return new TransformationException(
            "TRANSFORM_SCHEMA_VALIDATION_FAILED",
            String.format("%s validation failed: %s", schemaType, validationErrors)
        ).withContext("schemaType", schemaType)
         .withContext("validationErrors", validationErrors);
    }
    
    /**
     * Creates an exception for missing required transformation data.
     * 
     * @param dataElement missing data element
     * @param transformationStep step where data was required
     * @return transformation exception
     */
    public static TransformationException missingRequiredData(String dataElement, 
                                                            String transformationStep) {
        return new TransformationException(
            "TRANSFORM_MISSING_REQUIRED_DATA",
            String.format("Required data element '%s' is missing for transformation step '%s'", 
                        dataElement, transformationStep)
        ).withContext("dataElement", dataElement)
         .withContext("transformationStep", transformationStep);
    }
    
    @Override
    public int getHttpStatusCode() {
        return 422; // Unprocessable Entity - the request was well-formed but semantically incorrect
    }
    
    @Override
    public TransformationException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}