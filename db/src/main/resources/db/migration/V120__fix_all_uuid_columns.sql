-- V120__fix_all_uuid_columns.sql
-- Fix all character(36) columns that should be UUID type

-- Fix integration_flows columns
ALTER TABLE integration_flows 
ALTER COLUMN source_structure_id TYPE uuid USING NULLIF(TRIM(source_structure_id), '')::uuid,
ALTER COLUMN target_structure_id TYPE uuid USING NULLIF(TRIM(target_structure_id), '')::uuid,
ALTER COLUMN source_flow_structure_id TYPE uuid USING NULLIF(TRIM(source_flow_structure_id), '')::uuid,
ALTER COLUMN target_flow_structure_id TYPE uuid USING NULLIF(TRIM(target_flow_structure_id), '')::uuid;

-- Fix system_logs columns
ALTER TABLE system_logs
ALTER COLUMN component_id TYPE uuid USING NULLIF(TRIM(component_id), '')::uuid,
ALTER COLUMN domain_reference_id TYPE uuid USING NULLIF(TRIM(domain_reference_id), '')::uuid,
ALTER COLUMN source_id TYPE uuid USING NULLIF(TRIM(source_id), '')::uuid;

-- Fix event_store columns
ALTER TABLE event_store
ALTER COLUMN event_id TYPE uuid USING TRIM(event_id)::uuid,
ALTER COLUMN aggregate_id TYPE uuid USING TRIM(aggregate_id)::uuid,
ALTER COLUMN causation_id TYPE uuid USING NULLIF(TRIM(causation_id), '')::uuid,
ALTER COLUMN correlation_id TYPE uuid USING NULLIF(TRIM(correlation_id), '')::uuid;

-- Verify all changes
SELECT 'integration_flows' as table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'integration_flows' 
AND column_name IN ('source_structure_id', 'target_structure_id', 'source_flow_structure_id', 'target_flow_structure_id')
UNION ALL
SELECT 'system_logs' as table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'system_logs' 
AND column_name IN ('component_id', 'domain_reference_id', 'source_id')
UNION ALL
SELECT 'event_store' as table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'event_store' 
AND column_name IN ('event_id', 'aggregate_id', 'causation_id', 'correlation_id')
ORDER BY table_name, column_name;