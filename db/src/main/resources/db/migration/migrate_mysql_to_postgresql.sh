#!/bin/bash

# MySQL to PostgreSQL Data Migration Script
# This script exports data from MySQL and creates PostgreSQL seed scripts

# Configuration
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_DB="integrixflowbridge"
MYSQL_USER="root"
MYSQL_PASS="B3st3r@01"  # Add password when running

POSTGRES_HOST="localhost"
POSTGRES_PORT="5432"
POSTGRES_DB="integrixflowbridge"
POSTGRES_USER="integrix"
POSTGRES_PASS="B3st3r@01"

OUTPUT_DIR="./postgresql_seeds"
mkdir -p $OUTPUT_DIR

echo "Starting MySQL to PostgreSQL data migration..."

# Function to export MySQL data to CSV
export_mysql_data() {
    local table=$1
    local output_file=$2
    echo "Exporting $table..."
    
    mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB \
        --batch --raw -e "SELECT * FROM $table" | \
        sed 's/\t/,/g' > "$OUTPUT_DIR/${output_file}.csv"
}

# Function to generate PostgreSQL INSERT statements
generate_insert_script() {
    local table=$1
    local csv_file=$2
    local output_file=$3
    
    echo "-- PostgreSQL seed data for $table" > "$OUTPUT_DIR/$output_file"
    echo "-- Generated from MySQL data on $(date)" >> "$OUTPUT_DIR/$output_file"
    echo "" >> "$OUTPUT_DIR/$output_file"
}

# Export and convert users table
echo "Processing users table..."
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT 
    id,
    username,
    password,
    email,
    role_id,
    is_active,
    created_at,
    updated_at,
    last_login,
    login_attempts,
    locked_until
FROM users;" | tail -n +2 | while IFS=$'\t' read -r id username password email role_id is_active created_at updated_at last_login login_attempts locked_until; do
    cat >> "$OUTPUT_DIR/01_users_seed.sql" << EOF
