-- Add source_type column to flow_structures table
ALTER TABLE flow_structures 
ADD COLUMN source_type VARCHAR(20) DEFAULT 'INTERNAL' AFTER wsdl_content;

-- Update existing records to INTERNAL
UPDATE flow_structures 
SET source_type = 'INTERNAL' 
WHERE source_type IS NULL;