package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for field mapping data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingDTO {
    private String id;
    private String transformationId;
    private String sourceFields;
    private String targetField;
    private String javaFunction;
    private String mappingRule;
    private String sourceXPath;
    private String targetXPath;
    private boolean isArrayMapping;
    private String arrayContextPath;
    private boolean namespaceAware;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}