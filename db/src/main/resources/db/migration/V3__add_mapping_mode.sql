-- V3__add_mapping_mode.sql
-- Add mapping mode field to integration_flows table

ALTER TABLE integration_flows
ADD COLUMN mapping_mode VARCHAR(50) NOT NULL DEFAULT 'WITH_MAPPING' 
    COMMENT 'Mapping mode: WITH_MAPPING or PASS_THROUGH';

-- Add index for better query performance
CREATE INDEX idx_integration_flows_mapping_mode ON integration_flows(mapping_mode);