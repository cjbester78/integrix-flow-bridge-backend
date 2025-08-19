package com.integrixs.data.repository;

import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowStructureMessage;
import com.integrixs.data.model.FlowStructureMessageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlowStructureMessageRepository extends JpaRepository<FlowStructureMessage, FlowStructureMessageId> {
    
    List<FlowStructureMessage> findByFlowStructureId(UUID flowStructureId);
    
    Optional<FlowStructureMessage> findByFlowStructureAndMessageType(FlowStructure flowStructure,
                                                                    FlowStructureMessage.MessageType messageType);
    
    void deleteByFlowStructureId(UUID flowStructureId);
    
    @Query("SELECT DISTINCT fsm.flowStructure FROM FlowStructureMessage fsm WHERE fsm.messageStructure.id = :messageStructureId")
    List<FlowStructure> findFlowStructuresByMessageStructureId(@Param("messageStructureId") UUID messageStructureId);
}