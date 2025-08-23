-- Add flow_type column to integration_flows table
ALTER TABLE integration_flows 
ADD COLUMN flow_type VARCHAR(50) DEFAULT 'DIRECT_MAPPING';

-- Update existing flows to have DIRECT_MAPPING as default
UPDATE integration_flows 
SET flow_type = 'DIRECT_MAPPING' 
WHERE flow_type IS NULL;

-- Add NOT NULL constraint after setting default values
ALTER TABLE integration_flows 
ALTER COLUMN flow_type SET NOT NULL;

-- Add check constraint to ensure valid enum values
ALTER TABLE integration_flows 
ADD CONSTRAINT chk_flow_type 
CHECK (flow_type IN ('DIRECT_MAPPING', 'ORCHESTRATION'));