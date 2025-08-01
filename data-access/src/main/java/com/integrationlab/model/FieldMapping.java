package com.integrationlab.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "field_mappings")
/**
 * Entity representing FieldMapping.
 * This maps to the corresponding table in the database.
 */
public class FieldMapping {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformation_id", nullable = false)
    private FlowTransformation transformation;

    @Column(name = "source_fields", columnDefinition = "json", nullable = false)
    private String sourceFields;

    @Column(name = "target_field", length = 500, nullable = false)
    private String targetField;

    @Column(name = "java_function", columnDefinition = "TEXT")
    private String javaFunction;

    @Column(name = "mapping_rule", columnDefinition = "TEXT")
    private String mappingRule;

    // Support for hierarchical XML mapping
    @Column(name = "source_xpath", length = 1000)
    private String sourceXPath;

    @Column(name = "target_xpath", length = 1000)
    private String targetXPath;

    @Column(name = "is_array_mapping", nullable = false)
    private boolean isArrayMapping = false;

    @Column(name = "array_context_path", length = 500)
    private String arrayContextPath;

    @Column(name = "namespace_aware", nullable = false)
    private boolean namespaceAware = false;

    // New metadata fields
    @Column(name = "input_types", columnDefinition = "json")
    private String inputTypes;

    @Column(name = "output_type", length = 100)
    private String outputType;

    @Column(name = "description", columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;

    @Column(length = 50)
    private String version;

    @Column(name = "function_name", length = 255)
    private String functionName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    /** Timestamp of entity creation */
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    /** Timestamp of last entity update */
    private LocalDateTime updatedAt;

    /**
     * Automatically sets creation and update timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
    /** Timestamp of entity creation */
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates the timestamp before any update operation.
     */
    @PreUpdate
    protected void onUpdate() {
    /** Timestamp of last entity update */
        updatedAt = LocalDateTime.now();
    }

    // Convenience method to parse sourceFields JSON into List<String>
    @Transient
    public List<String> getParsedSourceFields() {
        if (sourceFields == null || sourceFields.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(sourceFields, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Convenience setter to set sourceFields from List<String>
    @Transient
    public void setParsedSourceFields(List<String> fields) {
        try {
            this.sourceFields = new ObjectMapper().writeValueAsString(fields);
        } catch (Exception e) {
            this.sourceFields = "[]";
        }
    }

    // Convenience method to parse inputTypes JSON into List<String>
    @Transient
    public List<String> getParsedInputTypes() {
        if (inputTypes == null || inputTypes.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(inputTypes, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Convenience setter to set inputTypes from List<String>
    @Transient
    public void setParsedInputTypes(List<String> types) {
        try {
            this.inputTypes = new ObjectMapper().writeValueAsString(types);
        } catch (Exception e) {
            this.inputTypes = "[]";
        }
    }

    // Getters and setters (unchanged)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowTransformation getTransformation() {
        return transformation;
    }

    public void setTransformation(FlowTransformation transformation) {
        this.transformation = transformation;
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
    /** Timestamp of entity creation */
        return createdAt;
    }

    /** Timestamp of entity creation */
    public void setCreatedAt(LocalDateTime createdAt) {
    /** Timestamp of entity creation */
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
    /** Timestamp of last entity update */
        return updatedAt;
    }

    /** Timestamp of last entity update */
    public void setUpdatedAt(LocalDateTime updatedAt) {
    /** Timestamp of last entity update */
        this.updatedAt = updatedAt;
    }

    public String getSourceXPath() {
        return sourceXPath;
    }

    public void setSourceXPath(String sourceXPath) {
        this.sourceXPath = sourceXPath;
    }

    public String getTargetXPath() {
        return targetXPath;
    }

    public void setTargetXPath(String targetXPath) {
        this.targetXPath = targetXPath;
    }

    public boolean isArrayMapping() {
        return isArrayMapping;
    }

    public void setArrayMapping(boolean arrayMapping) {
        isArrayMapping = arrayMapping;
    }

    public String getArrayContextPath() {
        return arrayContextPath;
    }

    public void setArrayContextPath(String arrayContextPath) {
        this.arrayContextPath = arrayContextPath;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    @Override
    public String toString() {
        return "FieldMapping{" +
                "id='" + id + '\'' +
                ", transformation=" + (transformation != null ? transformation.getId() : "null") +
                ", sourceFields=" + sourceFields +
                ", targetField='" + targetField + '\'' +
                ", javaFunction='" + (javaFunction != null ? "[javaFunction]" : "null") + '\'' +
                ", mappingRule='" + (mappingRule != null ? "[mappingRule]" : "null") + '\'' +
                ", inputTypes=" + inputTypes +
                ", outputType='" + outputType + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", functionName='" + functionName + '\'' +
                ", isActive=" + isActive +
    /** Timestamp of entity creation */
                ", createdAt=" + createdAt +
    /** Timestamp of last entity update */
                ", updatedAt=" + updatedAt +
                '}';
    }
}
