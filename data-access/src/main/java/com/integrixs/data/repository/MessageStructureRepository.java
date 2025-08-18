package com.integrixs.data.repository;

import com.integrixs.data.model.MessageStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageStructureRepository extends JpaRepository<MessageStructure, UUID> {
    
    Optional<MessageStructure> findByIdAndIsActiveTrue(UUID id);
    
    List<MessageStructure> findAllByIsActiveTrue();
    
    @Query("SELECT ms FROM MessageStructure ms WHERE ms.isActive = true " +
           "AND (:businessComponentId IS NULL OR ms.businessComponent.id = :businessComponentId) " +
           "AND (:search IS NULL OR LOWER(ms.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(ms.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MessageStructure> findAllWithFilters(@Param("businessComponentId") UUID businessComponentId,
                                            @Param("search") String search,
                                            Pageable pageable);
    
    @Query("SELECT ms FROM MessageStructure ms WHERE ms.businessComponent.id = :businessComponentId " +
           "AND ms.isActive = true ORDER BY ms.name")
    List<MessageStructure> findByBusinessComponentId(@Param("businessComponentId") UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, UUID businessComponentId, UUID id);
    
    boolean existsByNameAndIsActiveTrue(String name);
    
    Optional<MessageStructure> findByNameAndIsActiveTrue(String name);
}