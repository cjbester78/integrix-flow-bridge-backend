package com.integrixs.adapters.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Centralized exception handling framework for all adapters.
 * Provides error classification, retry logic, circuit breaker patterns, and error reporting.
 */
public class AdapterExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterExceptionHandler.class);
    
    // Circuit breaker state tracking
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    
    // Error statistics
    private final Map<String, ErrorStatistics> errorStats = new ConcurrentHashMap<>();
    
    // Error listeners for monitoring
    private final Map<String, Consumer<AdapterErrorEvent>> errorListeners = new ConcurrentHashMap<>();
    
    // Singleton instance
    private static final AdapterExceptionHandler INSTANCE = new AdapterExceptionHandler();
    
    private AdapterExceptionHandler() {}
    
    public static AdapterExceptionHandler getInstance() {
        return INSTANCE;
    }
    
    /**
     * Handle an adapter exception with full error processing pipeline.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId unique adapter instance identifier
     * @param exception the exception to handle
     * @param context additional context information
     * @return ErrorHandlingResult with recommended actions
     */
    public ErrorHandlingResult handleException(AdapterType adapterType, 
                                             AdapterMode adapterMode,
                                             String adapterId,
                                             Exception exception,
                                             Map<String, Object> context) {
        
        String adapterKey = createAdapterKey(adapterType, adapterMode, adapterId);
        
        // Classify the error
        ErrorClassification classification = classifyError(exception);
        
        // Update error statistics
        updateErrorStatistics(adapterKey, classification, exception);
        
        // Check circuit breaker state
        CircuitBreakerState circuitState = getOrCreateCircuitBreaker(adapterKey);
        boolean circuitOpen = updateCircuitBreaker(circuitState, classification);
        
        // Determine retry strategy
        RetryStrategy retryStrategy = determineRetryStrategy(classification, circuitState, context);
        
        // Create error event
        AdapterErrorEvent errorEvent = new AdapterErrorEvent(
                adapterType, adapterMode, adapterId, exception, classification, 
                LocalDateTime.now(), context);
        
        // Notify error listeners
        notifyErrorListeners(adapterKey, errorEvent);
        
        // Log the error appropriately
        logError(adapterKey, exception, classification, retryStrategy);
        
        // Create handling result
        ErrorHandlingResult result = new ErrorHandlingResult(
                classification, retryStrategy, circuitOpen, errorEvent);
        
        // Add recovery recommendations
        addRecoveryRecommendations(result, exception, context);
        
        return result;
    }
    
    /**
     * Register an error listener for monitoring and alerting.
     * 
     * @param listenerId unique listener identifier
     * @param listener the error event consumer
     */
    public void registerErrorListener(String listenerId, Consumer<AdapterErrorEvent> listener) {
        errorListeners.put(listenerId, listener);
        logger.info("Registered error listener: {}", listenerId);
    }
    
    /**
     * Unregister an error listener.
     * 
     * @param listenerId the listener identifier to remove
     */
    public void unregisterErrorListener(String listenerId) {
        errorListeners.remove(listenerId);
        logger.info("Unregistered error listener: {}", listenerId);
    }
    
    /**
     * Get error statistics for an adapter.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter instance identifier
     * @return error statistics or null if not found
     */
    public ErrorStatistics getErrorStatistics(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        String adapterKey = createAdapterKey(adapterType, adapterMode, adapterId);
        return errorStats.get(adapterKey);
    }
    
    /**
     * Reset circuit breaker for an adapter.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter instance identifier
     */
    public void resetCircuitBreaker(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        String adapterKey = createAdapterKey(adapterType, adapterMode, adapterId);
        CircuitBreakerState state = circuitBreakers.get(adapterKey);
        if (state != null) {
            state.reset();
            logger.info("Circuit breaker reset for adapter: {}", adapterKey);
        }
    }
    
    /**
     * Check if circuit breaker is open for an adapter.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter instance identifier
     * @return true if circuit is open, false otherwise
     */
    public boolean isCircuitOpen(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        String adapterKey = createAdapterKey(adapterType, adapterMode, adapterId);
        CircuitBreakerState state = circuitBreakers.get(adapterKey);
        return state != null && state.isOpen();
    }
    
    private ErrorClassification classifyError(Exception exception) {
        if (exception instanceof AdapterException.ConnectionException) {
            return ErrorClassification.CONNECTION_ERROR;
        } else if (exception instanceof AdapterException.AuthenticationException) {
            return ErrorClassification.AUTHENTICATION_ERROR;
        } else if (exception instanceof AdapterException.ConfigurationException) {
            return ErrorClassification.CONFIGURATION_ERROR;
        } else if (exception instanceof AdapterException.ValidationException) {
            return ErrorClassification.VALIDATION_ERROR;
        } else if (exception instanceof AdapterException.TimeoutException) {
            return ErrorClassification.TIMEOUT_ERROR;
        } else if (exception instanceof AdapterException) {
            return ErrorClassification.ADAPTER_ERROR;
        } else if (exception instanceof java.net.ConnectException) {
            return ErrorClassification.CONNECTION_ERROR;
        } else if (exception instanceof java.net.SocketTimeoutException) {
            return ErrorClassification.TIMEOUT_ERROR;
        } else if (exception instanceof javax.net.ssl.SSLException) {
            return ErrorClassification.SECURITY_ERROR;
        } else if (exception instanceof java.sql.SQLException) {
            return ErrorClassification.DATABASE_ERROR;
        } else if (exception instanceof java.io.IOException) {
            return ErrorClassification.IO_ERROR;
        } else if (exception instanceof SecurityException) {
            return ErrorClassification.SECURITY_ERROR;
        } else if (exception instanceof IllegalArgumentException || 
                   exception instanceof IllegalStateException) {
            return ErrorClassification.VALIDATION_ERROR;
        } else {
            return ErrorClassification.UNKNOWN_ERROR;
        }
    }
    
    private RetryStrategy determineRetryStrategy(ErrorClassification classification, 
                                               CircuitBreakerState circuitState,
                                               Map<String, Object> context) {
        
        if (circuitState.isOpen()) {
            return new RetryStrategy(false, 0, 0, "Circuit breaker is open");
        }
        
        switch (classification) {
            case CONNECTION_ERROR:
            case TIMEOUT_ERROR:
            case IO_ERROR:
                return new RetryStrategy(true, 3, 5000, "Transient error - retry recommended");
                
            case DATABASE_ERROR:
                return new RetryStrategy(true, 2, 10000, "Database error - limited retry");
                
            case AUTHENTICATION_ERROR:
                return new RetryStrategy(false, 0, 0, "Authentication error - manual intervention required");
                
            case CONFIGURATION_ERROR:
            case VALIDATION_ERROR:
                return new RetryStrategy(false, 0, 0, "Configuration/validation error - fix required");
                
            case SECURITY_ERROR:
                return new RetryStrategy(false, 0, 0, "Security error - manual intervention required");
                
            case ADAPTER_ERROR:
                return new RetryStrategy(true, 1, 30000, "Adapter error - single retry");
                
            default:
                return new RetryStrategy(false, 0, 0, "Unknown error - no retry");
        }
    }
    
    private void updateErrorStatistics(String adapterKey, ErrorClassification classification, Exception exception) {
        ErrorStatistics stats = errorStats.computeIfAbsent(adapterKey, k -> new ErrorStatistics());
        stats.recordError(classification, exception);
    }
    
    private CircuitBreakerState getOrCreateCircuitBreaker(String adapterKey) {
        return circuitBreakers.computeIfAbsent(adapterKey, k -> new CircuitBreakerState());
    }
    
    private boolean updateCircuitBreaker(CircuitBreakerState state, ErrorClassification classification) {
        // Only certain error types contribute to circuit breaker
        boolean shouldTrip = classification == ErrorClassification.CONNECTION_ERROR ||
                            classification == ErrorClassification.TIMEOUT_ERROR ||
                            classification == ErrorClassification.DATABASE_ERROR;
        
        if (shouldTrip) {
            state.recordFailure();
        } else {
            state.recordSuccess();
        }
        
        return state.isOpen();
    }
    
    private void notifyErrorListeners(String adapterKey, AdapterErrorEvent errorEvent) {
        for (Map.Entry<String, Consumer<AdapterErrorEvent>> entry : errorListeners.entrySet()) {
            try {
                entry.getValue().accept(errorEvent);
            } catch (Exception e) {
                logger.warn("Error listener {} failed to process error event", entry.getKey(), e);
            }
        }
    }
    
    private void logError(String adapterKey, Exception exception, 
                         ErrorClassification classification, RetryStrategy retryStrategy) {
        
        String logMessage = String.format("Adapter error [%s]: %s - Classification: %s, Retry: %s", 
                                         adapterKey, exception.getMessage(), classification, 
                                         retryStrategy.shouldRetry() ? "Yes" : "No");
        
        switch (classification) {
            case CONFIGURATION_ERROR:
            case VALIDATION_ERROR:
                logger.error(logMessage, exception);
                break;
            case CONNECTION_ERROR:
            case TIMEOUT_ERROR:
            case DATABASE_ERROR:
                logger.warn(logMessage, exception);
                break;
            case AUTHENTICATION_ERROR:
            case SECURITY_ERROR:
                logger.error("SECURITY ALERT - " + logMessage, exception);
                break;
            default:
                logger.info(logMessage, exception);
        }
    }
    
    private void addRecoveryRecommendations(ErrorHandlingResult result, Exception exception, 
                                          Map<String, Object> context) {
        
        switch (result.getClassification()) {
            case CONNECTION_ERROR:
                result.addRecommendation("Check network connectivity and endpoint availability");
                result.addRecommendation("Verify firewall and proxy settings");
                result.addRecommendation("Confirm target service is running");
                break;
                
            case AUTHENTICATION_ERROR:
                result.addRecommendation("Verify credentials are correct and not expired");
                result.addRecommendation("Check authentication configuration");
                result.addRecommendation("Confirm account has required permissions");
                break;
                
            case CONFIGURATION_ERROR:
                result.addRecommendation("Review adapter configuration parameters");
                result.addRecommendation("Check for missing or invalid configuration values");
                result.addRecommendation("Validate configuration against adapter requirements");
                break;
                
            case TIMEOUT_ERROR:
                result.addRecommendation("Increase timeout values if appropriate");
                result.addRecommendation("Check for network latency issues");
                result.addRecommendation("Verify target system performance");
                break;
                
            case DATABASE_ERROR:
                result.addRecommendation("Check database connectivity and health");
                result.addRecommendation("Verify SQL syntax and table/column names");
                result.addRecommendation("Confirm database user permissions");
                break;
                
            case VALIDATION_ERROR:
                result.addRecommendation("Verify input data format and structure");
                result.addRecommendation("Check data constraints and validation rules");
                result.addRecommendation("Review data transformation logic");
                break;
                
            case SECURITY_ERROR:
                result.addRecommendation("Review security certificates and configurations");
                result.addRecommendation("Check SSL/TLS settings");
                result.addRecommendation("Verify security policies and permissions");
                break;
                
            default:
                result.addRecommendation("Review error details and logs");
                result.addRecommendation("Check adapter and system health");
                result.addRecommendation("Contact support if issue persists");
        }
    }
    
    private String createAdapterKey(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        return String.format("%s-%s-%s", adapterType, adapterMode, adapterId);
    }
}

