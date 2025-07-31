package com.integrationlab.shared.dto.flow;

import java.time.LocalDateTime;
import java.util.List;

import com.integrationlab.shared.dto.mapping.FieldMappingDTO;

/**
 * DTO for FlowTransformationDTO.
 * Encapsulates data for transport between layers.
 */
public class FlowTransformationDTO {
    private String id;
    private String flowId;
    private String type;
    private String configuration;
    private int executionOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FieldMappingDTO> fieldMappings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder) {
        this.executionOrder = executionOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FieldMappingDTO> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(List<FieldMappingDTO> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }
}