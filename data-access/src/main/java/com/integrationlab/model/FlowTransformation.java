package com.integrationlab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flow_transformations")
/**
 * Entity representing FlowTransformation.
 * This maps to the corresponding table in the database.
 */
public class FlowTransformation {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(columnDefinition = "char(36)")
    /** Unique identifier (UUID) for the entity */
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_id", nullable = false)
	private IntegrationFlow flow;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TransformationType type;

	@Column(name = "configuration", columnDefinition = "json", nullable = false)
	private String configuration;

	@Column(name = "execution_order")
	private int executionOrder;

	@Column(name = "is_active")
	private boolean isActive = true;

	@Column(name = "created_at")
    /** Timestamp of entity creation */
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
    /** Timestamp of last entity update */
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "transformation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FieldMapping> fieldMappings;

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

	public enum TransformationType {
		FIELD_MAPPING, CUSTOM_FUNCTION, FILTER, ENRICHMENT, VALIDATION
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IntegrationFlow getFlow() {
		return flow;
	}

	public void setFlow(IntegrationFlow flow) {
		this.flow = flow;
	}

	public TransformationType getType() {
		return type;
	}

	public void setType(TransformationType type) {
		this.type = type;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
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

	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public void setFieldMappings(List<FieldMapping> fieldMappings) {
		this.fieldMappings = fieldMappings;
	}

}
