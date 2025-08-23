package com.integrixs.backend.saga;

import java.util.List;

/**
 * Interface for saga pattern implementation.
 * 
 * <p>Sagas manage distributed transactions across multiple services
 * using compensating transactions.
 * 
 * @param <T> the type of data the saga operates on
 * @author Integration Team
 * @since 1.0.0
 */
public interface Saga<T> {
    
    /**
     * Gets the saga ID.
     * 
     * @return saga ID
     */
    String getSagaId();
    
    /**
     * Gets the saga type.
     * 
     * @return saga type
     */
    String getSagaType();
    
    /**
     * Gets the saga steps.
     * 
     * @return list of saga steps
     */
    List<SagaStep<T>> getSteps();
    
    /**
     * Executes the saga.
     * 
     * @param data the input data
     * @return saga result
     */
    SagaResult<T> execute(T data);
    
    /**
     * Compensates the saga (rollback).
     * 
     * @param data the data to compensate
     * @param failedStep the step that failed
     */
    void compensate(T data, SagaStep<T> failedStep);
}