package com.integrationlab.shared.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO for flow import validation results.
 * Used to preview what will happen during import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowImportValidationDTO {
    
    /**
     * Whether the import can proceed
     */
    private boolean canImport;
    
    /**
     * Whether all validations passed
     */
    private boolean isValid;
    
    /**
     * Summary of what will be imported
     */
    private ImportPreview preview;
    
    /**
     * List of validation errors
     */
    @Builder.Default
    private List<ValidationIssue> errors = new ArrayList<>();
    
    /**
     * List of validation warnings
     */
    @Builder.Default
    private List<ValidationIssue> warnings = new ArrayList<>();
    
    /**
     * List of detected conflicts
     */
    @Builder.Default
    private List<Conflict> conflicts = new ArrayList<>();
    
    /**
     * Required permissions for import
     */
    @Builder.Default
    private List<String> requiredPermissions = new ArrayList<>();
    
    /**
     * Version compatibility information
     */
    private VersionCompatibility versionCompatibility;
    
    /**
     * Preview of what will be imported
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportPreview {
        private String flowName;
        private String flowDescription;
        private String businessComponentName;
        private String sourceAdapterName;
        private String targetAdapterName;
        private int transformationCount;
        private int fieldMappingCount;
        private int certificateReferenceCount;
        private Map<String, Integer> objectCounts;
    }
    
    /**
     * Validation issue (error or warning)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssue {
        private String code;
        private String message;
        private String field;
        private String objectType;
        private String objectId;
        private Severity severity;
        private Map<String, Object> context;
        
        public enum Severity {
            ERROR,
            WARNING,
            INFO
        }
    }
    
    /**
     * Conflict information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conflict {
        private String objectType;
        private String importId;
        private String importName;
        private String existingId;
        private String existingName;
        private ConflictType type;
        private List<String> resolutionOptions;
        
        public enum ConflictType {
            NAME_EXISTS,
            ID_EXISTS,
            UNIQUE_CONSTRAINT,
            REFERENCE_MISSING,
            VERSION_MISMATCH
        }
    }
    
    /**
     * Version compatibility information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionCompatibility {
        private String exportVersion;
        private String currentVersion;
        private boolean isCompatible;
        private boolean requiresMigration;
        private List<String> migrationSteps;
        private List<String> breakingChanges;
    }
}