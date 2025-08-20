-- Add security constraints to prevent deletion of referenced entities
-- This migration updates all foreign key constraints to use ON DELETE RESTRICT

-- 1. Message Structures Protection
-- Prevent deletion of message_structures referenced by flow_structure_messages
ALTER TABLE flow_structure_messages
DROP CONSTRAINT IF EXISTS fk_flow_structure_messages_message,
ADD CONSTRAINT fk_flow_structure_messages_message 
    FOREIGN KEY (message_structure_id) 
    REFERENCES message_structures(id) 
    ON DELETE RESTRICT;

-- 2. Flow Structures Protection  
-- Prevent deletion of flow_structures referenced by integration_flows
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_flow_structure,
ADD CONSTRAINT fk_flows_source_flow_structure 
    FOREIGN KEY (source_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_flow_structure,
ADD CONSTRAINT fk_flows_target_flow_structure 
    FOREIGN KEY (target_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

-- 3. Adapters Protection
-- Prevent deletion of adapters referenced by integration_flows
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_adapter,
ADD CONSTRAINT fk_flows_source_adapter 
    FOREIGN KEY (source_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_adapter,
ADD CONSTRAINT fk_flows_target_adapter 
    FOREIGN KEY (target_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of adapters referenced by adapter_payloads
ALTER TABLE adapter_payloads
DROP CONSTRAINT IF EXISTS fk_adapter_payloads_adapter,
ADD CONSTRAINT fk_adapter_payloads_adapter 
    FOREIGN KEY (adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of message_structures referenced by adapter_payloads
ALTER TABLE adapter_payloads
DROP CONSTRAINT IF EXISTS fk_adapter_payloads_message_structure,
ADD CONSTRAINT fk_adapter_payloads_message_structure 
    FOREIGN KEY (message_structure_id) 
    REFERENCES message_structures(id) 
    ON DELETE RESTRICT;

-- 4. Integration Flows Protection
-- Prevent deletion of flows with executions
ALTER TABLE flow_executions
DROP CONSTRAINT IF EXISTS fk_executions_flow,
ADD CONSTRAINT fk_executions_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of flows with transformations
ALTER TABLE flow_transformations
DROP CONSTRAINT IF EXISTS fk_transformations_flow,
ADD CONSTRAINT fk_transformations_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of flows with messages
ALTER TABLE messages
DROP CONSTRAINT IF EXISTS fk_messages_flow,
ADD CONSTRAINT fk_messages_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

-- 5. Transformations Protection
-- Prevent deletion of transformations with field mappings
-- NOTE: Skipping this constraint due to type mismatch (character(36) vs uuid)
-- transformation_id needs to be converted to uuid type first

-- 6. Business Components Protection
-- Prevent deletion of business components with flows
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_business_component,
ADD CONSTRAINT fk_flows_business_component 
    FOREIGN KEY (business_component_id) 
    REFERENCES business_components(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of business components with adapters
ALTER TABLE communication_adapters
DROP CONSTRAINT IF EXISTS fk_adapters_business_component,
ADD CONSTRAINT fk_adapters_business_component 
    FOREIGN KEY (business_component_id) 
    REFERENCES business_components(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of business components with message structures
ALTER TABLE message_structures
DROP CONSTRAINT IF EXISTS fk_message_structures_business_component,
ADD CONSTRAINT fk_message_structures_business_component 
    FOREIGN KEY (business_component_id) 
    REFERENCES business_components(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of business components with flow structures
ALTER TABLE flow_structures
DROP CONSTRAINT IF EXISTS fk_flow_structures_business_component,
ADD CONSTRAINT fk_flow_structures_business_component 
    FOREIGN KEY (business_component_id) 
    REFERENCES business_components(id) 
    ON DELETE RESTRICT;

-- 7. User Protection
-- Prevent deletion of users who created entities
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_created_by,
ADD CONSTRAINT fk_flows_created_by 
    FOREIGN KEY (created_by) 
    REFERENCES users(id) 
    ON DELETE RESTRICT;

ALTER TABLE communication_adapters
DROP CONSTRAINT IF EXISTS fk_adapters_created_by,
ADD CONSTRAINT fk_adapters_created_by 
    FOREIGN KEY (created_by) 
    REFERENCES users(id) 
    ON DELETE RESTRICT;

ALTER TABLE message_structures
DROP CONSTRAINT IF EXISTS fk_message_structures_created_by,
ADD CONSTRAINT fk_message_structures_created_by 
    FOREIGN KEY (created_by) 
    REFERENCES users(id) 
    ON DELETE RESTRICT;

ALTER TABLE flow_structures
DROP CONSTRAINT IF EXISTS fk_flow_structures_created_by,
ADD CONSTRAINT fk_flow_structures_created_by 
    FOREIGN KEY (created_by) 
    REFERENCES users(id) 
    ON DELETE RESTRICT;

-- Note: This migration changes all relevant foreign keys to use ON DELETE RESTRICT
-- This prevents deletion of any entity that is still referenced by another entity
-- Users will need to delete entities in the correct order: 
-- Field Mappings -> Transformations -> Flows -> Adapters -> Flow Structures -> Message Structures