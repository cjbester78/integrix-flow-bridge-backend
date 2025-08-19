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
    
    @Query(value = "SELECT * FROM message_structures ms WHERE ms.is_active = true " +
           "AND (:businessComponentId IS NULL OR ms.business_component_id = :businessComponentId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "(LOWER(ms.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (ms.description IS NOT NULL AND LOWER(CAST(ms.description AS TEXT)) LIKE LOWER(CONCAT('%', :search, '%')))))",
           countQuery = "SELECT COUNT(*) FROM message_structures ms WHERE ms.is_active = true " +
           "AND (:businessComponentId IS NULL OR ms.business_component_id = :businessComponentId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "(LOWER(ms.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (ms.description IS NOT NULL AND LOWER(CAST(ms.description AS TEXT)) LIKE LOWER(CONCAT('%', :search, '%')))))",
           nativeQuery = true)
    Page<MessageStructure> searchMessageStructures(@Param("businessComponentId") UUID businessComponentId,
                                                  @Param("search") String search,
                                                  Pageable pageable);
    
    List<MessageStructure> findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, UUID businessComponentId, UUID id);
    
    boolean existsByNameAndIsActiveTrue(String name);
    
    Optional<MessageStructure> findByNameAndIsActiveTrue(String name);
}