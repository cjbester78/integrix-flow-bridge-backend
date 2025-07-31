package com.integrationlab.shared.dto.transformation;

import java.time.LocalDateTime;

/**
 * DTO for ReusableJavaFunctionDTO.
 * Encapsulates data for transport between layers.
 */
public class ReusableJavaFunctionDTO {

    private String id;
    private String name;
    private String version;
    private String functionBody;
    private String inputTypes;
    private String outputType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReusableJavaFunctionDTO() {
        // No-args constructor
    }

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFunctionBody() {
        return functionBody;
    }

    public void setFunctionBody(String functionBody) {
        this.functionBody = functionBody;
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
        return "ReusableJavaFunctionDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", functionBody='" + (functionBody != null ? "[functionBody]" : "null") + '\'' +
                ", inputTypes='" + inputTypes + '\'' +
                ", outputType='" + outputType + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}