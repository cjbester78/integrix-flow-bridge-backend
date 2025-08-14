package com.integrixs.data.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.adapters.core.AdapterMode;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "communication_adapters")
/**
 * Entity representing CommunicationAdapter.
 * This maps to the corresponding table in the database.
 */
public class CommunicationAdapter {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
    private String id;

    @Column(nullable = false)
    /** Name of the component */
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdapterType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AdapterMode mode; // SENDER or RECEIVER
    
    @Column(length = 20)
    private String direction; // INBOUND, OUTBOUND, BIDIRECTIONAL

    @Column(columnDefinition = "json")
    private String configuration;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // === Getters and Setters ===

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

    public AdapterType getType() {
        return type;
    }

    public void setType(AdapterType type) {
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AdapterMode getMode() {
        return mode;
    }

    public void setMode(AdapterMode mode) {
        this.mode = mode;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getBusinessComponentId() {
        return businessComponent != null ? businessComponent.getId() : null;
    }

    public void setBusinessComponentId(String businessComponentId) {
        // This setter is kept for backward compatibility
        // In production code, use setBusinessComponent() instead
    }
    
    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }
    
    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    // === Generic Config Deserialization Method ===

    public <T> T getConfig(Class<T> configClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.configuration, configClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse adapter configuration for " + this.name, e);
        }
    }
}
