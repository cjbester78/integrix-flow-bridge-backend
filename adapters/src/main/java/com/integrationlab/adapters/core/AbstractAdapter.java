package com.integrationlab.adapters.core;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation providing common adapter functionality.
 */
public abstract class AbstractAdapter implements BaseAdapter {
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final AdapterType adapterType;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Instant lastActivityTime;
    private String adapterId;
    
    protected AbstractAdapter(AdapterType adapterType) {
        this.adapterType = adapterType;
        this.lastActivityTime = Instant.now();
        this.adapterId = generateAdapterId();
    }
    
    /**
     * Generate a unique adapter identifier.
     */
    private String generateAdapterId() {
        return adapterType + "-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString(this.hashCode());
    }
    
    /**
     * Get the unique adapter identifier.
     */
    protected String getAdapterId() {
        return adapterId;
    }
    
    @Override
    public AdapterType getAdapterType() {
        return adapterType;
    }
    
    @Override
    public boolean isActive() {
        return active.get();
    }
    
    @Override
    public void initialize() throws AdapterException {
        if (initialized.get()) {
            logger.warn("Adapter {} already initialized", getAdapterType());
            return;
        }
        
        try {
            logger.info("Initializing {} adapter in {} mode", getAdapterType(), getAdapterMode());
            doInitialize();
            initialized.set(true);
            active.set(true);
            updateLastActivity();
            logger.info("Successfully initialized {} adapter", getAdapterType());
        } catch (Exception e) {
            logger.error("Failed to initialize {} adapter", getAdapterType(), e);
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Initialization failed", e);
        }
    }
    
    @Override
    public void destroy() throws AdapterException {
        if (!initialized.get()) {
            logger.debug("Adapter {} not initialized, nothing to destroy", getAdapterType());
            return;
        }
        
        try {
            logger.info("Destroying {} adapter", getAdapterType());
            active.set(false);
            doDestroy();
            initialized.set(false);
            logger.info("Successfully destroyed {} adapter", getAdapterType());
        } catch (Exception e) {
            logger.error("Failed to destroy {} adapter", getAdapterType(), e);
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Destruction failed", e);
        }
    }
    
    @Override
    public AdapterResult testConnection() {
        if (!initialized.get()) {
            return AdapterResult.failure("Adapter not initialized");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Testing connection for {} adapter", getAdapterType());
            AdapterResult result = doTestConnection();
            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            updateLastActivity();
            
            if (result.isSuccess()) {
                logger.debug("Connection test successful for {} adapter in {}ms", getAdapterType(), duration);
            } else {
                logger.warn("Connection test failed for {} adapter: {}", getAdapterType(), result.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Connection test error for {} adapter", getAdapterType(), e);
            AdapterResult result = AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
            result.setDurationMs(duration);
            return result;
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("Adapter{type=%s, mode=%s, active=%s, initialized=%s, lastActivity=%s}",
                getAdapterType(), getAdapterMode(), isActive(), initialized.get(), lastActivityTime);
    }
    
    /**
     * Update the last activity timestamp.
     */
    protected void updateLastActivity() {
        this.lastActivityTime = Instant.now();
    }
    
    /**
     * Get the last activity timestamp.
     */
    protected Instant getLastActivityTime() {
        return lastActivityTime;
    }
    
    /**
     * Check if adapter is initialized.
     */
    protected boolean isInitialized() {
        return initialized.get();
    }
    
    /**
     * Validate that the adapter is ready for operation.
     * 
     * @throws AdapterException if adapter is not ready
     */
    protected void validateReady() throws AdapterException {
        if (!initialized.get()) {
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Adapter not initialized");
        }
        if (!active.get()) {
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Adapter not active");
        }
    }
    
    /**
     * Create a timed operation wrapper that tracks duration and updates activity.
     */
    protected AdapterResult executeTimedOperation(String operationName, TimedOperation operation) {
        String adapterId = getAdapterId();
        Map<String, Object> context = RetryExecutor.createRetryContext(operationName, null);
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Executing {} for {} adapter", operationName, getAdapterType());
            
            AdapterResult result = RetryExecutor.executeWithRetry(
                    getAdapterType(),
                    getAdapterMode(),
                    adapterId,
                    () -> {
                        try {
                            return operation.execute();
                        } catch (Exception e) {
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            }
                            throw new RuntimeException(e);
                        }
                    },
                    context
            );
            
            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            result.addMetadata("operation", operationName);
            updateLastActivity();
            
            if (result.isSuccess()) {
                logger.debug("{} completed successfully for {} adapter in {}ms", 
                        operationName, getAdapterType(), duration);
            } else {
                logger.warn("{} failed for {} adapter: {}", 
                        operationName, getAdapterType(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("{} error for {} adapter", operationName, getAdapterType(), e);
            AdapterResult result = AdapterResult.failure(operationName + " failed: " + e.getMessage(), e);
            result.setDurationMs(duration);
            result.addMetadata("operation", operationName);
            return result;
        }
    }
    
    /**
     * Functional interface for timed operations.
     */
    @FunctionalInterface
    protected interface TimedOperation {
        AdapterResult execute() throws Exception;
    }
    
    // Abstract methods that subclasses must implement
    
    /**
     * Perform adapter-specific initialization.
     */
    protected abstract void doInitialize() throws Exception;
    
    /**
     * Perform adapter-specific cleanup.
     */
    protected abstract void doDestroy() throws Exception;
    
    /**
     * Perform adapter-specific connection testing.
     */
    protected abstract AdapterResult doTestConnection() throws Exception;
}