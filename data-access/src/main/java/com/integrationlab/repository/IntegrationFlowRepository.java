package com.integrationlab.repository;

import com.integrationlab.model.IntegrationFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository interface for IntegrationFlowRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface IntegrationFlowRepository extends JpaRepository<IntegrationFlow, String> {
}