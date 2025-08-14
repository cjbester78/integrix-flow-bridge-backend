package com.integrixs.monitoring.service;



import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.SystemLog;
import com.integrixs.monitoring.model.UserManagementError;
import com.integrixs.monitoring.repository.UserManagementErrorRepository;
import com.integrixs.data.repository.SystemLogRepository;

@Service
/**
 * Class SystemLogServiceImpl - auto-generated documentation.
 */
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final UserManagementErrorRepository userManagementErrorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SystemLogServiceImpl(SystemLogRepository systemLogRepository,
                                UserManagementErrorRepository userManagementErrorRepository) {
        this.systemLogRepository = systemLogRepository;
        this.userManagementErrorRepository = userManagementErrorRepository;
    }

    @Override
    /**
     * Method: {()
     */
    public void log(SystemLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        systemLogRepository.save(log);
    }

    @Override
    /**
     * Method: {()
     */
    public void logUserManagementError(String action, String message, String detailsJson, String userId, String controller) {
        try {
            SystemLog log = new SystemLog();
            log.setLevel(SystemLog.LogLevel.ERROR);
            log.setMessage(message);
            log.setSource("api");
            log.setComponent(controller);
            log.setDomainType("UserManagement");
            log.setUserId(userId);
            log.setTimestamp(LocalDateTime.now());
            log.setCreatedAt(LocalDateTime.now());

            try {
                // Validate detailsJson
                objectMapper.readTree(detailsJson);
                log.setDetails(detailsJson);
            } catch (Exception jsonEx) {
                log.setDetails("{\"error\": \"Invalid JSON payload\"}");
            }

            log = systemLogRepository.save(log);

            UserManagementError error = new UserManagementError();
            error.setAction(action);
            error.setDescription(message);
            error.setPayload(detailsJson);
            error.setLog(log);
            error.setCreatedAt(LocalDateTime.now());

            userManagementErrorRepository.save(error);
        } catch (Exception e) {
            System.err.println("Failed to log user management error: " + e.getMessage());
            // DO NOT attempt recursive logging here!
        }
    }
    
    @Override
    public void logFlowActivity(String activity, String message, String flowId, String userId, String source) {
        SystemLog log = new SystemLog();
        log.setLevel(SystemLog.LogLevel.INFO);
        log.setMessage(message);
        log.setSource(source);
        log.setComponent("FlowEngine");
        log.setDomainType("FLOW");
        log.setDomainReferenceId(flowId);
        log.setUserId(userId);
        log.setTimestamp(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setDetails("{\"activity\": \"" + activity + "\"}");
        
        systemLogRepository.save(log);
    }
    
    @Override
    public void logFlowExecution(String flowId, String flowName, String status, Long flowVersion, 
                                long executionDuration, String correlationId, String userId, String source) {
        SystemLog log = new SystemLog();
        log.setLevel(status.equals("SUCCESS") ? SystemLog.LogLevel.INFO : SystemLog.LogLevel.ERROR);
        log.setMessage(String.format("Flow execution %s: %s", status.toLowerCase(), flowName));
        log.setSource(source);
        log.setComponent("FlowEngine");
        log.setDomainType("FLOW");
        log.setDomainReferenceId(flowId);
        log.setUserId(userId);
        log.setTimestamp(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        
        try {
            String details = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                put("status", status);
                put("duration_ms", executionDuration);
                put("flow_version", flowVersion);
                put("correlation_id", correlationId);
            }});
            log.setDetails(details);
        } catch (Exception e) {
            log.setDetails("{\"error\": \"Failed to serialize execution details\"}");
        }
        
        systemLogRepository.save(log);
    }
}