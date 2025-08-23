# XML-Native Design for Integrix Flow Bridge

## Current Issues

1. **JSON-XML Impedance Mismatch**:
   - Field mappings store source fields as JSON arrays like `["CelsiusToFahrenheit"]`
   - This flat structure cannot represent XML hierarchies properly
   - Namespaces are stored as JSON instead of being part of the XML

2. **Unused XPath Fields**:
   - FieldMapping entity has `sourceXPath` and `targetXPath` fields
   - These are not being used, instead JSON arrays are parsed

3. **SOAP Structure Issues**:
   - Current system treats SOAP elements as flat fields
   - Cannot express parent-child relationships (e.g., CelsiusToFahrenheit contains Celsius)
   - SOAP body structure is not properly handled

## Clean Slate Approach - What Needs to be Recreated

### 1. Database Changes

#### Drop JSON columns and add proper XML support:
```sql
-- Message Structures - Remove JSON columns
ALTER TABLE message_structures 
DROP COLUMN namespace,
DROP COLUMN metadata,
DROP COLUMN tags;

-- Flow Structures - Remove JSON columns  
ALTER TABLE flow_structures
DROP COLUMN namespace,
DROP COLUMN metadata,
DROP COLUMN tags;

-- Field Mappings - Complete redesign
ALTER TABLE field_mappings
DROP COLUMN source_fields,  -- No more JSON arrays
DROP COLUMN input_types,     -- No more JSON
DROP COLUMN visual_flow_data,-- No more JSON
DROP COLUMN function_node;   -- No more JSON

-- Make XPath fields required for XML flows
ALTER TABLE field_mappings
MODIFY COLUMN source_xpath VARCHAR(1000) NOT NULL,
MODIFY COLUMN target_xpath VARCHAR(1000) NOT NULL;
```

#### Add new tables for proper XML/namespace handling:
```sql
-- Namespaces for message structures
CREATE TABLE message_structure_namespaces (
    id VARCHAR(36) PRIMARY KEY,
    message_structure_id VARCHAR(36) NOT NULL,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (message_structure_id) REFERENCES message_structures(id)
);

-- Namespaces for flow structures
CREATE TABLE flow_structure_namespaces (
    id VARCHAR(36) PRIMARY KEY,
    flow_structure_id VARCHAR(36) NOT NULL,
    prefix VARCHAR(50),
    uri VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id)
);

-- WSDL operations for flow structures
CREATE TABLE flow_structure_operations (
    id VARCHAR(36) PRIMARY KEY,
    flow_structure_id VARCHAR(36) NOT NULL,
    operation_name VARCHAR(255) NOT NULL,
    soap_action VARCHAR(500),
    input_element_name VARCHAR(255),
    input_element_namespace VARCHAR(500),
    output_element_name VARCHAR(255),
    output_element_namespace VARCHAR(500),
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id)
);

-- New field mapping structure for XML
CREATE TABLE xml_field_mappings (
    id VARCHAR(36) PRIMARY KEY,
    transformation_id VARCHAR(36) NOT NULL,
    source_xpath VARCHAR(2000) NOT NULL,
    target_xpath VARCHAR(2000) NOT NULL,
    mapping_type ENUM('ELEMENT', 'ATTRIBUTE', 'TEXT', 'STRUCTURE') NOT NULL,
    is_repeating BOOLEAN DEFAULT FALSE,
    repeat_context_xpath VARCHAR(1000),
    transform_function TEXT,
    mapping_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (transformation_id) REFERENCES flow_transformations(id)
);
```

### 2. What You Need to Recreate

#### A. Delete and Recreate All:
1. **All Field Mappings** - They use JSON format and need to be XPath-based
2. **All Flow Structures** - To properly extract and store namespaces
3. **All Message Structures** - To properly extract and store namespaces
4. **All Direct Mapping Flows** - They reference the old field mappings

#### B. Steps to Recreate:

1. **Delete existing data**:
```sql
-- Delete in correct order due to foreign keys
DELETE FROM field_mappings;
DELETE FROM flow_transformations;
DELETE FROM integration_flows;
DELETE FROM flow_structure_messages;
DELETE FROM flow_structures;
DELETE FROM message_structures;
```

2. **Re-import XSDs for Message Structures**:
   - Upload XSD files again
   - System will parse and store namespaces properly in new tables
   - No JSON conversion

3. **Re-import WSDLs for Flow Structures**:
   - Upload WSDL files again
   - System will extract operations and namespaces into proper tables
   - Store operation details for SOAP services

4. **Recreate Field Mappings with XPath**:
   - For the Temperature Conversion example:
   ```
   Instead of:
   Source: ["Celsius"] → Target: "Celsius"
   
   Use:
   Source XPath: //con:CelsiusToFahrenheit/con:Celsius
   Target XPath: //tns:CelsiusToFahrenheit/tns:Celsius
   Mapping Type: TEXT
   ```

5. **Recreate Integration Flows**:
   - Link the new XPath-based field mappings
   - Ensure proper namespace prefixes are used

### 3. Entity Model Changes

