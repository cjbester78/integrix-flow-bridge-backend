package com.integrationlab.repository;

import com.integrationlab.model.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository interface for FieldMappingRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface FieldMappingRepository extends JpaRepository<FieldMapping, String> {
    List<FieldMapping> findByTransformationId(String transformationId);

	void deleteByTransformationId(String id);
}