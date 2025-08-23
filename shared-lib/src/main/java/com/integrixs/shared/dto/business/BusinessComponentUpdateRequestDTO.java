package com.integrixs.shared.dto.business;

/**
 * DTO for BusinessComponentUpdateRequestDTO.
 * Encapsulates data for transport between layers.
 */
public class BusinessComponentUpdateRequestDTO {
    private String name;
    private String description;
    private String contactEmail;
    private String contactPhone;

    // Getters and Setters
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
        return contactEmail;
    }
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    public String getContactPhone() {
        return contactPhone;
    }
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}