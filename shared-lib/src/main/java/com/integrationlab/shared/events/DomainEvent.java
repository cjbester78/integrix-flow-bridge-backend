package com.integrationlab.shared.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events.
 * 
 * <p>Domain events represent something that has happened in the domain
 * that domain experts care about.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public interface DomainEvent {
    
    /**
     * Gets the unique identifier of this event.
     * 
     * @return event ID
     */
    String getEventId();
    
    /**
     * Gets the timestamp when this event occurred.
     * 
     * @return event timestamp
     */
    LocalDateTime getOccurredAt();
    
    /**
     * Gets the type of this event.
     * 
     * @return event type
     */
    String getEventType();
    
    /**
     * Gets the aggregate ID this event is associated with.
     * 
     * @return aggregate ID
     */
    String getAggregateId();
    
    /**
     * Gets the version of the aggregate when this event was created.
     * 
     * @return aggregate version
     */
    Long getAggregateVersion();
    
    /**
     * Gets the user who triggered this event.
     * 
     * @return user ID
     */
    String getTriggeredBy();
    
    /**
     * Creates a new event ID.
     * 
     * @return new UUID
     */
    static String newEventId() {
        return UUID.randomUUID().toString();
    }
}