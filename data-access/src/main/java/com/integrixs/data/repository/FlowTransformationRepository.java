package com.integrixs.data.repository;

import com.integrixs.data.model.FlowTransformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository interface for FlowTransformationRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface FlowTransformationRepository extends JpaRepository<FlowTransformation, String> {
    List<FlowTransformation> findByFlowId(String flowId);

	void deleteByFlowId(String flowId);
    
    // Find transformation with field mappings eagerly loaded
    @EntityGraph(attributePaths = {"fieldMappings"})
    Optional<FlowTransformation> findWithFieldMappingsById(String id);
    
    // Find all transformations for a flow with field mappings eagerly loaded
    @EntityGraph(attributePaths = {"fieldMappings"})
    List<FlowTransformation> findWithFieldMappingsByFlowId(String flowId);
}