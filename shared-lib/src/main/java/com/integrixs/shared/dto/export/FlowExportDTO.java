package com.integrixs.shared.dto.export;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.integrixs.shared.dto.FlowDTO;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.CommunicationAdapterDTO;
import com.integrixs.shared.dto.FlowTransformationDTO;
import com.integrixs.shared.dto.FieldMappingDTO;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for exporting a complete integration flow with all dependencies.
 * Includes all objects needed to recreate the flow in another environment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowExportDTO {
    
    /**
     * Export metadata
     */
    private ExportMetadata metadata;
    
    /**
     * The main integration flow
     */
    private FlowDTO flow;
    
    /**
     * Business component that owns this flow
     */
    private BusinessComponentDTO businessComponent;
    
    /**
     * Source adapter (sender - receives data FROM external systems)
     */
    private CommunicationAdapterDTO sourceAdapter;
    
    /**
     * Target adapter (receiver - sends data TO external systems)
     */
    private CommunicationAdapterDTO targetAdapter;
    
    /**
     * All transformations associated with this flow
     */
    private List<FlowTransformationDTO> transformations;
    
    /**
     * All field mappings (nested within transformations but flattened for easy access)
     */
    private List<FieldMappingDTO> fieldMappings;
    
    /**
     * Certificates referenced by adapters (without content for security)
     */
    private List<CertificateReferenceDTO> certificateReferences;
    
    /**
     * Data structure definitions if any
     */
    private Map<String, Object> dataStructures;
    
    /**
     * Additional configuration that might be needed
     */
    private Map<String, Object> additionalConfig;
    
    /**
     * Export metadata information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportMetadata {
        private String exportId;
        private String exportVersion;
        private String applicationVersion;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime exportDate;
        
        private String exportedBy;
        private String exportedByUsername;
        private String environment;
        private String description;
        private Map<String, String> tags;
    }
    
    /**
     * Certificate reference without sensitive content
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateReferenceDTO {
        private String id;
        private String name;
        private String type;
        private String format;
        private String fileName;
        private boolean passwordProtected;
        private String checksum; // SHA-256 hash for verification
    }
}