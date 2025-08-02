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

## Development Memories
- Always run `./deploy.sh` after a code fix

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

[... rest of the existing content remains unchanged ...]