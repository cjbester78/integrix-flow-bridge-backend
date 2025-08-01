-- Seed Data for Integrix Flow Bridge
-- This file contains initial data for development and testing

-- Clear existing data (in reverse order of dependencies)
DELETE FROM field_mappings;
DELETE FROM flow_transformations;
DELETE FROM flow_executions;
DELETE FROM integration_flows;
DELETE FROM communication_adapters;
DELETE FROM data_structures;
DELETE FROM jar_files;
DELETE FROM certificates;
DELETE FROM system_logs;
DELETE FROM user_sessions;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM system_settings;
DELETE FROM flow_statistics;
DELETE FROM adapter_statistics;

-- Insert Roles
INSERT INTO roles (id, name, description, permissions) VALUES
('role-admin', 'administrator', 'Full system access with all administrative capabilities', 
 JSON_ARRAY('flows:create', 'flows:read', 'flows:update', 'flows:delete', 'flows:execute', 
            'adapters:create', 'adapters:read', 'adapters:update', 'adapters:delete', 'adapters:test',
            'structures:create', 'structures:read', 'structures:update', 'structures:delete',
            'users:create', 'users:read', 'users:update', 'users:delete',
            'system:admin', 'system:config', 'system:monitor')),

('role-integrator', 'integrator', 'Can create and manage integration flows and configurations',
 JSON_ARRAY('flows:create', 'flows:read', 'flows:update', 'flows:execute',
            'adapters:create', 'adapters:read', 'adapters:update', 'adapters:test',
            'structures:create', 'structures:read', 'structures:update')),

('role-viewer', 'viewer', 'Read-only access to monitoring and logs',
 JSON_ARRAY('flows:read', 'adapters:read', 'structures:read', 'system:monitor'));

-- Insert Users
-- Note: Default password for all users is 'password123' (bcrypt hashed)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role_id, status, email_verified, last_login_at) VALUES
('user-admin', 'admin', 'admin@integrixlab.com', 
 '$2a$10$YxQdJZF5htB3R0xU0LbFaOPL1dkcVw3D.XG0CKjE0AzLUaZm9na2a', 
 'System', 'Administrator', 'role-admin', 'active', TRUE, '2024-01-15 14:30:25'),

('user-integrator1', 'integrator1', 'integrator1@company.com', 
 '$2a$10$YxQdJZF5htB3R0xU0LbFaOPL1dkcVw3D.XG0CKjE0AzLUaZm9na2a', 
 'John', 'Integrator', 'role-integrator', 'active', TRUE, '2024-01-15 12:15:30'),

('user-integrator2', 'integrator2', 'integrator2@company.com', 
 '$2a$10$YxQdJZF5htB3R0xU0LbFaOPL1dkcVw3D.XG0CKjE0AzLUaZm9na2a', 
 'Sarah', 'Developer', 'role-integrator', 'active', TRUE, '2024-01-14 16:45:12'),

('user-viewer1', 'viewer1', 'viewer1@company.com', 
 '$2a$10$YxQdJZF5htB3R0xU0LbFaOPL1dkcVw3D.XG0CKjE0AzLUaZm9na2a', 
 'Jane', 'Viewer', 'role-viewer', 'active', TRUE, '2024-01-10 09:30:00'),

('user-inactive', 'inactive_user', 'inactive@company.com', 
 '$2a$10$YxQdJZF5htB3R0xU0LbFaOPL1dkcVw3D.XG0CKjE0AzLUaZm9na2a', 
 'Bob', 'Inactive', 'role-viewer', 'inactive', TRUE, '2023-12-01 10:00:00');

-- Insert Certificates
INSERT INTO certificates (id, name, type, issuer, valid_from, valid_to, status, `usage`, created_by) VALUES
('cert-001', 'Production SSL Certificate', 'SSL Certificate', 'DigiCert Inc', '2024-01-01', '2025-01-01', 'active', 'HTTPS/TLS connections for production APIs', 'user-admin'),
('cert-002', 'Development SSL Certificate', 'SSL Certificate', 'Let\'s Encrypt', '2024-01-01', '2024-12-31', 'active', 'Development environment SSL', 'user-admin'),
('cert-003', 'Client Authentication Certificate', 'Client Certificate', 'Internal CA', '2024-01-01', '2024-12-31', 'active', 'Mutual TLS authentication', 'user-integrator1'),
('cert-004', 'SOAP Service Certificate', 'Web Service Certificate', 'VeriSign', '2023-06-01', '2024-06-01', 'expiring', 'Legacy SOAP services', 'user-integrator1'),
('cert-005', 'Code Signing Certificate', 'Code Signing', 'GlobalSign', '2024-01-01', '2026-01-01', 'active', 'JAR file signing', 'user-admin');

