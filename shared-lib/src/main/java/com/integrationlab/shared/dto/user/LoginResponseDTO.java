package com.integrationlab.shared.dto.user;

/**
 * DTO for LoginResponseDTO.
 * Encapsulates data for transport between layers.
 */
public class LoginResponseDTO {
    private String token;
    private String username;

    public LoginResponseDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}