-- Fix transformation_order column name to match entity mapping
-- The entity uses execution_order but PostgreSQL schema had transformation_order

-- Check if we need to rename the column
DO $$
BEGIN
    -- Only rename if transformation_order exists and execution_order doesn't
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'flow_transformations' 
        AND column_name = 'transformation_order'
    ) AND NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'flow_transformations' 
        AND column_name = 'execution_order'
    ) THEN
        ALTER TABLE flow_transformations 
        RENAME COLUMN transformation_order TO execution_order;
    END IF;
END $$;

-- Ensure the column exists with correct name and type
ALTER TABLE flow_transformations 
ADD COLUMN IF NOT EXISTS execution_order INT DEFAULT 1;

-- Update any null values to 1
UPDATE flow_transformations 
SET execution_order = 1 
WHERE execution_order IS NULL;

-- Add NOT NULL constraint if not already present
DO $$
BEGIN
    ALTER TABLE flow_transformations 
    ALTER COLUMN execution_order SET NOT NULL;
EXCEPTION
    WHEN others THEN
        -- Column might already be NOT NULL
        NULL;
END $$;