-- Insert JAR Files
INSERT INTO jar_files (id, name, version, description, file_name, size_bytes, driver_type, upload_date, uploaded_by) VALUES
('jar-001', 'MySQL JDBC Driver', '8.0.33', 'MySQL Connector/J JDBC Driver for database connectivity', 'mysql-connector-java-8.0.33.jar', 2456789, 'Database', '2024-01-15', 'user-admin'),
('jar-002', 'PostgreSQL JDBC Driver', '42.6.0', 'PostgreSQL JDBC Driver for database operations', 'postgresql-42.6.0.jar', 1234567, 'Database', '2024-01-10', 'user-integrator1'),
('jar-003', 'Oracle JDBC Driver', '21.7.0.0', 'Oracle Database JDBC Driver', 'ojdbc11-21.7.0.0.jar', 4567890, 'Database', '2024-01-12', 'user-admin'),
('jar-004', 'SQL Server JDBC Driver', '12.2.0', 'Microsoft SQL Server JDBC Driver', 'mssql-jdbc-12.2.0.jre11.jar', 2345678, 'Database', '2024-01-11', 'user-integrator1'),
('jar-005', 'ActiveMQ Client', '5.18.3', 'ActiveMQ JMS Client for message queue operations', 'activemq-client-5.18.3.jar', 987654, 'Message Queue', '2024-01-08', 'user-integrator2'),
('jar-006', 'RabbitMQ Client', '5.16.0', 'RabbitMQ Java Client for AMQP messaging', 'amqp-client-5.16.0.jar', 876543, 'Message Queue', '2024-01-09', 'user-integrator2'),
('jar-007', 'Apache Kafka Client', '3.6.0', 'Apache Kafka client for event streaming', 'kafka-clients-3.6.0.jar', 1987654, 'Message Queue', '2024-01-07', 'user-admin'),
('jar-008', 'MongoDB Driver', '4.8.2', 'MongoDB Java Driver for NoSQL database operations', 'mongodb-driver-sync-4.8.2.jar', 1567890, 'Database', '2024-01-11', 'user-integrator1'),
('jar-009', 'Apache POI', '5.2.4', 'Apache POI for Excel file processing', 'poi-5.2.4.jar', 3456789, 'File Processing', '2024-01-13', 'user-integrator2'),
('jar-010', 'Commons CSV', '1.10.0', 'Apache Commons CSV for CSV file handling', 'commons-csv-1.10.0.jar', 234567, 'File Processing', '2024-01-14', 'user-integrator2');

-- Insert System Settings (with explicit IDs)
INSERT INTO system_settings (id, category, `key`, `value`, description) VALUES
-- Integration Settings
('setting-001', 'integration', 'default_timeout', '30000', 'Default timeout for API calls in milliseconds'),
('setting-002', 'integration', 'max_retries', '3', 'Maximum number of retries for failed operations'),
('setting-003', 'integration', 'retry_delay', '5000', 'Delay between retries in milliseconds'),
('setting-004', 'integration', 'batch_size', '100', 'Default batch size for bulk operations'),
('setting-005', 'integration', 'log_retention_days', '90', 'Number of days to retain execution logs'),

-- Security Settings
('setting-006', 'security', 'session_timeout', '3600', 'User session timeout in seconds'),
('setting-007', 'security', 'max_login_attempts', '5', 'Maximum failed login attempts before lockout'),
('setting-008', 'security', 'lockout_duration', '900', 'Account lockout duration in seconds'),
('setting-009', 'security', 'password_policy', '{"minLength":8,"requireUppercase":true,"requireLowercase":true,"requireNumbers":true,"requireSpecialChars":true,"maxAge":90}', 'Password complexity requirements'),
('setting-010', 'security', 'jwt_expiry', '86400', 'JWT token expiry time in seconds'),

-- Email Settings
('setting-011', 'email', 'smtp_host', 'smtp.gmail.com', 'SMTP server hostname'),
('setting-012', 'email', 'smtp_port', '587', 'SMTP server port'),
('setting-013', 'email', 'smtp_username', 'notifications@integrixlab.com', 'SMTP username'),
('setting-014', 'email', 'smtp_from', 'Integrix Flow Bridge <notifications@integrixlab.com>', 'Default from address'),
('setting-015', 'email', 'admin_notifications', '["admin@integrixlab.com","support@integrixlab.com"]', 'Email addresses for system notifications'),

-- Monitoring Settings
('setting-016', 'monitoring', 'health_check_interval', '300', 'Health check interval in seconds'),
('setting-017', 'monitoring', 'metrics_retention_days', '30', 'Number of days to retain metrics data'),
('setting-018', 'monitoring', 'alert_thresholds', '{"error_rate":0.05,"response_time":5000,"queue_depth":1000,"cpu_usage":80,"memory_usage":85}', 'Monitoring alert thresholds'),

