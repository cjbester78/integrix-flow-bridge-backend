#!/usr/bin/env python3
"""
MySQL to PostgreSQL Data Migration Script with UUID Conversion
Generates new UUIDs for PostgreSQL since MySQL uses non-UUID strings
"""

import mysql.connector
import psycopg2
import sys
import uuid
import json

# Configuration
MYSQL_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'database': 'integrixflowbridge',
    'user': 'root',
    'password': 'B3st3r@01'
}

POSTGRES_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'integrixflowbridge',
    'user': 'integrix',
    'password': 'B3st3r@01'
}

# Keep track of ID mappings for foreign key references
id_mappings = {
    'users': {},
    'business_components': {},
    'transformation_custom_functions': {},
    'system_settings': {},
    'jar_files': {},
    'certificates': {}
}

def is_valid_uuid(val):
    """Check if a string is a valid UUID"""
    try:
        uuid.UUID(str(val))
        return True
    except:
        return False

def get_or_create_uuid(table, old_id):
    """Get existing UUID mapping or create a new one"""
    if not old_id:
        return None
    
    if old_id not in id_mappings[table]:
        if is_valid_uuid(old_id):
            id_mappings[table][old_id] = old_id
        else:
            id_mappings[table][old_id] = str(uuid.uuid4())
    
    return id_mappings[table][old_id]

def connect_mysql():
    """Connect to MySQL database"""
    try:
        conn = mysql.connector.connect(**MYSQL_CONFIG)
        print("Connected to MySQL successfully")
        return conn
    except Exception as e:
        print(f"Error connecting to MySQL: {e}")
        sys.exit(1)

def connect_postgres():
    """Connect to PostgreSQL database"""
    try:
        conn = psycopg2.connect(**POSTGRES_CONFIG)
        conn.autocommit = False
        print("Connected to PostgreSQL successfully")
        return conn
    except Exception as e:
        print(f"Error connecting to PostgreSQL: {e}")
        sys.exit(1)

