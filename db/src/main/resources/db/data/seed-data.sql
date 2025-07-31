-- Seed Data for Integration Platform (MySQL)
-- This file contains sample data for development and testing

-- Insert default users with simplified structure
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, status, permissions, email_verified, last_login_at) VALUES
-- Administrator user (password: admin123)
('550e8400-e29b-41d4-a716-446655440000', 'admin', 'admin@integrixlab.com', '$2b$10$rKjWJL8sZZ8fCLwn8yJLruN1.5HJLfMvKxWHNM3YzNHN1EpZUxzKu', 'System', 'Administrator', 'admin', 'active', 
 JSON_ARRAY('flows:create', 'flows:read', 'flows:update', 'flows:delete', 'flows:execute', 'adapters:create', 'adapters:read', 'adapters:update', 'adapters:delete', 'adapters:test', 'structures:create', 'structures:read', 'structures:update', 'structures:delete', 'users:create', 'users:read', 'users:update', 'users:delete', 'system:admin'), TRUE, '2024-01-15 14:30:25'),

-- Integrator user (password: integrator123)
('550e8400-e29b-41d4-a716-446655440001', 'integrator1', 'integrator1@company.com', '$2b$10$rKjWJL8sZZ8fCLwn8yJLruN1.5HJLfMvKxWHNM3YzNHN1EpZUxzKu', 'John', 'Integrator', 'integrator', 'active',
 JSON_ARRAY('flows:create', 'flows:read', 'flows:update', 'flows:execute', 'adapters:create', 'adapters:read', 'adapters:update', 'adapters:test', 'structures:create', 'structures:read', 'structures:update'), TRUE, '2024-01-15 12:15:30'),

-- Viewer user (password: viewer123)
('550e8400-e29b-41d4-a716-446655440002', 'viewer1', 'viewer1@company.com', '$2b$10$rKjWJL8sZZ8fCLwn8yJLruN1.5HJLfMvKxWHNM3YzNHN1EpZUxzKu', 'Jane', 'Viewer', 'viewer', 'inactive',
 JSON_ARRAY('flows:read', 'adapters:read', 'structures:read'), TRUE, '2024-01-10 16:45:12');

-- Insert certificates
INSERT INTO certificates (id, name, type, issuer, valid_from, valid_to, status, `usage`, created_by) VALUES
('330e8400-e29b-41d4-a716-446655440000', 'SAP Production SSL', 'SSL Certificate', 'DigiCert Inc', '2024-01-01', '2025-01-01', 'active', 'SAP ERP Connection', '550e8400-e29b-41d4-a716-446655440000'),
('330e8400-e29b-41d4-a716-446655440001', 'Salesforce OAuth', 'OAuth Certificate', 'Salesforce.com', '2024-01-01', '2024-12-31', 'active', 'Salesforce API Authentication', '550e8400-e29b-41d4-a716-446655440000'),
('330e8400-e29b-41d4-a716-446655440002', 'Legacy System Cert', 'Client Certificate', 'Internal CA', '2023-06-01', '2024-02-01', 'expiring', 'Legacy SOAP Service', '550e8400-e29b-41d4-a716-446655440001'),
('330e8400-e29b-41d4-a716-446655440003', 'Database SSL Cert', 'SSL Certificate', 'Let\'s Encrypt', '2024-01-01', '2024-12-31', 'active', 'Database Connection Security', '550e8400-e29b-41d4-a716-446655440000'),
('330e8400-e29b-41d4-a716-446655440004', 'API Gateway Cert', 'SSL Certificate', 'Verisign', '2024-01-01', '2025-06-01', 'active', 'External API Gateway', '550e8400-e29b-41d4-a716-446655440001');

