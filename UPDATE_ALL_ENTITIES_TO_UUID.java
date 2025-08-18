// This file shows all the changes needed to update entities to use UUID

// For ALL entities with String id, apply these changes:

// 1. Add import
import java.util.UUID;

// 2. Change ID field from:
@Id
@GeneratedValue(generator = "uuid2")
@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
@Column(columnDefinition = "char(36)")
@EqualsAndHashCode.Include
private String id;

// To:
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@EqualsAndHashCode.Include
private UUID id;

// 3. For any field that references another entity ID, change from String to UUID:
// From:
private String businessComponentId;
// To:
private UUID businessComponentId;

// 4. For createdBy/updatedBy fields, change from String to User entity:
// From:
@Column(name = "created_by")
private String createdBy;
// To:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "created_by")
private User createdBy;

// 5. Remove GenericGenerator import if no longer needed
// Remove: import org.hibernate.annotations.GenericGenerator;

// 6. In repository interfaces, change from:
public interface SomeRepository extends JpaRepository<SomeEntity, String> {
// To:
public interface SomeRepository extends JpaRepository<SomeEntity, UUID> {

// 7. In service methods, change parameter types:
// From:
public SomeEntity findById(String id) {
// To:
public SomeEntity findById(UUID id) {

// 8. When parsing IDs from strings:
// From:
String id = request.getId();
// To:
UUID id = UUID.fromString(request.getId());

// Entities that need updating:
// - BusinessComponent
// - MessageStructure
// - FlowStructure
// - IntegrationFlow
// - CommunicationAdapter
// - FlowTransformation
// - FieldMapping (migrate to XmlFieldMapping)
// - SystemLog
// - AdapterPayload
// - Role
// - AuditTrail
// - SystemSetting
// - Certificate
// - DataStructure
// - UserSession
// - FlowExecution
// - Message
// - FlowStructureMessage