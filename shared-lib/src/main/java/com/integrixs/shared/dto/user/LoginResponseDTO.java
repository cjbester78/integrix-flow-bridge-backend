package com.integrixs.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for successful login responses.
 * 
 * <p>Contains authentication token and user information after
 * successful authentication.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    
    /**
     * JWT authentication token
     */
    @NotBlank
    private String token;
    
    /**
     * Username of authenticated user
     */
    @NotBlank
    private String username;
    
    /**
     * User role (administrator, integrator, viewer)
     */
    private String role;
    
    /**
     * Token expiration time
     */
    private LocalDateTime expiresAt;
    
    /**
     * Token type (typically "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
}