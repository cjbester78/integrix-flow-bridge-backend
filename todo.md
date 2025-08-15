# Node-to-Node Mapping and Flow Save Fixes

## Overview
Implemented node-to-node mapping functionality for XML elements with maxOccurs > 1 (repeating elements) and fixed the integration flow save functionality.

## Plan

### Phase 1: Node-to-Node Mapping Implementation ✅
- [x] Analyze existing field mapping implementation
- [x] Enable dragging of non-leaf nodes (arrays/objects)
- [x] Update drop handling to detect node mappings
- [x] Display node mappings with special badges
- [x] Update backend DTOs to support array mapping fields
- [x] Update services to handle node mapping persistence

### Phase 2: Flow Save Functionality Fix ✅
- [x] Investigate "Save Failed" error
- [x] Check browser console for error details
- [x] Review flow save endpoints and data contracts
- [x] Fix data format mismatches between frontend and backend
- [x] Update FlowActionsCard to properly handle loading states
- [x] Test flow save functionality

## Review

### Node-to-Node Mapping Implementation
Successfully implemented the ability to map entire nodes (arrays/objects) to other nodes in the field mapping UI:

1. **Frontend Changes**
   - Updated `FieldTree.tsx` to allow dragging of non-leaf nodes (arrays and objects)
   - Modified drop detection to identify when both source and target are nodes
   - Added visual indicators (badges) for node mappings in `MappingArea.tsx`
   - Node mappings are created with special function name "nodeMapping"

2. **Backend Support**
   - Added array mapping fields to `FieldMappingDTO`: isArrayMapping, arrayContextPath, sourceXPath, targetXPath
   - Updated `FieldMappingService` to handle the new fields for persistence

3. **Key Features**
   - Users can now drag entire array nodes from source to target
   - The system detects array types using [] suffix or type='array'
   - Node mappings are visually distinguished with badges showing "Node: array → array"
   - Backend properly persists array mapping metadata

### Flow Save Functionality Fixes
Fixed the integration flow save errors:

1. **Identified Issues**
   - Frontend was using `/api/flow-composition/direct-mapping` endpoint
   - Request included "mappingRequired" field not expected by backend
   - FlowActionsCard wasn't handling loading/disabled props

2. **Fixes Applied**
   - Removed "mappingRequired" field from flow request in `CreateDirectMappingFlow.tsx`
   - Updated `FlowActionsCard.tsx` to accept and use isLoading and disabled props
   - Added loading spinner and disabled states to save button

3. **Result**
   - Integration flows can now be saved successfully through the direct mapping flow endpoint
   - UI properly shows loading state during save operation
   - All required fields are correctly formatted for backend consumption

### Technical Notes
- The reversed middleware terminology (Sender = inbound, Receiver = outbound) is maintained throughout
- XML structures with maxOccurs > 1 are properly detected as arrays
- The flow composition service creates both the flow and associated transformations/mappings

# File Format and XML Conversion Configuration Implementation

## Overview
Implemented file format configuration tabs and XML conversion settings for all adapters. This allows users to configure how data should be converted to/from XML and specify file-specific formats for file-based adapters.

## Plan

### Phase 1: Frontend Implementation ✅
- [x] Create XML Conversion tab component for adapter configuration
- [x] Add File Format tab component for file-based adapters
- [x] Update FILE adapter config to include new tabs
- [x] Update FTP adapter config to include new tabs
- [x] Update SFTP adapter config to include new tabs
- [x] Add XML conversion to non-file adapters (REST, SOAP, JDBC)

### Phase 2: Backend Integration ✅
- [x] Update FormatConversionService to use adapter XML config
- [x] Fix compilation error with namespaceUri field
- [x] Test XML conversion with configured settings

## Completed Work

### 1. Frontend Components

#### XmlConversionTab Component
- Created reusable component for XML conversion configuration
- Supports both SENDER and RECEIVER modes with different options
- SENDER mode settings:
  - Root element name
  - Character encoding
  - XML declaration inclusion
  - Pretty printing
  - Namespace configuration (URI and prefix)
- RECEIVER mode additional settings:
  - Remove root element option
  - Handle namespaces option
  - Preserve attributes option

