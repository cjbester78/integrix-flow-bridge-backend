# MySQL to PostgreSQL Migration Guide

## Overview
This guide explains how to migrate data from MySQL to PostgreSQL for the Integrix Flow Bridge application.

## Prerequisites
1. MySQL database with existing data
2. PostgreSQL database already created and initial schema applied
3. Python 3.x installed (for the Python migration script)
4. Required Python packages: `mysql-connector-python`, `psycopg2-binary`

## Migration Steps

### Step 1: Create Missing Tables in PostgreSQL
First, apply the missing tables to your PostgreSQL database:

```bash
cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration
psql -U integrix -d integrixflowbridge -f POSTGRESQL_V2__missing_tables.sql
```

### Step 2: Install Python Dependencies (if using Python script)
```bash
pip install mysql-connector-python psycopg2-binary
```

### Step 3: Run Data Migration

#### Option A: Using Python Script (Recommended - handles BLOB data)
```bash
python3 migrate_mysql_to_postgresql.py
```

The script will:
- Connect to both MySQL and PostgreSQL databases
- Migrate data table by table
- Handle BLOB data for jar_files and certificates
- Show progress for each table

#### Option B: Using Shell Script (for non-BLOB data)
```bash
./migrate_mysql_to_postgresql.sh
```

This will generate SQL files in the `postgresql_seeds` directory that you can review and apply.

### Step 4: Verify Migration
Connect to PostgreSQL and verify the data:

```sql
-- Check record counts
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'transformation_custom_functions', COUNT(*) FROM transformation_custom_functions
UNION ALL
SELECT 'system_settings', COUNT(*) FROM system_settings
UNION ALL
SELECT 'jar_files', COUNT(*) FROM jar_files
UNION ALL
SELECT 'certificates', COUNT(*) FROM certificates
UNION ALL
SELECT 'audit_trail', COUNT(*) FROM audit_trail;
```

## Tables Being Migrated

### Tables with Data Migration:
1. **users** - User accounts (existing table, additional columns added)
2. **transformation_custom_functions** - Custom transformation functions
3. **system_settings** - System configuration settings
4. **jar_files** - JAR file storage (includes BLOB data)
5. **certificates** - SSL/TLS certificates (includes BLOB data)
6. **audit_trail** - Audit trail records (last 10,000 records)

### Tables Created but No Data Migration:
1. **user_sessions** - User session tokens (ignore old data)
2. **user_management_errors** - Error logs (ignore old data)
3. **audit_logs** - Separate audit logging table

## Important Notes

1. **BLOB Data**: The Python script handles BLOB data correctly for jar_files and certificates. The shell script cannot handle BLOB data directly.

2. **UUID Conversion**: All ID fields are being converted from MySQL's CHAR(36) to PostgreSQL's UUID type.

3. **JSON Data**: JSON columns are properly typed in PostgreSQL.

4. **Array Data**: The `adapter_types` field in jar_files is converted to PostgreSQL array type.

5. **Audit Trail**: Only the last 10,000 audit trail records are migrated to keep the data manageable.

## Troubleshooting

### Connection Issues
- Ensure both MySQL and PostgreSQL services are running
- Check credentials in the migration scripts
- Verify network connectivity if databases are remote

### Data Type Mismatches
- The scripts handle most conversions automatically
- Check for any custom data types that may need manual conversion

### Large Data Sets
- For very large tables, consider migrating in batches
- The audit_trail migration is already limited to 10,000 records

## Post-Migration Tasks

1. Update application configuration to use PostgreSQL
2. Test all functionality with migrated data
3. Update any stored procedures or database-specific queries
4. Set up PostgreSQL backups and monitoring