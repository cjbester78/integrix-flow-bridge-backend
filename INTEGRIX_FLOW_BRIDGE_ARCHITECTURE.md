# Integrix Flow Bridge - System Architecture Document

## Table of Contents
1. [System Overview](#system-overview)
2. [Core Principles](#core-principles)
3. [System Components](#system-components)
4. [Data Flow Architecture](#data-flow-architecture)
5. [Adapter Framework](#adapter-framework)
6. [Transformation Engine](#transformation-engine)
7. [Field Mapping Architecture](#field-mapping-architecture)
8. [Flow Execution Process](#flow-execution-process)
9. [Data Structures](#data-structures)
10. [Deployment Architecture](#deployment-architecture)
11. [Security Architecture](#security-architecture)
12. [Monitoring and Logging](#monitoring-and-logging)

---

## 1. System Overview

Integrix Flow Bridge is an enterprise integration middleware platform that enables seamless data exchange between disparate systems through visual flow composition, adapter management, and field mapping capabilities.

### Key Capabilities
- Visual flow design and orchestration
- Protocol-agnostic adapter framework
- XML-based field mapping engine
- Real-time and batch processing
- Multi-environment deployment (Dev/QA/Prod)

### Technology Stack
- **Backend**: Spring Boot 3.x, Java 21
- **Frontend**: React 18, TypeScript, Vite
- **Database**: PostgreSQL 15.x
- **Message Processing**: XML/XSLT based transformation engine
- **API**: RESTful services with JWT authentication

---

## 2. Core Principles

### 2.1 XML as Universal Format
**ALL data transformations happen through XML**. This is a fundamental architectural decision:
- Non-XML formats (JSON, CSV, Fixed-width, etc.) are converted to XML at the source adapter
- Field mapping and transformations operate exclusively on XML
- Target adapter converts from XML to the required output format

### 2.2 Adapter Terminology (IMPORTANT)
The system uses **REVERSED** middleware terminology:
- **Sender Adapter** = Receives data FROM external systems (traditionally called "receiver/inbound")
- **Receiver Adapter** = Sends data TO external systems (traditionally called "sender/outbound")

### 2.3 Namespace-Aware Processing
- All XML processing must preserve and correctly handle namespaces
- WSDL-based structures define namespace mappings
- Field mappings use XPath expressions with namespace prefixes

### 2.4 Stateless Processing
- Each message is processed independently
- No session state between messages
- Adapters maintain connection pools but not message state

---

## 3. System Components

### 3.1 Frontend Components
```
frontend-ui/
├── Flow Designer        # Visual flow composition
├── Field Mapping UI     # Drag-and-drop field mapping
├── Adapter Manager      # Adapter configuration
├── Structure Designer   # Data structure definition
├── Monitoring Dashboard # Real-time flow monitoring
└── Admin Panel         # System configuration
```

### 3.2 Backend Modules
```
backend/
├── API Layer           # REST endpoints
├── Service Layer       # Business logic
├── Security           # JWT auth & authorization
└── WebSocket          # Real-time updates

engine/
├── Flow Executor      # Orchestrates flow execution
├── XML Processor      # XML transformation engine
├── Field Mapper       # XPath-based mapping
└── Format Converters  # JSON/CSV/etc to XML

adapters/
├── Protocol Adapters  # HTTP, SOAP, FTP, JDBC, etc.
├── Adapter Factory    # Dynamic adapter creation
└── Connection Pools   # Resource management

data-access/
├── Entities          # JPA entities
├── Repositories      # Data access layer
└── Migrations        # Database schemas
```

---

## 4. Data Flow Architecture

### 4.1 Format Conversion Logic

#### JSON to XML Conversion
```javascript
// Input JSON
{
  "order": {
    "id": "12345",
    "items": [
      {"sku": "ABC", "qty": 2},
      {"sku": "XYZ", "qty": 1}
    ]
  }
}

// Output XML
<order>
  <id>12345</id>
  <items>
    <item>
      <sku>ABC</sku>
      <qty>2</qty>
    </item>
    <item>
      <sku>XYZ</sku>
      <qty>1</qty>
    </item>
  </items>
</order>
```

#### CSV to XML Conversion
```
// Input CSV
id,name,amount
1,John Doe,100.50
2,Jane Smith,200.75

// Output XML
<rows>
  <row>
    <id>1</id>
    <name>John Doe</name>
    <amount>100.50</amount>
  </row>
  <row>
    <id>2</id>
    <name>Jane Smith</name>
    <amount>200.75</amount>
  </row>
</rows>
```

#### Database ResultSet to XML
```sql
-- Query
SELECT order_id, customer_name, order_date FROM orders

-- Output XML
<resultset>
  <row>
    <order_id>1001</order_id>
    <customer_name>ACME Corp</customer_name>
    <order_date>2025-01-15</order_date>
  </row>
  <row>
    <order_id>1002</order_id>
    <customer_name>ABC Inc</customer_name>
    <order_date>2025-01-16</order_date>
  </row>
</resultset>
```

### 4.2 Message Flow Sequence

```
External System → Sender Adapter → XML Conversion → Field Mapping → Receiver Adapter → Target System
```

### 4.3 Detailed Flow Process

1. **Message Reception**
   - Sender adapter receives data from external system
   - Protocol-specific handling (HTTP, SOAP, File, Database, etc.)
   - Authentication/authorization if required

2. **Format Conversion to XML**
   - Non-XML formats converted using format-specific converters:
     - JSON → XML (preserves structure)
     - CSV → XML (configurable row/column mapping)
     - Fixed-width → XML (position-based parsing)
     - Database ResultSet → XML (column-to-element mapping)
   - XML formats pass through with namespace preservation

3. **Structure Validation**
   - Validate against source Flow Structure (WSDL/XSD)
   - Extract namespace information
   - Verify required fields presence

4. **Field Mapping Transformation**
   - Apply XPath-based field mappings
   - Execute transformation functions if configured
   - Preserve target namespace requirements

5. **Target Format Generation**
   - Generate output XML with target namespace
   - Validate against target Flow Structure

6. **Format Conversion from XML**
   - Convert to target format if non-XML
   - Apply format-specific rules

7. **Message Delivery**
   - Receiver adapter sends to target system
   - Handle authentication/protocols
   - Manage retries and error handling

---

## 5. Adapter Framework

The application supports **13 adapter types**, each with sender and receiver implementations.

### 5.1 Adapter Types

| Adapter Type | Direction | Purpose |
|-------------|-----------|---------|
| HTTP | Both | HTTP/HTTPS endpoint integration |
| REST | Both | RESTful API integration with enhanced features |
| SOAP | Both | SOAP web service integration |
| JDBC | Both | Database operations |
| File | Both | File system operations |
| FTP/SFTP | Both | File transfer protocols |
| JMS | Both | Message queue integration |
| Mail | Both | Email processing (SMTP/IMAP/POP3) |
| OData | Both | OData v4 service integration |
| RFC | Both | SAP Remote Function Call integration |
| IDoc | Both | SAP Intermediate Document integration |

### 5.2 Detailed Adapter Logic

#### 5.2.1 HTTP Adapter

**Sender Mode (Receives from external system):**
```
Logic Flow:
1. Listen on configured endpoint/port
2. Receive HTTP request (GET/POST/PUT/DELETE)
3. Extract headers, body, query parameters
4. Convert to XML structure:
   <HttpMessage>
     <Headers>
       <Header name="Content-Type">application/json</Header>
     </Headers>
     <Body>
       <!-- If JSON: Convert to XML -->
       <!-- If XML: Pass through -->
       <!-- If Form data: Convert to XML -->
     </Body>
     <QueryParams>
       <Param name="id">123</Param>
     </QueryParams>
   </HttpMessage>
5. Pass to transformation engine
```

**Receiver Mode (Sends to external system):**
```
Logic Flow:
1. Receive XML from transformation
2. Extract target URL, method, headers from config
3. Convert XML to required format (JSON/Form/XML)
4. Build HTTP request
5. Send request with retry logic
6. Convert response to XML for return path
```

#### 5.2.2 REST Adapter

**Sender Mode (RESTful endpoint):**
```
Logic Flow:
1. Create RESTful endpoints with proper resource paths
2. Accept JSON/XML with content negotiation
3. Validate against OpenAPI/Swagger spec
4. Convert request body to XML:
   <RestMessage>
     <Method>POST</Method>
     <Resource>/api/orders</Resource>
     <Headers>
       <Header name="Content-Type">application/json</Header>
     </Headers>
     <Body>
       <!-- JSON converted to XML -->
     </Body>
     <PathParams>
       <Param name="id">123</Param>
     </PathParams>
   </RestMessage>
5. Handle REST-specific features (HATEOAS, pagination)
```

**Receiver Mode (REST client):**
```
Logic Flow:
1. Receive XML from transformation
2. Build RESTful request with resource paths
3. Handle content negotiation
4. Convert XML to JSON/XML based on Accept header
5. Execute with proper HTTP verbs
6. Handle REST-specific responses (status codes, links)
7. Convert response for return path
```

#### 5.2.3 SOAP Adapter

**Sender Mode (Receives SOAP requests):**
```
Logic Flow:
1. Expose SOAP endpoint from WSDL
2. Receive SOAP envelope
3. Validate against WSDL
4. Extract SOAP body content (already XML)
5. Preserve namespaces
6. Pass to transformation preserving namespace context
```

**Receiver Mode (Sends SOAP requests):**
```
Logic Flow:
1. Receive XML from transformation
2. Load target WSDL configuration
3. Build SOAP envelope with correct namespaces
4. Add SOAP headers (if configured)
5. Send to target endpoint
6. Handle SOAP faults
7. Extract response body for return path
```

#### 5.2.3 JDBC Adapter

**Sender Mode (Database polling/triggers):**
```
Logic Flow:
1. Execute configured SELECT query at intervals
2. For each row in ResultSet:
   <Row>
     <Column name="ID">123</Column>
     <Column name="NAME">John Doe</Column>
     <Column name="DATE">2025-01-15</Column>
   </Row>
3. Mark processed records (if configured)
4. Convert ResultSet to XML
5. Pass to transformation
```

**Receiver Mode (Database operations):**
```
Logic Flow:
1. Receive XML from transformation
2. Parse operation type (INSERT/UPDATE/DELETE)
3. Build SQL from XML structure:
   - Map XML elements to columns
   - Handle data type conversions
4. Execute with transaction support
5. Return affected rows count
```

#### 5.2.4 File Adapter

**Sender Mode (File reading):**
```
Logic Flow:
1. Monitor configured directory
2. Detect new/modified files
3. Read file content
4. Based on file type:
   - CSV: Parse and convert to XML
   - XML: Validate and pass through
   - JSON: Convert to XML
   - Fixed-width: Parse positions to XML
5. Move/rename processed files
6. Pass to transformation
```

**Receiver Mode (File writing):**
```
Logic Flow:
1. Receive XML from transformation
2. Convert to target format (CSV/JSON/XML/Fixed)
3. Generate filename (pattern-based)
4. Write to target directory
5. Set file permissions
6. Archive if configured
```

#### 5.2.5 FTP/SFTP Adapter

**Sender Mode (File download):**
```
Logic Flow:
1. Connect to FTP/SFTP server
2. List files matching pattern
3. Download each file
4. Process content like File adapter
5. Delete/move remote file after processing
6. Handle connection pooling
```

**Receiver Mode (File upload):**
```
Logic Flow:
1. Receive XML from transformation
2. Convert to target file format
3. Connect to FTP/SFTP server
4. Upload with temporary name
5. Rename to final name (atomic operation)
6. Verify upload success
```

#### 5.2.6 JMS Adapter

**Sender Mode (Message consumer):**
```
Logic Flow:
1. Connect to JMS provider (ActiveMQ/RabbitMQ/etc)
2. Subscribe to queue/topic
3. Receive message
4. Extract headers and body
5. Convert to XML:
   <JmsMessage>
     <Headers>
       <Property name="JMSCorrelationID">123</Property>
     </Headers>
     <Body><!-- Message content as XML --></Body>
   </JmsMessage>
6. Acknowledge receipt
```

**Receiver Mode (Message producer):**
```
Logic Flow:
1. Receive XML from transformation
2. Create JMS message
3. Set headers/properties from XML
4. Set message body (Text/Bytes/Object)
5. Send to queue/topic
6. Handle delivery confirmation
```

#### 5.2.7 Mail Adapter

**Sender Mode (Email receiver):**
```
Logic Flow:
1. Connect to mail server (IMAP/POP3)
2. Fetch unread messages
3. For each email:
   <Email>
     <From>sender@example.com</From>
     <To>recipient@example.com</To>
     <Subject>Order Confirmation</Subject>
     <Body><!-- Email body --></Body>
     <Attachments>
       <Attachment name="invoice.pdf">
         <!-- Base64 encoded content -->
       </Attachment>
     </Attachments>
   </Email>
4. Mark as read/move to folder
5. Pass to transformation
```

**Receiver Mode (Email sender):**
```
Logic Flow:
1. Receive XML from transformation
2. Build email from XML structure
3. Handle attachments (decode base64)
4. Connect to SMTP server
5. Send email
6. Handle delivery status
```

#### 5.2.8 OData Adapter

**Sender Mode (OData consumer):**
```
Logic Flow:
1. Build OData query from configuration
2. Handle pagination
3. Execute query with filters
4. Convert OData JSON/XML response to standard XML
5. Handle navigation properties
6. Pass to transformation
```

**Receiver Mode (OData producer):**
```
Logic Flow:
1. Receive XML from transformation
2. Determine operation (Create/Update/Delete)
3. Convert to OData format
4. Handle entity relationships
5. Execute OData operation
6. Process response
```

#### 5.2.9 RFC Adapter (SAP)

**Sender Mode (RFC Server):**
```
Logic Flow:
1. Register as RFC server with SAP gateway
2. Listen for incoming RFC calls
3. Receive function parameters
4. Convert ABAP structures to XML:
   <RfcMessage>
     <FunctionName>Z_GET_CUSTOMER</FunctionName>
     <ImportParameters>
       <CUSTOMER_ID>12345</CUSTOMER_ID>
     </ImportParameters>
     <Tables>
       <CUSTOMER_DATA>
         <Row>...</Row>
       </CUSTOMER_DATA>
     </Tables>
   </RfcMessage>
5. Process and pass to transformation
6. Return RFC response to SAP
```

**Receiver Mode (RFC Client):**
```
Logic Flow:
1. Receive XML from transformation
2. Connect to SAP system via JCo
3. Map XML to RFC parameters
4. Execute RFC/BAPI call
5. Handle ABAP exceptions
6. Convert response to XML
7. Manage connection pooling
```

#### 5.2.10 IDoc Adapter (SAP)

**Sender Mode (IDoc Receiver):**
```
Logic Flow:
1. Register with SAP ALE layer
2. Receive IDoc from SAP
3. Parse IDoc structure:
   <IDoc>
     <Control>
       <IDOCTYP>ORDERS05</IDOCTYP>
       <MESTYP>ORDERS</MESTYP>
     </Control>
     <Data>
       <E1EDK01>...</E1EDK01>
       <E1EDK14>...</E1EDK14>
     </Data>
   </IDoc>
4. Validate IDoc against metadata
5. Send acknowledgment to SAP
6. Pass to transformation
```

**Receiver Mode (IDoc Sender):**
```
Logic Flow:
1. Receive XML from transformation
2. Build IDoc structure
3. Set control record fields
4. Connect to SAP via JCo/tRFC
5. Send IDoc with transaction control
6. Handle status updates
7. Process acknowledgments
```

### 5.3 Adapter Configuration Structure

```json
{
  "adapterType": "SOAP",
  "mode": "SENDER|RECEIVER",
  "configuration": {
    "endpoint": "http://example.com/service",
    "wsdlUrl": "http://example.com/service?wsdl",
    "operation": "ProcessData",
    "authentication": {
      "type": "BASIC|TOKEN|CERTIFICATE",
      "credentials": {}
    },
    "connectionPool": {
      "maxConnections": 10,
      "timeout": 30000
    }
  }
}
```

### 5.4 Adapter Lifecycle

1. **Initialization**
   - Load configuration
   - Establish connection pools
   - Validate connectivity

2. **Message Processing**
   - Sender: Poll/Listen → Receive → Convert to XML
   - Receiver: Accept XML → Convert to target format → Send

3. **Error Handling**
   - Retry mechanism with exponential backoff
   - Dead letter queue for failed messages
   - Detailed error logging

---

## 6. Transformation Engine

### 6.1 Transformation Processing Logic

```
Transformation Pipeline:
1. Receive Source XML
   ↓
2. Load Transformation Configuration
   - Field mappings
   - Functions
   - XSLT (if configured)
   ↓
3. Initialize Target XML Document
   - Create root structure
   - Set namespaces
   ↓
4. Process Field Mappings (in order)
   For each mapping:
   - Evaluate source XPath
   - Apply function (if any)
   - Set value at target XPath
   ↓
5. Apply Global Functions
   - Date formatting
   - Code lookups
   - Business rules
   ↓
6. Execute XSLT (if configured)
   - Apply stylesheet
   - Handle parameters
   ↓
7. Validate Output
   - Check against target XSD/WSDL
   - Verify required fields
   ↓
8. Return Transformed XML
```

### 6.2 Transformation Types

1. **Field Mapping** (Primary)
   - XPath to XPath mapping
   - Namespace-aware processing
   - One-to-one, one-to-many, many-to-one mappings

2. **Function-based Transformation**
   - Built-in functions (concat, substring, format, etc.)
   - Custom JavaScript functions
   - Visual flow designer for complex logic

3. **XSLT Transformation** (Advanced)
   - Full XSLT 2.0 support
   - Custom stylesheets

### 6.3 Built-in Transformation Functions

```javascript
// String Functions
concat(str1, str2, delimiter) - Concatenate strings
substring(str, start, end) - Extract substring
uppercase(str) - Convert to uppercase
lowercase(str) - Convert to lowercase
trim(str) - Remove whitespace
replace(str, find, replace) - Replace text
padLeft(str, length, char) - Left pad string
padRight(str, length, char) - Right pad string

// Date Functions
formatDate(date, inputFormat, outputFormat) - Format dates
addDays(date, days) - Add/subtract days
currentDate() - Get current date
currentTimestamp() - Get current timestamp

// Number Functions
add(num1, num2) - Addition
subtract(num1, num2) - Subtraction
multiply(num1, num2) - Multiplication
divide(num1, num2) - Division
round(num, decimals) - Round number
formatNumber(num, pattern) - Format number

// Logical Functions
if(condition, trueValue, falseValue) - Conditional
equals(val1, val2) - Equality check
notEquals(val1, val2) - Inequality check
isEmpty(val) - Check if empty
isNotEmpty(val) - Check if not empty

// Array Functions
count(array) - Count elements
sum(array) - Sum array values
average(array) - Calculate average
min(array) - Find minimum
max(array) - Find maximum
join(array, delimiter) - Join array elements
```

### 6.4 Transformation Execution Order

```
Source XML → Validate → Field Mappings → Functions → XSLT → Target XML
```

---

## 7. Field Mapping Architecture

### 7.1 Field Mapping Process Flow

```
1. Load Source WSDL/XSD → Extract Namespace Mappings
2. Load Target WSDL/XSD → Extract Namespace Mappings
3. Build XPath Context with all namespaces
4. For each field mapping:
   a. Evaluate source XPath → Get value(s)
   b. Apply transformation function (if any)
   c. Create/Update target node at target XPath
5. Validate output against target structure
```

### 7.2 Mapping Data Model

```sql
field_mappings:
- id                 # UUID
- transformation_id  # Link to flow transformation
- source_xpath      # XPath with namespace (e.g., //ns1:OrderId)
- target_xpath      # XPath with namespace (e.g., //ns2:PurchaseOrderNumber)
- java_function     # Optional transformation function
- is_array_mapping  # Handle repeating elements
- mapping_order     # Execution sequence
```

### 7.3 XPath Processing

1. **Namespace Resolution**
   - Extract namespaces from source WSDL
   - Extract namespaces from target WSDL
   - Build namespace context for XPath evaluation

2. **Path Evaluation**
   ```java
   // Example XPath with namespace
   Source: //soapenv:Body/ord:Order/ord:OrderNumber
   Target: //soapenv:Body/po:PurchaseOrder/po:Number
   ```

3. **Value Extraction and Assignment**
   - Extract value(s) from source using source XPath
   - Apply transformation function if specified
   - Create/update target node using target XPath

### 7.4 Array/Repeating Element Handling

For structures like:
```xml
<Orders>
  <Order><Id>1</Id><Amount>100</Amount></Order>
  <Order><Id>2</Id><Amount>200</Amount></Order>
</Orders>
```

Mappings support:
- Array context path: `//Orders/Order`
- Element mappings: `Id → OrderId`, `Amount → OrderAmount`

---

### 7.5 Namespace Handling Logic

```java
// Extract namespaces from WSDL
Map<String, String> extractNamespaces(String wsdl) {
  // Parse WSDL document
  // Extract targetNamespace
  // Extract all xmlns declarations
  // Build prefix → URI mapping
}

// Apply mapping with namespaces
void applyMapping(FieldMapping mapping, Document source, Document target) {
  // Create XPath with namespace context
  XPath xpath = XPathFactory.newInstance().newXPath();
  xpath.setNamespaceContext(namespaceContext);
  
  // Evaluate source XPath
  String value = xpath.evaluate(mapping.getSourceXPath(), source);
  
  // Apply transformation
  if (mapping.hasFunction()) {
    value = executeFunction(mapping.getFunction(), value);
  }
  
  // Set value at target XPath
  setValueAtPath(target, mapping.getTargetXPath(), value, xpath);
}
```

## 8. Flow Execution Process

### 8.1 Complete Message Flow

```
1. Message Arrival
   ↓
2. Source Adapter Processing
   - Protocol handling
   - Authentication
   - Format detection
   ↓
3. Convert to XML (if needed)
   - JSON → XML
   - CSV → XML
   - Database → XML
   ↓
4. Validate against Source Structure
   - WSDL/XSD validation
   - Namespace verification
   ↓
5. Field Mapping Transformation
   - Load mappings
   - Apply XPath transformations
   - Execute functions
   ↓
6. Generate Target XML
   - Build with target namespaces
   - Validate against target structure
   ↓
7. Convert from XML (if needed)
   - XML → JSON
   - XML → CSV
   - XML → Database
   ↓
8. Target Adapter Processing
   - Protocol handling
   - Authentication
   - Delivery
   ↓
9. Response Processing (if sync)
   - Reverse transformation
   - Return to caller
```

### 8.2 Flow Types

The application supports two primary flow types with distinct architectural patterns:

#### 8.2.1 Direct Mapping Flow

Direct mapping flows are point-to-point integrations with data transformation between a **single source** and **single target**.

**Process Steps:**

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

#### 8.2.2 Orchestration Flow

Orchestration flows handle complex integrations with **one source** and **multiple targets**, including conditional logic and parallel processing.

**Process Steps:**

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

**Key Orchestration Patterns:**
- **Fan-out**: One source message delivered to multiple targets
- **Content-based routing**: Route to specific targets based on message content
- **Conditional delivery**: Only send to certain targets if conditions are met
- **Format branching**: Same data transformed differently for each target
- **Partial failure handling**: Continue processing even if some targets fail

**Example Use Cases:**
- Order received via HTTP → Send to ERP, Warehouse, and CRM systems
- File pickup → Distribute to multiple departments with different formats
- Database change → Propagate to multiple downstream applications

### 8.3 Key Differences Between Flow Types

| Aspect | Direct Mapping | Orchestration |
|--------|----------------|---------------|
| Source Adapters | Single | Single |
| Target Adapters | Single | Multiple |
| Transformation | One transformation pipeline | Multiple transformation pipelines |
| Routing | Direct point-to-point | Conditional and parallel routing |
| Error Handling | Simple retry/fail | Complex compensation logic |
| State Management | Minimal | Full workflow state tracking |
| Use Case | Simple A→B integration | Complex distribution scenarios |

### 8.4 Execution Modes

1. **Synchronous**
   - Request/Response pattern
   - Used for: SOAP, REST APIs
   - Timeout handling

2. **Asynchronous**
   - Fire-and-forget pattern
   - Used for: File processing, batch operations
   - Status tracking via correlation ID

### 8.5 Common Flow Execution Steps

```
1. Flow Trigger (Schedule, Event, API call)
2. Initialize execution context
3. Execute source adapter
4. For each transformation:
   - Load field mappings
   - Apply mappings with namespace context
   - Execute functions
5. Execute target adapter(s)
6. Log execution result
7. Handle errors/retries
```

---

## 9. Data Structures

### 9.1 Structure Types

1. **Flow Structures**
   - WSDL-based for SOAP services
   - Define operations and message structures
   - Include namespace definitions

2. **Message Structures**
   - XSD-based schemas
   - Reusable across flows
   - Version controlled

3. **Data Structures** (Deprecated for direct use)
   - JSON schemas
   - CSV definitions
   - Used only for validation before XML conversion

### 9.2 Structure Storage

```sql
flow_structures:
- id
- name
- wsdl_content      # Complete WSDL
- namespace         # Extracted namespace mappings (JSON)
- operations        # Available operations (JSON)
- business_component_id

message_structures:
- id
- name
- xsd_content       # XSD schema
- root_element      # Root element name
- target_namespace
```

---

## 10. Deployment Architecture

### 10.1 Environment Model

```
Development → Quality Assurance → Production
```

Each environment has:
- Separate database
- Environment-specific restrictions
- Independent configuration

### 10.2 Deployment Components

```
┌─────────────────────────────────┐
│         Load Balancer           │
└─────────────────────────────────┘
                │
    ┌───────────┴───────────┐
    │                       │
┌───┴────┐            ┌────┴───┐
│ Node 1 │            │ Node 2 │
│        │            │        │
│ Spring │            │ Spring │
│  Boot  │            │  Boot  │
│   JAR  │            │   JAR  │
└───┬────┘            └────┬───┘
    │                       │
    └───────────┬───────────┘
                │
        ┌───────┴────────┐
        │   PostgreSQL   │
        │   Database     │
        └────────────────┘
```

### 10.3 Deployment Process

1. Frontend builds to static files
2. Static files embedded in Spring Boot JAR
3. Single JAR deployment
4. Database migrations run automatically

---

## 11. Security Architecture

### 11.1 Security Processing Logic

```
Request Flow:
1. Incoming Request
   ↓
2. JWT Token Validation
   - Check signature
   - Verify expiration
   - Extract claims
   ↓
3. Role-Based Authorization
   - Check user role
   - Verify permissions
   - Apply environment restrictions
   ↓
4. Audit Logging
   - Log user action
   - Record timestamp
   - Track changes
   ↓
5. Process Request
```

### 11.2 Authentication
- JWT-based authentication
- Token refresh mechanism
- Session timeout handling

### 11.3 Authorization
- Role-based access control (RBAC)
- Roles: ADMINISTRATOR, DEVELOPER, INTEGRATOR, VIEWER
- Environment-based restrictions

### 11.4 Data Security
- TLS/SSL for all communications
- Certificate management for mutual TLS
- Encrypted credential storage
- Audit logging for all operations

---

## 12. Monitoring and Logging

### 12.1 Logging Architecture

```
Logging Flow:
1. Event Occurs
   ↓
2. Generate Log Entry
   - Timestamp
   - Correlation ID
   - Level (INFO/WARN/ERROR)
   - Category
   - Message
   - Context data
   ↓
3. Apply Filters
   - Log level threshold
   - Category filtering
   - Sensitive data masking
   ↓
4. Write to Destinations
   - Database (system_logs table)
   - File (rolling logs)
   - WebSocket (real-time)
   ↓
5. Retention Policy
   - Archive old logs
   - Purge expired logs
```

### 12.2 System Monitoring
- Flow execution metrics
- Adapter performance statistics
- Resource utilization
- Error rates and patterns

### 12.2 Message Tracking
- Correlation ID for end-to-end tracking
- Payload logging (configurable)
- Processing step timestamps
- Error details and stack traces

### 12.3 Audit Trail
- User actions
- Configuration changes
- Flow deployments
- Security events

---

## Appendix A: Common Integration Patterns

### A.1 SOAP to REST
```
SOAP Request → XML → Field Mapping → JSON → REST API
```

### A.2 Database to File
```
JDBC Query → ResultSet XML → Field Mapping → CSV → File System
```

### A.3 File to Multiple Systems
```
CSV File → XML → Orchestration → [System A, System B, System C]
```

---

## Appendix B: Best Practices

1. **Always define namespaces** in WSDL/XSD structures
2. **Use XPath with namespace prefixes** in field mappings
3. **Test field mappings** with sample data before deployment
4. **Monitor adapter connection pools** for resource leaks
5. **Implement proper error handling** at each step
6. **Use correlation IDs** for troubleshooting
7. **Version control** all flow configurations

---

## Document Version
- Version: 1.1
- Date: 2025-08-18
- Status: Updated with all 13 adapter types and comprehensive flow type details

## Next Steps
1. Review and amend this document
2. Identify any missing components
3. Clarify any architectural decisions
4. Use as reference for all future development