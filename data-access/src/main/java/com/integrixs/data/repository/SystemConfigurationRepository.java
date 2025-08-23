package com.integrixs.data.repository;

import com.integrixs.data.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for system configuration settings.
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, UUID> {
    
    /**
     * Find configuration by key
     */
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    
    /**
     * Check if configuration exists by key
     */
    boolean existsByConfigKey(String configKey);
}