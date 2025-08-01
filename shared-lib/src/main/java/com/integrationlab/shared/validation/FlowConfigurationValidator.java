package com.integrationlab.shared.validation;

import com.integrationlab.shared.dto.flow.FlowCreateRequestDTO;
import com.integrationlab.shared.dto.flow.FlowTransformationDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validator implementation for flow configuration validation.
 * 
 * <p>Validates integration flow configurations ensuring proper
 * adapter compatibility and transformation ordering.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowConfigurationValidator implements ConstraintValidator<ValidFlowConfiguration, FlowCreateRequestDTO> {
    
    @Override
    public void initialize(ValidFlowConfiguration constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(FlowCreateRequestDTO flow, ConstraintValidatorContext context) {
        if (flow == null) {
            return true; // Let @NotNull handle null checks
        }
        
        // Disable default constraint violation
        context.disableDefaultConstraintViolation();
        
        boolean isValid = true;
        
        // Validate source and target adapters are different
        if (flow.getSourceAdapterId() != null && 
            flow.getSourceAdapterId().equals(flow.getTargetAdapterId())) {
            context.buildConstraintViolationWithTemplate(
                "Source and target adapters must be different")
                .addConstraintViolation();
            isValid = false;
        }
        
        // Validate transformations if present
        if (flow.getTransformations() != null && !flow.getTransformations().isEmpty()) {
            isValid = validateTransformations(flow.getTransformations(), context) && isValid;
        }
        
        // Validate initial status
        if (flow.isActivateOnCreation() && "ERROR".equals(flow.getStatus())) {
            context.buildConstraintViolationWithTemplate(
                "Cannot activate flow with ERROR status")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean validateTransformations(List<FlowTransformationDTO> transformations, 
                                          ConstraintValidatorContext context) {
        boolean valid = true;
        
        // Check for duplicate execution orders
        Set<Integer> executionOrders = new HashSet<>();
        for (FlowTransformationDTO transformation : transformations) {
            if (transformation.getExecutionOrder() > 0) {
                if (!executionOrders.add(transformation.getExecutionOrder())) {
                    context.buildConstraintViolationWithTemplate(
                        "Duplicate execution order found: " + transformation.getExecutionOrder())
                        .addConstraintViolation();
                    valid = false;
                }
            }
        }
        
        // Validate transformation dependencies
        for (int i = 0; i < transformations.size(); i++) {
            FlowTransformationDTO transformation = transformations.get(i);
            
            // Validate transformation type specific rules
            if ("VALIDATION".equals(transformation.getType())) {
                // Validation transformations should come early in the flow
                if (transformation.getExecutionOrder() > transformations.size() / 2) {
                    context.buildConstraintViolationWithTemplate(
                        "Validation transformations should be placed early in the flow")
                        .addConstraintViolation();
                    valid = false;
                }
            }
            
            // Validate error strategy based on transformation type
            if ("ENRICHMENT".equals(transformation.getType()) && 
                "FAIL".equals(transformation.getErrorStrategy())) {
                context.buildConstraintViolationWithTemplate(
                    "Enrichment transformations should not use FAIL error strategy")
                    .addConstraintViolation();
                valid = false;
            }
        }
        
        return valid;
    }
}