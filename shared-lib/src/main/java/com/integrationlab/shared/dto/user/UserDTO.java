package com.integrationlab.shared.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for User information.
 * 
 * <p>This DTO is used to transfer user data between different layers of the application,
 * particularly between the REST API and service layers. It includes all user attributes
 * including authentication status, permissions, and timestamps.</p>
 * 
 * @author Integration Team
 * @since 1.0.0
 * @see com.integrationlab.model.User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    /**
     * Unique identifier for the user (UUID format).
     */
    private String id;

    /**
     * Unique username for authentication.
     * Must be between 3-50 characters and contain only alphanumeric characters and underscores.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * User's email address.
     * Used for notifications and password recovery.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User's first name.
     */
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * User's role identifier.
     * References the role that determines user permissions.
     */
    @NotNull(message = "Role is required")
    private String role;

    /**
     * Current status of the user account.
     * Possible values: active, inactive, pending, locked
     */
    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(active|inactive|pending|locked)$", message = "Status must be one of: active, inactive, pending, locked")
    private String status;

    /**
     * Indicates whether the user's email address has been verified.
     */
    private boolean emailVerified;

    /**
     * Timestamp when the user account was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Timestamp of the user's last successful login.
     */
    private LocalDateTime lastLoginAt;

    /**
     * Map of user permissions organized by resource.
     * Key: resource name (e.g., "flows", "adapters")
     * Value: array of allowed actions (e.g., ["read", "write", "delete"])
     */
    private Map<String, String[]> permissions;
} 