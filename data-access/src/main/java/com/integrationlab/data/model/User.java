package com.integrationlab.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a system user.
 * 
 * <p>This entity maps to the 'users' table and contains all user
 * authentication and profile information.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash", "emailVerificationToken", "passwordResetToken"})
public class User {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String id;

    /**
     * Unique username for authentication
     */
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * User email address
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    /**
     * Hashed password (BCrypt)
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    private String passwordHash;

    /**
     * User's first name
     */
    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    /**
     * User's last name
     */
    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    /**
     * Role ID reference
     */
    @Column(name = "role_id", columnDefinition = "char(36)")
    private String roleId;

    /**
     * User role (administrator, developer, integrator, viewer)
     */
    @Column(length = 50)
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(administrator|developer|integrator|viewer)$", 
             message = "Role must be administrator, developer, integrator, or viewer")
    private String role;

    /**
     * User account status (active, inactive, locked)
     */
    @Column(length = 50)
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(active|inactive|locked)$", 
             message = "Status must be active, inactive, or locked")
    @Builder.Default
    private String status = "active";

    /**
     * JSON string containing user permissions
     */
    @Column(columnDefinition = "json")
    private String permissions;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Timestamp of last successful login
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Whether email has been verified
     */
    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    /**
     * Token for email verification
     */
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    /**
     * Token for password reset
     */
    @Column(name = "password_reset_token")
    private String passwordResetToken;

    /**
     * Expiration time for password reset token
     */
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    /**
     * Lifecycle callback to ensure timestamps are set
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback to update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