-- Insert JAR files
INSERT INTO jar_files (id, name, version, description, file_name, size_bytes, driver_type, upload_date, uploaded_by) VALUES
('220e8400-e29b-41d4-a716-446655440000', 'MySQL JDBC Driver', '8.0.33', 'MySQL Connector/J JDBC Driver for database connectivity', 'mysql-connector-java-8.0.33.jar', 2456789, 'Database', '2024-01-15', '550e8400-e29b-41d4-a716-446655440000'),
('220e8400-e29b-41d4-a716-446655440001', 'PostgreSQL JDBC Driver', '42.6.0', 'PostgreSQL JDBC Driver for database operations', 'postgresql-42.6.0.jar', 1234567, 'Database', '2024-01-10', '550e8400-e29b-41d4-a716-446655440001'),
('220e8400-e29b-41d4-a716-446655440002', 'ActiveMQ Client', '5.18.3', 'ActiveMQ JMS Client for message queue operations', 'activemq-client-5.18.3.jar', 987654, 'Message Queue', '2024-01-08', '550e8400-e29b-41d4-a716-446655440001'),
('220e8400-e29b-41d4-a716-446655440003', 'Oracle JDBC Driver', '21.7.0.0', 'Oracle Database JDBC Driver', 'ojdbc11-21.7.0.0.jar', 4567890, 'Database', '2024-01-12', '550e8400-e29b-41d4-a716-446655440000'),
('220e8400-e29b-41d4-a716-446655440004', 'RabbitMQ Client', '5.16.0', 'RabbitMQ Java Client for AMQP messaging', 'amqp-client-5.16.0.jar', 876543, 'Message Queue', '2024-01-09', '550e8400-e29b-41d4-a716-446655440001'),
('220e8400-e29b-41d4-a716-446655440005', 'MongoDB Driver', '4.8.2', 'MongoDB Java Driver for NoSQL database operations', 'mongodb-driver-sync-4.8.2.jar', 1567890, 'Database', '2024-01-11', '550e8400-e29b-41d4-a716-446655440000');

-- Sample Data Structures
INSERT INTO data_structures (id, name, type, description, `usage`, structure, tags, created_by) VALUES
('660e8400-e29b-41d4-a716-446655440000', 'Business Component Order', 'json', 'Standard business component order structure for e-commerce systems', 'source', 
 JSON_OBJECT('orderId', 'string', 'businessComponentId', 'string', 'businessComponentEmail', 'string', 'orderDate', 'datetime', 'items', JSON_ARRAY(JSON_OBJECT('productId', 'string', 'productName', 'string', 'quantity', 'integer', 'unitPrice', 'decimal', 'totalPrice', 'decimal')), 'totalAmount', 'decimal', 'currency', 'string', 'shippingAddress', JSON_OBJECT('street', 'string', 'city', 'string', 'state', 'string', 'zipCode', 'string', 'country', 'string'), 'status', 'string'),
 JSON_ARRAY('ecommerce', 'order', 'businessComponent'), '550e8400-e29b-41d4-a716-446655440000'),

('660e8400-e29b-41d4-a716-446655440001', 'Payment Response', 'json', 'Payment gateway response format', 'target',
 JSON_OBJECT('transactionId', 'string', 'orderId', 'string', 'amount', 'decimal', 'currency', 'string', 'status', 'string', 'paymentMethod', 'string', 'processingTime', 'datetime', 'gatewayResponse', JSON_OBJECT('code', 'string', 'message', 'string'), 'fees', JSON_OBJECT('processing', 'decimal', 'gateway', 'decimal')),
 JSON_ARRAY('payment', 'gateway', 'transaction'), '550e8400-e29b-41d4-a716-446655440000'),

('660e8400-e29b-41d4-a716-446655440002', 'User Profile', 'json', 'Universal user profile structure', 'both',
 JSON_OBJECT('userId', 'string', 'username', 'string', 'email', 'string', 'firstName', 'string', 'lastName', 'string', 'phone', 'string', 'dateOfBirth', 'date', 'address', JSON_OBJECT('street', 'string', 'city', 'string', 'state', 'string', 'zipCode', 'string', 'country', 'string'), 'preferences', JSON_OBJECT('language', 'string', 'timezone', 'string', 'notifications', JSON_OBJECT('email', 'boolean', 'sms', 'boolean')), 'createdAt', 'datetime', 'updatedAt', 'datetime', 'status', 'string'),
 JSON_ARRAY('user', 'profile', 'universal'), '550e8400-e29b-41d4-a716-446655440001'),

