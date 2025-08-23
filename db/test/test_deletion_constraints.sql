-- Test script for deletion constraints
-- This script tests that the foreign key constraints prevent unwanted deletions

-- Test 1: Try to delete a message structure that's used by a flow structure
-- Expected: Should fail with foreign key constraint violation
BEGIN;
    SELECT 'Test 1: Delete message structure used by flow structure' as test_name;
    
    -- Show the relationship before attempting delete
    SELECT 
        ms.id as message_id,
        ms.name as message_name,
        fs.name as flow_structure_name
    FROM message_structures ms
    JOIN flow_structure_messages fsm ON ms.id = fsm.message_structure_id
    JOIN flow_structures fs ON fsm.flow_structure_id = fs.id
    WHERE ms.id = '750e8400-e29b-41d4-a716-446655440001';
    
    -- Attempt delete (should fail)
    DELETE FROM message_structures WHERE id = '750e8400-e29b-41d4-a716-446655440001';
ROLLBACK;

-- Test 2: Try to delete a flow structure that's used by an adapter
-- Expected: Should fail with foreign key constraint violation
BEGIN;
    SELECT 'Test 2: Delete flow structure used by adapter' as test_name;
    
    -- Show the relationship before attempting delete
    SELECT 
        fs.id as flow_structure_id,
        fs.name as flow_structure_name,
        ca.name as adapter_name
    FROM flow_structures fs
    JOIN communication_adapters ca ON fs.id = ca.flow_structure_id
    WHERE fs.id = '850e8400-e29b-41d4-a716-446655440001';
    
    -- Attempt delete (should fail)
    DELETE FROM flow_structures WHERE id = '850e8400-e29b-41d4-a716-446655440001';
ROLLBACK;

-- Test 3: Try to delete a flow structure that's used by an integration flow
-- Expected: Should fail with foreign key constraint violation
BEGIN;
    SELECT 'Test 3: Delete flow structure used by integration flow' as test_name;
    
    -- Show the relationship before attempting delete
    SELECT 
        fs.id as flow_structure_id,
        fs.name as flow_structure_name,
        if.name as flow_name,
        'source' as used_as
    FROM flow_structures fs
    JOIN integration_flows if ON fs.id = if.source_flow_structure_id
    WHERE fs.id = '850e8400-e29b-41d4-a716-446655440001'
    UNION ALL
    SELECT 
        fs.id as flow_structure_id,
        fs.name as flow_structure_name,
        if.name as flow_name,
        'target' as used_as
    FROM flow_structures fs
    JOIN integration_flows if ON fs.id = if.target_flow_structure_id
    WHERE fs.id = '850e8400-e29b-41d4-a716-446655440002';
    
    -- Attempt delete (should fail)
    DELETE FROM flow_structures WHERE id = '850e8400-e29b-41d4-a716-446655440001';
ROLLBACK;

-- Test 4: Successfully delete unreferenced structures
-- Expected: Should succeed
BEGIN;
    SELECT 'Test 4: Delete unreferenced structures' as test_name;
    
    -- Create a structure that's not referenced
    INSERT INTO message_structures (id, name, description, content, structure_format, is_active, created_at, updated_at) VALUES
    ('e50e8400-e29b-41d4-a716-446655440001', 'Temporary Message Structure', 'Can be deleted', '{}', 'JSON', true, NOW(), NOW());
    
    -- Verify it exists
    SELECT id, name FROM message_structures WHERE id = 'e50e8400-e29b-41d4-a716-446655440001';
    
    -- Delete it (should succeed)
    DELETE FROM message_structures WHERE id = 'e50e8400-e29b-41d4-a716-446655440001';
    
    -- Verify it's gone
    SELECT COUNT(*) as should_be_zero FROM message_structures WHERE id = 'e50e8400-e29b-41d4-a716-446655440001';
COMMIT;

-- Summary of constraint tests
SELECT 
    'Constraint Test Summary' as summary,
    'Run each test block individually to see constraint violations' as note;