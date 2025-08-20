-- Set environment to DEVELOPMENT if not already set
INSERT INTO system_configuration (id, config_key, config_value, config_type, description, updated_at)
VALUES (
    gen_random_uuid(), 
    'system.environment.type', 
    'DEVELOPMENT', 
    'ENVIRONMENT',
    'System environment type (DEVELOPMENT, QUALITY_ASSURANCE, PRODUCTION)', 
    NOW()
)
ON CONFLICT (config_key) DO UPDATE SET
    config_value = 'DEVELOPMENT',
    updated_at = NOW();

-- Disable environment restrictions for development
INSERT INTO system_configuration (id, config_key, config_value, config_type, description, updated_at)
VALUES (
    gen_random_uuid(), 
    'system.environment.enforceRestrictions', 
    'false', 
    'BOOLEAN',
    'Whether to enforce environment restrictions', 
    NOW()
)
ON CONFLICT (config_key) DO UPDATE SET
    config_value = 'false',
    updated_at = NOW();