-- Performance Settings
('setting-019', 'performance', 'thread_pool_size', '10', 'Default thread pool size for async operations'),
('setting-020', 'performance', 'connection_pool_size', '20', 'Database connection pool size'),
('setting-021', 'performance', 'cache_ttl', '3600', 'Default cache time-to-live in seconds'),

-- System Settings
('setting-022', 'system', 'maintenance_mode', 'false', 'Enable/disable maintenance mode'),
('setting-023', 'system', 'api_rate_limit', '1000', 'API rate limit per hour per user'),
('setting-024', 'system', 'max_file_upload_size', '104857600', 'Maximum file upload size in bytes (100MB)'),
('setting-025', 'system', 'supported_file_types', '["json","xml","csv","txt","xls","xlsx"]', 'Supported file types for upload');

-- Insert Business Components (required for adapters and structures)
INSERT INTO business_components (id, name, description, industry, contact_email, created_by) VALUES
('bc-001', 'Acme Corporation', 'Enterprise retail and e-commerce solutions', 'Retail', 'integration@acme.com', 'user-admin'),
('bc-002', 'TechStart Inc', 'Technology startup specializing in SaaS products', 'Technology', 'api@techstart.com', 'user-integrator1'),
('bc-003', 'Global Manufacturing Ltd', 'International manufacturing and supply chain', 'Manufacturing', 'systems@globalmanufacturing.com', 'user-integrator2');

-- Insert Data Structures (keeping some basic ones)
INSERT INTO data_structures (id, name, type, description, `usage`, structure, tags, created_by, business_component_id) VALUES
('struct-001', 'Customer Order', 'json', 'Standard e-commerce order structure', 'source', 
 '{"orderId":"string","customerId":"string","orderDate":"datetime","items":[{"productId":"string","quantity":"integer","price":"decimal"}],"totalAmount":"decimal","status":"string"}',
 '["ecommerce","order","customer"]', 'user-integrator1', 'bc-001'),

('struct-002', 'Invoice', 'json', 'Standard invoice format', 'target',
 '{"invoiceId":"string","orderId":"string","invoiceDate":"date","dueDate":"date","lineItems":[{"description":"string","amount":"decimal"}],"totalAmount":"decimal","status":"string"}',
 '["finance","invoice","accounting"]', 'user-integrator1', 'bc-001'),

('struct-003', 'Product Catalog', 'json', 'Product information structure', 'both',
 '{"productId":"string","name":"string","description":"string","price":"decimal","category":"string","stock":"integer","active":"boolean"}',
 '["product","catalog","inventory"]', 'user-integrator2', 'bc-002');

-- Insert Communication Adapters (a few examples)
INSERT INTO communication_adapters (id, name, type, mode, direction, description, configuration, status, is_active, created_by, business_component_id) VALUES
('adapter-001', 'REST API Endpoint', 'REST', 'SENDER', 'INBOUND', 'REST API for receiving orders',
 '{"baseUrl":"https://api.acme.com/v1","authentication":{"type":"oauth2"},"timeout":30000}',
 'active', TRUE, 'user-integrator1', 'bc-001'),

('adapter-002', 'Database Connection', 'JDBC', 'RECEIVER', 'OUTBOUND', 'MySQL database for storing processed data',
 '{"connectionString":"jdbc:mysql://localhost:3306/acme_db","poolSize":10}',
 'active', TRUE, 'user-integrator1', 'bc-001'),

('adapter-003', 'SFTP File Transfer', 'SFTP', 'BIDIRECTIONAL', 'BIDIRECTIONAL', 'Secure file transfer for batch processing',
 '{"host":"sftp.techstart.com","port":22,"directory":"/integrations"}',
 'active', TRUE, 'user-integrator2', 'bc-002');

-- Add minimal system logs for testing
INSERT INTO system_logs (id, timestamp, level, message, details, source, source_id, source_name, user_id) VALUES
('log-001', NOW(), 'info', 'System initialized successfully', '{"version":"1.0.0"}', 'system', NULL, 'System', 'user-admin'),
('log-002', NOW() - INTERVAL 1 HOUR, 'info', 'User login successful', '{"ip":"192.168.1.100"}', 'authentication', NULL, 'Auth Service', 'user-integrator1'),
('log-003', NOW() - INTERVAL 2 HOUR, 'warn', 'High memory usage detected', '{"usage":85,"threshold":80}', 'monitoring', NULL, 'Monitor Service', NULL),
('log-004', NOW() - INTERVAL 3 HOUR, 'error', 'Failed to connect to external API', '{"endpoint":"https://api.external.com","error":"Connection timeout"}', 'adapter', 'adapter-001', 'REST API Endpoint', NULL),
('log-005', NOW() - INTERVAL 4 HOUR, 'info', 'Flow execution completed', '{"flowId":"flow-001","duration":1250,"records":100}', 'flow', 'flow-001', 'Order Processing Flow', 'user-integrator1');