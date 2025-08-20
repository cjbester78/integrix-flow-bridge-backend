-- Convert all character(36) ID columns to UUID type for consistency
-- This migration converts the following tables: adapter_payloads, audit_trail, certificates, 
-- field_mappings, system_logs, system_settings, user_management_errors, user_sessions

-- Temporarily drop foreign key constraints
ALTER TABLE user_management_errors DROP CONSTRAINT IF EXISTS user_management_errors_log_id_fkey;
ALTER TABLE user_management_errors DROP CONSTRAINT IF EXISTS fkimg43psosy45cm3umx8lx86fy;

-- Convert adapter_payloads
ALTER TABLE adapter_payloads 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert audit_trail
ALTER TABLE audit_trail 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert certificates
ALTER TABLE certificates 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert field_mappings
ALTER TABLE field_mappings 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert system_logs
ALTER TABLE system_logs 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert system_settings
ALTER TABLE system_settings 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Convert user_management_errors
ALTER TABLE user_management_errors 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Also convert the log_id foreign key column
ALTER TABLE user_management_errors 
    ALTER COLUMN log_id TYPE UUID USING log_id::uuid;

-- Convert user_sessions
ALTER TABLE user_sessions 
    ALTER COLUMN id TYPE UUID USING id::uuid,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- Re-add the foreign key constraint
ALTER TABLE user_management_errors 
    ADD CONSTRAINT user_management_errors_log_id_fkey 
    FOREIGN KEY (log_id) REFERENCES system_logs(id);