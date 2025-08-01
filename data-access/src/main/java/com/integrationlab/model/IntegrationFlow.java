package com.integrationlab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "integration_flows")
/**
 * Entity representing IntegrationFlow.
 * This maps to the corresponding table in the database.
 */
public class IntegrationFlow {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
	private String id;

	@Column(nullable = false)
    /** Name of the component */
	private String name;

	@Column(columnDefinition = "TEXT")
    /** Detailed description of the component */
	private String description;

	@Column(name = "source_adapter_id", columnDefinition = "char(36)", nullable = false)
	private String sourceAdapterId;

	@Column(name = "target_adapter_id", columnDefinition = "char(36)", nullable = false)
	private String targetAdapterId;

	@Column(name = "source_structure_id", columnDefinition = "char(36)")
	private String sourceStructureId;

	@Column(name = "target_structure_id", columnDefinition = "char(36)")
	private String targetStructureId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FlowStatus status = FlowStatus.DRAFT;

	@Column(columnDefinition = "json")
	private String configuration;

	@Column(name = "is_active")
	private boolean isActive = true;

	@Enumerated(EnumType.STRING)
	@Column(name = "mapping_mode", length = 50, nullable = false)
	private MappingMode mappingMode = MappingMode.WITH_MAPPING;

	// Deployment information
	@Column(name = "deployed_at")
	private LocalDateTime deployedAt;

	@Column(name = "deployed_by", columnDefinition = "char(36)")
	private String deployedBy;

	@Column(name = "deployment_endpoint", length = 500)
	private String deploymentEndpoint;

	@Column(name = "deployment_metadata", columnDefinition = "json")
	private String deploymentMetadata;

	@Column(name = "created_at")
    /** Timestamp of entity creation */
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
    /** Timestamp of last entity update */
	private LocalDateTime updatedAt;

	@Column(name = "created_by", columnDefinition = "char(36)")
	private String createdBy;

	@Column(name = "last_execution_at")
	private LocalDateTime lastExecutionAt;

	@Column(name = "execution_count")
	private int executionCount = 0;

	@Column(name = "success_count")
	private int successCount = 0;

	@Column(name = "error_count")
	private int errorCount = 0;

	@OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FlowTransformation> transformations;

    /**
     * Automatically sets creation and update timestamps before persisting.
     */
	@PrePersist
	protected void onCreate() {
    /** Timestamp of entity creation */
		createdAt = updatedAt = LocalDateTime.now();
	}

    /**
     * Automatically updates the timestamp before any update operation.
     */
	@PreUpdate
	protected void onUpdate() {
    /** Timestamp of last entity update */
		updatedAt = LocalDateTime.now();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourceAdapterId() {
		return sourceAdapterId;
	}

	public void setSourceAdapterId(String sourceAdapterId) {
		this.sourceAdapterId = sourceAdapterId;
	}

	public String getTargetAdapterId() {
		return targetAdapterId;
	}

	public void setTargetAdapterId(String targetAdapterId) {
		this.targetAdapterId = targetAdapterId;
	}

	public String getSourceStructureId() {
		return sourceStructureId;
	}

	public void setSourceStructureId(String sourceStructureId) {
		this.sourceStructureId = sourceStructureId;
	}

	public String getTargetStructureId() {
		return targetStructureId;
	}

	public void setTargetStructureId(String targetStructureId) {
		this.targetStructureId = targetStructureId;
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public MappingMode getMappingMode() {
		return mappingMode;
	}

	public void setMappingMode(MappingMode mappingMode) {
		this.mappingMode = mappingMode;
	}

	public LocalDateTime getDeployedAt() {
		return deployedAt;
	}

	public void setDeployedAt(LocalDateTime deployedAt) {
		this.deployedAt = deployedAt;
	}

	public String getDeployedBy() {
		return deployedBy;
	}

	public void setDeployedBy(String deployedBy) {
		this.deployedBy = deployedBy;
	}

	public String getDeploymentEndpoint() {
		return deploymentEndpoint;
	}

	public void setDeploymentEndpoint(String deploymentEndpoint) {
		this.deploymentEndpoint = deploymentEndpoint;
	}

	public String getDeploymentMetadata() {
		return deploymentMetadata;
	}

	public void setDeploymentMetadata(String deploymentMetadata) {
		this.deploymentMetadata = deploymentMetadata;
	}

	public LocalDateTime getCreatedAt() {
    /** Timestamp of entity creation */
		return createdAt;
	}

    /** Timestamp of entity creation */
	public void setCreatedAt(LocalDateTime createdAt) {
    /** Timestamp of entity creation */
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
    /** Timestamp of last entity update */
		return updatedAt;
	}

    /** Timestamp of last entity update */
	public void setUpdatedAt(LocalDateTime updatedAt) {
    /** Timestamp of last entity update */
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getLastExecutionAt() {
		return lastExecutionAt;
	}

	public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
		this.lastExecutionAt = lastExecutionAt;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	public void setExecutionCount(int executionCount) {
		this.executionCount = executionCount;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public List<FlowTransformation> getTransformations() {
		return transformations;
	}

	public void setTransformations(List<FlowTransformation> transformations) {
		this.transformations = transformations;
	}

}
