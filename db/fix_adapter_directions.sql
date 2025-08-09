-- Fix adapter directions to follow the reversed middleware convention
-- SENDER = OUTBOUND (receives from external systems)
-- RECEIVER = INBOUND (sends to external systems)

-- Update all SENDER adapters to have OUTBOUND direction
UPDATE communication_adapters 
SET direction = 'OUTBOUND' 
WHERE mode = 'SENDER';

-- Update all RECEIVER adapters to have INBOUND direction  
UPDATE communication_adapters 
SET direction = 'INBOUND' 
WHERE mode = 'RECEIVER';

-- Show the results
SELECT id, name, type, mode, direction 
FROM communication_adapters 
ORDER BY created_at;