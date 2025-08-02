package com.integrationlab.shared.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for importing an integration flow with configuration options.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowImportRequestDTO {
    
    /**
     * The exported flow data
     */
    @NotNull(message = "Flow export data is required")
    private FlowExportDTO flowExport;
    
    /**
     * Import options
     */
    @NotNull(message = "Import options are required")
    private ImportOptions options;
    
    /**
     * Mapping of old IDs to new IDs for conflict resolution
     */
    private Map<String, String> idMappings;
    
    /**
     * Import configuration options
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportOptions {
        
        /**
         * Strategy for handling conflicts
         */
        @Builder.Default
        private ConflictStrategy conflictStrategy = ConflictStrategy.FAIL;
        
        /**
         * Whether to import the business component
         */
        @Builder.Default
        private boolean importBusinessComponent = true;
        
        /**
         * Whether to import adapters
         */
        @Builder.Default
        private boolean importAdapters = true;
        
        /**
         * Whether to import certificates (references only)
         */
        @Builder.Default
        private boolean importCertificateReferences = true;
        
        /**
         * Whether to validate all references exist
         */
        @Builder.Default
        private boolean validateReferences = true;
        
        /**
         * Whether to activate the flow after import
         */
        @Builder.Default
        private boolean activateAfterImport = false;
        
        /**
         * Prefix to add to imported object names
         */
        private String namePrefix;
        
        /**
         * Suffix to add to imported object names
         */
        private String nameSuffix;
        
        /**
         * Target business component ID (if different from export)
         */
        private String targetBusinessComponentId;
        
        /**
         * Environment-specific configuration overrides
         */
        private Map<String, Object> configOverrides;
    }
    
    /**
     * Strategy for handling conflicts during import
     */
    public enum ConflictStrategy {
        /**
         * Fail the import if any conflicts are found
         */
        FAIL,
        
        /**
         * Skip conflicting objects and continue
         */
        SKIP,
        
        /**
         * Create new objects with modified names
         */
        CREATE_NEW,
        
        /**
         * Update existing objects (requires permissions)
         */
        UPDATE_EXISTING,
        
        /**
         * Prompt user for each conflict (interactive mode)
         */
        PROMPT
    }
}