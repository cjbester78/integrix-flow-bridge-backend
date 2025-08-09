# File Adapter Operations Guide

## Overview

This document provides comprehensive guidance for file-based adapter operations in Integrix Flow Bridge, including file-to-file transfers, format conversions, special character handling, and performance optimization strategies.

## Table of Contents

1. [File Adapter Types](#file-adapter-types)
2. [File-to-File Scenarios](#file-to-file-scenarios)
3. [Format Conversions](#format-conversions)
4. [Special Character Handling](#special-character-handling)
5. [Configuration Options](#configuration-options)
6. [Performance Considerations](#performance-considerations)
7. [Troubleshooting](#troubleshooting)

## File Adapter Types

### 1. FILE Adapter
- **Purpose**: Local file system operations
- **Sender**: Polls directories for files
- **Receiver**: Writes files to directories
- **Protocols**: Local file system access

### 2. FTP Adapter
- **Purpose**: FTP server file transfers
- **Sender**: Polls FTP directories
- **Receiver**: Uploads files to FTP
- **Protocols**: FTP, FTPS (with TLS)

### 3. SFTP Adapter
- **Purpose**: Secure file transfers
- **Sender**: Polls SFTP directories
- **Receiver**: Uploads files via SFTP
- **Protocols**: SSH File Transfer Protocol

## File-to-File Scenarios

### 1. Direct Passthrough (No Transformation)

**Scenario**: Copy files without any modification

```
Source File → FILE Sender → [No XML Conversion] → FILE Receiver → Target File
```

**Configuration**:
- Flow Type: Direct Mapping
- Transformation: None (Passthrough mode)
- Benefits: Maximum performance, preserves original format

**Use Cases**:
- Binary file transfers (PDFs, images, documents)
- Large file movements
- Format-preserving migrations

### 2. File Transfer with Transformation

**Scenario**: Transfer files with content transformation

```
Source File → FILE Sender → XML Conversion → Transformation → XML to Target Format → FILE Receiver → Target File
```

**Configuration**:
- Flow Type: Direct Mapping
- Transformation: Field mappings or XSLT
- Process: Convert to XML → Apply transformations → Convert to target format

**Use Cases**:
- Data enrichment
- Field mapping/renaming
- Structure modifications

### 3. Format Conversion

**Scenario**: Convert between different file formats

```
CSV File → FILE Sender → CSV-to-XML → XML-to-JSON → FILE Receiver → JSON File
```

**Supported Conversions**:
- CSV ↔ XML ↔ JSON
- Fixed-Length ↔ XML ↔ JSON
- Plain Text → XML → Any Format
- XML ↔ SQL Insert Statements

## Format Conversions

### CSV to XML Conversion

**Input CSV**:
```csv
orderId,customerName,amount,date
12345,John Doe,99.99,2024-01-15
12346,Jane Smith,149.99,2024-01-16
```

**Output XML**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Message>
  <Record>
    <orderId>12345</orderId>
    <customerName>John Doe</customerName>
    <amount>99.99</amount>
    <date>2024-01-15</date>
  </Record>
  <Record>
    <orderId>12346</orderId>
    <customerName>Jane Smith</customerName>
    <amount>149.99</amount>
    <date>2024-01-16</date>
  </Record>
</Message>
```

**Configuration Options**:
- Delimiter: `,` (configurable)
- Line Terminator: `\n` (LF), `\r\n` (CRLF), `\r` (CR)
- Quote Character: `"` (for values with delimiters)
- Include Headers: true/false
- Skip Empty Lines: true/false

### Fixed-Length to XML Conversion

**Input Fixed-Length**:
```
12345John Doe      0009999
12346Jane Smith    0014999
```

**Field Configuration**:
- orderId: Length 5, Position 1-5
- customerName: Length 15, Position 6-20
- amount: Length 7, Position 21-27

**Output XML**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Message>
  <Record>
    <orderId>12345</orderId>
    <customerName>John Doe</customerName>
    <amount>0009999</amount>
  </Record>
  <Record>
    <orderId>12346</orderId>
    <customerName>Jane Smith</customerName>
    <amount>0014999</amount>
  </Record>
</Message>
```

**Configuration Options**:
- Field Lengths: Map of field names to lengths
- Field Order: Sequence of fields
- Pad Character: Space (default) or custom
- Pad Direction: LEFT or RIGHT
- Line Terminator: Configurable

### JSON to XML Conversion

**Input JSON**:
```json
{
  "orders": [
    {
      "orderId": "12345",
      "customer": {
        "name": "John Doe",
        "email": "john@example.com"
      },
      "amount": 99.99
    }
  ]
}
```

**Output XML**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Message>
  <orders>
    <orderId>12345</orderId>
    <customer>
      <name>John Doe</name>
      <email>john@example.com</email>
    </customer>
    <amount>99.99</amount>
  </orders>
</Message>
```

## Special Character Handling

### Problem: XML Conversion Breaking with Special Characters

Special characters can cause XML parsing errors when not properly handled:

1. **Control Characters** (0x00-0x1F)
2. **XML Reserved Characters** (&, <, >, ", ')
3. **Unicode Characters** (emoji, non-Latin scripts)
4. **Invalid XML 1.0 Characters**

### Current Limitation

The system currently uses basic `setTextContent()` which only handles standard XML entity escaping:
- `&` → `&amp;`
- `<` → `&lt;`
- `>` → `&gt;`
- `"` → `&quot;`
- `'` → `&apos;`

**This is insufficient for**:
- Control characters (tabs, newlines, etc.)
- Binary data embedded in text
- Invalid Unicode sequences

### Recommended Solutions

#### 1. Direct Passthrough Mode (Preferred for Problematic Files)
```
Bypass XML conversion entirely for file-to-file transfers
```

**Benefits**:
- No character encoding issues
- 10x performance improvement
- Preserves original file integrity

#### 2. CDATA Sections (For XML Conversion)
```xml
<field><![CDATA[Content with <special> & characters]]></field>
```

**When to Use**:
- Fields containing XML-like content
- Fields with many special characters
- Preserving exact formatting

#### 3. Character Sanitization
- Remove control characters (0x00-0x08, 0x0B-0x0C, 0x0E-0x1F)
- Replace with placeholders or Unicode escape sequences
- Log sanitization actions for debugging

#### 4. XML 1.1 Support
- Allows more control characters
- Better Unicode support
- Requires explicit version declaration

## Configuration Options

### File Format Configuration

```json
{
  "fileFormatConfig": {
    "fileFormat": "CSV",
    "delimiter": ",",
    "lineTerminator": "\n",
    "quoteCharacter": "\"",
    "includeHeaders": true,
    "encoding": "UTF-8"
  }
}
```

### XML Conversion Configuration

```json
{
  "xmlConversion": {
    "rootElementName": "Message",
    "encoding": "UTF-8",
    "includeXmlDeclaration": true,
    "prettyPrint": true,
    "namespaceUri": "http://example.com/integration",
    "namespacePrefix": "int",
    "handleSpecialCharacters": "CDATA",
    "sanitizeControlCharacters": true
  }
}
```

### Line Terminator Options

| Platform | Line Terminator | Escape Sequence | Hex |
|----------|----------------|-----------------|-----|
| Unix/Linux/Mac | LF (Line Feed) | `\n` | 0x0A |
| Windows | CRLF (Carriage Return + Line Feed) | `\r\n` | 0x0D 0x0A |
| Classic Mac | CR (Carriage Return) | `\r` | 0x0D |

### Character Encoding Options

| Encoding | Description | Use Case |
|----------|-------------|----------|
| UTF-8 | Universal, variable-length | Default, supports all languages |
| UTF-16 | Universal, fixed-length | Legacy Windows systems |
| ISO-8859-1 | Latin-1 | Western European languages |
| ASCII | 7-bit US | Legacy systems, English only |

## Performance Considerations

### 1. Direct Passthrough vs XML Conversion

| Operation | Throughput | Latency | Memory Usage |
|-----------|------------|---------|--------------|
| Direct Passthrough | 1000 MB/s | < 1ms | Minimal (streaming) |
| XML Conversion | 100 MB/s | 10-50ms | High (DOM parsing) |

### 2. Optimization Strategies

#### For Large Files (> 100MB)
1. **Use Direct Passthrough** when transformation not required
2. **Enable Streaming Mode** for XML processing
3. **Implement Chunked Processing** for transformations
4. **Use NIO Channels** for file operations

#### For High-Volume Processing
1. **Parallel Processing**: Multiple adapter instances
2. **Batch Operations**: Group small files
3. **Async Processing**: Non-blocking I/O
4. **Connection Pooling**: For FTP/SFTP

### 3. Memory Management

```java
// Recommended settings for large file processing
-Xmx4G           # Maximum heap size
-XX:+UseG1GC     # G1 garbage collector
-XX:MaxGCPauseMillis=200  # Target pause time
```

## Troubleshooting

### Common Issues and Solutions

#### 1. "XML Parsing Error: Invalid Character"
**Cause**: Control characters or invalid Unicode in data
**Solution**: 
- Enable passthrough mode
- Implement CDATA wrapping
- Use character sanitization

#### 2. "Out of Memory Error"
**Cause**: Large file loaded into memory for XML conversion
**Solution**:
- Use streaming mode
- Increase heap size
- Implement chunked processing

#### 3. "File Not Found After Processing"
**Cause**: File moved/deleted by processing mode
**Solution**:
- Check processing mode (Archive/Delete/Test)
- Verify archive directory permissions
- Enable duplicate handling

#### 4. "Character Encoding Mismatch"
**Cause**: Source encoding differs from configured encoding
**Solution**:
- Auto-detect source encoding
- Configure correct encoding
- Implement encoding conversion

### Debug Logging

Enable detailed logging for troubleshooting:

```yaml
logging:
  level:
    com.integrationlab.adapters.impl.File: DEBUG
    com.integrationlab.engine.xml: TRACE
```

## Best Practices

1. **Always Test with Sample Data**
   - Include special characters in test files
   - Test with various encodings
   - Verify line terminator handling

2. **Choose Appropriate Processing Mode**
   - Passthrough for simple transfers
   - XML conversion only when needed
   - Consider performance requirements

3. **Handle Errors Gracefully**
   - Implement retry mechanisms
   - Log failed conversions
   - Provide fallback options

4. **Monitor Performance**
   - Track processing times
   - Monitor memory usage
   - Set up alerts for failures

5. **Document Configuration**
   - Record encoding choices
   - Document special character handling
   - Maintain transformation mappings

## Future Enhancements

1. **Direct Passthrough Mode Implementation**
   - Bypass XML conversion for better performance
   - Streaming file transfer support
   - Binary file handling

2. **Advanced Character Handling**
   - Automatic CDATA detection
   - Configurable sanitization rules
   - XML 1.1 support option

3. **Enhanced UI**
   - Slider-based format selection
   - Real-time preview
   - Drag-and-drop field configuration

4. **Performance Optimizations**
   - Parallel file processing
   - Adaptive batch sizing
   - Intelligent caching

---

*Last Updated: 2025-08-09*