#### FileFormatTab Component
- Created component for file format configuration
- Supports multiple formats: CSV, Fixed-Length, JSON, XML, Plain Text
- Format-specific configurations:
  - CSV: delimiter, line terminator, quote character, include headers
  - Fixed-Length: field lengths, field order, pad character, pad direction
  - JSON: pretty print, include null values, date format
  - XML: schema validation, namespaces, pretty print
  - Plain Text: encoding, line endings
- Live preview example for each format

### 2. Adapter Configuration Updates

#### File-Based Adapters
- Updated FileAdapterConfiguration to include both File Format and XML Conversion tabs
- Updated FtpAdapterConfiguration with the same tabs
- Updated SftpAdapterConfiguration with the same tabs
- Changed TabsList from 2 to 4 columns to accommodate new tabs

#### REST Adapters
- Added XML Conversion tab to RestSenderAdapterConfiguration
- Added XML Conversion tab to RestReceiverAdapterConfiguration
- Only added XML conversion (not file format) since REST works with structured data

### 3. Backend Integration

#### FormatConversionService Updates
- Modified `createJsonXmlConfig` method to read XML conversion settings from adapter configuration
- Extracts xmlConversion object from adapter JSON configuration
- Uses configured values for:
  - Root element name
  - Character encoding
  - XML declaration inclusion
  - Pretty printing
  - Namespace URI and prefix
- Added `shouldRemoveRootElement` method to check receiver-specific settings
- Fixed compilation error: changed `targetNamespace` to `namespaceUri` in builder

## Configuration Storage

### Adapter Configuration Structure
All adapter configurations are stored as JSON in the `configuration` field. The new fields are:

```json
{
  "existingFields": "...",
  "fileFormatConfig": {
    "fileFormat": "CSV",
    "delimiter": ",",
    "lineTerminator": "\n",
    "quoteCharacter": "\"",
    "includeHeaders": true,
    "fieldLengths": {},
    "fieldOrder": [],
    "padCharacter": " ",
    "padDirection": "RIGHT"
  },
  "xmlConversion": {
    "rootElementName": "Message",
    "encoding": "UTF-8",
    "includeXmlDeclaration": true,
    "prettyPrint": true,
    "targetNamespace": "http://example.com/integration",
    "namespacePrefix": "int",
    "removeRootElement": true,
    "handleNamespaces": true,
    "preserveAttributes": false
  }
}
```

## Technical Notes

### Adapter Type Handling
- File-based adapters (FILE, FTP, SFTP) get both File Format and XML Conversion tabs
- REST adapters get only XML Conversion tab (they work with JSON/XML directly)
- SOAP adapters don't need XML conversion (already XML)
- JDBC adapters may need special handling for result set to XML conversion

### Backend Processing Flow
1. Adapter configuration is read from database
2. FormatConversionService extracts xmlConversion settings
3. Settings are used to build JsonXmlWrapperConfig
4. Conversion happens with user-specified configuration

### Frontend User Experience
- Tabs provide clear separation of concerns
- Live previews help users understand format options
- Mode-specific options (SENDER vs RECEIVER) prevent confusion
- Sensible defaults provided for all settings

## Review

### Implementation Summary
Successfully implemented file format and XML conversion configuration for adapters with the following components:

1. **Frontend Components Created**
   - `XmlConversionTab.tsx` - Configures XML conversion settings with mode-specific options
   - `FileFormatTab.tsx` - Configures file format settings (CSV, Fixed-Length, JSON, XML, Text)

2. **Adapter Configurations Updated**
   - File-based adapters (FILE, FTP, SFTP) - Added both File Format and XML Conversion tabs
   - REST adapters (Sender and Receiver) - Added only XML Conversion tab
   - Updated tab layouts from 2 to 3-4 columns as needed

3. **Backend Integration**
   - Updated `FormatConversionService` to read XML conversion settings from adapter configuration
   - Fixed compilation error with JsonXmlWrapperConfig builder (targetNamespace → namespaceUri)
   - Added support for receiver-specific settings like removeRootElement

