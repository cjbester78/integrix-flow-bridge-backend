package com.integrixs.data.repository;

import com.integrixs.data.model.FlowTransformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository interface for FlowTransformationRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface FlowTransformationRepository extends JpaRepository<FlowTransformation, String> {
    List<FlowTransformation> findByFlowId(String flowId);

	void deleteByFlowId(String flowId);
}