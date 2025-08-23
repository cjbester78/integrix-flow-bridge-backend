-- Add columns to store visual flow data and function node information
ALTER TABLE field_mappings 
ADD COLUMN visual_flow_data JSON NULL COMMENT 'Visual flow graph data (nodes and edges)',
ADD COLUMN function_node JSON NULL COMMENT 'Function node configuration data';

-- Add index for performance when querying mappings with visual flows
ALTER TABLE field_mappings 
ADD INDEX idx_visual_flow_exists ((CASE WHEN visual_flow_data IS NOT NULL THEN 1 ELSE 0 END));