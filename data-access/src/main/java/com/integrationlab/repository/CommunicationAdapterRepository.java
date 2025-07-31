package com.integrationlab.repository;

import com.integrationlab.model.CommunicationAdapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository interface for CommunicationAdapterRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface CommunicationAdapterRepository extends JpaRepository<CommunicationAdapter, String> {

}