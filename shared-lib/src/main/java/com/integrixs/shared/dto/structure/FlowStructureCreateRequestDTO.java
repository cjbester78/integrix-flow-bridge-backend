package com.integrixs.shared.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowStructureCreateRequestDTO {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Processing mode is required")
    private FlowStructureDTO.ProcessingMode processingMode;
    
    @NotNull(message = "Direction is required")
    private FlowStructureDTO.Direction direction;
    
    private Map<String, Object> namespace;
    
    private Map<String, Object> metadata;
    
    private Set<String> tags;
    
    @NotNull(message = "Business component ID is required")
    private String businessComponentId;
    
    private Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds;
}