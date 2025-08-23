package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AdapterPayload entities
 */
@Repository
public interface AdapterPayloadRepository extends JpaRepository<AdapterPayload, UUID> {
    
    List<AdapterPayload> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
    
    List<AdapterPayload> findByAdapterIdOrderByCreatedAtDesc(UUID adapterId);
    
    void deleteByCorrelationId(String correlationId);
}