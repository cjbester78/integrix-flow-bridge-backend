-- Set environment to DEVELOPMENT if not already set
INSERT INTO system_configuration (id, config_key, config_value, description, updated_at)
VALUES (
    UUID(), 
    'system.environment.type', 
    'DEVELOPMENT', 
    'System environment type (DEVELOPMENT, QUALITY_ASSURANCE, PRODUCTION)', 
    NOW()
)
ON DUPLICATE KEY UPDATE 
    config_value = 'DEVELOPMENT',
    updated_at = NOW();

-- Disable environment restrictions for development
INSERT INTO system_configuration (id, config_key, config_value, description, updated_at)
VALUES (
    UUID(), 
    'system.environment.enforceRestrictions', 
    'false', 
    'Whether to enforce environment restrictions', 
    NOW()
)
ON DUPLICATE KEY UPDATE 
    config_value = 'false',
    updated_at = NOW();