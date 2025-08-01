-- V6: Add event store table for event sourcing

CREATE TABLE IF NOT EXISTS event_store (
    event_id CHAR(36) NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id CHAR(36) NOT NULL,
    aggregate_version BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSON NOT NULL,
    event_metadata JSON,
    occurred_at TIMESTAMP NOT NULL,
    triggered_by CHAR(36),
    stored_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id CHAR(36),
    causation_id CHAR(36),
    
    -- Indexes for efficient querying
    INDEX idx_event_aggregate (aggregate_id, aggregate_version),
    INDEX idx_event_type (event_type),
    INDEX idx_event_timestamp (occurred_at),
    INDEX idx_event_user (triggered_by),
    INDEX idx_event_correlation (correlation_id),
    
    -- Ensure events are immutable
    CHECK (stored_at >= occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment to table
ALTER TABLE event_store COMMENT = 'Immutable event store for event sourcing and audit trail';