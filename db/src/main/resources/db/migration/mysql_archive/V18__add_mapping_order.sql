-- Add mapping_order column to preserve the order of field mappings
ALTER TABLE field_mappings 
ADD COLUMN mapping_order INT DEFAULT 0 COMMENT 'Order of the mapping within the transformation';

-- Add index for ordering queries
ALTER TABLE field_mappings 
ADD INDEX idx_mapping_order (transformation_id, mapping_order);