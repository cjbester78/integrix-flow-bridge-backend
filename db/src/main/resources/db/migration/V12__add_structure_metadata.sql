-- Add metadata column to data_structures table to store additional information like WSDL operation info
ALTER TABLE data_structures 
ADD COLUMN metadata JSON AFTER namespace;

-- Update description
COMMENT ON COLUMN data_structures.metadata IS 'Additional metadata about the structure (e.g., WSDL operation info)';