/**
 * Error classification enumeration.
 */
enum ErrorClassification {
    CONNECTION_ERROR,
    AUTHENTICATION_ERROR,
    CONFIGURATION_ERROR,
    VALIDATION_ERROR,
    TIMEOUT_ERROR,
    DATABASE_ERROR,
    IO_ERROR,
    SECURITY_ERROR,
    ADAPTER_ERROR,
    UNKNOWN_ERROR
}

/**
 * Retry strategy configuration.
 */
class RetryStrategy {
    private final boolean shouldRetry;
    private final int maxRetries;
    private final long retryDelayMs;
    private final String reason;
    
    public RetryStrategy(boolean shouldRetry, int maxRetries, long retryDelayMs, String reason) {
        this.shouldRetry = shouldRetry;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.reason = reason;
    }
    
    public boolean shouldRetry() { return shouldRetry; }
    public int getMaxRetries() { return maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public String getReason() { return reason; }
}

/**
 * Circuit breaker state management.
 */
class CircuitBreakerState {
    private static final int FAILURE_THRESHOLD = 5;
    private static final long TIMEOUT_MS = 60000; // 1 minute
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;
    private volatile boolean open = false;
    
    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        
        if (failures >= FAILURE_THRESHOLD && !open) {
            open = true;
            LoggerFactory.getLogger(CircuitBreakerState.class)
                    .warn("Circuit breaker opened after {} failures", failures);
        }
    }
    
