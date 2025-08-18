-- PostgreSQL Migration V1: Complete XML-Native Schema
-- This consolidates all necessary tables without JSON columns

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles mapping
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Business components
CREATE TABLE business_components (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Message structures (XML-native)
CREATE TABLE message_structures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    xsd_content XML NOT NULL,
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    version VARCHAR(50) DEFAULT '1.0',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    CONSTRAINT valid_xsd CHECK (xml_is_well_formed_document(xsd_content::text))
);

-- Message structure namespaces
CREATE TABLE message_structure_namespaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_structure_id UUID NOT NULL REFERENCES message_structures(id) ON DELETE CASCADE,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (message_structure_id, prefix)
);

-- Flow structures (XML-native)
CREATE TABLE flow_structures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    processing_mode VARCHAR(20) NOT NULL CHECK (processing_mode IN ('SYNC', 'ASYNC')),
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('INBOUND', 'OUTBOUND', 'BIDIRECTIONAL')),
    wsdl_content XML,
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    version VARCHAR(50) DEFAULT '1.0',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    CONSTRAINT valid_wsdl CHECK (wsdl_content IS NULL OR xml_is_well_formed_document(wsdl_content::text))
);

-- Flow structure namespaces
CREATE TABLE flow_structure_namespaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_structure_id UUID NOT NULL REFERENCES flow_structures(id) ON DELETE CASCADE,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (flow_structure_id, prefix)
);

-- Flow structure operations
CREATE TABLE flow_structure_operations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_structure_id UUID NOT NULL REFERENCES flow_structures(id) ON DELETE CASCADE,
    operation_name VARCHAR(255) NOT NULL,
    soap_action VARCHAR(500),
    input_element_name VARCHAR(255),
    input_element_namespace VARCHAR(500),
    output_element_name VARCHAR(255),
    output_element_namespace VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (flow_structure_id, operation_name)
);

-- Communication adapters
CREATE TABLE communication_adapters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    adapter_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('SENDER', 'RECEIVER')),
    connection_config TEXT, -- Will store XML configuration
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    is_deployed BOOLEAN DEFAULT FALSE,
    deployment_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Integration flows
CREATE TABLE integration_flows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    source_adapter_id UUID REFERENCES communication_adapters(id),
    target_adapter_id UUID REFERENCES communication_adapters(id),
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    status VARCHAR(50) DEFAULT 'DRAFT',
    version VARCHAR(50) DEFAULT '1.0',
    is_deployed BOOLEAN DEFAULT FALSE,
    deployment_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Flow transformations
CREATE TABLE flow_transformations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    flow_id UUID NOT NULL REFERENCES integration_flows(id) ON DELETE CASCADE,
    source_structure_id UUID REFERENCES message_structures(id),
    target_structure_id UUID REFERENCES message_structures(id),
    transformation_order INT DEFAULT 0,
    is_xml_transformation BOOLEAN DEFAULT TRUE,
    namespace_aware BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- XML field mappings (replacing JSON-based field_mappings)
CREATE TABLE xml_field_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transformation_id UUID NOT NULL REFERENCES flow_transformations(id) ON DELETE CASCADE,
    source_xpath VARCHAR(2000) NOT NULL,
    target_xpath VARCHAR(2000) NOT NULL,
    mapping_type VARCHAR(20) NOT NULL CHECK (mapping_type IN ('ELEMENT', 'ATTRIBUTE', 'TEXT', 'STRUCTURE')),
    is_repeating BOOLEAN DEFAULT FALSE,
    repeat_context_xpath VARCHAR(1000),
    transform_function TEXT,
    mapping_order INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Flow executions
CREATE TABLE flow_executions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_id UUID NOT NULL REFERENCES integration_flows(id),
    execution_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_end TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    input_message_id UUID,
    output_message_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_id UUID REFERENCES integration_flows(id),
    adapter_id UUID REFERENCES communication_adapters(id),
    message_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    content XML NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_message_xml CHECK (xml_is_well_formed_document(content::text))
);

