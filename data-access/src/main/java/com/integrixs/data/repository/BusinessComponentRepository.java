package com.integrixs.data.repository;

import com.integrixs.data.model.BusinessComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository interface for BusinessComponentRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface BusinessComponentRepository extends JpaRepository<BusinessComponent, String> {
    boolean existsByName(String name);
}