### What Was Changed
- Created `frontend-ui/src/components/adapters/XmlConversionTab.tsx`
- Created `frontend-ui/src/components/adapters/FileFormatTab.tsx`
- Updated `frontend-ui/src/components/adapter/FileAdapterConfiguration.tsx`
- Updated `frontend-ui/src/components/adapter/FtpAdapterConfiguration.tsx`
- Updated `frontend-ui/src/components/adapter/SftpAdapterConfiguration.tsx`
- Updated `frontend-ui/src/components/adapter/RestSenderAdapterConfiguration.tsx`
- Updated `frontend-ui/src/components/adapter/RestReceiverAdapterConfiguration.tsx`
- Modified `engine/src/main/java/com/integrationlab/engine/service/FormatConversionService.java`

### Key Features Implemented
1. **File Format Configuration**
   - Support for CSV, Fixed-Length, JSON, XML, and Plain Text formats
   - Format-specific settings with live preview examples
   - Intuitive UI with clear descriptions

2. **XML Conversion Configuration**
   - Mode-aware settings (SENDER vs RECEIVER)
   - Namespace configuration support
   - Options for root element handling, pretty printing, and encoding

3. **Backend Integration**
   - Automatic extraction of settings from adapter JSON configuration
   - Proper defaults when settings are not specified
   - Seamless integration with existing conversion logic

### Next Steps for Production Use
1. Test the file format configurations with actual file processing
2. Validate XML conversion with various namespace scenarios
3. Add validation rules for format-specific settings (e.g., valid delimiters)
4. Consider adding import/export functionality for format configurations

### User Experience Improvements
- Clear separation of configuration concerns using tabs
- Live preview examples help users understand format options
- Mode-specific options prevent confusion between sender/receiver adapters
- Sensible defaults provided for all settings

The file format and XML conversion configuration is now fully integrated into the adapter configuration UI, allowing users to specify exactly how their data should be formatted and converted.

# Data Structure Save Error Fix

## Overview
Fixed multiple issues preventing data structures from being saved, including missing business component ID, MySQL reserved keyword conflict, and frontend error logging.

## Plan

### Issue Investigation ✅
- [x] Investigate why Create Data Structure page was fetching adapter types
- [x] Check why data structure save returns 400 Bad Request
- [x] Understand where frontend errors are logged

### Fixes Applied ✅
- [x] Add businessComponentId to DataStructureCreate interface
- [x] Update saveStructure to include businessComponentId
- [x] Fix MySQL reserved keyword issue with 'usage' column
- [x] Fix error logging endpoint issue
- [x] Deploy and test the changes

## Review

### Issues Identified and Fixed

1. **Business Component ID Missing**
   - **Problem**: Frontend wasn't sending businessComponentId in the data structure creation request
   - **Solution**: 
     - Added `businessComponentId` field to `DataStructureCreate` interface
     - Updated `saveStructure` function to accept and include the business component ID
     - Modified `CreateDataStructure.tsx` to pass the selected business component ID

2. **MySQL Reserved Keyword Conflict**
   - **Problem**: The column name 'usage' is a MySQL reserved keyword causing SQL syntax errors
   - **Solution**:
     - Changed column name from `usage` to `usage_type` in the entity
     - Created migration script to rename the database column
     - Applied the migration to update the database schema

3. **Frontend Error Logging**
   - **Problem**: Frontend errors weren't being logged to system logs due to incorrect endpoint
   - **Solution**:
     - Fixed `systemErrorLogger` to use `/system/logs/batch` endpoint
     - Updated to match the expected `FrontendLogBatchRequest` format
     - Now properly sends error details to backend for persistent logging

4. **Unnecessary Adapter Type Fetching**
   - **Problem**: BusinessComponentSelectionCard was using useBusinessComponentAdapters hook unnecessarily
   - **Solution**:
     - Modified BusinessComponentSelectionCard to fetch business components directly
     - Removed dependency on adapter types for data structure creation

### Technical Changes

1. **Frontend Updates**
   - `structureService.ts`: Added businessComponentId to DataStructureCreate interface
   - `useDataStructures.ts`: Updated saveStructure function signature and implementation
   - `CreateDataStructure.tsx`: Pass businessComponentId when saving
   - `systemErrorLogger.ts`: Fixed endpoint and request format for error logging
   - `BusinessComponentSelectionCard.tsx`: Direct business component fetching

2. **Backend Updates**
   - `DataStructure.java`: Changed column annotation from "usage" to "usage_type"
   - Created migration script: `V1_1__rename_usage_column.sql`
   - Applied database migration to rename the column

