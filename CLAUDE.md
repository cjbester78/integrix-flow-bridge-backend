# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CRITICAL PRINCIPLES

1. **Never do work arounds - always fix the root issue**
2. **Never do place holders - implement the full solution**
3. Always follow Java OO best practise for all code changes.

## Project Overview

Integrix Flow Bridge is a comprehensive integration middleware platform built with Spring Boot backend and React/TypeScript frontend. It provides visual flow composition, adapter management, field mapping, and orchestration capabilities for enterprise integration scenarios.

## CRITICAL: Instruction Verification Process

**CRITICAL**: Before making changes, always read the codebase to check if the solution to the problem may already exist elsewhere.  

**CRITICAL**: After every fix do a git commit.
**CRITICAL**: Frontend has it's own repository.
**CRITICAL**: Backend has it's own repository.
**Critical**: Run below bash after each fix.
```bash
# Full deployment (frontend + backend)
./deploy.sh
```

**CRITICAL**: When working with Flow Structures in field mapping, NEVER convert WSDL/XML content to XML. Flow structures already contain XML/WSDL content in their `wsdlContent` field. Always use the original XML content directly instead of trying to convert JSON to XML. The `/structures/{id}/convert-to-xml` endpoint is only for data structures (JSON), not flow structures.

**IMPORTANT**: Before implementing any user request, follow this process:

1. **Read** the user's instruction carefully
2. **Interpret** and write out your understanding of what they're asking
3. **Display** your interpretation on screen with:
   - What you think they want
   - How you plan to implement it
   - Any assumptions you're making
4. **Wait** for user confirmation that you understood correctly
5. **Only proceed** with implementation after confirmation

### Example Format:
```
USER REQUEST: [User's actual request]

MY UNDERSTANDING:
- [Point 1 of what I think they want]
- [Point 2 of what I think they want]
- [Any assumptions I'm making]

PLANNED APPROACH:
- [Step 1 of how I'll implement]
- [Step 2 of how I'll implement]

Please confirm if this understanding is correct before I proceed.
```

This prevents misunderstandings, wasted work, and ensures alignment between user expectations and implementation.

## Standard Workflow
1. First think through the problem, read the codebase for relevant files, and write a plan to todo.md.
2. The plan should have a list of todo items that you can check off as you complete them
3. Before you begin working, check in with me and I will verify the plan.
4. Then, begin working on the todo items, marking them as complete as you go.
5. Please every step of the way just give me a high level explanation of what changes you made
6. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
7. Finally, add a review section to the todo.md file with a summary of the changes you made and any other relevant information.

**CRITICAL**: Do what has been asked; nothing more, nothing less.
**CRITICAL**: NEVER create files unless they're absolutely necessary for achieving your goal.
**CRITICAL**: ALWAYS prefer editing an existing file to creating a new one.
**CRITICAL**: NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.

**IMPORTANT**: this context may or may not be relevant to your tasks. You should not respond to this context or otherwise consider it in your response unless it is highly relevant to your task. Most of the time, it is not relevant. </system-reminder>

## Middleware Conventions
**CRITICAL**: This project uses REVERSED middleware terminology:
- **Sender Adapter** = Receives data FROM external systems (outbound/receiver in traditional terms)
- **Receiver Adapter** = Sends data TO external systems (inbound/sender in traditional terms)

**Frontend-Backend Mapping:**
- **Source = Sender Adapter** (receives data FROM external systems - outbound)
- **Target = Receiver Adapter** (sends data TO external systems - inbound)

Always use this convention when working with adapters. When creating or modifying adapter configurations:
- Use "Source" for Sender Adapters (outbound operations like SELECT, polling, listening)
- Use "Target" for Receiver Adapters (inbound operations like INSERT, POST, sending)

## Development Memories
- Always run `./deploy.sh` after a code fix

## Multi-Tenancy Model

### Environment-Based Multi-Tenancy
This application implements environment-based multi-tenancy with three distinct environments:
1. **Development** - Full access to all features, creation and modification allowed
2. **Quality Assurance** - Limited write access, focused on testing and validation
3. **Production** - Read-only access with deployment capabilities, maximum restrictions

**Important**: This is NOT a multi-customer SaaS multi-tenancy model. Each environment runs as a separate instance with its own database and configuration.

### Environment Restrictions
The `EnvironmentPermissionService` enforces the following restrictions:
- **Development**: All operations allowed (create, modify, delete, deploy)
- **QA**: Limited to testing operations (no structural changes)
- **Production**: Read-only with deployment capabilities only

Environment type is stored in the `system_configuration` table and can be changed by administrators.

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

### Key Backend Components

