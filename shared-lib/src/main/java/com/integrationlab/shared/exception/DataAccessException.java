package com.integrationlab.shared.exception;

/**
 * Exception thrown when data access operations fail.
 * 
 * <p>This exception covers database and persistence-related errors including:
 * <ul>
 *   <li>SQL execution failures</li>
 *   <li>Entity not found errors</li>
 *   <li>Constraint violations</li>
 *   <li>Transaction failures</li>
 *   <li>Connection pool exhaustion</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class DataAccessException extends BaseIntegrationException {
    
    /**
     * Constructs a new data access exception.
     * 
     * @param errorCode specific data access error code
     * @param message human-readable error message
     */
    public DataAccessException(String errorCode, String message) {
        super(errorCode, ErrorCategory.DATA_ACCESS, message);
    }
    
    /**
     * Constructs a new data access exception with cause.
     * 
     * @param errorCode specific data access error code
     * @param message human-readable error message
     * @param cause the underlying cause
     */
    public DataAccessException(String errorCode, String message, Throwable cause) {
        super(errorCode, ErrorCategory.DATA_ACCESS, message, cause);
    }
    
    /**
     * Creates an exception for entity not found.
     * 
     * @param entityType type of entity (User, Flow, Adapter, etc.)
     * @param id ID of the entity
     * @return data access exception
     */
    public static DataAccessException entityNotFound(String entityType, Object id) {
        return new DataAccessException(
            "DATA_ENTITY_NOT_FOUND",
            String.format("%s with ID '%s' not found", entityType, id)
        ).withContext("entityType", entityType)
         .withContext("id", id);
    }
    
    /**
     * Creates an exception for unique constraint violation.
     * 
     * @param entityType type of entity
     * @param fieldName field that violated uniqueness
     * @param value the duplicate value
     * @return data access exception
     */
    public static DataAccessException uniqueConstraintViolation(String entityType, String fieldName, 
                                                              Object value) {
        return new DataAccessException(
            "DATA_UNIQUE_CONSTRAINT",
            String.format("%s with %s '%s' already exists", entityType, fieldName, value)
        ).withContext("entityType", entityType)
         .withContext("fieldName", fieldName)
         .withContext("value", value);
    }
    
    /**
     * Creates an exception for foreign key constraint violation.
     * 
     * @param entityType type of entity
     * @param operation operation that failed (INSERT, UPDATE, DELETE)
     * @param referencedEntity entity referenced by foreign key
     * @return data access exception
     */
    public static DataAccessException foreignKeyViolation(String entityType, String operation, 
                                                        String referencedEntity) {
        return new DataAccessException(
            "DATA_FOREIGN_KEY_VIOLATION",
            String.format("Cannot %s %s due to foreign key constraint with %s", 
                        operation.toLowerCase(), entityType, referencedEntity)
        ).withContext("entityType", entityType)
         .withContext("operation", operation)
         .withContext("referencedEntity", referencedEntity);
    }
    
    /**
     * Creates an exception for SQL execution failure.
     * 
     * @param operation SQL operation (SELECT, INSERT, UPDATE, DELETE)
     * @param sqlError specific SQL error message
     * @param cause underlying SQL exception
     * @return data access exception
     */
    public static DataAccessException sqlExecutionFailed(String operation, String sqlError, 
                                                       Throwable cause) {
        return new DataAccessException(
            "DATA_SQL_EXECUTION_FAILED",
            String.format("SQL %s operation failed: %s", operation, sqlError),
            cause
        ).withContext("operation", operation)
         .withContext("sqlError", sqlError);
    }
    
    /**
     * Creates an exception for transaction failure.
     * 
     * @param transactionId transaction identifier
     * @param reason failure reason
     * @param cause underlying cause
     * @return data access exception
     */
    public static DataAccessException transactionFailed(String transactionId, String reason, 
                                                      Throwable cause) {
        return new DataAccessException(
            "DATA_TRANSACTION_FAILED",
            String.format("Transaction failed: %s", reason),
            cause
        ).withContext("transactionId", transactionId)
         .withContext("reason", reason);
    }
    
    /**
     * Creates an exception for connection pool exhaustion.
     * 
     * @param poolName name of the connection pool
     * @param maxSize maximum pool size
     * @return data access exception
     */
    public static DataAccessException connectionPoolExhausted(String poolName, int maxSize) {
        return new DataAccessException(
            "DATA_CONNECTION_POOL_EXHAUSTED",
            String.format("Connection pool '%s' exhausted (max size: %d)", poolName, maxSize)
        ).withContext("poolName", poolName)
         .withContext("maxSize", maxSize);
    }
    
    @Override
    public int getHttpStatusCode() {
        if ("DATA_ENTITY_NOT_FOUND".equals(getErrorCode())) {
            return 404; // Not Found
        } else if (getErrorCode().contains("CONSTRAINT")) {
            return 409; // Conflict
        } else if ("DATA_CONNECTION_POOL_EXHAUSTED".equals(getErrorCode())) {
            return 503; // Service Unavailable
        }
        return 500; // Internal Server Error
    }
    
    @Override
    public boolean isRetryable() {
        // Connection pool exhaustion and some transaction failures might be retryable
        return "DATA_CONNECTION_POOL_EXHAUSTED".equals(getErrorCode()) ||
               "DATA_TRANSACTION_FAILED".equals(getErrorCode());
    }
    
    @Override
    public DataAccessException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
}