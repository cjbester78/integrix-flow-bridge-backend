package com.integrationlab.shared.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token requests.
 * 
 * <p>Used to request a new access token using a refresh token.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    
    /**
     * The refresh token
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}