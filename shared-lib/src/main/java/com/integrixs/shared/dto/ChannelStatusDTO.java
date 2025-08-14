package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelStatusDTO {
    private String name;
    private String status; // running, idle, stopped
    private int load; // 0-100 percentage
    private String businessComponentId;
}