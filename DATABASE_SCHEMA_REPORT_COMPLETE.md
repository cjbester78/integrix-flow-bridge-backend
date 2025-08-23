# Integrix Flow Bridge Complete Database Schema Report

## Executive Summary

The Integrix Flow Bridge database consists of **31 tables** that support a comprehensive integration middleware platform. The schema has evolved through migrations, resulting in some redundancy and confusion about field usage. This report documents every table, its purpose, fields, and relationships to clarify the current state of the database.

## Complete Table List (31 Tables)

1. **adapter_payloads** - Links adapters to message structures
2. **audit_logs** - User action audit logs  
3. **audit_trail** - Detailed audit trail
4. **business_components** - Organizations/customers
5. **certificates** - SSL/TLS certificates
6. **communication_adapters** - Adapter configurations
7. **event_store** - Event sourcing records
8. **field_mappings** - Field-level mapping rules
9. **flow_executions** - Flow execution history
10. **flow_structure_messages** - Links flow structures to messages
11. **flow_structure_namespaces** - XML namespaces for flow structures
12. **flow_structure_operations** - WSDL operations
13. **flow_structures** - WSDL/SOAP service definitions
14. **flow_transformations** - Transformation steps
15. **function_test_cases** - Test cases for custom functions
16. **function_test_dependencies** - Function test dependencies
17. **integration_flows** - Main flow definitions
18. **jar_files** - JAR file storage
19. **message_structure_namespaces** - XML namespaces for messages
20. **message_structures** - XML/XSD message definitions
21. **messages** - Message processing records
22. **roles** - User roles
23. **system_configuration** - Environment configuration
24. **system_logs** - Application logs
25. **system_settings** - System-wide settings
26. **transformation_custom_functions** - Custom transformation functions
27. **user_management_errors** - User management error logs
28. **user_roles** - User-role mappings
29. **user_sessions** - Active sessions
30. **users** - System users
31. **xml_field_mappings** - XML-specific field mappings

## Detailed Table Documentation

### 1. adapter_payloads
**Purpose**: Links communication adapters to their input/output message structures
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| adapter_id | UUID | NO | FK to communication_adapters |
| message_structure_id | UUID | NO | FK to message_structures |
| payload_type | ENUM | NO | INPUT/OUTPUT |
| created_at | TIMESTAMP | NO | Creation timestamp |

### 2. audit_logs
**Purpose**: Basic audit logging (legacy, use audit_trail instead)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| action | VARCHAR | NO | Action performed |
| entity_type | VARCHAR | NO | Entity type affected |
| entity_id | VARCHAR | NO | Entity ID affected |
| user_id | UUID | YES | FK to users |
| timestamp | TIMESTAMP | NO | When action occurred |

### 3. audit_trail
**Purpose**: Comprehensive audit trail with before/after values
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| entity_type | VARCHAR | NO | Type of entity |
| entity_id | VARCHAR | NO | ID of entity |
| action | ENUM | NO | CREATE/UPDATE/DELETE/etc. |
| changes | JSON | YES | Before/after values |
| user_id | UUID | YES | FK to users |
| user_ip | VARCHAR | YES | User's IP address |
| created_at | TIMESTAMP | NO | When action occurred |

### 4. business_components
**Purpose**: Organizations/customers using the system
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Organization name |
| description | TEXT | YES | Description |
| industry | VARCHAR | YES | Industry sector |
| contact_email | VARCHAR | YES | Contact email |
| is_active | BOOLEAN | NO | Active status |
| metadata | JSON | YES | Additional metadata |

### 5. certificates
**Purpose**: SSL/TLS certificates for secure communications
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Certificate name |
| type | VARCHAR | NO | Certificate type |
| issuer | VARCHAR | NO | Certificate issuer |
| valid_from | DATE | NO | Valid from date |
| valid_to | DATE | NO | Expiration date |
| content | LONGBLOB | YES | Certificate content |
| private_key | LONGBLOB | YES | Encrypted private key |
| status | ENUM | NO | active/expired/revoked |

### 6. communication_adapters
**Purpose**: Adapter configurations for various protocols
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Adapter name |
| type | ENUM | NO | HTTP/JDBC/FTP/SOAP/etc. |
| mode | ENUM | NO | SENDER/RECEIVER |
| direction | ENUM | NO | INBOUND/OUTBOUND |
| configuration | JSON | NO | Protocol-specific config |
| status | ENUM | NO | active/inactive/error |
| last_test_date | TIMESTAMP | YES | Last connection test |
| last_test_result | JSON | YES | Test results |

### 7. event_store
**Purpose**: Event sourcing for tracking all system events
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| event_id | UUID | NO | Primary key |
| aggregate_type | VARCHAR | NO | Type of aggregate |
| aggregate_id | UUID | NO | ID of aggregate |
| event_type | VARCHAR | NO | Type of event |
| event_data | JSON | NO | Event payload |
| occurred_at | TIMESTAMP | NO | When event occurred |
| triggered_by | UUID | YES | User who triggered |

