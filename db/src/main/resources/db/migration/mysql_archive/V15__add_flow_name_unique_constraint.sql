-- Add unique constraint to flow name
ALTER TABLE integration_flows 
ADD CONSTRAINT uk_flow_name UNIQUE (name);

-- Add index for better performance on name lookups
CREATE INDEX idx_flow_name ON integration_flows(name);