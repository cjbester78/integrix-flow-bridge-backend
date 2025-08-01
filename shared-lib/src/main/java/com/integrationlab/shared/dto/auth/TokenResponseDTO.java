package com.integrationlab.shared.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for JWT token responses.
 * 
 * <p>Contains both access and refresh tokens with expiry information.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponseDTO {
    
    /**
     * JWT access token
     */
    private String accessToken;
    
    /**
     * Refresh token for obtaining new access tokens
     */
    private String refreshToken;
    
    /**
     * Token type (typically "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Access token expiry time in seconds
     */
    private Long expiresIn;
    
    /**
     * Refresh token expiry time in seconds
     */
    private Long refreshExpiresIn;
    
    /**
     * Access token expiration timestamp
     */
    private LocalDateTime accessTokenExpiresAt;
    
    /**
     * Refresh token expiration timestamp
     */
    private LocalDateTime refreshTokenExpiresAt;
    
    /**
     * User's username
     */
    private String username;
    
    /**
     * User's role
     */
    private String role;
}