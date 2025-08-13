-- Fix skip_xml_conversion column to be NOT NULL
-- First ensure all existing NULL values are set to FALSE
UPDATE integration_flows 
SET skip_xml_conversion = FALSE 
WHERE skip_xml_conversion IS NULL;

-- Now alter the column to be NOT NULL
ALTER TABLE integration_flows 
MODIFY COLUMN skip_xml_conversion BOOLEAN NOT NULL DEFAULT FALSE 
COMMENT 'Skip XML conversion for direct file passthrough';