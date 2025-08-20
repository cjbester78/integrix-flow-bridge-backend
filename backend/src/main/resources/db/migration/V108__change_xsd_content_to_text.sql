-- Change xsd_content column from XML to TEXT type in message_structures
ALTER TABLE message_structures 
ALTER COLUMN xsd_content TYPE TEXT 
USING xsd_content::TEXT;

-- Change wsdl_content column from XML to TEXT type in flow_structures if it exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'flow_structures' 
        AND column_name = 'wsdl_content'
    ) THEN
        ALTER TABLE flow_structures 
        ALTER COLUMN wsdl_content TYPE TEXT 
        USING wsdl_content::TEXT;
    END IF;
END $$;

-- Change original_wsdl_content column from XML to TEXT type in flow_structures if it exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'flow_structures' 
        AND column_name = 'original_wsdl_content'
    ) THEN
        ALTER TABLE flow_structures 
        ALTER COLUMN original_wsdl_content TYPE TEXT 
        USING original_wsdl_content::TEXT;
    END IF;
END $$;