package com.integrationlab.monitoring.service;



import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.model.SystemLog;
import com.integrationlab.monitoring.model.UserManagementError;
import com.integrationlab.monitoring.repository.UserManagementErrorRepository;
import com.integrationlab.repository.SystemLogRepository;

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
            log.setLevel("ERROR");
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
}