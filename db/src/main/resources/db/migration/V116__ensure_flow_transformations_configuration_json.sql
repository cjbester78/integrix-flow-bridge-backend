-- Ensure flow_transformations.configuration is properly typed as JSON
-- This resolves the error: column "configuration" is of type json but expression is of type character varying
ALTER TABLE flow_transformations 
ALTER COLUMN configuration TYPE JSON USING configuration::JSON;

-- Add comment to document the column
COMMENT ON COLUMN flow_transformations.configuration IS 'JSON configuration for the transformation';