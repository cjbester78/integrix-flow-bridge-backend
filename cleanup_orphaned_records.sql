-- Cleanup script for orphaned flow_structure_messages records
-- This will remove references to message structures that need to be deleted

-- First, let's check what we're dealing with
SELECT 'Checking message structures to be cleaned up...' as status;

-- Find the message structure IDs
SELECT id, name FROM message_structures 
WHERE name IN ('CelsiusToFahrenheit', 'CelsiusToFahrenheitResponse', 'CelsiusToFahrenheitFault');

-- Check which flow structures are using these message structures
SELECT 
    fs.id as flow_structure_id,
    fs.name as flow_structure_name,
    fs.is_active,
    ms.id as message_structure_id,
    ms.name as message_structure_name,
    fsm.message_type
FROM flow_structure_messages fsm
JOIN flow_structures fs ON fs.id = fsm.flow_structure_id
JOIN message_structures ms ON ms.id = fsm.message_structure_id
WHERE ms.name IN ('CelsiusToFahrenheit', 'CelsiusToFahrenheitResponse', 'CelsiusToFahrenheitFault');

-- Delete orphaned flow_structure_messages records
-- This will remove the foreign key constraint violations
DELETE fsm FROM flow_structure_messages fsm
JOIN message_structures ms ON ms.id = fsm.message_structure_id
WHERE ms.name IN ('CelsiusToFahrenheit', 'CelsiusToFahrenheitResponse', 'CelsiusToFahrenheitFault');

-- Now you should be able to delete the message structures
-- First verify the cleanup worked
SELECT 
    COUNT(*) as remaining_references 
FROM flow_structure_messages fsm
JOIN message_structures ms ON ms.id = fsm.message_structure_id
WHERE ms.name IN ('CelsiusToFahrenheit', 'CelsiusToFahrenheitResponse', 'CelsiusToFahrenheitFault');

-- If remaining_references is 0, you can now safely delete the message structures through the UI
SELECT 'Cleanup complete. You can now delete the message structures through the UI.' as status;