```java
// FieldMapping.java - Simplified for XML
@Entity
@Table(name = "xml_field_mappings")
public class XmlFieldMapping {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "transformation_id", nullable = false)
    private FlowTransformation transformation;
    
    @Column(name = "source_xpath", nullable = false, length = 2000)
    private String sourceXPath;
    
    @Column(name = "target_xpath", nullable = false, length = 2000)
    private String targetXPath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false)
    private MappingType mappingType;
    
    @Column(name = "is_repeating")
    private boolean isRepeating;
    
    @Column(name = "repeat_context_xpath", length = 1000)
    private String repeatContextXPath;
    
    @Column(name = "transform_function", columnDefinition = "TEXT")
    private String transformFunction;
    
    @Column(name = "mapping_order")
    private Integer mappingOrder;
    
    // NO JSON FIELDS!
    
    public enum MappingType {
        ELEMENT,    // Map entire element
        ATTRIBUTE,  // Map attribute value
        TEXT,       // Map text content only
        STRUCTURE   // Define structure (no value mapping)
    }
}
```

### 4. Service Layer Changes

```java
// FlowStructureService.java
public class FlowStructureService {
    
    public FlowStructureDTO createFromWsdl(String wsdlContent) {
        // Parse WSDL
        Document wsdlDoc = parseXml(wsdlContent);
        
        // Extract namespaces directly - NO JSON
        Map<String, String> namespaces = extractNamespaces(wsdlDoc);
        
        // Save namespaces to flow_structure_namespaces table
        for (Map.Entry<String, String> ns : namespaces.entrySet()) {
            FlowStructureNamespace nsEntity = new FlowStructureNamespace();
            nsEntity.setPrefix(ns.getKey());
            nsEntity.setUri(ns.getValue());
            nsEntity.setFlowStructure(flowStructure);
            namespaceRepository.save(nsEntity);
        }
        
        // Extract operations
        List<WsdlOperation> operations = extractOperations(wsdlDoc);
        
        // Save operations to flow_structure_operations table
        for (WsdlOperation op : operations) {
            FlowStructureOperation opEntity = new FlowStructureOperation();
            opEntity.setOperationName(op.getName());
            opEntity.setSoapAction(op.getSoapAction());
            opEntity.setInputElementName(op.getInputElement());
            opEntity.setFlowStructure(flowStructure);
            operationRepository.save(opEntity);
        }
        
        // NO JSON STORAGE!
    }
}
```

### 5. XML Mapper Changes

```java
public class XmlFieldMapper {
    
    public String mapXmlFields(String sourceXml, 
                              List<XmlFieldMapping> mappings,
                              FlowStructure targetStructure) {
        
        Document sourceDoc = parseXml(sourceXml);
        Document targetDoc;
        
        // Load namespaces from database - NOT from JSON
        Map<String, String> namespaces = loadNamespacesFromDb(targetStructure);
        
        // For SOAP, create proper structure
        if (targetStructure.getWsdlContent() != null) {
            targetDoc = createSoapEnvelope(namespaces);
            
            // Get operation details from database
            FlowStructureOperation operation = loadOperation(targetStructure);
            
            // Create operation element in SOAP body
            Element body = getSoapBody(targetDoc);
            Element opElement = targetDoc.createElementNS(
                operation.getInputElementNamespace(),
                operation.getInputElementName()
            );
            body.appendChild(opElement);
        } else {
            targetDoc = createEmptyDocument();
        }
        
        // Apply XPath mappings
        XPath xpath = createXPath(namespaces);
        
        for (XmlFieldMapping mapping : mappings) {
            String value = extractValueByXPath(sourceDoc, mapping.getSourceXPath(), xpath);
            setValueByXPath(targetDoc, mapping.getTargetXPath(), value, xpath);
        }
        
        return documentToString(targetDoc);
    }
}
```

### 6. UI Changes Required

The UI needs to be updated to:

1. **Remove all JSON field arrays** - No more `["fieldName"]` format
2. **Add XPath builders** - Help users create valid XPath expressions
3. **Show XML structure trees** - Parse and display XML/XSD/WSDL structure
4. **Namespace awareness** - Show and use proper namespace prefixes
5. **SOAP operation understanding** - Show operation structure for SOAP services

Example of new field mapping UI:
```
Source Structure (from XSD):          Target Structure (from WSDL):
└── CelsiusToFahrenheit              └── soap:Envelope
    └── Celsius                          └── soap:Body
                                            └── CelsiusToFahrenheit
                                                └── Celsius

Mapping:
Source XPath: //con:CelsiusToFahrenheit/con:Celsius
Target XPath: //soap:Body/tns:CelsiusToFahrenheit/tns:Celsius
Type: TEXT (extract text content only)
```

## Summary

To implement XML-native approach, you need to:

1. **Delete all existing**:
   - Field mappings
   - Flow transformations  
   - Integration flows
   - Flow structures
   - Message structures

2. **Update database schema**:
   - Remove all JSON columns
   - Add proper namespace tables
   - Make XPath fields required
   - Add operation tables for SOAP

3. **Re-import all XSDs and WSDLs**:
   - System will store them properly without JSON

4. **Recreate all field mappings**:
   - Use XPath expressions only
   - No JSON arrays

5. **Update the UI**:
   - Remove JSON-based field mapping
   - Add XPath-based mapping with namespace support

This clean-slate approach will eliminate all JSON-XML conversion issues and provide proper XML handling throughout the system.