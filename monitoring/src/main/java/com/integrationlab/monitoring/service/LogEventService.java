package com.integrationlab.monitoring.service;

import com.integrationlab.monitoring.model.LogLevel;
import com.integrationlab.monitoring.model.LogSource;
import com.integrationlab.monitoring.model.LogDetailsType;

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