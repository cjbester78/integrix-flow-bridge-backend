package com.integrixs.data.model;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FlowStructureMessageId implements Serializable {
    private UUID flowStructure;
    private FlowStructureMessage.MessageType messageType;
}