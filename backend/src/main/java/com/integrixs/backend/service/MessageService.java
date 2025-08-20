package com.integrixs.backend.service;

import com.integrixs.shared.dto.RecentMessageDTO;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.dto.MessageStatsDTO;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.model.AdapterPayload;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.repository.AdapterPayloadRepository;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog.LogLevel;
import com.integrixs.data.repository.IntegrationFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.UUID;

@Service
public class MessageService {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private SystemLogRepository logRepository;
    
    @Autowired
    private AdapterPayloadRepository payloadRepository;
    
    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SystemConfigurationService systemConfigurationService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Map<String, Object> getMessages(Map<String, Object> filters, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<SystemLog> spec = buildSpecification(filters);
        
        // Get all logs matching the filters
        Page<SystemLog> logPage = logRepository.findAll(spec, pageRequest);
        
        // Group by correlation ID to find main flow messages and their adapter activities
        Map<String, List<SystemLog>> logsByCorrelation = new HashMap<>();
        List<SystemLog> mainFlowLogs = new ArrayList<>();
        
        for (SystemLog log : logPage.getContent()) {
            if (log.getCorrelationId() != null) {
                // Group by correlation ID
                logsByCorrelation.computeIfAbsent(log.getCorrelationId(), k -> new ArrayList<>()).add(log);
                
                // Identify main flow execution logs
                if ("FLOW_EXECUTION".equals(log.getCategory()) || 
                    "IntegrationFlow".equals(log.getDomainType())) {
                    mainFlowLogs.add(log);
                }
            } else if ("FLOW_EXECUTION".equals(log.getCategory()) || 
                       "IntegrationFlow".equals(log.getDomainType())) {
                // Flow logs without correlation ID
                mainFlowLogs.add(log);
            }
        }
        
        // Convert main flow logs to DTOs and include related adapter logs
        List<MessageDTO> messages = mainFlowLogs.stream()
                .map(log -> convertToMessageDTOWithAdapterLogs(log, logsByCorrelation))
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", mainFlowLogs.size());
        
        return result;
    }
    
    /**
     * Get a specific message by ID
     */
    @Transactional(readOnly = true)
    public MessageDTO getMessageById(String id) {
        SystemLog log = logRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        return convertToMessageDTO(log);
    }
    
