-- Change deployment_metadata from JSON to TEXT type
-- This field stores JSON as a string and is only populated during deployment
ALTER TABLE integration_flows 
ALTER COLUMN deployment_metadata TYPE TEXT;

COMMENT ON COLUMN integration_flows.deployment_metadata IS 'Additional deployment information as JSON string (WSDL URL, API docs, etc.)';