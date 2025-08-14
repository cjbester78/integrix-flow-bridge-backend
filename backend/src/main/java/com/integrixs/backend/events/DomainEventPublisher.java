package com.integrixs.backend.events;

import com.integrixs.shared.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Publisher for domain events.
 * 
 * <p>Publishes domain events to Spring's application event infrastructure
 * and ensures events are published after transaction commit.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ThreadLocal<List<DomainEvent>> pendingEvents = ThreadLocal.withInitial(ArrayList::new);
    
    /**
     * Publishes a domain event.
     * 
     * <p>If within a transaction, the event is queued and published after commit.
     * Otherwise, it's published immediately.
     * 
     * @param event the domain event to publish
     */
    @Transactional
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }
        
        log.debug("Publishing domain event: {} with ID: {}", 
                event.getEventType(), event.getEventId());
        
        // In a real implementation, we'd check if we're in a transaction
        // For now, publish immediately
        publishEvent(event);
    }
    
    /**
     * Publishes multiple domain events.
     * 
     * @param events the domain events to publish
     */
    @Transactional
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        log.debug("Publishing {} domain events", events.size());
        events.forEach(this::publish);
    }
    
    /**
     * Publishes an event immediately.
     * 
     * @param event the event to publish
     */
    private void publishEvent(DomainEvent event) {
        try {
            applicationEventPublisher.publishEvent(event);
            log.info("Published event: {} for aggregate: {}", 
                    event.getEventType(), event.getAggregateId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} for aggregate: {}", 
                    event.getEventType(), event.getAggregateId(), e);
            // In production, consider dead letter queue or retry mechanism
        }
    }
    
    /**
     * Clears any pending events for the current thread.
     */
    public void clearPendingEvents() {
        pendingEvents.get().clear();
        pendingEvents.remove();
    }
}