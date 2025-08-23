-- Test data to verify migrations work correctly
-- This script creates test data before running migrations

-- Create test users
INSERT INTO users (id, username, email, password, is_active, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'test_admin', 'admin@test.com', '$2a$10$encrypted', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'test_user', 'user@test.com', '$2a$10$encrypted', true, NOW(), NOW());

-- Create test business components
INSERT INTO business_components (id, name, description, is_active, created_at, updated_at) VALUES
('650e8400-e29b-41d4-a716-446655440001', 'Test Source System', 'Test source system for migration', true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440002', 'Test Target System', 'Test target system for migration', true, NOW(), NOW());

-- Create test message structures
INSERT INTO message_structures (id, name, description, content, structure_format, is_active, created_at, updated_at) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'Test Message Structure 1', 'Test message structure', '{"fields": ["field1", "field2"]}', 'JSON', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440002', 'Test Message Structure 2', 'Another test message structure', '{"fields": ["fieldA", "fieldB"]}', 'JSON', true, NOW(), NOW());

-- Create test flow structures
INSERT INTO flow_structures (id, name, description, content, structure_format, wsdl_content, is_active, created_at, updated_at) VALUES
('850e8400-e29b-41d4-a716-446655440001', 'Test Flow Structure 1', 'Test flow structure', '{"flow": "test"}', 'JSON', NULL, true, NOW(), NOW()),
('850e8400-e29b-41d4-a716-446655440002', 'Test Flow Structure 2', 'Another test flow structure', NULL, 'WSDL', '<wsdl>test</wsdl>', true, NOW(), NOW());

-- Link message structures to flow structures
INSERT INTO flow_structure_messages (id, flow_structure_id, message_structure_id, message_type, created_at) VALUES
('950e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440001', 'REQUEST', NOW()),
('950e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440002', 'RESPONSE', NOW());

-- Create test adapters with flow structures
INSERT INTO communication_adapters (id, name, type, description, configuration, business_component_id, flow_structure_id, is_active, created_at, updated_at) VALUES
('a50e8400-e29b-41d4-a716-446655440001', 'Test Source Adapter', 'HTTP', 'Test HTTP adapter', '{"url": "http://test.com"}', '650e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', true, NOW(), NOW()),
('a50e8400-e29b-41d4-a716-446655440002', 'Test Target Adapter', 'JDBC', 'Test JDBC adapter', '{"connection": "test"}', '650e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440002', true, NOW(), NOW());

-- Create test integration flows with OLD structure columns (to be migrated)
INSERT INTO integration_flows (id, name, description, source_adapter_id, target_adapter_id, source_structure_id, target_structure_id, source_flow_structure_id, target_flow_structure_id, flow_type, is_active, created_at, updated_at, created_by) VALUES
-- Flow with old structure IDs that need migration
('b50e8400-e29b-41d4-a716-446655440001', 'Test Flow for Migration', 'This flow has old structure IDs', 'a50e8400-e29b-41d4-a716-446655440001', 'a50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440002', NULL, NULL, 'DIRECT_MAPPING', true, NOW(), NOW(), 'test_admin'),
-- Flow with new structure IDs already set
('b50e8400-e29b-41d4-a716-446655440002', 'Test Flow Already Migrated', 'This flow already uses new columns', 'a50e8400-e29b-41d4-a716-446655440001', 'a50e8400-e29b-41d4-a716-446655440002', NULL, NULL, '850e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440002', 'DIRECT_MAPPING', true, NOW(), NOW(), 'test_admin'),
-- Flow with both old and new (should keep new)
('b50e8400-e29b-41d4-a716-446655440003', 'Test Flow Mixed', 'This flow has both old and new', 'a50e8400-e29b-41d4-a716-446655440001', 'a50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440002', 'DIRECT_MAPPING', true, NOW(), NOW(), 'test_admin');

-- Create test transformations (with transformation_order to be renamed)
INSERT INTO flow_transformations (id, flow_id, name, type, execution_order, configuration, is_active, created_at, updated_at) VALUES
('c50e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440001', 'Test Transformation 1', 'FIELD_MAPPING', 1, '{}', true, NOW(), NOW()),
('c50e8400-e29b-41d4-a716-446655440002', 'b50e8400-e29b-41d4-a716-446655440002', 'Test Transformation 2', 'FIELD_MAPPING', 2, '{}', true, NOW(), NOW());

-- Create test field mappings (some with order = 0)
INSERT INTO field_mappings (id, transformation_id, source_fields, target_field, mapping_order, is_active, created_at, updated_at) VALUES
-- Mappings with order = 0 (to be fixed)
('d50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440001', 'sourceField1', 'targetField1', 0, true, NOW(), NOW()),
('d50e8400-e29b-41d4-a716-446655440002', 'c50e8400-e29b-41d4-a716-446655440001', 'sourceField2', 'targetField2', 0, true, NOW(), NOW()),
('d50e8400-e29b-41d4-a716-446655440003', 'c50e8400-e29b-41d4-a716-446655440001', 'sourceField3', 'targetField3', 0, true, NOW(), NOW()),
-- Mappings with correct order
('d50e8400-e29b-41d4-a716-446655440004', 'c50e8400-e29b-41d4-a716-446655440002', 'sourceFieldA', 'targetFieldA', 1, true, NOW(), NOW()),
('d50e8400-e29b-41d4-a716-446655440005', 'c50e8400-e29b-41d4-a716-446655440002', 'sourceFieldB', 'targetFieldB', 2, true, NOW(), NOW());