### 8. field_mappings
**Purpose**: Field-level mapping rules within transformations
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| transformation_id | UUID | NO | FK to flow_transformations |
| source_fields | JSON | NO | Array of source paths |
| target_field | VARCHAR | NO | Target field path |
| mapping_type | ENUM | NO | DIRECT/FUNCTION/CONSTANT |
| transformation_function | TEXT | YES | Function code |
| constant_value | TEXT | YES | For CONSTANT type |

### 9. flow_executions
**Purpose**: Records of flow execution history
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | NO | FK to integration_flows |
| execution_number | BIGINT | NO | Sequential number |
| status | ENUM | NO | RUNNING/SUCCESS/FAILED |
| started_at | TIMESTAMP | NO | Start time |
| completed_at | TIMESTAMP | YES | End time |
| processed_records | INT | NO | Records processed |
| error_message | TEXT | YES | Error if failed |

### 10. flow_structure_messages
**Purpose**: Links flow structures to their request/response messages
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_structure_id | UUID | NO | FK to flow_structures |
| message_structure_id | UUID | NO | FK to message_structures |
| message_type | VARCHAR | NO | REQUEST/RESPONSE |

### 11. flow_structure_namespaces
**Purpose**: XML namespace definitions for flow structures
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_structure_id | UUID | NO | FK to flow_structures |
| prefix | VARCHAR | YES | Namespace prefix |
| uri | VARCHAR | NO | Namespace URI |
| is_default | BOOLEAN | NO | Default namespace flag |

### 12. flow_structure_operations
**Purpose**: WSDL operations within flow structures
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_structure_id | UUID | NO | FK to flow_structures |
| operation_name | VARCHAR | NO | Operation name |
| soap_action | VARCHAR | YES | SOAP action header |
| input_element_name | VARCHAR | YES | Input element |
| output_element_name | VARCHAR | YES | Output element |

### 13. flow_structures
**Purpose**: WSDL/SOAP service definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Service name |
| processing_mode | ENUM | NO | SYNC/ASYNC |
| direction | ENUM | NO | SOURCE/TARGET |
| wsdl_content | TEXT | YES | WSDL content |
| business_component_id | UUID | NO | FK to business_components |

### 14. flow_transformations
**Purpose**: Transformation steps within integration flows
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | NO | FK to integration_flows |
| type | ENUM | NO | FIELD_MAPPING/FILTER/etc. |
| name | VARCHAR | YES | Transformation name |
| configuration | JSON | NO | Config settings |
| execution_order | INT | NO | Execution sequence |

### 15. function_test_cases
**Purpose**: Test cases for custom transformation functions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| function_id | UUID | NO | FK to transformation_custom_functions |
| test_name | VARCHAR | YES | Test case name |
| input_data | TEXT | YES | Test input |
| expected_output | TEXT | YES | Expected result |

### 16. function_test_dependencies
**Purpose**: Dependencies between function test cases
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| test_case_id | UUID | NO | FK to function_test_cases |
| dependency_id | UUID | NO | FK to function_test_cases |

### 17. integration_flows
**Purpose**: Main integration flow definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Flow name |
| flow_type | ENUM | NO | DIRECT_MAPPING/ORCHESTRATION |
| source_adapter_id | UUID | NO | FK to source adapter |
| target_adapter_id | UUID | NO | FK to target adapter |
| source_structure_id | UUID | YES | FK to data_structures (deprecated) |
| target_structure_id | UUID | YES | FK to data_structures (deprecated) |
| status | ENUM | NO | DRAFT/ACTIVE/DEPLOYED |
| deployed_at | TIMESTAMP | YES | Deployment time |

### 18. jar_files
**Purpose**: Storage for JAR files (drivers, libraries)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | JAR name |
| file_name | VARCHAR | NO | File name |
| file_path | VARCHAR | YES | Storage path |
| size_bytes | BIGINT | YES | File size |
| driver_type | VARCHAR | YES | Type of driver |
| vendor | VARCHAR | YES | Vendor name |

### 19. message_structure_namespaces
**Purpose**: XML namespace definitions for message structures
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| message_structure_id | UUID | NO | FK to message_structures |
| prefix | VARCHAR | YES | Namespace prefix |
| uri | VARCHAR | NO | Namespace URI |
| is_default | BOOLEAN | NO | Default namespace flag |

### 20. message_structures
**Purpose**: XML/XSD message definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Structure name |
| xsd_content | TEXT | NO | XSD schema content |
| namespace | JSON | YES | Namespace info |
| business_component_id | UUID | NO | FK to business_components |

### 21. messages
**Purpose**: Individual message processing records
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | YES | FK to integration_flows |
| execution_id | UUID | YES | FK to flow_executions |
| message_type | ENUM | NO | REQUEST/RESPONSE/ERROR |
| direction | ENUM | NO | INBOUND/OUTBOUND |
| status | ENUM | NO | RECEIVED/PROCESSING/PROCESSED |
| content | LONGTEXT | YES | Message content |
| correlation_id | VARCHAR | YES | For correlation |