('660e8400-e29b-41d4-a716-446655440003', 'Inventory Update', 'json', 'Product inventory update message', 'both',
 JSON_OBJECT('productId', 'string', 'sku', 'string', 'name', 'string', 'category', 'string', 'currentStock', 'integer', 'reservedStock', 'integer', 'availableStock', 'integer', 'reorderLevel', 'integer', 'supplier', JSON_OBJECT('id', 'string', 'name', 'string', 'contact', 'string'), 'lastUpdated', 'datetime', 'warehouse', JSON_OBJECT('id', 'string', 'name', 'string', 'location', 'string')),
 JSON_ARRAY('inventory', 'product', 'stock'), '550e8400-e29b-41d4-a716-446655440001');

-- Sample Communication Adapters
INSERT INTO communication_adapters (id, name, type, mode, description, configuration, status, is_active, created_by) VALUES
('770e8400-e29b-41d4-a716-446655440000', 'Shopify REST API', 'rest', 'bidirectional', 'Shopify e-commerce platform integration via REST API',
 JSON_OBJECT('baseUrl', 'https://your-shop.myshopify.com/admin/api/2023-10', 'authentication', JSON_OBJECT('type', 'api-key', 'credentials', JSON_OBJECT('header', 'X-Shopify-Access-Token', 'token', 'your-access-token')), 'headers', JSON_OBJECT('Content-Type', 'application/json', 'Accept', 'application/json'), 'timeout', 30000, 'retryPolicy', JSON_OBJECT('maxRetries', 3, 'backoffMultiplier', 2)),
 'active', TRUE, '550e8400-e29b-41d4-a716-446655440000'),

('770e8400-e29b-41d4-a716-446655440001', 'PayPal Payment Gateway', 'rest', 'outbound', 'PayPal payment processing integration',
 JSON_OBJECT('baseUrl', 'https://api.paypal.com', 'authentication', JSON_OBJECT('type', 'oauth2', 'credentials', JSON_OBJECT('clientId', 'your-client-id', 'clientSecret', 'your-client-secret', 'scope', 'payments')), 'headers', JSON_OBJECT('Content-Type', 'application/json', 'Accept', 'application/json'), 'timeout', 45000),
 'active', TRUE, '550e8400-e29b-41d4-a716-446655440000'),

('770e8400-e29b-41d4-a716-446655440002', 'SFTP File Transfer', 'file', 'bidirectional', 'Secure file transfer for batch processing',
 JSON_OBJECT('protocol', 'sftp', 'host', 'files.example.com', 'port', 22, 'authentication', JSON_OBJECT('type', 'key', 'username', 'integration', 'privateKeyPath', '/keys/sftp-key'), 'directories', JSON_OBJECT('inbound', '/incoming', 'outbound', '/outgoing', 'archive', '/archive'), 'filePattern', '*.{json,xml,csv}', 'encoding', 'UTF-8'),
 'active', TRUE, '550e8400-e29b-41d4-a716-446655440001'),

('770e8400-e29b-41d4-a716-446655440003', 'PostgreSQL Database', 'database', 'bidirectional', 'Primary application database connection',
 JSON_OBJECT('driver', 'postgresql', 'connectionString', 'postgresql://username:password@localhost:5432/integration_db', 'schema', 'public', 'poolSize', 10, 'connectionTimeout', 30000, 'queryTimeout', 60000, 'ssl', JSON_OBJECT('enabled', true, 'rejectUnauthorized', false)),
 'active', TRUE, '550e8400-e29b-41d4-a716-446655440001'),

