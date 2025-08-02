package com.integrationlab.data.repository;

import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.data.model.FlowStatus;
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
    
    // Using JPA method naming for pagination
    Page<IntegrationFlow> findByIsActiveOrderByUpdatedAtDesc(boolean isActive, Pageable pageable);
    
    // Using JPA method naming for count
    int countByIsActive(boolean isActive);
    
    // JPA handles this automatically
    int countByStatus(FlowStatus status);
    
    // Batch query to load multiple flows efficiently
    @Query("SELECT DISTINCT f FROM IntegrationFlow f LEFT JOIN FETCH f.transformations WHERE f.id IN :ids")
    List<IntegrationFlow> findAllByIdWithTransformations(@Param("ids") List<String> ids);
    
    // Using JPA method naming
    List<IntegrationFlow> findByStatusAndIsActiveTrueOrderByName(FlowStatus status);
    
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
    
    // Remove this complex query and use a simpler approach through service layer
    // Complex business component filtering should be done in the service layer
    List<IntegrationFlow> findByIsActive(boolean active);
    
    boolean existsByName(String name);
}