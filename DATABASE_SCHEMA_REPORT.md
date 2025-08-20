# Integrix Flow Bridge Database Schema Report

## Executive Summary

The Integrix Flow Bridge database consists of multiple interconnected tables that support a comprehensive integration middleware platform. The schema has evolved through migrations, resulting in some redundancy and confusion about field usage. This report documents every table, its purpose, fields, and relationships to clarify the current state of the database.

## Table Categories

### 1. Core User & Authentication Tables
- **users** - System users and authentication
- **roles** - User roles and permissions
- **user_sessions** - Active user sessions

### 2. Business Entity Tables
- **business_components** - Organizations/customers using the system

### 3. Security & Infrastructure Tables
- **certificates** - SSL/TLS certificates for secure communications
- **jar_files** - JAR files for database drivers and custom adapters
- **system_settings** - System-wide configuration settings
- **system_configuration** - Environment-specific configurations

### 4. Data Structure Tables
- **data_structures** - Legacy table (being phased out)
- **message_structures** - XML/XSD message definitions
- **flow_structures** - WSDL/SOAP service definitions
- **flow_structure_messages** - Links flow structures to message structures

### 5. Integration Core Tables
- **communication_adapters** - Adapter configurations (HTTP, JDBC, FTP, etc.)
- **integration_flows** - Main flow definitions linking source and target
- **flow_transformations** - Transformation steps within flows
- **field_mappings** - Field-level mapping rules

### 6. Execution & Monitoring Tables
- **flow_executions** - Flow execution history
- **messages** - Message processing records
- **system_logs** - Application logs
- **event_store** - Event sourcing records

### 7. Analytics Tables
- **flow_statistics** - Flow performance metrics
- **adapter_statistics** - Adapter performance metrics

### 8. Additional Tables
- **channels** - Logical grouping of adapters
- **audit_trail** - User action audit logs
- **scheduled_jobs** - Scheduled flow executions
- **transformation_custom_functions** - Custom transformation functions
- **adapter_payloads** - Links adapters to message structures

## Detailed Table Documentation

### users
**Purpose**: Stores system users and authentication information
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| username | VARCHAR(50) | NO | Unique username |
| email | VARCHAR(255) | NO | User email |
| password_hash | VARCHAR(255) | NO | Bcrypt password hash |
| first_name | VARCHAR(100) | YES | User's first name |
| last_name | VARCHAR(100) | YES | User's last name |
| role_id | UUID | YES | FK to roles table |
| status | ENUM | NO | active/inactive/pending/locked |
| last_login_at | TIMESTAMP | YES | Last login timestamp |
| created_at | TIMESTAMP | NO | Creation timestamp |
| updated_at | TIMESTAMP | NO | Last update timestamp |

### roles
**Purpose**: Defines user roles and their permissions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(50) | NO | Role name (ADMINISTRATOR, DEVELOPER, etc.) |
| description | TEXT | YES | Role description |
| permissions | JSON | NO | Array of permission strings |

### business_components
**Purpose**: Represents organizations/customers in the system
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(255) | NO | Organization name |
| description | TEXT | YES | Description |
| industry | VARCHAR(100) | YES | Industry sector |
| contact_email | VARCHAR(255) | YES | Contact email |
| is_active | BOOLEAN | NO | Active status |
| metadata | JSON | YES | Additional metadata |

### communication_adapters
**Purpose**: Defines adapters for various protocols (HTTP, JDBC, FTP, etc.)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(255) | NO | Adapter name |
| type | ENUM | NO | HTTP/JDBC/FTP/SOAP/etc. |
| mode | ENUM | NO | SENDER/RECEIVER/BIDIRECTIONAL |
| direction | ENUM | NO | INBOUND/OUTBOUND/BIDIRECTIONAL |
| configuration | JSON | NO | Adapter-specific settings |
| status | ENUM | NO | active/inactive/error/testing |
| business_component_id | UUID | YES | FK to business_components |

**Common NULL fields**: 
- `last_test_date`, `last_test_result` - Only populated after connection tests

### integration_flows
**Purpose**: Main integration flow definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(255) | NO | Flow name |
| flow_type | ENUM | NO | DIRECT_MAPPING/ORCHESTRATION |
| source_adapter_id | UUID | NO | FK to source adapter |
| target_adapter_id | UUID | NO | FK to target adapter |
| source_structure_id | UUID | YES | FK to data structures (deprecated) |
| target_structure_id | UUID | YES | FK to data structures (deprecated) |
| status | ENUM | NO | DRAFT/ACTIVE/DEPLOYED/etc. |
| version | INT | NO | Flow version |
| deployed_version | INT | YES | Currently deployed version |
| deployed_at | TIMESTAMP | YES | Deployment timestamp |

