package com.integrixs.shared.dto.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for field mapping configuration.
 * 
 * <p>Defines how fields from source data are transformed and mapped
 * to target fields using JavaScript functions or mapping rules.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldMappingDTO {
    
    /**
     * Unique identifier for the field mapping
     */
    private String id;
    
    /**
     * ID of the parent transformation
     */
    @NotBlank(message = "Transformation ID is required")
    private String transformationId;
    
    /**
     * Source fields (JSON array of field paths)
     * Example: ["customer.firstName", "customer.lastName"]
     */
    @NotBlank(message = "Source fields are required")
    private String sourceFields;
    
    /**
     * Target field path
     * Example: "contact.fullName"
     */
    @NotBlank(message = "Target field is required")
    private String targetField;
    
    /**
     * JavaScript function for transformation
     * Mutually exclusive with mappingRule
     */
    @Size(max = 10000, message = "Function cannot exceed 10000 characters")
    private String javaFunction;
    
    /**
     * Simple mapping rule (for direct mappings)
     * Mutually exclusive with javaFunction
     */
    @Size(max = 1000, message = "Mapping rule cannot exceed 1000 characters")
    private String mappingRule;
    
    /**
     * Input data types (JSON array)
     * Example: ["string", "string"]
     */
    private String inputTypes;
    
    /**
     * Output data type
     * Example: "string"
     */
    @Pattern(regexp = "^(string|number|boolean|object|array|date)$",
             message = "Output type must be: string, number, boolean, object, array, or date")
    private String outputType;
    
    /**
     * Description of the mapping logic
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Version of the mapping (for change tracking)
     */
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in format X.Y.Z")
    private String version;
    
    /**
     * Name of the function (for reusable functions)
     */
    @Size(max = 100, message = "Function name cannot exceed 100 characters")
    private String functionName;
    
    /**
     * Whether this mapping is active
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private boolean isActive = true;
    
    /**
     * Whether this is an array/node mapping
     */
    @Builder.Default
    private boolean isArrayMapping = false;
    
    /**
     * Array context path for nested arrays
     */
    @Size(max = 500, message = "Array context path cannot exceed 500 characters")
    private String arrayContextPath;
    
    /**
     * Source XPath for XML mappings
     */
    @Size(max = 1000, message = "Source XPath cannot exceed 1000 characters")
    private String sourceXPath;
    
    /**
     * Target XPath for XML mappings
     */
    @Size(max = 1000, message = "Target XPath cannot exceed 1000 characters")
    private String targetXPath;
    
    /**
     * Visual flow data (nodes and edges) in JSON format
     */
    private String visualFlowData;
    
    /**
     * Function node configuration in JSON format
     */
    private String functionNode;
    
    /**
     * Order of this mapping within the transformation
     */
    @Builder.Default
    private Integer mappingOrder = 0;
    
    /**
     * Timestamp when the mapping was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the mapping was last updated
     */
    private LocalDateTime updatedAt;
    
    @Override
    public String toString() {
        return "FieldMappingDTO{" +
                "id='" + id + '\'' +
                ", transformationId='" + transformationId + '\'' +
                ", sourceFields='" + sourceFields + '\'' +
                ", targetField='" + targetField + '\'' +
                ", javaFunction='" + (javaFunction != null ? "[function]" : "null") + '\'' +
                ", mappingRule='" + (mappingRule != null ? "[rule]" : "null") + '\'' +
                ", inputTypes='" + inputTypes + '\'' +
                ", outputType='" + outputType + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", functionName='" + functionName + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}