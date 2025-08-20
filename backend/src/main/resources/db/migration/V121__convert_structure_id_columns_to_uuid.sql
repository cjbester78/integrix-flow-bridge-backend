-- Convert structure ID columns from char(36) to uuid type in integration_flows table

-- Only convert columns that exist and are not already UUID type
DO $$
BEGIN
    -- Convert source_flow_structure_id if it exists and is not UUID
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='source_flow_structure_id' 
               AND data_type != 'uuid') THEN
        ALTER TABLE integration_flows 
        ALTER COLUMN source_flow_structure_id TYPE uuid USING source_flow_structure_id::uuid;
    END IF;

    -- Convert target_flow_structure_id if it exists and is not UUID
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='target_flow_structure_id' 
               AND data_type != 'uuid') THEN
        ALTER TABLE integration_flows 
        ALTER COLUMN target_flow_structure_id TYPE uuid USING target_flow_structure_id::uuid;
    END IF;

    -- Convert source_structure_id if it exists (old column)
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='source_structure_id') THEN
        ALTER TABLE integration_flows 
        ALTER COLUMN source_structure_id TYPE uuid USING source_structure_id::uuid;
    END IF;

    -- Convert target_structure_id if it exists (old column)
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='target_structure_id') THEN
        ALTER TABLE integration_flows 
        ALTER COLUMN target_structure_id TYPE uuid USING target_structure_id::uuid;
    END IF;

    -- Convert deployed_by if it exists and is not UUID
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='integration_flows' AND column_name='deployed_by' 
               AND data_type != 'uuid') THEN
        ALTER TABLE integration_flows 
        ALTER COLUMN deployed_by TYPE uuid USING deployed_by::uuid;
    END IF;
END $$;