('770e8400-e29b-41d4-a716-446655440004', 'Email Notification Service', 'email', 'outbound', 'SMTP service for system notifications',
 JSON_OBJECT('smtpHost', 'smtp.gmail.com', 'smtpPort', 587, 'encryption', 'tls', 'authentication', JSON_OBJECT('username', 'notifications@company.com', 'password', 'app-password'), 'fromAddress', 'Integration Platform <notifications@company.com>', 'templates', JSON_OBJECT('success', 'flow-success.html', 'error', 'flow-error.html')),
 'active', TRUE, '550e8400-e29b-41d4-a716-446655440002'),

-- Add some inactive adapters for testing
('770e8400-e29b-41d4-a716-446655440005', 'Legacy SOAP Service', 'soap', 'inbound', 'Legacy system SOAP web service - temporarily disabled',
 JSON_OBJECT('wsdlUrl', 'http://legacy.example.com/service.wsdl', 'endpoint', 'http://legacy.example.com/soap', 'timeout', 60000, 'authentication', JSON_OBJECT('type', 'basic', 'username', 'legacy_user', 'password', 'legacy_pass')),
 'inactive', FALSE, '550e8400-e29b-41d4-a716-446655440001'),

('770e8400-e29b-41d4-a716-446655440006', 'Test SMS Gateway', 'sms', 'outbound', 'Testing SMS adapter - currently inactive',
 JSON_OBJECT('provider', 'Twilio', 'apiKey', 'test-api-key', 'apiSecret', 'test-secret', 'fromNumber', '+1234567890'),
 'testing', FALSE, '550e8400-e29b-41d4-a716-446655440002');

-- Sample Integration Flows
INSERT INTO integration_flows (id, name, description, source_adapter_id, target_adapter_id, source_structure_id, target_structure_id, status, configuration, created_by) VALUES
('880e8400-e29b-41d4-a716-446655440000', 'Shopify to PayPal Order Processing', 'Process new orders from Shopify and create payments in PayPal',
 '770e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440000', '660e8400-e29b-41d4-a716-446655440001', 'active',
 JSON_OBJECT('schedule', JSON_OBJECT('type', 'webhook', 'endpoint', '/webhooks/shopify/orders'), 'errorHandling', JSON_OBJECT('maxRetries', 3, 'retryDelay', 5000, 'notificationEmail', 'admin@company.com'), 'logging', JSON_OBJECT('level', 'INFO', 'includePayload', true)),
 '550e8400-e29b-41d4-a716-446655440000'),

('880e8400-e29b-41d4-a716-446655440001', 'File to Database Import', 'Import business component data from CSV files to database',
 '770e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440002', 'active',
 JSON_OBJECT('schedule', JSON_OBJECT('type', 'cron', 'expression', '0 2 * * *', 'timezone', 'UTC'), 'fileProcessing', JSON_OBJECT('archiveProcessed', true, 'deleteAfterProcessing', false), 'batchSize', 1000),
 '550e8400-e29b-41d4-a716-446655440001'),

('880e8400-e29b-41d4-a716-446655440002', 'Inventory Sync Flow', 'Synchronize inventory levels between systems',
 '770e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440000', '660e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440003', 'draft',
 JSON_OBJECT('schedule', JSON_OBJECT('type', 'interval', 'intervalMinutes', 15), 'conflictResolution', 'lastWriteWins', 'deltaSync', true),
 '550e8400-e29b-41d4-a716-446655440001');

-- Sample Flow Transformations
INSERT INTO flow_transformations (id, flow_id, type, configuration, execution_order) VALUES
('990e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440000', 'field-mapping', 
 JSON_OBJECT('name', 'Order to Payment Mapping', 'description', 'Map Shopify order fields to PayPal payment request'), 1),

('990e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440000', 'custom-function',
 JSON_OBJECT('name', 'Currency Conversion', 'functionName', 'convertCurrency', 'parameters', JSON_OBJECT('from', 'order.currency', 'to', 'USD', 'source', 'exchangeRateAPI')), 2),

('990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 'validation',
 JSON_OBJECT('rules', JSON_ARRAY(JSON_OBJECT('field', 'email', 'type', 'email', 'required', true), JSON_OBJECT('field', 'phone', 'type', 'phone', 'required', false))), 1),

