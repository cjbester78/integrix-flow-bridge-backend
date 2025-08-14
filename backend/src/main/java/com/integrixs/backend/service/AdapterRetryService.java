package com.integrixs.backend.service;

import com.integrixs.shared.dto.GlobalRetrySettingsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterRetryService {
    
    private final SystemSettingService systemSettingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get effective retry settings for an adapter.
     * This merges global settings with adapter-specific overrides.
     * 
     * @param adapterConfiguration JSON string containing adapter configuration
     * @return Effective retry settings
     */
    public GlobalRetrySettingsDTO getEffectiveRetrySettings(String adapterConfiguration) {
        try {
            // Get global retry settings as baseline
            GlobalRetrySettingsDTO globalSettings = systemSettingService.getGlobalRetrySettings();
            
            if (adapterConfiguration == null || adapterConfiguration.isEmpty()) {
                return globalSettings;
            }
            
            // Parse adapter configuration
            JsonNode configNode = objectMapper.readTree(adapterConfiguration);
            JsonNode retryNode = configNode.path("retrySettings");
            
            // Check if adapter uses global settings
            if (retryNode.path("useGlobalSettings").asBoolean(true)) {
                return globalSettings;
            }
            
            // If adapter has custom settings, use those
            if (!retryNode.isMissingNode()) {
                GlobalRetrySettingsDTO adapterSettings = new GlobalRetrySettingsDTO();
                adapterSettings.setEnabled(retryNode.path("enabled").asBoolean(globalSettings.isEnabled()));
                adapterSettings.setMaxRetries(retryNode.path("maxRetries").asInt(globalSettings.getMaxRetries()));
                adapterSettings.setRetryInterval(retryNode.path("retryInterval").asInt(globalSettings.getRetryInterval()));
                adapterSettings.setRetryIntervalUnit(
                    retryNode.path("retryIntervalUnit").asText(globalSettings.getRetryIntervalUnit())
                );
                return adapterSettings;
            }
            
            // Default to global settings
            return globalSettings;
            
        } catch (Exception e) {
            log.error("Error parsing adapter retry configuration, using global defaults", e);
            return systemSettingService.getGlobalRetrySettings();
        }
    }
    
    /**
     * Calculate retry delay in milliseconds based on settings
     * 
     * @param settings Retry settings
     * @return Delay in milliseconds
     */
    public long calculateRetryDelayMillis(GlobalRetrySettingsDTO settings) {
        int interval = settings.getRetryInterval();
        String unit = settings.getRetryIntervalUnit();
        
        switch (unit.toLowerCase()) {
            case "hours":
                return interval * 60L * 60L * 1000L;
            case "minutes":
                return interval * 60L * 1000L;
            case "seconds":
            default:
                return interval * 1000L;
        }
    }
    
    /**
     * Update adapter configuration with retry settings
     * 
     * @param adapterConfiguration Current adapter configuration JSON
     * @param retrySettings New retry settings
     * @return Updated configuration JSON
     */
    public String updateAdapterRetrySettings(String adapterConfiguration, GlobalRetrySettingsDTO retrySettings, boolean useGlobalSettings) {
        try {
            ObjectNode configNode;
            if (adapterConfiguration == null || adapterConfiguration.isEmpty()) {
                configNode = objectMapper.createObjectNode();
            } else {
                configNode = (ObjectNode) objectMapper.readTree(adapterConfiguration);
            }
            
            ObjectNode retryNode = objectMapper.createObjectNode();
            retryNode.put("useGlobalSettings", useGlobalSettings);
            
            if (!useGlobalSettings) {
                retryNode.put("enabled", retrySettings.isEnabled());
                retryNode.put("maxRetries", retrySettings.getMaxRetries());
                retryNode.put("retryInterval", retrySettings.getRetryInterval());
                retryNode.put("retryIntervalUnit", retrySettings.getRetryIntervalUnit());
            }
            
            configNode.set("retrySettings", retryNode);
            
            return objectMapper.writeValueAsString(configNode);
            
        } catch (Exception e) {
            log.error("Error updating adapter retry settings", e);
            return adapterConfiguration;
        }
    }
}