package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentMessageDTO {
    private String id;
    private String source;
    private String target;
    private String status; // success, failed, processing
    private String time;
    private String businessComponentId;
}