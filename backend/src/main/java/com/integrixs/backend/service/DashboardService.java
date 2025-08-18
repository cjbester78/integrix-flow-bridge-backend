package com.integrixs.backend.service;

import com.integrixs.shared.dto.DashboardStatsDTO;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.model.SystemLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DashboardService {

    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(String businessComponentId) {
        // Calculate active integrations
        int activeIntegrations;
        if (businessComponentId != null) {
            // For business component filtering, we need to count flows that have adapters
            // belonging to the specified business component
            activeIntegrations = (int) flowRepository.findByIsActive(true).stream()
                    .filter(flow -> {
                        boolean sourceMatch = adapterRepository.findById(flow.getSourceAdapterId())
                                .map(adapter -> businessComponentId.equals(adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId().toString() : null))
                                .orElse(false);
                        boolean targetMatch = adapterRepository.findById(flow.getTargetAdapterId())
                                .map(adapter -> businessComponentId.equals(adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId().toString() : null))
                                .orElse(false);
                        return sourceMatch || targetMatch;
                    })
                    .count();
        } else {
            activeIntegrations = flowRepository.countByIsActive(true);
        }

        // Calculate messages today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long messagesToday = businessComponentId != null
            ? logRepository.countByComponentIdAndTimestampAfter(UUID.fromString(businessComponentId), startOfDay)
            : logRepository.countByTimestampAfter(startOfDay);

        // Calculate success rate
        long successfulMessages = businessComponentId != null
            ? logRepository.countByComponentIdAndLevelAndTimestampAfter(UUID.fromString(businessComponentId), SystemLog.LogLevel.INFO, startOfDay)
            : logRepository.countByLevelAndTimestampAfter(SystemLog.LogLevel.INFO, startOfDay);
        
        double successRate = messagesToday > 0 ? (successfulMessages * 100.0 / messagesToday) : 100.0;

        // Calculate average response time (mock for now)
        long avgResponseTime = 250; // milliseconds

        return DashboardStatsDTO.builder()
                .activeIntegrations(activeIntegrations)
                .messagesToday(messagesToday)
                .successRate(successRate)
                .avgResponseTime(avgResponseTime)
                .build();
    }
}