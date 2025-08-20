-- Fix flow structure UUID columns in integration_flows table
-- Convert char(36) to uuid type

-- Drop any dependent views or constraints first
ALTER TABLE integration_flows 
  ALTER COLUMN source_flow_structure_id TYPE uuid USING source_flow_structure_id::uuid,
  ALTER COLUMN target_flow_structure_id TYPE uuid USING target_flow_structure_id::uuid;

-- Also fix source_structure_id and target_structure_id if they exist as char
DO $$ 
BEGIN
  -- Check if columns exist and are char type
  IF EXISTS (
    SELECT 1 
    FROM information_schema.columns 
    WHERE table_name = 'integration_flows' 
    AND column_name = 'source_structure_id' 
    AND data_type = 'character'
  ) THEN
    ALTER TABLE integration_flows 
      ALTER COLUMN source_structure_id TYPE uuid USING source_structure_id::uuid;
  END IF;
  
  IF EXISTS (
    SELECT 1 
    FROM information_schema.columns 
    WHERE table_name = 'integration_flows' 
    AND column_name = 'target_structure_id' 
    AND data_type = 'character'
  ) THEN
    ALTER TABLE integration_flows 
      ALTER COLUMN target_structure_id TYPE uuid USING target_structure_id::uuid;
  END IF;
END $$;