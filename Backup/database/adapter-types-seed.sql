-- Seed Data for Adapter Types
INSERT INTO adapter_types (name, display_name, description, category, icon_name, supports_sender, supports_receiver, configuration_schema) VALUES
-- File Transfer Adapters
('ftp', 'FTP Adapter', 'File Transfer Protocol adapter for sending and receiving files', 'FILE_TRANSFER', 'folder', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'serverAddress', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 21),
        'username', JSON_OBJECT('type', 'string', 'required', true),
        'password', JSON_OBJECT('type', 'password', 'required', true),
        'connectionSecurity', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('plain-ftp', 'explicit-ftps', 'implicit-ftps'))
    ),
    'processing', JSON_OBJECT(
        'pollingInterval', JSON_OBJECT('type', 'number', 'default', 60),
        'fileConstructionMode', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('create', 'append', 'add-time-stamp'))
    )
)),

('sftp', 'SFTP Adapter', 'Secure File Transfer Protocol adapter with SSH encryption', 'FILE_TRANSFER', 'shield', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'serverAddress', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 22),
        'authenticationType', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('public-key', 'username-password', 'dual', 'dynamic'))
    ),
    'processing', JSON_OBJECT(
        'createFileDirectory', JSON_OBJECT('type', 'boolean', 'default', false),
        'overwriteExistingFile', JSON_OBJECT('type', 'boolean', 'default', false)
    )
)),

('file_system', 'File System Adapter', 'Local or network file system adapter', 'FILE_TRANSFER', 'hard-drive', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'basePath', JSON_OBJECT('type', 'string', 'required', true),
        'accessMode', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('local', 'network-share', 'mounted-drive'))
    )
)),

-- Messaging Adapters
('jms', 'JMS Adapter', 'Java Message Service adapter for enterprise messaging', 'MESSAGING', 'message-square', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'providerUrl', JSON_OBJECT('type', 'string', 'required', true),
        'connectionFactory', JSON_OBJECT('type', 'string', 'required', true),
        'username', JSON_OBJECT('type', 'string'),
        'password', JSON_OBJECT('type', 'password')
    )
)),

('amqp', 'AMQP Adapter', 'Advanced Message Queuing Protocol adapter', 'MESSAGING', 'zap', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'hostName', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 5672),
        'virtualHost', JSON_OBJECT('type', 'string', 'default', '/'),
        'username', JSON_OBJECT('type', 'string'),
        'password', JSON_OBJECT('type', 'password')
    )
)),

('kafka', 'Apache Kafka Adapter', 'High-throughput distributed streaming platform adapter', 'MESSAGING', 'activity', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'bootstrapServers', JSON_OBJECT('type', 'string', 'required', true),
        'securityProtocol', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('PLAINTEXT', 'SSL', 'SASL_PLAINTEXT', 'SASL_SSL')),
        'groupId', JSON_OBJECT('type', 'string')
    )
)),

-- Database Adapters
('jdbc', 'JDBC Adapter', 'Java Database Connectivity adapter for relational databases', 'DATABASE', 'database', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'jdbcUrl', JSON_OBJECT('type', 'string', 'required', true),
        'driverClass', JSON_OBJECT('type', 'string', 'required', true),
        'username', JSON_OBJECT('type', 'string'),
        'password', JSON_OBJECT('type', 'password'),
        'maxPoolSize', JSON_OBJECT('type', 'number', 'default', 10)
    )
)),

-- Web Service Adapters
('soap', 'SOAP Adapter', 'Simple Object Access Protocol web service adapter', 'WEB_SERVICE', 'globe', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'wsdlUrl', JSON_OBJECT('type', 'string', 'required', true),
        'serviceName', JSON_OBJECT('type', 'string'),
        'portName', JSON_OBJECT('type', 'string'),
        'authentication', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('none', 'basic', 'certificate'))
    )
)),

