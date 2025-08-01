package com.integrationlab.repository;

import com.integrationlab.model.IntegrationFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository interface for IntegrationFlowRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface IntegrationFlowRepository extends JpaRepository<IntegrationFlow, String> {
    @Query("SELECT COUNT(f) FROM IntegrationFlow f WHERE f.isActive = :active")
    int countByActive(@Param("active") boolean active);
    
    @Query("SELECT COUNT(DISTINCT f) FROM IntegrationFlow f " +
           "LEFT JOIN CommunicationAdapter sa ON f.sourceAdapterId = sa.id " +
           "LEFT JOIN CommunicationAdapter ta ON f.targetAdapterId = ta.id " +
           "WHERE (sa.businessComponentId = :businessComponentId OR ta.businessComponentId = :businessComponentId) " +
           "AND f.isActive = :active")
    int countByBusinessComponentIdAndActive(@Param("businessComponentId") String businessComponentId, @Param("active") boolean active);
}