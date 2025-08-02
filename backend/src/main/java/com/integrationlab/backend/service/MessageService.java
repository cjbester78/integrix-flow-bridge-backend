package com.integrationlab.backend.service;

import com.integrationlab.shared.dto.RecentMessageDTO;
import com.integrationlab.shared.dto.MessageDTO;
import com.integrationlab.shared.dto.MessageStatsDTO;
import com.integrationlab.data.model.SystemLog;
import com.integrationlab.data.repository.SystemLogRepository;
import com.integrationlab.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    @Autowired
    private SystemLogRepository logRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<RecentMessageDTO> getRecentMessages(String businessComponentId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        List<SystemLog> logs = businessComponentId != null
            ? logRepository.findByComponentId(businessComponentId, pageRequest)
            : logRepository.findAll(pageRequest).getContent();

        return logs.stream()
                .map(this::convertToRecentMessageDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get messages with filtering and pagination
     */
    public Map<String, Object> getMessages(Map<String, Object> filters, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<SystemLog> spec = buildSpecification(filters);
        
        Page<SystemLog> logPage = logRepository.findAll(spec, pageRequest);
        
        List<MessageDTO> messages = logPage.getContent().stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", logPage.getTotalElements());
        
        return result;
    }
    
    /**
     * Get a specific message by ID
     */
    public MessageDTO getMessageById(String id) {
        SystemLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        return convertToMessageDTO(log);
    }
    
    /**
     * Get message statistics
     */
    public MessageStatsDTO getMessageStats(Map<String, Object> filters) {
        Specification<SystemLog> spec = buildSpecification(filters);
        List<SystemLog> logs = logRepository.findAll(spec);
        
        long total = logs.size();
        long successful = logs.stream()
                .filter(log -> "SUCCESS".equals(log.getLevel().name()) || "INFO".equals(log.getLevel().name()))
                .count();
        long failed = logs.stream()
                .filter(log -> "ERROR".equals(log.getLevel().name()) || "FATAL".equals(log.getLevel().name()))
                .count();
        long processing = total - successful - failed;
        
        double successRate = total > 0 ? (double) successful / total * 100 : 0;
        
        // Calculate average processing time (dummy implementation for now)
        double avgProcessingTime = 250.0; // milliseconds
        
        return MessageStatsDTO.builder()
                .total(total)
                .successful(successful)
                .failed(failed)
                .processing(processing)
                .successRate(successRate)
                .avgProcessingTime(avgProcessingTime)
                .build();
    }
    
    /**
     * Reprocess a failed message
     */
    public MessageDTO reprocessMessage(String id) {
        SystemLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        
        // TODO: Implement actual reprocessing logic
        // For now, just return the message
        return convertToMessageDTO(log);
    }
    
    private Specification<SystemLog> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("status")) {
                List<String> statuses = (List<String>) filters.get("status");
                List<Predicate> statusPredicates = new ArrayList<>();
                for (String status : statuses) {
                    if ("success".equals(status)) {
                        statusPredicates.add(cb.or(
                                cb.equal(root.get("level"), "SUCCESS"),
                                cb.equal(root.get("level"), "INFO")
                        ));
                    } else if ("failed".equals(status)) {
                        statusPredicates.add(cb.or(
                                cb.equal(root.get("level"), "ERROR"),
                                cb.equal(root.get("level"), "FATAL")
                        ));
                    } else if ("processing".equals(status)) {
                        statusPredicates.add(cb.and(
                                cb.notEqual(root.get("level"), "SUCCESS"),
                                cb.notEqual(root.get("level"), "INFO"),
                                cb.notEqual(root.get("level"), "ERROR"),
                                cb.notEqual(root.get("level"), "FATAL")
                        ));
                    }
                }
                if (!statusPredicates.isEmpty()) {
                    predicates.add(cb.or(statusPredicates.toArray(new Predicate[0])));
                }
            }
            
            if (filters.containsKey("source")) {
                predicates.add(cb.equal(root.get("source"), filters.get("source")));
            }
            
            if (filters.containsKey("target")) {
                predicates.add(cb.equal(root.get("sourceName"), filters.get("target")));
            }
            
            if (filters.containsKey("type")) {
                predicates.add(cb.equal(root.get("category"), filters.get("type")));
            }
            
            if (filters.containsKey("dateFrom")) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), (LocalDateTime) filters.get("dateFrom")));
            }
            
            if (filters.containsKey("dateTo")) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), (LocalDateTime) filters.get("dateTo")));
            }
            
            if (filters.containsKey("search")) {
                String search = "%" + filters.get("search") + "%";
                predicates.add(cb.or(
                        cb.like(root.get("message"), search),
                        cb.like(root.get("source"), search),
                        cb.like(root.get("sourceName"), search)
                ));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private MessageDTO convertToMessageDTO(SystemLog log) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        // Create log entries
        List<MessageDTO.MessageLogDTO> logs = new ArrayList<>();
        logs.add(MessageDTO.MessageLogDTO.builder()
                .timestamp(log.getTimestamp())
                .level(log.getLevel().name())
                .message(log.getMessage())
                .build());
        
        return MessageDTO.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .source(log.getSource() != null ? log.getSource() : "System")
                .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                .type(log.getCategory() != null ? log.getCategory() : "INTEGRATION")
                .status(status)
                .processingTime("250ms") // Dummy value for now
                .size("1024 bytes") // Dummy value for now
                .businessComponentId(log.getComponentId())
                .logs(logs)
                .build();
    }

    private RecentMessageDTO convertToRecentMessageDTO(SystemLog log) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        return RecentMessageDTO.builder()
                .id(log.getId())
                .source(log.getSource() != null ? log.getSource() : "System")
                .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                .status(status)
                .time(log.getTimestamp().format(TIME_FORMATTER))
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