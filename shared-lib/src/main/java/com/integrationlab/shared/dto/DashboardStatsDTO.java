package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private int activeIntegrations;
    private long messagesToday;
    private double successRate;
    private long avgResponseTime;
}