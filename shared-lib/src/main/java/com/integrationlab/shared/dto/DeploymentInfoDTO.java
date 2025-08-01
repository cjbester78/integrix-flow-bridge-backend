package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for deployment information of an integration flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfoDTO {
    private String flowId;
    private String endpoint;
    private LocalDateTime deployedAt;
    private String deployedBy;
    private Map<String, Object> metadata;
    
    // Convenience methods for common metadata
    public String getWsdlUrl() {
        return metadata != null ? (String) metadata.get("wsdlUrl") : null;
    }
    
    public String getApiDocsUrl() {
        return metadata != null ? (String) metadata.get("apiDocsUrl") : null;
    }
    
    public String getAdapterType() {
        return metadata != null ? (String) metadata.get("adapterType") : null;
    }
}