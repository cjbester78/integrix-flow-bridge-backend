# PostgreSQL Migration for Better XML Support

## Why PostgreSQL?

MySQL's XML support is limited:
- No native XML type (stores as string)
- Limited XPath 1.0 only
- No XML indexing
- Poor namespace handling

PostgreSQL offers:
- Native XML data type
- Better XPath support
- XML indexing
- Proper namespace handling
- XML validation

## Migration Steps

### 1. Install PostgreSQL
```bash
# macOS
brew install postgresql@15
brew services start postgresql@15

# Ubuntu/Debian
sudo apt install postgresql-15
```

### 2. Create Database
```sql
CREATE DATABASE integrixflowbridge;
CREATE USER integrix WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE integrixflowbridge TO integrix;
```

### 3. Update Application Configuration
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/integrixflowbridge
    username: integrix
    password: your_password
    driver-class-name: org.postgresql.Driver
```

### 4. PostgreSQL Schema with Native XML

```sql
-- Message structures with native XML
CREATE TABLE message_structures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    xsd_content XML NOT NULL,
    business_component_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT valid_xsd CHECK (xml_is_well_formed_document(xsd_content::text))
);

-- Flow structures with native XML
CREATE TABLE flow_structures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    processing_mode VARCHAR(20) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    wsdl_content XML,
    business_component_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_wsdl CHECK (wsdl_content IS NULL OR xml_is_well_formed_document(wsdl_content::text))
);

-- XML field mappings with XPath
CREATE TABLE xml_field_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transformation_id UUID NOT NULL,
    source_xpath VARCHAR(2000) NOT NULL,
    target_xpath VARCHAR(2000) NOT NULL,
    mapping_type VARCHAR(20) NOT NULL,
    namespace_mappings JSONB, -- Store namespace prefix mappings
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on XML content
CREATE INDEX idx_message_structures_xml ON message_structures USING GIN ((
    xpath('//xs:element/@name', xsd_content, 
    ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])
));

-- Function to extract namespaces from XML
CREATE OR REPLACE FUNCTION extract_xml_namespaces(xml_content XML)
RETURNS TABLE(prefix TEXT, uri TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (xpath('@prefix', ns))[1]::text as prefix,
        (xpath('@uri', ns))[1]::text as uri
    FROM (
        SELECT unnest(xpath('//namespace::*', xml_content)) as ns
    ) namespaces;
END;
$$ LANGUAGE plpgsql;

-- Function to validate SOAP envelope
CREATE OR REPLACE FUNCTION is_valid_soap_envelope(xml_content XML)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM xpath('//soap:Envelope/soap:Body', xml_content,
            ARRAY[ARRAY['soap', 'http://schemas.xmlsoap.org/soap/envelope/']])
    );
END;
$$ LANGUAGE plpgsql;
```

### 5. Java/JPA Updates

```java
// Add PostgreSQL dependency to pom.xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>

// Entity with PostgreSQL XML type
@Entity
@Table(name = "message_structures")
public class MessageStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(columnDefinition = "xml")
    @Convert(converter = XmlConverter.class)
    private String xsdContent;
}

// XML Converter for JPA
@Converter
public class XmlConverter implements AttributeConverter<String, PGobject> {
    @Override
    public PGobject convertToDatabaseColumn(String xml) {
        PGobject pgObject = new PGobject();
        pgObject.setType("xml");
        pgObject.setValue(xml);
        return pgObject;
    }
    
    @Override
    public String convertToEntityAttribute(PGobject dbData) {
        return dbData.getValue();
    }
}
```

### 6. XML Operations in PostgreSQL

```java
// Repository with native XML queries
@Repository
public interface FlowStructureRepository extends JpaRepository<FlowStructure, UUID> {
    
    @Query(value = """
        SELECT id, name, 
               xpath('//wsdl:operation/@name', wsdl_content, 
                     ARRAY[ARRAY['wsdl', 'http://schemas.xmlsoap.org/wsdl/']]) as operations
        FROM flow_structures
        WHERE id = :id
        """, nativeQuery = true)
    FlowStructureWithOperations findWithOperations(@Param("id") UUID id);
    
    @Query(value = """
        SELECT * FROM flow_structures
        WHERE xpath_exists('//wsdl:operation[@name=$1]', wsdl_content,
                          ARRAY[ARRAY['wsdl', 'http://schemas.xmlsoap.org/wsdl/']],
                          ARRAY[:operationName])
        """, nativeQuery = true)
    List<FlowStructure> findByOperationName(@Param("operationName") String operationName);
}
```

## Benefits of PostgreSQL for XML

1. **Native XML Validation**
   - Validates XML on insert/update
   - Can validate against XSD schemas

2. **XPath Queries in Database**
   - No need to load full XML into memory
   - Database can filter/search inside XML

3. **XML Indexes**
   - Can index specific XPath expressions
   - Much faster searches

4. **Better Integration**
   - XMLTABLE for XML to relational mapping
   - Can join XML data with relational data

5. **Namespace Support**
   - Proper namespace handling in XPath
   - No string manipulation needed

## Stay with MySQL Option

If you must stay with MySQL, you can still make it work but with limitations:
- Store XML as LONGTEXT
- Parse XML in application layer
- Use stored procedures for common operations
- Consider XML database for complex queries
- Cache parsed XML structures

But for a proper XML-native application, PostgreSQL is the better choice.