### 22. roles
**Purpose**: User role definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR | NO | Role name |
| description | TEXT | YES | Role description |
| permissions | JSON | NO | Permission array |

### 23. system_configuration
**Purpose**: Environment-specific configuration
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| config_key | VARCHAR | NO | Configuration key |
| config_value | TEXT | NO | Configuration value |
| config_type | VARCHAR | NO | Value type |
| description | TEXT | YES | Description |

### 24. system_logs
**Purpose**: Application logs
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| timestamp | TIMESTAMP | NO | Log timestamp |
| level | ENUM | NO | DEBUG/INFO/WARN/ERROR |
| logger | VARCHAR | YES | Logger name |
| message | VARCHAR | NO | Log message |
| details | JSON | YES | Additional details |
| user_id | UUID | YES | FK to users |

### 25. system_settings
**Purpose**: System-wide settings (different from system_configuration)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| category | VARCHAR | NO | Setting category |
| key | VARCHAR | NO | Setting key |
| value | TEXT | NO | Setting value |
| data_type | ENUM | NO | string/number/boolean/json |
| is_editable | BOOLEAN | NO | Can be edited |

### 26. transformation_custom_functions
**Purpose**: Custom transformation functions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| function_id | UUID | NO | Primary key |
| name | VARCHAR | NO | Function name |
| language | ENUM | NO | JAVA/JAVASCRIPT/GROOVY |
| function_signature | VARCHAR | NO | Function signature |
| function_body | TEXT | NO | Function code |
| is_built_in | BOOLEAN | NO | Built-in flag |

### 27. user_management_errors
**Purpose**: Logs errors in user management operations
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| error_type | VARCHAR | NO | Type of error |
| error_message | TEXT | NO | Error message |
| user_id | UUID | YES | FK to users |
| occurred_at | TIMESTAMP | NO | When error occurred |
| resolved | BOOLEAN | NO | Resolution status |

### 28. user_roles
**Purpose**: Many-to-many mapping between users and roles
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| user_id | UUID | NO | FK to users |
| role_id | UUID | NO | FK to roles |

### 29. user_sessions
**Purpose**: Active user sessions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| user_id | UUID | NO | FK to users |
| refresh_token | VARCHAR | NO | JWT refresh token |
| expires_at | TIMESTAMP | NO | Session expiration |
| ip_address | VARCHAR | YES | User IP |
| user_agent | TEXT | YES | Browser info |

### 30. users
**Purpose**: System users
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| username | VARCHAR | NO | Username |
| email | VARCHAR | NO | Email address |
| password_hash | VARCHAR | NO | Bcrypt hash |
| first_name | VARCHAR | YES | First name |
| last_name | VARCHAR | YES | Last name |
| status | ENUM | NO | active/inactive/locked |
| role_id | UUID | YES | FK to roles (deprecated) |

### 31. xml_field_mappings
**Purpose**: XML-specific field mappings (legacy)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| transformation_id | UUID | NO | FK to flow_transformations |
| source_xpath | VARCHAR | NO | Source XPath |
| target_xpath | VARCHAR | NO | Target XPath |
| mapping_type | ENUM | NO | Type of mapping |
| mapping_order | INT | NO | Order of execution |

## Key Relationships and Issues

### 1. Structure Table Evolution
The system evolved from a single `data_structures` table to:
- **message_structures** - For XML/XSD message definitions
- **flow_structures** - For WSDL/SOAP service definitions
- **flow_structure_messages** - Links flows to messages
- **flow_structure_namespaces** - XML namespaces for flows
- **message_structure_namespaces** - XML namespaces for messages
- **flow_structure_operations** - WSDL operations

### 2. Redundant Tables
- **audit_logs** vs **audit_trail** - audit_trail is newer and more comprehensive
- **system_settings** vs **system_configuration** - Similar purpose, different schemas
- **field_mappings** vs **xml_field_mappings** - xml_field_mappings is legacy

### 3. Common NULL Fields Pattern
Many fields are NULL because they serve specific purposes:
- **Deployment fields** - Only populated after deployment
- **Error fields** - Only populated on failure  
- **Test fields** - Only populated after testing
- **Resolution fields** - Only populated when issues are resolved

### 4. Missing Direct Relationships
The `integration_flows` table still references old `data_structures` via:
- `source_structure_id` 
- `target_structure_id`

These should reference the new structure tables but haven't been migrated.

## Recommendations

1. **Complete Migration**: Finish migrating from `data_structures` to the new structure tables
2. **Remove Redundancy**: Consolidate redundant tables (audit_logs/audit_trail, system_settings/system_configuration)
3. **Update Foreign Keys**: Update `integration_flows` to properly reference new structure tables
4. **Document NULL Fields**: Add database comments explaining when nullable fields are populated
5. **Clean Up Legacy**: Remove or archive legacy tables like `xml_field_mappings`

## Conclusion

The database contains 31 tables supporting a complex integration platform. The main source of confusion is the incomplete migration from the original `data_structures` design to the new specialized structure tables. Many NULL fields are by design, used only in specific scenarios. The schema would benefit from completing the migration and removing redundant tables.