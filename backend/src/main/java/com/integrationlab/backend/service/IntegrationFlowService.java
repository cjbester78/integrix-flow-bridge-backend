package com.integrationlab.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.repository.IntegrationFlowRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationFlowService {

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;

    public List<IntegrationFlow> getAllFlows() {
        return integrationFlowRepository.findAll();
    }

    public Optional<IntegrationFlow> getFlowById(String id) {
        return integrationFlowRepository.findById(id);
    }

    public IntegrationFlow createFlow(IntegrationFlow flow) {
        return integrationFlowRepository.save(flow);
    }

    public IntegrationFlow updateFlow(String id, IntegrationFlow updatedFlow) {
        return integrationFlowRepository.findById(id)
            .map(existing -> {
                existing.setName(updatedFlow.getName());
                existing.setDescription(updatedFlow.getDescription());
                existing.setSourceAdapterId(updatedFlow.getSourceAdapterId());
                existing.setTargetAdapterId(updatedFlow.getTargetAdapterId());
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

    public void deleteFlow(String id) {
        integrationFlowRepository.deleteById(id);
    }
}
