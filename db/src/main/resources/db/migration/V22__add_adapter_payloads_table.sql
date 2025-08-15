-- Create adapter_payloads table for storing request/response payloads
CREATE TABLE IF NOT EXISTS adapter_payloads (
    id CHAR(36) PRIMARY KEY,
    correlation_id VARCHAR(100) NOT NULL,
    adapter_id CHAR(36) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    adapter_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL, -- INBOUND or OUTBOUND
    payload_type VARCHAR(20) NOT NULL, -- REQUEST or RESPONSE
    payload LONGTEXT, -- Store the actual payload
    payload_size INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_adapter_payload_correlation (correlation_id),
    INDEX idx_adapter_payload_created_at (created_at),
    INDEX idx_adapter_payload_adapter (adapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;