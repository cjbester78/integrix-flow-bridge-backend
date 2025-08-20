-- Drop the old adapter_payloads table and recreate with the correct structure
-- to match the JPA entity

-- First drop the old table
DROP TABLE IF EXISTS adapter_payloads CASCADE;

-- Create the new adapter_payloads table that matches the JPA entity
CREATE TABLE adapter_payloads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id VARCHAR(100) NOT NULL,
    adapter_id UUID NOT NULL REFERENCES communication_adapters(id) ON DELETE CASCADE,
    adapter_name VARCHAR(255) NOT NULL,
    adapter_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL, -- INBOUND or OUTBOUND
    payload_type VARCHAR(20) NOT NULL, -- REQUEST or RESPONSE
    message_structure_id UUID, -- Make this nullable since we don't always have it
    payload TEXT,
    payload_size INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_adapter_payload_correlation ON adapter_payloads(correlation_id);
CREATE INDEX idx_adapter_payload_created_at ON adapter_payloads(created_at);
CREATE INDEX idx_adapter_payload_adapter ON adapter_payloads(adapter_id);