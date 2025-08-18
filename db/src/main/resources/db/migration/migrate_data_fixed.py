#!/usr/bin/env python3
"""
Fixed MySQL to PostgreSQL Data Migration Script
Handles schema differences and data type conversions
"""

import mysql.connector
import psycopg2
import sys
from datetime import datetime
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
        conn.autocommit = False  # Use transactions
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
        # First, let's check what roles exist in MySQL
        mysql_cursor.execute("SELECT DISTINCT role FROM users WHERE role IS NOT NULL")
        roles = [row['role'] for row in mysql_cursor]
        print(f"Found roles: {roles}")
        
        mysql_cursor.execute("""
            SELECT id, username, password_hash, email, role, status,
                   created_at, updated_at, last_login_at, login_attempts, locked_until
            FROM users
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Map status to is_active
                is_active = row.get('status', 'active') == 'active'
                
                # Use role directly if available, otherwise default to 'VIEWER'
                role = row.get('role', 'VIEWER') or 'VIEWER'
                
                pg_cursor.execute("""
                    INSERT INTO users (id, username, password, email, role, is_active,
                                      created_at, updated_at, last_login, login_attempts, locked_until)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (id) DO UPDATE SET
                        username = EXCLUDED.username,
                        password = EXCLUDED.password,
                        email = EXCLUDED.email,
                        role = EXCLUDED.role,
                        is_active = EXCLUDED.is_active
                """, (
                    row['id'], row['username'], row['password_hash'], row['email'],
                    role, is_active,
                    row['created_at'], row['updated_at'], row['last_login_at'],
                    row.get('login_attempts', 0), row['locked_until']
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating user {row['username']}: {e}")
                pg_conn.rollback()
                raise
        
        pg_conn.commit()
        print(f"Migrated {count} users")
    except Exception as e:
        print(f"Failed to migrate users: {e}")
        pg_conn.rollback()

def migrate_transformation_custom_functions(mysql_conn, pg_conn):
    """Migrate transformation custom functions"""
    print("\nMigrating transformation_custom_functions...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
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
                # Convert MySQL tinyint(1) to PostgreSQL boolean
                is_safe = bool(row['is_safe']) if row['is_safe'] is not None else False
                is_public = bool(row['is_public']) if row['is_public'] is not None else True
                is_built_in = bool(row['is_built_in']) if row['is_built_in'] is not None else False
                
                # Convert parameters to JSON if it's a string
                parameters = row['parameters']
                if parameters and isinstance(parameters, str):
                    try:
                        # Validate it's valid JSON
                        json.loads(parameters)
                    except:
                        parameters = None
                
                pg_cursor.execute("""
                    INSERT INTO transformation_custom_functions 
                    (function_id, name, description, category, language, function_signature,
                     parameters, function_body, is_safe, is_public, is_built_in,
                     performance_class, version, created_by, created_at, updated_at)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s::json, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (function_id) DO NOTHING
                """, (
                    row['function_id'], row['name'], row['description'], row['category'],
                    row['language'], row['function_signature'], parameters,
                    row['function_body'], is_safe, is_public, is_built_in,
                    row['performance_class'], row['version'], row['created_by'],
                    row['created_at'], row['updated_at']
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating function {row['name']}: {e}")
                pg_conn.rollback()
                raise
        
        pg_conn.commit()
        print(f"Migrated {count} transformation custom functions")
    except Exception as e:
        print(f"Failed to migrate transformation_custom_functions: {e}")
        pg_conn.rollback()

def migrate_system_settings(mysql_conn, pg_conn):
    """Migrate system settings"""
    print("\nMigrating system_settings...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
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
                # Convert MySQL tinyint(1) to PostgreSQL boolean
                is_encrypted = bool(row.get('is_encrypted', 0))
                is_readonly = bool(row.get('is_readonly', 0))
                
                pg_cursor.execute("""
                    INSERT INTO system_settings 
                    (id, setting_key, setting_value, description, category,
                     data_type, is_encrypted, is_readonly, created_at, updated_at,
                     created_by, updated_by)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (id) DO UPDATE SET
                        setting_value = EXCLUDED.setting_value,
                        updated_at = EXCLUDED.updated_at
                """, (
                    row['id'], row['setting_key'], row['setting_value'], row['description'],
                    row['category'], row.get('data_type', 'STRING'), 
                    is_encrypted, is_readonly,
                    row['created_at'], row['updated_at'], row['created_by'], row['updated_by']
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating setting {row['setting_key']}: {e}")
                pg_conn.rollback()
                raise
        
        pg_conn.commit()
        print(f"Migrated {count} system settings")
    except Exception as e:
        print(f"Failed to migrate system_settings: {e}")
        pg_conn.rollback()

def migrate_jar_files(mysql_conn, pg_conn):
    """Migrate JAR files with BLOB data"""
    print("\nMigrating jar_files...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        # Check MySQL jar_files structure first
        mysql_cursor.execute("SHOW COLUMNS FROM jar_files")
        columns = [col['Field'] for col in mysql_cursor]
        print(f"JAR files columns: {columns}")
        
        # Use available columns
        select_columns = ['id', 'file_name', 'description', 'version', 'file_size', 
                         'checksum', 'file_content', 'uploaded_by', 'uploaded_at']
        if 'is_active' in columns:
            select_columns.append('is_active')
        if 'adapter_types' in columns:
            select_columns.append('adapter_types')
        if 'metadata' in columns:
            select_columns.append('metadata')
            
        mysql_cursor.execute(f"""
            SELECT {', '.join(select_columns)}
            FROM jar_files
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Set defaults for missing columns
                display_name = row.get('display_name', row['file_name'])
                is_active = bool(row.get('is_active', 1))
                
                # Convert adapter_types if present
                adapter_types = None
                if 'adapter_types' in row and row['adapter_types']:
                    if isinstance(row['adapter_types'], str):
                        adapter_types = row['adapter_types'].split(',')
                
                pg_cursor.execute("""
                    INSERT INTO jar_files 
                    (id, file_name, display_name, description, version,
                     file_size, checksum, file_content, adapter_types,
                     uploaded_by, uploaded_at, is_active, metadata)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s::json)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    row['id'], row['file_name'], display_name, row.get('description'),
                    row.get('version'), row.get('file_size'), row.get('checksum'), 
                    psycopg2.Binary(row['file_content']) if row['file_content'] else None,
                    adapter_types, row['uploaded_by'], row['uploaded_at'],
                    is_active, row.get('metadata')
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating JAR file {row['file_name']}: {e}")
                pg_conn.rollback()
                raise
        
        pg_conn.commit()
        print(f"Migrated {count} JAR files")
    except Exception as e:
        print(f"Failed to migrate jar_files: {e}")
        pg_conn.rollback()

def migrate_certificates(mysql_conn, pg_conn):
    """Migrate certificates with BLOB data"""
    print("\nMigrating certificates...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
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
                pg_cursor.execute("""
                    INSERT INTO certificates 
                    (id, name, format, type, file_name, password,
                     uploaded_by, uploaded_at, content)
                    VALUES (%s::uuid, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    row['id'], row['name'], row['format'], row['type'],
                    row['file_name'], row['password'], row['uploaded_by'],
                    row['uploaded_at'], 
                    psycopg2.Binary(row['content']) if row['content'] else None
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating certificate {row['name']}: {e}")
                pg_conn.rollback()
                raise
        
        pg_conn.commit()
        print(f"Migrated {count} certificates")
    except Exception as e:
        print(f"Failed to migrate certificates: {e}")
        pg_conn.rollback()

def migrate_audit_trail(mysql_conn, pg_conn):
    """Migrate audit trail (limited to recent records)"""
    print("\nMigrating audit_trail (last 1000 records)...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    try:
        mysql_cursor.execute("""
            SELECT id, entity_type, entity_id, action, changes,
                   user_id, user_ip, user_agent, business_component_id, created_at
            FROM audit_trail
            ORDER BY created_at DESC
            LIMIT 1000
        """)
        
        count = 0
        for row in mysql_cursor:
            try:
                # Convert changes to JSON if needed
                changes = row['changes']
                if changes and isinstance(changes, str):
                    try:
                        # Validate it's valid JSON
                        json.loads(changes)
                    except:
                        changes = None
                
                pg_cursor.execute("""
                    INSERT INTO audit_trail 
                    (id, entity_type, entity_id, action, changes,
                     user_id, user_ip, user_agent, business_component_id, created_at)
                    VALUES (%s::uuid, %s, %s, %s, %s::json, %s::uuid, %s, %s, %s::uuid, %s)
                    ON CONFLICT (id) DO NOTHING
                """, (
                    row['id'], row['entity_type'], row['entity_id'], row['action'],
                    changes, row['user_id'] if row['user_id'] else None, 
                    row['user_ip'], row['user_agent'],
                    row['business_component_id'] if row['business_component_id'] else None, 
                    row['created_at']
                ))
                count += 1
            except Exception as e:
                print(f"Error migrating audit trail record: {e}")
                # Continue with next record
        
        pg_conn.commit()
        print(f"Migrated {count} audit trail records")
    except Exception as e:
        print(f"Failed to migrate audit_trail: {e}")
        pg_conn.rollback()

def main():
    """Main migration function"""
    print("MySQL to PostgreSQL Data Migration")
    print("==================================")
    
    # Connect to databases
    mysql_conn = connect_mysql()
    pg_conn = connect_postgres()
    
    try:
        # Run migrations
        migrate_users(mysql_conn, pg_conn)
        migrate_transformation_custom_functions(mysql_conn, pg_conn)
        migrate_system_settings(mysql_conn, pg_conn)
        migrate_jar_files(mysql_conn, pg_conn)
        migrate_certificates(mysql_conn, pg_conn)
        migrate_audit_trail(mysql_conn, pg_conn)
        
        print("\nMigration completed successfully!")
        
    except Exception as e:
        print(f"\nMigration failed: {e}")
        pg_conn.rollback()
    finally:
        mysql_conn.close()
        pg_conn.close()

if __name__ == "__main__":
    main()