package com.integrationlab.data.repository;

import com.integrationlab.data.model.CommunicationAdapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository interface for CommunicationAdapterRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface CommunicationAdapterRepository extends JpaRepository<CommunicationAdapter, String> {
    List<CommunicationAdapter> findByBusinessComponent_Id(String businessComponentId);
    boolean existsByName(String name);
}