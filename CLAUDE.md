# Integrix Flow Bridge - Development Guide

## Project Overview

Integrix Flow Bridge is a comprehensive integration middleware platform built with Spring Boot backend and React/TypeScript frontend. It provides visual flow composition, adapter management, field mapping, and orchestration capabilities for enterprise integration scenarios.

## Middleware Conventions
**CRITICAL**: This project uses REVERSED middleware terminology:
- **Sender Adapter** = Receives data FROM external systems (inbound/receiver in traditional terms)
- **Receiver Adapter** = Sends data TO external systems (outbound/sender in traditional terms)

**Frontend-Backend Mapping:**
- **Source = Sender Adapter** (receives data FROM external systems - inbound)
- **Target = Receiver Adapter** (sends data TO external systems - outbound)

Always use this convention when working with adapters. When creating or modifying adapter configurations:
- Use "Source" for Sender Adapters (inbound operations like SELECT, polling, listening)
- Use "Target" for Receiver Adapters (outbound operations like INSERT, POST, sending)

## Architecture Overview

### Multi-Module Maven Structure
```
integrix-flow-bridge/
├── shared-lib/         # Common DTOs, enums, utilities
├── adapters/           # Adapter implementations & configurations
├── db/                 # Database schema & migrations
├── backend/            # Main Spring Boot application
├── monitoring/         # Logging & monitoring services
├── engine/             # Flow execution engine
├── data-access/        # JPA entities & repositories
├── webserver/          # External web service clients
├── webclient/          # Inbound message processing
├── soap-bindings/      # SOAP service bindings
└── frontend-ui/        # React/TypeScript frontend
```

### Package Structure Convention
**CRITICAL**: Each module has its own base package structure:

#### Backend Module
All backend classes must use the base package `com.integrationlab.backend`:
- Controllers: `com.integrationlab.backend.controller`
- Services: `com.integrationlab.backend.service`
- Security: `com.integrationlab.backend.security`
- Configuration: `com.integrationlab.backend.config`
- Exceptions: `com.integrationlab.backend.exception`
- Utilities: `com.integrationlab.backend.util`
- WebSocket: `com.integrationlab.backend.websocket`
- Domain: `com.integrationlab.backend.domain`
- Events: `com.integrationlab.backend.events`
- Saga: `com.integrationlab.backend.saga`

#### Data Access Module
All data-access classes must use the base package `com.integrationlab.data`:
- Entities/Models: `com.integrationlab.data.model`
- Repositories: `com.integrationlab.data.repository`
- Specifications: `com.integrationlab.data.specification`

#### Other Modules
- Monitoring: `com.integrationlab.monitoring`
- Engine: `com.integrationlab.engine`
- Adapters: `com.integrationlab.adapters`
- Shared: `com.integrationlab.shared`

**Never** create classes directly under `com.integrationlab` without the appropriate module subpackage. This ensures clear module separation and prevents classpath conflicts.

### Technology Stack
- **Backend**: Spring Boot 3.5.3, Java 21, MySQL 8.x
- **Frontend**: React 18, TypeScript, Vite, shadcn/ui, Tailwind CSS
- **Integration**: Apache CXF, WebSocket, REST APIs
- **Database**: MySQL with JPA/Hibernate
- **Build**: Maven (backend), npm/Vite (frontend)

## Development Setup

### Prerequisites
- Java 21
- Node.js 18+ with npm
- MySQL 8.x
- Maven 3.x

### Database Setup
```sql
CREATE DATABASE integrixflowbridge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Development Environment
1. **Backend Development**:
   ```bash
   # From project root
   mvn clean install
   cd backend
   mvn spring-boot:run
   ```
   Server runs on: http://localhost:8080

2. **Frontend Development**:
   ```bash
   cd frontend-ui
   npm install
   npm run dev
   ```
   Development server: http://localhost:8080 (Vite configured)

3. **Full Stack Development**:
   ```bash
   # Build frontend and copy to backend resources
   cd frontend-ui
   npm run build-and-copy
   
   # Run backend with embedded frontend
   cd ../backend
   mvn spring-boot:run
   ```

### Build Commands

#### Backend
```bash
# Clean build all modules
mvn clean install

# Run specific module
cd backend && mvn spring-boot:run

# Skip tests
mvn clean install -DskipTests

# Package for production
mvn clean package -Pprod
```

#### Frontend
```bash
# Development server
npm run dev

