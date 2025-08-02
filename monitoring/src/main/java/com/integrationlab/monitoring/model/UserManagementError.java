package com.integrationlab.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.integrationlab.data.model.SystemLog;

@Entity
@Table(name = "user_management_errors")
/**
 * Class UserManagementError - auto-generated documentation.
 */
public class UserManagementError {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "description")
    private String description;

    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "log_id", referencedColumnName = "id")
    private SystemLog log;

    // === Getters and Setters ===

    /**
     * Method: {()
     */
    public String getId() {
        return id;
    }

    /**
     * Method: {()
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method: {()
     */
    public String getAction() {
        return action;
    }

    /**
     * Method: {()
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Method: {()
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method: {()
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Method: {()
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Method: {()
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * Method: {()
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Method: {()
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Method: {()
     */
    public SystemLog getLog() {
        return log;
    }

    /**
     * Method: {()
     */
    public void setLog(SystemLog log) {
        this.log = log;
    }
}