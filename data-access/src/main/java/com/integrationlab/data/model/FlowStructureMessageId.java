package com.integrationlab.data.model;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FlowStructureMessageId implements Serializable {
    private String flowStructure;
    private FlowStructureMessage.MessageType messageType;
}