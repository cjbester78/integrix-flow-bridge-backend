# Flyway Database Migration Configuration

## Overview

Flyway has been configured to automatically manage database migrations for the Integrix Flow Bridge application.

## Configuration Details

### 1. Dependencies Added
- `flyway-core` - Core Flyway functionality
- Removed `flyway-mysql` as we're using PostgreSQL

### 2. Application Configuration (`application.yml`)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 121
    locations: classpath:db/migration
    sql-migration-prefix: V
    sql-migration-separator: __
    sql-migration-suffixes: .sql
    validate-on-migrate: true
    clean-disabled: true
    schemas: integrixflowbridge
```

### 3. Migration Files Location
- Source: `/db/src/main/resources/db/migration/`
- Runtime: `/backend/src/main/resources/db/migration/`

### 4. Key Migrations
- **V122** - Add deletion security constraints (ON DELETE RESTRICT)
- **V123** - Migrate structure references to new flow structure columns
- **V124** - Fix transformation_order field naming
- **V125** - Drop deprecated structure columns
- **V126** - Add structure relationship indexes
- **V127** - Fix field_mappings.transformation_id type (character to uuid)

### 5. FlywayConfig Class
Created a custom configuration class that:
- Provides enhanced logging
- Runs repair before migration
- Handles migration failures gracefully

## How It Works

1. **On Application Startup**: Flyway checks the `flyway_schema_history` table
2. **Compares Versions**: Identifies which migrations haven't been applied
3. **Runs Migrations**: Executes pending migrations in order
4. **Updates History**: Records successful migrations

## Benefits

1. **Automatic Schema Management**: No need to manually run SQL scripts
2. **Version Control**: All schema changes are versioned and tracked
3. **Rollback Safety**: Failed migrations don't leave the database in an inconsistent state
4. **Team Collaboration**: Everyone gets the same schema updates automatically

## Monitoring

Check migration status:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Troubleshooting

If migrations fail:
1. Check application logs for detailed error messages
2. Verify PostgreSQL connectivity
3. Ensure migration scripts are valid PostgreSQL syntax
4. Check for conflicting manual schema changes

## Next Steps

After deployment with Flyway:
1. Monitor the logs to confirm all migrations ran successfully
2. Verify the field_mappings constraints are in place
3. Test the deletion constraints work as expected
4. Confirm field mapping order values are properly maintained