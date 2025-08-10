import { Field } from '@/types/dataStructures';

const buildJsonStructure = (fields: Field[]): any => {
  const result: any = {};
  
  fields.forEach(field => {
    if (field.name) {
      const isArray = field.type === 'array' || 
                  (typeof field.maxOccurs === 'number' && field.maxOccurs > 1) || 
                  field.maxOccurs === 'unbounded';
      const isComplexType = field.isComplexType || (field.children && field.children.length > 0);
      
      if (isComplexType) {
        if (isArray) {
          // For arrays with children, create array with sample object
          result[field.name] = [buildJsonStructure(field.children!)];
        } else {
          // For complex types, build nested structure (no type property)
          result[field.name] = buildJsonStructure(field.children!);
        }
      } else {
        // Simple primitive field - show default values
        result[field.name] = field.type === 'integer' ? 0 : "";
      }
    }
  });
  
  return result;
};

export const generateJsonSchema = (fields: Field[]): string => {
  if (fields.length === 0) return '{}';
  
  // Use the first field as the root container
  if (fields.length === 1 && (fields[0].isComplexType || fields[0].children)) {
    const rootField = fields[0];
    const structure = {
      [rootField.name]: buildJsonStructure(rootField.children || [])
    };
    return JSON.stringify(structure, null, 2);
  }
  
  // Fallback: wrap all fields in a root container
  const structure = {
    root: buildJsonStructure(fields)
  };
  
  return JSON.stringify(structure, null, 2);
};

export const generateXmlSchema = (fields: Field[]): string => {
  let xsd = `<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xs:element name="root">
    <xs:complexType>
      <xs:sequence>`;
  
  fields.forEach(field => {
    if (field.name) {
      if (field.isComplexType || (field.children && field.children.length > 0)) {
        xsd += `
        <xs:element name="${field.name}">
          <xs:complexType>
            <xs:sequence>`;
        
        field.children?.forEach(child => {
          if (child.name) {
            const childMinOccurs = child.minOccurs || (child.required ? 1 : 0);
            const childMaxOccurs = child.maxOccurs === 'unbounded' ? 'unbounded' : (child.maxOccurs || 1);
            const childOccursAttr = `minOccurs="${childMinOccurs}" maxOccurs="${childMaxOccurs}"`;
            
            if (child.isComplexType || (child.children && child.children.length > 0)) {
              xsd += `
              <xs:element name="${child.name}">
                <xs:complexType>
                  <xs:sequence>`;
              
              child.children?.forEach(grandchild => {
                if (grandchild.name) {
                  const grandchildMinOccurs = grandchild.minOccurs || (grandchild.required ? 1 : 0);
                  const grandchildMaxOccurs = grandchild.maxOccurs === 'unbounded' ? 'unbounded' : (grandchild.maxOccurs || 1);
                  const grandchildOccursAttr = `minOccurs="${grandchildMinOccurs}" maxOccurs="${grandchildMaxOccurs}"`;
                  
                  xsd += `
                    <xs:element name="${grandchild.name}" type="xs:${grandchild.type === 'array' ? 'string' : grandchild.type}" ${grandchildOccursAttr}/>`;
                }
              });
              
              xsd += `
                  </xs:sequence>
                </xs:complexType>
              </xs:element>`;
            } else {
              xsd += `
              <xs:element name="${child.name}" type="xs:${child.type === 'array' ? 'string' : child.type}" ${childOccursAttr}/>`;
            }
          }
        });
        
        xsd += `
            </xs:sequence>
          </xs:complexType>
        </xs:element>`;
      } else {
        const minOccurs = field.minOccurs || (field.required ? 1 : 0);
        const maxOccurs = field.maxOccurs === 'unbounded' ? 'unbounded' : (field.maxOccurs || 1);
        const occursAttr = `minOccurs="${minOccurs}" maxOccurs="${maxOccurs}"`;
        
        xsd += `
        <xs:element name="${field.name}" type="xs:${field.type === 'array' ? 'string' : field.type}" ${occursAttr}/>`;
      }
    }
  });
  
  xsd += `
      </xs:sequence>
    </xs:complexType>
  </xs:element>
</xs:schema>`;
  
  return xsd;
};

const generateComplexType = (field: Field, indent: string = '        '): string => {
  let xsd = '';
  
  if (field.isComplexType || (field.children && field.children.length > 0)) {
    xsd += `${indent}<xsd:complexType name="${field.name}Type">
${indent}  <xsd:sequence>`;
    
    field.children?.forEach(child => {
      if (child.name) {
        const minOccurs = child.minOccurs || (child.required ? 1 : 0);
        const maxOccurs = child.maxOccurs === 'unbounded' ? 'unbounded' : (child.maxOccurs || 1);
        const occursAttr = `minOccurs="${minOccurs}" maxOccurs="${maxOccurs}"`;
        
        if (child.isComplexType || (child.children && child.children.length > 0)) {
          xsd += `
${indent}    <xsd:element name="${child.name}" type="tns:${child.name}Type" ${occursAttr}/>`;
        } else {
          const xsdType = child.type === 'array' ? 'string' : child.type === 'integer' ? 'int' : child.type;
          xsd += `
${indent}    <xsd:element name="${child.name}" type="xsd:${xsdType}" ${occursAttr}/>`;
        }
      }
    });
    
    xsd += `
${indent}  </xsd:sequence>
${indent}</xsd:complexType>`;
  }
  
  return xsd;
};

