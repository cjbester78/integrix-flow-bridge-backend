package com.integrationlab.model;

import com.integrationlab.shared.enums.TransformationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a flow transformation.
 * 
 * <p>Transformations are applied to data as it flows from source to target.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "flow_transformations", indexes = {
    @Index(name = "idx_transform_flow", columnList = "flow_id"),
    @Index(name = "idx_transform_type", columnList = "type"),
    @Index(name = "idx_transform_order", columnList = "flow_id, execution_order"),
    @Index(name = "idx_transform_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"flow", "fieldMappings", "configuration"})
public class FlowTransformation {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String id;

    /**
     * The integration flow this transformation belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", nullable = false)
    @NotNull(message = "Flow is required")
    private IntegrationFlow flow;

    /**
     * Type of transformation
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Transformation type is required")
    private TransformationType type;

    /**
     * Configuration in JSON format
     */
    @Column(name = "configuration", columnDefinition = "json", nullable = false)
    @NotBlank(message = "Configuration is required")
    @Size(max = 10000, message = "Configuration cannot exceed 10000 characters")
    private String configuration;

    /**
     * Order of execution within the flow
     */
    @Column(name = "execution_order")
    @Min(value = 1, message = "Execution order must be at least 1")
    @Builder.Default
    private int executionOrder = 1;

    /**
     * Whether this transformation is active
     */
    @Column(name = "is_active")
    @NotNull(message = "Active status is required")
    @Builder.Default
    private boolean isActive = true;

    /**
     * Transformation name
     */
    @Column(length = 100)
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    /**
     * Transformation description
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

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
     * Field mappings for this transformation
     * Batch size optimization for lazy loading
     */
    @OneToMany(mappedBy = "transformation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    @Builder.Default
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /**
     * Transformation types
     */
    public enum TransformationType {
        FIELD_MAPPING,
        CUSTOM_FUNCTION,
        FILTER,
        ENRICHMENT,
        VALIDATION,
        AGGREGATION,
        ROUTING
    }

    /**
     * Adds a field mapping to this transformation
     * 
     * @param fieldMapping the field mapping to add
     */
    public void addFieldMapping(FieldMapping fieldMapping) {
        fieldMappings.add(fieldMapping);
        fieldMapping.setTransformation(this);
    }

    /**
     * Removes a field mapping from this transformation
     * 
     * @param fieldMapping the field mapping to remove
     */
    public void removeFieldMapping(FieldMapping fieldMapping) {
        fieldMappings.remove(fieldMapping);
        fieldMapping.setTransformation(null);
    }
}
