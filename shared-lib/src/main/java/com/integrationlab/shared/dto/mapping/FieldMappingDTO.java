package com.integrationlab.shared.dto.mapping;

import java.time.LocalDateTime;

/**
 * DTO for FieldMappingDTO.
 * Encapsulates data for transport between layers.
 */
public class FieldMappingDTO {

    private String id;
    private String transformationId;
    private String sourceFields;
    private String targetField;
    private String javaFunction;
    private String mappingRule;

    // New metadata fields
    private String inputTypes;   // JSON string representing list of input types
    private String outputType;
    private String description;
    private String version;
    private String functionName;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FieldMappingDTO() {
        // no-args constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(String transformationId) {
        this.transformationId = transformationId;
    }

    public String getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getJavaFunction() {
        return javaFunction;
    }

    public void setJavaFunction(String javaFunction) {
        this.javaFunction = javaFunction;
    }

    public String getMappingRule() {
        return mappingRule;
    }

    public void setMappingRule(String mappingRule) {
        this.mappingRule = mappingRule;
    }

    public String getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(String inputTypes) {
        this.inputTypes = inputTypes;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
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

    @Override
    public String toString() {
        return "FieldMappingDTO{" +
                "id='" + id + '\'' +
                ", transformationId='" + transformationId + '\'' +
                ", sourceFields='" + sourceFields + '\'' +
                ", targetField='" + targetField + '\'' +
                ", javaFunction='" + (javaFunction != null ? "[function]" : "null") + '\'' +
                ", mappingRule='" + (mappingRule != null ? "[rule]" : "null") + '\'' +
                ", inputTypes='" + inputTypes + '\'' +
                ", outputType='" + outputType + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", functionName='" + functionName + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}