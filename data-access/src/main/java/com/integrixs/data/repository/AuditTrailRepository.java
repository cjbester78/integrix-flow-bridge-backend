package com.integrixs.data.repository;

import com.integrixs.data.model.AuditTrail;
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
 * Repository for AuditTrail entities.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, UUID> {
    
    /**
     * Find audit entries by entity type and ID
     */
    List<AuditTrail> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
    
    /**
     * Find audit entries by user ID
     */
    Page<AuditTrail> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find audit entries by action
     */
    Page<AuditTrail> findByActionOrderByCreatedAtDesc(AuditTrail.AuditAction action, Pageable pageable);
    
    /**
     * Find audit entries within a date range
     */
    Page<AuditTrail> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, 
                                                                LocalDateTime endDate, 
                                                                Pageable pageable);
    
    /**
     * Find audit entries by business component
     */
    Page<AuditTrail> findByBusinessComponentIdOrderByCreatedAtDesc(UUID businessComponentId, Pageable pageable);
    
    /**
     * Search audit entries by multiple criteria
     */
    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditTrail> searchAuditTrail(@Param("entityType") String entityType,
                                     @Param("action") AuditTrail.AuditAction action,
                                     @Param("userId") UUID userId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);
}