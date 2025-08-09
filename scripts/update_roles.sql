-- Update Roles with Correct Permissions
-- According to requirements:
-- Administrator: Full admin access to all pages and content
-- Developer: Same access as administrator EXCEPT they CANNOT access system settings and configurations
-- Integrator: Is only used to access integration API and run the interface transformations from start to finish
-- Viewer: allowed to view monitoring pages and flows to see if it is deployed or not

-- First, let's check if Developer role exists
INSERT INTO roles (id, name, description, permissions) 
VALUES ('role-developer', 'developer', 'Same access as administrator except no system settings and configurations', '[]')
ON DUPLICATE KEY UPDATE id=id;

-- Update Administrator role - Full admin access to all pages and content
UPDATE roles 
SET description = 'Full admin access to all pages and content',
    permissions = JSON_ARRAY(
        'flows:create', 'flows:read', 'flows:update', 'flows:delete', 'flows:deploy',
        'adapters:create', 'adapters:read', 'adapters:update', 'adapters:delete', 'adapters:test',
        'structures:create', 'structures:read', 'structures:update', 'structures:delete',
        'mappings:create', 'mappings:read', 'mappings:update', 'mappings:delete',
        'users:create', 'users:read', 'users:update', 'users:delete',
        'roles:read', 'roles:update',
        'logs:read', 'logs:delete',
        'messages:read', 'messages:retry', 'messages:delete',
        'dashboard:read',
        'monitoring:read',
        'development:read', 'development:write',
        'system:admin', 'system:config', 'system:settings'
    )
WHERE name = 'administrator';

-- Update Developer role - Same as admin EXCEPT no system settings and configurations
UPDATE roles 
SET description = 'Same access as administrator except no system settings and configurations',
    permissions = JSON_ARRAY(
        'flows:create', 'flows:read', 'flows:update', 'flows:delete', 'flows:deploy',
        'adapters:create', 'adapters:read', 'adapters:update', 'adapters:delete', 'adapters:test',
        'structures:create', 'structures:read', 'structures:update', 'structures:delete',
        'mappings:create', 'mappings:read', 'mappings:update', 'mappings:delete',
        'users:create', 'users:read', 'users:update', 'users:delete',
        'roles:read', 'roles:update',
        'logs:read', 'logs:delete',
        'messages:read', 'messages:retry', 'messages:delete',
        'dashboard:read',
        'monitoring:read',
        'development:read', 'development:write'
    )
WHERE name = 'developer';

-- Update Integrator role - ONLY execute message flows
UPDATE roles 
SET description = 'Only used to access integration API and run interface transformations',
    permissions = JSON_ARRAY(
        'flows:execute',
        'api:access'
    )
WHERE name = 'integrator';

-- Update Viewer role - View monitoring pages and flows deployment status
UPDATE roles 
SET description = 'View monitoring pages and flows to see deployment status',
    permissions = JSON_ARRAY(
        'flows:read',
        'adapters:read',
        'structures:read',
        'mappings:read',
        'logs:read',
        'messages:read',
        'dashboard:read',
        'monitoring:read'
    )
WHERE name = 'viewer';

-- Show updated roles
SELECT name, description, JSON_PRETTY(permissions) as permissions FROM roles ORDER BY name;