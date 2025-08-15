package com.integrixs.shared.dto.structure;

import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStructureDTO {
    private String id;
    private String name;
    private String description;
    private String xsdContent;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private Integer version;
    private String sourceType;
    private Boolean isEditable;
    private Boolean isActive;
    private Map<String, Object> importMetadata;
    private BusinessComponentDTO businessComponent;
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}