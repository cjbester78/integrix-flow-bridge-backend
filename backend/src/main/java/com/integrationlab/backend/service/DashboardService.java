package com.integrationlab.backend.service;

import com.integrationlab.shared.dto.DashboardStatsDTO;
import com.integrationlab.data.repository.IntegrationFlowRepository;
import com.integrationlab.data.repository.CommunicationAdapterRepository;
import com.integrationlab.data.repository.SystemLogRepository;
import com.integrationlab.data.model.SystemLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
                                .map(adapter -> businessComponentId.equals(adapter.getBusinessComponentId()))
                                .orElse(false);
                        boolean targetMatch = adapterRepository.findById(flow.getTargetAdapterId())
                                .map(adapter -> businessComponentId.equals(adapter.getBusinessComponentId()))
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
            ? logRepository.countByComponentIdAndTimestampAfter(businessComponentId, startOfDay)
            : logRepository.countByTimestampAfter(startOfDay);

        // Calculate success rate
        long successfulMessages = businessComponentId != null
            ? logRepository.countByComponentIdAndLevelAndTimestampAfter(businessComponentId, SystemLog.LogLevel.INFO, startOfDay)
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