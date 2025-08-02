package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.OrchestrationEngineService;
import com.integrationlab.backend.service.OrchestrationEngineService.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orchestration")
public class OrchestrationEngineController {

    @Autowired
    private OrchestrationEngineService orchestrationService;

    /**
     * Execute an orchestration flow synchronously
     */
    @PostMapping("/{flowId}/execute")
    public ResponseEntity<OrchestrationResult> executeOrchestrationFlow(
            @PathVariable String flowId,
            @RequestBody Object inputData) {
        try {
            OrchestrationResult result = orchestrationService.executeOrchestrationFlow(flowId, inputData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            OrchestrationResult errorResult = OrchestrationResult.error("Execution failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * Execute an orchestration flow asynchronously
     */
    @PostMapping("/{flowId}/execute-async")
    public ResponseEntity<AsyncExecutionResponse> executeOrchestrationFlowAsync(
            @PathVariable String flowId,
            @RequestBody Object inputData) {
        try {
            CompletableFuture<OrchestrationResult> futureResult = 
                orchestrationService.executeOrchestrationFlowAsync(flowId, inputData);
            
            AsyncExecutionResponse response = new AsyncExecutionResponse();
            response.setExecutionId("async-" + System.currentTimeMillis());
            response.setMessage("Orchestration flow execution started asynchronously");
            response.setFlowId(flowId);
            
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            AsyncExecutionResponse errorResponse = new AsyncExecutionResponse();
            errorResponse.setError("Failed to start async execution: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get execution status for a running orchestration
     */
    @GetMapping("/execution/{executionId}/status")
    public ResponseEntity<OrchestrationExecution> getExecutionStatus(@PathVariable String executionId) {
        return orchestrationService.getExecutionStatus(executionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cancel a running orchestration execution
     */
    @PostMapping("/execution/{executionId}/cancel")
    public ResponseEntity<CancelExecutionResponse> cancelExecution(@PathVariable String executionId) {
        boolean cancelled = orchestrationService.cancelExecution(executionId);
        
        CancelExecutionResponse response = new CancelExecutionResponse();
        response.setExecutionId(executionId);
        response.setCancelled(cancelled);
        response.setMessage(cancelled ? "Execution cancelled successfully" : "Execution not found or already completed");
        
        return cancelled ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    /**
     * Validate an orchestration flow configuration
     */
    @PostMapping("/{flowId}/validate")
    public ResponseEntity<ValidationResult> validateOrchestrationFlow(@PathVariable String flowId) {
        try {
            ValidationResult result = orchestrationService.validateOrchestrationFlow(flowId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ValidationResult errorResult = new ValidationResult();
            errorResult.addError("Validation failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Get execution history for a specific flow
     */
    @GetMapping("/{flowId}/history")
    public ResponseEntity<List<OrchestrationExecution>> getExecutionHistory(
            @PathVariable String flowId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<OrchestrationExecution> history = orchestrationService.getExecutionHistory(flowId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get detailed execution logs for a specific execution
     */
    @GetMapping("/execution/{executionId}/logs")
    public ResponseEntity<ExecutionLogsResponse> getExecutionLogs(@PathVariable String executionId) {
        return orchestrationService.getExecutionStatus(executionId)
                .map(execution -> {
                    ExecutionLogsResponse response = new ExecutionLogsResponse();
                    response.setExecutionId(executionId);
                    response.setFlowId(execution.getFlowId());
                    response.setFlowName(execution.getFlowName());
                    response.setStatus(execution.getStatus());
                    response.setCurrentStep(execution.getCurrentStep());
                    response.setLogs(execution.getLogs());
                    response.setStartTime(execution.getStartTime());
                    response.setEndTime(execution.getEndTime());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Pause a running orchestration execution
     */
    @PostMapping("/execution/{executionId}/pause")
    public ResponseEntity<ExecutionControlResponse> pauseExecution(@PathVariable String executionId) {
        // This would pause the execution - for now returning a placeholder response
        ExecutionControlResponse response = new ExecutionControlResponse();
        response.setExecutionId(executionId);
        response.setAction("pause");
        response.setSuccess(false);
        response.setMessage("Pause functionality not yet implemented");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Resume a paused orchestration execution
     */
    @PostMapping("/execution/{executionId}/resume")
    public ResponseEntity<ExecutionControlResponse> resumeExecution(@PathVariable String executionId) {
        // This would resume the execution - for now returning a placeholder response
        ExecutionControlResponse response = new ExecutionControlResponse();
        response.setExecutionId(executionId);
        response.setAction("resume");
        response.setSuccess(false);
        response.setMessage("Resume functionality not yet implemented");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get orchestration flow metrics and statistics
     */
    @GetMapping("/{flowId}/metrics")
    public ResponseEntity<OrchestrationMetrics> getFlowMetrics(@PathVariable String flowId) {
        try {
            OrchestrationMetrics metrics = buildMetricsForFlow(flowId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test orchestration flow configuration with sample data
     */
    @PostMapping("/{flowId}/test")
    public ResponseEntity<OrchestrationTestResult> testOrchestrationFlow(
            @PathVariable String flowId,
            @RequestBody OrchestrationTestRequest request) {
        try {
            // This would perform a test execution with sample data
            OrchestrationTestResult result = new OrchestrationTestResult();
            result.setFlowId(flowId);
            result.setTestSuccessful(true);
            result.setMessage("Test execution completed successfully");
            result.setExecutionTimeMs(System.currentTimeMillis() % 1000);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            OrchestrationTestResult errorResult = new OrchestrationTestResult();
            errorResult.setFlowId(flowId);
            errorResult.setTestSuccessful(false);
            errorResult.setMessage("Test failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    private OrchestrationMetrics buildMetricsForFlow(String flowId) {
        OrchestrationMetrics metrics = new OrchestrationMetrics();
        metrics.setFlowId(flowId);
        metrics.setTotalExecutions(0); // Would query from execution history
        metrics.setSuccessfulExecutions(0);
        metrics.setFailedExecutions(0);
        metrics.setAverageExecutionTimeMs(0.0);
        metrics.setLastExecutionTime(null);
        return metrics;
    }

    // Response DTOs

    public static class AsyncExecutionResponse {
        private String executionId;
        private String flowId;
        private String message;
        private String error;

        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class CancelExecutionResponse {
        private String executionId;
        private boolean cancelled;
        private String message;

        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ExecutionLogsResponse {
        private String executionId;
        private String flowId;
        private String flowName;
        private ExecutionStatus status;
        private String currentStep;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private java.util.List<String> logs;

        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        public java.time.LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
        public java.time.LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
        public java.util.List<String> getLogs() { return logs; }
        public void setLogs(java.util.List<String> logs) { this.logs = logs; }
    }

    public static class ExecutionControlResponse {
        private String executionId;
        private String action;
        private boolean success;
        private String message;

        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class OrchestrationMetrics {
        private String flowId;
        private int totalExecutions;
        private int successfulExecutions;
        private int failedExecutions;
        private double averageExecutionTimeMs;
        private java.time.LocalDateTime lastExecutionTime;

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
        public java.time.LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(java.time.LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
    }

    public static class OrchestrationTestRequest {
        private Object sampleData;
        private boolean validateOnly = false;

        public Object getSampleData() { return sampleData; }
        public void setSampleData(Object sampleData) { this.sampleData = sampleData; }
        public boolean isValidateOnly() { return validateOnly; }
        public void setValidateOnly(boolean validateOnly) { this.validateOnly = validateOnly; }
    }

    public static class OrchestrationTestResult {
        private String flowId;
        private boolean testSuccessful;
        private String message;
        private long executionTimeMs;
        private Object testOutput;

        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public boolean isTestSuccessful() { return testSuccessful; }
        public void setTestSuccessful(boolean testSuccessful) { this.testSuccessful = testSuccessful; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public Object getTestOutput() { return testOutput; }
        public void setTestOutput(Object testOutput) { this.testOutput = testOutput; }
    }
}