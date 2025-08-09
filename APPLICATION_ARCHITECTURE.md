# Application Architecture - Integrix Flow Bridge

## Overview

Integrix Flow Bridge is a comprehensive integration middleware platform built with Spring Boot backend and React/TypeScript frontend. It provides visual flow composition, adapter management, field mapping, and orchestration capabilities for enterprise integration scenarios.

## Adapter Architecture

The application supports 13 adapter types, each with sender and receiver implementations. 

**CRITICAL TERMINOLOGY**: This project uses REVERSED terminology:
- **Sender Adapter** = Receives data FROM external systems (inbound)
- **Receiver Adapter** = Sends data TO external systems (outbound)

### Supported Adapters

#### 1. HTTP/HTTPS Adapters
- **HttpSenderAdapter**: Listens for incoming HTTP requests, processes them, and passes data into the flow
- **HttpReceiverAdapter**: Sends HTTP requests to external endpoints with configurable methods (GET, POST, PUT, DELETE)
- Supports authentication (Basic, Bearer, OAuth2), SSL/TLS, custom headers, and retry logic

#### 2. JDBC Adapters
- **JdbcSenderAdapter**: Polls database tables using SELECT queries, fetches records based on conditions
- **JdbcReceiverAdapter**: Executes INSERT, UPDATE, DELETE operations on target databases
- Supports connection pooling, batch operations, and transaction management

#### 3. REST Adapters
- **RestSenderAdapter**: Creates RESTful endpoints to receive JSON/XML data
- **RestReceiverAdapter**: Calls REST APIs with proper content negotiation and error handling
- Enhanced HTTP adapters with REST-specific features

#### 4. SOAP Adapters
- **SoapSenderAdapter**: Hosts SOAP web service endpoints, processes SOAP envelopes
- **SoapReceiverAdapter**: Calls external SOAP services with WS-Security support
- Handles WSDL parsing and SOAP fault management

#### 5. File Adapters
- **FileSenderAdapter**: Monitors directories for new files, reads and processes them
- **FileReceiverAdapter**: Writes processed data to files in specified formats (CSV, XML, JSON, Fixed-length)
- Supports file patterns, archiving, and error handling

#### 6. FTP/SFTP Adapters
- **FtpSenderAdapter/SftpSenderAdapter**: Polls remote FTP/SFTP servers for files
- **FtpReceiverAdapter/SftpReceiverAdapter**: Uploads files to remote FTP/SFTP servers
- Handles secure connections, directory navigation, and transfer modes

#### 7. Mail Adapters
- **MailSenderAdapter**: Monitors email inboxes (IMAP/POP3) for new messages
- **MailReceiverAdapter**: Sends emails via SMTP with attachments
- Supports various authentication methods and email protocols

#### 8. JMS Adapters
- **JmsSenderAdapter**: Consumes messages from JMS queues/topics
- **JmsReceiverAdapter**: Publishes messages to JMS destinations
- Handles connection factories, message acknowledgment, and transaction support

#### 9. RFC Adapters (SAP)
- **RfcSenderAdapter**: Receives RFC calls from SAP systems
- **RfcReceiverAdapter**: Makes RFC calls to SAP systems
- Manages SAP JCo connections and BAPI interactions

#### 10. IDoc Adapters (SAP)
- **IdocSenderAdapter**: Receives IDocs from SAP systems
- **IdocReceiverAdapter**: Sends IDocs to SAP systems
- Handles IDoc parsing, validation, and acknowledgments

#### 11. OData Adapters
- **OdataSenderAdapter**: Exposes OData services for data consumption
- **OdataReceiverAdapter**: Consumes OData services with query support
- Supports OData v4 protocol with filtering and pagination

## Flow Types

The application supports two primary flow types:

### Direct Mapping Flow

Direct mapping flows are point-to-point integrations with data transformation between a single source and single target.

#### Process Steps:

1. **Flow Initialization**
   - Load flow configuration with source and target adapters
   - Validate adapter configurations and connectivity
   - Initialize execution context with correlation IDs

