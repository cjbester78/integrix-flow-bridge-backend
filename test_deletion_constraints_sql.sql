-- SQL script to test deletion constraints in PostgreSQL
-- Run this with: psql -U integrix -d integrixflowbridge -f test_deletion_constraints_sql.sql

-- Start transaction so we can rollback
BEGIN;

-- Create test data
DO $$
DECLARE
    v_user_id UUID;
    v_business_component_id UUID;
    v_message_structure_id UUID;
    v_flow_structure_id UUID;
    v_integration_flow_id UUID;
    v_transformation_id UUID;
    v_field_mapping_id UUID;
BEGIN
    RAISE NOTICE '=== Testing Deletion Constraints ===';
    
    -- Create test user
    INSERT INTO users (username, password_hash, email, role)
    VALUES ('test_constraint_user', '$2a$10$test', 'test@example.com', 'DEVELOPER')
    RETURNING id INTO v_user_id;
    RAISE NOTICE 'Created test user with ID: %', v_user_id;
    
    -- Create business component
    INSERT INTO business_components (name, description, status)
    VALUES ('Test Component', 'For deletion constraint testing', 'ACTIVE')
    RETURNING id INTO v_business_component_id;
    RAISE NOTICE 'Created business component with ID: %', v_business_component_id;
    
    -- Create message structure
    INSERT INTO message_structures (name, description, xsd_content, source_type, is_active, created_by, business_component_id)
    VALUES ('Test Message Structure', 'Will be referenced', '<xsd:schema/>', 'INTERNAL', true, v_user_id, v_business_component_id)
    RETURNING id INTO v_message_structure_id;
    RAISE NOTICE 'Created message structure with ID: %', v_message_structure_id;
    
    -- Create flow structure
    INSERT INTO flow_structures (name, description, processing_mode, direction, wsdl_content, is_active, created_by, business_component_id)
    VALUES ('Test Flow Structure', 'References message structure', 'SYNC', 'SOURCE', '<wsdl:definitions/>', true, v_user_id, v_business_component_id)
    RETURNING id INTO v_flow_structure_id;
    RAISE NOTICE 'Created flow structure with ID: %', v_flow_structure_id;
    
    -- Link message structure to flow structure
    INSERT INTO flow_structure_messages (flow_structure_id, message_structure_id, message_type)
    VALUES (v_flow_structure_id, v_message_structure_id, 'INPUT');
    RAISE NOTICE 'Linked message structure to flow structure';
    
    -- Create integration flow referencing flow structure
    INSERT INTO integration_flows (name, description, source_flow_structure_id, status, flow_type, mapping_mode, is_active, created_by, business_component_id)
    VALUES ('Test Integration Flow', 'References flow structure', v_flow_structure_id, 'DEVELOPED_INACTIVE', 'DIRECT_MAPPING', 'WITH_MAPPING', true, v_user_id, v_business_component_id)
    RETURNING id INTO v_integration_flow_id;
    RAISE NOTICE 'Created integration flow with ID: %', v_integration_flow_id;
    
    -- Create transformation
    INSERT INTO flow_transformations (flow_id, name, type, configuration, execution_order, is_active)
    VALUES (v_integration_flow_id, 'Test Transformation', 'FIELD_MAPPING', '{}', 1, true)
    RETURNING id INTO v_transformation_id;
    RAISE NOTICE 'Created transformation with ID: %', v_transformation_id;
    
    -- Create field mapping
    INSERT INTO field_mappings (transformation_id, source_fields, target_field, mapping_order, is_active)
    VALUES (v_transformation_id, '["source1"]', 'target1', 1, true)
    RETURNING id INTO v_field_mapping_id;
    RAISE NOTICE 'Created field mapping with ID: %', v_field_mapping_id;
    
    RAISE NOTICE '';
    RAISE NOTICE '=== Testing Constraint Violations ===';
    
    -- Test 1: Try to delete message structure (should fail)
    RAISE NOTICE 'Test 1: Attempting to delete message structure...';
    BEGIN
        DELETE FROM message_structures WHERE id = v_message_structure_id;
        RAISE EXCEPTION 'ERROR: Message structure deletion should have failed!';
    EXCEPTION
        WHEN foreign_key_violation THEN
            RAISE NOTICE '✓ PASS: Message structure deletion blocked by foreign key constraint';
            RAISE NOTICE '  Error: %', SQLERRM;
    END;
    
    -- Test 2: Try to delete flow structure (should fail)
    RAISE NOTICE '';
    RAISE NOTICE 'Test 2: Attempting to delete flow structure...';
    BEGIN
        DELETE FROM flow_structures WHERE id = v_flow_structure_id;
        RAISE EXCEPTION 'ERROR: Flow structure deletion should have failed!';
    EXCEPTION
        WHEN foreign_key_violation THEN
            RAISE NOTICE '✓ PASS: Flow structure deletion blocked by foreign key constraint';
            RAISE NOTICE '  Error: %', SQLERRM;
    END;
    
    -- Test 3: Try to delete transformation (should fail)
    RAISE NOTICE '';
    RAISE NOTICE 'Test 3: Attempting to delete transformation with field mappings...';
    BEGIN
        DELETE FROM flow_transformations WHERE id = v_transformation_id;
        RAISE EXCEPTION 'ERROR: Transformation deletion should have failed!';
    EXCEPTION
        WHEN foreign_key_violation THEN
            RAISE NOTICE '✓ PASS: Transformation deletion blocked by foreign key constraint';
            RAISE NOTICE '  Error: %', SQLERRM;
    END;
    
    RAISE NOTICE '';
    RAISE NOTICE '=== Testing Correct Deletion Order ===';
    
    -- Delete in correct order
    RAISE NOTICE 'Deleting field mapping...';
    DELETE FROM field_mappings WHERE id = v_field_mapping_id;
    RAISE NOTICE '✓ Field mapping deleted successfully';
    
    RAISE NOTICE 'Deleting transformation...';
    DELETE FROM flow_transformations WHERE id = v_transformation_id;
    RAISE NOTICE '✓ Transformation deleted successfully';
    
    RAISE NOTICE 'Deleting integration flow...';
    DELETE FROM integration_flows WHERE id = v_integration_flow_id;
    RAISE NOTICE '✓ Integration flow deleted successfully';
    
    RAISE NOTICE 'Deleting flow structure message link...';
    DELETE FROM flow_structure_messages WHERE flow_structure_id = v_flow_structure_id;
    RAISE NOTICE '✓ Flow structure message link deleted successfully';
    
    RAISE NOTICE 'Deleting flow structure...';
    DELETE FROM flow_structures WHERE id = v_flow_structure_id;
    RAISE NOTICE '✓ Flow structure deleted successfully';
    
    RAISE NOTICE 'Deleting message structure...';
    DELETE FROM message_structures WHERE id = v_message_structure_id;
    RAISE NOTICE '✓ Message structure deleted successfully';
    
    RAISE NOTICE '';
    RAISE NOTICE '=== All Deletion Constraint Tests Passed! ===';
END $$;

-- Rollback to keep database clean
ROLLBACK;

-- Show current foreign key constraints with DELETE RESTRICT
SELECT 
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
    JOIN information_schema.referential_constraints AS rc
      ON rc.constraint_name = tc.constraint_name
      AND rc.constraint_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
    AND tc.table_schema = 'public'
    AND rc.delete_rule = 'RESTRICT'
ORDER BY tc.table_name, kcu.column_name;