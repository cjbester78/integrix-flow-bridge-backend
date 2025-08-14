package com.integrixs.backend.controller;

import com.integrixs.backend.service.CommunicationAdapterService;
import com.integrixs.backend.service.SystemLogService;
import com.integrixs.shared.dto.adapter.AdapterConfigDTO;
import com.integrixs.shared.dto.adapter.AdapterTestRequestDTO;
import com.integrixs.shared.dto.adapter.AdapterTestResultDTO;
import com.integrixs.shared.dto.system.SystemLogDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/adapters")
@CrossOrigin(origins = "*")
public class CommunicationAdapterController {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationAdapterController.class);
    private final CommunicationAdapterService adapterService;
    private final SystemLogService systemLogService;

    public CommunicationAdapterController(CommunicationAdapterService adapterService, SystemLogService systemLogService) {
        this.adapterService = adapterService;
        this.systemLogService = systemLogService;
    }

    @PostMapping
    public ResponseEntity<AdapterConfigDTO> createAdapter(@RequestBody AdapterConfigDTO dto) {
        logger.info("Creating adapter - Received DTO: {}", dto);
        logger.info("Business Component ID: {}", dto.getBusinessComponentId());
        logger.info("Adapter Type: {}", dto.getType());
        logger.info("Adapter Mode: {}", dto.getMode());
        
        try {
            AdapterConfigDTO result = adapterService.createAdapter(dto);
            logger.info("Successfully created adapter with ID: {}", result.getId());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating adapter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating adapter", e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<AdapterConfigDTO>> getAllAdapters() {
        try {
            logger.info("Getting all adapters");
            List<AdapterConfigDTO> adapters = adapterService.getAllAdapters();
            logger.info("Successfully retrieved {} adapters", adapters.size());
            return ResponseEntity.ok(adapters);
        } catch (Exception e) {
            logger.error("Error getting all adapters", e);
            // Return empty list to prevent frontend errors
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdapterConfigDTO> getAdapterById(@PathVariable String id) {
        return adapterService.getAdapterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdapterConfigDTO> updateAdapter(@PathVariable String id,
                                                         @RequestBody AdapterConfigDTO dto) {
        return adapterService.updateAdapter(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdapter(@PathVariable String id) {
        adapterService.deleteAdapter(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AdapterConfigDTO> activateAdapter(@PathVariable String id) {
        return adapterService.activateAdapter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<AdapterConfigDTO> deactivateAdapter(@PathVariable String id) {
        return adapterService.deactivateAdapter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<AdapterConfigDTO> cloneAdapter(@PathVariable String id) {
        return adapterService.getAdapterById(id)
                .map(adapter -> {
                    adapter.setName(adapter.getName() + " (Copy)");
                    return ResponseEntity.ok(adapterService.createAdapter(adapter));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<AdapterTestResultDTO> testAdapter(@PathVariable String id, 
                                                          @RequestBody AdapterTestRequestDTO request) {
        String testPayload = request.getPayload();
        AdapterTestResultDTO result = adapterService.testAdapter(id, testPayload);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/test")
    public ResponseEntity<AdapterTestResultDTO> testAdapterConfiguration(@RequestBody AdapterTestRequestDTO request) {
        // AdapterConfigDTO config = request.getAdapterConfig(); TODO: Fix this
        String testPayload = request.getPayload();
        AdapterTestResultDTO result = adapterService.testAdapterConfiguration(null, testPayload); // TODO: Fix config parameter
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<SystemLogDTO>> getAdapterLogs(@PathVariable String id, Pageable pageable) {
        logger.info("Getting logs for adapter: {}", id);
        Page<SystemLogDTO> logs = systemLogService.getAdapterLogs(id, pageable);
        return ResponseEntity.ok(logs);
    }
}