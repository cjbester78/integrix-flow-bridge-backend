-- ============================================
-- Integrix Flow Bridge Database Schema
-- MySQL 8.0+ Compatible
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS integrixflowbridge 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE integrixflowbridge;

-- Set SQL mode for strict compliance
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================
-- SECTION 1: CORE USER & AUTHENTICATION
-- ============================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSON NOT NULL COMMENT 'Array of permission strings',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role_id VARCHAR(36),
    status ENUM('active', 'inactive', 'pending', 'locked') NOT NULL DEFAULT 'active',
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role_id (role_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    refresh_token VARCHAR(500) UNIQUE NOT NULL,
    access_token_jti VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_info JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sessions_user_id (user_id),
    INDEX idx_sessions_expires_at (expires_at),
    INDEX idx_sessions_refresh_token (refresh_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 2: BUSINESS ENTITIES
-- ============================================

-- Business Components (Organizations/Customers)
CREATE TABLE IF NOT EXISTS business_components (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    industry VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address JSON,
    metadata JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_business_components_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_business_components_name (name),
    INDEX idx_business_components_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 3: SECURITY & CERTIFICATES
-- ============================================

-- Certificates
CREATE TABLE IF NOT EXISTS certificates (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'SSL Certificate, Client Certificate, Code Signing, etc',
    issuer VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    serial_number VARCHAR(100),
    thumbprint VARCHAR(128),
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    status ENUM('active', 'expiring', 'expired', 'revoked') DEFAULT 'active',
    `usage` TEXT COMMENT 'Description of certificate usage',
    content LONGBLOB COMMENT 'Certificate file content',
    private_key LONGBLOB COMMENT 'Encrypted private key if applicable',
    password_hint VARCHAR(255),
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_certificates_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_certificates_status (status),
    INDEX idx_certificates_valid_to (valid_to),
    INDEX idx_certificates_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- JAR Files
CREATE TABLE IF NOT EXISTS jar_files (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    size_bytes BIGINT,
    checksum VARCHAR(64),
    driver_type VARCHAR(100) COMMENT 'Database, Message Queue, File Processing, etc',
    vendor VARCHAR(100),
    license_info TEXT,
    dependencies JSON,
    is_active BOOLEAN DEFAULT TRUE,
    upload_date DATE DEFAULT (CURRENT_DATE),
    uploaded_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_jar_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_jar_files_driver_type (driver_type),
    INDEX idx_jar_files_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 4: SYSTEM CONFIGURATION
-- ============================================

-- System Settings
CREATE TABLE IF NOT EXISTS system_settings (
    id VARCHAR(36) PRIMARY KEY,
    category VARCHAR(50) NOT NULL COMMENT 'integration, security, email, monitoring, etc',
    `key` VARCHAR(100) NOT NULL,
    `value` TEXT NOT NULL,
    description VARCHAR(500),
    data_type ENUM('string', 'number', 'boolean', 'json', 'encrypted') DEFAULT 'string',
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_editable BOOLEAN DEFAULT TRUE,
    validation_rules JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    
    CONSTRAINT fk_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_settings_category_key (category, `key`),
    INDEX idx_settings_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 5: DATA STRUCTURES & TRANSFORMATIONS
-- ============================================

-- Data Structures
CREATE TABLE IF NOT EXISTS data_structures (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type ENUM('json', 'xml', 'xsd', 'wsdl', 'edmx', 'custom') NOT NULL,
    description TEXT,
    `usage` ENUM('source', 'target', 'both') NOT NULL,
    structure JSON NOT NULL COMMENT 'The actual structure definition',
    namespace JSON COMMENT 'Namespace information for XML-based structures',
    tags JSON COMMENT 'Array of tags for categorization',
    version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    business_component_id VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_structures_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE CASCADE,
    CONSTRAINT fk_structures_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_structures_type (type),
    INDEX idx_structures_usage (`usage`),
    INDEX idx_structures_business_component (business_component_id),
    INDEX idx_structures_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Communication Adapters
CREATE TABLE IF NOT EXISTS communication_adapters (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type ENUM('HTTP', 'HTTPS', 'REST', 'SOAP', 'FTP', 'SFTP', 'FILE', 'JDBC', 'JMS', 'MAIL', 'ODATA', 'RFC', 'IDOC', 'KAFKA', 'RABBITMQ', 'ACTIVEMQ', 'SMS', 'LDAP') NOT NULL,
    mode ENUM('SENDER', 'RECEIVER', 'BIDIRECTIONAL') NOT NULL,
    direction ENUM('INBOUND', 'OUTBOUND', 'BIDIRECTIONAL') NOT NULL,
    description TEXT,
    configuration JSON NOT NULL COMMENT 'Adapter-specific configuration',
    status ENUM('active', 'inactive', 'error', 'testing') DEFAULT 'active',
    is_active BOOLEAN DEFAULT TRUE,
    last_test_date TIMESTAMP NULL,
    last_test_result JSON,
    business_component_id VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_adapters_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE CASCADE,
    CONSTRAINT fk_adapters_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_adapters_type (type),
    INDEX idx_adapters_mode (mode),
    INDEX idx_adapters_direction (direction),
    INDEX idx_adapters_business_component (business_component_id),
    INDEX idx_adapters_status (status),
    INDEX idx_adapters_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 6: INTEGRATION FLOWS
-- ============================================

-- Integration Flows
CREATE TABLE IF NOT EXISTS integration_flows (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    flow_type ENUM('DIRECT_MAPPING', 'ORCHESTRATION') DEFAULT 'DIRECT_MAPPING',
    source_adapter_id VARCHAR(36) NOT NULL,
    target_adapter_id VARCHAR(36) NOT NULL,
    source_structure_id VARCHAR(36),
    target_structure_id VARCHAR(36),
    status ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'ERROR', 'TESTING', 'DEPLOYED') DEFAULT 'DRAFT',
    configuration JSON COMMENT 'Flow-specific settings',
    schedule JSON COMMENT 'Scheduling configuration',
    error_handling JSON COMMENT 'Error handling rules',
    is_active BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    deployed_version INT,
    business_component_id VARCHAR(36),
    created_by VARCHAR(36),
    last_modified_by VARCHAR(36),
    deployed_by VARCHAR(36),
    deployed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_flows_source_adapter FOREIGN KEY (source_adapter_id) REFERENCES communication_adapters(id) ON DELETE RESTRICT,
    CONSTRAINT fk_flows_target_adapter FOREIGN KEY (target_adapter_id) REFERENCES communication_adapters(id) ON DELETE RESTRICT,
    CONSTRAINT fk_flows_source_structure FOREIGN KEY (source_structure_id) REFERENCES data_structures(id) ON DELETE SET NULL,
    CONSTRAINT fk_flows_target_structure FOREIGN KEY (target_structure_id) REFERENCES data_structures(id) ON DELETE SET NULL,
    CONSTRAINT fk_flows_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE CASCADE,
    CONSTRAINT fk_flows_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_flows_modified_by FOREIGN KEY (last_modified_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_flows_deployed_by FOREIGN KEY (deployed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_flows_status (status),
    INDEX idx_flows_type (flow_type),
    INDEX idx_flows_business_component (business_component_id),
    INDEX idx_flows_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Flow Transformations
CREATE TABLE IF NOT EXISTS flow_transformations (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    type ENUM('FIELD_MAPPING', 'CUSTOM_FUNCTION', 'FILTER', 'ENRICHMENT', 'VALIDATION', 'AGGREGATION', 'SPLIT', 'MERGE') NOT NULL,
    name VARCHAR(255),
    description TEXT,
    configuration JSON NOT NULL,
    execution_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transformations_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE,
    INDEX idx_transformations_flow (flow_id),
    INDEX idx_transformations_type (type),
    INDEX idx_transformations_order (flow_id, execution_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Field Mappings
CREATE TABLE IF NOT EXISTS field_mappings (
    id VARCHAR(36) PRIMARY KEY,
    transformation_id VARCHAR(36) NOT NULL,
    source_fields JSON NOT NULL COMMENT 'Array of source field paths',
    target_field VARCHAR(500) NOT NULL,
    mapping_type ENUM('DIRECT', 'FUNCTION', 'CONSTANT', 'CONDITIONAL') DEFAULT 'DIRECT',
    transformation_function TEXT COMMENT 'JavaScript or Java function',
    function_type ENUM('JAVASCRIPT', 'JAVA', 'BUILTIN') DEFAULT 'JAVASCRIPT',
    constant_value TEXT,
    conditions JSON,
    data_type_conversion JSON,
    default_value TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_mappings_transformation FOREIGN KEY (transformation_id) REFERENCES flow_transformations(id) ON DELETE CASCADE,
    INDEX idx_mappings_transformation (transformation_id),
    INDEX idx_mappings_target_field (target_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 7: EXECUTION & MONITORING
-- ============================================

-- Flow Executions
CREATE TABLE IF NOT EXISTS flow_executions (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    execution_number BIGINT,
    status ENUM('RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'TIMEOUT', 'PARTIAL') NOT NULL,
    trigger_type ENUM('MANUAL', 'SCHEDULED', 'WEBHOOK', 'EVENT', 'API') DEFAULT 'MANUAL',
    triggered_by VARCHAR(36),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    execution_time_ms BIGINT,
    processed_records INT DEFAULT 0,
    failed_records INT DEFAULT 0,
    skipped_records INT DEFAULT 0,
    input_message_id VARCHAR(36),
    output_message_id VARCHAR(36),
    error_message TEXT,
    error_stack_trace TEXT,
    retry_count INT DEFAULT 0,
    parent_execution_id VARCHAR(36),
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_executions_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE,
    CONSTRAINT fk_executions_triggered_by FOREIGN KEY (triggered_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_executions_parent FOREIGN KEY (parent_execution_id) REFERENCES flow_executions(id) ON DELETE CASCADE,
    CONSTRAINT fk_executions_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    INDEX idx_executions_flow (flow_id),
    INDEX idx_executions_status (status),
    INDEX idx_executions_started_at (started_at),
    INDEX idx_executions_business_component (business_component_id),
    UNIQUE KEY uk_executions_flow_number (flow_id, execution_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36),
    execution_id VARCHAR(36),
    adapter_id VARCHAR(36),
    message_type ENUM('REQUEST', 'RESPONSE', 'ERROR', 'ACKNOWLEDGMENT', 'NOTIFICATION') NOT NULL,
    direction ENUM('INBOUND', 'OUTBOUND') NOT NULL,
    status ENUM('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED', 'RETRY', 'ARCHIVED') NOT NULL,
    correlation_id VARCHAR(255),
    parent_message_id VARCHAR(36),
    content_type VARCHAR(100),
    content LONGTEXT,
    content_size BIGINT,
    headers JSON,
    metadata JSON,
    error_details JSON,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_messages_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE SET NULL,
    CONSTRAINT fk_messages_execution FOREIGN KEY (execution_id) REFERENCES flow_executions(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_adapter FOREIGN KEY (adapter_id) REFERENCES communication_adapters(id) ON DELETE SET NULL,
    CONSTRAINT fk_messages_parent FOREIGN KEY (parent_message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    INDEX idx_messages_flow (flow_id),
    INDEX idx_messages_execution (execution_id),
    INDEX idx_messages_status (status),
    INDEX idx_messages_correlation (correlation_id),
    INDEX idx_messages_received_at (received_at),
    INDEX idx_messages_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System Logs
CREATE TABLE IF NOT EXISTS system_logs (
    id VARCHAR(36) PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    level ENUM('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL,
    logger VARCHAR(255),
    message VARCHAR(1000) NOT NULL,
    details JSON,
    exception_class VARCHAR(255),
    exception_message TEXT,
    stack_trace TEXT,
    source VARCHAR(100) COMMENT 'system, adapter, flow, channel, etc',
    source_id VARCHAR(36),
    source_name VARCHAR(255),
    user_id VARCHAR(36),
    correlation_id VARCHAR(255),
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_logs_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    INDEX idx_logs_timestamp (timestamp),
    INDEX idx_logs_level (level),
    INDEX idx_logs_source (source, source_id),
    INDEX idx_logs_correlation (correlation_id),
    INDEX idx_logs_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 8: STATISTICS & ANALYTICS
-- ============================================

-- Flow Statistics
CREATE TABLE IF NOT EXISTS flow_statistics (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    hour INT DEFAULT NULL COMMENT 'NULL for daily stats, 0-23 for hourly',
    total_executions INT DEFAULT 0,
    successful_executions INT DEFAULT 0,
    failed_executions INT DEFAULT 0,
    cancelled_executions INT DEFAULT 0,
    total_execution_time_ms BIGINT DEFAULT 0,
    avg_execution_time_ms BIGINT DEFAULT 0,
    min_execution_time_ms BIGINT DEFAULT 0,
    max_execution_time_ms BIGINT DEFAULT 0,
    total_records_processed BIGINT DEFAULT 0,
    total_records_failed BIGINT DEFAULT 0,
    total_data_volume_mb DECIMAL(10,2) DEFAULT 0,
    error_rate DECIMAL(5,2) DEFAULT 0,
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_flow_stats_flow FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE CASCADE,
    CONSTRAINT fk_flow_stats_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    UNIQUE KEY uk_flow_stats_date_hour (flow_id, date, hour),
    INDEX idx_flow_stats_date (date),
    INDEX idx_flow_stats_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Adapter Statistics
CREATE TABLE IF NOT EXISTS adapter_statistics (
    id VARCHAR(36) PRIMARY KEY,
    adapter_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    hour INT DEFAULT NULL COMMENT 'NULL for daily stats, 0-23 for hourly',
    total_messages INT DEFAULT 0,
    successful_messages INT DEFAULT 0,
    failed_messages INT DEFAULT 0,
    total_response_time_ms BIGINT DEFAULT 0,
    avg_response_time_ms BIGINT DEFAULT 0,
    min_response_time_ms BIGINT DEFAULT 0,
    max_response_time_ms BIGINT DEFAULT 0,
    total_data_volume_mb DECIMAL(10,2) DEFAULT 0,
    error_rate DECIMAL(5,2) DEFAULT 0,
    availability_percentage DECIMAL(5,2) DEFAULT 100,
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_adapter_stats_adapter FOREIGN KEY (adapter_id) REFERENCES communication_adapters(id) ON DELETE CASCADE,
    CONSTRAINT fk_adapter_stats_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    UNIQUE KEY uk_adapter_stats_date_hour (adapter_id, date, hour),
    INDEX idx_adapter_stats_date (date),
    INDEX idx_adapter_stats_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SECTION 9: ADDITIONAL FEATURES
-- ============================================

-- Channels (for grouping adapters)
CREATE TABLE IF NOT EXISTS channels (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    configuration JSON,
    status ENUM('active', 'inactive', 'maintenance') DEFAULT 'active',
    business_component_id VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_channels_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE CASCADE,
    CONSTRAINT fk_channels_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_channels_type (type),
    INDEX idx_channels_status (status),
    INDEX idx_channels_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit Trail
CREATE TABLE IF NOT EXISTS audit_trail (
    id VARCHAR(36) PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'DEPLOY', 'ACTIVATE', 'DEACTIVATE', 'EXECUTE', 'LOGIN', 'LOGOUT') NOT NULL,
    changes JSON COMMENT 'Before and after values for updates',
    user_id VARCHAR(36),
    user_ip VARCHAR(45),
    user_agent TEXT,
    business_component_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_business_component FOREIGN KEY (business_component_id) REFERENCES business_components(id) ON DELETE SET NULL,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_action (action),
    INDEX idx_audit_business_component (business_component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scheduled Jobs
CREATE TABLE IF NOT EXISTS scheduled_jobs (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    job_type ENUM('FLOW_EXECUTION', 'CLEANUP', 'REPORT', 'MAINTENANCE') NOT NULL,
    target_id VARCHAR(36) COMMENT 'Flow ID or other target',
    schedule_expression VARCHAR(255) NOT NULL COMMENT 'Cron expression or interval',
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN DEFAULT TRUE,
    last_run_at TIMESTAMP NULL,
    last_run_status ENUM('SUCCESS', 'FAILED', 'SKIPPED'),
    next_run_at TIMESTAMP NULL,
    configuration JSON,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_jobs_type (job_type),
    INDEX idx_jobs_active (is_active),
    INDEX idx_jobs_next_run (next_run_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- END OF SCHEMA
-- ============================================