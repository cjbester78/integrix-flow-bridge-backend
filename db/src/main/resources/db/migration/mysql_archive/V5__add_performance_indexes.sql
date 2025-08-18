-- V5: Add performance indexes for optimized queries

-- Integration Flows indexes (already added via JPA annotations, but ensuring they exist)
CREATE INDEX IF NOT EXISTS idx_flow_name ON integration_flows(name);
CREATE INDEX IF NOT EXISTS idx_flow_status ON integration_flows(status);
CREATE INDEX IF NOT EXISTS idx_flow_active ON integration_flows(is_active);
CREATE INDEX IF NOT EXISTS idx_flow_source ON integration_flows(source_adapter_id);
CREATE INDEX IF NOT EXISTS idx_flow_target ON integration_flows(target_adapter_id);
CREATE INDEX IF NOT EXISTS idx_flow_created_by ON integration_flows(created_by);
CREATE INDEX IF NOT EXISTS idx_flow_updated_at ON integration_flows(updated_at);

-- Flow Transformations indexes
CREATE INDEX IF NOT EXISTS idx_transform_flow ON flow_transformations(flow_id);
CREATE INDEX IF NOT EXISTS idx_transform_type ON flow_transformations(type);
CREATE INDEX IF NOT EXISTS idx_transform_order ON flow_transformations(flow_id, execution_order);
CREATE INDEX IF NOT EXISTS idx_transform_active ON flow_transformations(is_active);

-- Field Mappings indexes
CREATE INDEX IF NOT EXISTS idx_mapping_transformation ON field_mappings(transformation_id);
CREATE INDEX IF NOT EXISTS idx_mapping_target_field ON field_mappings(target_field);
CREATE INDEX IF NOT EXISTS idx_mapping_active ON field_mappings(is_active);

-- Business Components indexes
CREATE INDEX IF NOT EXISTS idx_business_component_name ON business_components(name);
CREATE INDEX IF NOT EXISTS idx_business_component_active ON business_components(active);

-- Communication Adapters indexes
CREATE INDEX IF NOT EXISTS idx_adapter_business_component ON communication_adapters(business_component_id);
CREATE INDEX IF NOT EXISTS idx_adapter_type ON communication_adapters(adapter_type);
CREATE INDEX IF NOT EXISTS idx_adapter_mode ON communication_adapters(adapter_mode);
CREATE INDEX IF NOT EXISTS idx_adapter_active ON communication_adapters(active);

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);

-- User Sessions indexes
CREATE INDEX IF NOT EXISTS idx_session_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_refresh_token ON user_sessions(refresh_token);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON user_sessions(expires_at);

-- System Logs indexes
CREATE INDEX IF NOT EXISTS idx_log_timestamp ON system_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_log_level ON system_logs(level);
CREATE INDEX IF NOT EXISTS idx_log_component ON system_logs(component);
CREATE INDEX IF NOT EXISTS idx_log_user ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_log_flow ON system_logs(flow_id);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_flow_status_active ON integration_flows(status, is_active);
CREATE INDEX IF NOT EXISTS idx_flow_user_active ON integration_flows(created_by, is_active);
CREATE INDEX IF NOT EXISTS idx_adapter_component_type ON communication_adapters(business_component_id, adapter_type);
CREATE INDEX IF NOT EXISTS idx_log_flow_timestamp ON system_logs(flow_id, timestamp);

-- Full-text indexes for search functionality (MySQL specific)
CREATE FULLTEXT INDEX IF NOT EXISTS ft_flow_name_desc ON integration_flows(name, description);
CREATE FULLTEXT INDEX IF NOT EXISTS ft_component_name_desc ON business_components(name, description);
CREATE FULLTEXT INDEX IF NOT EXISTS ft_adapter_name_desc ON communication_adapters(name, description);