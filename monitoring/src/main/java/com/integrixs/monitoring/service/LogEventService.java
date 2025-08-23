package com.integrixs.monitoring.service;

import com.integrixs.monitoring.model.LogLevel;
import com.integrixs.monitoring.model.LogSource;
import com.integrixs.monitoring.model.LogDetailsType;

/**
 * Interface LogEventService - auto-generated documentation.
 */
public interface LogEventService {
    void logEvent(
        LogLevel level,
        LogSource source,
        String message,
        Object detailsJson,
        String domainType,
        String domainReferenceId,
        String userId
    );

    void logEvent(
        LogLevel level,
        LogSource source,
        String message,
        Object detailsJson
    );

    void logError(
        String message,
        Throwable exception,
        String domainType,
        String domainReferenceId,
        String userId
    );
}