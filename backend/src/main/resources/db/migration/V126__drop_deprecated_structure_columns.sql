-- Drop deprecated structure columns after verifying migration is complete
-- This should only be run after confirming all data has been migrated to the new columns

-- First, let's verify that all data has been migrated
DO $$
DECLARE
    unmigrated_count INTEGER;
    total_with_old_data INTEGER;
BEGIN
    -- Count flows that have old data but no new data
    SELECT COUNT(*) INTO unmigrated_count
    FROM integration_flows
    WHERE (source_structure_id IS NOT NULL AND source_flow_structure_id IS NULL)
       OR (target_structure_id IS NOT NULL AND target_flow_structure_id IS NULL);
    
    -- Count total flows with old data
    SELECT COUNT(*) INTO total_with_old_data
    FROM integration_flows
    WHERE source_structure_id IS NOT NULL 
       OR target_structure_id IS NOT NULL;
    
    IF unmigrated_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop columns: % flows still have unmigrated structure references', unmigrated_count;
    END IF;
    
    RAISE NOTICE 'Migration verified: % flows had structure references, all successfully migrated', total_with_old_data;
END $$;

-- Drop the deprecated columns
ALTER TABLE integration_flows 
DROP COLUMN IF EXISTS source_structure_id,
DROP COLUMN IF EXISTS target_structure_id;

-- Log the completion
DO $$
BEGIN
    RAISE NOTICE 'Successfully dropped deprecated columns: source_structure_id and target_structure_id from integration_flows table';
END $$;