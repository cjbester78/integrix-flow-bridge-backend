package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowDTO {
    private String id;
    private String name;
    private String uniqueFlowId;
    private String description;
    private String type;
    private String sourceAdapterId;
    private String targetAdapterId;
    private String sourceBusinessComponentId;
    private String targetBusinessComponentId;
    private String sourceFlowStructureId;
    private String targetFlowStructureId;
    private String status;
    private String configuration;
    private String mappingMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}