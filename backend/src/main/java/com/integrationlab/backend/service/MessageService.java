package com.integrationlab.backend.service;

import com.integrationlab.shared.dto.RecentMessageDTO;
import com.integrationlab.data.entity.SystemLog;
import com.integrationlab.data.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private SystemLogRepository logRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<RecentMessageDTO> getRecentMessages(String businessComponentId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<SystemLog> logs = businessComponentId != null
            ? logRepository.findByComponentId(businessComponentId, pageRequest)
            : logRepository.findAll(pageRequest).getContent();

        return logs.stream()
                .map(this::convertToRecentMessageDTO)
                .collect(Collectors.toList());
    }

    private RecentMessageDTO convertToRecentMessageDTO(SystemLog log) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        return RecentMessageDTO.builder()
                .id(log.getId())
                .source(log.getSource() != null ? log.getSource() : "System")
                .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                .status(status)
                .time(log.getCreatedAt().format(TIME_FORMATTER))
                .businessComponentId(log.getComponentId())
                .build();
    }

    private String mapLogLevelToStatus(String logLevel) {
        if (logLevel == null) return "processing";
        
        switch (logLevel.toUpperCase()) {
            case "ERROR":
            case "FATAL":
                return "failed";
            case "SUCCESS":
            case "INFO":
                return "success";
            default:
                return "processing";
        }
    }
}