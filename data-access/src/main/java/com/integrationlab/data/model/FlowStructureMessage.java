package com.integrationlab.data.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flow_structure_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"flowStructure", "messageStructure"})
@IdClass(FlowStructureMessageId.class)
public class FlowStructureMessage {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_structure_id", nullable = false)
    @EqualsAndHashCode.Include
    private FlowStructure flowStructure;
    
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @EqualsAndHashCode.Include
    private MessageType messageType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_structure_id", nullable = false)
    private MessageStructure messageStructure;
    
    public enum MessageType {
        INPUT,
        OUTPUT,
        FAULT
    }
}