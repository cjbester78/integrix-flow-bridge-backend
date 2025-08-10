-- Test Users Insert Script for IntegrixLab
-- Note: These password hashes are for testing only. In production, use proper password hashing.
-- Password hashes below are bcrypt hashes for common test passwords

USE integrixlab;

-- Clean up existing test users first (optional - uncomment if needed)
-- DELETE FROM users WHERE username IN ('admin', 'integrator', 'viewer', 'testuser', 'inactive');

-- Insert test users with different roles (ignore duplicates)
INSERT IGNORE INTO users (
    id,
    username,
    email,
    password_hash,
    first_name,
    last_name,
    role,
    status,
    email_verified,
    permissions
) VALUES 
-- Administrator user (password: admin123)
(
    UUID(),
    'admin',
    'admin@integrixlab.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyN.wJ4DPmv2O6',
    'System',
    'Administrator',
    'administrator',
    'active',
    TRUE,
    JSON_OBJECT(
        'admin', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'users', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'flows', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'adapters', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'certificates', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'system', JSON_ARRAY('create', 'read', 'update', 'delete')
    )
),

-- Integrator user (password: integrator123)
(
    UUID(),
    'integrator',
    'integrator@integrixlab.com',
    '$2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'John',
    'Integrator',
    'integrator',
    'active',
    TRUE,
    JSON_OBJECT(
        'flows', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'adapters', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'data-structures', JSON_ARRAY('create', 'read', 'update', 'delete'),
        'messages', JSON_ARRAY('read'),
        'channels', JSON_ARRAY('read')
    )
),

-- Viewer user (password: viewer123)
(
    UUID(),
    'viewer',
    'viewer@integrixlab.com',
    '$2b$12$wuiAOZOzUOqFWG8YZJ4KU.6CQD2UUWQNVJtVY/HV7vOD8zG0i6J2q',
    'Jane',
    'Viewer',
    'viewer',
    'active',
    TRUE,
    JSON_OBJECT(
        'flows', JSON_ARRAY('read'),
        'adapters', JSON_ARRAY('read'),
        'data-structures', JSON_ARRAY('read'),
        'messages', JSON_ARRAY('read'),
        'channels', JSON_ARRAY('read')
    )
),

-- Test Integrator user (password: test123)
(
    UUID(),
    'testuser',
    'test@integrixlab.com',
    '$2b$12$ZiKz.tWQ4Q1gZL4LvQ4OQO2rQ4QR6QZ4Qz6QzQzQzQzQzQzQzQzQz',
    'Test',
    'User',
    'integrator',
    'active',
    TRUE,
    JSON_OBJECT(
        'flows', JSON_ARRAY('create', 'read', 'update'),
        'adapters', JSON_ARRAY('create', 'read', 'update'),
        'data-structures', JSON_ARRAY('create', 'read', 'update'),
        'messages', JSON_ARRAY('read')
    )
),

-- Inactive user for testing (password: inactive123)
(
    UUID(),
    'inactive',
    'inactive@integrixlab.com',
    '$2b$12$X7RJ2k3PW9vQ5nD8cL6mR.oK8mN9jP2sT4uV6wX1yZ3aB5cD7eF9gH',
    'Inactive',
    'User',
    'viewer',
    'inactive',
    FALSE,
    JSON_OBJECT()
);

-- Display inserted users for verification
SELECT 
    username,
    email,
    first_name,
    last_name,
    role,
    status,
    email_verified,
    created_at
FROM users 
ORDER BY created_at DESC;