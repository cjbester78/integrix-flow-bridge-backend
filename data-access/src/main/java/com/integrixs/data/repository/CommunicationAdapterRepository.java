package com.integrixs.data.repository;

import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
/**
 * Repository interface for CommunicationAdapterRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface CommunicationAdapterRepository extends JpaRepository<CommunicationAdapter, UUID> {
    List<CommunicationAdapter> findByBusinessComponent_Id(UUID businessComponentId);
    boolean existsByName(String name);
}