    public void recordSuccess() {
        if (open && (System.currentTimeMillis() - lastFailureTime) > TIMEOUT_MS) {
            reset();
        } else {
            failureCount.set(0);
        }
    }
    
    public boolean isOpen() {
        if (open && (System.currentTimeMillis() - lastFailureTime) > TIMEOUT_MS) {
            // Half-open state - allow one attempt
            return false;
        }
        return open;
    }
    
    public void reset() {
        failureCount.set(0);
        open = false;
        lastFailureTime = 0;
        LoggerFactory.getLogger(CircuitBreakerState.class)
                .info("Circuit breaker reset");
    }
}

/**
 * Error statistics tracking.
 */
class ErrorStatistics {
    private final Map<ErrorClassification, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private volatile long firstErrorTime = 0;
    private volatile long lastErrorTime = 0;
    private volatile Exception lastException;
    
    public void recordError(ErrorClassification classification, Exception exception) {
        errorCounts.computeIfAbsent(classification, k -> new AtomicInteger(0)).incrementAndGet();
        totalErrors.incrementAndGet();
        
        long currentTime = System.currentTimeMillis();
        if (firstErrorTime == 0) {
            firstErrorTime = currentTime;
        }
        lastErrorTime = currentTime;
        lastException = exception;
    }
    
