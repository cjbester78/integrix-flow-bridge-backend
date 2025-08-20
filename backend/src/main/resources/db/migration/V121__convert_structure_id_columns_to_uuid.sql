-- Convert structure ID columns from char(36) to uuid type in integration_flows table

-- First convert source_flow_structure_id
ALTER TABLE integration_flows 
ALTER COLUMN source_flow_structure_id TYPE uuid USING source_flow_structure_id::uuid;

-- Convert target_flow_structure_id
ALTER TABLE integration_flows 
ALTER COLUMN target_flow_structure_id TYPE uuid USING target_flow_structure_id::uuid;

-- Convert source_structure_id
ALTER TABLE integration_flows 
ALTER COLUMN source_structure_id TYPE uuid USING source_structure_id::uuid;

-- Convert target_structure_id
ALTER TABLE integration_flows 
ALTER COLUMN target_structure_id TYPE uuid USING target_structure_id::uuid;

-- Convert deployed_by
ALTER TABLE integration_flows 
ALTER COLUMN deployed_by TYPE uuid USING deployed_by::uuid;