('990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440001', 'field-mapping',
 JSON_OBJECT('name', 'CSV to Database Mapping', 'description', 'Map CSV columns to database fields'), 2);

-- Sample Field Mappings
INSERT INTO field_mappings (id, transformation_id, source_fields, target_field, java_function, mapping_rule) VALUES
('aa0e8400-e29b-41d4-a716-446655440000', '990e8400-e29b-41d4-a716-446655440000', 
 JSON_ARRAY('orderId'), 'transactionId', NULL, 'Direct mapping with prefix "ORDER_"'),

('aa0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440000',
 JSON_ARRAY('totalAmount'), 'amount', 'Math.round(value * 100) / 100', 'Round to 2 decimal places'),

('aa0e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440000',
 JSON_ARRAY('currency'), 'currency', NULL, 'Direct mapping'),

('aa0e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440003',
 JSON_ARRAY('firstName', 'lastName'), 'fullName', 'firstName + " " + lastName', 'Concatenate first and last name'),

('aa0e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440003',
 JSON_ARRAY('email'), 'email', 'value.toLowerCase()', 'Convert to lowercase');

-- Sample System Settings
INSERT INTO system_settings (category, `key`, `value`, description) VALUES
('integration', 'default_timeout', '30000', 'Default timeout for API calls in milliseconds'),
('integration', 'max_retries', '3', 'Maximum number of retries for failed operations'),
('integration', 'log_retention_days', '90', 'Number of days to retain execution logs'),
('security', 'session_timeout', '3600', 'User session timeout in seconds'),
('security', 'password_policy', JSON_OBJECT('minLength', 8, 'requireUppercase', true, 'requireLowercase', true, 'requireNumbers', true, 'requireSpecialChars', false), 'Password complexity requirements'),
('email', 'admin_notifications', JSON_ARRAY('admin@integration-platform.com'), 'Email addresses for system notifications'),
('monitoring', 'health_check_interval', '300', 'Health check interval in seconds'),
('monitoring', 'alert_thresholds', JSON_OBJECT('error_rate', 0.05, 'response_time', 5000, 'queue_depth', 1000), 'Monitoring alert thresholds');

-- Sample Flow Executions (for demo/testing)
INSERT INTO flow_executions (id, flow_id, status, started_at, completed_at, execution_time_ms, processed_records, failed_records) VALUES
('bb0e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440000', 'success', 
 DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 59 MINUTE), 1250, 1, 0),

('bb0e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440000', 'success',
 DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 119 MINUTE), 980, 1, 0),

('bb0e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 'success',
 DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 718 MINUTE), 15000, 500, 0),

('bb0e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440001', 'error',
 DATE_SUB(NOW(), INTERVAL 36 HOUR), DATE_SUB(NOW(), INTERVAL 2159 MINUTE), 5000, 250, 250);

