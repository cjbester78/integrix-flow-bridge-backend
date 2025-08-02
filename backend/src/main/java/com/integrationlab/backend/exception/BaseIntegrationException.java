package com.integrationlab.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all integration-related exceptions
 */
public abstract class BaseIntegrationException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String category;
    private final java.time.LocalDateTime timestamp;
    private final java.util.Map<String, Object> context;
    
    public BaseIntegrationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = this.getClass().getSimpleName();
        this.category = "INTEGRATION";
        this.timestamp = java.time.LocalDateTime.now();
        this.context = new java.util.HashMap<>();
    }
    
    public BaseIntegrationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = this.getClass().getSimpleName();
        this.category = "INTEGRATION";
        this.timestamp = java.time.LocalDateTime.now();
        this.context = new java.util.HashMap<>();
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getCategory() {
        return category;
    }
    
    public java.time.LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public java.util.Map<String, Object> getContext() {
        return context;
    }
    
    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}