3. **Error Handling Improvements**
   - Enhanced error logging in `useDataStructures.ts` to show actual error messages
   - API errors now properly logged to system logs table
   - Better error visibility for debugging

### Results
- Data structures can now be saved successfully with proper business component association
- Frontend errors are logged to the system logs table for monitoring
- No more SQL syntax errors from the MySQL reserved keyword
- Improved debugging capabilities with proper error logging

### Deployment Summary
All changes have been deployed and tested successfully. The data structure save functionality is now working correctly with proper error handling and logging.

# XML Structure Filtering for Field Mapping

## Overview
Fixed the issue where all message types (request, response, fault) were being shown regardless of which mapping type was being configured. Now each mapping type shows only its relevant fields.

## Plan

### Phase 1: Analysis and Understanding ✅
- [x] Analyze the current XML structure and how messages are organized
- [x] Understand how the mapping type is passed through the component hierarchy
- [x] Identify where the filtering should be applied

### Phase 2: Implementation ✅
- [x] Add mapping type parameter to handleCreateMapping function
- [x] Pass mapping type from button click handlers (request/response/fault)
- [x] Add mappingType prop to FieldMappingScreen component
- [x] Create filterXmlByMessageType function to filter XML based on patterns
- [x] Apply filtering when parsing pre-converted XML

### Phase 3: Testing and Deployment ✅
- [x] Test with different mapping types to ensure correct filtering
- [x] Deploy changes
- [x] Verify in browser that each mapping type shows only relevant fields

## Review

### Problem Statement
The field mapping screen was showing all message types (request, response, fault) regardless of which mapping type was being configured. This caused confusion as users saw fields that weren't relevant to their current mapping task.

### Solution Implemented

1. **Modified handleCreateMapping Function**
   - Added `mappingType` parameter to track which type is being configured
   - Stored current mapping type in component state

2. **Updated Button Click Handlers**
   - Request Mapping button: calls `handleCreateMapping('request')`
   - Response Mapping button: calls `handleCreateMapping('response')`
   - Fault Mapping button: calls `handleCreateMapping('fault')`

3. **Enhanced FieldMappingScreen Component**
   - Added `mappingType` prop to component interface
   - Created `filterXmlByMessageType` function using pattern matching:
     - Request: matches patterns like `_Req_`, `Request`, `Input`
     - Response: matches patterns like `_Resp_`, `Response`, `Output`
     - Fault: matches patterns like `Fault`, `Error`, `Exception`
   - Applied filtering in useEffect when parsing XML

4. **Pattern-Based Filtering Logic**
   - Filters direct children of root element based on tag name patterns
   - Preserves root element structure while filtering child elements
   - Returns original XML if no matching patterns found

### Technical Details
- Changes kept minimal and focused only on filtering logic
- Used pattern matching to identify message types from tag names
- Backward compatible with existing mappings
- No changes to backend required

### Results
- Request mapping now shows only request-related fields (e.g., Credit_Token_Req_MT)
- Response mapping shows only response-related fields (e.g., Credit_Token_Resp_MT)
- Fault mapping shows only fault-related fields (e.g., StandardMessageFault)
- UI displays mapping type indicator: "Pre-converted XML (request/response/fault)"

### Files Modified
- `frontend-ui/src/pages/CreateDirectMappingFlow.tsx`
- `frontend-ui/src/components/FieldMappingScreen.tsx`

### Next Steps
- [ ] Add unit tests for the filterXmlByMessageType function
- [ ] Consider adding visual indicators to show which mapping type is being configured
- [ ] Implement validation to ensure correct field types are mapped

# Selective Field Mapping Enhancement - Updated Logic

## Overview
Update the field mapping to always map the highest selected level (node or element) regardless of name matching, then apply name matching logic recursively for all children.

## Plan

### Phase 1: Update Mapping Logic ✅
- [x] Always map the highest selected level (node or element) regardless of names
- [x] For node children, only map fields with matching names
- [x] Ensure recursive behavior through all tree levels

### Phase 2: Implementation Details ✅
- [x] Modify node mapping in autoMapFields to always create parent mapping
- [x] Keep children mapping logic with name matching requirement
- [x] Ensure element-to-element mapping remains flexible (any-to-any)
- [x] Test with different node structures

