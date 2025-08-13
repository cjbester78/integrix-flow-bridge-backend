package com.integrationlab.backend.service;

import com.integrationlab.data.repository.SystemSettingRepository;
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
    public void cleanupDuplicateRetrySettings() {
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
        } catch (Exception e) {
            log.error("Error cleaning up duplicate retry settings", e);
        }
    }
}