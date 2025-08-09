-- First, check the current enum values
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'transformation_custom_functions' 
-- AND COLUMN_NAME = 'language';

-- Update the language enum to include JAVA
ALTER TABLE transformation_custom_functions 
MODIFY COLUMN language ENUM('JAVA', 'JAVASCRIPT', 'GROOVY', 'PYTHON') NOT NULL;

-- Now update all built-in functions to use JAVA
UPDATE transformation_custom_functions 
SET language = 'JAVA',
    updated_at = NOW()
WHERE is_built_in = true;

-- Verify the update
SELECT function_id, name, language, is_built_in 
FROM transformation_custom_functions 
WHERE is_built_in = true 
LIMIT 5;