package com.integrationlab.monitoring.service;

import com.integrationlab.model.SystemLog;

/**
 * Interface SystemLogService - auto-generated documentation.
 */
public interface SystemLogService {
    void log(SystemLog log);

    void logUserManagementError(String action, String message, String detailsJson, String userId, String controller);
}