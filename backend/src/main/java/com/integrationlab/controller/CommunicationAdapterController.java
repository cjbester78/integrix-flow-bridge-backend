package com.integrationlab.controller;

import com.integrationlab.service.CommunicationAdapterService;
import com.integrationlab.shared.dto.adapter.AdapterConfigDTO;
import com.integrationlab.shared.dto.adapter.AdapterTestRequestDTO;
import com.integrationlab.shared.dto.adapter.AdapterTestResultDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adapters")
public class CommunicationAdapterController {

    private final CommunicationAdapterService adapterService;

    public CommunicationAdapterController(CommunicationAdapterService adapterService) {
        this.adapterService = adapterService;
    }

    @PostMapping
    public ResponseEntity<AdapterConfigDTO> createAdapter(@RequestBody AdapterConfigDTO dto) {
        return ResponseEntity.ok(adapterService.createAdapter(dto));
    }

    @GetMapping
    public ResponseEntity<List<AdapterConfigDTO>> getAllAdapters() {
        return ResponseEntity.ok(adapterService.getAllAdapters());
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
}