-- System logs
CREATE TABLE system_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    level VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255),
    message TEXT,
    exception TEXT,
    thread_name VARCHAR(255),
    correlation_id VARCHAR(100),
    user_id UUID REFERENCES users(id),
    flow_id UUID REFERENCES integration_flows(id),
    adapter_id UUID REFERENCES communication_adapters(id)
);

-- System configuration
CREATE TABLE system_configuration (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Flow structure messages (link table)
CREATE TABLE flow_structure_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_structure_id UUID NOT NULL REFERENCES flow_structures(id) ON DELETE CASCADE,
    message_structure_id UUID NOT NULL REFERENCES message_structures(id),
    message_type VARCHAR(50) NOT NULL CHECK (message_type IN ('REQUEST', 'RESPONSE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (flow_structure_id, message_structure_id, message_type)
);

-- Adapter payloads
CREATE TABLE adapter_payloads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_id UUID NOT NULL REFERENCES communication_adapters(id) ON DELETE CASCADE,
    message_structure_id UUID NOT NULL REFERENCES message_structures(id),
    payload_type VARCHAR(50) NOT NULL CHECK (payload_type IN ('INPUT', 'OUTPUT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (adapter_id, message_structure_id, payload_type)
);

-- Create indexes for performance
CREATE INDEX idx_message_structures_name ON message_structures(name);
CREATE INDEX idx_flow_structures_name ON flow_structures(name);
CREATE INDEX idx_integration_flows_name ON integration_flows(name);
CREATE INDEX idx_messages_flow_id ON messages(flow_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_flow_executions_flow_id ON flow_executions(flow_id);
CREATE INDEX idx_flow_executions_status ON flow_executions(status);
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_xml_field_mappings_transformation ON xml_field_mappings(transformation_id);
CREATE INDEX idx_xml_field_mappings_order ON xml_field_mappings(transformation_id, mapping_order);

-- XML-specific indexes using PostgreSQL GIN
CREATE INDEX idx_message_structures_xml ON message_structures USING GIN ((
    xpath('//xs:element/@name', xsd_content, 
    ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])
));

CREATE INDEX idx_flow_structures_xml ON flow_structures USING GIN ((
    xpath('//wsdl:operation/@name', wsdl_content,
    ARRAY[ARRAY['wsdl', 'http://schemas.xmlsoap.org/wsdl/']])
)) WHERE wsdl_content IS NOT NULL;

-- Create update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update trigger to all relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_business_components_updated_at BEFORE UPDATE ON business_components
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_message_structures_updated_at BEFORE UPDATE ON message_structures
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flow_structures_updated_at BEFORE UPDATE ON flow_structures
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_communication_adapters_updated_at BEFORE UPDATE ON communication_adapters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_integration_flows_updated_at BEFORE UPDATE ON integration_flows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flow_transformations_updated_at BEFORE UPDATE ON flow_transformations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_xml_field_mappings_updated_at BEFORE UPDATE ON xml_field_mappings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_configuration_updated_at BEFORE UPDATE ON system_configuration
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('ADMINISTRATOR', 'Full system access'),
('DEVELOPER', 'Development access'),
('INTEGRATOR', 'Integration configuration access'),
('VIEWER', 'Read-only access');

-- Insert default system configuration
INSERT INTO system_configuration (config_key, config_value, config_type, description) VALUES
('environment.type', 'DEVELOPMENT', 'STRING', 'Environment type: DEVELOPMENT, QA, or PRODUCTION'),
('xml.namespace.aware', 'true', 'BOOLEAN', 'Whether XML processing should be namespace aware'),
('xml.validation.enabled', 'true', 'BOOLEAN', 'Whether to validate XML against schemas'),
('flow.execution.timeout', '300000', 'INTEGER', 'Flow execution timeout in milliseconds'),
('adapter.connection.timeout', '30000', 'INTEGER', 'Adapter connection timeout in milliseconds');