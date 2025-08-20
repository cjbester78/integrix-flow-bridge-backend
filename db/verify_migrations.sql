-- Script to verify migration status and data integrity

-- 1. Check if migrations have been applied (Flyway schema history)
SELECT version, description, success, executed_on 
FROM flyway_schema_history 
WHERE version IN ('122', '123', '124', '125', '126')
ORDER BY installed_rank DESC;

-- 2. Verify structure migration status
SELECT 
    COUNT(*) as total_flows,
    COUNT(source_structure_id) as flows_with_old_source,
    COUNT(target_structure_id) as flows_with_old_target,
    COUNT(source_flow_structure_id) as flows_with_new_source,
    COUNT(target_flow_structure_id) as flows_with_new_target,
    COUNT(CASE WHEN source_structure_id IS NOT NULL AND source_flow_structure_id IS NULL THEN 1 END) as unmigrated_source,
    COUNT(CASE WHEN target_structure_id IS NOT NULL AND target_flow_structure_id IS NULL THEN 1 END) as unmigrated_target
FROM integration_flows;

-- 3. Check column existence in flow_transformations
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'flow_transformations'
AND column_name IN ('execution_order', 'transformation_order');

-- 4. Verify field mapping orders
SELECT 
    ft.name as transformation_name,
    COUNT(fm.id) as field_mapping_count,
    MIN(fm.mapping_order) as min_order,
    MAX(fm.mapping_order) as max_order,
    COUNT(CASE WHEN fm.mapping_order = 0 OR fm.mapping_order IS NULL THEN 1 END) as mappings_with_zero_order
FROM flow_transformations ft
LEFT JOIN field_mappings fm ON fm.transformation_id = ft.id
GROUP BY ft.id, ft.name
ORDER BY ft.name;

-- 5. Check for deprecated columns that should be dropped
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'integration_flows'
AND column_name IN ('source_structure_id', 'target_structure_id');

-- 6. Verify foreign key constraints
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
JOIN information_schema.referential_constraints AS rc
    ON rc.constraint_name = tc.constraint_name
WHERE tc.table_name IN ('flow_structures', 'message_structures', 'integration_flows', 'communication_adapters')
AND tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, tc.constraint_name;