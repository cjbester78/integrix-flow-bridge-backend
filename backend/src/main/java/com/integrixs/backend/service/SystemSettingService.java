package com.integrixs.backend.service;

import com.integrixs.data.model.SystemSetting;
import com.integrixs.data.repository.SystemSettingRepository;
import com.integrixs.shared.dto.system.SystemSettingDTO;
import com.integrixs.shared.dto.GlobalRetrySettingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemSettingService {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingService.class);

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    /**
     * Get all system settings
     */
    public List<SystemSettingDTO> getAllSettings() {
        logger.debug("Retrieving all system settings");
        return systemSettingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get settings by category
     */
    public List<SystemSettingDTO> getSettingsByCategory(String category) {
        logger.debug("Retrieving system settings for category: {}", category);
        return systemSettingRepository.findByCategoryOrderBySettingKeyAsc(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific setting by key
     */
    public Optional<SystemSettingDTO> getSettingByKey(String settingKey) {
        logger.debug("Retrieving system setting: {}", settingKey);
        return systemSettingRepository.findBySettingKey(settingKey)
                .map(this::toDTO);
    }

    /**
     * Get setting value by key (returns null if not found)
     */
    public String getSettingValue(String settingKey) {
        return systemSettingRepository.findBySettingKey(settingKey)
                .map(SystemSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * Get setting value with default fallback
     */
    public String getSettingValue(String settingKey, String defaultValue) {
        String value = getSettingValue(settingKey);
        return value != null ? value : defaultValue;
    }

    /**
     * Create or update a system setting
     */
    public SystemSettingDTO saveSetting(SystemSettingDTO dto) {
        logger.info("Saving system setting: {}", dto.getSettingKey());
        
        SystemSetting setting;
        Optional<SystemSetting> existing = systemSettingRepository.findBySettingKey(dto.getSettingKey());
        
        if (existing.isPresent()) {
            setting = existing.get();
            if (setting.isReadonly()) {
                throw new IllegalArgumentException("Cannot modify readonly setting: " + dto.getSettingKey());
            }
            setting.setSettingValue(dto.getSettingValue());
            setting.setDescription(dto.getDescription());
            setting.setCategory(dto.getCategory());
            setting.setDataType(dto.getDataType());
            setting.setUpdatedAt(LocalDateTime.now());
            setting.setUpdatedBy(dto.getUpdatedBy());
        } else {
            setting = fromDTO(dto);
        }
        
        SystemSetting saved = systemSettingRepository.save(setting);
        logger.info("Successfully saved system setting: {}", saved.getSettingKey());
        return toDTO(saved);
    }

    /**
     * Delete a system setting
     */
    public void deleteSetting(String settingKey) {
        logger.info("Deleting system setting: {}", settingKey);
        
        Optional<SystemSetting> setting = systemSettingRepository.findBySettingKey(settingKey);
        if (setting.isPresent()) {
            if (setting.get().isReadonly()) {
                throw new IllegalArgumentException("Cannot delete readonly setting: " + settingKey);
            }
            systemSettingRepository.delete(setting.get());
            logger.info("Successfully deleted system setting: {}", settingKey);
        } else {
            throw new IllegalArgumentException("Setting not found: " + settingKey);
        }
    }

    /**
     * Get all distinct categories
     */
    public List<String> getCategories() {
        return systemSettingRepository.findByCategoryIsNotNullOrderByCategory()
                .stream()
                .map(SystemSetting::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Initialize default system settings if they don't exist
     */
    public void initializeDefaultSettings() {
        logger.info("Initializing default system settings");
        
        createSettingIfNotExists("base_domain", "http://localhost:8080", 
                "Base domain URL for generating endpoint URLs", "integration");
        
        createSettingIfNotExists("default_timeout", "30", 
                "Default timeout in seconds for adapter connections", "integration");
        
        createSettingIfNotExists("log_level", "INFO", 
                "Application log level (DEBUG, INFO, WARN, ERROR)", "system");
        
        createSettingIfNotExists("enable_ssl", "false", 
                "Enable SSL/TLS for secure connections", "security");
        
        logger.info("Default system settings initialization completed");
    }

    /**
     * Create a setting if it doesn't already exist
     */
    private void createSettingIfNotExists(String key, String value, String description, String category) {
        if (!systemSettingRepository.existsBySettingKey(key)) {
            SystemSetting setting = new SystemSetting(key, value, description, category);
            systemSettingRepository.save(setting);
            logger.debug("Created default setting: {} = {}", key, value);
        }
    }

    /**
     * Convert entity to DTO
     */
    private SystemSettingDTO toDTO(SystemSetting entity) {
        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setId(entity.getId());
        dto.setSettingKey(entity.getSettingKey());
        dto.setSettingValue(entity.getSettingValue());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setDataType(entity.getDataType());
        dto.setEncrypted(entity.isEncrypted());
        dto.setReadonly(entity.isReadonly());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    /**
     * Convert DTO to entity
     */
    private SystemSetting fromDTO(SystemSettingDTO dto) {
        SystemSetting entity = new SystemSetting();
        entity.setSettingKey(dto.getSettingKey());
        entity.setSettingValue(dto.getSettingValue());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setDataType(dto.getDataType());
        entity.setEncrypted(dto.isEncrypted());
        entity.setReadonly(dto.isReadonly());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        return entity;
    }

    /**
     * Get global retry settings
     */
    public GlobalRetrySettingsDTO getGlobalRetrySettings() {
        logger.debug("Retrieving global retry settings");
        
        GlobalRetrySettingsDTO settings = new GlobalRetrySettingsDTO();
        
        // Get retry enabled setting
        String retryEnabled = getSettingValue("global_retry_enabled", "true");
        settings.setEnabled(Boolean.parseBoolean(retryEnabled));
        
        // Get max retries
        String maxRetries = getSettingValue("global_max_retries", "3");
        settings.setMaxRetries(Integer.parseInt(maxRetries));
        
        // Get retry interval
        String retryInterval = getSettingValue("global_retry_interval", "30");
        settings.setRetryInterval(Integer.parseInt(retryInterval));
        
        // Get retry interval unit
        String retryIntervalUnit = getSettingValue("global_retry_interval_unit", "seconds");
        settings.setRetryIntervalUnit(retryIntervalUnit);
        
        return settings;
    }

    /**
     * Update global retry settings
     */
    public GlobalRetrySettingsDTO updateGlobalRetrySettings(GlobalRetrySettingsDTO dto) {
        logger.info("Updating global retry settings");
        
        // Save each setting
        saveSetting(createSettingDTO("global_retry_enabled", 
            String.valueOf(dto.isEnabled()), 
            "Enable/disable automatic retries globally", 
            "retry"));
            
        saveSetting(createSettingDTO("global_max_retries", 
            String.valueOf(dto.getMaxRetries()), 
            "Maximum number of retry attempts", 
            "retry"));
            
        saveSetting(createSettingDTO("global_retry_interval", 
            String.valueOf(dto.getRetryInterval()), 
            "Interval between retry attempts", 
            "retry"));
            
        saveSetting(createSettingDTO("global_retry_interval_unit", 
            dto.getRetryIntervalUnit(), 
            "Unit for retry interval (seconds, minutes, hours)", 
            "retry"));
        
        logger.info("Successfully updated global retry settings");
        return getGlobalRetrySettings();
    }

    /**
     * Helper method to create a SystemSettingDTO
     */
    private SystemSettingDTO createSettingDTO(String key, String value, String description, String category) {
        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setSettingKey(key);
        dto.setSettingValue(value);
        dto.setDescription(description);
        dto.setCategory(category);
        dto.setDataType("STRING");
        dto.setUpdatedBy("system");
        return dto;
    }
}