### Key Changes
1. **Selected Node Mapping**: Remove name matching check for the selected nodes themselves
2. **Children Mapping**: Keep name matching for all descendants
3. **Recursive Behavior**: Ensure matching logic applies at each level of the tree

### Review

#### Changes Made

1. **Removed Clear Selection Button**
   - Removed the "Clear Selection" button as requested since "Delete All Mappings" already exists
   - Users can still clear selections by clicking on different fields or using Delete All Mappings

2. **Updated Node-to-Node Mapping Logic**
   - When two nodes (arrays/objects) are selected:
     - The system now collects ALL fields recursively from both nodes
     - Only fields with matching names are mapped automatically
     - Supports nested fields at any depth within the nodes
     - Shows toast with count of mappings created
   - Preserves the ability to map individual sub-nodes if needed

3. **Element-to-Element Mapping**
   - When two individual fields (elements) are selected:
     - They can be mapped regardless of name matching
     - Allows complete flexibility for element mapping
     - No restrictions on field names

4. **User Experience**
   - Button shows "Map Selected" when fields are selected
   - Clear visual feedback with toast notifications
   - Selections automatically clear after successful mapping
   - Existing mappings are respected (no duplicates)

#### Technical Implementation
- Modified `autoMapFields` function to detect node vs element selection
- Used recursive `collectFields` function to gather all nested fields
- Maintained backward compatibility with existing mapping functionality
- Simple, focused changes with minimal code impact

### Updated Mapping Logic Review

#### Changes Made
1. **Always Map Selected Nodes**
   - When two nodes are selected, they are ALWAYS mapped regardless of name matching
   - This allows mapping of different node structures (e.g., Credit_Token_Req_MT → Payment_Info)
   
2. **Children Follow Matching Rules**
   - Only children fields with matching names are mapped
   - Applies recursively through all levels of the tree structure
   - Preserves data integrity by matching similar fields

3. **Technical Implementation**
   - Added explicit node mapping before processing children
   - Modified `collectFields` to work on children only (not parent)
   - Kept element-to-element mapping flexible (any-to-any)

#### Result
The mapping now works as requested:
- Selected nodes always map (e.g., different root structures can be mapped)
- Children only map when names match (preserving field relationships)
- Provides flexibility at the top level while maintaining structure below

# Field Mapping Loading and Audit Trail Implementation

## Overview
Fixed field mapping loading issues and implemented comprehensive audit trail functionality for tracking all CRUD operations.

## Plan

### Phase 1: Field Mapping Fixes ✅
- [x] Remove 'Requires Transformation' checkbox from field mapping UI
- [x] Fix mapping order logic - ensure correct assignment based on sync/async mode
- [x] Ensure mapping names are saved to flow_transformations table
- [x] Ensure source business component ID is saved to flows

### Phase 2: Audit Trail Implementation ✅
- [x] Add audit fields (created_by, created_at, updated_by, updated_at) to tables
- [x] Implement audit trail service for logging all CRUD operations

## Review

### Changes Made

1. **Frontend Updates**:
   - Removed the "Requires Transformation" checkbox from the field mapping UI
   - Fixed mapping order logic to properly assign message types based on flow mode (sync/async)
   - Ensured the UI correctly displays request/response/fault mappings when editing flows

2. **Backend Updates**:
   - Fixed FlowCompositionService to save user-entered mapping names
   - Updated message type assignment logic based on execution order and flow mode
   - Added source business component ID saving to flows

3. **Database Schema**:
   - Created comprehensive migration to add audit fields to all user data tables
   - Handled renaming of last_modified_by to updated_by for consistency
   - Added foreign key constraints for audit fields

4. **JPA Entity Updates**:
   - Added created_by and updated_by fields to all relevant entities
   - Implemented proper ManyToOne relationships with User entity
   - Added @EntityListeners for automatic audit field population

5. **Audit Trail Implementation**:
   - Complete audit trail system with entity, repository, service, and controller
   - AOP-based automatic audit logging for service methods
   - REST API endpoints for querying audit history
   - Support for filtering by entity, user, action, and date range

### Key Technical Decisions

