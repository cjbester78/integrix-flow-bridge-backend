package com.integrixs.backend.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.EventStore;
import com.integrixs.data.repository.EventStoreRepository;
import com.integrixs.shared.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for event sourcing - stores all domain events.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSourcingService {
    
    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;
    
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
    /**
     * Stores a domain event.
     * 
     * @param event the domain event
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeEvent(DomainEvent event) {
        try {
            // Get or create correlation ID
            String corrId = correlationId.get();
            if (corrId == null) {
                corrId = UUID.randomUUID().toString();
            }
            
            // Get latest version for aggregate
            Long latestVersion = eventStoreRepository.getLatestVersionForAggregate(event.getAggregateId());
            
            // Create event store entry
            EventStore eventStore = EventStore.builder()
                .eventId(UUID.fromString(event.getEventId()))
                .aggregateType(getAggregateType(event))
                .aggregateId(event.getAggregateId())
                .aggregateVersion(latestVersion + 1)
                .eventType(event.getEventType())
                .eventData(serializeEvent(event))
                .eventMetadata(createMetadata(event))
                .occurredAt(event.getOccurredAt())
                .triggeredBy(event.getTriggeredBy())
                .correlationId(corrId)
                .build();
            
            eventStoreRepository.save(eventStore);
            
            log.debug("Stored event: {} for aggregate: {} version: {}", 
                     event.getEventType(), event.getAggregateId(), eventStore.getAggregateVersion());
            
        } catch (Exception e) {
            log.error("Failed to store event: {} for aggregate: {}", 
                     event.getEventType(), event.getAggregateId(), e);
            // Don't throw - event sourcing should not break business logic
        }
    }
    
    /**
     * Gets event history for an aggregate.
     * 
     * @param aggregateId the aggregate ID
     * @return list of events
     */
    public List<EventStore> getEventHistory(String aggregateId) {
        return eventStoreRepository.findByAggregateIdOrderByAggregateVersionAsc(aggregateId);
    }
    
    /**
     * Gets event history for a specific aggregate type.
     * 
     * @param aggregateId the aggregate ID
     * @param aggregateType the aggregate type
     * @return list of events
     */
    public List<EventStore> getEventHistory(String aggregateId, String aggregateType) {
        return eventStoreRepository.findByAggregateIdAndAggregateTypeOrderByAggregateVersionAsc(
            aggregateId, aggregateType);
    }
    
    /**
     * Rebuilds aggregate state from events.
     * 
     * @param aggregateId the aggregate ID
     * @param aggregateType the aggregate type
     * @param <T> the aggregate type
     * @return rebuilt aggregate or null
     */
    public <T> T rebuildAggregate(String aggregateId, Class<T> aggregateType) {
        List<EventStore> events = getEventHistory(aggregateId, aggregateType.getSimpleName());
        
        if (events.isEmpty()) {
            return null;
        }
        
        // This is a simplified version - in real implementation,
        // you would apply each event to rebuild the aggregate state
        log.info("Rebuilding aggregate {} from {} events", aggregateId, events.size());
        
        // TODO: Implement actual event replay logic
        return null;
    }
    
    /**
     * Gets audit trail for a time period.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of events
     */
    public List<EventStore> getAuditTrail(LocalDateTime startTime, LocalDateTime endTime) {
        return eventStoreRepository.findAll().stream()
            .filter(e -> e.getOccurredAt().isAfter(startTime) && e.getOccurredAt().isBefore(endTime))
            .sorted((e1, e2) -> e1.getOccurredAt().compareTo(e2.getOccurredAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets user activity audit trail.
     * 
     * @param userId the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of events
     */
    public List<EventStore> getUserActivityAudit(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return eventStoreRepository.findByTriggeredByAndOccurredAtBetween(
            userId, startTime, endTime, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }
    
    /**
     * Sets correlation ID for the current thread.
     * 
     * @param id the correlation ID
     */
    public static void setCorrelationId(String id) {
        correlationId.set(id);
    }
    
    /**
     * Clears correlation ID for the current thread.
     */
    public static void clearCorrelationId() {
        correlationId.remove();
    }
    
    /**
     * Determines aggregate type from event.
     * 
     * @param event the domain event
     * @return aggregate type
     */
    private String getAggregateType(DomainEvent event) {
        // Extract aggregate type from event type
        String eventType = event.getEventType();
        if (eventType.contains("Flow")) {
            return "IntegrationFlow";
        } else if (eventType.contains("User")) {
            return "User";
        } else if (eventType.contains("Adapter")) {
            return "Adapter";
        }
        return "Unknown";
    }
    
    /**
     * Serializes event to JSON.
     * 
     * @param event the domain event
     * @return JSON string
     */
    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize event", e);
            return "{}";
        }
    }
    
    /**
     * Creates event metadata.
     * 
     * @param event the domain event
     * @return metadata JSON
     */
    private String createMetadata(DomainEvent event) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventClass", event.getClass().getName());
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("correlationId", correlationId.get());
            
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to create metadata", e);
            return "{}";
        }
    }
}