package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JarFileDTO {
    private String id;
    private String name;
    private String version;
    private String description;
    private Long size;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}