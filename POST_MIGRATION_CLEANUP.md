# Post-Migration Cleanup Tasks

After verifying that migration V126 has been successfully applied and the deprecated columns have been dropped, perform the following cleanup:

## 1. Remove Deprecated Fields from IntegrationFlow Entity

File: `/data-access/src/main/java/com/integrixs/data/model/IntegrationFlow.java`

Remove:
```java
/**
 * Source data structure ID (deprecated)
 * @deprecated Use sourceFlowStructureId instead
 */
@Column(name = "source_structure_id")
@Deprecated
private UUID sourceStructureId;

/**
 * Target data structure ID (deprecated)
 * @deprecated Use targetFlowStructureId instead
 */
@Column(name = "target_structure_id")
@Deprecated
private UUID targetStructureId;
```

## 2. Remove Deprecated Fields from IntegrationFlowDTO

File: `/shared-lib/src/main/java/com/integrixs/shared/dto/flow/IntegrationFlowDTO.java`

Remove:
```java
/**
 * Source data structure ID (deprecated - use sourceFlowStructureId)
 * @deprecated Use sourceFlowStructureId instead
 */
@Deprecated
private String sourceStructureId;

/**
 * Target data structure ID (deprecated - use targetFlowStructureId)
 * @deprecated Use targetFlowStructureId instead
 */
@Deprecated
private String targetStructureId;
```

## 3. Remove from Request/Response DTOs

Check and remove from:
- `DirectMappingFlowRequest` in FlowCompositionService
- `UpdateFlowRequest` in FlowCompositionService
- Any other DTOs that might have these fields

## 4. Search for Any Remaining References

Run these searches to find any remaining references:
```bash
# Search for sourceStructureId
grep -r "sourceStructureId" --include="*.java" .

# Search for targetStructureId
grep -r "targetStructureId" --include="*.java" .

# Search for source_structure_id
grep -r "source_structure_id" --include="*.java" --include="*.sql" .

# Search for target_structure_id
grep -r "target_structure_id" --include="*.java" --include="*.sql" .
```

## 5. Update Tests

Update any unit tests or integration tests that might be using these deprecated fields.

## 6. Final Verification

After cleanup:
1. Run `mvn clean compile` to ensure no compilation errors
2. Run `mvn test` to ensure all tests pass
3. Deploy and test the application
4. Create new integration flows to verify everything works

## 7. Commit the Cleanup

```bash
git add -A
git commit -m "Remove deprecated structure fields after migration

- Removed sourceStructureId and targetStructureId from IntegrationFlow entity
- Removed deprecated fields from IntegrationFlowDTO
- Cleaned up any remaining references
- All data has been migrated to sourceFlowStructureId and targetFlowStructureId"
```