export const generateWsdlSchema = (fields: Field[], serviceName: string = 'DataService', operationName: string = 'ProcessData'): string => {
  if (fields.length === 0) return '';
  
  let wsdl = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://schemas.xmlsoap.org/wsdl/"
             xmlns:tns="http://example.com/service"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
             xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
             targetNamespace="http://example.com/service"
             elementFormDefault="qualified">
  
  <!-- Types Section: XSD definitions for data types -->
  <types>
    <xsd:schema targetNamespace="http://example.com/service"
                xmlns:tns="http://example.com/service">
      
      <!-- Request Message Type -->
      <xsd:element name="${operationName}Request">
        <xsd:complexType>
          <xsd:sequence>`;

  // Add input fields
  fields.forEach(field => {
    if (field.name) {
      const minOccurs = field.minOccurs || (field.required ? 1 : 0);
      const maxOccurs = field.maxOccurs === 'unbounded' ? 'unbounded' : (field.maxOccurs || 1);
      const occursAttr = `minOccurs="${minOccurs}" maxOccurs="${maxOccurs}"`;
      
      if (field.isComplexType || (field.children && field.children.length > 0)) {
        wsdl += `
            <xsd:element name="${field.name}" type="tns:${field.name}Type" ${occursAttr}/>`;
      } else {
        const xsdType = field.type === 'array' ? 'string' : field.type === 'integer' ? 'int' : field.type;
        wsdl += `
            <xsd:element name="${field.name}" type="xsd:${xsdType}" ${occursAttr}/>`;
      }
    }
  });

  wsdl += `
          </xsd:sequence>
        </xsd:complexType>
      </xsd:element>
      
      <!-- Response Message Type -->
      <xsd:element name="${operationName}Response">
        <xsd:complexType>
          <xsd:sequence>
            <xsd:element name="result" type="xsd:string"/>
            <xsd:element name="status" type="xsd:string"/>
          </xsd:sequence>
        </xsd:complexType>
      </xsd:element>
      
      <!-- Fault Message Type -->
      <xsd:element name="${operationName}Fault">
        <xsd:complexType>
          <xsd:sequence>
            <xsd:element name="faultCode" type="xsd:string"/>
            <xsd:element name="faultString" type="xsd:string"/>
          </xsd:sequence>
        </xsd:complexType>
      </xsd:element>`;

  // Add complex type definitions
  fields.forEach(field => {
    if (field.isComplexType || (field.children && field.children.length > 0)) {
      wsdl += `
      
      ${generateComplexType(field)}`;
    }
  });

  wsdl += `
    </xsd:schema>
  </types>
  
  <!-- Messages Section: Define input and output messages -->
  <message name="${operationName}RequestMessage">
    <part name="parameters" element="tns:${operationName}Request"/>
  </message>
  
  <message name="${operationName}ResponseMessage">
    <part name="parameters" element="tns:${operationName}Response"/>
  </message>
  
  <message name="${operationName}FaultMessage">
    <part name="fault" element="tns:${operationName}Fault"/>
  </message>
  
  <!-- Port Type/Interface: Group related operations -->
  <portType name="${serviceName}PortType">
    <operation name="${operationName}">
      <documentation>Process data operation</documentation>
      <input message="tns:${operationName}RequestMessage"/>
      <output message="tns:${operationName}ResponseMessage"/>
      <fault name="${operationName}Fault" message="tns:${operationName}FaultMessage"/>
    </operation>
  </portType>
  
  <!-- Binding: Describe concrete protocol and data format (SOAP) -->
  <binding name="${serviceName}SOAPBinding" type="tns:${serviceName}PortType">
    <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
    <operation name="${operationName}">
      <soap:operation soapAction="http://example.com/service/${operationName}"/>
      <input>
        <soap:body use="literal"/>
      </input>
      <output>
        <soap:body use="literal"/>
      </output>
      <fault name="${operationName}Fault">
        <soap:fault name="${operationName}Fault" use="literal"/>
      </fault>
    </operation>
  </binding>
  
  <!-- Service: Network address (endpoint) - to be added by Java application -->
  <!-- 
  <service name="${serviceName}">
    <port name="${serviceName}SOAPPort" binding="tns:${serviceName}SOAPBinding">
      <soap:address location="[ENDPOINT_URL_TO_BE_SET_BY_JAVA_APP]"/>
    </port>
  </service>
  -->
  
</definitions>`;
  
  return wsdl;
};

export const generateSchemaPreview = (fields: Field[], schemaType: string): string => {
  if (fields.length === 0) return '';
  
  switch (schemaType) {
    case 'json':
      return generateJsonSchema(fields);
    case 'xml':
    case 'xsd':
      return generateXmlSchema(fields);
    case 'wsdl':
      return generateWsdlSchema(fields);
    default:
      return JSON.stringify(fields, null, 2);
  }
};