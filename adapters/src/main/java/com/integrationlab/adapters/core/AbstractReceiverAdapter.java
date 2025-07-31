package com.integrationlab.adapters.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation for receiver adapters.
 */
public abstract class AbstractReceiverAdapter extends AbstractAdapter implements ReceiverAdapter {
    
    private ScheduledExecutorService pollingExecutor;
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private final AtomicReference<ScheduledFuture<?>> pollingTask = new AtomicReference<>();
    private final AtomicReference<AdapterCallback> currentCallback = new AtomicReference<>();
    
    protected AbstractReceiverAdapter(AdapterType adapterType) {
        super(adapterType);
    }
    
    @Override
    protected void doInitialize() throws Exception {
        this.pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, getAdapterType() + "-receiver-polling");
            t.setDaemon(true);
            return t;
        });
        doReceiverInitialize();
    }
    
    @Override
    protected void doDestroy() throws Exception {
        // Stop polling if active
        if (polling.get()) {
            try {
                stopPolling();
            } catch (Exception e) {
                logger.warn("Error stopping polling during destroy", e);
            }
        }
        
        // Shutdown executor
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdown();
            try {
                if (!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    pollingExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                pollingExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        doReceiverDestroy();
    }
    
    @Override
    public AdapterResult receive() throws AdapterException {
        return receive(null);
    }
    
    @Override
    public AdapterResult receive(Object criteria) throws AdapterException {
        validateReady();
        return executeTimedOperation("receive", () -> doReceive(criteria));
    }
    
    @Override
    public void startPolling(AdapterCallback callback) throws AdapterException {
        validateReady();
        
        if (callback == null) {
            throw new AdapterException.ValidationException(getAdapterType(), "Callback cannot be null");
        }
        
        if (polling.get()) {
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Polling already active");
        }
        
        long pollingIntervalMs = getPollingIntervalMs();
        if (pollingIntervalMs <= 0) {
            throw new AdapterException.ConfigurationException(getAdapterType(), "Invalid polling interval: " + pollingIntervalMs);
        }
        
        currentCallback.set(callback);
        
        ScheduledFuture<?> task = pollingExecutor.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Polling for data with {} adapter", getAdapterType());
                AdapterResult result = doReceive(null);
                
                if (result.isSuccess() && result.getData() != null) {
                    callback.onSuccess(result);
                } else if (result.isFailure()) {
                    logger.warn("Polling failed for {} adapter: {}", getAdapterType(), result.getMessage());
                    callback.onFailure(result);
                }
                // If result is success but no data, continue polling silently
                
            } catch (Exception e) {
                logger.error("Polling error for {} adapter", getAdapterType(), e);
                AdapterResult errorResult = AdapterResult.failure("Polling error: " + e.getMessage(), e);
                callback.onFailure(errorResult);
            }
        }, 0, pollingIntervalMs, TimeUnit.MILLISECONDS);
        
        pollingTask.set(task);
        polling.set(true);
        
        logger.info("Started polling for {} adapter with interval {}ms", getAdapterType(), pollingIntervalMs);
    }
    
    @Override
    public void stopPolling() throws AdapterException {
        if (!polling.get()) {
            logger.debug("Polling not active for {} adapter", getAdapterType());
            return;
        }
        
        ScheduledFuture<?> task = pollingTask.getAndSet(null);
        if (task != null) {
            task.cancel(false);
        }
        
        currentCallback.set(null);
        polling.set(false);
        
        logger.info("Stopped polling for {} adapter", getAdapterType());
    }
    
    @Override
    public boolean isPolling() {
        return polling.get();
    }
    
    @Override
    public AdapterResult receiveBatch(int maxItems) throws AdapterException {
        validateReady();
        
        if (maxItems <= 0) {
            throw new AdapterException.ValidationException(getAdapterType(), "maxItems must be positive");
        }
        
        return executeTimedOperation("receiveBatch", () -> doReceiveBatch(maxItems));
    }
    
    @Override
    public void acknowledge(String messageId) throws AdapterException {
        validateReady();
        
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new AdapterException.ValidationException(getAdapterType(), "messageId cannot be null or empty");
        }
        
        try {
            doAcknowledge(messageId);
            updateLastActivity();
        } catch (Exception e) {
            logger.error("Acknowledgment failed for {} adapter", getAdapterType(), e);
            throw new AdapterException(getAdapterType(), getAdapterMode(), "Acknowledgment failed", e);
        }
    }
    
    /**
     * Get the polling interval in milliseconds from adapter configuration.
     * Subclasses should override this to provide the configured interval.
     */
    protected abstract long getPollingIntervalMs();
    
    /**
     * Default batch implementation that calls receive multiple times.
     * Subclasses can override for more efficient batch processing.
     */
    protected AdapterResult doReceiveBatch(int maxItems) throws Exception {
        java.util.List<Object> receivedItems = new java.util.ArrayList<>();
        int attempts = 0;
        
        while (receivedItems.size() < maxItems && attempts < maxItems * 2) {
            attempts++;
            AdapterResult result = doReceive(null);
            
            if (result.isSuccess() && result.getData() != null) {
                receivedItems.add(result.getData());
            } else if (result.isFailure()) {
                // If we have some items, return partial success
                if (!receivedItems.isEmpty()) {
                    break;
                } else {
                    return result; // Return the failure if no items received
                }
            }
            // If no data but no error, continue trying
        }
        
        AdapterResult result = AdapterResult.success(receivedItems, 
                String.format("Received %d items", receivedItems.size()));
        result.addMetadata("itemCount", receivedItems.size());
        result.addMetadata("maxRequested", maxItems);
        
        return result;
    }
    
    /**
     * Default acknowledgment implementation (no-op).
     * Subclasses should override if acknowledgment is supported.
     */
    protected void doAcknowledge(String messageId) throws Exception {
        logger.debug("Acknowledgment not implemented for {} adapter", getAdapterType());
    }
    
    // Abstract methods for subclasses to implement
    
    /**
     * Perform receiver-specific initialization.
     */
    protected abstract void doReceiverInitialize() throws Exception;
    
    /**
     * Perform receiver-specific cleanup.
     */
    protected abstract void doReceiverDestroy() throws Exception;
    
    /**
     * Receive data from the external system.
     */
    protected abstract AdapterResult doReceive(Object criteria) throws Exception;
}