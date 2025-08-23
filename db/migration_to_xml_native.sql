-- Migration to XML-Native Approach for Integrix Flow Bridge
-- This script removes all JSON columns and creates proper XML/namespace handling

-- First, backup important data before deletion
-- You should manually backup your database before running this!

-- Step 1: Delete all existing data that uses JSON format
-- Delete in correct order due to foreign keys

-- Delete field mappings and related data
DELETE FROM field_mappings;
DELETE FROM flow_transformations;
DELETE FROM integration_flows;
DELETE FROM flow_structure_messages;
DELETE FROM flow_structures;
DELETE FROM message_structures;

-- Step 2: Create new tables for namespace handling

-- Namespaces for message structures
CREATE TABLE IF NOT EXISTS message_structure_namespaces (
    id VARCHAR(36) PRIMARY KEY,
    message_structure_id VARCHAR(36) NOT NULL,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_structure_id) REFERENCES message_structures(id) ON DELETE CASCADE,
    INDEX idx_msg_struct_ns (message_structure_id),
    UNIQUE KEY uk_msg_struct_prefix (message_structure_id, prefix)
);

-- Namespaces for flow structures  
CREATE TABLE IF NOT EXISTS flow_structure_namespaces (
    id VARCHAR(36) PRIMARY KEY,
    flow_structure_id VARCHAR(36) NOT NULL,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id) ON DELETE CASCADE,
    INDEX idx_flow_struct_ns (flow_structure_id),
    UNIQUE KEY uk_flow_struct_prefix (flow_structure_id, prefix)
);

-- WSDL operations for flow structures
CREATE TABLE IF NOT EXISTS flow_structure_operations (
    id VARCHAR(36) PRIMARY KEY,
    flow_structure_id VARCHAR(36) NOT NULL,
    operation_name VARCHAR(255) NOT NULL,
    soap_action VARCHAR(500),
    input_element_name VARCHAR(255),
    input_element_namespace VARCHAR(500),
    output_element_name VARCHAR(255),
    output_element_namespace VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id) ON DELETE CASCADE,
    INDEX idx_flow_struct_ops (flow_structure_id),
    UNIQUE KEY uk_flow_struct_operation (flow_structure_id, operation_name)
);

-- Step 3: Modify existing tables to remove JSON columns

-- Message Structures - Remove JSON columns
ALTER TABLE message_structures DROP COLUMN namespace;
ALTER TABLE message_structures DROP COLUMN metadata;
ALTER TABLE message_structures DROP COLUMN tags;
ALTER TABLE message_structures DROP COLUMN import_metadata;

-- Flow Structures - Remove JSON columns  
ALTER TABLE flow_structures DROP COLUMN namespace;
ALTER TABLE flow_structures DROP COLUMN metadata;
ALTER TABLE flow_structures DROP COLUMN tags;

