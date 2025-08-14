package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing system logs including frontend application logs.
 * Enhanced to support comprehensive logging from both backend and frontend.
 */
@Entity
@Table(name = "system_logs", indexes = {
    @Index(name = "idx_system_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_system_log_level", columnList = "level"),
    @Index(name = "idx_system_log_source", columnList = "source"),
    @Index(name = "idx_system_log_user", columnList = "user_id"),
    @Index(name = "idx_system_log_correlation", columnList = "correlation_id"),
    @Index(name = "idx_system_log_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    private LogLevel level;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "source_id", columnDefinition = "char(36)")
    private String sourceId;

    @Column(name = "source_name", length = 255)
    private String sourceName;

    @Column(name = "component", length = 100)
    private String component;

    @Column(name = "component_id", columnDefinition = "char(36)")
    private String componentId;

    @Column(name = "domain_type", length = 50)
    private String domainType;

    @Column(name = "domain_reference_id", columnDefinition = "char(36)")
    private String domainReferenceId;

    @Column(name = "user_id", columnDefinition = "char(36)")
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    // Frontend-specific fields
    
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Log level enumeration
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    /**
     * Pre-persist method to set defaults
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (level == null) {
            level = LogLevel.INFO;
        }
    }
}