('rest', 'REST Adapter', 'RESTful web service adapter', 'WEB_SERVICE', 'cloud', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'baseUrl', JSON_OBJECT('type', 'string', 'required', true),
        'authentication', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('none', 'basic', 'bearer', 'api-key', 'oauth2')),
        'timeout', JSON_OBJECT('type', 'number', 'default', 30000)
    )
)),

-- Mail Adapters
('smtp', 'SMTP Adapter', 'Simple Mail Transfer Protocol adapter for sending emails', 'MAIL', 'mail', FALSE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'smtpServer', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 587),
        'encryption', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('none', 'tls', 'ssl')),
        'username', JSON_OBJECT('type', 'string'),
        'password', JSON_OBJECT('type', 'password')
    )
)),

('imap', 'IMAP Adapter', 'Internet Message Access Protocol adapter for receiving emails', 'MAIL', 'inbox', TRUE, FALSE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'imapServer', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 993),
        'encryption', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('ssl', 'tls', 'none')),
        'username', JSON_OBJECT('type', 'string', 'required', true),
        'password', JSON_OBJECT('type', 'password', 'required', true)
    )
)),

('pop3', 'POP3 Adapter', 'Post Office Protocol v3 adapter for receiving emails', 'MAIL', 'download', TRUE, FALSE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'pop3Server', JSON_OBJECT('type', 'string', 'required', true),
        'port', JSON_OBJECT('type', 'number', 'default', 995),
        'encryption', JSON_OBJECT('type', 'select', 'options', JSON_ARRAY('ssl', 'tls', 'none')),
        'username', JSON_OBJECT('type', 'string', 'required', true),
        'password', JSON_OBJECT('type', 'password', 'required', true)
    )
)),

-- Other Adapters
('idoc', 'IDoc Adapter', 'SAP Intermediate Document adapter', 'OTHER', 'file-text', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'sapSystem', JSON_OBJECT('type', 'string', 'required', true),
        'client', JSON_OBJECT('type', 'string', 'required', true),
        'username', JSON_OBJECT('type', 'string', 'required', true),
        'password', JSON_OBJECT('type', 'password', 'required', true)
    )
)),

('rfc', 'RFC Adapter', 'SAP Remote Function Call adapter', 'OTHER', 'cpu', TRUE, TRUE, JSON_OBJECT(
    'connection', JSON_OBJECT(
        'sapSystem', JSON_OBJECT('type', 'string', 'required', true),
        'client', JSON_OBJECT('type', 'string', 'required', true),
        'username', JSON_OBJECT('type', 'string', 'required', true),
        'password', JSON_OBJECT('type', 'password', 'required', true),
        'language', JSON_OBJECT('type', 'string', 'default', 'EN')
    )
));

-- Default Configuration Templates
INSERT INTO adapter_configuration_templates (adapter_type_id, template_name, template_description, configuration_json, is_default) VALUES
-- FTP Default Template
((SELECT id FROM adapter_types WHERE name = 'ftp'), 'Standard FTP', 'Standard FTP configuration template', JSON_OBJECT(
    'serverAddress', '',
    'port', 21,
    'connectionSecurity', 'plain-ftp',
    'pollingInterval', 60,
    'fileConstructionMode', 'create',
    'processingMode', 'archive',
    'emptyFileHandling', 'process-empty'
), TRUE),

-- SFTP Default Template  
((SELECT id FROM adapter_types WHERE name = 'sftp'), 'Standard SFTP', 'Standard SFTP configuration template', JSON_OBJECT(
    'serverAddress', '',
    'port', 22,
    'authenticationType', 'username-password',
    'createFileDirectory', false,
    'overwriteExistingFile', false,
    'fileConstructionMode', 'create',
    'processingMode', 'archive'
), TRUE),

-- REST Default Template
((SELECT id FROM adapter_types WHERE name = 'rest'), 'Standard REST', 'Standard REST API configuration template', JSON_OBJECT(
    'baseUrl', '',
    'authentication', 'none',
    'timeout', 30000,
    'retryAttempts', 3,
    'retryDelay', 1000
), TRUE);