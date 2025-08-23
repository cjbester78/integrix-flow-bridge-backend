-- Update the flow_structure_messages message_type constraint to match the Java enum values
ALTER TABLE flow_structure_messages 
DROP CONSTRAINT IF EXISTS flow_structure_messages_message_type_check;

ALTER TABLE flow_structure_messages 
ADD CONSTRAINT flow_structure_messages_message_type_check 
CHECK (message_type IN ('INPUT', 'OUTPUT', 'FAULT'));

-- Update any existing data to use the new values
UPDATE flow_structure_messages 
SET message_type = 'INPUT' 
WHERE message_type = 'REQUEST';

UPDATE flow_structure_messages 
SET message_type = 'OUTPUT' 
WHERE message_type = 'RESPONSE';