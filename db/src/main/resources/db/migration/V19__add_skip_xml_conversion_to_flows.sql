-- Add skip_xml_conversion column to integration_flows table
ALTER TABLE integration_flows 
ADD COLUMN skip_xml_conversion BOOLEAN DEFAULT FALSE 
COMMENT 'Skip XML conversion for direct file passthrough';

-- Update existing flows to default value (false)
UPDATE integration_flows 
SET skip_xml_conversion = FALSE 
WHERE skip_xml_conversion IS NULL;