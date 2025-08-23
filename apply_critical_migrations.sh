#!/bin/bash

# Script to apply critical migrations manually
# Run this after checking current database state

echo "🔧 Applying critical migrations to integrixflowbridge database..."
echo "⚠️  Please enter PostgreSQL password when prompted"

# Apply V122 - Deletion security constraints
echo "📝 Applying V122 - Add deletion security constraints..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V122__add_deletion_security_constraints.sql

# Apply V123 - Migrate structure references
echo "📝 Applying V123 - Migrate structure references..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V123__migrate_structure_references.sql

# Apply V124 - Fix field naming
echo "📝 Applying V124 - Fix transformation_order field naming..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V124__fix_transformation_order_field_naming.sql

# Apply V125 - Drop deprecated columns
echo "📝 Applying V125 - Drop deprecated columns..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V125__drop_deprecated_structure_columns.sql

# Apply V126 - Add structure indexes
echo "📝 Applying V126 - Add structure relationship indexes..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V126__add_structure_relationship_indexes.sql

# Apply V127 - Fix field mapping foreign key
echo "📝 Applying V127 - Fix field mapping transformation_id type..."
psql -U integrix -d integrixflowbridge -f /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/db/src/main/resources/db/migration/V127__fix_field_mapping_transformation_id_type.sql

echo "✅ All migrations applied!"
echo ""
echo "🔍 Verifying constraints..."
psql -U integrix -d integrixflowbridge -c "
SELECT 
    constraint_name,
    table_name,
    delete_rule
FROM information_schema.referential_constraints
WHERE constraint_schema = 'public'
AND delete_rule = 'RESTRICT'
ORDER BY table_name;"

echo ""
echo "🔍 Checking field_mappings transformation_id type..."
psql -U integrix -d integrixflowbridge -c "
SELECT 
    column_name,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name = 'field_mappings'
AND column_name = 'transformation_id';"