    public int getErrorCount(ErrorClassification classification) {
        AtomicInteger count = errorCounts.get(classification);
        return count != null ? count.get() : 0;
    }
    
    public int getTotalErrors() { return totalErrors.get(); }
    public long getFirstErrorTime() { return firstErrorTime; }
    public long getLastErrorTime() { return lastErrorTime; }
    public Exception getLastException() { return lastException; }
    public Map<ErrorClassification, AtomicInteger> getAllErrorCounts() { return new HashMap<>(errorCounts); }
}

/**
 * Error handling result.
 */
class ErrorHandlingResult {
    private final ErrorClassification classification;
    private final RetryStrategy retryStrategy;
    private final boolean circuitOpen;
    private final AdapterErrorEvent errorEvent;
    private final java.util.List<String> recommendations = new java.util.ArrayList<>();
    
    public ErrorHandlingResult(ErrorClassification classification, RetryStrategy retryStrategy, 
                              boolean circuitOpen, AdapterErrorEvent errorEvent) {
        this.classification = classification;
        this.retryStrategy = retryStrategy;
        this.circuitOpen = circuitOpen;
        this.errorEvent = errorEvent;
    }
    
    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }
    
    public ErrorClassification getClassification() { return classification; }
    public RetryStrategy getRetryStrategy() { return retryStrategy; }
    public boolean isCircuitOpen() { return circuitOpen; }
    public AdapterErrorEvent getErrorEvent() { return errorEvent; }
    public java.util.List<String> getRecommendations() { return new java.util.ArrayList<>(recommendations); }
}

/**
 * Error event for monitoring and alerting.
 */
class AdapterErrorEvent {
    private final AdapterType adapterType;
    private final AdapterMode adapterMode;
    private final String adapterId;
    private final Exception exception;
    private final ErrorClassification classification;
    private final LocalDateTime timestamp;
    private final Map<String, Object> context;
    
    public AdapterErrorEvent(AdapterType adapterType, AdapterMode adapterMode, String adapterId,
                            Exception exception, ErrorClassification classification, 
                            LocalDateTime timestamp, Map<String, Object> context) {
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.adapterId = adapterId;
        this.exception = exception;
        this.classification = classification;
        this.timestamp = timestamp;
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }
    
    public AdapterType getAdapterType() { return adapterType; }
    public AdapterMode getAdapterMode() { return adapterMode; }
    public String getAdapterId() { return adapterId; }
    public Exception getException() { return exception; }
    public ErrorClassification getClassification() { return classification; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getContext() { return new HashMap<>(context); }
    
    @Override
    public String toString() {
        return String.format("AdapterErrorEvent{adapter=%s-%s-%s, classification=%s, time=%s, error=%s}",
                adapterType, adapterMode, adapterId, classification, 
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), exception.getMessage());
    }
}