INSERT INTO users (id, username, password, email, role_id, is_active, created_at, updated_at, last_login, login_attempts, locked_until)
VALUES ('$id', '$username', '$password', '$email', ${role_id:-NULL}, ${is_active:-true}, '${created_at:-CURRENT_TIMESTAMP}', '${updated_at:-CURRENT_TIMESTAMP}', ${last_login:+\'$last_login\'}, ${login_attempts:-0}, ${locked_until:+\'$locked_until\'})
ON CONFLICT (id) DO NOTHING;
EOF
done

# Export transformation_custom_functions
echo "Processing transformation_custom_functions table..."
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT 
    function_id,
    name,
    description,
    category,
    language,
    function_signature,
    parameters,
    function_body,
    is_safe,
    is_public,
    is_built_in,
    performance_class,
    version,
    created_by,
    created_at,
    updated_at
FROM transformation_custom_functions;" | tail -n +2 | while IFS=$'\t' read -r function_id name description category language function_signature parameters function_body is_safe is_public is_built_in performance_class version created_by created_at updated_at; do
    # Escape single quotes in function body
    function_body=$(echo "$function_body" | sed "s/'/''/g")
    parameters=$(echo "$parameters" | sed "s/'/''/g")
    
    cat >> "$OUTPUT_DIR/02_transformation_custom_functions_seed.sql" << EOF
INSERT INTO transformation_custom_functions (function_id, name, description, category, language, function_signature, parameters, function_body, is_safe, is_public, is_built_in, performance_class, version, created_by, created_at, updated_at)
VALUES ('$function_id', '$name', ${description:+\'$description\'}, ${category:+\'$category\'}, '$language', '$function_signature', ${parameters:+\'$parameters\'::json}, '$function_body', $is_safe, $is_public, $is_built_in, '$performance_class', $version, ${created_by:+\'$created_by\'}, '$created_at', ${updated_at:+\'$updated_at\'})
ON CONFLICT (function_id) DO NOTHING;
EOF
done

# Export system_settings
echo "Processing system_settings table..."
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT 
    id,
    setting_key,
    setting_value,
    description,
    category,
    data_type,
    is_encrypted,
    is_readonly,
    created_at,
    updated_at,
    created_by,
    updated_by
FROM system_settings;" | tail -n +2 | while IFS=$'\t' read -r id setting_key setting_value description category data_type is_encrypted is_readonly created_at updated_at created_by updated_by; do
    # Escape single quotes
    setting_value=$(echo "$setting_value" | sed "s/'/''/g")
    description=$(echo "$description" | sed "s/'/''/g")
    
    cat >> "$OUTPUT_DIR/03_system_settings_seed.sql" << EOF
INSERT INTO system_settings (id, setting_key, setting_value, description, category, data_type, is_encrypted, is_readonly, created_at, updated_at, created_by, updated_by)
VALUES ('$id', '$setting_key', '$setting_value', ${description:+\'$description\'}, ${category:+\'$category\'}, '${data_type:-STRING}', ${is_encrypted:-false}, ${is_readonly:-false}, '${created_at:-CURRENT_TIMESTAMP}', '${updated_at:-CURRENT_TIMESTAMP}', ${created_by:+\'$created_by\'}, ${updated_by:+\'$updated_by\'})
ON CONFLICT (id) DO NOTHING;
EOF
done

# Export jar_files
echo "Processing jar_files table..."
# For BLOB data, we need a different approach
cat > "$OUTPUT_DIR/04_jar_files_export.sql" << 'EOF'
-- JAR files need to be exported manually due to BLOB data
-- Use this MySQL command to export:
-- SELECT id, file_name, display_name, description, version, file_size, checksum, uploaded_by, uploaded_at, is_active, metadata
-- FROM jar_files;
-- Then manually handle the file_content BLOB field
EOF

# Export certificates
echo "Processing certificates table..."
cat > "$OUTPUT_DIR/05_certificates_export.sql" << 'EOF'
-- Certificates need to be exported manually due to BLOB data
-- Use this MySQL command to export:
-- SELECT id, name, format, type, file_name, password, uploaded_by, uploaded_at
-- FROM certificates;
-- Then manually handle the content BLOB field
EOF

# Export audit_trail
echo "Processing audit_trail table..."
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT 
    id,
    entity_type,
    entity_id,
    action,
    changes,
    user_id,
    user_ip,
    user_agent,
    business_component_id,
    created_at
FROM audit_trail
ORDER BY created_at
LIMIT 10000;" | tail -n +2 | while IFS=$'\t' read -r id entity_type entity_id action changes user_id user_ip user_agent business_component_id created_at; do
    # Escape single quotes in JSON changes
    changes=$(echo "$changes" | sed "s/'/''/g")
    user_agent=$(echo "$user_agent" | sed "s/'/''/g")
    
    cat >> "$OUTPUT_DIR/06_audit_trail_seed.sql" << EOF
INSERT INTO audit_trail (id, entity_type, entity_id, action, changes, user_id, user_ip, user_agent, business_component_id, created_at)
VALUES ('$id', '$entity_type', '$entity_id', '$action', ${changes:+\'$changes\'::json}, ${user_id:+\'$user_id\'}, ${user_ip:+\'$user_ip\'}, ${user_agent:+\'$user_agent\'}, ${business_component_id:+\'$business_component_id\'}, '$created_at')
ON CONFLICT (id) DO NOTHING;
EOF
done

# Create a master seed file
cat > "$OUTPUT_DIR/00_run_all_seeds.sql" << 'EOF'
-- Master seed file for PostgreSQL
-- Run this file to seed all data

\i 01_users_seed.sql
\i 02_transformation_custom_functions_seed.sql
\i 03_system_settings_seed.sql
-- \i 04_jar_files_seed.sql -- Handle manually
-- \i 05_certificates_seed.sql -- Handle manually
\i 06_audit_trail_seed.sql

-- Add any post-seed operations here
-- Update sequences if needed
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
EOF

echo "Migration scripts generated in $OUTPUT_DIR"
echo ""
echo "Next steps:"
echo "1. Review and edit the generated SQL files"
echo "2. Handle BLOB data for jar_files and certificates manually"
echo "3. Run the PostgreSQL migration: psql -U $POSTGRES_USER -d $POSTGRES_DB -f $OUTPUT_DIR/00_run_all_seeds.sql"