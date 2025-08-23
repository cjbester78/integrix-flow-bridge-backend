-- Update transformation function parameters to include UI type (draggable vs configurable)
-- This allows the frontend to properly render function nodes with appropriate input handles and configuration fields

-- Math functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"double","required":true,"description":"First number","uiType":"draggable"},{"name":"b","type":"double","required":true,"description":"Second number","uiType":"draggable"}]'::json
WHERE name IN ('add', 'subtract', 'multiply', 'divide', 'max', 'min');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"double","required":true,"description":"First value","uiType":"draggable"},{"name":"b","type":"double","required":true,"description":"Second value","uiType":"draggable"}]'::json
WHERE name IN ('equals', 'lesser', 'greater');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"double","required":true,"description":"Input value","uiType":"draggable"}]'::json
WHERE name IN ('absolute', 'sqrt', 'square', 'sign', 'neg', 'inv', 'ceil', 'floor', 'round');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"base","type":"double","required":true,"description":"Base number","uiType":"draggable"},{"name":"exponent","type":"double","required":true,"description":"Exponent","uiType":"configurable"}]'::json
WHERE name = 'power';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"start","type":"int","required":false,"description":"Starting value","uiType":"configurable"},{"name":"step","type":"int","required":false,"description":"Step increment","uiType":"configurable"}]'::json
WHERE name = 'counter';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"double","required":true,"description":"Number to format","uiType":"draggable"},{"name":"totalDigits","type":"int","required":true,"description":"Total number of digits","uiType":"configurable"},{"name":"decimals","type":"int","required":false,"description":"Number of decimal places","uiType":"configurable"}]'::json
WHERE name = 'formatNumber';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"values","type":"List","required":true,"description":"List of values","uiType":"draggable"}]'::json
WHERE name IN ('sum', 'average', 'count');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"position","type":"int","required":true,"description":"Index position","uiType":"configurable"}]'::json
WHERE name = 'index';

-- Text functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"string1","type":"String","required":true,"description":"First string","uiType":"draggable"},{"name":"string2","type":"String","required":true,"description":"Second string","uiType":"draggable"},{"name":"delimiter","type":"String","required":false,"description":"Optional delimiter","uiType":"configurable"}]'::json
WHERE name = 'concat';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Input text","uiType":"draggable"},{"name":"start","type":"int","required":true,"description":"Start index","uiType":"configurable"},{"name":"end","type":"int","required":false,"description":"End index (optional)","uiType":"configurable"}]'::json
WHERE name = 'substring';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to search in","uiType":"draggable"},{"name":"searchValue","type":"String","required":true,"description":"Value to search for","uiType":"configurable"}]'::json
WHERE name IN ('indexOf', 'lastIndexOf');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"string1","type":"String","required":true,"description":"First string","uiType":"draggable"},{"name":"string2","type":"String","required":true,"description":"Second string","uiType":"draggable"}]'::json
WHERE name = 'compare';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to search in","uiType":"draggable"},{"name":"searchValue","type":"String","required":true,"description":"Value to search for","uiType":"configurable"},{"name":"replaceValue","type":"String","required":true,"description":"Replacement value","uiType":"configurable"}]'::json
WHERE name = 'replaceString';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Input text","uiType":"draggable"}]'::json
WHERE name IN ('length', 'toUpperCase', 'toLowerCase', 'trim');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to check","uiType":"draggable"},{"name":"suffix","type":"String","required":true,"description":"Suffix to check for","uiType":"configurable"}]'::json
WHERE name = 'endsWith';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"text","type":"String","required":true,"description":"Text to check","uiType":"draggable"},{"name":"prefix","type":"String","required":true,"description":"Prefix to check for","uiType":"configurable"}]'::json
WHERE name = 'startsWith';

-- Boolean functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"boolean","required":true,"description":"First value","uiType":"draggable"},{"name":"b","type":"boolean","required":true,"description":"Second value","uiType":"draggable"}]'::json
WHERE name IN ('and', 'or');

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"a","type":"Object","required":true,"description":"First value","uiType":"draggable"},{"name":"b","type":"Object","required":true,"description":"Second value","uiType":"draggable"}]'::json
WHERE name = 'notEquals';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"boolean","required":true,"description":"Boolean value to negate","uiType":"draggable"}]'::json
WHERE name = 'not';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"condition","type":"boolean","required":true,"description":"Condition to evaluate","uiType":"draggable"},{"name":"trueValue","type":"Object","required":true,"description":"Value if true","uiType":"draggable"},{"name":"falseValue","type":"Object","required":true,"description":"Value if false","uiType":"draggable"}]'::json
WHERE name = 'if';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"condition","type":"boolean","required":true,"description":"Condition to evaluate","uiType":"draggable"},{"name":"trueValue","type":"Object","required":true,"description":"Value if true","uiType":"draggable"}]'::json
WHERE name = 'ifWithoutElse';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to check","uiType":"draggable"}]'::json
WHERE name = 'isNil';

