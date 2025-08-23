-- V129: Add missing columns to data_structures table
-- The DataStructure entity expects additional columns that were missing from the initial table creation

-- Add missing columns
ALTER TABLE data_structures 
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS usage_type VARCHAR(50) NOT NULL DEFAULT 'both',
ADD COLUMN IF NOT EXISTS structure JSON NOT NULL DEFAULT '{}',
ADD COLUMN IF NOT EXISTS original_content TEXT,
ADD COLUMN IF NOT EXISTS original_format VARCHAR(20),
ADD COLUMN IF NOT EXISTS namespace JSON,
ADD COLUMN IF NOT EXISTS tags JSON,
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- PostgreSQL doesn't have LONGTEXT, use TEXT instead
-- Note: The structure column was changed from TEXT to JSON in the entity
-- but PostgreSQL stores JSON as TEXT internally anyway

-- Add check constraint for usage_type enum
ALTER TABLE data_structures 
DROP CONSTRAINT IF EXISTS chk_usage_type;

ALTER TABLE data_structures 
ADD CONSTRAINT chk_usage_type 
CHECK (usage_type IN ('source', 'target', 'both'));

-- Add index on active records
CREATE INDEX IF NOT EXISTS idx_data_structures_active 
ON data_structures(is_active) WHERE is_active = true;