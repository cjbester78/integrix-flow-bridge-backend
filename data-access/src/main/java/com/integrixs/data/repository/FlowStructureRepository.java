package com.integrixs.data.repository;

import com.integrixs.data.model.FlowStructure;
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
public interface FlowStructureRepository extends JpaRepository<FlowStructure, UUID> {
    
    Optional<FlowStructure> findByIdAndIsActiveTrue(UUID id);
    
    List<FlowStructure> findAllByIsActiveTrue();
    
    @Query(value = "SELECT * FROM flow_structures fs WHERE fs.is_active = true " +
           "AND (:businessComponentId IS NULL OR fs.business_component_id = :businessComponentId) " +
           "AND (:processingMode IS NULL OR fs.processing_mode = :processingMode) " +
           "AND (:direction IS NULL OR fs.direction = :direction) " +
           "AND (:search IS NULL OR LOWER(fs.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (fs.description IS NOT NULL AND LOWER(CAST(fs.description AS TEXT)) LIKE LOWER(CONCAT('%', :search, '%'))))",
           nativeQuery = true)
    Page<FlowStructure> findAllWithFilters(@Param("businessComponentId") UUID businessComponentId,
                                         @Param("processingMode") String processingMode,
                                         @Param("direction") String direction,
                                         @Param("search") String search,
                                         Pageable pageable);
    
    List<FlowStructure> findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, UUID businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, UUID businessComponentId, UUID id);
}