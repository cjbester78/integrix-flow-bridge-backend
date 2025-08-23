package com.integrixs.backend.service;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("backendMessageProcessingEngine")
public class MessageProcessingEngine {

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;
    
    @Autowired
    private TransformationExecutionService transformationService;
    
    @Autowired
    private OrchestrationEngineService orchestrationEngine;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<String, ProcessingExecution> activeExecutions = new ConcurrentHashMap<>();

    /**
     * Process a message using a saved integration flow
     */
    public ProcessingResult processMessage(String flowId, Object messageData) {
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(UUID.fromString(flowId));
            if (!flowOpt.isPresent()) {
                return ProcessingResult.error("Integration flow not found: " + flowId);
            }

            IntegrationFlow flow = flowOpt.get();
            ProcessingExecution execution = createExecution(flow, messageData);
            activeExecutions.put(execution.getExecutionId(), execution);

            return executeMessageProcessing(execution);
        } catch (Exception e) {
            return ProcessingResult.error("Message processing failed: " + e.getMessage());
        }
    }

    /**
     * Process message asynchronously
     */
    public CompletableFuture<ProcessingResult> processMessageAsync(String flowId, Object messageData) {
        return CompletableFuture.supplyAsync(() -> processMessage(flowId, messageData), executorService);
    }

    /**
     * Process batch of messages using a saved integration flow
     */
    public BatchProcessingResult processBatchMessages(String flowId, List<Object> messages) {
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(UUID.fromString(flowId));
            if (!flowOpt.isPresent()) {
                return BatchProcessingResult.error("Integration flow not found: " + flowId);
            }

            IntegrationFlow flow = flowOpt.get();
            BatchProcessingResult batchResult = new BatchProcessingResult();
            batchResult.setFlowId(flowId);
            batchResult.setTotalMessages(messages.size());
            batchResult.setStartTime(LocalDateTime.now());

            List<ProcessingResult> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            for (int i = 0; i < messages.size(); i++) {
                try {
                    Object message = messages.get(i);
                    ProcessingResult result = processMessage(flowId, message);
                    results.add(result);
                    
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                    
                    batchResult.addLog("Message " + (i + 1) + ": " + 
                        (result.isSuccess() ? "SUCCESS" : "FAILED - " + result.getMessage()));
                        
                } catch (Exception e) {
                    ProcessingResult errorResult = ProcessingResult.error("Processing failed: " + e.getMessage());
                    results.add(errorResult);
                    failureCount++;
                    batchResult.addLog("Message " + (i + 1) + ": FAILED - " + e.getMessage());
                }
            }

            batchResult.setProcessedMessages(messages.size());
            batchResult.setSuccessfulMessages(successCount);
            batchResult.setFailedMessages(failureCount);
            batchResult.setResults(results);
            batchResult.setEndTime(LocalDateTime.now());
            batchResult.setSuccess(failureCount == 0);

            return batchResult;
        } catch (Exception e) {
            return BatchProcessingResult.error("Batch processing failed: " + e.getMessage());
        }
    }

    /**
     * Get processing execution status
     */
    public Optional<ProcessingExecution> getExecutionStatus(String executionId) {
        return Optional.ofNullable(activeExecutions.get(executionId));
    }

    /**
     * Cancel active message processing
     */
    public boolean cancelProcessing(String executionId) {
        ProcessingExecution execution = activeExecutions.get(executionId);
        if (execution != null && execution.getStatus() == ProcessingStatus.RUNNING) {
            execution.setStatus(ProcessingStatus.CANCELLED);
            execution.addLog("Processing cancelled by user");
            return true;
        }
        return false;
    }

    /**
     * Get processing statistics for a flow
     */
    public ProcessingStats getFlowProcessingStats(String flowId) {
        ProcessingStats stats = new ProcessingStats();
        stats.setFlowId(flowId);
        
        // Calculate stats from active executions (in production, this would query a database)
        List<ProcessingExecution> flowExecutions = activeExecutions.values().stream()
                .filter(execution -> execution.getFlowId().equals(flowId))
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
        
        stats.setTotalProcessingRequests(flowExecutions.size());
        stats.setSuccessfulProcessings(
            (int) flowExecutions.stream().filter(e -> e.getStatus() == ProcessingStatus.COMPLETED).count()
        );
        stats.setFailedProcessings(
            (int) flowExecutions.stream().filter(e -> e.getStatus() == ProcessingStatus.FAILED).count()
        );
        
        // Calculate average processing time
        OptionalDouble avgTime = flowExecutions.stream()
                .filter(e -> e.getEndTime() != null)
                .mapToLong(e -> java.time.Duration.between(e.getStartTime(), e.getEndTime()).toMillis())
                .average();
        stats.setAverageProcessingTimeMs(avgTime.orElse(0.0));
        
        return stats;
    }

    /**
     * Validate that a flow can be executed
     */
    public ValidationResult validateFlowForExecution(String flowId) {
        ValidationResult result = new ValidationResult();
        
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(UUID.fromString(flowId));
            if (!flowOpt.isPresent()) {
                result.addError("Integration flow not found: " + flowId);
                return result;
            }

            IntegrationFlow flow = flowOpt.get();
            
            // Validate flow configuration
            if (flow.getSourceAdapterId() == null) {
                result.addError("Source adapter is required for message processing");
            }
            
            if (flow.getTargetAdapterId() == null) {
                result.addError("Target adapter is required for message processing");
            }
            
            // Validate that adapters are available and configured
            // In production, this would check adapter availability
            result.addWarning("Flow validation completed - adapters should be tested for availability");
            
        } catch (Exception e) {
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    private ProcessingExecution createExecution(IntegrationFlow flow, Object messageData) {
        ProcessingExecution execution = new ProcessingExecution();
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setFlowId(flow.getId().toString());
        execution.setFlowName(flow.getName());
        execution.setFlowType(determineFlowType(flow));
        execution.setStatus(ProcessingStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        execution.setInputMessage(messageData);
        execution.addLog("Message processing started for flow: " + flow.getName());
        
        return execution;
    }

    private ProcessingResult executeMessageProcessing(ProcessingExecution execution) {
        try {
            execution.addLog("Beginning message processing execution");
            
            // Step 1: Validate input message
            if (!validateInputMessage(execution)) {
                return ProcessingResult.error("Input message validation failed", execution.getLogs());
            }
            
            // Step 2: Route to appropriate processing engine based on flow type
            Object processedData;
            if (execution.getFlowType() == FlowType.DIRECT_MAPPING) {
                processedData = processDirectMappingFlow(execution);
            } else if (execution.getFlowType() == FlowType.ORCHESTRATION) {
                processedData = processOrchestrationFlow(execution);
            } else {
                throw new IllegalArgumentException("Unknown flow type: " + execution.getFlowType());
            }
            
            if (processedData == null) {
                return ProcessingResult.error("Message processing failed", execution.getLogs());
            }
            
            // Step 3: Finalize processing
            execution.setOutputMessage(processedData);
            execution.setStatus(ProcessingStatus.COMPLETED);
            execution.setEndTime(LocalDateTime.now());
            execution.addLog("Message processing completed successfully");
            
            return ProcessingResult.success(processedData, execution.getLogs());
            
        } catch (Exception e) {
            execution.setStatus(ProcessingStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.addLog("Processing failed: " + e.getMessage());
            return ProcessingResult.error("Message processing failed: " + e.getMessage(), execution.getLogs());
        }
    }

    private boolean validateInputMessage(ProcessingExecution execution) {
        try {
            if (execution.getInputMessage() == null) {
                execution.addLog("Input message validation failed: message is null");
                return false;
            }
            
            // Basic validation - in production this would be more comprehensive
            execution.addLog("Input message validation passed");
            return true;
        } catch (Exception e) {
            execution.addLog("Input message validation error: " + e.getMessage());
            return false;
        }
    }

    private Object processDirectMappingFlow(ProcessingExecution execution) {
        try {
            execution.addLog("Processing direct mapping flow");
            
            // Use transformation service for direct mapping
            String transformationId = execution.getFlowId() + "_transformation";
            var transformationResult = transformationService.executeTransformation(
                transformationId, 
                execution.getInputMessage()
            );
            
            if (transformationResult.isSuccess()) {
                execution.addLog("Direct mapping transformation completed successfully");
                return transformationResult.getData();
            } else {
                execution.addLog("Direct mapping transformation failed: " + transformationResult.getMessage());
                return null;
            }
        } catch (Exception e) {
            execution.addLog("Direct mapping processing error: " + e.getMessage());
            return null;
        }
    }

    private Object processOrchestrationFlow(ProcessingExecution execution) {
        try {
            execution.addLog("Processing orchestration flow");
            
            // Use orchestration engine for complex flows
            var orchestrationResult = orchestrationEngine.executeOrchestrationFlow(
                execution.getFlowId(), 
                execution.getInputMessage()
            );
            
            if (orchestrationResult.isSuccess()) {
                execution.addLog("Orchestration processing completed successfully");
                return orchestrationResult.getData();
            } else {
                execution.addLog("Orchestration processing failed: " + orchestrationResult.getMessage());
                return null;
            }
        } catch (Exception e) {
            execution.addLog("Orchestration processing error: " + e.getMessage());
            return null;
        }
    }

    private FlowType determineFlowType(IntegrationFlow flow) {
        // Simple heuristic - in production this would be stored as flow metadata
        if (flow.getDescription() != null && flow.getDescription().toLowerCase().contains("orchestration")) {
            return FlowType.ORCHESTRATION;
        }
        return FlowType.DIRECT_MAPPING;
    }

    // Result and execution classes
    public static class ProcessingResult {
        private boolean success;
        private Object data;
        private String message;
        private List<String> logs;
        private String executionId;

        public static ProcessingResult success(Object data, List<String> logs) {
            ProcessingResult result = new ProcessingResult();
            result.success = true;
            result.data = data;
            result.logs = logs != null ? logs : new ArrayList<>();
            return result;
        }

        public static ProcessingResult error(String message) {
            ProcessingResult result = new ProcessingResult();
            result.success = false;
            result.message = message;
            result.logs = new ArrayList<>();
            return result;
        }

        public static ProcessingResult error(String message, List<String> logs) {
            ProcessingResult result = new ProcessingResult();
            result.success = false;
            result.message = message;
            result.logs = logs != null ? logs : new ArrayList<>();
            return result;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
    }

    public static class BatchProcessingResult {
        private String flowId;
        private boolean success;
        private String message;
        private int totalMessages;
        private int processedMessages;
        private int successfulMessages;
        private int failedMessages;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<ProcessingResult> results = new ArrayList<>();
        private List<String> logs = new ArrayList<>();

        public static BatchProcessingResult error(String message) {
            BatchProcessingResult result = new BatchProcessingResult();
            result.success = false;
            result.message = message;
            return result;
        }

        // Getters and setters
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getTotalMessages() { return totalMessages; }
        public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
        public int getProcessedMessages() { return processedMessages; }
        public void setProcessedMessages(int processedMessages) { this.processedMessages = processedMessages; }
        public int getSuccessfulMessages() { return successfulMessages; }
        public void setSuccessfulMessages(int successfulMessages) { this.successfulMessages = successfulMessages; }
        public int getFailedMessages() { return failedMessages; }
        public void setFailedMessages(int failedMessages) { this.failedMessages = failedMessages; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public List<ProcessingResult> getResults() { return results; }
        public void setResults(List<ProcessingResult> results) { this.results = results; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
        
        public void addLog(String message) {
            this.logs.add(LocalDateTime.now() + ": " + message);
        }
    }

    public static class ProcessingExecution {
        private String executionId;
        private String flowId;
        private String flowName;
        private FlowType flowType;
        private ProcessingStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Object inputMessage;
        private Object outputMessage;
        private List<String> logs = new ArrayList<>();

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public FlowType getFlowType() { return flowType; }
        public void setFlowType(FlowType flowType) { this.flowType = flowType; }
        public ProcessingStatus getStatus() { return status; }
        public void setStatus(ProcessingStatus status) { this.status = status; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Object getInputMessage() { return inputMessage; }
        public void setInputMessage(Object inputMessage) { this.inputMessage = inputMessage; }
        public Object getOutputMessage() { return outputMessage; }
        public void setOutputMessage(Object outputMessage) { this.outputMessage = outputMessage; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
        
        public void addLog(String message) {
            this.logs.add(LocalDateTime.now() + ": " + message);
        }
    }

    public static class ProcessingStats {
        private String flowId;
        private int totalProcessingRequests;
        private int successfulProcessings;
        private int failedProcessings;
        private double averageProcessingTimeMs;

        // Getters and setters
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public int getTotalProcessingRequests() { return totalProcessingRequests; }
        public void setTotalProcessingRequests(int totalProcessingRequests) { this.totalProcessingRequests = totalProcessingRequests; }
        public int getSuccessfulProcessings() { return successfulProcessings; }
        public void setSuccessfulProcessings(int successfulProcessings) { this.successfulProcessings = successfulProcessings; }
        public int getFailedProcessings() { return failedProcessings; }
        public void setFailedProcessings(int failedProcessings) { this.failedProcessings = failedProcessings; }
        public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(double averageProcessingTimeMs) { this.averageProcessingTimeMs = averageProcessingTimeMs; }
    }

    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public void addError(String error) { 
            this.errors.add(error); 
            this.valid = false;
        }
        
        public void addWarning(String warning) { 
            this.warnings.add(warning); 
        }
    }

    public enum ProcessingStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public enum FlowType {
        DIRECT_MAPPING, ORCHESTRATION
    }
}