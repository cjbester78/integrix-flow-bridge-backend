-- Clean up duplicate columns in communication_adapters table

-- Drop the old connection_config column since we have configuration
ALTER TABLE communication_adapters 
DROP COLUMN IF EXISTS connection_config;

-- Drop the old adapter_type column since we have type
ALTER TABLE communication_adapters 
DROP COLUMN IF EXISTS adapter_type;

-- Ensure configuration column is NOT NULL and has proper type
ALTER TABLE communication_adapters 
ALTER COLUMN configuration TYPE JSON USING 
    CASE 
        WHEN configuration IS NULL OR configuration::TEXT = '' THEN '{}'::JSON
        ELSE configuration::JSON
    END;

-- Set default for configuration
ALTER TABLE communication_adapters 
ALTER COLUMN configuration SET DEFAULT '{}'::JSON;

-- Make configuration NOT NULL
UPDATE communication_adapters 
SET configuration = '{}'::JSON 
WHERE configuration IS NULL;

ALTER TABLE communication_adapters 
ALTER COLUMN configuration SET NOT NULL;