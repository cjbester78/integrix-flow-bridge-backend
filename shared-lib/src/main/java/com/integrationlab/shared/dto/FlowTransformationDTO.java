package com.integrationlab.shared.dto;

import com.integrationlab.shared.enums.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for flow transformation data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTransformationDTO {
    private String id;
    private String flowId;
    private String name;
    private String description;
    private TransformationType transformationType;
    private String configuration;
    private Integer sequence;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}