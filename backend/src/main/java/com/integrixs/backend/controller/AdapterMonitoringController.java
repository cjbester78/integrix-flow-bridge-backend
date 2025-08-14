package com.integrixs.backend.controller;

import com.integrixs.backend.service.AdapterMonitoringService;
import com.integrixs.shared.dto.AdapterStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adapter-monitoring")
@CrossOrigin(origins = "*")
public class AdapterMonitoringController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdapterMonitoringController.class);
    
    private final AdapterMonitoringService adapterMonitoringService;
    
    public AdapterMonitoringController(AdapterMonitoringService adapterMonitoringService) {
        this.adapterMonitoringService = adapterMonitoringService;
    }
    
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<List<AdapterStatusDTO>> getAdapterStatuses(
            @RequestParam(required = false) String businessComponentId) {
        log.debug("Getting adapter statuses for businessComponentId: {}", businessComponentId);
        List<AdapterStatusDTO> statuses = adapterMonitoringService.getAdapterStatuses(businessComponentId);
        return ResponseEntity.ok(statuses);
    }
    
    @GetMapping("/status/{adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<AdapterStatusDTO> getAdapterStatus(@PathVariable String adapterId) {
        log.debug("Getting status for adapter: {}", adapterId);
        AdapterStatusDTO status = adapterMonitoringService.getAdapterStatus(adapterId);
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/start/{adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterStatusDTO> startAdapter(@PathVariable String adapterId) {
        log.info("Starting adapter: {}", adapterId);
        AdapterStatusDTO status = adapterMonitoringService.startAdapter(adapterId);
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/stop/{adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterStatusDTO> stopAdapter(@PathVariable String adapterId) {
        log.info("Stopping adapter: {}", adapterId);
        AdapterStatusDTO status = adapterMonitoringService.stopAdapter(adapterId);
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/restart/{adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterStatusDTO> restartAdapter(@PathVariable String adapterId) {
        log.info("Restarting adapter: {}", adapterId);
        AdapterStatusDTO status = adapterMonitoringService.restartAdapter(adapterId);
        return ResponseEntity.ok(status);
    }
}