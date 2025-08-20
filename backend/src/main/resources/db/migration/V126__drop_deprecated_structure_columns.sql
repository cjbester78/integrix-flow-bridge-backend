-- Drop deprecated structure columns if they exist
-- The old columns source_structure_id and target_structure_id may have already been dropped

DO $$
BEGIN
    -- Check if old columns exist before trying to drop them
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='source_structure_id') THEN
        ALTER TABLE integration_flows DROP COLUMN source_structure_id;
        RAISE NOTICE 'Dropped deprecated column: source_structure_id from integration_flows table';
    ELSE
        RAISE NOTICE 'Column source_structure_id does not exist, skipping drop';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='target_structure_id') THEN
        ALTER TABLE integration_flows DROP COLUMN target_structure_id;
        RAISE NOTICE 'Dropped deprecated column: target_structure_id from integration_flows table';
    ELSE
        RAISE NOTICE 'Column target_structure_id does not exist, skipping drop';
    END IF;
END $$;