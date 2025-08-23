# Integration Flow Testing Guide

## Test Scenarios

### 1. Create New Integration Flow
**Steps:**
1. Navigate to http://localhost:8080
2. Login with admin credentials
3. Go to Integration Flows section
4. Click "Create New Flow"
5. Fill in:
   - Flow Name: "Test Flow After Migration"
   - Select Source Adapter (HTTP/REST)
   - Select Target Adapter (Database)
   - Select Source Flow Structure (if available)
   - Select Target Flow Structure (if available)
6. Save the flow

**Expected Result:**
- Flow created successfully
- sourceFlowStructureId and targetFlowStructureId are populated (not the old fields)

### 2. Add Field Mappings
**Steps:**
1. Open the created flow
2. Go to Field Mappings tab
3. Add several field mappings
4. Save the mappings

**Expected Result:**
- Field mappings saved with proper order (1, 2, 3, etc.)
- No mappings with order = 0

### 3. Test Deletion Validation
**Steps:**
1. Create a Message Structure
2. Create a Flow Structure that uses the Message Structure
3. Create an Adapter that uses the Flow Structure
4. Try to delete the Message Structure

**Expected Result:**
- Deletion should be prevented with error message
- System should indicate the structure is in use

### 4. Verify Existing Flows
**Steps:**
1. List all existing integration flows
2. Check that they still display correctly
3. Verify source/target adapters are shown
4. Check field mappings still work

**Expected Result:**
- All flows display properly
- No missing data
- Field mappings show in correct order

## Database Verification Queries

Run these queries to verify the migration results:

```sql
-- Check integration flows have new structure fields
SELECT 
    id, 
    name,
    source_flow_structure_id,
    target_flow_structure_id
FROM integration_flows
LIMIT 5;

-- Verify no field mappings with order = 0
SELECT 
    COUNT(*) as zero_order_count 
FROM field_mappings 
WHERE mapping_order = 0;

-- Check foreign key constraints
SELECT 
    constraint_name,
    delete_rule
FROM information_schema.referential_constraints
WHERE constraint_name LIKE 'fk_%'
AND delete_rule = 'RESTRICT';
```

## API Testing

Test the API endpoints:

```bash
# Get all flows
curl -X GET http://localhost:8080/api/integration-flows \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create new flow
curl -X POST http://localhost:8080/api/integration-flows \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "API Test Flow",
    "description": "Testing after migration",
    "sourceAdapterId": "ADAPTER_ID",
    "targetAdapterId": "ADAPTER_ID",
    "sourceFlowStructureId": "STRUCTURE_ID",
    "targetFlowStructureId": "STRUCTURE_ID"
  }'
```

## Success Criteria

- ✅ Can create new integration flows
- ✅ Field mappings save with proper order
- ✅ Deletion validation prevents orphaned data
- ✅ Existing flows still work
- ✅ No references to old structure fields in UI
- ✅ API returns correct data without deprecated fields