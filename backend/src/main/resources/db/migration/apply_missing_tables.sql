-- Quick script to apply missing tables to existing PostgreSQL database
-- Run this with: psql -U integrix -d integrixflowbridge -f apply_missing_tables.sql

-- First, apply the V2 migration for missing tables
\i POSTGRESQL_V2__missing_tables.sql

-- Verify tables were created
\dt user_sessions
\dt user_management_errors
\dt transformation_custom_functions
\dt system_settings
\dt jar_files
\dt certificates
\dt audit_trail
\dt audit_logs

-- Show all tables
\echo 'All tables in database:'
\dt