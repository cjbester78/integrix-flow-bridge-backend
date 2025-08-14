package com.integrixs.data.repository;

import com.integrixs.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for UserRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}