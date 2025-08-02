package com.integrationlab.shared.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for requesting flow export with options.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowExportRequestDTO {
    
    /**
     * ID of the flow to export
     */
    @NotNull(message = "Flow ID is required")
    private String flowId;
    
    /**
     * Export options
     */
    @Builder.Default
    private ExportOptions options = new ExportOptions();
    
    /**
     * Export configuration options
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportOptions {
        
        /**
         * Whether to include the business component
         */
        @Builder.Default
        private boolean includeBusinessComponent = true;
        
        /**
         * Whether to include adapter configurations
         */
        @Builder.Default
        private boolean includeAdapterConfigs = true;
        
        /**
         * Whether to include certificate references
         */
        @Builder.Default
        private boolean includeCertificateReferences = true;
        
        /**
         * Whether to include sensitive data (encrypted)
         */
        @Builder.Default
        private boolean includeSensitiveData = false;
        
        /**
         * Whether to include execution statistics
         */
        @Builder.Default
        private boolean includeStatistics = false;
        
        /**
         * Whether to include audit information
         */
        @Builder.Default
        private boolean includeAuditInfo = false;
        
        /**
         * Format for the export
         */
        @Builder.Default
        private ExportFormat format = ExportFormat.JSON;
        
        /**
         * Whether to compress the export
         */
        @Builder.Default
        private boolean compress = false;
        
        /**
         * Environment tag for the export
         */
        private String environment;
        
        /**
         * Description for this export
         */
        private String description;
        
        /**
         * Custom tags to add to the export
         */
        private Set<String> tags;
    }
    
    /**
     * Export format options
     */
    public enum ExportFormat {
        /**
         * JSON format (default)
         */
        JSON,
        
        /**
         * XML format
         */
        XML,
        
        /**
         * YAML format
         */
        YAML
    }
}