package com.integrationlab.shared.dto.user;

/**
 * DTO for LoginRequestDTO.
 * Encapsulates data for transport between layers.
 */
public class LoginRequestDTO {
    private String username;
    private String password;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}