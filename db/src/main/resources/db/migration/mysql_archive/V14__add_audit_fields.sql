-- Add audit fields to tables that are missing them

-- Add updated_by to integration_flows (rename last_modified_by to updated_by for consistency)
ALTER TABLE integration_flows 
    CHANGE COLUMN last_modified_by updated_by VARCHAR(36),
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

-- Add created_by and updated_by to flow_transformations
ALTER TABLE flow_transformations
    ADD COLUMN created_by VARCHAR(36) AFTER is_active,
    ADD COLUMN updated_by VARCHAR(36) AFTER created_by,
    ADD CONSTRAINT fk_transformations_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_transformations_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add created_by and updated_by to field_mappings
ALTER TABLE field_mappings
    ADD COLUMN created_by VARCHAR(36) AFTER is_required,
    ADD COLUMN updated_by VARCHAR(36) AFTER created_by,
    ADD CONSTRAINT fk_mappings_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_mappings_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add audit fields to communication_adapters if missing
ALTER TABLE communication_adapters
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36) AFTER created_by,
    ADD CONSTRAINT IF NOT EXISTS fk_adapters_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add audit fields to data_structures if missing  
ALTER TABLE data_structures
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36) AFTER created_by,
    ADD CONSTRAINT IF NOT EXISTS fk_structures_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add indexes for audit fields
CREATE INDEX IF NOT EXISTS idx_flows_updated_by ON integration_flows(updated_by);
CREATE INDEX IF NOT EXISTS idx_transformations_created_by ON flow_transformations(created_by);
CREATE INDEX IF NOT EXISTS idx_transformations_updated_by ON flow_transformations(updated_by);
CREATE INDEX IF NOT EXISTS idx_mappings_created_by ON field_mappings(created_by);
CREATE INDEX IF NOT EXISTS idx_mappings_updated_by ON field_mappings(updated_by);