-- Convert system_logs user_id back to character varying to match entity expectations
-- The SystemLog entity expects user_id as String, not UUID
ALTER TABLE system_logs 
    ALTER COLUMN user_id TYPE VARCHAR(36) USING CASE 
        WHEN user_id IS NULL THEN NULL 
        ELSE user_id::text 
    END;