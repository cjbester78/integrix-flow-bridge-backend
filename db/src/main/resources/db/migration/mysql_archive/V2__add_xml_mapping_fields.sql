-- V2__add_xml_mapping_fields.sql
-- Add XML mapping support fields to field_mappings table

ALTER TABLE field_mappings
ADD COLUMN source_xpath VARCHAR(1000) DEFAULT NULL COMMENT 'XPath expression for source element',
ADD COLUMN target_xpath VARCHAR(1000) DEFAULT NULL COMMENT 'XPath expression for target element',
ADD COLUMN is_array_mapping BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this is an array-to-array mapping',
ADD COLUMN array_context_path VARCHAR(500) DEFAULT NULL COMMENT 'XPath to array context for iteration',
ADD COLUMN namespace_aware BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether XPath evaluation should be namespace-aware';

-- Add indexes for better query performance
CREATE INDEX idx_field_mappings_array ON field_mappings(is_array_mapping);
CREATE INDEX idx_field_mappings_xpath ON field_mappings(source_xpath(100), target_xpath(100));