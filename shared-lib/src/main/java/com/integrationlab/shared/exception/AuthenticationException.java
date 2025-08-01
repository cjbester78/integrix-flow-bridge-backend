package com.integrationlab.shared.exception;

/**
 * Exception thrown when authentication fails.
 * 
 * <p>This exception covers authentication-related errors including:
 * <ul>
 *   <li>Invalid credentials</li>
 *   <li>Expired tokens</li>
 *   <li>Account locked</li>
 *   <li>Missing authentication</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class AuthenticationException extends BaseIntegrationException {
    
    /**
     * Constructs a new authentication exception.
     * 
     * @param errorCode specific authentication error code
     * @param message human-readable error message
     */
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, ErrorCategory.SECURITY, message);
    }
    
    /**
     * Constructs a new authentication exception with cause.
     * 
     * @param errorCode specific authentication error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.SECURITY, message, cause);
    }
    
    /**
     * Creates an exception for invalid credentials.
     * 
     * @param username the username attempted
     * @return authentication exception
     */
    public static AuthenticationException invalidCredentials(String username) {
        return new AuthenticationException(
            "AUTH_INVALID_CREDENTIALS",
            "Invalid username or password"
        ).withContext("username", username);
    }
    
    /**
     * Creates an exception for expired token.
     * 
     * @param tokenType type of token (JWT, API key, etc.)
     * @return authentication exception
     */
    public static AuthenticationException tokenExpired(String tokenType) {
        return new AuthenticationException(
            "AUTH_TOKEN_EXPIRED",
            String.format("%s token has expired", tokenType)
        ).withContext("tokenType", tokenType);
    }
    
    /**
     * Creates an exception for invalid token.
     * 
     * @param tokenType type of token
     * @param reason specific reason for invalidity
     * @return authentication exception
     */
    public static AuthenticationException invalidToken(String tokenType, String reason) {
        return new AuthenticationException(
            "AUTH_INVALID_TOKEN",
            String.format("Invalid %s token: %s", tokenType, reason)
        ).withContext("tokenType", tokenType)
         .withContext("reason", reason);
    }
    
    /**
     * Creates an exception for locked account.
     * 
     * @param username the locked username
     * @param unlockTime when the account will be unlocked (optional)
     * @return authentication exception
     */
    public static AuthenticationException accountLocked(String username, String unlockTime) {
        AuthenticationException ex = new AuthenticationException(
            "AUTH_ACCOUNT_LOCKED",
            "Account is locked due to multiple failed login attempts"
        ).withContext("username", username);
        
        if (unlockTime != null) {
            ex.withContext("unlockTime", unlockTime);
        }
        
        return ex;
    }
    
    /**
     * Creates an exception for missing authentication.
     * 
     * @return authentication exception
     */
    public static AuthenticationException missingAuthentication() {
        return new AuthenticationException(
            "AUTH_MISSING",
            "Authentication required but not provided"
        );
    }
    
    /**
     * Creates an exception for disabled account.
     * 
     * @param username the disabled username
     * @return authentication exception
     */
    public static AuthenticationException accountDisabled(String username) {
        return new AuthenticationException(
            "AUTH_ACCOUNT_DISABLED",
            "Account has been disabled"
        ).withContext("username", username);
    }
    
    @Override
    public int getHttpStatusCode() {
        return 401; // Unauthorized
    }
    
    @Override
    public AuthenticationException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}