def migrate_users(mysql_conn, pg_conn):
    """Migrate users table"""
    print("\nMigrating users...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        mysql_cursor.execute("""
            SELECT id, username, password_hash, email, role, status,
                   created_at, updated_at, last_login_at, login_attempts, locked_until
            FROM users
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID for non-UUID IDs
                new_id = get_or_create_uuid('users', row['id'])
                
                # Map status to is_active
                is_active = row.get('status', 'active') == 'active'
                
                # Use role directly if available, otherwise default to 'VIEWER'
                role = row.get('role', 'VIEWER') or 'VIEWER'
                
                pg_cursor.execute("""
                    INSERT INTO users (id, username, password, email, role, is_active,
                                      created_at, updated_at, last_login, login_attempts, locked_until)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (username) DO UPDATE SET
                        password = EXCLUDED.password,
                        email = EXCLUDED.email,
                        role = EXCLUDED.role,
                        is_active = EXCLUDED.is_active
                """, (
                    new_id, row['username'], row['password_hash'], row['email'],
                    role, is_active,
                    row['created_at'], row['updated_at'], row['last_login_at'],
                    row.get('login_attempts', 0), row['locked_until']
                ))
                count += 1
                print(f"  Migrated user: {row['username']} (old ID: {row['id']}, new ID: {new_id})")
            except Exception as e:
                print(f"  Error migrating user {row['username']}: {e}")
                raise
        
        pg_conn.commit()
        print(f"Successfully migrated {count} users")
    except Exception as e:
        print(f"Failed to migrate users: {e}")
        pg_conn.rollback()
        raise

def migrate_transformation_custom_functions(mysql_conn, pg_conn):
    """Migrate transformation custom functions"""
    print("\nMigrating transformation_custom_functions...")
    mysql_cursor = mysql_conn.cursor(dictionary=True, buffered=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        mysql_cursor.execute("""
            SELECT function_id, name, description, category, language, function_signature,
                   parameters, function_body, is_safe, is_public, is_built_in,
                   performance_class, version, created_by, created_at, updated_at
            FROM transformation_custom_functions
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID
                new_id = get_or_create_uuid('transformation_custom_functions', row['function_id'])
                
                # Convert MySQL tinyint(1) to PostgreSQL boolean
                is_safe = bool(row['is_safe']) if row['is_safe'] is not None else False
                is_public = bool(row['is_public']) if row['is_public'] is not None else True
                is_built_in = bool(row['is_built_in']) if row['is_built_in'] is not None else False
                
                # Convert parameters to JSON if it's a string
                parameters = row['parameters']
                if parameters and isinstance(parameters, str):
                    try:
                        json.loads(parameters)
                    except:
                        parameters = None
                
                pg_cursor.execute("""
                    INSERT INTO transformation_custom_functions 
                    (function_id, name, description, category, language, function_signature,
                     parameters, function_body, is_safe, is_public, is_built_in,
                     performance_class, version, created_by, created_at, updated_at)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s::json, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (name) DO UPDATE SET
                        function_body = EXCLUDED.function_body,
                        updated_at = EXCLUDED.updated_at
                """, (
                    new_id, row['name'], row['description'], row['category'],
                    row['language'], row['function_signature'], parameters,
                    row['function_body'], is_safe, is_public, is_built_in,
                    row['performance_class'], row['version'], row['created_by'],
                    row['created_at'], row['updated_at']
                ))
                count += 1
                if count % 10 == 0:
                    print(f"  Migrated {count} functions...")
            except Exception as e:
                print(f"  Error migrating function {row['name']}: {e}")
                raise
        
        pg_conn.commit()
        print(f"Successfully migrated {count} transformation custom functions")
    except Exception as e:
        print(f"Failed to migrate transformation_custom_functions: {e}")
        pg_conn.rollback()
        raise

def migrate_system_settings(mysql_conn, pg_conn):
    """Migrate system settings"""
    print("\nMigrating system_settings...")
    mysql_cursor = mysql_conn.cursor(dictionary=True, buffered=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        mysql_cursor.execute("""
            SELECT id, setting_key, setting_value, description, category,
                   data_type, is_encrypted, is_readonly, created_at, updated_at,
                   created_by, updated_by
            FROM system_settings
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID
                new_id = get_or_create_uuid('system_settings', row['id'])
                
                # Convert MySQL tinyint(1) to PostgreSQL boolean
                is_encrypted = bool(row.get('is_encrypted', 0))
                is_readonly = bool(row.get('is_readonly', 0))
                
                pg_cursor.execute("""
                    INSERT INTO system_settings 
                    (id, setting_key, setting_value, description, category,
                     data_type, is_encrypted, is_readonly, created_at, updated_at,
                     created_by, updated_by)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (setting_key) DO UPDATE SET
                        setting_value = EXCLUDED.setting_value,
                        updated_at = EXCLUDED.updated_at
                """, (
                    new_id, row['setting_key'], row['setting_value'], row['description'],
                    row['category'], row.get('data_type', 'STRING'), 
                    is_encrypted, is_readonly,
                    row['created_at'], row['updated_at'], row['created_by'], row['updated_by']
                ))
                count += 1
            except Exception as e:
                print(f"  Error migrating setting {row['setting_key']}: {e}")
                raise
        
        pg_conn.commit()
        print(f"Successfully migrated {count} system settings")
    except Exception as e:
        print(f"Failed to migrate system_settings: {e}")
        pg_conn.rollback()
        raise

def migrate_jar_files(mysql_conn, pg_conn):
    """Migrate JAR files with BLOB data"""
    print("\nMigrating jar_files...")
    mysql_cursor = mysql_conn.cursor(dictionary=True, buffered=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        # Check available columns
        mysql_cursor.execute("SHOW COLUMNS FROM jar_files")
        columns = {col['Field'] for col in mysql_cursor}
        
        # Build query based on available columns
        select_columns = ['id', 'file_name', 'description', 'version', 'size_bytes', 
                         'checksum', 'uploaded_by', 'created_at']
        
        # Add optional columns if they exist
        for optional in ['is_active', 'adapter_types', 'metadata']:
            if optional in columns:
                select_columns.append(optional)
        
        mysql_cursor.execute(f"SELECT {', '.join(select_columns)} FROM jar_files")
        
        count = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID
                new_id = get_or_create_uuid('jar_files', row['id'])
                
                # Set defaults
                display_name = row.get('display_name', row['file_name'])
                is_active = bool(row.get('is_active', 1))
                
                # Handle adapter_types
                adapter_types = None
                if 'adapter_types' in row and row['adapter_types']:
                    if isinstance(row['adapter_types'], str):
                        adapter_types = row['adapter_types'].split(',')
                
                # Need to read the file content from disk since it's not in the database
                file_content = None
                
                pg_cursor.execute("""
                    INSERT INTO jar_files 
                    (id, file_name, display_name, description, version,
                     file_size, checksum, file_content, adapter_types,
                     uploaded_by, uploaded_at, is_active, metadata)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s::json)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    new_id, row['file_name'], display_name, row.get('description'),
                    row.get('version'), row.get('size_bytes'), row.get('checksum'), 
                    file_content,  # JAR content would need to be loaded from file system
                    adapter_types, row['uploaded_by'], row.get('created_at'),
                    is_active, row.get('metadata')
                ))
                count += 1
                print(f"  Migrated JAR: {row['file_name']}")
            except Exception as e:
                print(f"  Error migrating JAR file {row['file_name']}: {e}")
                raise
        
        pg_conn.commit()
        print(f"Successfully migrated {count} JAR files")
    except Exception as e:
        print(f"Failed to migrate jar_files: {e}")
        pg_conn.rollback()
        raise

def migrate_certificates(mysql_conn, pg_conn):
    """Migrate certificates with BLOB data"""
    print("\nMigrating certificates...")
    mysql_cursor = mysql_conn.cursor(dictionary=True, buffered=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        mysql_cursor.execute("""
            SELECT id, name, format, type, file_name, password,
                   uploaded_by, uploaded_at, content
            FROM certificates
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID
                new_id = get_or_create_uuid('certificates', row['id'])
                
                pg_cursor.execute("""
                    INSERT INTO certificates 
                    (id, name, format, type, file_name, password,
                     uploaded_by, uploaded_at, content)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    new_id, row['name'], row['format'], row['type'],
                    row['file_name'], row['password'], row['uploaded_by'],
                    row['uploaded_at'], 
                    psycopg2.Binary(row['content']) if row['content'] else None
                ))
                count += 1
                print(f"  Migrated certificate: {row['name']}")
            except Exception as e:
                print(f"  Error migrating certificate {row['name']}: {e}")
                raise
        
        pg_conn.commit()
        print(f"Successfully migrated {count} certificates")
    except Exception as e:
        print(f"Failed to migrate certificates: {e}")
        pg_conn.rollback()
        raise

def migrate_audit_trail(mysql_conn, pg_conn):
    """Migrate audit trail (limited to recent records)"""
    print("\nMigrating audit_trail (last 500 records)...")
    mysql_cursor = mysql_conn.cursor(dictionary=True, buffered=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        # First check if we have business_components migrated
        pg_cursor.execute("SELECT COUNT(*) FROM business_components")
        bc_count = pg_cursor.fetchone()[0]
        print(f"  Found {bc_count} business components in PostgreSQL")
        
        mysql_cursor.execute("""
            SELECT id, entity_type, entity_id, action, changes,
                   user_id, user_ip, user_agent, business_component_id, created_at
            FROM audit_trail
            ORDER BY created_at DESC
            LIMIT 500
        """)
        
        count = 0
        skipped = 0
        for row in mysql_cursor:
            try:
                # Generate new UUID for audit trail
                new_id = str(uuid.uuid4())
                
                # Map user_id if it exists
                user_uuid = None
                if row['user_id']:
                    user_uuid = id_mappings['users'].get(row['user_id'])
                    if not user_uuid:
                        skipped += 1
                        continue
                
                # Skip if business_component_id references non-existent component
                bc_uuid = None
                if row['business_component_id'] and bc_count > 0:
                    # For now, skip records with business_component_id since we haven't migrated those
                    skipped += 1
                    continue
                
                # Convert changes to JSON
                changes = row['changes']
                if changes and isinstance(changes, str):
                    try:
                        json.loads(changes)
                    except:
                        changes = None
                
                pg_cursor.execute("""
                    INSERT INTO audit_trail 
                    (id, entity_type, entity_id, action, changes,
                     user_id, user_ip, user_agent, business_component_id, created_at)
                    VALUES (%s::uuid, %s, %s, %s, %s::json, %s::uuid, %s, %s, %s, %s)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    new_id, row['entity_type'], row['entity_id'], row['action'],
                    changes, user_uuid, row['user_ip'], row['user_agent'],
                    bc_uuid, row['created_at']
                ))
                count += 1
            except Exception as e:
                print(f"  Error migrating audit trail record: {e}")
                skipped += 1
        
        pg_conn.commit()
        print(f"Successfully migrated {count} audit trail records (skipped {skipped})")
    except Exception as e:
        print(f"Failed to migrate audit_trail: {e}")
        pg_conn.rollback()

def save_id_mappings():
    """Save ID mappings to a file for reference"""
    print("\nSaving ID mappings...")
    with open('id_mappings.json', 'w') as f:
        json.dump(id_mappings, f, indent=2)
    print("ID mappings saved to id_mappings.json")

def main():
    """Main migration function"""
    print("MySQL to PostgreSQL Data Migration (with UUID conversion)")
    print("=========================================================")
    
    # Connect to databases
    mysql_conn = connect_mysql()
    pg_conn = connect_postgres()
    
    try:
        # Run migrations in order
        migrate_users(mysql_conn, pg_conn)
        migrate_transformation_custom_functions(mysql_conn, pg_conn)
        migrate_system_settings(mysql_conn, pg_conn)
        migrate_jar_files(mysql_conn, pg_conn)
        migrate_certificates(mysql_conn, pg_conn)
        migrate_audit_trail(mysql_conn, pg_conn)
        
        # Save ID mappings
        save_id_mappings()
        
        print("\nMigration completed successfully!")
        print("\nNote: Business components and related tables were not migrated.")
        print("      These will need to be recreated in PostgreSQL.")
        
    except Exception as e:
        print(f"\nMigration failed: {e}")
        pg_conn.rollback()
    finally:
        mysql_conn.close()
        pg_conn.close()

if __name__ == "__main__":
    main()