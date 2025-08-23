-- Create message_structures table for pure XSD data definitions
CREATE TABLE IF NOT EXISTS `message_structures` (
    `id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `xsd_content` LONGTEXT NOT NULL,
    `namespace` JSON,
    `metadata` JSON,
    `tags` JSON,
    `version` INT DEFAULT 1,
    `is_active` BOOLEAN DEFAULT TRUE,
    `business_component_id` VARCHAR(36) NOT NULL,
    `created_by` VARCHAR(36),
    `updated_by` VARCHAR(36),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_message_structures_business_component` (`business_component_id`),
    KEY `idx_message_structures_name` (`name`),
    KEY `idx_message_structures_active` (`is_active`),
    CONSTRAINT `fk_message_structures_business_component` FOREIGN KEY (`business_component_id`) REFERENCES `business_components` (`id`),
    CONSTRAINT `fk_message_structures_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_message_structures_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create flow_structures table for WSDL service contracts
CREATE TABLE IF NOT EXISTS `flow_structures` (
    `id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `processing_mode` ENUM('SYNC', 'ASYNC') NOT NULL,
    `direction` ENUM('SOURCE', 'TARGET') NOT NULL,
    `wsdl_content` LONGTEXT,
    `namespace` JSON,
    `metadata` JSON,
    `tags` JSON,
    `version` INT DEFAULT 1,
    `is_active` BOOLEAN DEFAULT TRUE,
    `business_component_id` VARCHAR(36) NOT NULL,
    `created_by` VARCHAR(36),
    `updated_by` VARCHAR(36),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_flow_structures_business_component` (`business_component_id`),
    KEY `idx_flow_structures_name` (`name`),
    KEY `idx_flow_structures_active` (`is_active`),
    KEY `idx_flow_structures_mode_direction` (`processing_mode`, `direction`),
    CONSTRAINT `fk_flow_structures_business_component` FOREIGN KEY (`business_component_id`) REFERENCES `business_components` (`id`),
    CONSTRAINT `fk_flow_structures_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_flow_structures_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create flow_structure_messages table for linking flow structures to message structures
CREATE TABLE IF NOT EXISTS `flow_structure_messages` (
    `flow_structure_id` VARCHAR(36) NOT NULL,
    `message_type` ENUM('INPUT', 'OUTPUT', 'FAULT') NOT NULL,
    `message_structure_id` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`flow_structure_id`, `message_type`),
    KEY `idx_flow_structure_messages_structure` (`message_structure_id`),
    CONSTRAINT `fk_flow_structure_messages_flow` FOREIGN KEY (`flow_structure_id`) REFERENCES `flow_structures` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_flow_structure_messages_message` FOREIGN KEY (`message_structure_id`) REFERENCES `message_structures` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_message_structures_created_at ON message_structures(created_at);
CREATE INDEX idx_flow_structures_created_at ON flow_structures(created_at);

-- Add unique constraints to prevent duplicate names within business components
ALTER TABLE message_structures ADD CONSTRAINT uc_message_structure_name_business 
    UNIQUE KEY (name, business_component_id, is_active);
    
ALTER TABLE flow_structures ADD CONSTRAINT uc_flow_structure_name_business 
    UNIQUE KEY (name, business_component_id, is_active);