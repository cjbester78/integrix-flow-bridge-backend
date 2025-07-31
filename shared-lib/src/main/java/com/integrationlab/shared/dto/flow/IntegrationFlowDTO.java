// DTO: IntegrationFlowDTO.java
package com.integrationlab.shared.dto.flow;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for IntegrationFlowDTO.
 * Encapsulates data for transport between layers.
 */
public class IntegrationFlowDTO {
    private String id;
    private String name;
    private String description;
    private String sourceAdapterId;
    private String targetAdapterId;
    private String sourceStructureId;
    private String targetStructureId;
    private String status;
    private String configuration;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastExecutionAt;
    private int executionCount;
    private int successCount;
    private int errorCount;
    private List<FlowTransformationDTO> transformations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceAdapterId() {
        return sourceAdapterId;
    }

    public void setSourceAdapterId(String sourceAdapterId) {
        this.sourceAdapterId = sourceAdapterId;
    }

    public String getTargetAdapterId() {
        return targetAdapterId;
    }

    public void setTargetAdapterId(String targetAdapterId) {
        this.targetAdapterId = targetAdapterId;
    }

    public String getSourceStructureId() {
        return sourceStructureId;
    }

    public void setSourceStructureId(String sourceStructureId) {
        this.sourceStructureId = sourceStructureId;
    }

    public String getTargetStructureId() {
        return targetStructureId;
    }

    public void setTargetStructureId(String targetStructureId) {
        this.targetStructureId = targetStructureId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastExecutionAt() {
        return lastExecutionAt;
    }

    public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
        this.lastExecutionAt = lastExecutionAt;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<FlowTransformationDTO> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<FlowTransformationDTO> transformations) {
        this.transformations = transformations;
    }
}