**Common NULL fields**:
- `source_structure_id`, `target_structure_id` - These reference the old `data_structures` table and are often NULL because the system now uses `message_structures` and `flow_structures`
- `deployed_version`, `deployed_at`, `deployed_by` - Only populated after deployment

### flow_transformations
**Purpose**: Transformation steps within a flow
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | NO | FK to integration_flows |
| type | ENUM | NO | FIELD_MAPPING/FILTER/ENRICHMENT/etc. |
| name | VARCHAR(255) | YES | Transformation name |
| configuration | JSON | NO | Transformation settings |
| execution_order | INT | NO | Order of execution (1-based) |
| is_active | BOOLEAN | NO | Active status |

### field_mappings
**Purpose**: Field-level mapping rules within transformations
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| transformation_id | UUID | NO | FK to flow_transformations |
| source_fields | JSON | NO | Array of source field paths |
| target_field | VARCHAR(500) | NO | Target field path |
| mapping_type | ENUM | NO | DIRECT/FUNCTION/CONSTANT/CONDITIONAL |
| transformation_function | TEXT | YES | JavaScript/Java function code |
| function_type | ENUM | YES | JAVASCRIPT/JAVA/BUILTIN |
| constant_value | TEXT | YES | For CONSTANT mappings |

**Common NULL fields**:
- `transformation_function` - Only used for FUNCTION type mappings
- `constant_value` - Only used for CONSTANT type mappings
- `conditions`, `data_type_conversion` - Advanced features, often NULL

### message_structures
**Purpose**: XML/XSD message definitions (replaces data_structures for XML)
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(255) | NO | Structure name |
| xsd_content | TEXT | NO | XSD schema content |
| namespace | JSON | YES | XML namespace information |
| metadata | JSON | YES | Additional metadata |
| business_component_id | UUID | NO | FK to business_components |

### flow_structures
**Purpose**: WSDL/SOAP service definitions
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| name | VARCHAR(255) | NO | Service name |
| processing_mode | ENUM | NO | SYNC/ASYNC |
| direction | ENUM | NO | SOURCE/TARGET |
| wsdl_content | TEXT | YES | WSDL content |
| namespace | JSON | YES | Namespace information |
| business_component_id | UUID | NO | FK to business_components |

### flow_executions
**Purpose**: Records of flow execution history
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | NO | FK to integration_flows |
| execution_number | BIGINT | NO | Sequential execution number |
| status | ENUM | NO | RUNNING/SUCCESS/FAILED/etc. |
| started_at | TIMESTAMP | NO | Start timestamp |
| completed_at | TIMESTAMP | YES | Completion timestamp |
| processed_records | INT | NO | Number of records processed |
| error_message | TEXT | YES | Error details if failed |

**Common NULL fields**:
- `completed_at`, `execution_time_ms` - NULL while running
- `error_message`, `error_stack_trace` - Only populated on failure
- `input_message_id`, `output_message_id` - Reference to messages table

### messages
**Purpose**: Individual message processing records
| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| id | UUID | NO | Primary key |
| flow_id | UUID | YES | FK to integration_flows |
| execution_id | UUID | YES | FK to flow_executions |
| message_type | ENUM | NO | REQUEST/RESPONSE/ERROR/etc. |
| direction | ENUM | NO | INBOUND/OUTBOUND |
| status | ENUM | NO | RECEIVED/PROCESSING/PROCESSED/FAILED |
| content | LONGTEXT | YES | Message content |
| correlation_id | VARCHAR(255) | YES | For message correlation |

## Key Issues Identified

### 1. Redundant Structure Tables
- `data_structures` - Original table, being phased out
- `message_structures` - For XML/XSD definitions
- `flow_structures` - For WSDL/SOAP services

The `source_structure_id` and `target_structure_id` in `integration_flows` still reference the old `data_structures` table, causing confusion.

### 2. Commonly NULL Fields
Many fields are NULL because they're used for specific scenarios:
- Deployment fields - Only populated after deployment
- Error fields - Only populated on failure
- Test fields - Only populated after testing
- Optional metadata fields - Used as needed

### 3. Missing Direct Relationships
- Flows don't directly reference `message_structures` or `flow_structures`
- The relationship is indirect through adapters and adapter_payloads

## Recommendations

1. **Migration Needed**: Update `integration_flows` to properly reference the new structure tables
2. **Documentation**: Add comments to nullable fields explaining when they're populated
3. **Cleanup**: Remove or properly migrate away from `data_structures` table
4. **Validation**: Add constraints to ensure data integrity between related tables

## Conclusion

The database schema supports a complex integration platform but has accumulated technical debt through migrations. The main confusion stems from the evolution of structure storage (from `data_structures` to separate `message_structures` and `flow_structures` tables) and many nullable fields that are only used in specific scenarios.