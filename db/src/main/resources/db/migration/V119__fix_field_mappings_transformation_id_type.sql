-- V119__fix_field_mappings_transformation_id_type.sql
-- Fix field_mappings UUID columns from character(36) to uuid type

-- First, drop the foreign key constraint if it exists
ALTER TABLE field_mappings 
DROP CONSTRAINT IF EXISTS field_mappings_transformation_id_fkey;

-- Drop the indexes temporarily
DROP INDEX IF EXISTS idx_mapping_transformation;

-- Convert the id column type from character(36) to uuid
ALTER TABLE field_mappings 
ALTER COLUMN id TYPE uuid USING TRIM(id)::uuid;

-- Convert the transformation_id column type from character(36) to uuid
ALTER TABLE field_mappings 
ALTER COLUMN transformation_id TYPE uuid USING TRIM(transformation_id)::uuid;

-- Recreate the foreign key constraint
ALTER TABLE field_mappings 
ADD CONSTRAINT field_mappings_transformation_id_fkey 
FOREIGN KEY (transformation_id) 
REFERENCES flow_transformations(id) 
ON DELETE CASCADE;

-- Recreate the index
CREATE INDEX idx_mapping_transformation ON field_mappings(transformation_id);

-- Verify the changes
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'field_mappings' 
AND column_name IN ('id', 'transformation_id');