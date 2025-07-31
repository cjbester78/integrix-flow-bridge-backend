package com.integrationlab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_components")
/**
 * Entity representing Business Component.
 * This maps to the corresponding table in the database.
 */
public class BusinessComponent {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
    private String id;

    @Column(nullable = false)
    /** Name of the component */
    private String name;

    @Column(columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;

    @Column(name = "contact_email")
    /** Contact email address */
    private String contactEmail;

    @Column(name = "contact_phone")
    /** Contact phone number */
    private String contactPhone;

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
        createdAt = LocalDateTime.now();
    /** Timestamp of last entity update */
        updatedAt = LocalDateTime.now();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactEmail() {
    /** Contact email address */
        return contactEmail;
    }

    /** Contact email address */
    public void setContactEmail(String contactEmail) {
    /** Contact email address */
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
    /** Contact phone number */
        return contactPhone;
    }

    /** Contact phone number */
    public void setContactPhone(String contactPhone) {
    /** Contact phone number */
        this.contactPhone = contactPhone;
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
