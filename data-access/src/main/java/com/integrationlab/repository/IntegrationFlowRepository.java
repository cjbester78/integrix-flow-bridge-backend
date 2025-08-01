package com.integrationlab.repository;

import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.model.FlowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository interface for IntegrationFlowRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface IntegrationFlowRepository extends JpaRepository<IntegrationFlow, String> {
    
    // Optimized query with eager loading to prevent N+1
    @EntityGraph(attributePaths = {"transformations", "businessComponent"})
    Optional<IntegrationFlow> findWithTransformationsById(String id);
    
    // Paginated query with index usage
    @Query("SELECT f FROM IntegrationFlow f WHERE f.isActive = :active ORDER BY f.updatedAt DESC")
    Page<IntegrationFlow> findByActiveWithPagination(@Param("active") boolean active, Pageable pageable);
    
    // Count queries use indexes
    @Query("SELECT COUNT(f) FROM IntegrationFlow f WHERE f.isActive = :active")
    int countByActive(@Param("active") boolean active);
    
    @Query("SELECT COUNT(f) FROM IntegrationFlow f WHERE f.status = :status")
    int countByStatus(@Param("status") FlowStatus status);
    
    // Batch query to load multiple flows efficiently
    @Query("SELECT DISTINCT f FROM IntegrationFlow f LEFT JOIN FETCH f.transformations WHERE f.id IN :ids")
    List<IntegrationFlow> findAllByIdWithTransformations(@Param("ids") List<String> ids);
    
    // Query using indexes for performance
    @Query("SELECT f FROM IntegrationFlow f WHERE f.status = :status AND f.isActive = true ORDER BY f.name")
    List<IntegrationFlow> findActiveByStatus(@Param("status") FlowStatus status);
    
    // Efficient update queries
    @Modifying
    @Query("UPDATE IntegrationFlow f SET f.status = :status, f.updatedAt = :updatedAt WHERE f.id = :id")
    int updateStatus(@Param("id") String id, @Param("status") FlowStatus status, @Param("updatedAt") LocalDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE IntegrationFlow f SET f.executionCount = f.executionCount + 1, " +
           "f.successCount = CASE WHEN :success = true THEN f.successCount + 1 ELSE f.successCount END, " +
           "f.errorCount = CASE WHEN :success = false THEN f.errorCount + 1 ELSE f.errorCount END, " +
           "f.lastExecutionAt = :executionTime WHERE f.id = :id")
    int updateExecutionStats(@Param("id") String id, @Param("success") boolean success, @Param("executionTime") LocalDateTime executionTime);
    
    // Query with specific projections to reduce data transfer
    @Query("SELECT new map(f.id as id, f.name as name, f.status as status, f.isActive as active, " +
           "f.executionCount as executionCount, f.successCount as successCount, f.errorCount as errorCount) " +
           "FROM IntegrationFlow f WHERE f.createdBy = :userId")
    List<Object> findFlowStatsByUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(DISTINCT f) FROM IntegrationFlow f " +
           "LEFT JOIN CommunicationAdapter sa ON f.sourceAdapterId = sa.id " +
           "LEFT JOIN CommunicationAdapter ta ON f.targetAdapterId = ta.id " +
           "WHERE (sa.businessComponentId = :businessComponentId OR ta.businessComponentId = :businessComponentId) " +
           "AND f.isActive = :active")
    int countByBusinessComponentIdAndActive(@Param("businessComponentId") String businessComponentId, @Param("active") boolean active);
}