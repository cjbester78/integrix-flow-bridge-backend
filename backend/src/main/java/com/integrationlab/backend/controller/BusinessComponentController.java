package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.BusinessComponentService;
import com.integrationlab.shared.dto.business.BusinessComponentDTO;
import com.integrationlab.shared.dto.business.BusinessComponentCreateRequestDTO;
import com.integrationlab.shared.dto.business.BusinessComponentUpdateRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/business-components")
public class BusinessComponentController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessComponentController.class);
    private final BusinessComponentService businessComponentService;

    public BusinessComponentController(BusinessComponentService businessComponentService) {
        this.businessComponentService = businessComponentService;
    }

    @PostMapping
    public ResponseEntity<BusinessComponentDTO> createBusinessComponent(@RequestBody BusinessComponentCreateRequestDTO dto) {
        logger.info("=== FRONTEND REQUEST === POST /api/business-components");
        logger.info("Request payload: name='{}', description='{}', email='{}', phone='{}'", 
                   dto.getName(), dto.getDescription(), dto.getContactEmail(), dto.getContactPhone());
        
        try {
            BusinessComponentDTO result = businessComponentService.createBusinessComponent(dto);
            logger.info("=== SUCCESS === Created business component with ID: {}", result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("=== ERROR === Failed to create business component: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<BusinessComponentDTO>> getAllBusinessComponents() {
        logger.info("=== FRONTEND REQUEST === GET /api/business-components");
        List<BusinessComponentDTO> components = businessComponentService.getAllBusinessComponents();
        logger.info("=== RESPONSE === Returning {} business components", components.size());
        return ResponseEntity.ok(components);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessComponentDTO> getBusinessComponentById(@PathVariable("id") String id) {
        return businessComponentService.getBusinessComponentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessComponentDTO> updateBusinessComponent(@PathVariable("id") String id,
                                                      @RequestBody BusinessComponentUpdateRequestDTO dto) {
        return businessComponentService.updateBusinessComponent(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessComponent(@PathVariable("id") String id) {
        businessComponentService.deleteBusinessComponent(id);
        return ResponseEntity.noContent().build();
    }
}
