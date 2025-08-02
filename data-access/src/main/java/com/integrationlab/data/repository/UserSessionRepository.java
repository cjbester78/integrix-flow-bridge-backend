package com.integrationlab.data.repository;

import com.integrationlab.data.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repository interface for UserSessionRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refreshToken);

    void deleteAllByUser_Id(String userId);
}