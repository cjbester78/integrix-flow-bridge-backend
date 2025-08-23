package com.integrixs.backend.service;

import com.integrixs.data.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingCleanupService {
    
    private final SystemSettingRepository systemSettingRepository;
    
    @PostConstruct
    @Transactional
    public void cleanupDuplicateSettings() {
        try {
            // Remove the old max_retry_attempts setting if it exists
            systemSettingRepository.findBySettingKey("max_retry_attempts").ifPresent(setting -> {
                log.info("Removing deprecated max_retry_attempts setting");
                systemSettingRepository.delete(setting);
            });
            
            // Remove the old retry_delay setting if it exists
            systemSettingRepository.findBySettingKey("retry_delay").ifPresent(setting -> {
                log.info("Removing deprecated retry_delay setting");
                systemSettingRepository.delete(setting);
            });
            
            // Remove the old max_retries setting if it exists (duplicate of max_retry_attempts)
            systemSettingRepository.findBySettingKey("max_retries").ifPresent(setting -> {
                log.info("Removing deprecated max_retries setting");
                systemSettingRepository.delete(setting);
            });
            
            // Remove the old connection_pool_size setting (replaced by performance.connection.pool.size)
            systemSettingRepository.findBySettingKey("connection_pool_size").ifPresent(setting -> {
                log.info("Removing deprecated connection_pool_size setting");
                systemSettingRepository.delete(setting);
            });
        } catch (Exception e) {
            log.error("Error cleaning up duplicate settings", e);
        }
    }
}