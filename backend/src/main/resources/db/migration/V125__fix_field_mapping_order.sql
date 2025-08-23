-- Fix field mapping order for existing mappings that have order = 0
-- This assigns sequential order numbers to mappings within each transformation

WITH numbered_mappings AS (
    SELECT 
        id,
        transformation_id,
        ROW_NUMBER() OVER (PARTITION BY transformation_id ORDER BY created_at, id) as new_order
    FROM field_mappings
    WHERE mapping_order = 0 OR mapping_order IS NULL
)
UPDATE field_mappings fm
SET mapping_order = nm.new_order
FROM numbered_mappings nm
WHERE fm.id = nm.id;

-- Ensure no null values remain
UPDATE field_mappings 
SET mapping_order = 1 
WHERE mapping_order IS NULL;