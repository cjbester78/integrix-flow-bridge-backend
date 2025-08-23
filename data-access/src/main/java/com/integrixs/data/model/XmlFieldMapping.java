package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing XML field mappings using XPath expressions.
 * 
 * <p>This replaces the JSON-based field mappings with proper XML/XPath support.
 * 
 * @author Integration Team
 * @since 2.0.0
 */
@Entity
@Table(name = "xml_field_mappings", indexes = {
    @Index(name = "idx_xml_mapping_transformation", columnList = "transformation_id"),
    @Index(name = "idx_xml_mapping_order", columnList = "transformation_id,mapping_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"transformation", "createdBy", "updatedBy"})
public class XmlFieldMapping {

    /**
     * Unique identifier (UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
     * Source XPath expression
     */
    @Column(name = "source_xpath", nullable = false, length = 2000)
    @NotBlank(message = "Source XPath is required")
    @Size(max = 2000, message = "Source XPath cannot exceed 2000 characters")
    private String sourceXPath;

    /**
     * Target XPath expression
     */
    @Column(name = "target_xpath", nullable = false, length = 2000)
    @NotBlank(message = "Target XPath is required")
    @Size(max = 2000, message = "Target XPath cannot exceed 2000 characters")
    private String targetXPath;

    /**
     * Type of mapping
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false, length = 20)
    @NotNull(message = "Mapping type is required")
    private MappingType mappingType = MappingType.TEXT;

    /**
     * Whether this mapping handles repeating elements
     */
    @Column(name = "is_repeating", nullable = false)
    @Builder.Default
    private boolean isRepeating = false;

    /**
     * XPath context for repeating elements
     */
    @Column(name = "repeat_context_xpath", length = 1000)
    @Size(max = 1000, message = "Repeat context XPath cannot exceed 1000 characters")
    private String repeatContextXPath;

    /**
     * Optional transformation function (JavaScript or Java)
     */
    @Column(name = "transform_function", columnDefinition = "TEXT")
    private String transformFunction;

    /**
     * Order of this mapping within the transformation
     */
    @Column(name = "mapping_order")
    @Builder.Default
    private Integer mappingOrder = 0;

    /**
     * Description of the mapping
     */
    @Column(length = 500)
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
     * Enum defining the type of XML mapping
     */
    public enum MappingType {
        /**
         * Map entire element including child elements
         */
        ELEMENT,
        
        /**
         * Map attribute value only
         */
        ATTRIBUTE,
        
        /**
         * Map text content only (default)
         */
        TEXT,
        
        /**
         * Define structure without value mapping
         */
        STRUCTURE
    }
}