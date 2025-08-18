package com.integrixs.data.repository;

import com.integrixs.data.model.EventStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for event store operations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Repository
public interface EventStoreRepository extends JpaRepository<EventStore, UUID> {
    
    /**
     * Finds events by aggregate ID ordered by version.
     * 
     * @param aggregateId the aggregate ID
     * @return list of events
     */
    List<EventStore> findByAggregateIdOrderByAggregateVersionAsc(String aggregateId);
    
    /**
     * Finds events by aggregate ID and type.
     * 
     * @param aggregateId the aggregate ID
     * @param aggregateType the aggregate type
     * @return list of events
     */
    List<EventStore> findByAggregateIdAndAggregateTypeOrderByAggregateVersionAsc(
        String aggregateId, String aggregateType);
    
    /**
     * Finds events by type within a time range.
     * 
     * @param eventType the event type
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination
     * @return page of events
     */
    Page<EventStore> findByEventTypeAndOccurredAtBetween(
        String eventType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Finds events by user within a time range.
     * 
     * @param triggeredBy the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination
     * @return page of events
     */
    Page<EventStore> findByTriggeredByAndOccurredAtBetween(
        String triggeredBy, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Gets the latest version for an aggregate.
     * 
     * @param aggregateId the aggregate ID
     * @return latest version or 0
     */
    @Query("SELECT COALESCE(MAX(e.aggregateVersion), 0) FROM EventStore e WHERE e.aggregateId = :aggregateId")
    Long getLatestVersionForAggregate(@Param("aggregateId") String aggregateId);
    
    /**
     * Finds events by correlation ID.
     * 
     * @param correlationId the correlation ID
     * @return list of related events
     */
    List<EventStore> findByCorrelationIdOrderByOccurredAtAsc(String correlationId);
}