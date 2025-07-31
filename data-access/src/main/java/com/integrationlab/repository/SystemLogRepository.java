package com.integrationlab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.integrationlab.model.SystemLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SystemLogRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface SystemLogRepository extends JpaRepository<SystemLog, String> {

    @Query("SELECT l FROM SystemLog l " +
           "WHERE (:level IS NULL OR l.level = :level) " +
           "AND (:source IS NULL OR l.source = :source) " +
           "AND (:userId IS NULL OR l.userId = :userId) " +
           "AND (:from IS NULL OR l.timestamp >= :from) " +
           "AND (:to IS NULL OR l.timestamp <= :to)")
    List<SystemLog> findFiltered(
        @Param("level") String level,
        @Param("source") String source,
        @Param("userId") String userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}