-- Update the communication_adapters direction constraint to allow INBOUND/OUTBOUND values
ALTER TABLE communication_adapters 
DROP CONSTRAINT IF EXISTS communication_adapters_direction_check;

ALTER TABLE communication_adapters 
ADD CONSTRAINT communication_adapters_direction_check 
CHECK (direction IN ('INBOUND', 'OUTBOUND', 'BIDIRECTIONAL'));

-- Note: The mode column should remain SENDER/RECEIVER as it represents the adapter mode
-- The direction column represents the data flow direction