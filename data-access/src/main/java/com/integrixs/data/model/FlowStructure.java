package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "flow_structures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"businessComponent", "createdBy", "updatedBy", "flowStructureMessages"})
public class FlowStructure {
    
    @Id
    @Column(name = "id", length = 36)
    @EqualsAndHashCode.Include
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_mode", nullable = false)
    private ProcessingMode processingMode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;
    
    @Column(name = "wsdl_content", columnDefinition = "LONGTEXT")
    private String wsdlContent;
    
    @Column(name = "source_type")
    @Builder.Default
    private String sourceType = "INTERNAL";
    
    @Column(name = "namespace", columnDefinition = "JSON")
    private String namespace;
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;
    
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id", nullable = false)
    private BusinessComponent businessComponent;
    
    @OneToMany(mappedBy = "flowStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FlowStructureMessage> flowStructureMessages;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ProcessingMode {
        SYNC,
        ASYNC
    }
    
    public enum Direction {
        SOURCE,
        TARGET
    }
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (version == null) {
            version = 1;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    /**
     * Get the structured name with appropriate suffix based on direction
     */
    @Transient
    public String getStructuredName() {
        return name + (direction == Direction.SOURCE ? "_Out" : "_In");
    }
}