-- Step 4: Create new XML field mappings table to replace the old one
CREATE TABLE IF NOT EXISTS xml_field_mappings (
    id VARCHAR(36) PRIMARY KEY,
    transformation_id VARCHAR(36) NOT NULL,
    source_xpath VARCHAR(2000) NOT NULL,
    target_xpath VARCHAR(2000) NOT NULL,
    mapping_type ENUM('ELEMENT', 'ATTRIBUTE', 'TEXT', 'STRUCTURE') NOT NULL DEFAULT 'TEXT',
    is_repeating BOOLEAN DEFAULT FALSE,
    repeat_context_xpath VARCHAR(1000),
    transform_function TEXT,
    mapping_order INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    FOREIGN KEY (transformation_id) REFERENCES flow_transformations(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_xml_mapping_transformation (transformation_id),
    INDEX idx_xml_mapping_order (transformation_id, mapping_order)
);

-- Step 5: Keep the new table as xml_field_mappings for now
-- The old field_mappings will be dropped after confirming data migration

-- Step 6: Add XML-specific columns to flow_transformations if needed
ALTER TABLE flow_transformations
ADD COLUMN is_xml_transformation BOOLEAN DEFAULT TRUE,
ADD COLUMN namespace_aware BOOLEAN DEFAULT TRUE;

-- Step 8: Create views for easier namespace access

CREATE OR REPLACE VIEW v_message_structure_with_namespaces AS
SELECT 
    ms.id,
    ms.name,
    ms.description,
    ms.xsd_content,
    ms.business_component_id,
    GROUP_CONCAT(
        CONCAT(COALESCE(msn.prefix, ''), '=', msn.uri) 
        ORDER BY msn.is_default DESC, msn.prefix
        SEPARATOR '|'
    ) as namespaces
FROM message_structures ms
LEFT JOIN message_structure_namespaces msn ON ms.id = msn.message_structure_id
GROUP BY ms.id;

CREATE OR REPLACE VIEW v_flow_structure_with_namespaces AS
SELECT 
    fs.id,
    fs.name,
    fs.description,
    fs.wsdl_content,
    fs.business_component_id,
    GROUP_CONCAT(
        CONCAT(COALESCE(fsn.prefix, ''), '=', fsn.uri) 
        ORDER BY fsn.is_default DESC, fsn.prefix
        SEPARATOR '|'
    ) as namespaces
FROM flow_structures fs
LEFT JOIN flow_structure_namespaces fsn ON fs.id = fsn.flow_structure_id
GROUP BY fs.id;

-- Step 7: Add indexes for performance
CREATE INDEX idx_xml_field_mappings_source_xpath ON xml_field_mappings(source_xpath(255));
CREATE INDEX idx_xml_field_mappings_target_xpath ON xml_field_mappings(target_xpath(255));

-- Step 10: Create stored procedures for common operations

DELIMITER $$

-- Procedure to add namespace to message structure
CREATE PROCEDURE IF NOT EXISTS sp_add_message_namespace(
    IN p_message_structure_id VARCHAR(36),
    IN p_prefix VARCHAR(50),
    IN p_uri VARCHAR(500),
    IN p_is_default BOOLEAN
)
BEGIN
    INSERT INTO message_structure_namespaces (id, message_structure_id, prefix, uri, is_default)
    VALUES (UUID(), p_message_structure_id, p_prefix, p_uri, p_is_default)
    ON DUPLICATE KEY UPDATE uri = p_uri, is_default = p_is_default;
END$$

-- Procedure to add namespace to flow structure
CREATE PROCEDURE IF NOT EXISTS sp_add_flow_namespace(
    IN p_flow_structure_id VARCHAR(36),
    IN p_prefix VARCHAR(50),
    IN p_uri VARCHAR(500),
    IN p_is_default BOOLEAN
)
BEGIN
    INSERT INTO flow_structure_namespaces (id, flow_structure_id, prefix, uri, is_default)
    VALUES (UUID(), p_flow_structure_id, p_prefix, p_uri, p_is_default)
    ON DUPLICATE KEY UPDATE uri = p_uri, is_default = p_is_default;
END$$

-- Procedure to add WSDL operation
CREATE PROCEDURE IF NOT EXISTS sp_add_flow_operation(
    IN p_flow_structure_id VARCHAR(36),
    IN p_operation_name VARCHAR(255),
    IN p_soap_action VARCHAR(500),
    IN p_input_element_name VARCHAR(255),
    IN p_input_element_namespace VARCHAR(500)
)
BEGIN
    INSERT INTO flow_structure_operations (
        id, flow_structure_id, operation_name, soap_action, 
        input_element_name, input_element_namespace
    )
    VALUES (
        UUID(), p_flow_structure_id, p_operation_name, p_soap_action,
        p_input_element_name, p_input_element_namespace
    )
    ON DUPLICATE KEY UPDATE 
        soap_action = p_soap_action,
        input_element_name = p_input_element_name,
        input_element_namespace = p_input_element_namespace;
END$$

DELIMITER ;

-- Step 11: Create sample data structure for testing (optional)
-- This shows how the new structure works

-- Example: Insert a message structure for temperature conversion
-- INSERT INTO message_structures (id, name, description, xsd_content, business_component_id, created_by)
-- VALUES ('test-msg-001', 'TempConversion', 'Temperature conversion schema', '<xsd:schema>...</xsd:schema>', 'bc-001', 'user-001');

-- Add namespaces for the message structure
-- CALL sp_add_message_namespace('test-msg-001', 'tns', 'http://tempconvert.example.com', TRUE);
-- CALL sp_add_message_namespace('test-msg-001', 'xsd', 'http://www.w3.org/2001/XMLSchema', FALSE);

-- Print summary
SELECT 'XML-Native Migration Complete!' as Status;
SELECT 'Tables Created:' as Action, 'message_structure_namespaces, flow_structure_namespaces, flow_structure_operations' as Details
UNION ALL
SELECT 'Tables Modified:', 'message_structures, flow_structures, field_mappings (recreated)'
UNION ALL
SELECT 'JSON Columns Removed:', 'namespace, metadata, tags, source_fields, input_types, visual_flow_data'
UNION ALL
SELECT 'New Approach:', 'Use XPath expressions in field_mappings with proper namespace support';