package com.integrationlab.monitoring.service;

import com.integrationlab.data.model.SystemLog;

/**
 * Interface SystemLogService - auto-generated documentation.
 */
public interface SystemLogService {
    void log(SystemLog log);

    void logUserManagementError(String action, String message, String detailsJson, String userId, String controller);
    
    void logFlowActivity(String activity, String message, String flowId, String userId, String source);
    
    void logFlowExecution(String flowId, String flowName, String status, Long flowVersion, 
                         long executionDuration, String correlationId, String userId, String source);
}