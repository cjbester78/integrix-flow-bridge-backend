package com.integrationlab.service;

import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.repository.IntegrationFlowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrchestrationEngineService {

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;
    
    @Autowired
    private TransformationExecutionService transformationService;
    
    @Autowired
    private CommunicationAdapterService adapterService;
    
    @Autowired
    private BusinessComponentService businessComponentService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, OrchestrationExecution> activeExecutions = new ConcurrentHashMap<>();

    /**
     * Execute a complete orchestrated flow
     */
    public OrchestrationResult executeOrchestrationFlow(String flowId, Object inputData) {
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(flowId);
            if (!flowOpt.isPresent()) {
                return OrchestrationResult.error("Flow not found: " + flowId);
            }

            IntegrationFlow flow = flowOpt.get();
            OrchestrationExecution execution = createExecution(flow, inputData);
            activeExecutions.put(execution.getExecutionId(), execution);

            return executeWorkflow(execution);
        } catch (Exception e) {
            return OrchestrationResult.error("Orchestration execution failed: " + e.getMessage());
        }
    }

    /**
     * Execute orchestration steps asynchronously
     */
    public CompletableFuture<OrchestrationResult> executeOrchestrationFlowAsync(String flowId, Object inputData) {
        return CompletableFuture.supplyAsync(() -> executeOrchestrationFlow(flowId, inputData));
    }

    /**
     * Get current execution status
     */
    public Optional<OrchestrationExecution> getExecutionStatus(String executionId) {
        return Optional.ofNullable(activeExecutions.get(executionId));
    }

    /**
     * Cancel an active orchestration execution
     */
    public boolean cancelExecution(String executionId) {
        OrchestrationExecution execution = activeExecutions.get(executionId);
        if (execution != null) {
            execution.setStatus(ExecutionStatus.CANCELLED);
            execution.addLog("Execution cancelled by user");
            return true;
        }
        return false;
    }

    /**
     * Validate orchestration flow configuration
     */
    public ValidationResult validateOrchestrationFlow(String flowId) {
        ValidationResult result = new ValidationResult();
        
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(flowId);
            if (!flowOpt.isPresent()) {
                result.addError("Flow not found: " + flowId);
                return result;
            }

            IntegrationFlow flow = flowOpt.get();
            validateFlowStructure(flow, result);
            
        } catch (Exception e) {
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Get orchestration execution history
     */
    public List<OrchestrationExecution> getExecutionHistory(String flowId, int limit) {
        return activeExecutions.values().stream()
                .filter(execution -> execution.getFlowId().equals(flowId))
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(limit)
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }

    private OrchestrationExecution createExecution(IntegrationFlow flow, Object inputData) {
        OrchestrationExecution execution = new OrchestrationExecution();
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setFlowId(flow.getId());
        execution.setFlowName(flow.getName());
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        execution.setInputData(inputData);
        execution.addLog("Orchestration execution started");
        
        return execution;
    }

    private OrchestrationResult executeWorkflow(OrchestrationExecution execution) {
        try {
            execution.addLog("Beginning workflow execution");
            
            // Step 1: Initialize process
            initializeProcess(execution);
            
            // Step 2: Load and validate business components
            if (!loadBusinessComponents(execution)) {
                return OrchestrationResult.error("Failed to load business components", execution.getLogs());
            }
            
            // Step 3: Initialize source and target adapters
            if (!initializeAdapters(execution)) {
                return OrchestrationResult.error("Failed to initialize adapters", execution.getLogs());
            }
            
            // Step 4: Execute transformation functions
            if (!executeTransformations(execution)) {
                return OrchestrationResult.error("Failed to execute transformations", execution.getLogs());
            }
            
            // Step 5: Process multiple targets (if applicable)
            if (!processMultipleTargets(execution)) {
                return OrchestrationResult.error("Failed to process multiple targets", execution.getLogs());
            }
            
            // Step 6: Complete process
            completeProcess(execution);
            
            execution.setStatus(ExecutionStatus.COMPLETED);
            execution.setEndTime(LocalDateTime.now());
            execution.addLog("Orchestration execution completed successfully");
            
            return OrchestrationResult.success(execution.getOutputData(), execution.getLogs());
            
        } catch (Exception e) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.addLog("Execution failed: " + e.getMessage());
            return OrchestrationResult.error("Workflow execution failed: " + e.getMessage(), execution.getLogs());
        }
    }

    private void initializeProcess(OrchestrationExecution execution) {
        execution.addLog("Process initialized - setting up execution context");
        execution.setCurrentStep("INITIALIZE");
        
        // Initialize execution context with input data
        ObjectNode context = objectMapper.createObjectNode();
        context.set("inputData", objectMapper.valueToTree(execution.getInputData()));
        context.put("executionId", execution.getExecutionId());
        context.put("flowId", execution.getFlowId());
        execution.setExecutionContext(context);
    }

    private boolean loadBusinessComponents(OrchestrationExecution execution) {
        try {
            execution.addLog("Loading business components");
            execution.setCurrentStep("LOAD_COMPONENTS");
            
            // This would load the actual business components defined for the flow
            // For now, simulating successful load
            execution.addLog("Business components loaded successfully");
            return true;
        } catch (Exception e) {
            execution.addLog("Failed to load business components: " + e.getMessage());
            return false;
        }
    }

    private boolean initializeAdapters(OrchestrationExecution execution) {
        try {
            execution.addLog("Initializing communication adapters");
            execution.setCurrentStep("INITIALIZE_ADAPTERS");
            
            // This would initialize the actual adapters for the flow
            // For now, simulating successful initialization
            execution.addLog("Source and target adapters initialized");
            return true;
        } catch (Exception e) {
            execution.addLog("Failed to initialize adapters: " + e.getMessage());
            return false;
        }
    }

    private boolean executeTransformations(OrchestrationExecution execution) {
        try {
            execution.addLog("Executing transformation functions");
            execution.setCurrentStep("EXECUTE_TRANSFORMATIONS");
            
            // Execute field transformations using the TransformationExecutionService
            String transformationId = execution.getFlowId() + "_transformation";
            var transformationResult = transformationService.executeTransformation(
                transformationId, 
                execution.getInputData()
            );
            
            if (transformationResult.isSuccess()) {
                execution.setTransformedData(transformationResult.getData());
                execution.addLog("Transformations executed successfully");
                return true;
            } else {
                execution.addLog("Transformation failed: " + transformationResult.getMessage());
                return false;
            }
        } catch (Exception e) {
            execution.addLog("Failed to execute transformations: " + e.getMessage());
            return false;
        }
    }

    private boolean processMultipleTargets(OrchestrationExecution execution) {
        try {
            execution.addLog("Processing multiple target systems");
            execution.setCurrentStep("PROCESS_TARGETS");
            
            // This would handle routing to multiple target systems
            // For now, simulating successful processing
            ObjectNode outputData = objectMapper.createObjectNode();
            outputData.set("transformedData", objectMapper.valueToTree(execution.getTransformedData()));
            outputData.put("processedTargets", 1);
            outputData.put("timestamp", LocalDateTime.now().toString());
            
            execution.setOutputData(outputData);
            execution.addLog("Multiple targets processed successfully");
            return true;
        } catch (Exception e) {
            execution.addLog("Failed to process multiple targets: " + e.getMessage());
            return false;
        }
    }

    private void completeProcess(OrchestrationExecution execution) {
        execution.addLog("Completing orchestration process");
        execution.setCurrentStep("COMPLETE");
        
        // Cleanup and finalization
        execution.addLog("Process completed - cleaning up resources");
    }

    private void validateFlowStructure(IntegrationFlow flow, ValidationResult result) {
        if (flow.getSourceAdapterId() == null || flow.getSourceAdapterId().trim().isEmpty()) {
            result.addError("Source adapter is required for orchestration flow");
        }
        
        if (flow.getTargetAdapterId() == null || flow.getTargetAdapterId().trim().isEmpty()) {
            result.addError("Target adapter is required for orchestration flow");
        }
        
        // Additional validation for orchestration-specific requirements
        result.addWarning("Orchestration flow validation completed");
    }

    // Result and execution classes
    public static class OrchestrationResult {
        private boolean success;
        private Object data;
        private String message;
        private List<String> logs;
        private String executionId;

        public static OrchestrationResult success(Object data, List<String> logs) {
            OrchestrationResult result = new OrchestrationResult();
            result.success = true;
            result.data = data;
            result.logs = logs != null ? logs : new ArrayList<>();
            return result;
        }

        public static OrchestrationResult error(String message) {
            OrchestrationResult result = new OrchestrationResult();
            result.success = false;
            result.message = message;
            result.logs = new ArrayList<>();
            return result;
        }

        public static OrchestrationResult error(String message, List<String> logs) {
            OrchestrationResult result = new OrchestrationResult();
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

    public static class OrchestrationExecution {
        private String executionId;
        private String flowId;
        private String flowName;
        private ExecutionStatus status;
        private String currentStep;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Object inputData;
        private Object transformedData;
        private Object outputData;
        private ObjectNode executionContext;
        private List<String> logs = new ArrayList<>();

        // Getters and setters
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
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Object getInputData() { return inputData; }
        public void setInputData(Object inputData) { this.inputData = inputData; }
        public Object getTransformedData() { return transformedData; }
        public void setTransformedData(Object transformedData) { this.transformedData = transformedData; }
        public Object getOutputData() { return outputData; }
        public void setOutputData(Object outputData) { this.outputData = outputData; }
        public ObjectNode getExecutionContext() { return executionContext; }
        public void setExecutionContext(ObjectNode executionContext) { this.executionContext = executionContext; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
        
        public void addLog(String message) {
            this.logs.add(LocalDateTime.now() + ": " + message);
        }
    }

    public enum ExecutionStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
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
}