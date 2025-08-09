-- Rename 'usage' column to 'usage_type' to avoid MySQL reserved keyword conflict
ALTER TABLE data_structures 
CHANGE COLUMN `usage` `usage_type` VARCHAR(20) NOT NULL;