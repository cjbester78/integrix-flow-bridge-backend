package com.integrationlab.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.integrationlab.model.ReusableFunction;

import java.util.Optional;

@Repository
/**
 * Repository interface for ReusableJavaFunctionRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface ReusableFunctionRepository extends JpaRepository<ReusableFunction, String> {

    Optional<ReusableFunction> findByName(String name);
}