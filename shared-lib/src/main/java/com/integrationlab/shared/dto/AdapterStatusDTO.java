package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterStatusDTO {
    private String id;
    private String name;
    private String type;
    private String mode;
    private String status; // running, stopped, error
    private Integer load; // 0-100 percentage
    private String businessComponentId;
    private String businessComponentName;
    private Long messagesProcessed;
    private Long errorsCount;
    private LocalDateTime lastActivity;
    private String lastError;
}