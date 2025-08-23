package com.integrixs.backend.service;

import com.integrixs.backend.websocket.FlowExecutionWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FlowExecutionMonitoringService {

    @Autowired
    private FlowExecutionWebSocketHandler webSocketHandler;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, FlowExecutionTrace> executionTraces = new ConcurrentHashMap<>();
    private final Map<String, List<FlowExecutionEvent>> executionHistory = new ConcurrentHashMap<>();
    private final Map<String, FlowPerformanceMetrics> flowMetrics = new ConcurrentHashMap<>();

    public FlowExecutionMonitoringService() {
        // Start periodic cleanup of old execution traces
        scheduler.scheduleAtFixedRate(this::cleanupOldTraces, 1, 1, TimeUnit.HOURS);
        
        // Start periodic metrics calculation
        scheduler.scheduleAtFixedRate(this::calculatePerformanceMetrics, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Start monitoring a flow execution
     */
    public String startMonitoring(String flowId, String executionId, String flowType) {
        FlowExecutionTrace trace = new FlowExecutionTrace();
        trace.setExecutionId(executionId);
        trace.setFlowId(flowId);
        trace.setFlowType(flowType);
        trace.setStatus(ExecutionStatus.STARTED);
        trace.setStartTime(LocalDateTime.now());
        trace.addEvent("EXECUTION_STARTED", "Flow execution monitoring started");
        
        executionTraces.put(executionId, trace);
        
        // Notify WebSocket clients
        webSocketHandler.broadcastFlowExecutionStarted(flowId, executionId);
        
        return executionId;
    }

    /**
     * Update execution progress
     */
    public void updateExecutionProgress(String executionId, String currentStep, String stepMessage) {
        FlowExecutionTrace trace = executionTraces.get(executionId);
        if (trace != null) {
            trace.setCurrentStep(currentStep);
            trace.setLastUpdate(LocalDateTime.now());
            trace.addEvent("STEP_PROGRESS", currentStep + ": " + stepMessage);
            
            // Notify WebSocket clients
            webSocketHandler.broadcastFlowExecutionProgress(
                trace.getFlowId(), 
                executionId, 
                currentStep, 
                stepMessage
            );
        }
    }

    /**
     * Record execution completion
     */
    public void completeExecution(String executionId, boolean success, String message) {
        FlowExecutionTrace trace = executionTraces.get(executionId);
        if (trace != null) {
            trace.setStatus(success ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED);
            trace.setEndTime(LocalDateTime.now());
            trace.setCompletionMessage(message);
            trace.addEvent("EXECUTION_COMPLETED", message);
            
            // Calculate execution duration
            if (trace.getStartTime() != null) {
                Duration duration = Duration.between(trace.getStartTime(), trace.getEndTime());
                trace.setExecutionDurationMs(duration.toMillis());
            }
            
            // Move to execution history
            moveToHistory(trace);
            
            // Update flow performance metrics
            updateFlowMetrics(trace.getFlowId(), trace);
            
            // Notify WebSocket clients
            webSocketHandler.broadcastFlowExecutionCompleted(trace.getFlowId(), executionId, success);
        }
    }

    /**
     * Record execution error
     */
    public void recordExecutionError(String executionId, String errorMessage, Throwable exception) {
        FlowExecutionTrace trace = executionTraces.get(executionId);
        if (trace != null) {
            trace.setStatus(ExecutionStatus.ERROR);
            trace.setEndTime(LocalDateTime.now());
            trace.setErrorMessage(errorMessage);
            if (exception != null) {
                trace.setExceptionDetails(getStackTrace(exception));
            }
            trace.addEvent("EXECUTION_ERROR", errorMessage);
            
            // Move to execution history
            moveToHistory(trace);
            
            // Update flow performance metrics
            updateFlowMetrics(trace.getFlowId(), trace);
            
            // Notify WebSocket clients
            webSocketHandler.broadcastFlowExecutionError(trace.getFlowId(), executionId, errorMessage);
        }
    }

    /**
     * Cancel execution monitoring
     */
    public boolean cancelExecution(String executionId) {
        FlowExecutionTrace trace = executionTraces.get(executionId);
        if (trace != null && trace.getStatus() == ExecutionStatus.RUNNING) {
            trace.setStatus(ExecutionStatus.CANCELLED);
            trace.setEndTime(LocalDateTime.now());
            trace.addEvent("EXECUTION_CANCELLED", "Execution cancelled by user");
            
            // Move to execution history
            moveToHistory(trace);
            
            return true;
        }
        return false;
    }

    /**
     * Get current execution status
     */
    public Optional<FlowExecutionTrace> getExecutionTrace(String executionId) {
        return Optional.ofNullable(executionTraces.get(executionId));
    }

    /**
     * Get execution history for a flow
     */
    public List<FlowExecutionEvent> getExecutionHistory(String flowId, int limit) {
        return executionHistory.getOrDefault(flowId, new ArrayList<>())
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get flow performance metrics
     */
    public Optional<FlowPerformanceMetrics> getFlowMetrics(String flowId) {
        return Optional.ofNullable(flowMetrics.get(flowId));
    }

    /**
     * Get all active executions
     */
    public List<FlowExecutionTrace> getActiveExecutions() {
        return new ArrayList<>(executionTraces.values());
    }

    /**
     * Get execution statistics
     */
    public ExecutionStatistics getExecutionStatistics() {
        ExecutionStatistics stats = new ExecutionStatistics();
        
        // Count active executions by status
        Map<ExecutionStatus, Long> statusCounts = executionTraces.values().stream()
                .collect(Collectors.groupingBy(FlowExecutionTrace::getStatus, Collectors.counting()));
        
        stats.setActiveExecutions(executionTraces.size());
        stats.setRunningExecutions(statusCounts.getOrDefault(ExecutionStatus.RUNNING, 0L).intValue());
        stats.setCompletedExecutions(statusCounts.getOrDefault(ExecutionStatus.COMPLETED, 0L).intValue());
        stats.setFailedExecutions(statusCounts.getOrDefault(ExecutionStatus.FAILED, 0L).intValue());
        
        // Calculate average execution time from completed executions
        OptionalDouble avgTime = executionTraces.values().stream()
                .filter(trace -> trace.getExecutionDurationMs() > 0)
                .mapToLong(FlowExecutionTrace::getExecutionDurationMs)
                .average();
        stats.setAverageExecutionTimeMs(avgTime.orElse(0.0));
        
        // Count total flows being monitored
        long uniqueFlows = executionTraces.values().stream()
                .map(FlowExecutionTrace::getFlowId)
                .distinct()
                .count();
        stats.setUniqueFlowsMonitored((int) uniqueFlows);
        
        return stats;
    }

    /**
     * Search executions by criteria
     */
    public List<FlowExecutionTrace> searchExecutions(ExecutionSearchCriteria criteria) {
        return executionTraces.values().stream()
                .filter(trace -> matchesCriteria(trace, criteria))
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(criteria.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * Get execution alerts (long-running, failed, etc.)
     */
    public List<ExecutionAlert> getExecutionAlerts() {
        List<ExecutionAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (FlowExecutionTrace trace : executionTraces.values()) {
            // Check for long-running executions (>30 minutes)
            if (trace.getStatus() == ExecutionStatus.RUNNING && 
                trace.getStartTime() != null &&
                Duration.between(trace.getStartTime(), now).toMinutes() > 30) {
                
                ExecutionAlert alert = new ExecutionAlert();
                alert.setType(AlertType.LONG_RUNNING);
                alert.setExecutionId(trace.getExecutionId());
                alert.setFlowId(trace.getFlowId());
                alert.setMessage("Execution has been running for over 30 minutes");
                alert.setTimestamp(now);
                alerts.add(alert);
            }
            
            // Check for failed executions
            if (trace.getStatus() == ExecutionStatus.FAILED || trace.getStatus() == ExecutionStatus.ERROR) {
                ExecutionAlert alert = new ExecutionAlert();
                alert.setType(AlertType.EXECUTION_FAILED);
                alert.setExecutionId(trace.getExecutionId());
                alert.setFlowId(trace.getFlowId());
                alert.setMessage("Execution failed: " + trace.getErrorMessage());
                alert.setTimestamp(trace.getEndTime());
                alerts.add(alert);
            }
        }
        
        return alerts;
    }

    private void moveToHistory(FlowExecutionTrace trace) {
        // Convert trace to history events
        FlowExecutionEvent completionEvent = new FlowExecutionEvent();
        completionEvent.setExecutionId(trace.getExecutionId());
        completionEvent.setFlowId(trace.getFlowId());
        completionEvent.setEventType("EXECUTION_COMPLETED");
        completionEvent.setStatus(trace.getStatus());
        completionEvent.setMessage(trace.getCompletionMessage());
        completionEvent.setTimestamp(trace.getEndTime());
        completionEvent.setExecutionDurationMs(trace.getExecutionDurationMs());
        
        executionHistory.computeIfAbsent(trace.getFlowId(), k -> new ArrayList<>()).add(completionEvent);
        
        // Remove from active traces
        executionTraces.remove(trace.getExecutionId());
    }

    private void updateFlowMetrics(String flowId, FlowExecutionTrace trace) {
        FlowPerformanceMetrics metrics = flowMetrics.computeIfAbsent(flowId, k -> new FlowPerformanceMetrics());
        metrics.setFlowId(flowId);
        metrics.setLastUpdate(LocalDateTime.now());
        
        // Update execution counts
        metrics.setTotalExecutions(metrics.getTotalExecutions() + 1);
        
        if (trace.getStatus() == ExecutionStatus.COMPLETED) {
            metrics.setSuccessfulExecutions(metrics.getSuccessfulExecutions() + 1);
        } else {
            metrics.setFailedExecutions(metrics.getFailedExecutions() + 1);
        }
        
        // Update timing metrics
        if (trace.getExecutionDurationMs() > 0) {
            if (metrics.getMinExecutionTimeMs() == 0 || trace.getExecutionDurationMs() < metrics.getMinExecutionTimeMs()) {
                metrics.setMinExecutionTimeMs(trace.getExecutionDurationMs());
            }
            if (trace.getExecutionDurationMs() > metrics.getMaxExecutionTimeMs()) {
                metrics.setMaxExecutionTimeMs(trace.getExecutionDurationMs());
            }
            
            // Calculate average execution time
            double totalTime = metrics.getAverageExecutionTimeMs() * (metrics.getTotalExecutions() - 1) + trace.getExecutionDurationMs();
            metrics.setAverageExecutionTimeMs(totalTime / metrics.getTotalExecutions());
        }
    }

    private boolean matchesCriteria(FlowExecutionTrace trace, ExecutionSearchCriteria criteria) {
        if (criteria.getFlowId() != null && !criteria.getFlowId().equals(trace.getFlowId())) {
            return false;
        }
        if (criteria.getStatus() != null && !criteria.getStatus().equals(trace.getStatus())) {
            return false;
        }
        if (criteria.getStartTimeAfter() != null && trace.getStartTime().isBefore(criteria.getStartTimeAfter())) {
            return false;
        }
        if (criteria.getStartTimeBefore() != null && trace.getStartTime().isAfter(criteria.getStartTimeBefore())) {
            return false;
        }
        return true;
    }

    private void cleanupOldTraces() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        executionTraces.entrySet().removeIf(entry -> 
            entry.getValue().getStartTime().isBefore(cutoff)
        );
        
        // Also cleanup old history entries (keep last 1000 per flow)
        executionHistory.values().forEach(events -> {
            if (events.size() > 1000) {
                events.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                events.subList(1000, events.size()).clear();
            }
        });
    }

    private void calculatePerformanceMetrics() {
        // This could include more sophisticated performance calculations
        // For now, the metrics are updated in updateFlowMetrics method
    }

    private String getStackTrace(Throwable exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    // Data classes

    public enum ExecutionStatus {
        STARTED, RUNNING, COMPLETED, FAILED, ERROR, CANCELLED
    }

    public enum AlertType {
        LONG_RUNNING, EXECUTION_FAILED, HIGH_ERROR_RATE, PERFORMANCE_DEGRADATION
    }

    public static class FlowExecutionTrace {
        private String executionId;
        private String flowId;
        private String flowType;
        private ExecutionStatus status;
        private String currentStep;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime lastUpdate;
        private long executionDurationMs;
        private String completionMessage;
        private String errorMessage;
        private String exceptionDetails;
        private List<TraceEvent> events = new ArrayList<>();

        public void addEvent(String eventType, String message) {
            TraceEvent event = new TraceEvent();
            event.setEventType(eventType);
            event.setMessage(message);
            event.setTimestamp(LocalDateTime.now());
            events.add(event);
        }

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String flowType) { this.flowType = flowType; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
        public long getExecutionDurationMs() { return executionDurationMs; }
        public void setExecutionDurationMs(long executionDurationMs) { this.executionDurationMs = executionDurationMs; }
        public String getCompletionMessage() { return completionMessage; }
        public void setCompletionMessage(String completionMessage) { this.completionMessage = completionMessage; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getExceptionDetails() { return exceptionDetails; }
        public void setExceptionDetails(String exceptionDetails) { this.exceptionDetails = exceptionDetails; }
        public List<TraceEvent> getEvents() { return events; }
        public void setEvents(List<TraceEvent> events) { this.events = events; }
    }

    public static class TraceEvent {
        private String eventType;
        private String message;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class FlowExecutionEvent {
        private String executionId;
        private String flowId;
        private String eventType;
        private ExecutionStatus status;
        private String message;
        private LocalDateTime timestamp;
        private long executionDurationMs;

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public long getExecutionDurationMs() { return executionDurationMs; }
        public void setExecutionDurationMs(long executionDurationMs) { this.executionDurationMs = executionDurationMs; }
    }

    public static class FlowPerformanceMetrics {
        private String flowId;
        private int totalExecutions;
        private int successfulExecutions;
        private int failedExecutions;
        private double averageExecutionTimeMs;
        private long minExecutionTimeMs;
        private long maxExecutionTimeMs;
        private LocalDateTime lastUpdate;

        // Getters and setters
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public int getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }
        public int getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(int successfulExecutions) { this.successfulExecutions = successfulExecutions; }
        public int getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(int failedExecutions) { this.failedExecutions = failedExecutions; }
        public double getAverageExecutionTimeMs() { return averageExecutionTimeMs; }
        public void setAverageExecutionTimeMs(double averageExecutionTimeMs) { this.averageExecutionTimeMs = averageExecutionTimeMs; }
        public long getMinExecutionTimeMs() { return minExecutionTimeMs; }
        public void setMinExecutionTimeMs(long minExecutionTimeMs) { this.minExecutionTimeMs = minExecutionTimeMs; }
        public long getMaxExecutionTimeMs() { return maxExecutionTimeMs; }
        public void setMaxExecutionTimeMs(long maxExecutionTimeMs) { this.maxExecutionTimeMs = maxExecutionTimeMs; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }

    public static class ExecutionStatistics {
        private int activeExecutions;
        private int runningExecutions;
        private int completedExecutions;
        private int failedExecutions;
        private double averageExecutionTimeMs;
        private int uniqueFlowsMonitored;

        // Getters and setters
        public int getActiveExecutions() { return activeExecutions; }
        public void setActiveExecutions(int activeExecutions) { this.activeExecutions = activeExecutions; }
        public int getRunningExecutions() { return runningExecutions; }
        public void setRunningExecutions(int runningExecutions) { this.runningExecutions = runningExecutions; }
        public int getCompletedExecutions() { return completedExecutions; }
        public void setCompletedExecutions(int completedExecutions) { this.completedExecutions = completedExecutions; }
        public int getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(int failedExecutions) { this.failedExecutions = failedExecutions; }
        public double getAverageExecutionTimeMs() { return averageExecutionTimeMs; }
        public void setAverageExecutionTimeMs(double averageExecutionTimeMs) { this.averageExecutionTimeMs = averageExecutionTimeMs; }
        public int getUniqueFlowsMonitored() { return uniqueFlowsMonitored; }
        public void setUniqueFlowsMonitored(int uniqueFlowsMonitored) { this.uniqueFlowsMonitored = uniqueFlowsMonitored; }
    }

    public static class ExecutionSearchCriteria {
        private String flowId;
        private ExecutionStatus status;
        private LocalDateTime startTimeAfter;
        private LocalDateTime startTimeBefore;
        private int limit = 100;

        // Getters and setters
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public LocalDateTime getStartTimeAfter() { return startTimeAfter; }
        public void setStartTimeAfter(LocalDateTime startTimeAfter) { this.startTimeAfter = startTimeAfter; }
        public LocalDateTime getStartTimeBefore() { return startTimeBefore; }
        public void setStartTimeBefore(LocalDateTime startTimeBefore) { this.startTimeBefore = startTimeBefore; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
    }

    public static class ExecutionAlert {
        private AlertType type;
        private String executionId;
        private String flowId;
        private String message;
        private LocalDateTime timestamp;

        // Getters and setters
        public AlertType getType() { return type; }
        public void setType(AlertType type) { this.type = type; }
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}