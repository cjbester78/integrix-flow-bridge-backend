package com.integrixs.data.repository;

import com.integrixs.data.model.BusinessComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
/**
 * Repository interface for BusinessComponentRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface BusinessComponentRepository extends JpaRepository<BusinessComponent, UUID> {
    boolean existsByName(String name);
}