    /**
     * Get message statistics
     */
    @Transactional(readOnly = true)
    public MessageStatsDTO getMessageStats(Map<String, Object> filters) {
        logger.debug("Getting message stats with filters: {}", filters);
        Specification<SystemLog> spec = buildSpecification(filters);
        List<SystemLog> logs = logRepository.findAll(spec);
        
        // Filter to only count unique flow executions (not adapter logs)
        Set<String> countedFlows = new HashSet<>();
        long total = 0;
        long successful = 0;
        long failed = 0;
        
        for (SystemLog log : logs) {
            // Only count main flow execution logs for statistics
            if ("FLOW_EXECUTION".equals(log.getCategory()) || 
                "IntegrationFlow".equals(log.getDomainType())) {
                
                // Avoid counting the same flow multiple times
                String flowKey = log.getCorrelationId() != null ? log.getCorrelationId() : log.getId().toString();
                if (!countedFlows.contains(flowKey)) {
                    countedFlows.add(flowKey);
                    total++;
                    
                    if ("SUCCESS".equals(log.getLevel().name()) || "INFO".equals(log.getLevel().name())) {
                        successful++;
                    } else if ("ERROR".equals(log.getLevel().name()) || "FATAL".equals(log.getLevel().name())) {
                        failed++;
                    }
                }
            }
        }
        
        long processing = total - successful - failed;
        double successRate = total > 0 ? (double) successful / total * 100 : 0;
        
        // Calculate average processing time from flow execution details
        double avgProcessingTime = 250.0; // Default
        List<Long> processingTimes = new ArrayList<>();
        
        for (SystemLog log : logs) {
            if (("FLOW_EXECUTION".equals(log.getCategory()) || "IntegrationFlow".equals(log.getDomainType())) 
                && log.getDetails() != null) {
                try {
                    JsonNode details = objectMapper.readTree(log.getDetails());
                    if (details.has("durationMs")) {
                        processingTimes.add(details.get("durationMs").asLong());
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
        
        if (!processingTimes.isEmpty()) {
            avgProcessingTime = processingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(250.0);
        }
        
        logger.debug("Message stats - total: {}, successful: {}, failed: {}, processing: {}", 
                    total, successful, failed, processing);
        
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
    @Transactional
    public MessageDTO reprocessMessage(String id) {
        SystemLog log = logRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        
        // Only reprocess messages that are flow executions
        if (!"FLOW_EXECUTION".equals(log.getCategory())) {
            throw new IllegalArgumentException("Can only reprocess flow execution messages");
        }
        
        // Get the flow ID from the domain reference
        String flowId = log.getDomainReferenceId();
        if (flowId == null || flowId.isEmpty()) {
            throw new IllegalArgumentException("No flow ID found in message");
        }
        
        // Check if we have the original message payload
        String originalPayload = null;
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            try {
                JsonNode details = objectMapper.readTree(log.getDetails());
                if (details.has("payload")) {
                    originalPayload = details.get("payload").asText();
                } else if (details.has("messageContent")) {
                    originalPayload = details.get("messageContent").asText();
                }
            } catch (Exception e) {
                logger.warn("Failed to extract original payload from message details", e);
            }
        }
        
        // If no payload in details, check adapter payloads
        if (originalPayload == null && log.getCorrelationId() != null) {
            List<AdapterPayload> payloads = payloadRepository.findByCorrelationIdOrderByCreatedAtAsc(log.getCorrelationId());
            // Find the first source adapter request payload
            for (AdapterPayload payload : payloads) {
                if ("request".equalsIgnoreCase(payload.getPayloadType()) && "source".equalsIgnoreCase(payload.getDirection())) {
                    originalPayload = payload.getPayload();
                    break;
                }
            }
        }
        
        // Create a new correlation ID for the reprocessing
        String newCorrelationId = UUID.randomUUID().toString();
        
        // Log the reprocessing attempt
        logger.info("Reprocessing message {} (flow: {}) with new correlation ID: {}", id, flowId, newCorrelationId);
        
        try {
            // Get the flow execution service instance
            FlowExecutionAsyncService flowExecutionService = applicationContext.getBean(FlowExecutionAsyncService.class);
            
            // Create a new message for the reprocessing
            IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + flowId));
            
            String reprocessCorrelationId = createMessage(flow, 
                    "Reprocessing message from " + log.getCorrelationId(), 
                    "REPROCESS", 
                    newCorrelationId);
            
            // Execute the flow
            // Note: The flow will pull fresh data from the source adapter
            // TODO: In the future, we could enhance to support replaying with original payload
            flowExecutionService.executeFlow(flowId);
            
            // Update the original message to indicate it was reprocessed
            log.setDetails(updateDetailsWithReprocessInfo(log.getDetails(), newCorrelationId));
            logRepository.save(log);
            
            // Return the updated message DTO
            return convertToMessageDTO(log);
            
        } catch (Exception e) {
            logger.error("Failed to reprocess message {}: {}", id, e.getMessage(), e);
            
            // Log the failure
            SystemLog failureLog = new SystemLog();
            failureLog.setTimestamp(LocalDateTime.now());
            failureLog.setCategory("REPROCESS_FAILURE");
            failureLog.setLevel(LogLevel.ERROR);
            failureLog.setMessage("Failed to reprocess message: " + e.getMessage());
            failureLog.setDomainType("SystemLog");
            failureLog.setDomainReferenceId(id);
            failureLog.setCorrelationId(newCorrelationId);
            try {
                failureLog.setDetails(objectMapper.writeValueAsString(Map.of(
                        "originalMessageId", id,
                        "error", e.getMessage(),
                        "stackTrace", Arrays.toString(e.getStackTrace())
                )));
            } catch (Exception jsonEx) {
                failureLog.setDetails("{\"error\": \"Failed to serialize error details\"}");
            }
            logRepository.save(failureLog);
            
            throw new RuntimeException("Failed to reprocess message: " + e.getMessage(), e);
        }
    }
    
    private String updateDetailsWithReprocessInfo(String existingDetails, String newCorrelationId) {
        try {
            ObjectNode details = existingDetails != null && !existingDetails.isEmpty() 
                    ? (ObjectNode) objectMapper.readTree(existingDetails)
                    : objectMapper.createObjectNode();
            
            // Add reprocess information
            ArrayNode reprocessHistory = details.has("reprocessHistory") 
                    ? (ArrayNode) details.get("reprocessHistory")
                    : details.putArray("reprocessHistory");
            
            ObjectNode reprocessEntry = objectMapper.createObjectNode();
            reprocessEntry.put("timestamp", LocalDateTime.now().toString());
            reprocessEntry.put("newCorrelationId", newCorrelationId);
            reprocessHistory.add(reprocessEntry);
            
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            logger.warn("Failed to update details with reprocess info", e);
            return existingDetails;
        }
    }
    
    /**
     * Log adapter payload (request or response) to dedicated payload table
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAdapterPayload(String correlationId, CommunicationAdapter adapter, 
                                  String payloadType, String payload, String direction) {
        logger.info("DEBUG: logAdapterPayload called - correlationId: {}, adapter: {}, direction: {}, payloadType: {}", 
            correlationId, adapter.getName(), direction, payloadType);
        try {
            // Use the new AdapterPayload entity
            AdapterPayload adapterPayload = AdapterPayload.builder()
                .correlationId(correlationId)
                .adapterId(adapter.getId())
                .adapterName(adapter.getName())
                .adapterType(adapter.getType() != null ? adapter.getType().name() : "UNKNOWN")
                .direction(direction)
                .payloadType(payloadType)
                .payload(payload)
                .payloadSize(payload != null ? payload.length() : 0)
                .build();
            
            logger.info("DEBUG: About to save AdapterPayload to database");
            AdapterPayload saved = payloadRepository.save(adapterPayload);
            logger.info("DEBUG: Successfully saved adapter payload with ID: {}", saved.getId());
            
            // Force flush to ensure it's written
            payloadRepository.flush();
            logger.info("DEBUG: Flushed to database");
            
            // Also log a simple entry to system_logs for tracking
            try {
                SystemLog log = new SystemLog();
                log.setTimestamp(LocalDateTime.now());
                log.setCreatedAt(LocalDateTime.now());
                log.setLevel(SystemLog.LogLevel.INFO);
                log.setMessage(String.format("Adapter %s payload logged - %s", direction, payloadType));
                log.setCategory("ADAPTER_PAYLOAD");
                log.setDomainType("CommunicationAdapter");
                log.setDomainReferenceId(adapter.getId().toString());
                log.setCorrelationId(correlationId);
                log.setSourceName(adapter.getName());
                log.setSource(adapter.getType() != null ? adapter.getType().name() : "UNKNOWN");
                ObjectNode logDetails = objectMapper.createObjectNode();
                logDetails.put("message", String.format("Payload stored in adapter_payloads table with ID: %s", saved.getId()));
                log.setDetails(logDetails.toString());
                logRepository.save(log);
            } catch (Exception ex) {
                logger.warn("Failed to create system log entry for payload: ", ex);
            }
            
        } catch (Exception e) {
            logger.error("Error logging adapter payload for adapter: {} - Error: {}", adapter.getName(), e.getMessage());
            logger.error("Full error details: ", e);
        }
    }
    
    /**
     * Get adapter payloads by correlation ID
     */
    public List<AdapterPayload> getAdapterPayloads(String correlationId) {
        return payloadRepository.findByCorrelationIdOrderByCreatedAtAsc(correlationId);
    }
    
    private Specification<SystemLog> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter to show messages from integration flows AND adapter activity
            // Include both flow execution logs and adapter activity logs (linked by correlation ID)
            predicates.add(cb.or(
                cb.equal(root.get("domainType"), "IntegrationFlow"),
                cb.equal(root.get("domainType"), "CommunicationAdapter"),
                cb.equal(root.get("category"), "FLOW_EXECUTION"),
                cb.equal(root.get("category"), "ADAPTER_ACTIVITY"),
                cb.equal(root.get("category"), "ADAPTER_PAYLOAD"),
                cb.equal(root.get("category"), "DIRECT_FLOW"),
                cb.equal(root.get("category"), "ORCHESTRATION_FLOW"),
                cb.like(root.get("category"), "FLOW_%"),
                cb.isNotNull(root.get("correlationId")) // Include any log with a correlation ID
            ));
            
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
                LocalDateTime dateFrom = (LocalDateTime) filters.get("dateFrom");
                // Convert from UTC to system timezone
                String systemTimezone = systemConfigurationService.getSystemTimezone();
                ZoneId systemZone = ZoneId.of(systemTimezone);
                ZoneId utcZone = ZoneId.of("UTC");
                
                // Assume incoming dateFrom is in UTC, convert to system timezone
                ZonedDateTime utcDateTime = dateFrom.atZone(utcZone);
                LocalDateTime localDateTime = utcDateTime.withZoneSameInstant(systemZone).toLocalDateTime();
                
                logger.debug("Filtering from date - UTC: {}, Local ({}): {}", dateFrom, systemTimezone, localDateTime);
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), localDateTime));
            }
            
            if (filters.containsKey("dateTo")) {
                LocalDateTime dateTo = (LocalDateTime) filters.get("dateTo");
                // Convert from UTC to system timezone
                String systemTimezone = systemConfigurationService.getSystemTimezone();
                ZoneId systemZone = ZoneId.of(systemTimezone);
                ZoneId utcZone = ZoneId.of("UTC");
                
                // Assume incoming dateTo is in UTC, convert to system timezone
                ZonedDateTime utcDateTime = dateTo.atZone(utcZone);
                LocalDateTime localDateTime = utcDateTime.withZoneSameInstant(systemZone).toLocalDateTime();
                
                logger.debug("Filtering to date - UTC: {}, Local ({}): {}", dateTo, systemTimezone, localDateTime);
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), localDateTime));
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
    
    private MessageDTO convertToMessageDTOWithAdapterLogs(SystemLog log, Map<String, List<SystemLog>> logsByCorrelation) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        // Create log entries
        List<MessageDTO.MessageLogDTO> logs = new ArrayList<>();
        
        // Add main log entry
        logs.add(MessageDTO.MessageLogDTO.builder()
                .timestamp(log.getTimestamp())
                .level(log.getLevel().name())
                .message(log.getMessage())
                .build());
        
        // Extract processing steps from details JSON if available
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            try {
                JsonNode detailsNode = objectMapper.readTree(log.getDetails());
                if (detailsNode.has("processingSteps") && detailsNode.get("processingSteps").isArray()) {
                    ArrayNode steps = (ArrayNode) detailsNode.get("processingSteps");
                    for (JsonNode step : steps) {
                        String stepTimestamp = step.has("timestamp") ? step.get("timestamp").asText() : "";
                        String stepLevel = step.has("level") ? step.get("level").asText() : "INFO";
                        String stepMessage = step.has("step") ? step.get("step").asText() : "";
                        String stepDetails = step.has("details") ? step.get("details").asText() : "";
                        
                        // Format the timestamp to match frontend expectations
                        LocalDateTime parsedTime = LocalDateTime.parse(stepTimestamp);
                        
                        logs.add(MessageDTO.MessageLogDTO.builder()
                                .timestamp(parsedTime)
                                .level(stepLevel)
                                .message(stepMessage + (stepDetails.isEmpty() ? "" : " - " + stepDetails))
                                .build());
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing log details: {}", e.getMessage());
            }
        }
        
        // Add adapter activity logs if available
        if (log.getCorrelationId() != null && logsByCorrelation.containsKey(log.getCorrelationId())) {
            List<SystemLog> relatedLogs = logsByCorrelation.get(log.getCorrelationId());
            for (SystemLog relatedLog : relatedLogs) {
                // Skip the main log itself
                if (relatedLog.getId().equals(log.getId())) {
                    continue;
                }
                
                // Add adapter activity logs
                if ("ADAPTER_ACTIVITY".equals(relatedLog.getCategory()) || 
                    "CommunicationAdapter".equals(relatedLog.getDomainType())) {
                    
                    String adapterMessage = String.format("[%s] %s", 
                        relatedLog.getSourceName() != null ? relatedLog.getSourceName() : "Adapter",
                        relatedLog.getMessage());
                    
                    logs.add(MessageDTO.MessageLogDTO.builder()
                            .timestamp(relatedLog.getTimestamp())
                            .level(relatedLog.getLevel().name())
                            .message(adapterMessage)
                            .build());
                    
                    // Parse adapter activity details if available
                    if (relatedLog.getDetails() != null && !relatedLog.getDetails().isEmpty()) {
                        try {
                            JsonNode adapterDetails = objectMapper.readTree(relatedLog.getDetails());
                            if (adapterDetails.has("activityDetails")) {
                                logs.add(MessageDTO.MessageLogDTO.builder()
                                        .timestamp(relatedLog.getTimestamp())
                                        .level(relatedLog.getLevel().name())
                                        .message("  → " + adapterDetails.get("activityDetails").asText())
                                        .build());
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors for adapter details
                        }
                    }
                }
            }
        }
        
        // Sort logs by timestamp
        logs.sort(Comparator.comparing(MessageDTO.MessageLogDTO::getTimestamp));
        
        // Calculate processing time and message size
        String processingTime = "250ms";
        String messageSize = "1024 bytes";
        
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            try {
                JsonNode detailsNode = objectMapper.readTree(log.getDetails());
                
                // Extract message size
                if (detailsNode.has("messageSize")) {
                    messageSize = detailsNode.get("messageSize").asInt() + " bytes";
                }
                
                // Calculate processing time
                if (detailsNode.has("startTime") && detailsNode.has("endTime")) {
                    try {
                        LocalDateTime startTime = LocalDateTime.parse(detailsNode.get("startTime").asText());
                        LocalDateTime endTime = LocalDateTime.parse(detailsNode.get("endTime").asText());
                        long millis = java.time.Duration.between(startTime, endTime).toMillis();
                        processingTime = millis + "ms";
                    } catch (Exception e) {
                        // Keep default if parsing fails
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing log details for processing time: {}", e.getMessage());
            }
        }
        
        return MessageDTO.builder()
                .id(log.getId().toString())
                .timestamp(log.getTimestamp())
                .source(log.getSource() != null ? log.getSource() : "System")
                .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                .type(log.getCategory() != null ? log.getCategory() : "INTEGRATION")
                .status(status)
                .processingTime(processingTime)
                .size(messageSize)
                .businessComponentId(log.getComponentId())
                .correlationId(log.getCorrelationId())
                .logs(logs)
                .build();
    }
    
    private MessageDTO convertToMessageDTO(SystemLog log) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        // Create log entries
        List<MessageDTO.MessageLogDTO> logs = new ArrayList<>();
        
        // Add main log entry
        logs.add(MessageDTO.MessageLogDTO.builder()
                .timestamp(log.getTimestamp())
                .level(log.getLevel().name())
                .message(log.getMessage())
                .build());
        
        // Extract processing steps from details JSON if available
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            try {
                JsonNode detailsNode = objectMapper.readTree(log.getDetails());
                if (detailsNode.has("processingSteps") && detailsNode.get("processingSteps").isArray()) {
                    ArrayNode steps = (ArrayNode) detailsNode.get("processingSteps");
                    for (JsonNode step : steps) {
                        String stepTimestamp = step.has("timestamp") ? step.get("timestamp").asText() : "";
                        String stepLevel = step.has("level") ? step.get("level").asText() : "INFO";
                        String stepMessage = step.has("step") ? step.get("step").asText() : "";
                        String stepDetails = step.has("details") ? step.get("details").asText() : "";
                        
                        // Format the timestamp to match frontend expectations
                        LocalDateTime parsedTime = LocalDateTime.parse(stepTimestamp);
                        
                        logs.add(MessageDTO.MessageLogDTO.builder()
                                .timestamp(parsedTime)
                                .level(stepLevel)
                                .message(stepMessage + (stepDetails.isEmpty() ? "" : " - " + stepDetails))
                                .build());
                    }
                }
                
                // Extract message size and processing time if available
                String messageSize = detailsNode.has("messageSize") ? 
                    detailsNode.get("messageSize").asInt() + " bytes" : "1024 bytes";
                
                // Calculate processing time from start/end time if available
                String processingTime = "250ms"; // Default
                if (detailsNode.has("startTime") && detailsNode.has("endTime")) {
                    try {
                        LocalDateTime startTime = LocalDateTime.parse(detailsNode.get("startTime").asText());
                        LocalDateTime endTime = LocalDateTime.parse(detailsNode.get("endTime").asText());
                        long millis = java.time.Duration.between(startTime, endTime).toMillis();
                        processingTime = millis + "ms";
                    } catch (Exception e) {
                        // Keep default if parsing fails
                    }
                }
                
                return MessageDTO.builder()
                        .id(log.getId().toString())
                        .timestamp(log.getTimestamp())
                        .source(log.getSource() != null ? log.getSource() : "System")
                        .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                        .type(log.getCategory() != null ? log.getCategory() : "INTEGRATION")
                        .status(status)
                        .processingTime(processingTime)
                        .size(messageSize)
                        .businessComponentId(log.getComponentId())
                        .correlationId(log.getCorrelationId())
                        .logs(logs)
                        .build();
                
            } catch (Exception e) {
                logger.error("Error parsing log details: {}", e.getMessage());
                // Fall back to simple response if JSON parsing fails
            }
        }
        
        return MessageDTO.builder()
                .id(log.getId().toString())
                .timestamp(log.getTimestamp())
                .source(log.getSource() != null ? log.getSource() : "System")
                .target(log.getSourceName() != null ? log.getSourceName() : "Integration Flow")
                .type(log.getCategory() != null ? log.getCategory() : "INTEGRATION")
                .status(status)
                .processingTime("250ms")
                .size("1024 bytes")
                .businessComponentId(log.getComponentId())
                .correlationId(log.getCorrelationId())
                .logs(logs)
                .build();
    }

    private RecentMessageDTO convertToRecentMessageDTO(SystemLog log) {
        String status = mapLogLevelToStatus(log.getLevel().name());
        
        return RecentMessageDTO.builder()
                .id(log.getId().toString())
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
    
    /**
     * Create a new message record for flow processing
     */
    public String createMessage(IntegrationFlow flow, String messageContent, String protocol) {
        return createMessage(flow, messageContent, protocol, null);
    }
    
    /**
     * Create a new message record for flow processing with optional correlation ID
     */
    public String createMessage(IntegrationFlow flow, String messageContent, String protocol, String existingCorrelationId) {
        String correlationId = existingCorrelationId != null ? existingCorrelationId : UUID.randomUUID().toString();
        
        try {
            // Create initial message received entry with empty processing steps
            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setCategory("FLOW_EXECUTION");
            log.setLevel(LogLevel.INFO);
            log.setMessage("Flow execution: " + flow.getName());
            log.setDomainType("IntegrationFlow");
            log.setDomainReferenceId(flow.getId().toString());
            log.setComponentId(flow.getBusinessComponent() != null ? flow.getBusinessComponent().getId().toString() : null); // Set business component ID from flow
            log.setSource(protocol);
            log.setSourceId(flow.getId().toString());
            log.setSourceName(flow.getName());
            log.setCorrelationId(correlationId);
            
            // Initialize details with processing steps array
            ObjectNode details = objectMapper.createObjectNode();
            details.put("protocol", protocol);
            details.put("messageSize", messageContent.length());
            details.put("startTime", LocalDateTime.now().toString());
            details.set("processingSteps", objectMapper.createArrayNode());
            
            log.setDetails(details.toString());
            
            logRepository.save(log);
            logRepository.flush();
            logger.info("Successfully created message log with correlation ID: {} and ID: {}", correlationId, log.getId());
        } catch (Exception e) {
            logger.error("Error creating message log: {}", e.getMessage(), e);
            // Don't throw - logging should not break flow processing
        }
        
        return correlationId; // Return correlation ID regardless
    }
    
    /**
     * Log a processing step - adds to existing message log
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logProcessingStep(String correlationId, IntegrationFlow flow, String step, String stepDetails, LogLevel level) {
        try {
            logger.debug("Adding processing step for correlation ID: {} - Step: {}", correlationId, step);
            
            // Find the main message log by correlation ID
            List<SystemLog> logs = logRepository.findByCorrelationId(correlationId);
            if (logs.isEmpty()) {
                logger.warn("No message log found for correlation ID: {}", correlationId);
                return;
            }
            
            logger.debug("Found {} logs for correlation ID: {}", logs.size(), correlationId);
            SystemLog mainLog = logs.get(0); // Get the main log entry
            
            // Parse existing details
            ObjectNode details = (ObjectNode) objectMapper.readTree(
                mainLog.getDetails() != null ? mainLog.getDetails() : "{}"
            );
            
            // Get or create processing steps array
            ArrayNode steps = (ArrayNode) details.get("processingSteps");
            if (steps == null) {
                steps = objectMapper.createArrayNode();
                details.set("processingSteps", steps);
            }
            
            // Add new step
            ObjectNode newStep = objectMapper.createObjectNode();
            newStep.put("timestamp", LocalDateTime.now().toString());
            newStep.put("step", step);
            // Use ObjectMapper to properly handle JSON serialization
            if (stepDetails != null) {
                newStep.put("details", stepDetails);
            } else {
                newStep.put("details", "");
            }
            newStep.put("level", level.toString());
            steps.add(newStep);
            
            // Update the main log entry
            mainLog.setDetails(details.toString());
            
            // Update level if this step has error
            if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
                mainLog.setLevel(level);
            }
            
            logRepository.save(mainLog);
            logRepository.flush();
            
            logger.debug("Successfully saved processing step. Total steps now: {}", steps.size());
        } catch (Exception e) {
            logger.error("Error logging processing step: {}", e.getMessage(), e);
            e.printStackTrace();
            // Don't throw - logging should not break flow processing
        }
    }
    
    /**
     * Update message status after processing
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMessageStatus(String correlationId, String status, String statusDetails) {
        try {
            // Find the main message log by correlation ID
            List<SystemLog> logs = logRepository.findByCorrelationId(correlationId);
            if (logs.isEmpty()) {
                logger.warn("No message log found for correlation ID: {}", correlationId);
                return;
            }
            
            SystemLog mainLog = logs.get(0);
            
            // Parse existing details
            ObjectNode details = (ObjectNode) objectMapper.readTree(
                mainLog.getDetails() != null ? mainLog.getDetails() : "{}"
            );
            
            // Update completion info
            details.put("endTime", LocalDateTime.now().toString());
            details.put("status", status);
            details.put("statusDetails", statusDetails != null ? statusDetails : "");
            
            // Calculate duration if startTime exists
            if (details.has("startTime")) {
                try {
                    LocalDateTime startTime = LocalDateTime.parse(details.get("startTime").asText());
                    LocalDateTime endTime = LocalDateTime.now();
                    long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
                    details.put("durationMs", durationMs);
                } catch (Exception e) {
                    logger.warn("Could not calculate duration: {}", e.getMessage());
                }
            }
            
            // Update the main log entry
            mainLog.setDetails(details.toString());
            
            // Update message and level based on status
            switch (status) {
                case "COMPLETED":
                    mainLog.setMessage("Flow completed: " + mainLog.getSourceName());
                    if (mainLog.getLevel() != LogLevel.ERROR && mainLog.getLevel() != LogLevel.FATAL) {
                        mainLog.setLevel(LogLevel.INFO);
                    }
                    break;
                case "FAILED":
                    mainLog.setMessage("Flow failed: " + mainLog.getSourceName());
                    mainLog.setLevel(LogLevel.ERROR);
                    break;
                case "PROCESSING":
                    mainLog.setMessage("Flow processing: " + mainLog.getSourceName());
                    break;
            }
            
            logRepository.save(mainLog);
            logRepository.flush();
        } catch (Exception e) {
            logger.error("Error updating message status: {}", e.getMessage(), e);
            e.printStackTrace();
            // Don't throw - logging should not break flow processing
        }
    }
    
    /**
     * Log adapter-specific activity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAdapterActivity(CommunicationAdapter adapter, String message, String activityDetails, LogLevel level, String correlationId) {
        try {
            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setCategory("ADAPTER_ACTIVITY");
            log.setLevel(level);
            log.setMessage(message);
            log.setDomainType("CommunicationAdapter");
            log.setDomainReferenceId(adapter.getId().toString());
            log.setComponentId(adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId().toString() : null);
            log.setSource("ADAPTER");
            log.setSourceId(adapter.getId().toString());
            log.setSourceName(adapter.getName());
            log.setCorrelationId(correlationId); // Link to flow execution
            
            // Create JSON details for adapter activity
            ObjectNode details = objectMapper.createObjectNode();
            details.put("timestamp", LocalDateTime.now().toString());
            details.put("adapterType", adapter.getType().toString());
            details.put("adapterMode", adapter.getMode().toString());
            // Use ObjectMapper to properly handle JSON serialization
            if (activityDetails != null) {
                details.put("activityDetails", activityDetails);
            }
            
            log.setDetails(details.toString());
            
            logRepository.save(log);
            logRepository.flush();
        } catch (Exception e) {
            logger.error("Error logging adapter activity: {}", e.getMessage(), e);
            e.printStackTrace();
            // Don't throw - logging should not break flow processing
        }
    }
}