1. **Adapter Framework** (`adapters/` module)
   - AbstractAdapter base classes for sender/receiver adapters
   - Factory pattern for adapter instantiation
   - Support for 13 adapter types (HTTP, JDBC, FTP, SOAP, etc.)

2. **Flow Execution Engine** 
   - Orchestration of adapters and transformations
   - Saga pattern for deployment workflows
   - Async message processing with retry mechanisms

3. **Security Layer**
   - JWT authentication with Spring Security
   - Role-based access control (ADMINISTRATOR, DEVELOPER, INTEGRATOR, VIEWER)
   - Environment-based restrictions (Dev/QA/Prod)

4. **Data Model**
   - JPA entities in `data-access` module
   - MySQL database with comprehensive schema
   - Audit logging and system monitoring

### Frontend Architecture

1. **Tech Stack**
   - React 18 with TypeScript
   - Vite for build tooling
   - TailwindCSS + shadcn/ui components
   - React Query for data fetching
   - Zustand for state management

2. **Key Features**
   - Visual flow editor with React Flow
   - Real-time monitoring via WebSockets
   - Comprehensive admin panel
   - Field mapping with drag-and-drop

## Common Development Commands

### Build Commands
```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl backend -am

# Build frontend only
cd frontend-ui && npm run build

# Full deployment (frontend + backend)
./deploy.sh
```

### Run Commands
```bash
# Run backend (from backend directory)
mvn spring-boot:run

# Run backend with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run frontend dev server
cd frontend-ui && npm run dev

# Run tests
mvn test

# Run specific test
mvn test -Dtest=ClassName#methodName
```

### Database Commands
```bash
# Connect to MySQL
mysql -u root -p integrixflowbridge

# Run migrations manually
mysql -u root -p integrixflowbridge < db/src/main/resources/db/migration/V1__initial_schema.sql
```

### Frontend Commands
```bash
# Install dependencies
cd frontend-ui && npm install

# Lint code
npm run lint
```

## API Structure

### Base URL
- Development: `http://localhost:8080/api`
- API uses JWT authentication (Bearer token in Authorization header)

### Key Endpoints
- `/api/auth/*` - Authentication (login, refresh, logout)
- `/api/integration-flows/*` - Flow management
- `/api/adapters/*` - Adapter configuration
- `/api/field-mappings/*` - Field mapping management
- `/api/messages/*` - Message monitoring
- `/api/system-logs/*` - System logging

### WebSocket Endpoints
- `/ws/flow-execution` - Real-time flow execution monitoring
- `/ws/messages` - Live message streaming
- `/ws/logs` - System log streaming

## Database Schema

Key tables:
- `integration_flows` - Flow definitions
- `communication_adapters` - Adapter configurations
- `field_mappings` - Field mapping rules
- `flow_executions` - Execution history
- `system_logs` - Comprehensive logging
- `system_configuration` - Environment settings

## Environment Configuration

### Spring Profiles
- `dev` - Development with verbose logging
- `test` - Testing configuration
- `prod` - Production optimizations

### Key Configuration Files
- `application.yml` - Base configuration
- `application-dev.yml` - Development overrides
- `application-prod.yml` - Production settings
- `application-security.yml` - Security configuration

## Testing Approach

1. **Unit Tests** - Standard JUnit 5 + Mockito
2. **Integration Tests** - @SpringBootTest with H2/MySQL
3. **Frontend Tests** - Vitest + React Testing Library
4. **E2E Tests** - Manual testing via UI

## Deployment Process

1. Frontend builds to `dist/`
2. Deploy script copies to backend's `public/` directory
3. Spring Boot serves static files and API from same port
4. Single JAR deployment with embedded frontend

## Important Considerations

1. **Adapter Terminology** - Always remember the reversed naming convention
2. **API Client** - Frontend apiClient already includes `/api` prefix
3. **Environment Restrictions** - Certain operations blocked in QA/Prod
4. **Logging** - Comprehensive logging with correlation IDs
5. **WebSocket Security** - JWT authentication required for WS connections

## Common Issues and Solutions

1. **Double /api prefix** - Don't add `/api` when using apiClient
2. **CORS issues** - Backend configured for all origins in dev
3. **JWT expiration** - Auto-refresh implemented in apiClient
4. **File uploads** - Multipart support configured
5. **WebSocket disconnects** - Auto-reconnect implemented

## Performance Optimizations

1. **Database** - Connection pooling, batch operations, query optimization
2. **Caching** - Caffeine cache for frequently accessed data
3. **Frontend** - Code splitting, lazy loading, React Query caching
4. **API** - Pagination, selective field loading, compression

## Production Deployment

See `PRODUCTION_DEPLOYMENT.md` for detailed production deployment instructions including:
- Database setup and configuration
- Security hardening steps
- Docker deployment options
- Monitoring and health checks
- Performance tuning recommendations

