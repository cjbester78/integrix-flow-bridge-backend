package com.integrationlab.shared.dto.user;

/**
 * DTO for RegisterResponseDTO.
 * Encapsulates data for transport between layers.
 */
public class RegisterResponseDTO {
    private String message;
    private String userId;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}