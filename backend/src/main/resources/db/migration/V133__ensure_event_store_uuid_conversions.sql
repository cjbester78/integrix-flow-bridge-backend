-- V133: Ensure all UUID columns in event_store are properly converted
-- Some columns might not have been converted in V132

DO $$
BEGIN
    -- Convert triggered_by if it's still CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='triggered_by' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN triggered_by TYPE UUID USING triggered_by::UUID;
        RAISE NOTICE 'Converted triggered_by to UUID';
    END IF;
    
    -- Convert correlation_id if it's still CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='correlation_id' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN correlation_id TYPE UUID USING correlation_id::UUID;
        RAISE NOTICE 'Converted correlation_id to UUID';
    END IF;
    
    -- Convert causation_id if it's still CHAR
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='event_store' AND column_name='causation_id' 
               AND data_type IN ('character', 'character varying')) THEN
        ALTER TABLE event_store 
        ALTER COLUMN causation_id TYPE UUID USING causation_id::UUID;
        RAISE NOTICE 'Converted causation_id to UUID';
    END IF;
END $$;