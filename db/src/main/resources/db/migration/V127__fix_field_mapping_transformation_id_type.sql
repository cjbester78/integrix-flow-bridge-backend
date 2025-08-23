-- V127: Fix field_mappings.transformation_id type to match flow_transformations.id
-- This migration converts transformation_id from character(36) to uuid type

-- First, check if any invalid UUIDs exist
DO $$
BEGIN
    -- Validate all transformation_ids are valid UUIDs
    PERFORM id FROM field_mappings 
    WHERE transformation_id IS NOT NULL 
    AND transformation_id::text !~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
    
    IF FOUND THEN
        RAISE EXCEPTION 'Invalid UUID format found in field_mappings.transformation_id';
    END IF;
END $$;

-- Convert the column type from character(36) to uuid
ALTER TABLE field_mappings 
ALTER COLUMN transformation_id TYPE uuid 
USING transformation_id::uuid;

-- Now add the foreign key constraint that was skipped in V122
ALTER TABLE field_mappings
ADD CONSTRAINT fk_mappings_transformation 
    FOREIGN KEY (transformation_id) 
    REFERENCES flow_transformations(id) 
    ON DELETE RESTRICT;

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_field_mappings_transformation_fk 
    ON field_mappings(transformation_id);

-- Verify the constraint was created
DO $$
DECLARE
    constraint_exists boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_mappings_transformation'
        AND table_name = 'field_mappings'
    ) INTO constraint_exists;
    
    IF constraint_exists THEN
        RAISE NOTICE 'Foreign key constraint fk_mappings_transformation successfully created';
    ELSE
        RAISE EXCEPTION 'Failed to create foreign key constraint';
    END IF;
END $$;