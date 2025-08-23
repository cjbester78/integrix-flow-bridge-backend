-- Drop the reusable_java_functions table as it's no longer used
-- Custom functions are now stored in transformation_custom_functions table

-- Drop the table
DROP TABLE IF EXISTS reusable_java_functions;

-- Remove from V16 migration references if any
-- Note: The table was referenced in V16__fix_updated_at_default.sql 
-- but this won't affect existing deployments