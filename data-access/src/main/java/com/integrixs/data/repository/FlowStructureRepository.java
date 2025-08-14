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

@Repository
public interface FlowStructureRepository extends JpaRepository<FlowStructure, String> {
    
    Optional<FlowStructure> findByIdAndIsActiveTrue(String id);
    
    List<FlowStructure> findAllByIsActiveTrue();
    
    @Query("SELECT fs FROM FlowStructure fs WHERE fs.isActive = true " +
           "AND (:businessComponentId IS NULL OR fs.businessComponent.id = :businessComponentId) " +
           "AND (:processingMode IS NULL OR fs.processingMode = :processingMode) " +
           "AND (:direction IS NULL OR fs.direction = :direction) " +
           "AND (:search IS NULL OR LOWER(fs.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(fs.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<FlowStructure> findAllWithFilters(@Param("businessComponentId") String businessComponentId,
                                         @Param("processingMode") FlowStructure.ProcessingMode processingMode,
                                         @Param("direction") FlowStructure.Direction direction,
                                         @Param("search") String search,
                                         Pageable pageable);
    
    @Query("SELECT fs FROM FlowStructure fs WHERE fs.businessComponent.id = :businessComponentId " +
           "AND fs.isActive = true ORDER BY fs.name")
    List<FlowStructure> findByBusinessComponentId(@Param("businessComponentId") String businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, String businessComponentId);
    
    boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, String businessComponentId, String id);
}