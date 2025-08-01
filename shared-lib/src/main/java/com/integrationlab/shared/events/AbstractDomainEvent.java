package com.integrationlab.shared.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Abstract base class for domain events.
 * 
 * <p>Provides common implementation for all domain events.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractDomainEvent implements DomainEvent {
    
    private String eventId;
    private LocalDateTime occurredAt;
    private String aggregateId;
    private Long aggregateVersion;
    private String triggeredBy;
    
    /**
     * Initializes the event with default values.
     */
    protected AbstractDomainEvent(String aggregateId, String triggeredBy) {
        this.eventId = DomainEvent.newEventId();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.triggeredBy = triggeredBy;
        this.aggregateVersion = 1L;
    }
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}