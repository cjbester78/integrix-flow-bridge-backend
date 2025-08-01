package com.integrationlab.backend.saga;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of saga execution.
 * 
 * @param <T> the type of result data
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
public class SagaResult<T> {
    
    private String sagaId;
    private boolean success;
    private T data;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Builder.Default
    private List<StepResult<T>> stepResults = new ArrayList<>();
    
    /**
     * Creates a successful saga result.
     * 
     * @param sagaId the saga ID
     * @param data the result data
     * @param <T> the data type
     * @return successful result
     */
    public static <T> SagaResult<T> success(String sagaId, T data) {
        return SagaResult.<T>builder()
                .sagaId(sagaId)
                .success(true)
                .data(data)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a failed saga result.
     * 
     * @param sagaId the saga ID
     * @param errorMessage the error message
     * @param <T> the data type
     * @return failed result
     */
    public static <T> SagaResult<T> failure(String sagaId, String errorMessage) {
        return SagaResult.<T>builder()
                .sagaId(sagaId)
                .success(false)
                .errorMessage(errorMessage)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Adds a step result.
     * 
     * @param stepResult the step result
     */
    public void addStepResult(StepResult<T> stepResult) {
        this.stepResults.add(stepResult);
    }
}