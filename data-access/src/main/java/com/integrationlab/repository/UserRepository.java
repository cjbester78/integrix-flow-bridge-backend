package com.integrationlab.repository;

import com.integrationlab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for UserRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}