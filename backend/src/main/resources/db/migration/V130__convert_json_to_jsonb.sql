-- V130: Convert JSON columns to JSONB in data_structures table
-- Hibernate entity expects JSONB columns but table has mixed JSON/JSONB types

ALTER TABLE data_structures 
ALTER COLUMN structure TYPE JSONB USING structure::JSONB,
ALTER COLUMN namespace TYPE JSONB USING namespace::JSONB,
ALTER COLUMN tags TYPE JSONB USING tags::JSONB;

-- Update default value for structure column
ALTER TABLE data_structures 
ALTER COLUMN structure SET DEFAULT '{}'::jsonb;