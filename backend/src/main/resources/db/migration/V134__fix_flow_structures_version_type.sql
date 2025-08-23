-- V134: Fix version column type in flow_structures table
-- Entity expects INTEGER but table has VARCHAR

-- First drop the default
ALTER TABLE flow_structures 
ALTER COLUMN version DROP DEFAULT;

-- Convert column type
ALTER TABLE flow_structures 
ALTER COLUMN version TYPE INTEGER USING COALESCE(version::INTEGER, 1);

-- Set new default value
ALTER TABLE flow_structures 
ALTER COLUMN version SET DEFAULT 1;