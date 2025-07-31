package com.integrationlab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
/**
 * Entity representing SystemLog.
 * This maps to the corresponding table in the database.
 */
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
    private String id;

    private LocalDateTime timestamp;
    private String level;
    private String message;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String source;
    private String sourceId;
    private String sourceName; // Added
    private String component;
    private String componentId;

    private String domainType;

    @Column(columnDefinition = "char(36)")
    private String domainReferenceId;

    private String userId;

    @Column(name = "created_at")
    /** Timestamp of entity creation */
    private LocalDateTime createdAt;

    // === Getters and Setters ===

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
