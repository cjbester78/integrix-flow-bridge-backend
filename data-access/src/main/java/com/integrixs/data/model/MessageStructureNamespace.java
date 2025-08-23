package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing XML namespaces for message structures.
 * 
 * <p>Stores namespace prefix and URI mappings for XSD schemas.
 * 
 * @author Integration Team
 * @since 2.0.0
 */
@Entity
@Table(name = "message_structure_namespaces", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"message_structure_id", "prefix"}),
       indexes = @Index(name = "idx_msg_struct_ns", columnList = "message_structure_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"messageStructure"})
public class MessageStructureNamespace {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * The message structure this namespace belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_structure_id", nullable = false)
    @NotNull(message = "Message structure is required")
    private MessageStructure messageStructure;

    /**
     * Namespace prefix (can be null for default namespace)
     */
    @Column(length = 50)
    @Size(max = 50, message = "Prefix cannot exceed 50 characters")
    private String prefix;

    /**
     * Namespace URI
     */
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Namespace URI is required")
    @Size(max = 500, message = "URI cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*|^urn:.*", message = "URI must be a valid HTTP(S) URL or URN")
    private String uri;

    /**
     * Whether this is the default namespace
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}