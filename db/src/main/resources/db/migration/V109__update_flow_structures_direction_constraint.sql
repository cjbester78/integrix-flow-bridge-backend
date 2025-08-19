-- Update the flow_structures direction constraint to match the Java enum values
ALTER TABLE flow_structures 
DROP CONSTRAINT IF EXISTS flow_structures_direction_check;

ALTER TABLE flow_structures 
ADD CONSTRAINT flow_structures_direction_check 
CHECK (direction IN ('SOURCE', 'TARGET'));

-- Update any existing data to use the new values
UPDATE flow_structures 
SET direction = 'SOURCE' 
WHERE direction = 'INBOUND';

UPDATE flow_structures 
SET direction = 'TARGET' 
WHERE direction = 'OUTBOUND';

-- Note: BIDIRECTIONAL doesn't have a direct mapping, 
-- so it should be handled on a case-by-case basis if any exist