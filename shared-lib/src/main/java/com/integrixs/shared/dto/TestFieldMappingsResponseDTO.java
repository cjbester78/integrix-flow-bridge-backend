package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for testing field mappings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestFieldMappingsResponseDTO {
    
    private boolean success;
    private String outputXml;
    private String error;
    private List<String> warnings;
    private long executionTimeMs;
}