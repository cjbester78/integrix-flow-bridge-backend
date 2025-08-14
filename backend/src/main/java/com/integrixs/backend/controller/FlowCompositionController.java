package com.integrixs.backend.controller;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.backend.service.FlowCompositionService;
import com.integrixs.backend.service.FlowCompositionService.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flow-composition")
public class FlowCompositionController {

    @Autowired
    private FlowCompositionService flowCompositionService;

    /**
     * Create a complete direct mapping flow
     */
    @PostMapping("/direct-mapping")
    public ResponseEntity<?> createDirectMappingFlow(@RequestBody DirectMappingFlowRequest request) {
        try {
            IntegrationFlow flow = flowCompositionService.createDirectMappingFlow(request);
            return ResponseEntity.ok(flow);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Create a complete orchestration flow
     */
    @PostMapping("/orchestration")
    public ResponseEntity<IntegrationFlow> createOrchestrationFlow(@RequestBody OrchestrationFlowRequest request) {
        try {
            IntegrationFlow flow = flowCompositionService.createOrchestrationFlow(request);
            return ResponseEntity.ok(flow);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update an existing flow composition
     */
    @PutMapping("/{flowId}")
    public ResponseEntity<IntegrationFlow> updateFlowComposition(@PathVariable String flowId, @RequestBody UpdateFlowRequest request) {
        try {
            return flowCompositionService.updateFlowComposition(flowId, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get complete flow composition with all related components
     */
    @GetMapping("/{flowId}/complete")
    public ResponseEntity<CompleteFlowComposition> getCompleteFlowComposition(@PathVariable String flowId) {
        return flowCompositionService.getCompleteFlowComposition(flowId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a complete flow and all its components
     */
    @DeleteMapping("/{flowId}")
    public ResponseEntity<Void> deleteFlowComposition(@PathVariable String flowId) {
        boolean deleted = flowCompositionService.deleteFlowComposition(flowId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Test a flow configuration before saving
     */
    @PostMapping("/validate/direct-mapping")
    public ResponseEntity<ValidationResult> validateDirectMappingFlow(@RequestBody DirectMappingFlowRequest request) {
        try {
            ValidationResult result = validateDirectMappingRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ValidationResult result = new ValidationResult();
            result.setValid(false);
            result.addError("Validation failed: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Test an orchestration flow configuration before saving
     */
    @PostMapping("/validate/orchestration")
    public ResponseEntity<ValidationResult> validateOrchestrationFlow(@RequestBody OrchestrationFlowRequest request) {
        try {
            ValidationResult result = validateOrchestrationRequest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ValidationResult result = new ValidationResult();
            result.setValid(false);
            result.addError("Validation failed: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Clone an existing flow with new name
     */
    @PostMapping("/{flowId}/clone")
    public ResponseEntity<IntegrationFlow> cloneFlow(@PathVariable String flowId, @RequestParam String newName) {
        return flowCompositionService.getCompleteFlowComposition(flowId)
                .map(composition -> {
                    // Create a new direct mapping flow based on the existing one
                    DirectMappingFlowRequest cloneRequest = new DirectMappingFlowRequest();
                    cloneRequest.setFlowName(newName + " (Copy)");
                    cloneRequest.setDescription("Cloned from: " + composition.getFlow().getName());
                    cloneRequest.setSourceAdapterId(composition.getFlow().getSourceAdapterId());
                    cloneRequest.setTargetAdapterId(composition.getFlow().getTargetAdapterId());
                    cloneRequest.setSourceStructureId(composition.getFlow().getSourceStructureId());
                    cloneRequest.setTargetStructureId(composition.getFlow().getTargetStructureId());
                    
                    IntegrationFlow clonedFlow = flowCompositionService.createDirectMappingFlow(cloneRequest);
                    return ResponseEntity.ok(clonedFlow);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ValidationResult validateDirectMappingRequest(DirectMappingFlowRequest request) {
        ValidationResult result = new ValidationResult();
        
        if (request.getFlowName() == null || request.getFlowName().trim().isEmpty()) {
            result.addError("Flow name is required");
        }
        
        if (request.getSourceAdapterId() == null) {
            result.addError("Source adapter is required");
        }
        
        if (request.getTargetAdapterId() == null) {
            result.addError("Target adapter is required");
        }
        
        if (request.getSourceAdapterId() != null && request.getSourceAdapterId().equals(request.getTargetAdapterId())) {
            result.addError("Source and target adapters cannot be the same");
        }
        
        result.setValid(result.getErrors().isEmpty());
        return result;
    }

    private ValidationResult validateOrchestrationRequest(OrchestrationFlowRequest request) {
        ValidationResult result = new ValidationResult();
        
        if (request.getFlowName() == null || request.getFlowName().trim().isEmpty()) {
            result.addError("Flow name is required");
        }
        
        if (request.getSourceAdapterId() == null) {
            result.addError("Source adapter is required");
        }
        
        if (request.getTargetAdapterId() == null) {
            result.addError("Target adapter is required");
        }
        
        if (request.getOrchestrationSteps() == null || request.getOrchestrationSteps().isEmpty()) {
            result.addError("At least one orchestration step is required");
        }
        
        result.setValid(result.getErrors().isEmpty());
        return result;
    }

    // Validation result DTO
    public static class ValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public java.util.List<String> getErrors() { return errors; }
        public void setErrors(java.util.List<String> errors) { this.errors = errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }
        
        public void addError(String error) { 
            this.errors.add(error); 
            this.valid = false;
        }
        
        public void addWarning(String warning) { 
            this.warnings.add(warning); 
        }
    }
    
    // Error response DTO
    public static class ErrorResponse {
        private String error;
        private String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}