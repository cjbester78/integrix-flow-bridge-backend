package com.integrationlab.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.data.entity.SystemLog;
import com.integrationlab.monitoring.model.LogLevel;
import com.integrationlab.monitoring.model.LogSource;
import com.integrationlab.data.repository.SystemLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
/**
 * Class LogEventServiceImpl - auto-generated documentation.
 */
public class LogEventServiceImpl implements LogEventService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void logEvent(
        LogLevel level,
        LogSource source,
        String message,
        Object detailsJson,
        String domainType,
        String domainReferenceId,
        String userId
    ) {
        SystemLog log = new SystemLog();
        log.setTimestamp(LocalDateTime.now());
        log.setLevel(SystemLog.LogLevel.valueOf(level.name()));
        log.setSource(source.name());
        log.setMessage(message);
        log.setDomainType(domainType);
        log.setDomainReferenceId(domainReferenceId);
        log.setUserId(userId);
        log.setSourceName(source.name());

        try {
            if (detailsJson != null) {
                log.setDetails(objectMapper.writeValueAsString(detailsJson));
            }
        } catch (JsonProcessingException e) {
            log.setDetails("{\"error\": \"Failed to serialize log details\"}");
        }

        systemLogRepository.save(log);
    }

    @Override
    public void logEvent(
        LogLevel level,
        LogSource source,
        String message,
        Object detailsJson
    ) {
        logEvent(level, source, message, detailsJson, null, null, null);
    }

    @Override
    public void logError(
        String message,
        Throwable exception,
        String domainType,
        String domainReferenceId,
        String userId
    ) {
        logEvent(
            LogLevel.ERROR,
            LogSource.SYSTEM,
            message,
            exception != null ? exception.getMessage() : null,
            domainType,
            domainReferenceId,
            userId
        );
    }
}