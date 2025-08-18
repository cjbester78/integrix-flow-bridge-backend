#!/usr/bin/env python3
"""
MySQL to PostgreSQL Data Migration Script
Handles migration of data including BLOB fields
"""

import mysql.connector
import psycopg2
import sys
import base64
from datetime import datetime
import json

# Configuration
MYSQL_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'database': 'integrixflowbridge',
    'user': 'root',
    'password': 'B3st3r@01'  # Set this when running
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
    
    mysql_cursor.execute("""
        SELECT id, username, password_hash, email, role_id, status,
               created_at, updated_at, last_login_at, login_attempts, locked_until
        FROM users
    """)
    
    count = 0
    for row in mysql_cursor:
        try:
            # Map status to is_active
            is_active = row.get('status', 'active') == 'active'
            
            pg_cursor.execute("""
                INSERT INTO users (id, username, password, email, role_id, is_active,
                                  created_at, updated_at, last_login, login_attempts, locked_until)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET
                    username = EXCLUDED.username,
                    password = EXCLUDED.password,
                    email = EXCLUDED.email,
                    is_active = EXCLUDED.is_active
            """, (
                row['id'], row['username'], row['password_hash'], row['email'],
                row['role_id'], is_active,
                row['created_at'], row['updated_at'], row['last_login_at'],
                row.get('login_attempts', 0), row['locked_until']
            ))
            count += 1
        except Exception as e:
            print(f"Error migrating user {row['username']}: {e}")
    
    pg_conn.commit()
    print(f"Migrated {count} users")

def migrate_transformation_custom_functions(mysql_conn, pg_conn):
    """Migrate transformation custom functions"""
    print("\nMigrating transformation_custom_functions...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    mysql_cursor.execute("""
        SELECT function_id, name, description, category, language, function_signature,
               parameters, function_body, is_safe, is_public, is_built_in,
               performance_class, version, created_by, created_at, updated_at
        FROM transformation_custom_functions
    """)
    
    count = 0
    for row in mysql_cursor:
        try:
            # Convert parameters to JSON if it's a string
            parameters = row['parameters']
            if parameters and isinstance(parameters, str):
                try:
                    parameters = json.dumps(json.loads(parameters))
                except:
                    pass
            
            pg_cursor.execute("""
                INSERT INTO transformation_custom_functions 
                (function_id, name, description, category, language, function_signature,
                 parameters, function_body, is_safe, is_public, is_built_in,
                 performance_class, version, created_by, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (function_id) DO NOTHING
            """, (
                row['function_id'], row['name'], row['description'], row['category'],
                row['language'], row['function_signature'], parameters,
                row['function_body'], row['is_safe'], row['is_public'], row['is_built_in'],
                row['performance_class'], row['version'], row['created_by'],
                row['created_at'], row['updated_at']
            ))
            count += 1
        except Exception as e:
            print(f"Error migrating function {row['name']}: {e}")
    
    pg_conn.commit()
    print(f"Migrated {count} transformation custom functions")

def migrate_system_settings(mysql_conn, pg_conn):
    """Migrate system settings"""
    print("\nMigrating system_settings...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    mysql_cursor.execute("""
        SELECT id, setting_key, setting_value, description, category,
               data_type, is_encrypted, is_readonly, created_at, updated_at,
               created_by, updated_by
        FROM system_settings
    """)
    
    count = 0
    for row in mysql_cursor:
        try:
            pg_cursor.execute("""
                INSERT INTO system_settings 
                (id, setting_key, setting_value, description, category,
                 data_type, is_encrypted, is_readonly, created_at, updated_at,
                 created_by, updated_by)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET
                    setting_value = EXCLUDED.setting_value,
                    updated_at = EXCLUDED.updated_at
            """, (
                row['id'], row['setting_key'], row['setting_value'], row['description'],
                row['category'], row.get('data_type', 'STRING'), 
                row.get('is_encrypted', False), row.get('is_readonly', False),
                row['created_at'], row['updated_at'], row['created_by'], row['updated_by']
            ))
            count += 1
        except Exception as e:
            print(f"Error migrating setting {row['setting_key']}: {e}")
    
    pg_conn.commit()
    print(f"Migrated {count} system settings")

def migrate_jar_files(mysql_conn, pg_conn):
    """Migrate JAR files with BLOB data"""
    print("\nMigrating jar_files...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    mysql_cursor.execute("""
        SELECT id, file_name, display_name, description, version,
               file_size, checksum, file_content, adapter_types,
               uploaded_by, uploaded_at, is_active, metadata
        FROM jar_files
    """)
    
    count = 0
    for row in mysql_cursor:
        try:
            # Convert adapter_types string to PostgreSQL array
            adapter_types = None
            if row['adapter_types']:
                if isinstance(row['adapter_types'], str):
                    adapter_types = row['adapter_types'].split(',')
                else:
                    adapter_types = row['adapter_types']
            
            pg_cursor.execute("""
                INSERT INTO jar_files 
                (id, file_name, display_name, description, version,
                 file_size, checksum, file_content, adapter_types,
                 uploaded_by, uploaded_at, is_active, metadata)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
            """, (
                row['id'], row['file_name'], row['display_name'], row['description'],
                row['version'], row['file_size'], row['checksum'], 
                psycopg2.Binary(row['file_content']) if row['file_content'] else None,
                adapter_types, row['uploaded_by'], row['uploaded_at'],
                row.get('is_active', True), row['metadata']
            ))
            count += 1
        except Exception as e:
            print(f"Error migrating JAR file {row['file_name']}: {e}")
    
    pg_conn.commit()
    print(f"Migrated {count} JAR files")

def migrate_certificates(mysql_conn, pg_conn):
    """Migrate certificates with BLOB data"""
    print("\nMigrating certificates...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
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
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
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
    
    pg_conn.commit()
    print(f"Migrated {count} certificates")

def migrate_audit_trail(mysql_conn, pg_conn):
    """Migrate audit trail (limited to recent records)"""
    print("\nMigrating audit_trail (last 10000 records)...")
    mysql_cursor = mysql_conn.cursor(dictionary=True)
    pg_cursor = pg_conn.cursor()
    
    mysql_cursor.execute("""
        SELECT id, entity_type, entity_id, action, changes,
               user_id, user_ip, user_agent, business_component_id, created_at
        FROM audit_trail
        ORDER BY created_at DESC
        LIMIT 10000
    """)
    
    count = 0
    for row in mysql_cursor:
        try:
            # Convert changes to JSON if needed
            changes = row['changes']
            if changes and isinstance(changes, str):
                try:
                    changes = json.dumps(json.loads(changes))
                except:
                    pass
            
            pg_cursor.execute("""
                INSERT INTO audit_trail 
                (id, entity_type, entity_id, action, changes,
                 user_id, user_ip, user_agent, business_component_id, created_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
            """, (
                row['id'], row['entity_type'], row['entity_id'], row['action'],
                changes, row['user_id'], row['user_ip'], row['user_agent'],
                row['business_component_id'], row['created_at']
            ))
            count += 1
        except Exception as e:
            print(f"Error migrating audit trail record: {e}")
    
    pg_conn.commit()
    print(f"Migrated {count} audit trail records")

def main():
    """Main migration function"""
    print("MySQL to PostgreSQL Data Migration")
    print("==================================")
    
    # Get MySQL password if not set
    if not MYSQL_CONFIG['password']:
        MYSQL_CONFIG['password'] = input("Enter MySQL password: ")
    
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