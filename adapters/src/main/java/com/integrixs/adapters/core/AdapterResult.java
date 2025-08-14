package com.integrixs.adapters.core;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Standard result object for adapter operations.
 * Contains status, data, metadata, and error information.
 */
public class AdapterResult {
    
    public enum Status {
        SUCCESS,
        FAILURE,
        PARTIAL_SUCCESS,
        TIMEOUT,
        CONNECTION_ERROR,
        AUTHENTICATION_ERROR,
        VALIDATION_ERROR
    }
    
    private Status status;
    private String message;
    private Object data;
    private Throwable error;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private long durationMs;
    
    public AdapterResult() {
        this.metadata = new HashMap<>();
        this.timestamp = Instant.now();
    }
    
    public static AdapterResult success(Object data) {
        AdapterResult result = new AdapterResult();
        result.status = Status.SUCCESS;
        result.data = data;
        result.message = "Operation completed successfully";
        return result;
    }
    
    public static AdapterResult success(Object data, String message) {
        AdapterResult result = new AdapterResult();
        result.status = Status.SUCCESS;
        result.data = data;
        result.message = message;
        return result;
    }
    
    public static AdapterResult failure(String message) {
        AdapterResult result = new AdapterResult();
        result.status = Status.FAILURE;
        result.message = message;
        return result;
    }
    
    public static AdapterResult failure(String message, Throwable error) {
        AdapterResult result = new AdapterResult();
        result.status = Status.FAILURE;
        result.message = message;
        result.error = error;
        return result;
    }
    
    public static AdapterResult connectionError(String message, Throwable error) {
        AdapterResult result = new AdapterResult();
        result.status = Status.CONNECTION_ERROR;
        result.message = message;
        result.error = error;
        return result;
    }
    
    public static AdapterResult authenticationError(String message) {
        AdapterResult result = new AdapterResult();
        result.status = Status.AUTHENTICATION_ERROR;
        result.message = message;
        return result;
    }
    
    public static AdapterResult timeout(String message) {
        AdapterResult result = new AdapterResult();
        result.status = Status.TIMEOUT;
        result.message = message;
        return result;
    }
    
    // Getters and setters
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public void setError(Throwable error) {
        this.error = error;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isFailure() {
        return status == Status.FAILURE || status == Status.CONNECTION_ERROR || 
               status == Status.AUTHENTICATION_ERROR || status == Status.VALIDATION_ERROR ||
               status == Status.TIMEOUT;
    }
    
    @Override
    public String toString() {
        return "AdapterResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", durationMs=" + durationMs +
                '}';
    }
}