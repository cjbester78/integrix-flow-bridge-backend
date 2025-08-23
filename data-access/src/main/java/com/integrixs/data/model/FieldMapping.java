package com.integrixs.data.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a field mapping within a transformation.
 * 
 * <p>Field mappings define how source fields are transformed to target fields.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "field_mappings", indexes = {
    @Index(name = "idx_mapping_transformation", columnList = "transformation_id"),
    @Index(name = "idx_mapping_target_field", columnList = "target_field"),
    @Index(name = "idx_mapping_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"transformation", "javaFunction", "mappingRule"})
public class FieldMapping {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * The transformation this mapping belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformation_id", nullable = false)
    @NotNull(message = "Transformation is required")
    private FlowTransformation transformation;

    /**
     * Source fields in JSON format
     */
    @Column(name = "source_fields", columnDefinition = "json", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @NotBlank(message = "Source fields are required")
    @Size(max = 5000, message = "Source fields cannot exceed 5000 characters")
    private String sourceFields;

    /**
     * Target field name
     */
    @Column(name = "target_field", length = 500, nullable = false)
    @NotBlank(message = "Target field is required")
    @Size(max = 500, message = "Target field cannot exceed 500 characters")
    private String targetField;

    /**
     * JavaScript/Java function for transformation
     */
    @Column(name = "java_function", columnDefinition = "TEXT")
    @Size(max = 10000, message = "Function cannot exceed 10000 characters")
    private String javaFunction;

    /**
     * Mapping rule definition
     */
    @Column(name = "mapping_rule", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Mapping rule cannot exceed 5000 characters")
    private String mappingRule;

    /**
     * Source XPath for XML mappings
     */
    @Column(name = "source_xpath", length = 1000)
    @Size(max = 1000, message = "Source XPath cannot exceed 1000 characters")
    private String sourceXPath;

    /**
     * Target XPath for XML mappings
     */
    @Column(name = "target_xpath", length = 1000)
    @Size(max = 1000, message = "Target XPath cannot exceed 1000 characters")
    private String targetXPath;

    /**
     * Whether this is an array mapping
     */
    @Column(name = "is_array_mapping", nullable = false)
    @NotNull(message = "Array mapping flag is required")
    @Builder.Default
    private boolean isArrayMapping = false;

    /**
     * Array context path for nested arrays
     */
    @Column(name = "array_context_path", length = 500)
    @Size(max = 500, message = "Array context path cannot exceed 500 characters")
    private String arrayContextPath;

    /**
     * Whether XML namespaces should be considered
     */
    @Column(name = "namespace_aware", nullable = false)
    @NotNull(message = "Namespace aware flag is required")
    @Builder.Default
    private boolean namespaceAware = false;

    /**
     * Input data types in JSON format
     */
    @Column(name = "input_types", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Size(max = 1000, message = "Input types cannot exceed 1000 characters")
    private String inputTypes;

    /**
     * Output data type
     */
    @Column(name = "output_type", length = 100)
    @Size(max = 100, message = "Output type cannot exceed 100 characters")
    private String outputType;

    /**
     * Detailed description of the mapping
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Mapping version
     */
    @Column(length = 50)
    @Size(max = 50, message = "Version cannot exceed 50 characters")
    private String version;

    /**
     * Function name for reference
     */
    @Column(name = "function_name", length = 255)
    @Size(max = 255, message = "Function name cannot exceed 255 characters")
    private String functionName;

    /**
     * Whether this mapping is active
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active status is required")
    @Builder.Default
    private boolean isActive = true;

    /**
     * Visual flow data (nodes and edges) in JSON format
     */
    @Column(name = "visual_flow_data", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String visualFlowData;

    /**
     * Function node configuration in JSON format
     */
    @Column(name = "function_node", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String functionNode;

    /**
     * Order of this mapping within the transformation
     */
    @Column(name = "mapping_order")
    @Builder.Default
    private Integer mappingOrder = 0;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * User who created this field mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * User who last updated this field mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Transient field for parsed source fields list
     */
    @Transient
    private List<String> parsedSourceFields;

    /**
     * Gets the source fields as a list
     * 
     * @return list of source field names
     */
    public List<String> getSourceFieldsList() {
        if (parsedSourceFields == null && sourceFields != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                parsedSourceFields = mapper.readValue(sourceFields, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                parsedSourceFields = Collections.emptyList();
            }
        }
        return parsedSourceFields != null ? parsedSourceFields : Collections.emptyList();
    }

    /**
     * Sets the source fields from a list
     * 
     * @param fields list of field names
     */
    public void setSourceFieldsList(List<String> fields) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.sourceFields = mapper.writeValueAsString(fields);
            this.parsedSourceFields = fields;
        } catch (Exception e) {
            this.sourceFields = "[]";
        }
    }

    /**
     * Convenience method to parse inputTypes JSON into List<String>
     * 
     * @return list of input types
     */
    @Transient
    public List<String> getParsedInputTypes() {
        if (inputTypes == null || inputTypes.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(inputTypes, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Convenience setter to set inputTypes from List<String>
     * 
     * @param types list of input types
     */
    @Transient
    public void setParsedInputTypes(List<String> types) {
        try {
            this.inputTypes = new ObjectMapper().writeValueAsString(types);
        } catch (Exception e) {
            this.inputTypes = "[]";
        }
    }
}
