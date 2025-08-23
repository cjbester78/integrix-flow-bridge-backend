-- V136: Fix CHAR columns in system_logs table to VARCHAR
-- PostgreSQL treats CHAR(36) as bpchar which Hibernate doesn't handle well

-- Convert CHAR columns to VARCHAR to match Hibernate expectations
ALTER TABLE system_logs 
ALTER COLUMN component_id TYPE VARCHAR(36);

ALTER TABLE system_logs 
ALTER COLUMN source_id TYPE VARCHAR(36);

ALTER TABLE system_logs 
ALTER COLUMN domain_reference_id TYPE VARCHAR(36);