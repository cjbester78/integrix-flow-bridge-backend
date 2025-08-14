package com.integrationlab.shared.dto.structure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowStructureMessageDTO {
    private String flowStructureId;
    private MessageType messageType;
    private MessageStructureDTO messageStructure;
    
    public enum MessageType {
        INPUT,
        OUTPUT,
        FAULT
    }
}