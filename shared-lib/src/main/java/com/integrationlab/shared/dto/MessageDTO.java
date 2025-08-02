package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;
    private LocalDateTime timestamp;
    private String source;
    private String target;
    private String type;
    private String status; // success, failed, processing
    private String processingTime; // in milliseconds, e.g. "250ms"
    private String size; // in bytes, e.g. "1024 bytes"
    private String businessComponentId;
    private List<MessageLogDTO> logs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageLogDTO {
        private LocalDateTime timestamp;
        private String level;
        private String message;
    }
}