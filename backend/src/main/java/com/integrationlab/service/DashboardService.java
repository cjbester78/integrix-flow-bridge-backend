package com.integrationlab.service;

import com.integrationlab.shared.dto.DashboardStatsDTO;
import com.integrationlab.repository.IntegrationFlowRepository;
import com.integrationlab.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardService {

    @Autowired
    private IntegrationFlowRepository flowRepository;

    @Autowired
    private SystemLogRepository logRepository;

    public DashboardStatsDTO getDashboardStats(String businessComponentId) {
        // Calculate active integrations
        int activeIntegrations = businessComponentId != null
            ? flowRepository.countByBusinessComponentIdAndActive(businessComponentId, true)
            : flowRepository.countByActive(true);

        // Calculate messages today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long messagesToday = businessComponentId != null
            ? logRepository.countByComponentIdAndCreatedAtAfter(businessComponentId, startOfDay)
            : logRepository.countByCreatedAtAfter(startOfDay);

        // Calculate success rate
        long successfulMessages = businessComponentId != null
            ? logRepository.countByComponentIdAndLevelAndCreatedAtAfter(businessComponentId, "INFO", startOfDay)
            : logRepository.countByLevelAndCreatedAtAfter("INFO", startOfDay);
        
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