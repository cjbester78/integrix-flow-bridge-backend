-- Add parameters column to transformation_custom_functions table
-- This will store parsed parameter information as JSON

ALTER TABLE transformation_custom_functions 
ADD COLUMN parameters JSON NULL COMMENT 'Function parameters metadata' AFTER function_signature;

-- Update existing built-in functions with their parameters
-- Math functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"double","required":true,"description":"First number"},{"name":"b","type":"double","required":true,"description":"Second number"}]'
WHERE name IN ('add', 'subtract', 'multiply', 'divide', 'max', 'min');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"double","required":true,"description":"First value"},{"name":"b","type":"double","required":true,"description":"Second value"}]'
WHERE name IN ('equals', 'lesser', 'greater');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"double","required":true,"description":"Input value"}]'
WHERE name IN ('absolute', 'sqrt', 'square', 'sign', 'neg', 'inv', 'ceil', 'floor', 'round');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"base","type":"double","required":true,"description":"Base number"},{"name":"exponent","type":"double","required":true,"description":"Exponent"}]'
WHERE name = 'power';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"start","type":"int","required":false,"description":"Starting value"},{"name":"step","type":"int","required":false,"description":"Step increment"}]'
WHERE name = 'counter';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"double","required":true,"description":"Number to format"},{"name":"totalDigits","type":"int","required":true,"description":"Total number of digits"},{"name":"decimals","type":"int","required":false,"description":"Number of decimal places"}]'
WHERE name = 'formatNumber';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"values","type":"List","required":true,"description":"List of values"}]'
WHERE name IN ('sum', 'average', 'count');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"position","type":"int","required":true,"description":"Index position"}]'
WHERE name = 'index';

-- Text functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"string1","type":"String","required":true,"description":"First string"},{"name":"string2","type":"String","required":true,"description":"Second string"},{"name":"delimiter","type":"String","required":false,"description":"Optional delimiter"}]'
WHERE name = 'concat';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Input text"},{"name":"start","type":"int","required":true,"description":"Start index"},{"name":"end","type":"int","required":false,"description":"End index (optional)"}]'
WHERE name = 'substring';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to search in"},{"name":"searchValue","type":"String","required":true,"description":"Value to search for"}]'
WHERE name IN ('indexOf', 'lastIndexOf');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"string1","type":"String","required":true,"description":"First string"},{"name":"string2","type":"String","required":true,"description":"Second string"}]'
WHERE name = 'compare';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to search in"},{"name":"searchValue","type":"String","required":true,"description":"Value to search for"},{"name":"replaceValue","type":"String","required":true,"description":"Replacement value"}]'
WHERE name = 'replaceString';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Input text"}]'
WHERE name IN ('length', 'toUpperCase', 'toLowerCase', 'trim');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to check"},{"name":"suffix","type":"String","required":true,"description":"Suffix to check for"}]'
WHERE name = 'endsWith';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to check"},{"name":"prefix","type":"String","required":true,"description":"Prefix to check for"}]'
WHERE name = 'startsWith';

-- Boolean functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"boolean","required":true,"description":"First value"},{"name":"b","type":"boolean","required":true,"description":"Second value"}]'
WHERE name IN ('and', 'or');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"Object","required":true,"description":"First value"},{"name":"b","type":"Object","required":true,"description":"Second value"}]'
WHERE name = 'notEquals';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"boolean","required":true,"description":"Boolean value to negate"}]'
WHERE name = 'not';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"condition","type":"boolean","required":true,"description":"Condition to evaluate"},{"name":"trueValue","type":"Object","required":true,"description":"Value if true"},{"name":"falseValue","type":"Object","required":true,"description":"Value if false"}]'
WHERE name = 'if';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"condition","type":"boolean","required":true,"description":"Condition to evaluate"},{"name":"trueValue","type":"Object","required":true,"description":"Value if true"}]'
WHERE name = 'ifWithoutElse';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to check"}]'
WHERE name = 'isNil';

-- Conversion functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to fix"},{"name":"format","type":"String","required":true,"description":"Format specification"}]'
WHERE name = 'fixValues';