package com.integrationlab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
/**
 * Entity representing User.
 * This maps to the corresponding table in the database.
 */
public class User {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
	private String id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	@Column(name = "role_id", columnDefinition = "char(36)")
	private String roleId;

	@Column(length = 50)
	private String role;

	@Column(length = 50)
	private String status;

	@Column(columnDefinition = "json")
	private String permissions;

	@Column(name = "created_at")
    /** Timestamp of entity creation */
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
    /** Timestamp of last entity update */
	private LocalDateTime updatedAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "email_verified")
	private boolean emailVerified;

	@Column(name = "email_verification_token")
	private String emailVerificationToken;

	@Column(name = "password_reset_token")
	private String passwordResetToken;

	@Column(name = "password_reset_expires_at")
	private LocalDateTime passwordResetExpiresAt;

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
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

	public String getEmailVerificationToken() {
		return emailVerificationToken;
	}

	public void setEmailVerificationToken(String emailVerificationToken) {
		this.emailVerificationToken = emailVerificationToken;
	}

	public String getPasswordResetToken() {
		return passwordResetToken;
	}

	public void setPasswordResetToken(String passwordResetToken) {
		this.passwordResetToken = passwordResetToken;
	}

	public LocalDateTime getPasswordResetExpiresAt() {
		return passwordResetExpiresAt;
	}

	public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
		this.passwordResetExpiresAt = passwordResetExpiresAt;
	}
}
