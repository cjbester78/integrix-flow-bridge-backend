package com.integrixs.data.repository;

import com.integrixs.data.model.TransformationCustomFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TransformationCustomFunction entities
 */
@Repository
public interface TransformationCustomFunctionRepository extends JpaRepository<TransformationCustomFunction, UUID>, JpaSpecificationExecutor<TransformationCustomFunction> {

    /**
     * Find function by name
     */
    Optional<TransformationCustomFunction> findByName(String name);

    /**
     * Check if a function exists with the given name but not the given ID
     */
    boolean existsByNameAndFunctionIdNot(String name, UUID functionId);

    /**
     * Find all public functions
     */
    List<TransformationCustomFunction> findByIsPublicTrue();

    /**
     * Find functions by language
     */
    List<TransformationCustomFunction> findByLanguage(TransformationCustomFunction.FunctionLanguage language);

    /**
     * Find functions by performance class
     */
    List<TransformationCustomFunction> findByPerformanceClass(TransformationCustomFunction.PerformanceClass performanceClass);

    /**
     * Find functions created by a specific user
     */
    List<TransformationCustomFunction> findByCreatedBy(String createdBy);

    /**
     * Search functions by name or description
     */
    @Query("SELECT f FROM TransformationCustomFunction f WHERE " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TransformationCustomFunction> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    /**
     * Find all built-in functions
     */
    List<TransformationCustomFunction> findByBuiltInTrue();
    
    /**
     * Find all custom (non-built-in) functions
     */
    List<TransformationCustomFunction> findByBuiltInFalse();
    
    /**
     * Find functions by category
     */
    List<TransformationCustomFunction> findByCategory(String category);
    
    /**
     * Find built-in functions by category
     */
    List<TransformationCustomFunction> findByBuiltInTrueAndCategory(String category);
}