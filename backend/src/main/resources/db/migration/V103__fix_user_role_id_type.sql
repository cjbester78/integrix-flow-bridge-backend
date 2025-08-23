-- First, drop the existing foreign key constraint if it exists
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_user_role;

-- Convert role_id from character to UUID
ALTER TABLE users ALTER COLUMN role_id TYPE UUID USING role_id::uuid;

-- Now add the foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_user_role 
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL;