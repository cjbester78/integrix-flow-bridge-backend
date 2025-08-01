package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for IntegrationFlow entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationFlowDTO {
    private String id;
    private String name;
    private String description;
    private String sourceAdapterId;
    private String targetAdapterId;
    private String sourceStructureId;
    private String targetStructureId;
    private String status;
    private String configuration;
    private boolean isActive;
    private String mappingMode; // WITH_MAPPING or PASS_THROUGH
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastExecutionAt;
    private Integer executionCount;
    private Integer successCount;
    private Integer errorCount;
    private String businessComponentId;
}