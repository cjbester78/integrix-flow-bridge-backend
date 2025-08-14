package com.integrixs.backend.saga;

import lombok.Builder;
import lombok.Data;

/**
 * Result of a saga step execution.
 * 
 * @param <T> the type of result data
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
public class StepResult<T> {
    
    private String stepName;
    private boolean success;
    private T data;
    private String errorMessage;
    private long executionTimeMs;
    
    /**
     * Creates a successful step result.
     * 
     * @param stepName the step name
     * @param data the result data
     * @param <T> the data type
     * @return successful result
     */
    public static <T> StepResult<T> success(String stepName, T data) {
        return StepResult.<T>builder()
                .stepName(stepName)
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Creates a failed step result.
     * 
     * @param stepName the step name
     * @param errorMessage the error message
     * @param <T> the data type
     * @return failed result
     */
    public static <T> StepResult<T> failure(String stepName, String errorMessage) {
        return StepResult.<T>builder()
                .stepName(stepName)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}