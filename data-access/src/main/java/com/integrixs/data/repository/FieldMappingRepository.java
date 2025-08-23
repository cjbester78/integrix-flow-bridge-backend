package com.integrixs.data.repository;

import com.integrixs.data.model.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
/**
 * Repository interface for FieldMappingRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {
    List<FieldMapping> findByTransformationId(UUID transformationId);

	void deleteByTransformationId(UUID id);
	
	List<FieldMapping> findByTransformationFlowIdAndIsActiveTrueOrderByTransformationExecutionOrder(UUID flowId);
}