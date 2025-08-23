package com.integrixs.backend.controller;

import com.integrixs.backend.service.TestMappingService;
import com.integrixs.shared.dto.TestFieldMappingsRequestDTO;
import com.integrixs.shared.dto.TestFieldMappingsResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing field mappings without deploying flows
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestMappingController {

    private final TestMappingService testMappingService;

    /**
     * Test field mappings with sample XML
     */
    @PostMapping("/field-mappings")
    public ResponseEntity<TestFieldMappingsResponseDTO> testFieldMappings(
            @RequestBody TestFieldMappingsRequestDTO request) {
        log.info("Testing field mappings for type: {}", request.getMappingType());
        
        try {
            TestFieldMappingsResponseDTO response = testMappingService.testFieldMappings(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing field mappings", e);
            return ResponseEntity.ok(TestFieldMappingsResponseDTO.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }
}