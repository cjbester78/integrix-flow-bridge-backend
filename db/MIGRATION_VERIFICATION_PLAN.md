# Migration Verification Plan

## Migrations Created

1. **V122__add_deletion_security_constraints.sql**
   - Adds ON DELETE RESTRICT constraints to prevent deletion of referenced entities
   - Affects: flow_structure_messages, adapter_payloads, integration_flows

2. **V123__migrate_structure_references.sql**
   - Migrates data from deprecated columns to new columns:
     - `source_structure_id` → `source_flow_structure_id`
     - `target_structure_id` → `target_flow_structure_id`
   - Logs any unmigrated references

3. **V124__fix_transformation_order_column.sql**
   - Renames `transformation_order` to `execution_order` in flow_transformations table
   - Sets default value to 1

4. **V125__fix_field_mapping_order.sql**
   - Fixes existing field mappings with mapping_order = 0
   - Assigns sequential order numbers

5. **V126__drop_deprecated_structure_columns.sql**
   - Drops deprecated columns after verification
   - Only runs if all data is migrated

## Verification Steps

### 1. Check Migration Status
```sql
-- Run verify_migrations.sql to check:
-- - Flyway schema history
-- - Data migration status
-- - Column existence
-- - Foreign key constraints
```

### 2. Manual Verification Before Running V126
```sql
-- Check if any data remains in deprecated columns
SELECT COUNT(*) FROM integration_flows 
WHERE (source_structure_id IS NOT NULL AND source_flow_structure_id IS NULL)
   OR (target_structure_id IS NOT NULL AND target_flow_structure_id IS NULL);
```

### 3. Post-Migration Cleanup

After V126 is successfully applied:

1. **Remove deprecated fields from IntegrationFlow entity**:
   - Remove `sourceStructureId` field
   - Remove `targetStructureId` field

2. **Remove deprecated fields from DTOs**:
   - Remove from IntegrationFlowDTO
   - Remove from any request/response DTOs

3. **Update any remaining code references**:
   - FlowCompositionService (already updated)
   - Any mappers or converters

## Testing Plan

1. **Before running migrations**:
   - Backup database
   - Note current state of integration_flows table

2. **After each migration**:
   - Verify migration succeeded in flyway_schema_history
   - Check data integrity
   - Test application functionality

3. **After all migrations**:
   - Verify deprecated columns are removed
   - Test creating new integration flows
   - Test updating existing flows
   - Verify field mappings have proper order

## Rollback Plan

If issues occur:

1. **For V122-V125**: These are mostly additive/update operations
   - Can be manually reversed if needed

2. **For V126**: This drops columns
   - Have backup ready before running
   - If needed, recreate columns and restore data from backup

## Commands to Run

```bash
# 1. Connect to database
psql -U your_user -d integrixflowbridge

# 2. Run verification script
\i verify_migrations.sql

# 3. Check specific migration status
SELECT * FROM flyway_schema_history WHERE version >= '122';

# 4. After all verifications pass, restart application to apply migrations
./deploy.sh
```