-- Integration Platform Database Schema (MySQL)
-- This schema supports the complete integration platform functionality
-- Based on comprehensive analysis of all entity classes

-- Create schema if not exists and switch to it
CREATE DATABASE IF NOT EXISTS integrixflowbridge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE integrixflowbridge;

-- Set SQL mode for strict compliance
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- ==========================================
-- CORE AUTHENTICATION & USER MANAGEMENT
-- ==========================================

-- Roles table for admin management
CREATE TABLE IF NOT EXISTS roles (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Users and Authentication
CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role_id CHAR(36),
    role ENUM('administrator', 'integrator', 'viewer') NOT NULL DEFAULT 'viewer',
    status ENUM('active', 'inactive', 'pending') NOT NULL DEFAULT 'active',
    permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP NULL,
    CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
);

-- User Sessions/Tokens
CREATE TABLE IF NOT EXISTS user_sessions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    refresh_token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    version INT DEFAULT 0,
    CONSTRAINT fk_user_sessions_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================
-- BUSINESS COMPONENTS & CERTIFICATES
-- ==========================================

-- Business Components table (business_components/organizations)
CREATE TABLE IF NOT EXISTS business_components (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Certificates table for admin management
CREATE TABLE IF NOT EXISTS certificates (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    format VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    content LONGBLOB NOT NULL,
    uploaded_by CHAR(36) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    valid_from DATE,
    valid_to DATE,
    status ENUM('active', 'expiring', 'expired', 'revoked') DEFAULT 'active',
    `usage` TEXT,
    fingerprint VARCHAR(128),
    serial_number VARCHAR(100),
    CONSTRAINT fk_certificates_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- JAR Files table for admin management
CREATE TABLE IF NOT EXISTS jar_files (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    size_bytes BIGINT,
    driver_type VARCHAR(100),
    upload_date DATE DEFAULT (CURDATE()),
    checksum VARCHAR(64),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    uploaded_by CHAR(36),
    CONSTRAINT fk_jar_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- SYSTEM CONFIGURATION & LOGGING
-- ==========================================

-- System Settings table for configurable parameters
CREATE TABLE IF NOT EXISTS system_settings (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    data_type VARCHAR(20) DEFAULT 'STRING',
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_readonly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- System Logs table for audit trail and monitoring
CREATE TABLE IF NOT EXISTS system_logs (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    level VARCHAR(20) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    details TEXT,
    source VARCHAR(255),
    source_id VARCHAR(255),
    source_name VARCHAR(255),
    component VARCHAR(255),
    component_id VARCHAR(255),
    domain_type VARCHAR(255),
    domain_reference_id CHAR(36),
    user_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- DATA STRUCTURES & FUNCTIONS
-- ==========================================

-- Data Structures table for schema definitions
CREATE TABLE IF NOT EXISTS data_structures (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    type ENUM('json', 'xsd', 'wsdl', 'custom') NOT NULL,
    description TEXT,
    `usage` ENUM('source', 'target', 'both') NOT NULL,
    structure JSON NOT NULL,
    namespace_uri VARCHAR(500),
    namespace_prefix VARCHAR(50),
    schema_location VARCHAR(500),
    tags JSON,
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    CONSTRAINT fk_data_structures_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_data_structures_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL
);

-- Reusable Java Functions table
CREATE TABLE IF NOT EXISTS reusable_java_functions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) UNIQUE NOT NULL,
    version VARCHAR(50),
    function_body TEXT NOT NULL,
    input_types JSON,
    output_type VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    CONSTRAINT fk_reusable_functions_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- INTEGRATION FLOWS & ADAPTERS
-- ==========================================

-- Communication Adapters table
CREATE TABLE IF NOT EXISTS communication_adapters (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    type ENUM('HTTP', 'FTP', 'SFTP', 'FILE', 'JDBC', 'JMS', 'MAIL', 'REST', 'SOAP', 'ODATA', 'RFC', 'IDOC') NOT NULL,
    mode ENUM('SENDER', 'RECEIVER') NOT NULL,
    configuration JSON,
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    CONSTRAINT fk_communication_adapters_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    CONSTRAINT fk_communication_adapters_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Integration Flows table
CREATE TABLE IF NOT EXISTS integration_flows (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_adapter_id CHAR(36) NOT NULL,
    target_adapter_id CHAR(36) NOT NULL,
    source_structure_id CHAR(36),
    target_structure_id CHAR(36),
    status ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'ERROR', 'DEVELOPED_INACTIVE', 'DEPLOYED_ACTIVE') DEFAULT 'DRAFT',
    configuration JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    last_execution_at TIMESTAMP NULL,
    execution_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    business_component_id CHAR(36),
    CONSTRAINT fk_integration_flows_source_adapter FOREIGN KEY (source_adapter_id) REFERENCES communication_adapters(id) ON DELETE RESTRICT,
    CONSTRAINT fk_integration_flows_target_adapter FOREIGN KEY (target_adapter_id) REFERENCES communication_adapters(id) ON DELETE RESTRICT,
    CONSTRAINT fk_integration_flows_source_structure FOREIGN KEY (source_structure_id) REFERENCES data_structures(id) ON DELETE SET NULL,
    CONSTRAINT fk_integration_flows_target_structure FOREIGN KEY (target_structure_id) REFERENCES data_structures(id) ON DELETE SET NULL,
    CONSTRAINT fk_integration_flows_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_integration_flows_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL
);

-- Flow Transformations table
CREATE TABLE IF NOT EXISTS flow_transformations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    flow_id CHAR(36) NOT NULL,
    type ENUM('FIELD_MAPPING', 'CUSTOM_FUNCTION', 'FILTER', 'ENRICHMENT', 'VALIDATION') NOT NULL,
    configuration JSON NOT NULL,
    execution_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flow_transformations_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE
);

-- Field Mappings table
CREATE TABLE IF NOT EXISTS field_mappings (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    transformation_id CHAR(36) NOT NULL,
    source_fields JSON NOT NULL,
    target_field VARCHAR(500) NOT NULL,
    java_function TEXT,
    mapping_rule TEXT,
    input_types JSON,
    output_type VARCHAR(100),
    description TEXT,
    version VARCHAR(50),
    function_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_field_mappings_transformation FOREIGN KEY (transformation_id) REFERENCES flow_transformations(id) ON DELETE CASCADE
);

-- ==========================================
-- EXECUTION & MONITORING TABLES
-- ==========================================

-- Flow Executions table for tracking execution history
CREATE TABLE IF NOT EXISTS flow_executions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    flow_id CHAR(36) NOT NULL,
    execution_id VARCHAR(100) UNIQUE NOT NULL,
    status ENUM('RUNNING', 'SUCCESS', 'ERROR', 'CANCELLED') NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration_ms BIGINT,
    input_message TEXT,
    output_message TEXT,
    error_message TEXT,
    error_details TEXT,
    processed_records INT DEFAULT 0,
    failed_records INT DEFAULT 0,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flow_executions_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE,
    CONSTRAINT fk_flow_executions_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL
);

-- Flow Statistics table for performance metrics
CREATE TABLE IF NOT EXISTS flow_statistics (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    flow_id CHAR(36) NOT NULL,
    date_period DATE NOT NULL,
    total_executions INT DEFAULT 0,
    successful_executions INT DEFAULT 0,
    failed_executions INT DEFAULT 0,
    average_duration_ms BIGINT DEFAULT 0,
    max_duration_ms BIGINT DEFAULT 0,
    min_duration_ms BIGINT DEFAULT 0,
    total_records_processed BIGINT DEFAULT 0,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flow_statistics_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE,
    CONSTRAINT fk_flow_statistics_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    UNIQUE KEY uk_flow_statistics_flow_date (flow_id, date_period)
);

-- Messages table for message tracking
CREATE TABLE IF NOT EXISTS messages (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    flow_id CHAR(36),
    execution_id CHAR(36),
    message_id VARCHAR(255),
    correlation_id VARCHAR(255),
    message_type ENUM('REQUEST', 'RESPONSE', 'ERROR', 'ACKNOWLEDGMENT') NOT NULL,
    direction ENUM('INBOUND', 'OUTBOUND') NOT NULL,
    status ENUM('PROCESSING', 'PROCESSED', 'ERROR', 'FAILED') NOT NULL,
    content TEXT,
    headers JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    error_details TEXT,
    retry_count INT DEFAULT 0,
    business_component_id CHAR(36),
    CONSTRAINT fk_messages_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE SET NULL,
    CONSTRAINT fk_messages_execution FOREIGN KEY (execution_id) REFERENCES flow_executions(id) ON DELETE SET NULL,
    CONSTRAINT fk_messages_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL
);

-- ==========================================
-- WEBSERVICE & CHANNEL MANAGEMENT
-- ==========================================

-- Channels table for communication channels
CREATE TABLE IF NOT EXISTS channels (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    configuration JSON,
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    CONSTRAINT fk_channels_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    CONSTRAINT fk_channels_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- WebService Files table for uploaded WSDL/XSD files
CREATE TABLE IF NOT EXISTS webservice_files (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    type ENUM('WSDL', 'XSD', 'JSON_SCHEMA') NOT NULL,
    content TEXT NOT NULL,
    namespace_uri VARCHAR(500),
    target_namespace VARCHAR(500),
    file_path VARCHAR(500),
    checksum VARCHAR(64),
    is_active BOOLEAN DEFAULT TRUE,
    business_component_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    uploaded_by CHAR(36),
    CONSTRAINT fk_webservice_files_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    CONSTRAINT fk_webservice_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ==========================================

-- User and authentication indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Business components indexes
CREATE INDEX idx_business_components_name ON business_components(name);

-- System logs indexes
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_component ON system_logs(component);

-- Communication adapter indexes
CREATE INDEX idx_communication_adapters_type ON communication_adapters(type);
CREATE INDEX idx_communication_adapters_mode ON communication_adapters(mode);
CREATE INDEX idx_communication_adapters_business_component ON communication_adapters(business_component_id);
CREATE INDEX idx_communication_adapters_active ON communication_adapters(is_active);

-- Integration flow indexes
CREATE INDEX idx_integration_flows_status ON integration_flows(status);
CREATE INDEX idx_integration_flows_source_adapter ON integration_flows(source_adapter_id);
CREATE INDEX idx_integration_flows_target_adapter ON integration_flows(target_adapter_id);
CREATE INDEX idx_integration_flows_business_component ON integration_flows(business_component_id);
CREATE INDEX idx_integration_flows_created_by ON integration_flows(created_by);

-- Flow execution indexes
CREATE INDEX idx_flow_executions_flow_id ON flow_executions(flow_id);
CREATE INDEX idx_flow_executions_status ON flow_executions(status);
CREATE INDEX idx_flow_executions_start_time ON flow_executions(start_time);
CREATE INDEX idx_flow_executions_business_component ON flow_executions(business_component_id);

-- Message tracking indexes
CREATE INDEX idx_messages_flow_id ON messages(flow_id);
CREATE INDEX idx_messages_execution_id ON messages(execution_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_timestamp ON messages(timestamp);
CREATE INDEX idx_messages_correlation_id ON messages(correlation_id);

-- Data structure indexes
CREATE INDEX idx_data_structures_type ON data_structures(type);
CREATE INDEX idx_data_structures_usage ON data_structures(`usage`);
CREATE INDEX idx_data_structures_business_component ON data_structures(business_component_id);
CREATE INDEX idx_data_structures_active ON data_structures(is_active);

-- Certificate indexes
CREATE INDEX idx_certificates_status ON certificates(status);
CREATE INDEX idx_certificates_valid_to ON certificates(valid_to);
CREATE INDEX idx_certificates_uploaded_by ON certificates(uploaded_by);

-- System settings indexes
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);

-- Flow transformation indexes
CREATE INDEX idx_flow_transformations_flow_id ON flow_transformations(flow_id);
CREATE INDEX idx_flow_transformations_type ON flow_transformations(type);

-- Field mapping indexes
CREATE INDEX idx_field_mappings_transformation_id ON field_mappings(transformation_id);

-- Statistics indexes
CREATE INDEX idx_flow_statistics_flow_date ON flow_statistics(flow_id, date_period);
CREATE INDEX idx_flow_statistics_business_component ON flow_statistics(business_component_id);

-- Channel indexes
CREATE INDEX idx_channels_business_component ON channels(business_component_id);
CREATE INDEX idx_channels_type ON channels(type);
CREATE INDEX idx_channels_active ON channels(is_active);

-- WebService file indexes
CREATE INDEX idx_webservice_files_type ON webservice_files(type);
CREATE INDEX idx_webservice_files_business_component ON webservice_files(business_component_id);
CREATE INDEX idx_webservice_files_active ON webservice_files(is_active);