2. **Data Acquisition (Source Adapter)**
   - Sender adapter receives/fetches data from external system
   - Data is captured in its native format (JSON, XML, CSV, etc.)
   - Initial validation and error checking performed

3. **Format Conversion to XML**
   - All data is converted to canonical XML format
   - This provides a common data model for transformations
   - Preserves data structure and metadata

4. **Field Mapping & Transformation**
   - Apply field mappings defined in the flow
   - Execute transformation functions (concatenation, splitting, formatting)
   - Support for custom Java functions and built-in transformations
   - Maintain mapping order and dependencies

5. **Target Format Conversion**
   - Convert transformed XML to target adapter's required format
   - Apply format-specific configurations (delimiters, encodings)
   - Validate output against target schema

6. **Data Delivery (Target Adapter)**
   - Receiver adapter sends data to target system
   - Handle acknowledgments and confirmations
   - Manage error scenarios and retries

7. **Logging & Monitoring**
   - Log all steps with timestamps and correlation IDs
   - Track message flow through WebSocket notifications
   - Store execution history for audit trails

### Orchestration Flow

Orchestration flows handle complex integrations with one source and multiple targets, including conditional logic and parallel processing.

#### Process Steps:

1. **Orchestration Initialization**
   - Create execution context with workflow state
   - Load orchestration configuration and steps
   - Initialize the single source adapter and multiple target adapters

2. **Business Component Loading**
   - Load all business components involved in the flow
   - Validate component configurations
   - Establish component relationships

3. **Single Source, Multi-Target Adapter Initialization**
   - Initialize ONE sender adapter (source) to receive/fetch data
   - Initialize MULTIPLE receiver adapters (targets) for different destinations
   - Set up routing rules to determine which targets receive data
   - Configure conditional logic for target selection

4. **Sequential/Parallel Step Execution**
   - Fetch data from the single source adapter
   - Execute orchestration steps based on defined order
   - Support parallel processing to multiple targets
   - Handle conditional routing based on data content or business rules
   - Manage state between steps

5. **Complex Transformations**
   - Apply different transformations for each target system
   - Support data splitting (one source record → multiple target records)
   - Handle target-specific data enrichment and validation
   - Execute business rules to determine target routing

6. **Multi-Target Processing**
   - Route transformed data to multiple receiver adapters simultaneously
   - Apply different data formats for each target system
   - Handle partial failures (some targets succeed, others fail)
   - Manage transaction boundaries per target
   - Support "fire-and-forget" or "all-or-nothing" delivery modes

7. **Process Completion**
   - Aggregate delivery results from all target adapters
   - Generate consolidated execution report
   - Update orchestration state with success/failure per target
   - Trigger completion notifications

8. **Error Handling & Compensation**
   - Implement saga pattern for distributed transactions
   - Support compensation logic for failed targets
   - Retry logic per individual target adapter
   - Maintain consistency across target systems
   - Provide detailed error reporting per target

#### Key Orchestration Patterns:
- **Fan-out**: One source message delivered to multiple targets
- **Content-based routing**: Route to specific targets based on message content
- **Conditional delivery**: Only send to certain targets if conditions are met
- **Format branching**: Same data transformed differently for each target
- **Partial failure handling**: Continue processing even if some targets fail

#### Example Use Cases:
- Order received via HTTP → Send to ERP, Warehouse, and CRM systems
- File pickup → Distribute to multiple departments with different formats
- Database change → Propagate to multiple downstream applications

## Key Differences Between Flow Types

| Aspect | Direct Mapping | Orchestration |
|--------|----------------|---------------|
| Source Adapters | Single | Single |
| Target Adapters | Single | Multiple |
| Transformation | One transformation pipeline | Multiple transformation pipelines |
| Routing | Direct point-to-point | Conditional and parallel routing |
| Error Handling | Simple retry/fail | Complex compensation logic |
| State Management | Minimal | Full workflow state tracking |
| Use Case | Simple A→B integration | Complex distribution scenarios |

---

*Note: This document will be expanded with additional architectural details as needed.*