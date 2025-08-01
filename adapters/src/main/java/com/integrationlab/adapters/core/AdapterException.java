package com.integrationlab.adapters.core;

/**
 * Base exception class for all adapter-related errors.
 */
public class AdapterException extends Exception {
    
    private final AdapterType adapterType;
    private final AdapterMode adapterMode;
    private final String errorCode;
    
    public AdapterException(String message) {
        super(message);
        this.adapterType = null;
        this.adapterMode = null;
        this.errorCode = null;
    }
    
    public AdapterException(String message, Throwable cause) {
        super(message, cause);
        this.adapterType = null;
        this.adapterMode = null;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterType adapterType, AdapterMode adapterMode, String message) {
        super(message);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterType adapterType, AdapterMode adapterMode, String message, Throwable cause) {
        super(message, cause);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterType adapterType, AdapterMode adapterMode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = errorCode;
    }
    
    public AdapterType getAdapterType() {
        return adapterType;
    }
    
    public AdapterMode getAdapterMode() {
        return adapterMode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Connection-related exceptions
     */
    public static class ConnectionException extends AdapterException {
        public ConnectionException(AdapterType adapterType, String message) {
            super(adapterType, null, "CONNECTION_ERROR", message, null);
        }
        
        public ConnectionException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "CONNECTION_ERROR", message, cause);
        }
    }
    
    /**
     * Authentication-related exceptions
     */
    public static class AuthenticationException extends AdapterException {
        public AuthenticationException(AdapterType adapterType, String message) {
            super(adapterType, null, "AUTH_ERROR", message, null);
        }
        
        public AuthenticationException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "AUTH_ERROR", message, cause);
        }
    }
    
    /**
     * Configuration-related exceptions
     */
    public static class ConfigurationException extends AdapterException {
        public ConfigurationException(AdapterType adapterType, String message) {
            super(adapterType, null, "CONFIG_ERROR", message, null);
        }
        
        public ConfigurationException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "CONFIG_ERROR", message, cause);
        }
    }
    
    /**
     * Data validation exceptions
     */
    public static class ValidationException extends AdapterException {
        public ValidationException(AdapterType adapterType, String message) {
            super(adapterType, null, "VALIDATION_ERROR", message, null);
        }
        
        public ValidationException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "VALIDATION_ERROR", message, cause);
        }
    }
    
    /**
     * Circuit breaker exceptions
     */
    public static class CircuitBreakerException extends AdapterException {
        public CircuitBreakerException(AdapterType adapterType, String message) {
            super(adapterType, null, "CIRCUIT_BREAKER", message, null);
        }
        
        public CircuitBreakerException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "CIRCUIT_BREAKER", message, cause);
        }
    }
    
    /**
     * Timeout exceptions
     */
    public static class TimeoutException extends AdapterException {
        public TimeoutException(AdapterType adapterType, String message) {
            super(adapterType, null, "TIMEOUT_ERROR", message, null);
        }
        
        public TimeoutException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "TIMEOUT_ERROR", message, cause);
        }
    }
    
    /**
     * Processing exceptions
     */
    public static class ProcessingException extends AdapterException {
        public ProcessingException(AdapterType adapterType, String message) {
            super(adapterType, null, "PROCESSING_ERROR", message, null);
        }
        
        public ProcessingException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "PROCESSING_ERROR", message, cause);
        }
    }
    
    /**
     * Operation exceptions
     */
    public static class OperationException extends AdapterException {
        public OperationException(AdapterType adapterType, String message) {
            super(adapterType, null, "OPERATION_ERROR", message, null);
        }
        
        public OperationException(AdapterType adapterType, String message, Throwable cause) {
            super(adapterType, null, "OPERATION_ERROR", message, cause);
        }
    }
}