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
public class FlowStructureDTO {
    private String id;
    private String name;
    private String description;
    private ProcessingMode processingMode;
    private Direction direction;
    private String wsdlContent;
    private String sourceType;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private Integer version;
    private Boolean isActive;
    private BusinessComponentDTO businessComponent;
    private Set<FlowStructureMessageDTO> flowStructureMessages;
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum ProcessingMode {
        SYNC,
        ASYNC
    }
    
    public enum Direction {
        SOURCE,
        TARGET
    }
}