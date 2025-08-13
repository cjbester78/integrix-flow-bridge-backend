package com.integrationlab.data.repository;

import com.integrationlab.data.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SystemLog entities
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, String>, JpaSpecificationExecutor<SystemLog> {
    
    List<SystemLog> findBySourceAndLevelAndTimestampAfter(String source, SystemLog.LogLevel level, LocalDateTime timestamp);
    
    // Complex filtering will be handled by JPA Specifications in the service layer
    
    // Methods for DashboardService
    long countByComponentIdAndTimestampAfter(String componentId, LocalDateTime date);
    long countByTimestampAfter(LocalDateTime date);
    long countByComponentIdAndLevelAndTimestampAfter(String componentId, SystemLog.LogLevel level, LocalDateTime date);
    long countByLevelAndTimestampAfter(SystemLog.LogLevel level, LocalDateTime date);
    
    // Methods for MessageService
    List<SystemLog> findByComponentId(String componentId, org.springframework.data.domain.Pageable pageable);
    
    // Find logs by correlation ID
    List<SystemLog> findByCorrelationId(String correlationId);
    
    // Methods for adapter monitoring
    List<SystemLog> findByMessageContainingAndSourceOrderByTimestampDesc(String message, String source);
    
    List<SystemLog> findByMessageContainingOrderByTimestampDesc(String message);
}