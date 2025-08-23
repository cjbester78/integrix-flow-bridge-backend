#!/bin/bash
# Archive old MySQL migrations that are no longer needed with PostgreSQL

# Create archive directory
mkdir -p mysql_archive

# Move all old migration files to archive
# These contain JSON columns and MySQL-specific syntax
mv V1__initial_schema.sql mysql_archive/
mv V2__add_xml_mapping_fields.sql mysql_archive/
mv V3__add_mapping_mode.sql mysql_archive/
mv V4__add_deployment_fields.sql mysql_archive/
mv V5__add_performance_indexes.sql mysql_archive/
mv V6__add_event_store.sql mysql_archive/
mv V7__add_system_logs_table.sql mysql_archive/
mv V8__add_transformation_functions.sql mysql_archive/
mv V9__update_builtin_functions_to_java.sql mysql_archive/
mv V101__add_source_type_to_flow_structures.sql mysql_archive/
mv V11__add_function_parameters_column.sql mysql_archive/
mv V12__add_structure_metadata.sql mysql_archive/
mv V13__add_original_content_fields.sql mysql_archive/
mv V14__add_audit_fields.sql mysql_archive/
mv V15__add_flow_name_unique_constraint.sql mysql_archive/
mv V16__fix_updated_at_default.sql mysql_archive/
mv V17__add_visual_flow_data_to_field_mappings.sql mysql_archive/
mv V18__add_mapping_order.sql mysql_archive/
mv V19__add_skip_xml_conversion_to_flows.sql mysql_archive/
mv V20__fix_skip_xml_conversion_not_null.sql mysql_archive/
mv V21__drop_reusable_java_functions_table.sql mysql_archive/
mv V22__add_adapter_payloads_table.sql mysql_archive/
mv V23__split_data_structures.sql mysql_archive/
mv V24__add_flow_structure_columns.sql mysql_archive/

echo "MySQL migrations archived to mysql_archive/"
echo "Only PostgreSQL migrations remain in the migration directory"