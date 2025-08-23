package com.integrixs.shared.dto.system;

import java.time.LocalDateTime;

/**
 * DTO for SystemSettingDTO.
 * Encapsulates data for transport between layers.
 */
public class SystemSettingDTO {

    private String id;
    private String settingKey;
    private String settingValue;
    private String description;
    private String category;
    private String dataType;
    private boolean isEncrypted;
    private boolean isReadonly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public SystemSettingDTO() {
        // No-args constructor
    }

    public SystemSettingDTO(String settingKey, String settingValue, String description, String category) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.category = category;
        this.dataType = "STRING";
        this.isEncrypted = false;
        this.isReadonly = false;
    }

    // === Getters and Setters ===

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean readonly) {
        isReadonly = readonly;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "SystemSettingDTO{" +
                "id='" + id + '\'' +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + (isEncrypted ? "[ENCRYPTED]" : settingValue) + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isEncrypted=" + isEncrypted +
                ", isReadonly=" + isReadonly +
                '}';
    }
}