package com.integrixs.backend.service;

import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.shared.dto.adapter.AdapterConfigDTO;
import com.integrixs.shared.dto.adapter.AdapterTestResultDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.adapters.core.AdapterMode;
import com.integrixs.adapters.core.BaseAdapter;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.adapters.core.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunicationAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationAdapterService.class);
    
    private final CommunicationAdapterRepository repository;
    private final BusinessComponentRepository businessComponentRepository;
    private final ObjectMapper objectMapper;
    private final AdapterFactoryManager factoryManager;

    public CommunicationAdapterService(CommunicationAdapterRepository repository,
                                     BusinessComponentRepository businessComponentRepository) {
        this.repository = repository;
        this.businessComponentRepository = businessComponentRepository;
        this.objectMapper = new ObjectMapper();
        this.factoryManager = AdapterFactoryManager.getInstance();
    }

    public AdapterConfigDTO createAdapter(AdapterConfigDTO dto) {
        logger.info("Creating {} adapter of type {} for business component {}", 
                    dto.getMode(), dto.getType(), dto.getBusinessComponentId());
        
        // Validate mode-specific configuration
        validateAdapterConfiguration(dto);
        
        CommunicationAdapter adapter = new CommunicationAdapter();
        adapter.setName(dto.getName());
        adapter.setType(AdapterType.valueOf(dto.getType().toUpperCase()));
        
        // Map DTO modes to internal AdapterMode
        AdapterMode adapterMode = mapDtoModeToAdapterMode(dto.getMode(), dto.getDirection());
        adapter.setMode(adapterMode);
        
        adapter.setConfiguration(dto.getConfigJson());
        adapter.setDescription(dto.getDescription());
        
        // Set direction based on mode (following reversed convention)
        // SENDER = OUTBOUND (receives from external), RECEIVER = INBOUND (sends to external)
        adapter.setDirection(adapterMode == AdapterMode.SENDER ? "OUTBOUND" : "INBOUND");
        
        // Properly set the business component entity, not just the ID
        BusinessComponent businessComponent = businessComponentRepository.findById(dto.getBusinessComponentId())
                .orElseThrow(() -> new IllegalArgumentException("Business component not found: " + dto.getBusinessComponentId()));
        adapter.setBusinessComponent(businessComponent);
        
        adapter.setActive(dto.isActive());
        
        CommunicationAdapter savedAdapter = repository.save(adapter);
        logger.info("Successfully created adapter: {} with ID: {}", savedAdapter.getName(), savedAdapter.getId());
        
        return toDTO(savedAdapter);
    }
    
    /**
     * Map DTO mode values to internal AdapterMode enum
     * Following reversed middleware convention:
     * SENDER = OUTBOUND (receives data FROM external systems)
     * RECEIVER = INBOUND (sends data TO external systems)
     */
    private AdapterMode mapDtoModeToAdapterMode(String mode, String direction) {
        // Support both mode and direction fields for flexibility
        if ("SENDER".equalsIgnoreCase(mode) || "OUTBOUND".equalsIgnoreCase(direction)) {
            return AdapterMode.SENDER;
        } else if ("RECEIVER".equalsIgnoreCase(mode) || "INBOUND".equalsIgnoreCase(direction)) {
            return AdapterMode.RECEIVER;
        } else {
            throw new IllegalArgumentException("Invalid adapter mode: " + mode + " / direction: " + direction);
        }
    }
    
    /**
     * Validate adapter configuration based on mode and type
     */
    private void validateAdapterConfiguration(AdapterConfigDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Adapter name is required");
        }
        
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Adapter type is required");
        }
        
        if (dto.getBusinessComponentId() == null || dto.getBusinessComponentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Business component ID is required");
        }
        
        // Mode-specific validation would go here
        // For now, we rely on the frontend to send only relevant fields
        logger.debug("Adapter configuration validation passed for {} {} adapter", 
                     dto.getType(), dto.getMode());
    }

    @Transactional(readOnly = true)
    public List<AdapterConfigDTO> getAllAdapters() {
        try {
            // Use repository method that properly handles the entity relationships
            List<CommunicationAdapter> adapters = repository.findAll();
            
            return adapters.stream()
                    .map(adapter -> {
                        // Access the business component within the transaction
                        AdapterConfigDTO dto = new AdapterConfigDTO();
                        dto.setId(adapter.getId());
                        dto.setName(adapter.getName());
                        dto.setType(adapter.getType().name());
                        dto.setMode(adapter.getMode().name());
                        dto.setConfigJson(adapter.getConfiguration());
                        dto.setDescription(adapter.getDescription());
                        dto.setActive(adapter.isActive());
                        
                        // Safely access the business component ID and name within transaction
                        if (adapter.getBusinessComponent() != null) {
                            dto.setBusinessComponentId(adapter.getBusinessComponent().getId());
                            dto.setBusinessComponentName(adapter.getBusinessComponent().getName());
                        }
                        
                        // Include stored direction
                        dto.setDirection(adapter.getDirection());
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all adapters", e);
            throw new RuntimeException("Error fetching adapters: " + e.getMessage(), e);
        }
    }

    public Optional<AdapterConfigDTO> getAdapterById(String id) {
        return repository.findById(id).map(this::toDTO);
    }

    public Optional<AdapterConfigDTO> updateAdapter(String id, AdapterConfigDTO dto) {
        return repository.findById(id).map(adapter -> {
            adapter.setName(dto.getName());
            adapter.setType(AdapterType.valueOf(dto.getType().toUpperCase()));
            
            AdapterMode adapterMode = AdapterMode.valueOf(dto.getMode().toUpperCase());
            adapter.setMode(adapterMode);
            
            // Set direction based on mode (following reversed convention)
            adapter.setDirection(adapterMode == AdapterMode.SENDER ? "OUTBOUND" : "INBOUND");
            
            adapter.setConfiguration(dto.getConfigJson());
            adapter.setDescription(dto.getDescription());
            
            // Properly set the business component entity, not just the ID
            BusinessComponent businessComponent = businessComponentRepository.findById(dto.getBusinessComponentId())
                    .orElseThrow(() -> new IllegalArgumentException("Business component not found: " + dto.getBusinessComponentId()));
            adapter.setBusinessComponent(businessComponent);
            
            adapter.setActive(dto.isActive());
            return toDTO(repository.save(adapter));
        });
    }

    public void deleteAdapter(String id) {
        repository.deleteById(id);
    }

    public Optional<AdapterConfigDTO> activateAdapter(String id) {
        return repository.findById(id).map(adapter -> {
            adapter.setActive(true);
            return toDTO(repository.save(adapter));
        });
    }

    public Optional<AdapterConfigDTO> deactivateAdapter(String id) {
        return repository.findById(id).map(adapter -> {
            adapter.setActive(false);
            return toDTO(repository.save(adapter));
        });
    }

    /**
     * Test an adapter configuration by creating and testing the actual adapter instance
     */
    public AdapterTestResultDTO testAdapter(String id, String testPayload) {
        Optional<CommunicationAdapter> adapterOpt = repository.findById(id);
        if (adapterOpt.isEmpty()) {
            return createFailureResult("Adapter not found with id: " + id);
        }
        
        CommunicationAdapter adapter = adapterOpt.get();
        return testAdapterConfiguration(adapter, testPayload);
    }
    
    /**
     * Test an adapter configuration without saving it first
     */
    public AdapterTestResultDTO testAdapterConfiguration(AdapterConfigDTO dto, String testPayload) {
        CommunicationAdapter adapter = new CommunicationAdapter();
        adapter.setType(AdapterType.valueOf(dto.getType().toUpperCase()));
        adapter.setMode(AdapterMode.valueOf(dto.getMode().toUpperCase()));
        adapter.setConfiguration(dto.getConfigJson());
        
        return testAdapterConfiguration(adapter, testPayload);
    }
    
    private AdapterTestResultDTO testAdapterConfiguration(CommunicationAdapter adapter, String testPayload) {
        try {
            // Convert AdapterType to com.integrixs.adapters.core.AdapterType
            com.integrixs.adapters.core.AdapterType adapterType = 
                com.integrixs.adapters.core.AdapterType.valueOf(adapter.getType().name());
            
            // Parse configuration JSON into appropriate config object
            Object configuration = parseConfiguration(adapter);
            
            // Create and initialize adapter
            BaseAdapter adapterInstance = factoryManager.createAndInitialize(
                adapterType, adapter.getMode(), configuration);
            
            // Test connection
            AdapterResult connectionTest = adapterInstance.testConnection();
            
            AdapterTestResultDTO result = new AdapterTestResultDTO();
            result.setSuccess(connectionTest.isSuccess());
            result.setMessage(connectionTest.isSuccess() ? "Connection test successful" : "Connection test failed");
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error testing adapter configuration", e);
            return createFailureResult("Test failed: " + e.getMessage());
        }
    }
    
    private Object parseConfiguration(CommunicationAdapter adapter) {
        try {
            // This is a simplified approach - in reality, you'd have a mapping
            // from AdapterType + AdapterMode to the appropriate config class
            String configClassName = String.format("com.integrixs.adapters.config.%s%sAdapterConfig",
                adapter.getType().name().substring(0, 1).toUpperCase() + 
                adapter.getType().name().substring(1).toLowerCase(),
                adapter.getMode().name().substring(0, 1).toUpperCase() + 
                adapter.getMode().name().substring(1).toLowerCase());
                
            Class<?> configClass = Class.forName(configClassName);
            return objectMapper.readValue(adapter.getConfiguration(), configClass);
            
        } catch (Exception e) {
            logger.error("Failed to parse adapter configuration", e);
            throw new RuntimeException("Invalid adapter configuration", e);
        }
    }
    
    private AdapterTestResultDTO createFailureResult(String message) {
        AdapterTestResultDTO result = new AdapterTestResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    @Transactional(readOnly = true)
    private AdapterConfigDTO toDTO(CommunicationAdapter adapter) {
        AdapterConfigDTO dto = new AdapterConfigDTO();
        dto.setId(adapter.getId());
        dto.setName(adapter.getName());
        dto.setType(adapter.getType().name());
        dto.setMode(adapter.getMode().name());
        dto.setConfigJson(adapter.getConfiguration());
        dto.setDescription(adapter.getDescription());
        dto.setActive(adapter.isActive());
        
        // Safely access business component ID and name
        // This method might be called outside transaction, so use the safer approach
        if (adapter.getBusinessComponent() != null) {
            dto.setBusinessComponentId(adapter.getBusinessComponent().getId());
            dto.setBusinessComponentName(adapter.getBusinessComponent().getName());
        }
        
        // Include stored direction
        dto.setDirection(adapter.getDirection());
        
        return dto;
    }
}