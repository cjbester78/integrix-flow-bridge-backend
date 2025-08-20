-- V134: Fix version column type in flow_structures table
-- Entity expects INTEGER but table has VARCHAR

ALTER TABLE flow_structures 
ALTER COLUMN version TYPE INTEGER USING COALESCE(version::INTEGER, 1);

-- Set default value
ALTER TABLE flow_structures 
ALTER COLUMN version SET DEFAULT 1;