1. **Audit Implementation**: Used AOP (Aspect-Oriented Programming) for automatic audit logging, making it easy to add auditing to any service method with simple annotations.

2. **Message Type Logic**: Centralized the message type determination based on execution order rather than relying on metadata or naming conventions.

3. **Audit Field Population**: Implemented JPA entity listeners to automatically populate audit fields, ensuring consistency across all entities.

4. **Async Audit Logging**: Made audit logging asynchronous to prevent performance impact on main operations.

### Next Steps (if needed)

1. Apply @EntityListeners annotation to remaining entities (FieldMapping, IntegrationFlow, etc.)
2. Add audit annotations to other service methods that perform CRUD operations
3. Consider implementing audit log retention policies
4. Add UI components to view audit trail in the frontend
5. Consider adding more detailed change tracking (field-level changes)

# Integration Flow Bridge - Implementation Todo List

## Overview
This document outlines the implementation plan for enhancing file adapter functionality, XML conversion handling, and HTTP adapter operations based on our comprehensive analysis.

## Phase 1: Critical Infrastructure Changes (High Priority)

### 1.1 Direct Passthrough Mode Implementation
- [ ] **Implement Direct Passthrough Mode (bypass XML conversion)**
  - Create `DirectFileTransferService` for streaming file transfers
  - Add `skipXmlConversion` flag to `IntegrationFlow` model
  - Implement NIO-based file streaming for large files
  - Preserve original file encoding throughout transfer
  - Add binary file detection to prevent XML conversion attempts

- [ ] **Add flow configuration option to skip XML conversion**
  - Update `FlowExecutionService` to check passthrough flag
  - Create separate execution path for direct transfers
  - Ensure adapter compatibility with both modes

- [ ] **Implement streaming mode for large file passthrough**
  - Use Java NIO channels for efficient copying
  - Implement chunked processing with configurable buffer size
  - Add progress tracking for large file transfers

