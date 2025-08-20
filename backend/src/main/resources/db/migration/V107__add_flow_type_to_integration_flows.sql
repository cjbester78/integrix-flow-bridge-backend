-- Add flow_type column to integration_flows table if it doesn't exist
ALTER TABLE integration_flows 
ADD COLUMN IF NOT EXISTS flow_type VARCHAR(50) DEFAULT 'DIRECT_MAPPING';

-- Update existing flows to have DIRECT_MAPPING as default
UPDATE integration_flows 
SET flow_type = 'DIRECT_MAPPING' 
WHERE flow_type IS NULL;

-- Add NOT NULL constraint after setting default values (only if column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='flow_type') THEN
        ALTER TABLE integration_flows ALTER COLUMN flow_type SET NOT NULL;
    END IF;
END $$;

-- Add check constraint to ensure valid enum values (only if constraint doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'chk_flow_type' AND table_name = 'integration_flows') THEN
        ALTER TABLE integration_flows 
        ADD CONSTRAINT chk_flow_type 
        CHECK (flow_type IN ('DIRECT_MAPPING', 'ORCHESTRATION'));
    END IF;
END $$;