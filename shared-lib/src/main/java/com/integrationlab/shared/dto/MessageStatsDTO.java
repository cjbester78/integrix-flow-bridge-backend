package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatsDTO {
    private long total;
    private long successful;
    private long processing;
    private long failed;
    private double successRate;
    private double avgProcessingTime; // in milliseconds
}