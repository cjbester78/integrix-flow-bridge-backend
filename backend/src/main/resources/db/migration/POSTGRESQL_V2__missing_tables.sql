-- PostgreSQL Migration V2: Add missing tables
-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User Sessions table (Ignore data)
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    version INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(refresh_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires ON user_sessions(expires_at);

-- User Management Errors table (Ignore data)
CREATE TABLE IF NOT EXISTS user_management_errors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    error_type VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(100),
    request_data TEXT,
    stack_trace TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_mgmt_errors_type (error_type),
    INDEX idx_user_mgmt_errors_user (user_id),
    INDEX idx_user_mgmt_errors_occurred (occurred_at)
);

-- Transformation Custom Functions table (Migrate data)
CREATE TABLE IF NOT EXISTS transformation_custom_functions (
    function_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    category VARCHAR(50),
    language VARCHAR(20) NOT NULL CHECK (language IN ('JAVA', 'JAVASCRIPT', 'GROOVY', 'PYTHON')),
    function_signature VARCHAR(500) NOT NULL,
    parameters JSON,
    function_body TEXT NOT NULL,
    is_safe BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_built_in BOOLEAN NOT NULL DEFAULT FALSE,
    performance_class VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (performance_class IN ('FAST', 'NORMAL', 'SLOW')),
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_custom_func_name (name),
    INDEX idx_custom_func_category (category),
    INDEX idx_custom_func_language (language)
);

-- Function Dependencies table
CREATE TABLE IF NOT EXISTS function_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    function_id UUID NOT NULL REFERENCES transformation_custom_functions(function_id) ON DELETE CASCADE,
    dependency VARCHAR(255) NOT NULL,
    UNIQUE(function_id, dependency)
);

-- Function Test Cases table
CREATE TABLE IF NOT EXISTS function_test_cases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    function_id UUID NOT NULL REFERENCES transformation_custom_functions(function_id) ON DELETE CASCADE,
    test_name VARCHAR(100),
    input_data TEXT,
    expected_output TEXT,
    test_description VARCHAR(500),
    INDEX idx_test_cases_function (function_id)
);

-- System Settings table (Migrate data)
CREATE TABLE IF NOT EXISTS system_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    data_type VARCHAR(20) DEFAULT 'STRING',
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_readonly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_system_settings_key (setting_key),
    INDEX idx_system_settings_category (category)
);

-- JAR Files table (Migrate data)
CREATE TABLE IF NOT EXISTS jar_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50),
    file_size BIGINT,
    checksum VARCHAR(64),
    file_content BYTEA NOT NULL,
    adapter_types TEXT[], -- Array of adapter types this JAR supports
    uploaded_by VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSON,
    INDEX idx_jar_files_name (file_name),
    INDEX idx_jar_files_active (is_active)
);

-- Certificates table (Migrate data)
CREATE TABLE IF NOT EXISTS certificates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    format VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    password VARCHAR(500), -- Should be encrypted
    uploaded_by VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content BYTEA NOT NULL,
    INDEX idx_certificates_name (name)
);

-- Audit Trail table (Migrate data)
CREATE TABLE IF NOT EXISTS audit_trail (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    action VARCHAR(10) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'DEPLOY', 'ACTIVATE', 'DEACTIVATE', 'EXECUTE', 'LOGIN', 'LOGOUT')),
    changes JSON,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    user_ip VARCHAR(45),
    user_agent TEXT,
    business_component_id UUID REFERENCES business_components(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_action (action),
    INDEX idx_audit_business_component (business_component_id)
);

-- Audit Logs table (if different from audit_trail)
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_type VARCHAR(50) NOT NULL,
    log_level VARCHAR(10) NOT NULL CHECK (log_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL')),
    message TEXT NOT NULL,
    details JSON,
    source VARCHAR(100),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    correlation_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_logs_type (log_type),
    INDEX idx_audit_logs_level (log_level),
    INDEX idx_audit_logs_created (created_at),
    INDEX idx_audit_logs_correlation (correlation_id)
);

-- Add any missing columns to users table if needed
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS login_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;