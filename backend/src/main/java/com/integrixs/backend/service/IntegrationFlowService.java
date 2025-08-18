package com.integrixs.backend.service;

import com.integrixs.backend.annotation.AuditCreate;
import com.integrixs.backend.annotation.AuditDelete;
import com.integrixs.backend.annotation.AuditUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.shared.dto.IntegrationFlowDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class IntegrationFlowService {

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;
    
    @Autowired
    private com.integrixs.data.repository.CommunicationAdapterRepository adapterRepository;

    public List<IntegrationFlow> getAllFlows() {
        return integrationFlowRepository.findAll();
    }

    public Optional<IntegrationFlow> getFlowById(String id) {
        return integrationFlowRepository.findById(UUID.fromString(id));
    }

    @AuditCreate
    @Transactional
    public IntegrationFlow createFlow(IntegrationFlow flow) {
        // Check if flow name already exists
        if (integrationFlowRepository.existsByName(flow.getName())) {
            throw new IllegalArgumentException("A flow with the name '" + flow.getName() + "' already exists");
        }
        return integrationFlowRepository.save(flow);
    }

    @AuditUpdate
    @Transactional
    public IntegrationFlow updateFlow(String id, IntegrationFlow updatedFlow) {
        return integrationFlowRepository.findById(UUID.fromString(id))
            .map(existing -> {
                // Check if new name conflicts with another flow (excluding current flow)
                if (!existing.getName().equals(updatedFlow.getName()) && 
                    integrationFlowRepository.existsByNameAndIdNot(updatedFlow.getName(), UUID.fromString(id))) {
                    throw new IllegalArgumentException("A flow with the name '" + updatedFlow.getName() + "' already exists");
                }
                existing.setName(updatedFlow.getName());
                existing.setDescription(updatedFlow.getDescription());
                existing.setSourceAdapterId(updatedFlow.getSourceAdapterId());
                existing.setTargetAdapterId(updatedFlow.getTargetAdapterId());
                existing.setSourceFlowStructureId(updatedFlow.getSourceFlowStructureId());
                existing.setTargetFlowStructureId(updatedFlow.getTargetFlowStructureId());
                // Keep legacy fields for backward compatibility
                existing.setSourceStructureId(updatedFlow.getSourceStructureId());
                existing.setTargetStructureId(updatedFlow.getTargetStructureId());
                existing.setStatus(updatedFlow.getStatus());
                existing.setConfiguration(updatedFlow.getConfiguration());
                existing.setActive(updatedFlow.isActive());
                existing.setCreatedBy(updatedFlow.getCreatedBy());
                existing.setExecutionCount(updatedFlow.getExecutionCount());
                existing.setSuccessCount(updatedFlow.getSuccessCount());
                existing.setErrorCount(updatedFlow.getErrorCount());
                existing.setLastExecutionAt(updatedFlow.getLastExecutionAt());
                return integrationFlowRepository.save(existing);
            }).orElseThrow(() -> new RuntimeException("Flow not found: " + id));
    }

    @AuditDelete
    @Transactional
    public void deleteFlow(String id) {
        integrationFlowRepository.deleteById(UUID.fromString(id));
    }
    
    @Transactional(readOnly = true)
    public List<IntegrationFlowDTO> getAllFlowsAsDTO() {
        return integrationFlowRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<IntegrationFlowDTO> getFlowByIdAsDTO(String id) {
        return integrationFlowRepository.findById(UUID.fromString(id))
                .map(this::convertToDTO);
    }
    
    private IntegrationFlowDTO convertToDTO(IntegrationFlow flow) {
        IntegrationFlowDTO.IntegrationFlowDTOBuilder builder = IntegrationFlowDTO.builder()
                .id(flow.getId().toString())
                .name(flow.getName())
                .description(flow.getDescription())
                .sourceAdapterId(flow.getSourceAdapterId() != null ? flow.getSourceAdapterId().toString() : null)
                .targetAdapterId(flow.getTargetAdapterId() != null ? flow.getTargetAdapterId().toString() : null)
                .sourceFlowStructureId(flow.getSourceFlowStructureId())
                .targetFlowStructureId(flow.getTargetFlowStructureId())
                .sourceStructureId(flow.getSourceStructureId())
                .targetStructureId(flow.getTargetStructureId())
                .status(flow.getStatus() != null ? flow.getStatus().toString() : null)
                .configuration(flow.getConfiguration())
                .isActive(flow.isActive())
                .mappingMode(flow.getMappingMode() != null ? flow.getMappingMode().toString() : null)
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .createdBy(flow.getCreatedBy())
                .lastExecutionAt(flow.getLastExecutionAt())
                .executionCount(flow.getExecutionCount())
                .successCount(flow.getSuccessCount())
                .errorCount(flow.getErrorCount())
                .businessComponentId(flow.getBusinessComponent() != null ? flow.getBusinessComponent().getId().toString() : null);
                
        // Fetch adapter details
        if (flow.getSourceAdapterId() != null) {
            adapterRepository.findById(flow.getSourceAdapterId()).ifPresent(adapter -> {
                builder.sourceAdapterName(adapter.getName());
                builder.sourceAdapterType(adapter.getType() != null ? adapter.getType().toString() : null);
            });
        }
        
        if (flow.getTargetAdapterId() != null) {
            adapterRepository.findById(flow.getTargetAdapterId()).ifPresent(adapter -> {
                builder.targetAdapterName(adapter.getName());
                builder.targetAdapterType(adapter.getType() != null ? adapter.getType().toString() : null);
            });
        }
        
        return builder.build();
    }
}
