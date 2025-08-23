package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalRetrySettingsDTO {
    
    private boolean enabled;
    
    private int maxRetries;
    
    private int retryInterval;
    
    private String retryIntervalUnit; // "seconds", "minutes", "hours"
    
}