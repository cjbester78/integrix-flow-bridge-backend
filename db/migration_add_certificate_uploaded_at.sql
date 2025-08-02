-- Add missing uploaded_at column to certificates table
ALTER TABLE certificates 
ADD COLUMN uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Update existing records if any
UPDATE certificates 
SET uploaded_at = CURRENT_TIMESTAMP 
WHERE uploaded_at IS NULL;