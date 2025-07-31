package com.integrationlab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.model.SystemLog;
import com.integrationlab.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LogService {

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void log(SystemLog log) {
        if (log.getId() == null) log.setId(UUID.randomUUID().toString());
        if (log.getTimestamp() == null) log.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        logRepository.save(log);
    }

    public void logUserManagementError(String action, String message, String detailsJson, String userId, String controller) {
        SystemLog log = new SystemLog();
        log.setId(UUID.randomUUID().toString());
        log.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        log.setLevel("ERROR");
        log.setMessage(message);
        log.setDetails(detailsJson);
        log.setUserId(userId);
        log.setSource(controller);
        log.setDomainType("UserManagement");
        log.setDomainReferenceId(action);
        logRepository.save(log);
    }

    public void logFlowExecutionSuccess(IntegrationFlow flow, String inputData, String outputData) {
        SystemLog log = new SystemLog();
        log.setId(UUID.randomUUID().toString());
        log.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        log.setLevel("INFO");
        log.setMessage("Flow executed successfully: " + flow.getName());
        log.setDomainType("INTEGRATION_FLOW");
        log.setDomainReferenceId(flow.getId());

        Map<String, Object> details = new HashMap<>();
        details.put("input", inputData);
        details.put("output", outputData);

        log.setDetails(toJson(details));
        logRepository.save(log);
    }

    public void logFlowExecutionError(IntegrationFlow flow, Exception e) {
        SystemLog log = new SystemLog();
        log.setId(UUID.randomUUID().toString());
        log.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        log.setLevel("ERROR");
        log.setMessage("Flow execution failed: " + flow.getName());
        log.setDomainType("INTEGRATION_FLOW");
        log.setDomainReferenceId(flow.getId());

        Map<String, Object> details = new HashMap<>();
        details.put("exception", e.getMessage());
        details.put("stackTrace", e.getStackTrace());

        log.setDetails(toJson(details));
        logRepository.save(log);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize details\"}";
        }
    }
}
