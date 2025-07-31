package com.integrationlab.shared.dto.transformation;

import java.util.List;

/**
 * DTO for ValidationTransformationConfigDTO.
 * Encapsulates data for transport between layers.
 */
public class ValidationTransformationConfigDTO {

    /**
     * List of validation rules as JavaScript function bodies or expressions.
     * Each rule should return boolean (true if valid, false otherwise).
     */
    private List<String> validationRules;

    /**
     * Optional error messages corresponding to each validation rule.
     * Should be same size as validationRules.
     */
    private List<String> errorMessages;

    /**
     * If true, validation will stop at the first failure.
     */
    private boolean failFast = true;

    public ValidationTransformationConfigDTO() {
        // no-arg constructor
    }

    public List<String> getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(List<String> validationRules) {
        this.validationRules = validationRules;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override
    public String toString() {
        return "ValidationTransformationConfigDTO{" +
                "validationRules=" + validationRules +
                ", errorMessages=" + errorMessages +
                ", failFast=" + failFast +
                '}';
    }
}