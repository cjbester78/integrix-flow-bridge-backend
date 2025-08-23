-- Drop the configuration column from integration_flows table
-- We are moving away from JSON configuration to native database columns
ALTER TABLE integration_flows DROP COLUMN IF EXISTS configuration;