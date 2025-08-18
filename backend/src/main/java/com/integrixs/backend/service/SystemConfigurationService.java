package com.integrixs.backend.service;

import com.integrixs.data.model.SystemConfiguration;
import com.integrixs.data.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Service for managing system configuration settings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigurationService {
    
    private final SystemConfigurationRepository configurationRepository;
    
    // Configuration keys
    public static final String TIMEZONE_KEY = "system.timezone";
    public static final String DATE_FORMAT_KEY = "system.dateFormat";
    public static final String TIME_FORMAT_KEY = "system.timeFormat";
    public static final String DATETIME_FORMAT_KEY = "system.dateTimeFormat";
    
    // Default values
    private static final String DEFAULT_TIMEZONE = "Africa/Johannesburg";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Get system timezone
     */
    @Cacheable(value = "systemConfig", key = "'timezone'")
    public String getSystemTimezone() {
        return getConfigValue(TIMEZONE_KEY, DEFAULT_TIMEZONE);
    }
    
    /**
     * Get system date format
     */
    @Cacheable(value = "systemConfig", key = "'dateFormat'")
    public String getDateFormat() {
        return getConfigValue(DATE_FORMAT_KEY, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Get system time format
     */
    @Cacheable(value = "systemConfig", key = "'timeFormat'")
    public String getTimeFormat() {
        return getConfigValue(TIME_FORMAT_KEY, DEFAULT_TIME_FORMAT);
    }
    
    /**
     * Get system datetime format
     */
    @Cacheable(value = "systemConfig", key = "'dateTimeFormat'")
    public String getDateTimeFormat() {
        return getConfigValue(DATETIME_FORMAT_KEY, DEFAULT_DATETIME_FORMAT);
    }
    
    /**
     * Get all system configurations
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllConfigurations() {
        Map<String, String> configs = new HashMap<>();
        List<SystemConfiguration> allConfigs = configurationRepository.findAll();
        
        for (SystemConfiguration config : allConfigs) {
            configs.put(config.getConfigKey(), config.getConfigValue());
        }
        
        // Add defaults if not present
        configs.putIfAbsent(TIMEZONE_KEY, DEFAULT_TIMEZONE);
        configs.putIfAbsent(DATE_FORMAT_KEY, DEFAULT_DATE_FORMAT);
        configs.putIfAbsent(TIME_FORMAT_KEY, DEFAULT_TIME_FORMAT);
        configs.putIfAbsent(DATETIME_FORMAT_KEY, DEFAULT_DATETIME_FORMAT);
        
        return configs;
    }
    
    /**
     * Update system timezone
     */
    @Transactional
    @CacheEvict(value = "systemConfig", key = "'timezone'")
    public void updateSystemTimezone(String timezone, String updatedBy) {
        // Validate timezone
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }
        
        updateConfigValue(TIMEZONE_KEY, timezone, "System timezone", updatedBy);
        log.info("System timezone updated to: {} by {}", timezone, updatedBy);
    }
    
    /**
     * Update date format
     */
    @Transactional
    @CacheEvict(value = "systemConfig", key = "'dateFormat'")
    public void updateDateFormat(String format, String updatedBy) {
        updateConfigValue(DATE_FORMAT_KEY, format, "Date display format", updatedBy);
        log.info("Date format updated to: {} by {}", format, updatedBy);
    }
    
    /**
     * Update time format
     */
    @Transactional
    @CacheEvict(value = "systemConfig", key = "'timeFormat'")
    public void updateTimeFormat(String format, String updatedBy) {
        updateConfigValue(TIME_FORMAT_KEY, format, "Time display format", updatedBy);
        log.info("Time format updated to: {} by {}", format, updatedBy);
    }
    
    /**
     * Update datetime format
     */
    @Transactional
    @CacheEvict(value = "systemConfig", key = "'dateTimeFormat'")
    public void updateDateTimeFormat(String format, String updatedBy) {
        updateConfigValue(DATETIME_FORMAT_KEY, format, "DateTime display format", updatedBy);
        log.info("DateTime format updated to: {} by {}", format, updatedBy);
    }
    
    /**
     * Get available timezones
     */
    public List<Map<String, String>> getAvailableTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
            .sorted()
            .map(zoneId -> {
                Map<String, String> timezone = new HashMap<>();
                timezone.put("id", zoneId);
                timezone.put("displayName", getTimezoneDisplayName(zoneId));
                return timezone;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get configuration value by key
     */
    private String getConfigValue(String key, String defaultValue) {
        Optional<SystemConfiguration> config = configurationRepository.findByConfigKey(key);
        return config.map(SystemConfiguration::getConfigValue).orElse(defaultValue);
    }
    
    /**
     * Update configuration value
     */
    private void updateConfigValue(String key, String value, String description, String updatedBy) {
        SystemConfiguration config = configurationRepository.findByConfigKey(key)
            .orElse(SystemConfiguration.builder()
                .configKey(key)
                .configType("STRING")
                .description(description)
                .build());
        
        config.setConfigValue(value);
        // TODO: Fix this - updatedBy is a String but setUpdatedBy expects User
        // config.setUpdatedBy(updatedBy);
        
        configurationRepository.save(config);
    }
    
    /**
     * Get timezone display name
     */
    private String getTimezoneDisplayName(String zoneId) {
        try {
            TimeZone tz = TimeZone.getTimeZone(zoneId);
            int offset = tz.getRawOffset() / 3600000;
            String offsetStr = offset >= 0 ? "+" + offset : String.valueOf(offset);
            return String.format("%s (UTC%s)", zoneId, offsetStr);
        } catch (Exception e) {
            return zoneId;
        }
    }
}