-- V4__add_deployment_fields.sql
-- Add deployment fields to integration_flows table

ALTER TABLE integration_flows
ADD COLUMN deployed_at TIMESTAMP NULL COMMENT 'Timestamp when flow was deployed',
ADD COLUMN deployed_by CHAR(36) NULL COMMENT 'User ID who deployed the flow',
ADD COLUMN deployment_endpoint VARCHAR(500) NULL COMMENT 'Generated endpoint URL for deployed flow',
ADD COLUMN deployment_metadata JSON NULL COMMENT 'Additional deployment information (WSDL URL, API docs, etc.)';

-- Add indexes for deployment queries
CREATE INDEX idx_integration_flows_deployed ON integration_flows(status, deployed_at);
CREATE INDEX idx_integration_flows_deployed_by ON integration_flows(deployed_by);