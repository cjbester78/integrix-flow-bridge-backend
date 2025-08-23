package com.integrixs.shared.exception;

/**
 * Exception thrown when authorization/permission checks fail.
 * 
 * <p>This exception covers authorization-related errors including:
 * <ul>
 *   <li>Insufficient permissions</li>
 *   <li>Role-based access violations</li>
 *   <li>Resource access denied</li>
 *   <li>Operation not permitted</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class AuthorizationException extends BaseIntegrationException {
    
    /**
     * Constructs a new authorization exception.
     * 
     * @param errorCode specific authorization error code
     * @param message human-readable error message
     */
    public AuthorizationException(String errorCode, String message) {
        super(errorCode, ErrorCategory.SECURITY, message);
    }
    
    /**
     * Constructs a new authorization exception with cause.
     * 
     * @param errorCode specific authorization error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public AuthorizationException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.SECURITY, message, cause);
    }
    
    /**
     * Creates an exception for insufficient permissions.
     * 
     * @param username the user attempting the action
     * @param requiredPermission the permission required
     * @param resource the resource being accessed
     * @return authorization exception
     */
    public static AuthorizationException insufficientPermissions(String username, 
                                                               String requiredPermission, 
                                                               String resource) {
        return new AuthorizationException(
            "AUTHZ_INSUFFICIENT_PERMISSIONS",
            String.format("User '%s' lacks required permission '%s' for resource '%s'", 
                        username, requiredPermission, resource)
        ).withContext("username", username)
         .withContext("requiredPermission", requiredPermission)
         .withContext("resource", resource);
    }
    
    /**
     * Creates an exception for role-based access denial.
     * 
     * @param username the user attempting the action
     * @param userRole current role of the user
     * @param requiredRole role required for the action
     * @return authorization exception
     */
    public static AuthorizationException roleAccessDenied(String username, String userRole, 
                                                        String requiredRole) {
        return new AuthorizationException(
            "AUTHZ_ROLE_ACCESS_DENIED",
            String.format("Access denied: user '%s' with role '%s' requires role '%s'", 
                        username, userRole, requiredRole)
        ).withContext("username", username)
         .withContext("userRole", userRole)
         .withContext("requiredRole", requiredRole);
    }
    
    /**
     * Creates an exception for resource access denial.
     * 
     * @param username the user attempting access
     * @param resourceType type of resource (flow, adapter, etc.)
     * @param resourceId ID of the resource
     * @return authorization exception
     */
    public static AuthorizationException resourceAccessDenied(String username, String resourceType, 
                                                            String resourceId) {
        return new AuthorizationException(
            "AUTHZ_RESOURCE_ACCESS_DENIED",
            String.format("User '%s' is not authorized to access %s '%s'", 
                        username, resourceType, resourceId)
        ).withContext("username", username)
         .withContext("resourceType", resourceType)
         .withContext("resourceId", resourceId);
    }
    
    /**
     * Creates an exception for operation not permitted.
     * 
     * @param username the user attempting the operation
     * @param operation the operation attempted (CREATE, UPDATE, DELETE, etc.)
     * @param resourceType type of resource
     * @return authorization exception
     */
    public static AuthorizationException operationNotPermitted(String username, String operation, 
                                                             String resourceType) {
        return new AuthorizationException(
            "AUTHZ_OPERATION_NOT_PERMITTED",
            String.format("User '%s' is not authorized to %s %s", 
                        username, operation, resourceType)
        ).withContext("username", username)
         .withContext("operation", operation)
         .withContext("resourceType", resourceType);
    }
    
    @Override
    public int getHttpStatusCode() {
        return 403; // Forbidden
    }
    
    @Override
    public AuthorizationException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}