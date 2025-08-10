-- ALTER TABLE statements to modify existing schema
-- Run these statements if you already have the tables created

-- Add customers table if it doesn't exist
CREATE TABLE IF NOT EXISTS customers (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add customer_id to data_structures table
ALTER TABLE data_structures 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_data_structures_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to communication_adapters table
ALTER TABLE communication_adapters 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_adapters_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to integration_flows table
ALTER TABLE integration_flows 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_flows_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to flow_executions table
ALTER TABLE flow_executions 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_flow_executions_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to certificates table
ALTER TABLE certificates 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_certificates_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to user_sessions table
ALTER TABLE user_sessions 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_user_sessions_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Add customer_id to system_logs table
ALTER TABLE system_logs 
ADD COLUMN customer_id CHAR(36),
ADD CONSTRAINT fk_system_logs_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- Create channels table if it doesn't exist
CREATE TABLE IF NOT EXISTS channels (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type ENUM('inbound', 'outbound', 'bidirectional') NOT NULL,
    status ENUM('active', 'inactive', 'error', 'stopped') NOT NULL DEFAULT 'inactive',
    configuration JSON NOT NULL,
    customer_id CHAR(36),
    flow_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36),
    last_started_at TIMESTAMP NULL,
    last_stopped_at TIMESTAMP NULL,
    message_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    CONSTRAINT fk_channels_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_channels_flow
        FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE SET NULL,
    CONSTRAINT fk_channels_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create messages table if it doesn't exist
CREATE TABLE IF NOT EXISTS messages (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    channel_id CHAR(36) NOT NULL,
    flow_id CHAR(36),
    customer_id CHAR(36),
    direction ENUM('inbound', 'outbound') NOT NULL,
    status ENUM('processing', 'success', 'error', 'warning') NOT NULL,
    message_type VARCHAR(100),
    content_type VARCHAR(100),
    payload JSON,
    headers JSON,
    metadata JSON,
    error_message TEXT,
    error_details JSON,
    processing_time_ms INTEGER,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    retries INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    CONSTRAINT fk_messages_channel
        FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_flow
        FOREIGN KEY (flow_id) REFERENCES integration_flows(id) ON DELETE SET NULL,
    CONSTRAINT fk_messages_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Create webservice_files table if it doesn't exist
CREATE TABLE IF NOT EXISTS webservice_files (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_type ENUM('wsdl', 'xsd', 'other') NOT NULL,
    content TEXT,
    customer_id CHAR(36),
    size_bytes BIGINT,
    upload_date DATE DEFAULT (CURDATE()),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    uploaded_by CHAR(36),
    CONSTRAINT fk_webservice_files_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_webservice_files_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for better performance (drop if exists first, then create)
DROP INDEX IF EXISTS idx_data_structures_customer_id ON data_structures;
CREATE INDEX idx_data_structures_customer_id ON data_structures(customer_id);

DROP INDEX IF EXISTS idx_communication_adapters_customer_id ON communication_adapters;
CREATE INDEX idx_communication_adapters_customer_id ON communication_adapters(customer_id);

DROP INDEX IF EXISTS idx_integration_flows_customer_id ON integration_flows;
CREATE INDEX idx_integration_flows_customer_id ON integration_flows(customer_id);

DROP INDEX IF EXISTS idx_flow_executions_customer_id ON flow_executions;
CREATE INDEX idx_flow_executions_customer_id ON flow_executions(customer_id);

DROP INDEX IF EXISTS idx_channels_customer_id ON channels;
CREATE INDEX idx_channels_customer_id ON channels(customer_id);

DROP INDEX IF EXISTS idx_messages_customer_id ON messages;
CREATE INDEX idx_messages_customer_id ON messages(customer_id);

DROP INDEX IF EXISTS idx_webservice_files_customer_id ON webservice_files;
CREATE INDEX idx_webservice_files_customer_id ON webservice_files(customer_id);

DROP INDEX IF EXISTS idx_certificates_customer_id ON certificates;
CREATE INDEX idx_certificates_customer_id ON certificates(customer_id);

DROP INDEX IF EXISTS idx_user_sessions_customer_id ON user_sessions;
CREATE INDEX idx_user_sessions_customer_id ON user_sessions(customer_id);

DROP INDEX IF EXISTS idx_system_logs_customer_id ON system_logs;
CREATE INDEX idx_system_logs_customer_id ON system_logs(customer_id);