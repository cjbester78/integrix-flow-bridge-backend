-- Create test users for each role
-- Password for all test users: password123

-- Create test developer user
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role_id, role, status, email_verified, created_at)
VALUES (
    'user-test-developer', 
    'testdev', 
    'testdev@integrixlab.com',
    '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.',  -- password123
    'Test', 
    'Developer', 
    'role-developer', 
    'DEVELOPER',
    'active', 
    TRUE, 
    NOW()
) ON DUPLICATE KEY UPDATE 
    role = 'DEVELOPER',
    role_id = 'role-developer',
    password_hash = '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.';

-- Create test integrator user
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role_id, role, status, email_verified, created_at)
VALUES (
    'user-test-integrator', 
    'testint', 
    'testint@integrixlab.com',
    '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.',  -- password123
    'Test', 
    'Integrator', 
    'role-integrator', 
    'INTEGRATOR',
    'active', 
    TRUE, 
    NOW()
) ON DUPLICATE KEY UPDATE 
    role = 'INTEGRATOR',
    role_id = 'role-integrator',
    password_hash = '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.';

-- Create test viewer user
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role_id, role, status, email_verified, created_at)
VALUES (
    'user-test-viewer', 
    'testview', 
    'testview@integrixlab.com',
    '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.',  -- password123
    'Test', 
    'Viewer', 
    'role-viewer', 
    'VIEWER',
    'active', 
    TRUE, 
    NOW()
) ON DUPLICATE KEY UPDATE 
    role = 'VIEWER',
    role_id = 'role-viewer',
    password_hash = '$2a$10$8fTVpn6NGScktXzh4RKoGuSpQVefL/WYXLj.NinveOSAv0n.Cwj5.';

-- Show all test users
SELECT username, email, role FROM users WHERE username LIKE 'test%' OR username = 'admin' ORDER BY role;