package com.integrationlab.data.repository;

import com.integrationlab.data.model.AdapterPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AdapterPayload entities
 */
@Repository
public interface AdapterPayloadRepository extends JpaRepository<AdapterPayload, String> {
    
    List<AdapterPayload> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
    
    List<AdapterPayload> findByAdapterIdOrderByCreatedAtDesc(String adapterId);
    
    void deleteByCorrelationId(String correlationId);
}