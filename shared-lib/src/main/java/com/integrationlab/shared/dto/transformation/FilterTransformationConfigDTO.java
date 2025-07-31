package com.integrationlab.shared.dto.transformation;

import java.util.Map;

/**
 * DTO for FilterTransformationConfigDTO.
 * Encapsulates data for transport between layers.
 */
public class FilterTransformationConfigDTO {

    /**
     * The filter expression as a JavaScript boolean function.
     * Example: "(record) => record.amount > 100"
     */
    private String filterExpression;

    /**
     * Additional parameters for the filter if needed.
     */
    private Map<String, Object> parameters;

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}