-- Conversion functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to fix","uiType":"draggable"},{"name":"format","type":"String","required":true,"description":"Format specification","uiType":"configurable"}]'::json
WHERE name = 'fixValues';

-- Date functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"format","type":"String","required":false,"description":"Date format","uiType":"configurable"}]'::json
WHERE name = 'currentDate';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"date","type":"String","required":true,"description":"Date to transform","uiType":"draggable"},{"name":"fromFormat","type":"String","required":true,"description":"Source format","uiType":"configurable"},{"name":"toFormat","type":"String","required":true,"description":"Target format","uiType":"configurable"},{"name":"firstWeekday","type":"int","required":false,"description":"First day of week","uiType":"configurable"},{"name":"minDays","type":"int","required":false,"description":"Minimum days in first week","uiType":"configurable"},{"name":"lenient","type":"boolean","required":false,"description":"Lenient parsing","uiType":"configurable"}]'::json
WHERE name = 'dateTrans';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"date1","type":"String","required":true,"description":"First date","uiType":"draggable"},{"name":"date2","type":"String","required":true,"description":"Second date","uiType":"draggable"}]'::json
WHERE name IN ('dateBefore', 'dateAfter', 'compareDates');

-- Node functions
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"condition","type":"boolean","required":true,"description":"Condition to check","uiType":"draggable"},{"name":"value","type":"Object","required":true,"description":"Value to create","uiType":"draggable"}]'::json
WHERE name = 'createIf';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"node","type":"Object","required":true,"description":"Node to process","uiType":"draggable"}]'::json
WHERE name = 'removeContexts';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"node","type":"Object","required":true,"description":"Node to process","uiType":"draggable"},{"name":"oldValue","type":"String","required":true,"description":"Value to replace","uiType":"configurable"},{"name":"newValue","type":"String","required":true,"description":"New value","uiType":"configurable"}]'::json
WHERE name = 'replaceValue';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"node","type":"Object","required":true,"description":"Node to check","uiType":"draggable"}]'::json
WHERE name = 'exists';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"headerName","type":"String","required":true,"description":"Header name","uiType":"configurable"}]'::json
WHERE name = 'getHeader';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"propertyName","type":"String","required":true,"description":"Property name","uiType":"configurable"}]'::json
WHERE name = 'getProperty';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"String","required":true,"description":"Value to split","uiType":"draggable"},{"name":"delimiter","type":"String","required":true,"description":"Delimiter","uiType":"configurable"}]'::json
WHERE name = 'splitByValue';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"contexts","type":"List","required":true,"description":"Contexts to collapse","uiType":"draggable"}]'::json
WHERE name = 'collapseContexts';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"singleField","type":"Object","required":true,"description":"Field that occurs once","uiType":"draggable"},{"name":"countField","type":"String","required":true,"description":"Field to count","uiType":"configurable"},{"name":"multipleField","type":"Object","required":true,"description":"Field that occurs multiple times","uiType":"draggable"}]'::json
WHERE name = 'useOneAsMany';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"values","type":"List","required":true,"description":"Values to sort","uiType":"draggable"}]'::json
WHERE name = 'sort';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"values","type":"List","required":true,"description":"Values to sort","uiType":"draggable"},{"name":"key","type":"String","required":true,"description":"Sort key","uiType":"configurable"}]'::json
WHERE name = 'sortByKey';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to map","uiType":"draggable"},{"name":"defaultValue","type":"Object","required":true,"description":"Default value","uiType":"configurable"}]'::json
WHERE name = 'mapWithDefault';

UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Value to format","uiType":"draggable"},{"name":"example","type":"String","required":true,"description":"Example pattern","uiType":"configurable"}]'::json
WHERE name = 'formatByExample';

-- Constants functions - IMPORTANT: constant function should have configurable parameter, not draggable
UPDATE transformation_custom_functions 
SET parameters = '[{"name":"value","type":"Object","required":true,"description":"Constant value","uiType":"configurable"}]'::json
WHERE name = 'constant';