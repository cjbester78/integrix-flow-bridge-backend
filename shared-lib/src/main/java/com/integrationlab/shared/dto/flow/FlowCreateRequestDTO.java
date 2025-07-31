package com.integrationlab.shared.dto.flow;

import java.util.List;

/**
 * DTO for FlowCreateRequestDTO.
 * Encapsulates data for transport between layers.
 */
public class FlowCreateRequestDTO {
    private String name;
    private String description;
    private String sourceAdapterId;
    private String targetAdapterId;
    private String sourceStructureId;
    private String targetStructureId;
    private String configuration;
    private String createdBy;
    private List<FlowTransformationDTO> transformations;

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

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<FlowTransformationDTO> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<FlowTransformationDTO> transformations) {
        this.transformations = transformations;
    }
}