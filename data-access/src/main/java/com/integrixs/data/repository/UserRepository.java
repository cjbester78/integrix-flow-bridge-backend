package com.integrixs.data.repository;

import com.integrixs.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository interface for UserRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}