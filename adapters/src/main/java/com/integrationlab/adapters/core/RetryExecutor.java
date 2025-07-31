package com.integrationlab.adapters.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Retry execution utility for adapter operations.
 * Provides configurable retry strategies with exponential backoff, jitter, and circuit breaker integration.
 */
public class RetryExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);
    
    /**
     * Execute an operation with retry logic based on error handling recommendations.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId unique adapter instance identifier
     * @param operation the operation to execute
     * @param context additional context information
     * @param <T> the return type
     * @return the operation result
     * @throws Exception if all retry attempts fail
     */
    public static <T> T executeWithRetry(AdapterType adapterType,
                                        AdapterMode adapterMode,
                                        String adapterId,
                                        Supplier<T> operation,
                                        Map<String, Object> context) throws Exception {
        
        AdapterExceptionHandler errorHandler = AdapterExceptionHandler.getInstance();
        Exception lastException = null;
        int attemptCount = 0;
        
        while (true) {
            attemptCount++;
            
            try {
                // Check if circuit breaker is open before attempting
                if (attemptCount > 1 && errorHandler.isCircuitOpen(adapterType, adapterMode, adapterId)) {
                    logger.warn("Circuit breaker is open for adapter {}-{}-{}, aborting retry", 
                               adapterType, adapterMode, adapterId);
                    throw new AdapterException.CircuitBreakerException(adapterType, 
                            "Circuit breaker is open, operation aborted");
                }
                
                logger.debug("Executing operation attempt {} for adapter {}-{}-{}", 
                            attemptCount, adapterType, adapterMode, adapterId);
                
                T result = operation.get();
                
                if (attemptCount > 1) {
                    logger.info("Operation succeeded on attempt {} for adapter {}-{}-{}", 
                               attemptCount, adapterType, adapterMode, adapterId);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                // Handle the exception and get retry strategy
                ErrorHandlingResult handlingResult = errorHandler.handleException(
                        adapterType, adapterMode, adapterId, e, context);
                
                RetryStrategy retryStrategy = handlingResult.getRetryStrategy();
                
                if (!retryStrategy.shouldRetry() || attemptCount >= retryStrategy.getMaxRetries()) {
                    logger.error("Operation failed after {} attempts for adapter {}-{}-{}: {}", 
                                attemptCount, adapterType, adapterMode, adapterId, e.getMessage());
                    throw e;
                }
                
                long delayMs = calculateRetryDelay(attemptCount, retryStrategy.getRetryDelayMs());
                
                logger.warn("Operation failed on attempt {} for adapter {}-{}-{}, retrying in {}ms: {}", 
                           attemptCount, adapterType, adapterMode, adapterId, delayMs, e.getMessage());
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AdapterException(adapterType, adapterMode, "Retry interrupted", ie);
                }
            }
        }
    }
    
    /**
     * Execute an operation with custom retry configuration.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId unique adapter instance identifier
     * @param operation the operation to execute
     * @param maxRetries maximum number of retry attempts
     * @param baseDelayMs base delay between retries in milliseconds
     * @param useExponentialBackoff whether to use exponential backoff
     * @param useJitter whether to add random jitter to delays
     * @param context additional context information
     * @param <T> the return type
     * @return the operation result
     * @throws Exception if all retry attempts fail
     */
    public static <T> T executeWithCustomRetry(AdapterType adapterType,
                                              AdapterMode adapterMode,
                                              String adapterId,
                                              Supplier<T> operation,
                                              int maxRetries,
                                              long baseDelayMs,
                                              boolean useExponentialBackoff,
                                              boolean useJitter,
                                              Map<String, Object> context) throws Exception {
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                logger.debug("Custom retry attempt {} for adapter {}-{}-{}", 
                            attempt, adapterType, adapterMode, adapterId);
                
                T result = operation.get();
                
                if (attempt > 1) {
                    logger.info("Operation succeeded on custom retry attempt {} for adapter {}-{}-{}", 
                               attempt, adapterType, adapterMode, adapterId);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempt > maxRetries) {
                    logger.error("Operation failed after {} custom retry attempts for adapter {}-{}-{}: {}", 
                                attempt, adapterType, adapterMode, adapterId, e.getMessage());
                    break;
                }
                
                long delayMs = calculateCustomRetryDelay(attempt, baseDelayMs, 
                                                       useExponentialBackoff, useJitter);
                
                logger.warn("Custom retry attempt {} failed for adapter {}-{}-{}, retrying in {}ms: {}", 
                           attempt, adapterType, adapterMode, adapterId, delayMs, e.getMessage());
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AdapterException(adapterType, adapterMode, "Custom retry interrupted", ie);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Execute an operation with a simple retry count.
     * 
     * @param operation the operation to execute
     * @param maxRetries maximum number of retry attempts
     * @param delayMs delay between retries in milliseconds
     * @param <T> the return type
     * @return the operation result
     * @throws Exception if all retry attempts fail
     */
    public static <T> T executeWithSimpleRetry(Supplier<T> operation, int maxRetries, long delayMs) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt > maxRetries) {
                    break;
                }
                
                logger.debug("Simple retry attempt {} failed, retrying in {}ms", attempt, delayMs);
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Simple retry interrupted", ie);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Calculate retry delay with exponential backoff and jitter.
     * 
     * @param attemptNumber the current attempt number (1-based)
     * @param baseDelayMs the base delay in milliseconds
     * @return calculated delay in milliseconds
     */
    private static long calculateRetryDelay(int attemptNumber, long baseDelayMs) {
        // Exponential backoff: delay = baseDelay * 2^(attempt-1)
        long exponentialDelay = baseDelayMs * (1L << (attemptNumber - 1));
        
        // Cap the maximum delay to prevent extremely long waits
        long cappedDelay = Math.min(exponentialDelay, 60000); // Max 1 minute
        
        // Add jitter (±25% of the delay)
        double jitterFactor = 0.75 + (ThreadLocalRandom.current().nextDouble() * 0.5);
        
        return (long) (cappedDelay * jitterFactor);
    }
    
    /**
     * Calculate custom retry delay.
     * 
     * @param attemptNumber the current attempt number (1-based)
     * @param baseDelayMs the base delay in milliseconds
     * @param useExponentialBackoff whether to use exponential backoff
     * @param useJitter whether to add jitter
     * @return calculated delay in milliseconds
     */
    private static long calculateCustomRetryDelay(int attemptNumber, long baseDelayMs, 
                                                 boolean useExponentialBackoff, boolean useJitter) {
        
        long delay = baseDelayMs;
        
        if (useExponentialBackoff) {
            delay = baseDelayMs * (1L << (attemptNumber - 1));
            delay = Math.min(delay, 60000); // Cap at 1 minute
        }
        
        if (useJitter) {
            double jitterFactor = 0.75 + (ThreadLocalRandom.current().nextDouble() * 0.5);
            delay = (long) (delay * jitterFactor);
        }
        
        return delay;
    }
    
    /**
     * Create a context map with common retry information.
     * 
     * @param operationName the name of the operation being retried
     * @param additionalContext any additional context information
     * @return context map
     */
    public static Map<String, Object> createRetryContext(String operationName, 
                                                        Map<String, Object> additionalContext) {
        Map<String, Object> context = new HashMap<>();
        context.put("operation", operationName);
        context.put("retryTimestamp", System.currentTimeMillis());
        
        if (additionalContext != null) {
            context.putAll(additionalContext);
        }
        
        return context;
    }
}

