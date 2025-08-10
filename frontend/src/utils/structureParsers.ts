// @ts-nocheck
import { Field } from '@/types/dataStructures';

// Custom JSON stringify that preserves property order
export const orderedStringify = (obj: any, space?: number): string => {
  const replacer = (key: string, value: any): any => {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      // For objects, we'll rely on the fact that modern JS preserves insertion order
      return value;
    }
    return value;
  };
  
  return JSON.stringify(obj, replacer, space);
};

export const parseJsonStructure = (jsonString: string) => {
  try {
    const obj = JSON.parse(jsonString);
    return analyzeJsonStructure(obj);
  } catch {
    return null;
  }
};

export const analyzeJsonStructure = (obj: any): any => {
  if (Array.isArray(obj)) {
    return obj.length > 0 ? ['array', analyzeJsonStructure(obj[0])] : 'array';
  } else if (obj !== null && typeof obj === 'object') {
    const structure: any = {};
    for (const [key, value] of Object.entries(obj)) {
      structure[key] = analyzeJsonStructure(value);
    }
    return structure;
  } else {
    return typeof obj;
  }
};

export const parseWsdlStructure = (wsdlString: string) => {
  try {
    // Basic WSDL parsing - extract complex types and elements
    const parser = new DOMParser();
    const doc = parser.parseFromString(wsdlString, 'text/xml');
    
    // Check for parsing errors
    const parserError = doc.querySelector('parsererror');
    if (parserError) {
      return null;
    }
    
    // Use array to collect entries in order, then build object
    const structureEntries: [string, any][] = [];
    const complexTypes: Map<string, any> = new Map();
    
    // Helper function to resolve type references
    const resolveType = (typeName: string, schemas: NodeListOf<Element>): any => {
      // Remove namespace prefix
      const cleanTypeName = typeName.split(':').pop() || '';
      
      // Check if we already processed this type
      if (complexTypes.has(cleanTypeName)) {
        return complexTypes.get(cleanTypeName);
      }
      
      // Look for the type definition
      for (const schema of schemas) {
        const typeElement = schema.querySelector(`complexType[name="${cleanTypeName}"], xs\\:complexType[name="${cleanTypeName}"], xsd\\:complexType[name="${cleanTypeName}"]`);
        if (typeElement) {
          const typeStructure: any = {};
          extractElements(typeElement, typeStructure, 0, schemas);
          complexTypes.set(cleanTypeName, typeStructure);
          return typeStructure;
        }
        
        // Check simple types
        const simpleType = schema.querySelector(`simpleType[name="${cleanTypeName}"], xs\\:simpleType[name="${cleanTypeName}"], xsd\\:simpleType[name="${cleanTypeName}"]`);
        if (simpleType) {
          const restriction = simpleType.querySelector('restriction, xs\\:restriction, xsd\\:restriction');
          if (restriction) {
            const base = restriction.getAttribute('base') || 'string';
            return base.replace(/^(xs:|xsd:|tns:)/, '');
          }
        }
      }
      
      // Default to the type name if not found
      return cleanTypeName.replace(/^(xs:|xsd:|tns:)/, '');
    };
    
    // Helper function to create ordered object from entries
    const createOrderedObject = (entries: [string, any][]): any => {
      const obj: any = {};
      // By iterating through entries in order and setting properties,
      // we ensure that modern JavaScript engines preserve insertion order
      for (const [key, value] of entries) {
        obj[key] = value;
      }
      return obj;
    };

    // Helper function to extract elements recursively
    const extractElements = (parent: Element, targetStructure: any, depth: number = 0, schemas: NodeListOf<Element>) => {
      const elements = parent.querySelectorAll(':scope > sequence > element, :scope > sequence > xs\\:element, :scope > sequence > xsd\\:element, :scope > element, :scope > xs\\:element, :scope > xsd\\:element');
      
      // Collect entries in order to preserve field sequence
      const entries: [string, any][] = [];
      
      elements.forEach((element) => {
        const elementName = element.getAttribute('name');
        const elementType = element.getAttribute('type');
        const elementRef = element.getAttribute('ref');
        const maxOccurs = element.getAttribute('maxOccurs');
        const isArray = maxOccurs === 'unbounded' || (maxOccurs && parseInt(maxOccurs) > 1);
        
        if (elementName || elementRef) {
          const name = elementName || elementRef?.split(':').pop() || '';
          const fieldName = isArray ? `${name}[]` : name;
          let fieldValue: any;
          
          // Check if this element has inline complex content
          const complexType = element.querySelector('complexType, xs\\:complexType, xsd\\:complexType');
          if (complexType) {
            // Nested complex type - create ordered object
            const nestedObj = {};
            extractElements(complexType, nestedObj, depth + 1, schemas);
            fieldValue = nestedObj;
          } else if (elementType) {
            // Type reference - resolve it
            fieldValue = resolveType(elementType, schemas);
          } else if (elementRef) {
            // Element reference - find the element definition
            const refName = elementRef.split(':').pop() || '';
            for (const schema of schemas) {
              const refElement = schema.querySelector(`element[name="${refName}"], xs\\:element[name="${refName}"], xsd\\:element[name="${refName}"]`);
              if (refElement) {
                const refType = refElement.getAttribute('type');
                if (refType) {
                  fieldValue = resolveType(refType, schemas);
                }
                break;
              }
            }
            if (!fieldValue) {
              fieldValue = 'string';
            }
          } else {
            // Default to string if no type specified
            fieldValue = 'string';
          }
          
          entries.push([fieldName, fieldValue]);
        }
      });
      
      // Apply entries to target structure in order
      for (const [key, value] of entries) {
        targetStructure[key] = value;
      }
    };
    
    // Get all schemas
    const schemas = doc.querySelectorAll('schema, xs\\:schema, xsd\\:schema');
    
    // Try to find message definitions and their corresponding elements
    const messages = doc.querySelectorAll('message, wsdl\\:message');
    messages.forEach((message) => {
      const parts = message.querySelectorAll('part, wsdl\\:part');
      
      parts.forEach((part) => {
        const partElement = part.getAttribute('element');
        
        if (partElement) {
          // Find the element definition
          const elementName = partElement.split(':').pop();
          for (const schema of schemas) {
            const element = schema.querySelector(`element[name="${elementName}"], xs\\:element[name="${elementName}"], xsd\\:element[name="${elementName}"]`);
            if (element) {
              const elementType = element.getAttribute('type');
              if (elementType) {
                // Resolve the type
                const resolvedType = resolveType(elementType, schemas);
                if (typeof resolvedType === 'object') {
                  structureEntries.push([elementName!, resolvedType]);
                } else {
                  structureEntries.push([elementName!, { value: resolvedType }]);
                }
              } else {
                // Check for inline complex type
                const complexType = element.querySelector('complexType, xs\\:complexType, xsd\\:complexType');
                if (complexType) {
                  const nestedStructure = {};
                  extractElements(complexType, nestedStructure, 0, schemas);
                  structureEntries.push([elementName!, nestedStructure]);
                }
              }
              break;
            }
          }
        }
      });
    });
    
    // If no message elements found, try root elements in schemas
    if (structureEntries.length === 0) {
      schemas.forEach((schema) => {
        const rootElements = schema.querySelectorAll(':scope > element, :scope > xs\\:element, :scope > xsd\\:element');
        rootElements.forEach((rootElement) => {
          const rootName = rootElement.getAttribute('name');
          if (rootName) {
            const rootType = rootElement.getAttribute('type');
            if (rootType) {
              const resolvedType = resolveType(rootType, schemas);
              if (typeof resolvedType === 'object') {
                structureEntries.push([rootName, resolvedType]);
              } else {
                structureEntries.push([rootName, { value: resolvedType }]);
              }
            } else {
              const complexType = rootElement.querySelector('complexType, xs\\:complexType, xsd\\:complexType');
              if (complexType) {
                const nestedStructure = {};
                extractElements(complexType, nestedStructure, 0, schemas);
                structureEntries.push([rootName, nestedStructure]);
              }
            }
          }
        });
      });
    }
    
    // Build the structure object from entries to preserve order
    const structure = createOrderedObject(structureEntries);
    
    // Detect operation pattern for sync/async determination
    const operations = doc.querySelectorAll('operation, wsdl\\:operation');
    let operationInfo = null;
    
    if (operations.length > 0) {
      const operation = operations[0];
      const hasInput = !!operation.querySelector('input, wsdl\\:input');
      const hasOutput = !!operation.querySelector('output, wsdl\\:output');
      const hasFault = !!operation.querySelector('fault, wsdl\\:fault');
      
      operationInfo = {
        hasInput,
        hasOutput,
        hasFault,
        isSynchronous: hasInput && hasOutput, // Sync if has both input and output
        messageTypes: []
      };
      
      // Collect message types
      if (hasInput) operationInfo.messageTypes.push('input');
      if (hasOutput) operationInfo.messageTypes.push('output');
      if (hasFault) operationInfo.messageTypes.push('fault');
    }
    
    return Object.keys(structure).length > 0 ? { structure, operationInfo } : null;
  } catch (error) {
    console.error('Error parsing WSDL:', error);
    return null;
  }
};

