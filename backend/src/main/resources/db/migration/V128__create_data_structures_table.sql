-- V128: Create data_structures table
-- This table was missing from the initial schema but is required by the DataStructure entity

CREATE TABLE IF NOT EXISTS data_structures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    version VARCHAR(50),
    content TEXT,
    metadata JSONB,
    parent_id UUID,
    business_component_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_data_structures_parent
        FOREIGN KEY (parent_id) 
        REFERENCES data_structures(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_data_structures_business_component
        FOREIGN KEY (business_component_id) 
        REFERENCES business_components(id) 
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_data_structures_name ON data_structures(name);
CREATE INDEX IF NOT EXISTS idx_data_structures_type ON data_structures(type);
CREATE INDEX IF NOT EXISTS idx_data_structures_parent ON data_structures(parent_id);
CREATE INDEX IF NOT EXISTS idx_data_structures_business_component ON data_structures(business_component_id);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_data_structures_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_data_structures_updated_at_trigger
    BEFORE UPDATE ON data_structures
    FOR EACH ROW
    EXECUTE FUNCTION update_data_structures_updated_at();