-- Update statistics tables with sample data
INSERT INTO flow_statistics (flow_id, date, total_executions, successful_executions, failed_executions, total_execution_time_ms, total_records_processed) VALUES
('880e8400-e29b-41d4-a716-446655440000', CURDATE(), 24, 23, 1, 28500, 24),
('880e8400-e29b-41d4-a716-446655440001', CURDATE(), 1, 1, 0, 15000, 500),
('880e8400-e29b-41d4-a716-446655440000', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, 22, 0, 26400, 22),
('880e8400-e29b-41d4-a716-446655440001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 0, 1, 5000, 250);

INSERT INTO adapter_statistics (adapter_id, date, total_messages, successful_messages, failed_messages, total_response_time_ms, min_response_time_ms, max_response_time_ms) VALUES
('770e8400-e29b-41d4-a716-446655440000', CURDATE(), 48, 46, 2, 48000, 800, 2500),
('770e8400-e29b-41d4-a716-446655440001', CURDATE(), 24, 23, 1, 28500, 950, 1800),
('770e8400-e29b-41d4-a716-446655440002', CURDATE(), 2, 2, 0, 30000, 14000, 16000),
('770e8400-e29b-41d4-a716-446655440003', CURDATE(), 1500, 1500, 0, 75000, 20, 250);

-- Insert enhanced system logs for admin interface
INSERT INTO system_logs (id, timestamp, level, message, details, source, source_id, source_name, user_id) VALUES
('110e8400-e29b-41d4-a716-446655440000', '2024-01-15 14:30:00', 'info', 'User logged in successfully', JSON_OBJECT('ip', '192.168.1.100', 'userAgent', 'Mozilla/5.0'), 'system', '550e8400-e29b-41d4-a716-446655440000', 'Authentication Service', '550e8400-e29b-41d4-a716-446655440000'),
('110e8400-e29b-41d4-a716-446655440001', '2024-01-15 14:25:00', 'info', 'Adapter started successfully', JSON_OBJECT('adapterId', '770e8400-e29b-41d4-a716-446655440000', 'version', '1.2.3'), 'adapter', '770e8400-e29b-41d4-a716-446655440000', 'Shopify REST API', '550e8400-e29b-41d4-a716-446655440001'),
('110e8400-e29b-41d4-a716-446655440002', '2024-01-15 14:20:00', 'warn', 'Certificate expiring soon', JSON_OBJECT('certificateId', '330e8400-e29b-41d4-a716-446655440002', 'daysUntilExpiry', 15), 'system', '330e8400-e29b-41d4-a716-446655440002', 'Certificate Manager', NULL),
('110e8400-e29b-41d4-a716-446655440003', '2024-01-15 14:15:00', 'error', 'Database connection failed', JSON_OBJECT('host', 'db.example.com', 'port', 5432, 'error', 'Connection timeout'), 'adapter', '770e8400-e29b-41d4-a716-446655440003', 'PostgreSQL Database', '550e8400-e29b-41d4-a716-446655440001'),
('110e8400-e29b-41d4-a716-446655440004', '2024-01-15 14:10:00', 'info', 'Message processed successfully', JSON_OBJECT('messageId', 'msg_12345', 'processingTime', 150), 'channel', 'ch_001', 'HTTP Channel', NULL),
('110e8400-e29b-41d4-a716-446655440005', '2024-01-15 14:05:00', 'debug', 'Flow execution started', JSON_OBJECT('flowId', '880e8400-e29b-41d4-a716-446655440000', 'trigger', 'scheduler'), 'flow', '880e8400-e29b-41d4-a716-446655440000', 'Shopify to PayPal Order Processing', '550e8400-e29b-41d4-a716-446655440001'),
('110e8400-e29b-41d4-a716-446655440006', '2024-01-15 14:00:00', 'info', 'System health check completed', JSON_OBJECT('cpu', 45, 'memory', 68, 'disk', 23), 'system', NULL, 'Health Monitor', NULL),
('110e8400-e29b-41d4-a716-446655440007', '2024-01-15 13:55:00', 'warn', 'High memory usage detected', JSON_OBJECT('usage', 85, 'threshold', 80), 'system', NULL, 'Resource Monitor', NULL),
('110e8400-e29b-41d4-a716-446655440008', '2024-01-15 13:50:00', 'error', 'API rate limit exceeded', JSON_OBJECT('endpoint', '/api/messages', 'limit', 1000, 'current', 1001), 'api', NULL, 'API Gateway', NULL),
('110e8400-e29b-41d4-a716-446655440009', '2024-01-15 13:45:00', 'info', 'Certificate renewed successfully', JSON_OBJECT('certificateId', '330e8400-e29b-41d4-a716-446655440001', 'validUntil', '2025-01-15'), 'system', '330e8400-e29b-41d4-a716-446655440001', 'Certificate Manager', '550e8400-e29b-41d4-a716-446655440000'),
('110e8400-e29b-41d4-a716-446655440010', '2024-01-15 13:40:00', 'warn', 'Flow execution taking longer than expected', JSON_OBJECT('flowId', '880e8400-e29b-41d4-a716-446655440001', 'duration', 300000, 'expected', 120000), 'flow', '880e8400-e29b-41d4-a716-446655440001', 'File to Database Import', NULL);