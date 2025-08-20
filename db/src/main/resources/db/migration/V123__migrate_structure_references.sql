-- Migrate data from deprecated structure columns to new flow structure columns
-- This handles the incomplete migration from data_structures to flow_structures

-- 1. Migrate source structure references
UPDATE integration_flows 
SET source_flow_structure_id = source_structure_id::uuid 
WHERE source_structure_id IS NOT NULL 
  AND source_flow_structure_id IS NULL
  AND EXISTS (
    SELECT 1 FROM flow_structures fs 
    WHERE fs.id = source_structure_id::uuid
  );

-- 2. Migrate target structure references  
UPDATE integration_flows 
SET target_flow_structure_id = target_structure_id::uuid 
WHERE target_structure_id IS NOT NULL 
  AND target_flow_structure_id IS NULL
  AND EXISTS (
    SELECT 1 FROM flow_structures fs 
    WHERE fs.id = target_structure_id::uuid
  );

-- 3. Log any unmigrated references (pointing to non-existent structures)
DO $$
DECLARE
    unmigrated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unmigrated_count
    FROM integration_flows
    WHERE (source_structure_id IS NOT NULL AND source_flow_structure_id IS NULL)
       OR (target_structure_id IS NOT NULL AND target_flow_structure_id IS NULL);
    
    IF unmigrated_count > 0 THEN
        RAISE NOTICE 'Found % flows with structure references that could not be migrated', unmigrated_count;
    END IF;
END $$;

-- Note: After verifying all data is migrated and the application is updated,
-- run V124__drop_deprecated_structure_columns.sql to remove the old columns