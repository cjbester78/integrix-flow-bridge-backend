# Role Permissions Matrix

## Overview
Integrix Flow Bridge implements a role-based access control (RBAC) system with 4 distinct roles, each with specific permissions and access levels.

## Roles Summary

| Role | Description | Key Access |
|------|-------------|------------|
| **Administrator** | Full admin access to all pages and content | Everything including system settings |
| **Developer** | Same as Administrator EXCEPT no system settings | All features except system configuration |
| **Integrator** | Execute-only access for running integrations | Only execute flows and access APIs |
| **Viewer** | Read-only access for monitoring | View flows, logs, and monitoring data |

## Detailed Permission Matrix

### Administrator Role
- **Full access to all system features**
- System settings and configuration
- User and role management
- All CRUD operations on flows, adapters, structures
- Development functions and tools
- Message monitoring and reprocessing
- System logs and audit trails

### Developer Role
- **Everything Administrator can do EXCEPT:**
  - ❌ System settings and configuration (`/api/system-settings/**`)
  - ❌ System-wide configuration changes
- ✅ Create, read, update, delete flows
- ✅ Manage adapters and data structures
- ✅ Development functions and tools
- ✅ Message monitoring and reprocessing
- ✅ User management

### Integrator Role
- **Execute-only permissions:**
  - ✅ Execute message flows (`flows:execute`)
  - ✅ Access integration APIs (`api:access`)
  - ✅ Reprocess failed messages (`/api/messages/{id}/reprocess`)
- ❌ No create, update, or delete permissions
- ❌ No access to configuration or settings
- ❌ No access to development tools

### Viewer Role
- **Read-only access:**
  - ✅ View flows and deployment status
  - ✅ View adapters and data structures
  - ✅ View field mappings
  - ✅ View system logs
  - ✅ View messages and statistics
  - ✅ Access dashboard and monitoring
- ❌ No create, update, delete, or execute permissions

## API Endpoint Access by Role

| Endpoint Pattern | Administrator | Developer | Integrator | Viewer |
|-----------------|---------------|-----------|------------|---------|
| `/api/system-settings/**` | ✅ | ❌ | ❌ | ❌ |
| `/api/admin/**` | ✅ | ✅ | ❌ | ❌ |
| `/api/flows/execute/**` | ✅ | ✅ | ✅ | ❌ |
| `/api/messages/{id}/reprocess` | ✅ | ✅ | ✅ | ❌ |
| `/api/development/functions/**` | ✅ | ✅ | ❌ | ❌ |
| `/api/flows/**` (CRUD) | ✅ | ✅ | ❌ | ❌ |
| `/api/flows/**` (Read) | ✅ | ✅ | ❌ | ✅ |
| `/api/messages/**` (Read) | ✅ | ✅ | ❌ | ✅ |
| `/api/logs/**` (Read) | ✅ | ✅ | ❌ | ✅ |
| `/api/dashboard/**` | ✅ | ✅ | ❌ | ✅ |

## Spring Security Configuration

### Role Names in Code
- Database: `administrator`, `developer`, `integrator`, `viewer` (lowercase)
- Spring Security: `ROLE_ADMINISTRATOR`, `ROLE_DEVELOPER`, `ROLE_INTEGRATOR`, `ROLE_VIEWER`
- JWT Token: `ADMINISTRATOR`, `DEVELOPER`, `INTEGRATOR`, `VIEWER` (uppercase)

### PreAuthorize Annotations Examples
```java
// Administrator only
@PreAuthorize("hasRole('ADMINISTRATOR')")

// Administrator and Developer
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")

// Execute permissions (Admin, Developer, Integrator)
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")

// Read permissions (All roles except Integrator needs view access)
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
```

## Database Permissions Structure

Each role has a JSON array of fine-grained permissions stored in the database:

### Administrator Permissions
```json
[
  "flows:create", "flows:read", "flows:update", "flows:delete", "flows:deploy",
  "adapters:create", "adapters:read", "adapters:update", "adapters:delete", "adapters:test",
  "structures:create", "structures:read", "structures:update", "structures:delete",
  "mappings:create", "mappings:read", "mappings:update", "mappings:delete",
  "users:create", "users:read", "users:update", "users:delete",
  "roles:read", "roles:update",
  "logs:read", "logs:delete",
  "messages:read", "messages:retry", "messages:delete",
  "dashboard:read",
  "monitoring:read",
  "development:read", "development:write",
  "system:admin", "system:config", "system:settings"
]
```

### Developer Permissions
Same as Administrator but WITHOUT:
- `system:admin`
- `system:config` 
- `system:settings`

### Integrator Permissions
```json
[
  "flows:execute",
  "api:access"
]
```

### Viewer Permissions
```json
[
  "flows:read",
  "adapters:read",
  "structures:read",
  "mappings:read",
  "logs:read",
  "messages:read",
  "dashboard:read",
  "monitoring:read"
]
```

## Implementation Notes

1. **Role Hierarchy**: There is no implicit hierarchy - each role has explicit permissions
2. **Default Role**: New users should be assigned the "viewer" role by default
3. **Role Changes**: Role changes require administrator privileges
4. **API Access**: The Integrator role is specifically designed for API/automation access
5. **Audit Trail**: All role-based actions should be logged for security audit purposes