package com.integrixs.backend.domain.services;

import com.integrixs.backend.domain.valueobjects.FlowName;
import com.integrixs.backend.events.DomainEventPublisher;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.User;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.shared.events.flow.FlowCreatedEvent;
import com.integrixs.shared.events.flow.FlowStatusChangedEvent;
import com.integrixs.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for integration flow business logic.
 * 
 * <p>Encapsulates complex business rules and orchestrates domain operations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDomainService {
    
    private final IntegrationFlowRepository flowRepository;
    private final DomainEventPublisher eventPublisher;
    
    /**
     * Creates a new integration flow.
     * 
     * @param flow the flow to create
     * @param createdBy the user creating the flow
     * @return created flow
     */
    @Transactional
    public IntegrationFlow createFlow(IntegrationFlow flow, User createdBy) {
        // Validate flow name using value object
        FlowName flowName = FlowName.of(flow.getName());
        flow.setName(flowName.getValue());
        
        // Apply business rules
        validateFlowCreation(flow);
        
        // Set initial state
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(false);
        flow.setCreatedBy(createdBy);
        flow.setCreatedAt(LocalDateTime.now());
        flow.setUpdatedAt(LocalDateTime.now());
        
        // Save flow
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Publish domain event
        eventPublisher.publish(new FlowCreatedEvent(
            savedFlow.getId().toString(),
            savedFlow.getName(),
            savedFlow.getSourceAdapterId() != null ? savedFlow.getSourceAdapterId().toString() : null,
            savedFlow.getTargetAdapterId() != null ? savedFlow.getTargetAdapterId().toString() : null,
            createdBy.getUsername()
        ));
        
        log.info("Created integration flow: {} with ID: {}", savedFlow.getName(), savedFlow.getId());
        
        return savedFlow;
    }
    
    /**
     * Activates an integration flow.
     * 
     * @param flowId the flow ID
     * @param activatedBy the user activating the flow
     * @return activated flow
     */
    @Transactional
    public IntegrationFlow activateFlow(String flowId, String activatedBy) {
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new BusinessException("Flow not found: " + flowId));
        
        if (flow.isActive()) {
            throw new BusinessException("Flow is already active");
        }
        
        // Validate activation rules
        validateFlowActivation(flow);
        
        // Store old status
        FlowStatus oldStatus = flow.getStatus();
        
        // Update flow state
        flow.setStatus(FlowStatus.ACTIVE);
        flow.setActive(true);
        flow.setDeployedAt(LocalDateTime.now());
        flow.setDeployedBy(activatedBy);
        flow.setUpdatedAt(LocalDateTime.now());
        
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Publish domain event
        eventPublisher.publish(new FlowStatusChangedEvent(
            flowId,
            oldStatus.toString(),
            FlowStatus.ACTIVE.toString(),
            "Flow activated by user",
            activatedBy
        ));
        
        log.info("Activated flow: {} by user: {}", flowId, activatedBy);
        
        return savedFlow;
    }
    
    /**
     * Deactivates an integration flow.
     * 
     * @param flowId the flow ID
     * @param deactivatedBy the user deactivating the flow
     * @param reason the reason for deactivation
     * @return deactivated flow
     */
    @Transactional
    public IntegrationFlow deactivateFlow(String flowId, String deactivatedBy, String reason) {
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new BusinessException("Flow not found: " + flowId));
        
        if (!flow.isActive()) {
            throw new BusinessException("Flow is already inactive");
        }
        
        // Store old status
        FlowStatus oldStatus = flow.getStatus();
        
        // Update flow state
        flow.setStatus(FlowStatus.INACTIVE);
        flow.setActive(false);
        flow.setUpdatedAt(LocalDateTime.now());
        
        IntegrationFlow savedFlow = flowRepository.save(flow);
        
        // Publish domain event
        eventPublisher.publish(new FlowStatusChangedEvent(
            flowId,
            oldStatus.toString(),
            FlowStatus.INACTIVE.toString(),
            reason,
            deactivatedBy
        ));
        
        log.info("Deactivated flow: {} by user: {} - Reason: {}", flowId, deactivatedBy, reason);
        
        return savedFlow;
    }
    
    /**
     * Validates flow creation business rules.
     * 
     * @param flow the flow to validate
     * @throws BusinessException if validation fails
     */
    private void validateFlowCreation(IntegrationFlow flow) {
        // Check for duplicate names
        if (flowRepository.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(flow.getName()))) {
            throw new BusinessException("Flow with name already exists: " + flow.getName());
        }
        
        // Validate source and target adapters are different
        if (flow.getSourceAdapterId().equals(flow.getTargetAdapterId())) {
            throw new BusinessException("Source and target adapters must be different");
        }
        
        // Additional business rules can be added here
    }
    
    /**
     * Validates flow activation business rules.
     * 
     * @param flow the flow to validate
     * @throws BusinessException if validation fails
     */
    private void validateFlowActivation(IntegrationFlow flow) {
        // Flow must be in a valid state for activation
        if (flow.getStatus() == FlowStatus.ERROR) {
            throw new BusinessException("Cannot activate flow in ERROR state");
        }
        
        // Flow must have at least one transformation or be pass-through mapping
        if (flow.getTransformations().isEmpty() && 
            flow.getMappingMode() != com.integrixs.data.model.MappingMode.PASS_THROUGH) {
            throw new BusinessException("Flow must have transformations or use pass-through mapping");
        }
        
        // Additional activation rules can be added here
    }
}