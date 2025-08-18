package com.integrixs.data.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_structures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"businessComponent", "createdBy", "updatedBy"})
public class MessageStructure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "xsd_content", columnDefinition = "XML", nullable = false)
    private String xsdContent;
    
    // Namespace, metadata, and tags removed - use related tables instead
    
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;
    
    @Column(name = "source_type", length = 20)
    @Builder.Default
    private String sourceType = "INTERNAL";
    
    @Column(name = "is_editable")
    @Builder.Default
    private Boolean isEditable = true;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    // Import metadata removed - stored in separate table if needed
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id", nullable = false)
    private BusinessComponent businessComponent;
    
    // One-to-many relationship with namespaces
    @OneToMany(mappedBy = "messageStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageStructureNamespace> namespaces = new ArrayList<>();
    
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
    
    @PrePersist
    protected void onCreate() {
        if (version == null) {
            version = 1;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}