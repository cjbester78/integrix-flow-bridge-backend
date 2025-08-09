package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.SystemSettingService;
import com.integrationlab.shared.dto.system.SystemSettingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system-settings")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class SystemSettingController {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingController.class);

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * Get all system settings
     */
    @GetMapping
    public ResponseEntity<List<SystemSettingDTO>> getAllSettings() {
        logger.debug("REST request to get all system settings");
        List<SystemSettingDTO> settings = systemSettingService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Get settings by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SystemSettingDTO>> getSettingsByCategory(@PathVariable String category) {
        logger.debug("REST request to get system settings for category: {}", category);
        List<SystemSettingDTO> settings = systemSettingService.getSettingsByCategory(category);
        return ResponseEntity.ok(settings);
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        logger.debug("REST request to get all setting categories");
        List<String> categories = systemSettingService.getCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific setting by key
     */
    @GetMapping("/key/{settingKey}")
    public ResponseEntity<SystemSettingDTO> getSettingByKey(@PathVariable String settingKey) {
        logger.debug("REST request to get system setting: {}", settingKey);
        Optional<SystemSettingDTO> setting = systemSettingService.getSettingByKey(settingKey);
        return setting.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get setting value by key
     */
    @GetMapping("/value/{settingKey}")
    public ResponseEntity<String> getSettingValue(@PathVariable String settingKey) {
        logger.debug("REST request to get setting value: {}", settingKey);
        String value = systemSettingService.getSettingValue(settingKey);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * Create or update a system setting
     */
    @PostMapping
    public ResponseEntity<SystemSettingDTO> saveSetting(@RequestBody SystemSettingDTO dto) {
        logger.info("REST request to save system setting: {}", dto.getSettingKey());
        try {
            SystemSettingDTO saved = systemSettingService.saveSetting(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            logger.error("Error saving system setting: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing system setting
     */
    @PutMapping("/{settingKey}")
    public ResponseEntity<SystemSettingDTO> updateSetting(@PathVariable String settingKey, 
                                                          @RequestBody SystemSettingDTO dto) {
        logger.info("REST request to update system setting: {}", settingKey);
        try {
            dto.setSettingKey(settingKey);
            SystemSettingDTO updated = systemSettingService.saveSetting(dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating system setting: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a system setting
     */
    @DeleteMapping("/{settingKey}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String settingKey) {
        logger.info("REST request to delete system setting: {}", settingKey);
        try {
            systemSettingService.deleteSetting(settingKey);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting system setting: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Initialize default settings
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeDefaultSettings() {
        logger.info("REST request to initialize default system settings");
        systemSettingService.initializeDefaultSettings();
        return ResponseEntity.ok("Default settings initialized successfully");
    }

    /**
     * Get base domain for endpoint URL generation
     */
    @GetMapping("/base-domain")
    public ResponseEntity<String> getBaseDomain() {
        logger.debug("REST request to get base domain");
        String baseDomain = systemSettingService.getSettingValue("base_domain", "http://localhost:8080");
        return ResponseEntity.ok(baseDomain);
    }
}