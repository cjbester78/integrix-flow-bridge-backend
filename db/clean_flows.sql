-- Script to clean all flow and mapping data
-- Run this with: mysql -u root -p integrixflowbridge < clean_flows.sql

SET FOREIGN_KEY_CHECKS = 0;

-- Delete all field mappings
DELETE FROM field_mappings;
SELECT 'Deleted field_mappings' as status, ROW_COUNT() as rows_deleted;

-- Delete all flow transformations
DELETE FROM flow_transformations;
SELECT 'Deleted flow_transformations' as status, ROW_COUNT() as rows_deleted;

-- Delete all integration flows
DELETE FROM integration_flows;
SELECT 'Deleted integration_flows' as status, ROW_COUNT() as rows_deleted;

-- Delete all flow executions (if any)
DELETE FROM flow_executions;
SELECT 'Deleted flow_executions' as status, ROW_COUNT() as rows_deleted;

-- Delete all flow execution logs (if any)
DELETE FROM flow_execution_logs;
SELECT 'Deleted flow_execution_logs' as status, ROW_COUNT() as rows_deleted;

SET FOREIGN_KEY_CHECKS = 1;

-- Show final counts to confirm everything is empty
SELECT '=== Final Table Counts ===' as info;
SELECT 'integration_flows' as table_name, COUNT(*) as count FROM integration_flows
UNION ALL
SELECT 'flow_transformations', COUNT(*) FROM flow_transformations
UNION ALL
SELECT 'field_mappings', COUNT(*) FROM field_mappings
UNION ALL
SELECT 'flow_executions', COUNT(*) FROM flow_executions
UNION ALL
SELECT 'flow_execution_logs', COUNT(*) FROM flow_execution_logs;