# Production build
npm run build

# Build and copy to backend
npm run build-and-copy

# Linting
npm run lint
```

## Configuration Management

### Profiles
- `dev` - Development (default)
- `prod` - Production
- `test` - Testing

### Application Configuration
- **Base**: `application.yml`
- **Development**: `application-dev.yml`
- **Production**: `application-prod.yml`

### Key Configuration Properties
```yaml
# Database
spring.datasource.url: jdbc:mysql://localhost:3306/integrixflowbridge
server.port: 8080

# Engine settings
engine.worker.thread-pool-size: 4
engine.worker.retry-attempts: 3

# Certificate storage
certificates.storage.path: /opt/integrixflowbridge/certs
```

## JPA Specification Pattern

### Dynamic Query Filtering
For complex dynamic queries with optional parameters, use JPA Specifications instead of JPQL:

```java
// Repository extends JpaSpecificationExecutor
public interface SystemLogRepository extends JpaRepository<SystemLog, String>, 
                                            JpaSpecificationExecutor<SystemLog> {
    // Simple queries can use method naming
    List<SystemLog> findBySourceAndLevelAndTimestampAfter(String source, LogLevel level, LocalDateTime timestamp);
}

// Specification class for dynamic queries
public class SystemLogSpecifications {
    public static Specification<SystemLog> withFilters(String source, String category, 
                                                       LogLevel level, String userId,
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (source != null) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// Service usage
List<SystemLog> logs = systemLogRepository.findAll(
    SystemLogSpecifications.withFilters(source, category, level, userId, startDate, endDate)
);
```

### Best Practices
- Use method naming for simple queries (findByFieldAndOtherField)
- Use Specifications for complex dynamic queries with optional parameters
- Avoid @Query with JPQL unless absolutely necessary
- Let Spring Data JPA generate the queries when possible

## Adapter Framework

### Adapter Types
- HTTP/HTTPS
- JDBC (Database)
- REST
- SOAP
- File System
- Email (SMTP/IMAP)
- FTP/SFTP
- SAP RFC/IDOC
- JMS
- OData

### Adapter Architecture
```java
// Core interfaces
BaseAdapter -> SenderAdapter/ReceiverAdapter
AdapterFactory -> DefaultAdapterFactory
AdapterConfig -> Specific config classes

// Configuration pattern
HttpSenderAdapterConfig   // Inbound HTTP
HttpReceiverAdapterConfig // Outbound HTTP
```

### Creating New Adapters
1. Create config classes in `adapters/src/main/java/com/integrationlab/adapters/config/`
2. Implement adapter classes in `adapters/src/main/java/com/integrationlab/adapters/impl/`
3. Register in `DefaultAdapterFactory`
4. Add service layer in `engine/src/main/java/com/integrationlab/engine/service/`

## API Patterns

### Controller Structure
```java
@RestController
@RequestMapping("/api/resource")
@CrossOrigin(origins = "*")
public class ResourceController {
    
    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getAllResources() {
        // Implementation
    }
    
    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(@Valid @RequestBody CreateResourceDTO request) {
        // Implementation with validation
    }
}
```

### Service Layer Pattern  
```java
@Service
@Transactional
public class ResourceService {
    
    public ResourceDTO createResource(CreateResourceDTO request) {
        // Business logic
        // Entity conversion
        // Repository operations
        return convertToDTO(savedEntity);
    }
}
```

### DTO Conversion
- All controllers use DTOs for request/response
- Services handle entity ↔ DTO conversion
- DTOs located in `shared-lib/src/main/java/com/integrationlab/shared/dto/`

## Frontend Architecture

### Component Structure
```
src/
├── components/          # Reusable UI components
│   ├── ui/             # shadcn/ui components
│   ├── adapter/        # Adapter-specific components
│   ├── flow/           # Flow composition components
│   └── layout/         # Layout components
├── pages/              # Route components
├── services/           # API service layers
├── hooks/              # Custom React hooks
├── types/              # TypeScript type definitions
└── utils/              # Utility functions
```

### State Management
- React Query for server state
- React Context for global state
- Local state with useState/useReducer

### API Service Pattern
```typescript
// services/resourceService.ts
import { api } from './api';

export const resourceService = {
  getAll: () => api.get<ResourceDTO[]>('/api/resources'),
  create: (data: CreateResourceDTO) => api.post<ResourceDTO>('/api/resources', data),
  // ... other methods
};
```

## Flow Composition System

### Flow Types
1. **Direct Mapping Flows**: Simple field-to-field transformations
2. **Orchestration Flows**: Complex multi-step business workflows

### Key Services
- `FlowCompositionService`: Complete integration flow management
- `TransformationExecutionService`: Field transformation with JavaScript
- `OrchestrationEngineService`: BPMN workflow execution
- `MessageProcessingEngine`: Runtime flow execution

### WebSocket Integration
Real-time flow execution monitoring via WebSocket:
```typescript
// Frontend connection
const ws = new WebSocket('ws://localhost:8080/flow-execution');

// Backend handler
@Component
public class FlowExecutionWebSocketHandler extends TextWebSocketHandler
```

## Database Schema

### Key Entities
- `User` - Authentication & authorization
- `BusinessComponent` - Business entity configuration
- `CommunicationAdapter` - Adapter instances
- `IntegrationFlow` - Flow definitions
- `FieldMapping` - Field transformation rules
- `SystemLog` - Audit & monitoring

### Entity Relationships
```
User -> Role (Many-to-One)
BusinessComponent -> CommunicationAdapter (One-to-Many)
IntegrationFlow -> FieldMapping (One-to-Many)
IntegrationFlow -> FlowTransformation (One-to-Many)
```

## Security & Authentication

### JWT-based Authentication
- Login endpoint: `POST /api/auth/login`
- Token validation via `JwtAuthFilter`
- Role-based authorization: administrator, integrator, viewer

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT filter chain configuration
    // CORS configuration
    // Role-based access control
}
```

## Testing Strategy

### Backend Testing
```bash
# Run all tests
mvn test

# Integration tests
mvn verify

# Specific test class
mvn test -Dtest=ControllerTest
```

### Frontend Testing
```bash
# Run tests
npm test

# Watch mode
npm run test:watch

# Coverage
npm run test:coverage
```

## Deployment

### Production Build
```bash
# Build frontend
cd frontend-ui && npm run build-and-copy

# Package backend
cd .. && mvn clean package -Pprod

# Run production JAR  
java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
```

### Docker (Future)
```dockerfile
# Multi-stage build recommended
# Frontend build -> Backend package -> Runtime image
```

## Development Guidelines

### Code Standards
- Java: Follow Spring Boot conventions, use Lombok for boilerplate
- TypeScript: Strict mode enabled, functional components preferred
- REST APIs: RESTful design, consistent HTTP status codes
- Database: Use JPA annotations, maintain referential integrity

### Code Documentation Standards

#### Java Documentation
- **Classes**: Add Javadoc with purpose and author
  ```java
  /**
   * Handles HTTP adapter configurations for inbound REST API connections.
   * Manages authentication, SSL, and request processing settings.
   * 
   * @author Integration Team
   * @since 1.0.0
   */
  ```

- **Methods**: Document purpose, parameters, return values, and exceptions
  ```java
  /**
   * Validates adapter configuration before saving.
   * 
   * @param config The adapter configuration to validate
   * @return ValidationResult containing any errors found
   * @throws InvalidConfigurationException if critical settings are missing
   */
  ```

- **Complex Logic**: Add inline comments explaining decisions
  ```java
  // Use synchronous processing for batch operations to maintain order
  if (config.isBatchMode()) {
      processingMode = ProcessingMode.SYNCHRONOUS;
  }
  ```

#### TypeScript/React Documentation
- **Components**: Document purpose and props
  ```typescript
  /**
   * Displays and manages SSL certificate selection for secure adapters.
   * Filters certificates by business component and status.
   */
  interface CertificateSelectionProps {
    /** Business component ID to filter certificates */
    businessComponentId?: string;
    /** Callback when certificate is selected */
    onChange: (certId: string) => void;
  }
  ```

- **Functions**: Document complex logic and decisions
  ```typescript
  // Fetch from API instead of using mock data
  // Empty array on failure to allow user to see UI
  const fetchData = async () => {
    try {
      const result = await api.get('/certificates');
      setCertificates(result.data || []);
    } catch (error) {
      setCertificates([]); // Show empty state, not error
    }
  };
  ```

- **Hooks**: Document purpose and return values
  ```typescript
  /**
   * Custom hook for managing adapter data with automatic refresh.
   * @returns {Object} Adapter data, loading state, and refresh function
   */
  ```

#### General Comment Guidelines
1. **WHY over WHAT**: Explain why code makes certain decisions, not just what it does
2. **Business Logic**: Document business rules and adapter conventions
3. **TODOs**: Mark incomplete features with TODO comments and ticket numbers
4. **Warnings**: Use WARNING comments for critical sections
5. **Performance**: Document performance-critical decisions

#### Examples of Good Comments
```java
// CRITICAL: Sender adapters receive data (inbound), despite the name
// This reverses typical middleware conventions
public class HttpSenderAdapter extends BaseSenderAdapter {
    
    // WARNING: Certificate validation must occur before any network operations
    // to prevent security vulnerabilities
    private void validateCertificate() {
        // Implementation
    }
    
    // TODO: Implement connection pooling for better performance (TICKET-123)
    private Connection getConnection() {
        // Temporary implementation - single connection
    }
}
```

```typescript
// Decision: Remove mock data fallback to ensure UI reflects actual system state
// Users need to see when no data exists so they can create new items
useEffect(() => {
  // Fetch real data from API, no mock fallback
}, []);
```

### Git Workflow
- Feature branches from `master`
- Descriptive commit messages
- Pull request reviews required
- Automated testing on CI/CD

### Performance Considerations
- Database connection pooling (HikariCP)
- Async processing for heavy operations
- React Query for efficient API caching
- WebSocket for real-time updates

## Monitoring & Logging

### Application Monitoring
- Spring Boot Actuator endpoints: `/actuator/*`
- Custom health checks
- Performance metrics collection

### Logging Strategy
```yaml
logging:
  level:
    com.integrationlab: DEBUG
    org.springframework.web: DEBUG
```

### System Logs
- Database-backed system logging
- Integration flow execution tracking
- Adapter performance monitoring

## Troubleshooting

### Common Issues
1. **Database Connection**: Check MySQL service, credentials in application-dev.yml
2. **Frontend Build**: Clear node_modules, reinstall dependencies
3. **CORS Issues**: Verify @CrossOrigin annotations on controllers
4. **WebSocket Connection**: Check server port configuration

### Debug Endpoints
- Health: `GET /actuator/health`
- Info: `GET /actuator/info`
- Metrics: `GET /actuator/metrics`

## Standard Workflow
**CRITICAL**:
1. First think through the problem, read the codebase for relevant files, and write a plan to todo.md.
2. The plan should have a list of todo items that you can check off as you complete them
3. Before you begin working, check in with me and I will verify the plan.
4. Then, begin working on the todo items, marking them as complete as you go.
5. Please every step of the way just give me a high level explanation of what changes you made
6. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
7. Finally, add a review section to the todo.md file with a summary of the changes you made and any other relevant information.

## Git Configuration

### Repository Structure
This project consists of two separate git repositories:
1. **Backend Repository**: `/Users/cjbester/git/integrix-flow-bridge/` - Java Spring Boot backend
2. **Frontend Repository**: `/Users/cjbester/git/integrix-flow-bridge/frontend-ui/` - React TypeScript frontend

### Git Commit Guidelines
- Always ensure you're in the correct repository directory before committing
- Use clear, descriptive commit messages
- Backend commits should be made from the root directory
- Frontend commits should be made from the frontend-ui directory
- Follow conventional commit format: `type(scope): description`
  - Types: feat, fix, docs, style, refactor, test, chore
  - Example: `fix(adapters): resolve compilation errors in JmsSenderAdapter`

### Git Configuration Requirements
To enable git commits, ensure:
1. Git is configured with user name and email
2. You have write access to both repositories
3. The repositories are properly initialized

### Git Update Strategy
**CRITICAL**: Regular git commits and pushes must be made to both repositories:
- **Backend Repository**: Commit and push changes after completing major backend fixes or features
- **Frontend Repository**: Commit and push changes after completing major frontend fixes or features
- **Frequency**: Make git updates regularly throughout development, not just at the end
- **Best Practice**: Commit related changes together with clear, descriptive messages
- **Important**: Always verify which repository you're in before committing (backend vs frontend)

## Important Reminders
- Do what has been asked; nothing more, nothing less.
- NEVER create files unless they're absolutely necessary for achieving your goal.
- ALWAYS prefer editing an existing file to creating a new one.
- NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
- NEVER create test files or suggest adding tests. This project does not use tests.