export const extractWsdlOperations = (wsdlString: string): { names: string[], hasMultiple: boolean } => {
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(wsdlString, 'text/xml');
    
    // Check for parsing errors
    const parserError = doc.querySelector('parsererror');
    if (parserError) {
      console.error('WSDL parsing error:', parserError);
      return { names: [], hasMultiple: false };
    }
    
    // Debug: Check what we're parsing
    const rootElement = doc.documentElement;
    console.log('WSDL root element:', rootElement.tagName, 'namespace:', rootElement.namespaceURI);
    
    // Find all portType elements first - use namespace-agnostic approach
    const portTypes: Element[] = [];
    const allElements = doc.getElementsByTagName('*');
    for (let i = 0; i < allElements.length; i++) {
      const elem = allElements[i];
      if (elem.localName === 'portType' || elem.tagName === 'portType' || elem.tagName === 'wsdl:portType') {
        portTypes.push(elem);
      }
    }
    console.log('Found portTypes:', portTypes.length);
    
    // Find all operation elements within portTypes
    const operations: Element[] = [];
    portTypes.forEach(portType => {
      const portTypeOps = portType.getElementsByTagName('*');
      for (let i = 0; i < portTypeOps.length; i++) {
        const elem = portTypeOps[i];
        if ((elem.localName === 'operation' || elem.tagName === 'operation' || elem.tagName === 'wsdl:operation') &&
            elem.parentElement === portType) {
          operations.push(elem);
        }
      }
    });
    const operationNames: string[] = [];
    
    console.log('Found operations in portType:', operations.length);
    operations.forEach((operation) => {
      const name = operation.getAttribute('name');
      console.log('Operation element:', operation.tagName, 'name attribute:', name);
      if (name && !operationNames.includes(name)) {
        operationNames.push(name);
        console.log('Added operation:', name);
      }
    });
    
    // If no operations found in portType, look in binding
    if (operationNames.length === 0) {
      console.log('No operations in portType, checking binding elements...');
      
      // Find all binding elements
      const bindings: Element[] = [];
      for (let i = 0; i < allElements.length; i++) {
        const elem = allElements[i];
        if (elem.localName === 'binding' || elem.tagName === 'binding' || elem.tagName === 'wsdl:binding') {
          bindings.push(elem);
        }
      }
      console.log('Found bindings:', bindings.length);
      
      // Find operations within bindings
      bindings.forEach(binding => {
        const bindingOps = binding.getElementsByTagName('*');
        for (let i = 0; i < bindingOps.length; i++) {
          const elem = bindingOps[i];
          if ((elem.localName === 'operation' || elem.tagName === 'operation' || elem.tagName === 'wsdl:operation') &&
              elem.parentElement === binding) {
            const name = elem.getAttribute('name');
            if (name && !operationNames.includes(name)) {
              operationNames.push(name);
              console.log('Found operation in binding:', name);
            }
          }
        }
      });
    }
    
    return {
      names: operationNames,
      hasMultiple: operationNames.length > 1
    };
  } catch (error) {
    console.error('Error extracting WSDL operations:', error);
    return { names: [], hasMultiple: false };
  }
};

