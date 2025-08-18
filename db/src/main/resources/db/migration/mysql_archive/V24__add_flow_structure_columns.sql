-- Add flow structure columns to integration_flows table
ALTER TABLE `integration_flows` 
ADD COLUMN `source_flow_structure_id` VARCHAR(36) AFTER `target_adapter_id`,
ADD COLUMN `target_flow_structure_id` VARCHAR(36) AFTER `source_flow_structure_id`;

-- Add indexes for performance
CREATE INDEX idx_flow_source_flow_structure ON integration_flows(source_flow_structure_id);
CREATE INDEX idx_flow_target_flow_structure ON integration_flows(target_flow_structure_id);

-- Add foreign key constraints
ALTER TABLE `integration_flows`
ADD CONSTRAINT `fk_integration_flows_source_flow_structure` 
    FOREIGN KEY (`source_flow_structure_id`) REFERENCES `flow_structures` (`id`),
ADD CONSTRAINT `fk_integration_flows_target_flow_structure` 
    FOREIGN KEY (`target_flow_structure_id`) REFERENCES `flow_structures` (`id`);