package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing adapter payloads (requests and responses)
 */
@Entity
@Table(name = "adapter_payloads", indexes = {
    @Index(name = "idx_adapter_payload_correlation", columnList = "correlation_id"),
    @Index(name = "idx_adapter_payload_created_at", columnList = "created_at"),
    @Index(name = "idx_adapter_payload_adapter", columnList = "adapter_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AdapterPayload {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "adapter_id", nullable = false)
    private UUID adapterId;

    @Column(name = "adapter_name", nullable = false, length = 255)
    private String adapterName;

    @Column(name = "adapter_type", nullable = false, length = 50)
    private String adapterType;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction; // INBOUND or OUTBOUND

    @Column(name = "payload_type", nullable = false, length = 20)
    private String payloadType; // REQUEST or RESPONSE

    @Column(name = "message_structure_id", nullable = false, columnDefinition = "UUID")
    private UUID messageStructureId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "payload_size")
    private Integer payloadSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}