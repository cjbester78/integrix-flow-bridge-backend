-- Fix any message_structures with null names
UPDATE message_structures 
SET name = COALESCE(name, 'Unnamed Structure ' || id::text) 
WHERE name IS NULL;

-- Ensure name column has NOT NULL constraint
ALTER TABLE message_structures 
ALTER COLUMN name SET NOT NULL;