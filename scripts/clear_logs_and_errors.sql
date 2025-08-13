-- Script to clear system logs and user management errors tables
-- WARNING: This will permanently delete all records from these tables

-- Disable foreign key checks temporarily to avoid constraint issues
SET FOREIGN_KEY_CHECKS = 0;

-- Clear system_logs table
DELETE FROM system_logs;
SELECT 'Deleted all records from system_logs table' AS status;
SELECT COUNT(*) AS remaining_count FROM system_logs;

-- Clear user_management_errors table
DELETE FROM user_management_errors;
SELECT 'Deleted all records from user_management_errors table' AS status;
SELECT COUNT(*) AS remaining_count FROM user_management_errors;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Show final status
SELECT 'Cleanup completed successfully' AS final_status;