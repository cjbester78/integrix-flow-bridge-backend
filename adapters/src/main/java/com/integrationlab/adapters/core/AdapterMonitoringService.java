package com.integrationlab.adapters.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Monitoring service for adapter health, performance, and error tracking.
 * Provides metrics collection, health checks, and alerting capabilities.
 */
public class AdapterMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterMonitoringService.class);
    
    // Singleton instance
    private static final AdapterMonitoringService INSTANCE = new AdapterMonitoringService();
    
    // Metrics storage
    private final Map<String, AdapterMetrics> adapterMetrics = new ConcurrentHashMap<>();
    private final Map<String, HealthStatus> healthStatuses = new ConcurrentHashMap<>();
    
    // Monitoring configuration
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private volatile boolean monitoringEnabled = true;
    private volatile int healthCheckIntervalMinutes = 5;
    private volatile int metricsRetentionHours = 24;
    
    // Alert handlers
    private final Map<String, Consumer<AlertEvent>> alertHandlers = new ConcurrentHashMap<>();
    
    private AdapterMonitoringService() {
        startHealthCheckScheduler();
        startMetricsCleanupScheduler();
        registerErrorListener();
    }
    
    public static AdapterMonitoringService getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register an adapter for monitoring.
     * 
     * @param adapter the adapter to monitor
     */
    public void registerAdapter(BaseAdapter adapter) {
        if (!monitoringEnabled) {
            return;
        }
        
        String adapterId = createAdapterKey(adapter.getAdapterType(), getAdapterMode(adapter), getAdapterId(adapter));
        
        AdapterMetrics metrics = new AdapterMetrics(adapter.getAdapterType(), getAdapterMode(adapter));
        adapterMetrics.put(adapterId, metrics);
        
        HealthStatus healthStatus = new HealthStatus(adapterId);
        healthStatuses.put(adapterId, healthStatus);
        
        logger.info("Registered adapter for monitoring: {}", adapterId);
    }
    
    /**
     * Unregister an adapter from monitoring.
     * 
     * @param adapter the adapter to unregister
     */
    public void unregisterAdapter(BaseAdapter adapter) {
        String adapterId = createAdapterKey(adapter.getAdapterType(), getAdapterMode(adapter), getAdapterId(adapter));
        
        adapterMetrics.remove(adapterId);
        healthStatuses.remove(adapterId);
        
        logger.info("Unregistered adapter from monitoring: {}", adapterId);
    }
    
    /**
     * Record a successful operation.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter ID
     * @param operationName the operation name
     * @param durationMs the operation duration in milliseconds
     */
    public void recordSuccess(AdapterType adapterType, AdapterMode adapterMode, String adapterId, 
                             String operationName, long durationMs) {
        if (!monitoringEnabled) {
            return;
        }
        
        String key = createAdapterKey(adapterType, adapterMode, adapterId);
        AdapterMetrics metrics = adapterMetrics.get(key);
        
        if (metrics != null) {
            metrics.recordSuccess(operationName, durationMs);
            updateHealthStatus(key, true, null);
        }
    }
    
    /**
     * Record a failed operation.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter ID
     * @param operationName the operation name
     * @param error the error that occurred
     * @param durationMs the operation duration in milliseconds
     */
    public void recordFailure(AdapterType adapterType, AdapterMode adapterMode, String adapterId,
                             String operationName, Exception error, long durationMs) {
        if (!monitoringEnabled) {
            return;
        }
        
        String key = createAdapterKey(adapterType, adapterMode, adapterId);
        AdapterMetrics metrics = adapterMetrics.get(key);
        
        if (metrics != null) {
            metrics.recordFailure(operationName, error, durationMs);
            updateHealthStatus(key, false, error.getMessage());
        }
    }
    
    /**
     * Get metrics for an adapter.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter ID
     * @return adapter metrics or null if not found
     */
    public AdapterMetrics getMetrics(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        String key = createAdapterKey(adapterType, adapterMode, adapterId);
        return adapterMetrics.get(key);
    }
    
    /**
     * Get health status for an adapter.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param adapterId the adapter ID
     * @return health status or null if not found
     */
    public HealthStatus getHealthStatus(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        String key = createAdapterKey(adapterType, adapterMode, adapterId);
        return healthStatuses.get(key);
    }
    
    /**
     * Get all registered adapter metrics.
     * 
     * @return map of adapter ID to metrics
     */
    public Map<String, AdapterMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(adapterMetrics);
    }
    
    /**
     * Get all health statuses.
     * 
     * @return map of adapter ID to health status
     */
    public Map<String, HealthStatus> getAllHealthStatuses() {
        return new ConcurrentHashMap<>(healthStatuses);
    }
    
    /**
     * Register an alert handler.
     * 
     * @param handlerId unique handler identifier
     * @param handler the alert event consumer
     */
    public void registerAlertHandler(String handlerId, Consumer<AlertEvent> handler) {
        alertHandlers.put(handlerId, handler);
        logger.info("Registered alert handler: {}", handlerId);
    }
    
    /**
     * Unregister an alert handler.
     * 
     * @param handlerId the handler identifier to remove
     */
    public void unregisterAlertHandler(String handlerId) {
        alertHandlers.remove(handlerId);
        logger.info("Unregistered alert handler: {}", handlerId);
    }
    
    /**
     * Enable or disable monitoring.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;
        logger.info("Monitoring {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Set the health check interval.
     * 
     * @param intervalMinutes interval in minutes
     */
    public void setHealthCheckInterval(int intervalMinutes) {
        this.healthCheckIntervalMinutes = intervalMinutes;
        logger.info("Health check interval set to {} minutes", intervalMinutes);
    }
    
    /**
     * Generate a monitoring report for all adapters.
     * 
     * @return formatted monitoring report
     */
    public String generateMonitoringReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Adapter Monitoring Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        // Health Status Summary
        report.append("Health Status Summary:\n");
        int healthyCount = 0;
        int unhealthyCount = 0;
        
        for (Map.Entry<String, HealthStatus> entry : healthStatuses.entrySet()) {
            HealthStatus status = entry.getValue();
            if (status.isHealthy()) {
                healthyCount++;
            } else {
                unhealthyCount++;
            }
        }
        
        report.append("  Healthy: ").append(healthyCount).append("\n");
        report.append("  Unhealthy: ").append(unhealthyCount).append("\n\n");
        
        // Detailed Metrics
        report.append("Detailed Metrics:\n");
        for (Map.Entry<String, AdapterMetrics> entry : adapterMetrics.entrySet()) {
            String adapterId = entry.getKey();
            AdapterMetrics metrics = entry.getValue();
            HealthStatus health = healthStatuses.get(adapterId);
            
            report.append("  ").append(adapterId).append(":\n");
            report.append("    Status: ").append(health != null && health.isHealthy() ? "HEALTHY" : "UNHEALTHY").append("\n");
            report.append("    Success Rate: ").append(String.format("%.2f%%", metrics.getSuccessRate())).append("\n");
            report.append("    Total Operations: ").append(metrics.getTotalOperations()).append("\n");
            report.append("    Avg Duration: ").append(String.format("%.2fms", metrics.getAverageDuration())).append("\n");
            report.append("    Last Activity: ").append(metrics.getLastActivityTime()).append("\n");
            
            if (health != null && !health.isHealthy()) {
                report.append("    Last Error: ").append(health.getLastErrorMessage()).append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    
    private void startHealthCheckScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performHealthChecks();
            } catch (Exception e) {
                logger.error("Error during health check", e);
            }
        }, healthCheckIntervalMinutes, healthCheckIntervalMinutes, TimeUnit.MINUTES);
    }
    
    private void startMetricsCleanupScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldMetrics();
            } catch (Exception e) {
                logger.error("Error during metrics cleanup", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }
    
    private void registerErrorListener() {
        AdapterExceptionHandler.getInstance().registerErrorListener("monitoring", errorEvent -> {
            recordFailure(
                    errorEvent.getAdapterType(),
                    errorEvent.getAdapterMode(),
                    errorEvent.getAdapterId(),
                    "error",
                    errorEvent.getException(),
                    0
            );
            
            // Check if we should trigger an alert
            checkAlertConditions(errorEvent);
        });
    }
    
    private void performHealthChecks() {
        for (Map.Entry<String, HealthStatus> entry : healthStatuses.entrySet()) {
            String adapterId = entry.getKey();
            HealthStatus status = entry.getValue();
            
            // Check if adapter has been inactive for too long
            long inactiveMinutes = status.getMinutesSinceLastActivity();
            if (inactiveMinutes > 30) { // 30 minutes threshold
                triggerAlert(AlertLevel.WARNING, adapterId, 
                           "Adapter inactive for " + inactiveMinutes + " minutes");
            }
            
            // Check error rate
            AdapterMetrics metrics = adapterMetrics.get(adapterId);
            if (metrics != null) {
                double errorRate = 100.0 - metrics.getSuccessRate();
                if (errorRate > 50) { // 50% error rate threshold
                    triggerAlert(AlertLevel.CRITICAL, adapterId, 
                               String.format("High error rate: %.2f%%", errorRate));
                } else if (errorRate > 20) { // 20% error rate threshold
                    triggerAlert(AlertLevel.WARNING, adapterId, 
                               String.format("Elevated error rate: %.2f%%", errorRate));
                }
            }
        }
    }
    
    private void cleanupOldMetrics() {
        long cutoffTime = System.currentTimeMillis() - (metricsRetentionHours * 60 * 60 * 1000);
        
        for (AdapterMetrics metrics : adapterMetrics.values()) {
            metrics.cleanupOldData(cutoffTime);
        }
    }
    
    private void updateHealthStatus(String adapterId, boolean healthy, String errorMessage) {
        HealthStatus status = healthStatuses.get(adapterId);
        if (status != null) {
            status.update(healthy, errorMessage);
        }
    }
    
    private void checkAlertConditions(AdapterErrorEvent errorEvent) {
        String adapterId = createAdapterKey(errorEvent.getAdapterType(), 
                                          errorEvent.getAdapterMode(), 
                                          errorEvent.getAdapterId());
        
        // Check for consecutive failures
        AdapterMetrics metrics = adapterMetrics.get(adapterId);
        if (metrics != null && metrics.getConsecutiveFailures() >= 5) {
            triggerAlert(AlertLevel.CRITICAL, adapterId, 
                       "5 consecutive failures detected");
        }
    }
    
    private void triggerAlert(AlertLevel level, String adapterId, String message) {
        AlertEvent alert = new AlertEvent(level, adapterId, message, LocalDateTime.now());
        
        logger.warn("ALERT [{}] {}: {}", level, adapterId, message);
        
        for (Consumer<AlertEvent> handler : alertHandlers.values()) {
            try {
                handler.accept(alert);
            } catch (Exception e) {
                logger.error("Alert handler failed to process alert", e);
            }
        }
    }
    
    private String createAdapterKey(AdapterType adapterType, AdapterMode adapterMode, String adapterId) {
        return String.format("%s-%s-%s", adapterType, adapterMode, adapterId);
    }
    
    // Helper methods to extract information from adapters
    private AdapterMode getAdapterMode(BaseAdapter adapter) {
        // This is a simplified implementation - in practice you'd have a proper way to determine mode
        return adapter instanceof SenderAdapter ? AdapterMode.SENDER : AdapterMode.RECEIVER;
    }
    
    private String getAdapterId(BaseAdapter adapter) {
        // This is a simplified implementation - in practice you'd have a proper adapter ID
        return adapter.getClass().getSimpleName() + "-" + adapter.hashCode();
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Alert levels for monitoring events.
 */
enum AlertLevel {
    INFO, WARNING, CRITICAL
}

/**
 * Alert event for monitoring notifications.
 */
class AlertEvent {
    private final AlertLevel level;
    private final String adapterId;
    private final String message;
    private final LocalDateTime timestamp;
    
    public AlertEvent(AlertLevel level, String adapterId, String message, LocalDateTime timestamp) {
        this.level = level;
        this.adapterId = adapterId;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public AlertLevel getLevel() { return level; }
    public String getAdapterId() { return adapterId; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Alert[%s] %s: %s at %s", level, adapterId, message, 
                           timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}

/**
 * Adapter metrics tracking.
 */
class AdapterMetrics {
    private final AdapterType adapterType;
    private final AdapterMode adapterMode;
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong successfulOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private volatile LocalDateTime lastActivityTime = LocalDateTime.now();
    private volatile Exception lastError;
    
    public AdapterMetrics(AdapterType adapterType, AdapterMode adapterMode) {
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
    }
    
    public void recordSuccess(String operationName, long durationMs) {
        totalOperations.incrementAndGet();
        successfulOperations.incrementAndGet();
        totalDuration.addAndGet(durationMs);
        consecutiveFailures.set(0);
        lastActivityTime = LocalDateTime.now();
    }
    
    public void recordFailure(String operationName, Exception error, long durationMs) {
        totalOperations.incrementAndGet();
        failedOperations.incrementAndGet();
        totalDuration.addAndGet(durationMs);
        consecutiveFailures.incrementAndGet();
        lastActivityTime = LocalDateTime.now();
        lastError = error;
    }
    
    public double getSuccessRate() {
        long total = totalOperations.get();
        return total > 0 ? (successfulOperations.get() * 100.0) / total : 0.0;
    }
    
    public double getAverageDuration() {
        long total = totalOperations.get();
        return total > 0 ? totalDuration.get() / (double) total : 0.0;
    }
    
    public long getTotalOperations() { return totalOperations.get(); }
    public long getSuccessfulOperations() { return successfulOperations.get(); }
    public long getFailedOperations() { return failedOperations.get(); }
    public long getConsecutiveFailures() { return consecutiveFailures.get(); }
    public LocalDateTime getLastActivityTime() { return lastActivityTime; }
    public Exception getLastError() { return lastError; }
    public AdapterType getAdapterType() { return adapterType; }
    public AdapterMode getAdapterMode() { return adapterMode; }
    
    public void cleanupOldData(long cutoffTime) {
        // In a real implementation, you'd maintain time-series data and clean up old entries
        // For now, this is a placeholder
    }
}

/**
 * Health status tracking.
 */
class HealthStatus {
    private final String adapterId;
    private volatile boolean healthy = true;
    private volatile LocalDateTime lastUpdateTime = LocalDateTime.now();
    private volatile String lastErrorMessage;
    private volatile LocalDateTime lastErrorTime;
    
    public HealthStatus(String adapterId) {
        this.adapterId = adapterId;
    }
    
    public void update(boolean healthy, String errorMessage) {
        this.healthy = healthy;
        this.lastUpdateTime = LocalDateTime.now();
        
        if (!healthy && errorMessage != null) {
            this.lastErrorMessage = errorMessage;
            this.lastErrorTime = LocalDateTime.now();
        }
    }
    
    public boolean isHealthy() { return healthy; }
    public String getAdapterId() { return adapterId; }
    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public LocalDateTime getLastErrorTime() { return lastErrorTime; }
    
    public long getMinutesSinceLastActivity() {
        return java.time.Duration.between(lastUpdateTime, LocalDateTime.now()).toMinutes();
    }
}