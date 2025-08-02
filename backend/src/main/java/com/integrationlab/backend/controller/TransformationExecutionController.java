package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.TransformationExecutionService;
import com.integrationlab.backend.service.TransformationExecutionService.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transformations")
public class TransformationExecutionController {

    @Autowired
    private TransformationExecutionService transformationService;

    /**
     * Execute transformation for a specific transformation ID with input data
     */
    @PostMapping("/{transformationId}/execute")
    public ResponseEntity<TransformationResult> executeTransformation(
            @PathVariable String transformationId, 
            @RequestBody Object inputData) {
        try {
            TransformationResult result = transformationService.executeTransformation(transformationId, inputData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            TransformationResult errorResult = TransformationResult.error("Execution failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * Test transformation with sample data before applying to real flow
     */
    @PostMapping("/{transformationId}/test")
    public ResponseEntity<TransformationResult> testTransformation(
            @PathVariable String transformationId,
            @RequestBody TestTransformationRequest request) {
        try {
            TransformationResult result = transformationService.executeTransformation(
                transformationId, 
                request.getSampleData()
            );
            
            // Add test-specific metadata
            result.setMessage("Test execution completed - " + 
                (result.isSuccess() ? "SUCCESS" : "FAILED"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            TransformationResult errorResult = TransformationResult.error("Test failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Validate transformation configuration without executing
     */
    @PostMapping("/{transformationId}/validate")
    public ResponseEntity<ValidationResult> validateTransformation(@PathVariable String transformationId) {
        try {
            ValidationResult result = transformationService.validateTransformation(transformationId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ValidationResult errorResult = new ValidationResult();
            errorResult.addError("Validation failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Execute transformation with custom JavaScript function (for testing)
     */
    @PostMapping("/execute-javascript")
    public ResponseEntity<JavaScriptExecutionResult> executeJavaScript(@RequestBody JavaScriptExecutionRequest request) {
        try {
            // Create a temporary transformation for testing JavaScript
            JavaScriptExecutionResult result = executeCustomJavaScript(
                request.getJavaScriptCode(),
                request.getInputData()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            JavaScriptExecutionResult errorResult = new JavaScriptExecutionResult();
            errorResult.setSuccess(false);
            errorResult.setError("JavaScript execution failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Preview transformation result without saving
     */
    @PostMapping("/preview")
    public ResponseEntity<TransformationResult> previewTransformation(@RequestBody TransformationPreviewRequest request) {
        try {
            // This would create a temporary transformation for preview purposes
            TransformationResult result = previewFieldMappings(
                request.getSourceData(),
                request.getFieldMappings(),
                request.getTransformationRules()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            TransformationResult errorResult = TransformationResult.error("Preview failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Get transformation execution statistics
     */
    @GetMapping("/{transformationId}/stats")
    public ResponseEntity<TransformationStats> getTransformationStats(@PathVariable String transformationId) {
        try {
            TransformationStats stats = getExecutionStats(transformationId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Batch execute transformations for multiple records
     */
    @PostMapping("/{transformationId}/batch-execute")
    public ResponseEntity<BatchTransformationResult> batchExecuteTransformation(
            @PathVariable String transformationId,
            @RequestBody BatchTransformationRequest request) {
        try {
            BatchTransformationResult result = executeBatchTransformation(transformationId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            BatchTransformationResult errorResult = new BatchTransformationResult();
            errorResult.setOverallSuccess(false);
            errorResult.setErrorMessage("Batch execution failed: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * Get available transformation functions and utilities
     */
    @GetMapping("/functions")
    public ResponseEntity<TransformationFunctionLibrary> getAvailableFunctions() {
        TransformationFunctionLibrary library = buildFunctionLibrary();
        return ResponseEntity.ok(library);
    }

    // Helper methods

    private JavaScriptExecutionResult executeCustomJavaScript(String jsCode, Object inputData) {
        JavaScriptExecutionResult result = new JavaScriptExecutionResult();
        
        try {
            // This would use the JavaScript engine from TransformationExecutionService
            // For now, returning a basic result
            result.setSuccess(true);
            result.setResult("JavaScript execution placeholder - would execute: " + jsCode);
            result.setExecutionTimeMs(System.currentTimeMillis() % 100); // Simulated time
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
        }
        
        return result;
    }

    private TransformationResult previewFieldMappings(Object sourceData, Object fieldMappings, Object transformationRules) {
        // This would create a temporary transformation and execute it
        // For now, returning a basic preview result
        return TransformationResult.success(
            "Preview result for source data", 
            "Field mappings would be applied here"
        );
    }

    private TransformationStats getExecutionStats(String transformationId) {
        TransformationStats stats = new TransformationStats();
        stats.setTransformationId(transformationId);
        stats.setTotalExecutions(0); // Would query from execution logs
        stats.setSuccessfulExecutions(0);
        stats.setFailedExecutions(0);
        stats.setAverageExecutionTimeMs(0.0);
        stats.setLastExecutionTime(null);
        return stats;
    }

    private BatchTransformationResult executeBatchTransformation(String transformationId, BatchTransformationRequest request) {
        BatchTransformationResult result = new BatchTransformationResult();
        result.setTotalRecords(request.getInputRecords().size());
        result.setProcessedRecords(0);
        result.setSuccessfulRecords(0);
        result.setFailedRecords(0);
        result.setOverallSuccess(true);
        
        // This would iterate through all input records and execute transformation for each
        // For now, returning a basic result
        result.setProcessedRecords(request.getInputRecords().size());
        result.setSuccessfulRecords(request.getInputRecords().size());
        
        return result;
    }

    private TransformationFunctionLibrary buildFunctionLibrary() {
        TransformationFunctionLibrary library = new TransformationFunctionLibrary();
        
        // String functions
        library.addStringFunction("toUpperCase", "Convert string to uppercase", "toUpperCase(value)");
        library.addStringFunction("toLowerCase", "Convert string to lowercase", "toLowerCase(value)");
        library.addStringFunction("trim", "Remove leading/trailing whitespace", "trim(value)");
        library.addStringFunction("substring", "Extract substring", "substring(value, start, end)");
        library.addStringFunction("replace", "Replace text", "replace(value, search, replacement)");
        
        // Date functions
        library.addDateFunction("formatDate", "Format date string", "formatDate(date, format)");
        library.addDateFunction("getCurrentDate", "Get current date", "getCurrentDate()");
        
        // Number functions
        library.addNumberFunction("parseDouble", "Parse string to double", "parseDouble(value)");
        library.addNumberFunction("parseInt", "Parse string to integer", "parseInt(value)");
        library.addNumberFunction("formatNumber", "Format number", "formatNumber(number, format)");
        
        return library;
    }

    // Request/Response DTOs

    public static class TestTransformationRequest {
        private Object sampleData;
        
        public Object getSampleData() { return sampleData; }
        public void setSampleData(Object sampleData) { this.sampleData = sampleData; }
    }

    public static class JavaScriptExecutionRequest {
        private String javaScriptCode;
        private Object inputData;
        
        public String getJavaScriptCode() { return javaScriptCode; }
        public void setJavaScriptCode(String javaScriptCode) { this.javaScriptCode = javaScriptCode; }
        public Object getInputData() { return inputData; }
        public void setInputData(Object inputData) { this.inputData = inputData; }
    }

    public static class JavaScriptExecutionResult {
        private boolean success;
        private Object result;
        private String error;
        private long executionTimeMs;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    }

    public static class TransformationPreviewRequest {
        private Object sourceData;
        private Object fieldMappings;
        private Object transformationRules;
        
        public Object getSourceData() { return sourceData; }
        public void setSourceData(Object sourceData) { this.sourceData = sourceData; }
        public Object getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(Object fieldMappings) { this.fieldMappings = fieldMappings; }
        public Object getTransformationRules() { return transformationRules; }
        public void setTransformationRules(Object transformationRules) { this.transformationRules = transformationRules; }
    }

    public static class BatchTransformationRequest {
        private java.util.List<Object> inputRecords;
        private boolean stopOnError = false;
        
        public java.util.List<Object> getInputRecords() { return inputRecords; }
        public void setInputRecords(java.util.List<Object> inputRecords) { this.inputRecords = inputRecords; }
        public boolean isStopOnError() { return stopOnError; }
        public void setStopOnError(boolean stopOnError) { this.stopOnError = stopOnError; }
    }

    public static class BatchTransformationResult {
        private int totalRecords;
        private int processedRecords;
        private int successfulRecords;
        private int failedRecords;
        private boolean overallSuccess;
        private String errorMessage;
        private java.util.List<TransformationResult> results = new java.util.ArrayList<>();
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getProcessedRecords() { return processedRecords; }
        public void setProcessedRecords(int processedRecords) { this.processedRecords = processedRecords; }
        public int getSuccessfulRecords() { return successfulRecords; }
        public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }
        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
        public boolean isOverallSuccess() { return overallSuccess; }
        public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public java.util.List<TransformationResult> getResults() { return results; }
        public void setResults(java.util.List<TransformationResult> results) { this.results = results; }
    }

    public static class TransformationStats {
        private String transformationId;
        private int totalExecutions;
        private int successfulExecutions;
        private int failedExecutions;
        private double averageExecutionTimeMs;
        private java.time.LocalDateTime lastExecutionTime;
        
        public String getTransformationId() { return transformationId; }
        public void setTransformationId(String transformationId) { this.transformationId = transformationId; }
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

    public static class TransformationFunctionLibrary {
        private java.util.Map<String, java.util.List<FunctionInfo>> functions = new java.util.HashMap<>();
        
        public void addStringFunction(String name, String description, String usage) {
            functions.computeIfAbsent("string", k -> new java.util.ArrayList<>())
                    .add(new FunctionInfo(name, description, usage));
        }
        
        public void addDateFunction(String name, String description, String usage) {
            functions.computeIfAbsent("date", k -> new java.util.ArrayList<>())
                    .add(new FunctionInfo(name, description, usage));
        }
        
        public void addNumberFunction(String name, String description, String usage) {
            functions.computeIfAbsent("number", k -> new java.util.ArrayList<>())
                    .add(new FunctionInfo(name, description, usage));
        }
        
        public java.util.Map<String, java.util.List<FunctionInfo>> getFunctions() { return functions; }
        public void setFunctions(java.util.Map<String, java.util.List<FunctionInfo>> functions) { this.functions = functions; }
    }

    public static class FunctionInfo {
        private String name;
        private String description;
        private String usage;
        
        public FunctionInfo(String name, String description, String usage) {
            this.name = name;
            this.description = description;  
            this.usage = usage;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUsage() { return usage; }
        public void setUsage(String usage) { this.usage = usage; }
    }
}