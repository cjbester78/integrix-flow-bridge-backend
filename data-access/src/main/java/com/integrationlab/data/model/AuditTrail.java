package com.integrationlab.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Entity representing an audit trail entry.
 * 
 * <p>Tracks all CRUD operations and significant system events.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "audit_trail", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_business_component", columnList = "business_component_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "businessComponent"})
public class AuditTrail {

    /**
     * Unique identifier (UUID) for the audit entry
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String id;

    /**
     * Type of entity being audited (e.g., IntegrationFlow, CommunicationAdapter)
     */
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    /**
     * ID of the entity being audited
     */
    @Column(name = "entity_id", length = 36, nullable = false)
    private String entityId;

    /**
     * Action performed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    /**
     * JSON representation of changes (before and after values for updates)
     */
    @Column(columnDefinition = "json")
    private String changes;

    /**
     * User who performed the action
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * IP address of the user
     */
    @Column(name = "user_ip", length = 45)
    private String userIp;

    /**
     * User agent string
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Business component context
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    /**
     * Timestamp when the action occurred
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Audit actions
     */
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        DEPLOY,
        ACTIVATE,
        DEACTIVATE,
        EXECUTE,
        LOGIN,
        LOGOUT
    }
}