-- Adapter Types Table Schema
CREATE TABLE IF NOT EXISTS adapter_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    description TEXT,
    category ENUM('FILE_TRANSFER', 'MESSAGING', 'DATABASE', 'WEB_SERVICE', 'MAIL', 'OTHER') NOT NULL,
    icon_name VARCHAR(50) DEFAULT 'network',
    is_active BOOLEAN DEFAULT TRUE,
    supports_sender BOOLEAN DEFAULT TRUE,
    supports_receiver BOOLEAN DEFAULT TRUE,
    configuration_schema JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    INDEX idx_category (category),
    INDEX idx_active (is_active),
    INDEX idx_name (name)
);

-- Adapter Configuration Templates Table
CREATE TABLE IF NOT EXISTS adapter_configuration_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    adapter_type_id BIGINT NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_description TEXT,
    configuration_json JSON NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (adapter_type_id) REFERENCES adapter_types(id) ON DELETE CASCADE,
    UNIQUE KEY unique_default_per_type (adapter_type_id, is_default),
    INDEX idx_adapter_type (adapter_type_id)
);