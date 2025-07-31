package com.integrationlab.shared.dto.transformation;

import java.util.List;

/**
 * DTO for CustomFunctionConfigDTO.
 * Encapsulates data for transport between layers.
 */
public class CustomFunctionConfigDTO {

    private List<String> sourceFields;
    private String javaFunction;

    public List<String> getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(List<String> sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getJavaFunction() {
        return javaFunction;
    }

    public void setJavaFunction(String javaFunction) {
        this.javaFunction = javaFunction;
    }

    @Override
    public String toString() {
        return "CustomFunctionConfigDTO{" +
                "sourceFields=" + sourceFields +
                ", javaFunction='" + javaFunction + '\'' +
                '}';
    }
}