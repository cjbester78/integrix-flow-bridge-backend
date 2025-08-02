package com.integrationlab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a business component.
 * 
 * <p>A business component represents an organizational unit or department
 * that owns and manages integration flows and adapters.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "business_components", indexes = {
    @Index(name = "idx_bc_name", columnList = "name"),
    @Index(name = "idx_bc_email", columnList = "contact_email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"communicationAdapters", "integrationFlows"})
public class BusinessComponent {

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
     * Name of the business component
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Business component name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Detailed description of the component
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Primary contact email address
     */
    @Column(name = "contact_email", length = 255)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    /**
     * Contact phone number
     */
    @Column(name = "contact_phone", length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactPhone;

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
     * Status of the business component (ACTIVE, INACTIVE)
     */
    @Column(length = 20)
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Department or division name
     */
    @Column(length = 100)
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    /**
     * Communication adapters owned by this component
     */
    @OneToMany(mappedBy = "businessComponent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CommunicationAdapter> communicationAdapters = new ArrayList<>();

    /**
     * Integration flows owned by this component
     */
    @OneToMany(mappedBy = "businessComponent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IntegrationFlow> integrationFlows = new ArrayList<>();

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
}
