-- Add columns to store original content alongside JSON representation
ALTER TABLE data_structures 
ADD COLUMN original_content LONGTEXT DEFAULT NULL,
ADD COLUMN original_format VARCHAR(20) DEFAULT NULL;

-- Add index on original_format for filtering
CREATE INDEX idx_data_structures_original_format ON data_structures(original_format);

-- Update existing records to set original_format based on type
UPDATE data_structures 
SET original_format = CASE 
    WHEN type = 'json' THEN 'json'
    WHEN type = 'xml' THEN 'xml'
    WHEN type = 'xsd' THEN 'xml'
    WHEN type = 'wsdl' THEN 'xml'
    WHEN type = 'csv' THEN 'csv'
    WHEN type = 'custom' THEN 'custom'
    ELSE NULL
END
WHERE original_format IS NULL;