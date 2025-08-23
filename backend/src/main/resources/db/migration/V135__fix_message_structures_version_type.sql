-- V135: Fix version column type in message_structures table
-- Entity expects INTEGER but table has VARCHAR

-- First drop the default
ALTER TABLE message_structures 
ALTER COLUMN version DROP DEFAULT;

-- Convert column type
ALTER TABLE message_structures 
ALTER COLUMN version TYPE INTEGER USING COALESCE(version::INTEGER, 1);

-- Set new default value
ALTER TABLE message_structures 
ALTER COLUMN version SET DEFAULT 1;