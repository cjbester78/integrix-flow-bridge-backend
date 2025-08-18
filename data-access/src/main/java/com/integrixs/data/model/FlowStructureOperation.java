package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing WSDL operations for flow structures.
 * 
 * <p>Stores operation details extracted from WSDL definitions.
 * 
 * @author Integration Team
 * @since 2.0.0
 */
@Entity
@Table(name = "flow_structure_operations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"flow_structure_id", "operation_name"}),
       indexes = @Index(name = "idx_flow_struct_ops", columnList = "flow_structure_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"flowStructure"})
public class FlowStructureOperation {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * The flow structure this operation belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_structure_id", nullable = false)
    @NotNull(message = "Flow structure is required")
    private FlowStructure flowStructure;

    /**
     * Operation name
     */
    @Column(name = "operation_name", nullable = false, length = 255)
    @NotBlank(message = "Operation name is required")
    @Size(max = 255, message = "Operation name cannot exceed 255 characters")
    private String operationName;

    /**
     * SOAP action header value
     */
    @Column(name = "soap_action", length = 500)
    @Size(max = 500, message = "SOAP action cannot exceed 500 characters")
    private String soapAction;

    /**
     * Input element name
     */
    @Column(name = "input_element_name", length = 255)
    @Size(max = 255, message = "Input element name cannot exceed 255 characters")
    private String inputElementName;

    /**
     * Input element namespace URI
     */
    @Column(name = "input_element_namespace", length = 500)
    @Size(max = 500, message = "Input element namespace cannot exceed 500 characters")
    private String inputElementNamespace;

    /**
     * Output element name
     */
    @Column(name = "output_element_name", length = 255)
    @Size(max = 255, message = "Output element name cannot exceed 255 characters")
    private String outputElementName;

    /**
     * Output element namespace URI
     */
    @Column(name = "output_element_namespace", length = 500)
    @Size(max = 500, message = "Output element namespace cannot exceed 500 characters")
    private String outputElementNamespace;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}