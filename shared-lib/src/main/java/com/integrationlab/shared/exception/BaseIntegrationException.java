package com.integrationlab.shared.exception;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all integration-related exceptions.
 * 
 * <p>This abstract class provides a structured way to handle errors throughout the
 * Integrix Flow Bridge application. It includes error codes, categories, and contextual
 * information to help with debugging and error reporting.</p>
 * 
 * <p>All custom exceptions in the application should extend this class to ensure
 * consistent error handling and reporting.</p>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Getter
public abstract class BaseIntegrationException extends RuntimeException {
    
    /**
     * Unique error code for this exception type.
     * Format: CATEGORY_SPECIFIC_ERROR (e.g., ADAPTER_CONNECTION_TIMEOUT)
     */
    private final String errorCode;
    
    /**
     * Category of the error for grouping and filtering.
     */
    private final ErrorCategory category;
    
    /**
     * Additional context information about the error.
     * Can include field names, values, or any relevant debugging information.
     */
    private final Map<String, Object> context;
    
    /**
     * Timestamp when the error occurred.
     */
    private final LocalDateTime timestamp;
    
    /**
     * Constructs a new integration exception.
     * 
     * @param errorCode unique error code
     * @param category error category
     * @param message human-readable error message
     */
    protected BaseIntegrationException(String errorCode, ErrorCategory category, String message) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructs a new integration exception with a cause.
     * 
     * @param errorCode unique error code
     * @param category error category
     * @param message human-readable error message
     * @param cause the cause of this exception
     */
    protected BaseIntegrationException(String errorCode, ErrorCategory category, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructs a new integration exception with context.
     * 
     * @param errorCode unique error code
     * @param category error category
     * @param message human-readable error message
     * @param context additional context information
     */
    protected BaseIntegrationException(String errorCode, ErrorCategory category, String message, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
        this.context = new HashMap<>(context);
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructs a new integration exception with cause and context.
     * 
     * @param errorCode unique error code
     * @param category error category
     * @param message human-readable error message
     * @param cause the cause of this exception
     * @param context additional context information
     */
    protected BaseIntegrationException(String errorCode, ErrorCategory category, String message, 
                                     Throwable cause, Map<String, Object> context) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
        this.context = new HashMap<>(context);
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Adds context information to this exception.
     * 
     * @param key context key
     * @param value context value
     * @return this exception for fluent API
     */
    public BaseIntegrationException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
    
    /**
     * Determines if this error is retryable.
     * Subclasses should override this method to indicate if the operation
     * that caused this error can be retried.
     * 
     * @return true if the error is retryable, false otherwise
     */
    public boolean isRetryable() {
        return false;
    }
    
    /**
     * Gets the suggested HTTP status code for this error.
     * Used when translating exceptions to HTTP responses.
     * 
     * @return suggested HTTP status code
     */
    public abstract int getHttpStatusCode();
}