export const extractWsdlPartName = (wsdlString: string): string | null => {
  try {
    // Always try to get operation names first - this is the primary source
    const { names, hasMultiple } = extractWsdlOperations(wsdlString);
    console.log('WSDL operation extraction result:', { names, hasMultiple });
    
    if (names.length === 1) {
      // Single operation found - use it
      return names[0];
    } else if (names.length > 1) {
      // Multiple operations - don't auto-name, user needs to choose
      return null;
    }
    
    // If no operations found, don't fall back to element names
    // The operation name is what we want for WSDL naming
    return null;
  } catch (error) {
    console.error('Error extracting WSDL part name:', error);
    return null;
  }
};

export const extractWsdlNamespaceInfo = (wsdlString: string) => {
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(wsdlString, 'text/xml');
    
    // Check for parsing errors
    const parserError = doc.querySelector('parsererror');
    if (parserError) {
      return null;
    }
    
    const root = doc.documentElement;
    const targetNamespace = root.getAttribute('targetNamespace') || '';
    
    // Extract WSDL location from soap:address location
    const soapAddresses = doc.querySelectorAll('address, soap\\:address, soap12\\:address');
    let schemaLocation = '';
    if (soapAddresses.length > 0) {
      schemaLocation = soapAddresses[0].getAttribute('location') || '';
    }
    
    // Extract namespace prefix from xmlns attributes
    let prefix = '';
    for (let i = 0; i < root.attributes.length; i++) {
      const attr = root.attributes[i];
      if (attr.name.startsWith('xmlns:') && attr.value === targetNamespace) {
        prefix = attr.name.replace('xmlns:', '');
        break;
      }
    }
    
    return {
      uri: targetNamespace,
      prefix,
      targetNamespace,
      schemaLocation
    };
  } catch (error) {
    return null;
  }
};

export const buildNestedStructure = (fields: Field[]): any => {
  const structure: any = {};
  
  fields.forEach(field => {
    if (field.children && field.children.length > 0) {
      // Complex type with children
      structure[field.name] = buildNestedStructure(field.children);
    } else {
      // Simple field
      structure[field.name] = field.type;
    }
  });
  
  return structure;
};