package com.integrationlab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "reusable_java_functions")
/**
 * Entity representing ReusableJavaFunction.
 * This maps to the corresponding table in the database.
 */
public class ReusableFunction {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
    private String id;

    @Column(nullable = false, length = 255, unique = true)
    /** Name of the component */
    private String name;

    @Column(length = 50)
    private String version;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String functionBody;

    @Column(name = "input_types", columnDefinition = "json")
    private String inputTypes;  // JSON array string of input param types

    @Column(name = "output_type", length = 100)
    private String outputType;

    @Column(columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;

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

    // Getters and Setters
    
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
}
