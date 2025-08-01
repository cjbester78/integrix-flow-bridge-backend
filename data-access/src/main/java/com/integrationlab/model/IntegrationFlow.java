package com.integrationlab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an integration flow.
 * 
 * <p>An integration flow defines how data moves from a source adapter
 * to a target adapter, including all transformations and mappings.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "integration_flows", indexes = {
    @Index(name = "idx_flow_name", columnList = "name"),
    @Index(name = "idx_flow_status", columnList = "status"),
    @Index(name = "idx_flow_active", columnList = "is_active"),
    @Index(name = "idx_flow_source", columnList = "source_adapter_id"),
    @Index(name = "idx_flow_target", columnList = "target_adapter_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"transformations", "configuration", "deploymentMetadata"})
public class IntegrationFlow {

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
     * Name of the integration flow
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Flow name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Detailed description of the flow's purpose
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Source adapter ID (sender - receives data FROM external systems)
     */
    @Column(name = "source_adapter_id", columnDefinition = "char(36)", nullable = false)
    @NotBlank(message = "Source adapter is required")
    private String sourceAdapterId;

    /**
     * Target adapter ID (receiver - sends data TO external systems)
     */
    @Column(name = "target_adapter_id", columnDefinition = "char(36)", nullable = false)
    @NotBlank(message = "Target adapter is required")
    private String targetAdapterId;

    /**
     * Source data structure ID
     */
    @Column(name = "source_structure_id", columnDefinition = "char(36)")
    private String sourceStructureId;

    /**
     * Target data structure ID
     */
    @Column(name = "target_structure_id", columnDefinition = "char(36)")
    private String targetStructureId;

    /**
     * Current flow status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private FlowStatus status = FlowStatus.DRAFT;

    /**
     * Flow configuration in JSON format
     */
    @Column(columnDefinition = "json")
    @Size(max = 10000, message = "Configuration cannot exceed 10000 characters")
    private String configuration;

    /**
     * Whether the flow is currently active
     */
    @Column(name = "is_active")
    @NotNull(message = "Active status is required")
    @Builder.Default
    private boolean isActive = true;

    /**
     * Mapping mode for the flow
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_mode", length = 50, nullable = false)
    @NotNull(message = "Mapping mode is required")
    @Builder.Default
    private MappingMode mappingMode = MappingMode.WITH_MAPPING;

    /**
     * Timestamp when flow was deployed
     */
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    /**
     * User who deployed the flow
     */
    @Column(name = "deployed_by", columnDefinition = "char(36)")
    private String deployedBy;

    /**
     * Deployment endpoint URL
     */
    @Column(name = "deployment_endpoint", length = 500)
    @Size(max = 500, message = "Deployment endpoint cannot exceed 500 characters")
    private String deploymentEndpoint;

    /**
     * Deployment metadata in JSON format
     */
    @Column(name = "deployment_metadata", columnDefinition = "json")
    private String deploymentMetadata;

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
     * User who created the flow
     */
    @Column(name = "created_by", columnDefinition = "char(36)")
    @NotBlank(message = "Created by is required")
    private String createdBy;

    /**
     * Timestamp of last execution
     */
    @Column(name = "last_execution_at")
    private LocalDateTime lastExecutionAt;

    /**
     * Total number of executions
     */
    @Column(name = "execution_count")
    @Min(value = 0, message = "Execution count cannot be negative")
    @Builder.Default
    private int executionCount = 0;

    /**
     * Number of successful executions
     */
    @Column(name = "success_count")
    @Min(value = 0, message = "Success count cannot be negative")
    @Builder.Default
    private int successCount = 0;

    /**
     * Number of failed executions
     */
    @Column(name = "error_count")
    @Min(value = 0, message = "Error count cannot be negative")
    @Builder.Default
    private int errorCount = 0;

    /**
     * Transformations associated with this flow
     */
    @OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FlowTransformation> transformations = new ArrayList<>();

    /**
     * Business component that owns this flow
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    /**
     * Lifecycle callback to ensure timestamps are set
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback to update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increments execution statistics
     * 
     * @param success whether the execution was successful
     */
    public void recordExecution(boolean success) {
        this.executionCount++;
        if (success) {
            this.successCount++;
        } else {
            this.errorCount++;
        }
        this.lastExecutionAt = LocalDateTime.now();
    }

    /**
     * Adds a transformation to this flow
     * 
     * @param transformation the transformation to add
     */
    public void addTransformation(FlowTransformation transformation) {
        if (transformations == null) {
            transformations = new ArrayList<>();
        }
        transformations.add(transformation);
        transformation.setFlow(this);
    }

    /**
     * Removes a transformation from this flow
     * 
     * @param transformation the transformation to remove
     */
    public void removeTransformation(FlowTransformation transformation) {
        if (transformations != null) {
            transformations.remove(transformation);
            transformation.setFlow(null);
        }
    }
}
