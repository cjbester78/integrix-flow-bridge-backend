package com.integrixs.backend.saga;

/**
 * Interface for a saga step.
 * 
 * <p>Each step in a saga represents a local transaction
 * with its compensating action.
 * 
 * @param <T> the type of data the step operates on
 * @author Integration Team
 * @since 1.0.0
 */
public interface SagaStep<T> {
    
    /**
     * Gets the step name.
     * 
     * @return step name
     */
    String getName();
    
    /**
     * Executes the step.
     * 
     * @param data the input data
     * @return step result
     * @throws Exception if step fails
     */
    StepResult<T> execute(T data) throws Exception;
    
    /**
     * Compensates (rolls back) the step.
     * 
     * @param data the data to compensate
     * @throws Exception if compensation fails
     */
    void compensate(T data) throws Exception;
    
    /**
     * Checks if the step can be retried.
     * 
     * @return true if step can be retried
     */
    default boolean canRetry() {
        return true;
    }
    
    /**
     * Gets the maximum number of retries.
     * 
     * @return max retries
     */
    default int getMaxRetries() {
        return 3;
    }
}