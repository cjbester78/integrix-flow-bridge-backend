package com.integrationlab.shared.exception;

import java.util.Map;

/**
 * Exception thrown when adapter operations fail.
 * 
 * <p>This exception covers all adapter-related errors including:
 * <ul>
 *   <li>Connection failures</li>
 *   <li>Authentication errors</li>
 *   <li>Configuration issues</li>
 *   <li>Data transmission errors</li>
 *   <li>Timeout errors</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class AdapterException extends BaseIntegrationException {
    
    private final String adapterType;
    private final String adapterId;
    
    /**
     * Constructs a new adapter exception.
     * 
     * @param errorCode specific adapter error code
     * @param message human-readable error message
     * @param adapterType type of adapter (HTTP, JDBC, etc.)
     * @param adapterId adapter instance ID
     */
    public AdapterException(String errorCode, String message, String adapterType, String adapterId) {
        super(errorCode, ErrorCategory.ADAPTER, message);
        this.adapterType = adapterType;
        this.adapterId = adapterId;
        withContext("adapterType", adapterType);
        withContext("adapterId", adapterId);
    }
    
    /**
     * Constructs a new adapter exception with cause.
     * 
     * @param errorCode specific adapter error code
     * @param message human-readable error message
     * @param adapterType type of adapter
     * @param adapterId adapter instance ID
     * @param cause the underlying cause
     */
    public AdapterException(String errorCode, String message, String adapterType, 
                          String adapterId, Throwable cause) {
        super(errorCode, ErrorCategory.ADAPTER, message, cause);
        this.adapterType = adapterType;
        this.adapterId = adapterId;
        withContext("adapterType", adapterType);
        withContext("adapterId", adapterId);
    }
    
    /**
     * Creates an adapter connection exception.
     * 
     * @param adapterType type of adapter
     * @param adapterId adapter instance ID
     * @param endpoint connection endpoint
     * @param cause underlying connection error
     * @return adapter exception
     */
    public static AdapterException connectionFailed(String adapterType, String adapterId, 
                                                  String endpoint, Throwable cause) {
        return new AdapterException(
            "ADAPTER_CONNECTION_FAILED",
            String.format("Failed to connect to %s endpoint: %s", adapterType, endpoint),
            adapterType,
            adapterId,
            cause
        ).withContext("endpoint", endpoint);
    }
    
    /**
     * Creates an adapter timeout exception.
     * 
     * @param adapterType type of adapter
     * @param adapterId adapter instance ID
     * @param timeoutMs timeout in milliseconds
     * @return adapter exception
     */
    public static AdapterException timeout(String adapterType, String adapterId, long timeoutMs) {
        return new AdapterException(
            "ADAPTER_TIMEOUT",
            String.format("%s adapter timed out after %d ms", adapterType, timeoutMs),
            adapterType,
            adapterId
        ).withContext("timeoutMs", timeoutMs);
    }
    
    /**
     * Creates an adapter authentication exception.
     * 
     * @param adapterType type of adapter
     * @param adapterId adapter instance ID
     * @param authMethod authentication method used
     * @return adapter exception
     */
    public static AdapterException authenticationFailed(String adapterType, String adapterId, 
                                                      String authMethod) {
        return new AdapterException(
            "ADAPTER_AUTH_FAILED",
            String.format("Authentication failed for %s adapter using %s", adapterType, authMethod),
            adapterType,
            adapterId
        ).withContext("authMethod", authMethod);
    }
    
    /**
     * Creates an adapter configuration exception.
     * 
     * @param adapterType type of adapter
     * @param adapterId adapter instance ID
     * @param configError specific configuration error
     * @return adapter exception
     */
    public static AdapterException invalidConfiguration(String adapterType, String adapterId, 
                                                      String configError) {
        return new AdapterException(
            "ADAPTER_INVALID_CONFIG",
            String.format("Invalid configuration for %s adapter: %s", adapterType, configError),
            adapterType,
            adapterId
        ).withContext("configError", configError);
    }
    
    @Override
    public int getHttpStatusCode() {
        // Return 503 for connection issues, 401 for auth, 400 for config
        if (getErrorCode().contains("CONNECTION") || getErrorCode().contains("TIMEOUT")) {
            return 503; // Service Unavailable
        } else if (getErrorCode().contains("AUTH")) {
            return 401; // Unauthorized
        }
        return 400; // Bad Request
    }
    
    @Override
    public boolean isRetryable() {
        // Connection and timeout errors are typically retryable
        return getErrorCode().contains("CONNECTION") || getErrorCode().contains("TIMEOUT");
    }
    
    @Override
    public AdapterException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
    
    public String getAdapterType() {
        return adapterType;
    }
    
    public String getAdapterId() {
        return adapterId;
    }
}