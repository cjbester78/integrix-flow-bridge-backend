package com.integrationlab.shared.dto.structure;

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
public class MessageStructureCreateRequestDTO {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "XSD content is required")
    private String xsdContent;
    
    private Map<String, Object> namespace;
    
    private Map<String, Object> metadata;
    
    private Set<String> tags;
    
    @NotNull(message = "Business component ID is required")
    private String businessComponentId;
}