-- Script to verify migration results
-- Run this after migrations to ensure data integrity

-- 1. Verify structure migration (V123)
SELECT 
    'Checking integration flows structure migration' as test_name;

-- Should show flows with migrated structure IDs
SELECT 
    id, 
    name,
    source_structure_id,
    target_structure_id,
    source_flow_structure_id,
    target_flow_structure_id,
    CASE 
        WHEN source_flow_structure_id IS NOT NULL AND source_structure_id IS NULL THEN 'MIGRATED'
        WHEN source_flow_structure_id IS NOT NULL AND source_structure_id IS NOT NULL THEN 'ALREADY_HAD_NEW'
        WHEN source_flow_structure_id IS NULL AND source_structure_id IS NOT NULL THEN 'NOT_MIGRATED'
        ELSE 'NO_STRUCTURES'
    END as migration_status
FROM integration_flows
WHERE id IN (
    'b50e8400-e29b-41d4-a716-446655440001',
    'b50e8400-e29b-41d4-a716-446655440002',
    'b50e8400-e29b-41d4-a716-446655440003'
);

-- 2. Verify foreign key constraints (V122)
SELECT 
    'Checking deletion constraints' as test_name;

-- Try to delete a message structure that's referenced (should fail)
-- This will be a manual test - uncomment to test
-- DELETE FROM message_structures WHERE id = '750e8400-e29b-41d4-a716-446655440001';

-- Try to delete a flow structure that's referenced (should fail)
-- DELETE FROM flow_structures WHERE id = '850e8400-e29b-41d4-a716-446655440001';

-- 3. Verify field mapping order fix (V125)
SELECT 
    'Checking field mapping order fix' as test_name;

-- Should show no mappings with order = 0
SELECT 
    fm.id,
    fm.transformation_id,
    ft.name as transformation_name,
    fm.source_fields,
    fm.target_field,
    fm.mapping_order,
    CASE 
        WHEN fm.mapping_order = 0 THEN 'NEEDS_FIX'
        WHEN fm.mapping_order > 0 THEN 'CORRECT'
        ELSE 'NULL_ORDER'
    END as order_status
FROM field_mappings fm
JOIN flow_transformations ft ON fm.transformation_id = ft.id
ORDER BY fm.transformation_id, fm.mapping_order;

-- Count mappings by order status
SELECT 
    CASE 
        WHEN mapping_order = 0 THEN 'ZERO_ORDER'
        WHEN mapping_order > 0 THEN 'VALID_ORDER'
        ELSE 'NULL_ORDER'
    END as order_status,
    COUNT(*) as count
FROM field_mappings
GROUP BY order_status;

-- 4. Verify execution_order column rename (V124)
SELECT 
    'Checking transformation execution_order column' as test_name;

-- Should show execution_order column, not transformation_order
SELECT 
    column_name,
    data_type,
    column_default
FROM information_schema.columns
WHERE table_name = 'flow_transformations'
AND column_name IN ('execution_order', 'transformation_order');

-- 5. Verify deprecated columns are dropped (V126)
SELECT 
    'Checking deprecated columns removal' as test_name;

-- Should return no rows after V126 is applied
SELECT 
    column_name
FROM information_schema.columns
WHERE table_name = 'integration_flows'
AND column_name IN ('source_structure_id', 'target_structure_id');

-- 6. Summary report
SELECT 
    'Migration Summary' as report_type,
    (SELECT COUNT(*) FROM integration_flows WHERE source_flow_structure_id IS NOT NULL) as flows_with_new_structure,
    (SELECT COUNT(*) FROM field_mappings WHERE mapping_order > 0) as mappings_with_valid_order,
    (SELECT COUNT(*) FROM field_mappings WHERE mapping_order = 0) as mappings_with_zero_order,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'flow_transformations' AND column_name = 'execution_order') as has_execution_order_column,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'integration_flows' AND column_name IN ('source_structure_id', 'target_structure_id')) as deprecated_columns_count;