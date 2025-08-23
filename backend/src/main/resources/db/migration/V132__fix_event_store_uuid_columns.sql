-- V132: Fix UUID columns in event_store table
-- Convert CHAR(36) columns to proper UUID type

ALTER TABLE event_store 
ALTER COLUMN event_id TYPE UUID USING event_id::UUID;

-- Check if other UUID columns need conversion
DO $$
BEGIN
    -- Convert aggregate_id if it's CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='aggregate_id' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN aggregate_id TYPE UUID USING aggregate_id::UUID;
    END IF;
    
    -- Convert triggered_by if it's CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='triggered_by' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN triggered_by TYPE UUID USING triggered_by::UUID;
    END IF;
    
    -- Convert correlation_id if it's CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='correlation_id' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN correlation_id TYPE UUID USING correlation_id::UUID;
    END IF;
    
    -- Convert causation_id if it's CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='causation_id' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN causation_id TYPE UUID USING causation_id::UUID;
    END IF;
END $$;