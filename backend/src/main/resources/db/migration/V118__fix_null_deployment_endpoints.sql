-- V118__fix_null_deployment_endpoints.sql
-- Fix any deployed flows that have null deployment endpoints

-- Update TempConvert SOAP flow if it exists and has null deployment endpoint
UPDATE integration_flows 
SET deployment_endpoint = 'http://localhost:8080/soap/tempconvert'
WHERE name = 'TempConvert SOAP' 
  AND status = 'DEPLOYED_ACTIVE' 
  AND deployment_endpoint IS NULL;

-- Log what we did
SELECT 
    id,
    name,
    status,
    deployment_endpoint,
    deployed_at
FROM integration_flows 
WHERE status = 'DEPLOYED_ACTIVE';