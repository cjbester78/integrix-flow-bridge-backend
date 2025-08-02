package com.integrationlab.shared.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO representing the result of a flow import operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowImportResultDTO {
    
    /**
     * Whether the import was successful
     */
    private boolean success;
    
    /**
     * Imported flow ID (if successful)
     */
    private String importedFlowId;
    
    /**
     * Imported flow name
     */
    private String importedFlowName;
    
    /**
     * Summary of what was imported
     */
    @Builder.Default
    private ImportSummary summary = new ImportSummary();
    
    /**
     * Mapping of old IDs to new IDs
     */
    @Builder.Default
    private Map<String, String> idMappings = new HashMap<>();
    
    /**
     * List of warnings encountered
     */
    @Builder.Default
    private List<ImportMessage> warnings = new ArrayList<>();
    
    /**
     * List of errors encountered
     */
    @Builder.Default
    private List<ImportMessage> errors = new ArrayList<>();
    
    /**
     * List of conflicts that were resolved
     */
    @Builder.Default
    private List<ConflictResolution> conflictResolutions = new ArrayList<>();
    
    /**
     * Summary of imported objects
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportSummary {
        private boolean flowImported;
        private boolean businessComponentImported;
        private int adaptersImported;
        private int transformationsImported;
        private int fieldMappingsImported;
        private int certificateReferencesCreated;
        private int totalObjectsImported;
        private long importDurationMs;
    }
    
    /**
     * Import message (warning or error)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportMessage {
        private String code;
        private String message;
        private String objectType;
        private String objectId;
        private String objectName;
        private Map<String, Object> details;
    }
    
    /**
     * Conflict resolution information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictResolution {
        private String objectType;
        private String originalId;
        private String originalName;
        private String newId;
        private String newName;
        private String resolution; // SKIPPED, CREATED_NEW, UPDATED, etc.
        private String reason;
    }
}