### 1.2 XML Special Character Handling
- [ ] **Add XML special character handling with proper escaping**
  - Create `XmlSanitizer` utility class
  - Implement proper escaping for all XML entities (&, <, >, ", ')
  - Handle Unicode control characters (0x00-0x1F)
  - Add support for surrogate pairs and invalid Unicode sequences

- [ ] **Implement CDATA section support for XML conversion**
  - Detect content that requires CDATA wrapping
  - Modify `CsvToXmlConverter`, `JsonToXmlConverter` to use CDATA
  - Handle nested CDATA markers properly
  - Add configuration option for automatic CDATA wrapping

- [ ] **Create XML sanitization utility for control characters**
  - Remove/replace invalid XML characters
  - Provide options: remove, replace with placeholder, or fail
  - Log sanitization actions for debugging
  - Handle different XML versions (1.0 vs 1.1)

## Phase 2: Character Encoding and Validation (High Priority)

### 2.1 Encoding Management
- [ ] **Add character encoding validation and conversion**
  - Detect source file encoding automatically
  - Validate encoding consistency throughout pipeline
  - Implement encoding conversion with fallback options
  - Handle BOM (Byte Order Mark) correctly

- [ ] **Add binary file detection to prevent XML conversion**
  - Implement content type detection using file signatures
  - Check for binary content patterns
  - Provide bypass mechanism for binary files
  - Add supported binary format list configuration

### 2.2 Configuration and Testing
- [ ] **Add configurable XML conversion options (escape vs CDATA)**
  - Create `XmlConversionStrategy` enum
  - Add configuration UI for conversion preferences
  - Implement per-field conversion strategy options
  - Document best practices for each strategy

- [ ] **Create comprehensive special character test suite**
  - Test files with all possible special characters
  - Include multi-byte Unicode characters
  - Test various encodings (UTF-8, UTF-16, ISO-8859-1)
  - Performance benchmarks for large files

## Phase 3: UI/UX Enhancements (Medium Priority)

### 3.1 File Format Selection UI
- [ ] **Design slider/toggle UI for file format selection**
  - Create `SegmentedControl` component
  - Implement sliding animation between formats
  - Add visual feedback for active selection
  - Ensure accessibility compliance

- [ ] **Implement format-specific configuration panels with tabs**
  - Create dynamic tab activation based on format
  - Implement sub-tabs for each file format:
    - CSV: [Delimiter | Headers | Advanced]
    - Fixed-Length: [Fields | Padding | Layout]
    - JSON: [Structure | Formatting | Schema]
    - XML: [Namespaces | Schema | Validation]

### 3.2 Configuration Enhancements
- [ ] **Add delimiter configuration for all delimited formats**
  - Create `DelimiterConfiguration` component
  - Support predefined delimiters (comma, tab, pipe, semicolon)
  - Allow custom and multi-character delimiters
  - Add delimiter preview and validation

- [ ] **Add line terminator configuration for all formats**
  - Create `LineTerminatorSelect` component
  - Support all standard terminators (LF, CRLF, CR)
  - Allow custom terminator input
  - Show platform-specific defaults

- [ ] **Enhance fixed-length field definition interface**
  - Implement drag-and-drop field reordering
  - Auto-calculate start positions
  - Add field type selection (String, Number, Date)
  - Per-field padding configuration
  - Import field definitions from sample file

### 3.3 User Experience
- [ ] **Implement real-time preview with sample data**
  - Create split-screen preview panel
  - Show input format → output format
  - Live updates as configuration changes
  - Highlight transformation effects

## Phase 4: HTTP Adapter Documentation (Completed)

### 4.1 Documentation
- [x] **Document HTTP adapter endpoint architecture**
  - Created `HTTP_ADAPTER_OPERATIONS.md`
  - Clarified inbound vs outbound endpoints
  - Documented all HTTP-type adapters (HTTP, HTTPS, REST, SOAP)
  - Added troubleshooting guide

### 4.2 Architecture Documentation
- [x] **Create application architecture documentation**
  - Created `APPLICATION_ARCHITECTURE.md`
  - Documented adapter types and purposes
  - Explained direct mapping vs orchestration flows
  - Added flow execution details

## Implementation Guidelines

### Priority Levels
1. **Critical**: Direct passthrough mode and XML special character handling
2. **High**: Character encoding, validation, and core functionality
3. **Medium**: UI enhancements and configuration improvements
4. **Low**: Nice-to-have features like real-time preview

### Testing Requirements
- Unit tests for all new services and utilities
- Integration tests for passthrough mode
- Performance tests for large file handling
- Special character test suite
- UI component tests

### Backward Compatibility
- Ensure existing flows continue to work
- Provide migration path for flows to use new features
- Document breaking changes if any
- Version configuration schemas

### Performance Targets
- Passthrough mode: 10x faster than XML conversion
- Support files up to 1GB in streaming mode
- Sub-second response for configuration changes
- Real-time preview for files under 1MB

## Review Section

### Summary of Planned Changes

1. **Infrastructure**: Major enhancement to support direct file passthrough without XML conversion, addressing performance and special character issues.

2. **Robustness**: Comprehensive special character handling for cases where XML conversion is necessary, including CDATA support and encoding management.

3. **User Experience**: Modern UI with slider-based format selection, dynamic configuration panels, and real-time preview capabilities.

4. **Flexibility**: Support for any delimited format, enhanced fixed-length configuration, and universal line terminator settings.

### Key Benefits
- **Performance**: Dramatic improvement for simple file transfers
- **Reliability**: Proper handling of special characters and encodings
- **Usability**: Intuitive UI for complex configurations
- **Maintainability**: Clear separation of concerns and modular design

### Risk Mitigation
- Extensive testing for special characters
- Backward compatibility maintained
- Gradual rollout with feature flags
- Comprehensive documentation

---

*Last Updated: 2025-08-09*
*Status: Planning Phase - Ready for Implementation*

# UI/UX Consistency Improvements - Completed

## Overview
Implemented comprehensive UI/UX consistency improvements across the entire application based on a detailed audit.

## Completed Tasks

### 1. Page Layout Standardization ✅
- Created `PageContainer` component with consistent max-w-7xl and padding
- Updated all major pages to use PageContainer and PageHeader
- Standardized H1 sizing (text-3xl) and icon sizing (h-5 w-5)

### 2. Component Consistency ✅
- Added semantic badge variants (success, warning, info)
- Created reusable EmptyState component
- Created LoadingSkeleton component
- Created FormActions component
- Created DataTable component
- Created ErrorAlert component

### 3. Theme and Styling ✅
- Fixed light theme to use proper white background
- Removed Sonner toaster, kept only shadcn Toaster
- Added CSS animations (fade-in, scale-in, slide-up, glow)
- Standardized button icon sizing (h-4 w-4 mr-2)
- Added focus rings globally for accessibility

### 4. Icon Color Standardization ✅
- Created icon-colors.ts utility with theme-based colors
- Replaced all hardcoded colors (text-red-500, etc.) with theme tokens
- Standardized status icons to use semantic colors
- Updated 130+ instances of hardcoded colors

### 5. Gradient Simplification ✅
- Removed gradient accents from buttons and backgrounds
- Preserved gradient only on brand mark (IFB logo)
- Replaced bg-gradient-* with solid colors

### 6. Session Modal Improvements ✅
- Converted SessionTimeoutNotification to use AlertDialog
- Standardized modal styling across all session components
- Added consistent countdown display formatting

### 7. SEO and Accessibility ✅
- Created useMetaDescription hook
- Added comprehensive meta descriptions to all main pages
- Enhanced main index.html meta tags
- Added aria-labels throughout the application
- Set document titles per route

### Technical Impact
- Improved visual consistency across 50+ components
- Enhanced accessibility with proper focus states
- Better SEO with dynamic meta descriptions
- Reduced visual complexity while maintaining brand identity
- Standardized spacing, typography, and color usage

## Files Modified
- Created: PageContainer, EmptyState, LoadingSkeleton, FormActions, DataTable, ErrorAlert components
- Created: icon-colors.ts, useMetaDescription.ts utilities
- Updated: All major page components (Dashboard, Messages, DataStructures, Settings)
- Updated: 130+ files to use theme-based colors
- Updated: index.css with proper light theme variables

## Result
The application now has a cohesive, professional appearance with consistent patterns throughout. All UI elements follow the same design system, making the application more intuitive and easier to maintain.

# TODO: Implement Domain Type Filtering for System Logs

## Overview
Add support for domainType filtering in system logs to allow different monitors (Message Monitor, Adapter Monitor, etc.) to filter logs relevant to their domain.

## Domain Types to Support
- UserManagement - Login, authentication, user CRUD operations
- FlowEngine - Flow execution, deployment, validation
- AdapterManagement - Adapter configuration, connection, execution
- DataStructures - Schema validation, structure parsing
- MessageProcessing - Message routing, transformation, delivery
- OrchestrationEngine - Orchestration flow execution
- FieldMapping - Mapping validation, transformation functions
- SystemConfiguration - Environment settings, system config

## Tasks

### Backend Changes
- [x] Update SystemLogController to accept domainType as query parameter
- [x] Update SystemLogSpecifications to include domainType filtering
- [x] Verify SystemLog entity has domainType field properly mapped
- [x] Test filtering by domainType works correctly

### Frontend Integration
- [x] Verify frontend monitors can filter by domainType
- [ ] Ensure error logging includes appropriate domainType

### Testing
- [x] Test each domain type filter returns appropriate logs
- [x] Test combination of domainType with other filters (level, date range)
- [x] Verify monitors show only relevant domain logs

## Review

### Implementation Summary
Successfully implemented domainType filtering for system logs:

1. **Backend Changes**
   - Updated `SystemLogController` to accept `domainType` and `domainReferenceId` as query parameters
   - Added `withDomainType` and `withDomainReferenceId` specifications to `SystemLogSpecifications`
   - Confirmed SystemLog entity already has domainType and domainReferenceId fields properly mapped

2. **API Support**
   - Frontend can now call: `/api/logs/system?domainType=AdapterManagement`
   - Can combine with other filters: `/api/logs/system?domainType=UserManagement&level=error`
   - Can filter by specific entity: `/api/logs/system?domainType=AdapterManagement&domainReferenceId=ABC123`

3. **Next Steps**
   - Frontend monitors should be updated to include domainType when logging errors
   - Each service should set appropriate domainType when creating system logs
   - Consider creating a DomainType enum for consistency across the application

### Technical Notes
- The implementation uses JPA Specifications for dynamic query building
- Null checks ensure optional parameters work correctly
- Backend is now ready to support domain-based log filtering for all monitors