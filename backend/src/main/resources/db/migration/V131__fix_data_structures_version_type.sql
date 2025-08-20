-- V131: Fix version column type in data_structures table
-- Entity expects INTEGER but table has VARCHAR

ALTER TABLE data_structures 
ALTER COLUMN version TYPE INTEGER USING COALESCE(version::INTEGER, 1);

-- Set default value
ALTER TABLE data_structures 
ALTER COLUMN version SET DEFAULT 1;