package com.integrixs.shared.exception;

/**
 * Exception thrown during flow execution and orchestration.
 * 
 * <p>This exception covers flow-related errors including:
 * <ul>
 *   <li>Flow execution failures</li>
 *   <li>Step execution errors</li>
 *   <li>Flow state violations</li>
 *   <li>Message processing errors</li>
 *   <li>Transformation failures during flow execution</li>
 * </ul>
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowExecutionException extends BaseIntegrationException {
    
    private final String flowId;
    private final String stepId;
    
    /**
     * Constructs a new flow execution exception.
     * 
     * @param errorCode specific flow error code
     * @param message human-readable error message
     * @param flowId ID of the flow that failed
     */
    public FlowExecutionException(String errorCode, String message, String flowId) {
        super(errorCode, ErrorCategory.FLOW, message);
        this.flowId = flowId;
        this.stepId = null;
        withContext("flowId", flowId);
    }
    
    /**
     * Constructs a new flow execution exception for a specific step.
     * 
     * @param errorCode specific flow error code
     * @param message human-readable error message
     * @param flowId ID of the flow that failed
     * @param stepId ID of the step that failed
     */
    public FlowExecutionException(String errorCode, String message, String flowId, String stepId) {
        super(errorCode, ErrorCategory.FLOW, message);
        this.flowId = flowId;
        this.stepId = stepId;
        withContext("flowId", flowId);
        withContext("stepId", stepId);
    }
    
    /**
     * Constructs a new flow execution exception with cause.
     * 
     * @param errorCode specific flow error code
     * @param message human-readable error message
     * @param flowId ID of the flow that failed
     * @param cause the underlying cause
     */
    public FlowExecutionException(String errorCode, String message, String flowId, Throwable cause) {
        super(errorCode, ErrorCategory.FLOW, message, cause);
        this.flowId = flowId;
        this.stepId = null;
        withContext("flowId", flowId);
    }
    
    /**
     * Creates a flow not found exception.
     * 
     * @param flowId ID of the flow
     * @return flow execution exception
     */
    public static FlowExecutionException flowNotFound(String flowId) {
        return new FlowExecutionException(
            "FLOW_NOT_FOUND",
            String.format("Flow with ID '%s' not found", flowId),
            flowId
        );
    }
    
    /**
     * Creates a flow state violation exception.
     * 
     * @param flowId ID of the flow
     * @param currentState current state of the flow
     * @param attemptedAction action that was attempted
     * @return flow execution exception
     */
    public static FlowExecutionException invalidFlowState(String flowId, String currentState, 
                                                         String attemptedAction) {
        return new FlowExecutionException(
            "FLOW_INVALID_STATE",
            String.format("Cannot %s flow in %s state", attemptedAction, currentState),
            flowId
        ).withContext("currentState", currentState)
         .withContext("attemptedAction", attemptedAction);
    }
    
    /**
     * Creates a step execution failure exception.
     * 
     * @param flowId ID of the flow
     * @param stepId ID of the step
     * @param stepName name of the step
     * @param reason failure reason
     * @param cause underlying cause
     * @return flow execution exception
     */
    public static FlowExecutionException stepExecutionFailed(String flowId, String stepId, 
                                                           String stepName, String reason, 
                                                           Throwable cause) {
        return new FlowExecutionException(
            "FLOW_STEP_FAILED",
            String.format("Step '%s' failed: %s", stepName, reason),
            flowId,
            stepId
        ).withContext("stepName", stepName)
         .withContext("reason", reason);
    }
    
    /**
     * Creates a message processing exception.
     * 
     * @param flowId ID of the flow
     * @param messageId ID of the message
     * @param reason processing failure reason
     * @return flow execution exception
     */
    public static FlowExecutionException messageProcessingFailed(String flowId, String messageId, 
                                                               String reason) {
        return new FlowExecutionException(
            "FLOW_MESSAGE_PROCESSING_FAILED",
            String.format("Failed to process message: %s", reason),
            flowId
        ).withContext("messageId", messageId)
         .withContext("reason", reason);
    }
    
    /**
     * Creates a flow timeout exception.
     * 
     * @param flowId ID of the flow
     * @param timeoutMs timeout in milliseconds
     * @return flow execution exception
     */
    public static FlowExecutionException flowTimeout(String flowId, long timeoutMs) {
        return new FlowExecutionException(
            "FLOW_TIMEOUT",
            String.format("Flow execution timed out after %d ms", timeoutMs),
            flowId
        ).withContext("timeoutMs", timeoutMs);
    }
    
    @Override
    public int getHttpStatusCode() {
        if ("FLOW_NOT_FOUND".equals(getErrorCode())) {
            return 404; // Not Found
        } else if (getErrorCode().contains("STATE")) {
            return 409; // Conflict
        } else if (getErrorCode().contains("TIMEOUT")) {
            return 504; // Gateway Timeout
        }
        return 500; // Internal Server Error
    }
    
    @Override
    public boolean isRetryable() {
        // Timeout and some processing errors might be retryable
        return getErrorCode().contains("TIMEOUT") || 
               getErrorCode().equals("FLOW_MESSAGE_PROCESSING_FAILED");
    }
    
    @Override
    public FlowExecutionException withContext(String key, Object value) {
        super.withContext(key, value);
        return this;
    }
    
    public String getFlowId() {
        return flowId;
    }
    
    public String getStepId() {
        return stepId;
    }
}