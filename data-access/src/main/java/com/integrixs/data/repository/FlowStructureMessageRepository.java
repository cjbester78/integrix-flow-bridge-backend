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

@Repository
public interface FlowStructureMessageRepository extends JpaRepository<FlowStructureMessage, FlowStructureMessageId> {
    
    @Query("SELECT fsm FROM FlowStructureMessage fsm WHERE fsm.flowStructure.id = :flowStructureId")
    List<FlowStructureMessage> findByFlowStructureId(@Param("flowStructureId") String flowStructureId);
    
    @Query("SELECT fsm FROM FlowStructureMessage fsm WHERE fsm.flowStructure = :flowStructure " +
           "AND fsm.messageType = :messageType")
    Optional<FlowStructureMessage> findByFlowStructureAndMessageType(@Param("flowStructure") FlowStructure flowStructure,
                                                                    @Param("messageType") FlowStructureMessage.MessageType messageType);
    
    void deleteByFlowStructureId(String flowStructureId);
    
    @Query("SELECT DISTINCT fsm.flowStructure FROM FlowStructureMessage fsm WHERE fsm.messageStructure.id = :messageStructureId")
    List<FlowStructure> findFlowStructuresByMessageStructureId(@Param("messageStructureId") String messageStructureId);
}