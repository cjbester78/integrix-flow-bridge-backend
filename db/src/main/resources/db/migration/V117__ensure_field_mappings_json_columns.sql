-- Ensure field_mappings JSON columns are properly typed as JSON
-- This resolves the error: column "function_node" is of type json but expression is of type character varying
ALTER TABLE field_mappings 
ALTER COLUMN function_node TYPE JSON USING function_node::JSON;

-- Also ensure other JSON columns are properly typed
ALTER TABLE field_mappings 
ALTER COLUMN source_fields TYPE JSON USING source_fields::JSON;

ALTER TABLE field_mappings 
ALTER COLUMN input_types TYPE JSON USING input_types::JSON;

ALTER TABLE field_mappings 
ALTER COLUMN visual_flow_data TYPE JSON USING visual_flow_data::JSON;

-- Add comments to document the columns
COMMENT ON COLUMN field_mappings.function_node IS 'Function node configuration from visual editor (JSON)';
COMMENT ON COLUMN field_mappings.source_fields IS 'Array of source field names (JSON)';
COMMENT ON COLUMN field_mappings.input_types IS 'Input data types configuration (JSON)';
COMMENT ON COLUMN field_mappings.visual_flow_data IS 'Visual flow editor data (JSON)';