package com.integrationlab.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.shared.enums.AdapterType;
import com.integrationlab.adapters.core.AdapterMode;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

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

    @Column(columnDefinition = "json")
    private String configuration;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;
    
    @Column(name = "business_component_id")
    private String businessComponentId;

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

    public String getBusinessComponentId() {
        return businessComponentId;
    }

    public void setBusinessComponentId(String businessComponentId) {
        this.businessComponentId = businessComponentId;
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
