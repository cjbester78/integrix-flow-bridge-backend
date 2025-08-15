package com.integrationlab.data.repository;

import com.integrationlab.data.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for system configuration settings.
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
    
    /**
     * Find configuration by key
     */
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    
    /**
     * Check if configuration exists by key
     */
    boolean existsByConfigKey(String configKey);
}