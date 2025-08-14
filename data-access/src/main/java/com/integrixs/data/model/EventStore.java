package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Entity for event sourcing - stores all domain events.
 * 
 * <p>Provides an immutable audit trail of all system events.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "event_store", indexes = {
    @Index(name = "idx_event_aggregate", columnList = "aggregate_id, aggregate_version"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_event_timestamp", columnList = "occurred_at"),
    @Index(name = "idx_event_user", columnList = "triggered_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventStore {
    
    /**
     * Unique event ID
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String eventId;
    
    /**
     * Type of the aggregate this event belongs to
     */
    @Column(name = "aggregate_type", nullable = false, length = 100)
    @NotBlank(message = "Aggregate type is required")
    private String aggregateType;
    
    /**
     * ID of the aggregate this event belongs to
     */
    @Column(name = "aggregate_id", nullable = false, columnDefinition = "char(36)")
    @NotBlank(message = "Aggregate ID is required")
    private String aggregateId;
    
    /**
     * Version of the aggregate when this event was created
     */
    @Column(name = "aggregate_version", nullable = false)
    @NotNull(message = "Aggregate version is required")
    private Long aggregateVersion;
    
    /**
     * Type of the event
     */
    @Column(name = "event_type", nullable = false, length = 100)
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    /**
     * Event data in JSON format
     */
    @Column(name = "event_data", columnDefinition = "json", nullable = false)
    @NotBlank(message = "Event data is required")
    private String eventData;
    
    /**
     * Event metadata in JSON format
     */
    @Column(name = "event_metadata", columnDefinition = "json")
    private String eventMetadata;
    
    /**
     * When the event occurred
     */
    @Column(name = "occurred_at", nullable = false)
    @NotNull(message = "Occurred at is required")
    private LocalDateTime occurredAt;
    
    /**
     * User who triggered the event
     */
    @Column(name = "triggered_by", columnDefinition = "char(36)")
    private String triggeredBy;
    
    /**
     * When the event was stored
     */
    @Column(name = "stored_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime storedAt;
    
    /**
     * Correlation ID for tracking related events
     */
    @Column(name = "correlation_id", columnDefinition = "char(36)")
    private String correlationId;
    
    /**
     * Causation ID - ID of the event that caused this event
     */
    @Column(name = "causation_id", columnDefinition = "char(36)")
    private String causationId;
}