// @ts-nocheck
import { api } from '@/services/api';
import { FieldNode } from '@/components/fieldMapping/types';

export interface XmlConversionConfig {
  rootElementName?: string;
  includeXmlDeclaration?: boolean;
  prettyPrint?: boolean;
  encoding?: string;
  convertPropertyNames?: boolean;
  preserveNullValues?: boolean;
  namespaceUri?: string;
  namespacePrefix?: string;
  additionalNamespaces?: Record<string, string>;
  arrayElementNames?: Record<string, string>;
}

export interface XmlConversionResult {
  structureId: string;
  structureName: string;
  xmlContent: string;
  config: XmlConversionConfig;
}

/**
 * Convert a data structure to XML format
 */
export async function convertStructureToXml(
  structureId: string,
  config?: XmlConversionConfig
): Promise<XmlConversionResult> {
  try {
    const response = await api.post<XmlConversionResult>(
      `/structures/${structureId}/convert-to-xml`,
      config || {}
    );
    
    // Log the XML for debugging
    console.log('Received XML conversion result:', {
      structureId: response.data.structureId,
      structureName: response.data.structureName,
      xmlContent: response.data.xmlContent
    });
    
    return response.data;
  } catch (error: any) {
    console.error('XML conversion error for structure:', structureId, error.response?.data);
    throw error;
  }
}

/**
 * Parse XML content and convert to FieldNode tree structure
 */
export function parseXmlToFieldNodes(xmlContent: string): FieldNode[] {
  const parser = new DOMParser();
  const xmlDoc = parser.parseFromString(xmlContent, 'text/xml');
  
  // Debug logging to check XML structure
  console.log('Parsing XML content:', xmlContent);
  
  // Check for parsing errors
  const parserError = xmlDoc.querySelector('parsererror');
  if (parserError) {
    // Log the problematic XML content for debugging
    const lines = xmlContent.split('\n');
    console.error('XML parsing error details:', {
      errorText: parserError.textContent,
      lineCount: lines.length,
      problematicLines: lines.map((line, idx) => ({
        lineNumber: idx + 1,
        content: line,
        length: line.length
      })).filter(l => l.lineNumber >= 15 && l.lineNumber <= 25)
    });
    
    // Also log the full line 17 and 18 without truncation
    console.error('Full line 17:', lines[16]);
    console.error('Full line 18:', lines[17]);
    
    // Log specific character codes around column 14 of line 18
    if (lines[17]) {
      console.error('Line 18 character analysis around column 14:');
      for (let i = 10; i < 20 && i < lines[17].length; i++) {
        console.error(`  Position ${i}: '${lines[17][i]}' (char code: ${lines[17].charCodeAt(i)})`);
      }
    }
    
    throw new Error('Invalid XML: ' + parserError.textContent);
  }
  
  const rootElement = xmlDoc.documentElement;
  
  // Create a field node for the root element itself
  const rootFieldNode: FieldNode = {
    id: `${rootElement.tagName}_root`,
    name: rootElement.tagName,
    type: 'object',
    path: rootElement.tagName,
    expanded: true,
    children: convertXmlNodeToFieldNodes(rootElement, rootElement.tagName)
  };
  
  return [rootFieldNode];
}

/**
 * Recursively convert XML nodes to FieldNode structure
 */
function convertXmlNodeToFieldNodes(xmlNode: Element, parentPath: string = ''): FieldNode[] {
  const fieldNodes: FieldNode[] = [];
  
  // Process attributes
  if (xmlNode.attributes.length > 0) {
    for (let i = 0; i < xmlNode.attributes.length; i++) {
      const attr = xmlNode.attributes[i];
      if (!attr.name.startsWith('xmlns')) { // Skip namespace declarations
        const path = parentPath ? `${parentPath}.@${attr.name}` : `@${attr.name}`;
        fieldNodes.push({
          id: `${path}_attr_${i}`,
          name: `@${attr.name}`,
          type: 'attribute',
          path,
          expanded: false
        });
      }
    }
  }
  
  // Process child elements while preserving order
  const processedTags = new Set<string>();
  const elementGroups = new Map<string, Element[]>();
  
  // First, collect all elements by tag name
  for (let i = 0; i < xmlNode.children.length; i++) {
    const child = xmlNode.children[i] as Element;
    const tagName = child.tagName;
    if (!elementGroups.has(tagName)) {
      elementGroups.set(tagName, []);
    }
    elementGroups.get(tagName)!.push(child);
  }
  
  // Then process them in document order
  console.log('Processing children in order for node:', xmlNode.tagName);
  for (let i = 0; i < xmlNode.children.length; i++) {
    const child = xmlNode.children[i] as Element;
    const tagName = child.tagName;
    console.log(`  Child ${i}: ${tagName}`);
    
    // Skip if we've already processed this tag
    if (processedTags.has(tagName)) {
      continue;
    }
    processedTags.add(tagName);
    
    const elements = elementGroups.get(tagName)!;
    
    if (elements.length === 1) {
      // Single element
      const element = elements[0];
      const path = parentPath ? `${parentPath}.${tagName}` : tagName;
      const hasChildren = element.children.length > 0 || element.attributes.length > 0;
      const hasTextContent = element.textContent && element.children.length === 0;
      
      const fieldNode: FieldNode = {
        id: `${path}_${Date.now()}_${Math.random()}`,
        name: tagName,
        type: hasChildren ? 'object' : 'string',
        path,
        expanded: false,
        children: hasChildren ? convertXmlNodeToFieldNodes(element, path) : undefined
      };
      
      fieldNodes.push(fieldNode);
    } else {
      // Array of elements
      const path = parentPath ? `${parentPath}.${tagName}[]` : `${tagName}[]`;
      const fieldNode: FieldNode = {
        id: `${path}_array_${Date.now()}`,
        name: `${tagName}[]`,
        type: 'array',
        path,
        expanded: false,
        children: elements[0].children.length > 0 || elements[0].attributes.length > 0
          ? convertXmlNodeToFieldNodes(elements[0], `${path}[0]`)
          : undefined
      };
      
      fieldNodes.push(fieldNode);
    }
  }
  
  // If node has text content and no children, add it as a value
  if (xmlNode.textContent && xmlNode.children.length === 0 && fieldNodes.length === 0) {
    const path = parentPath ? `${parentPath}._text` : '_text';
    fieldNodes.push({
      id: `${path}_text`,
      name: '_text',
      type: 'string',
      path,
      expanded: false
    });
  }
  
  return fieldNodes;
}

/**
 * Extract XML paths from a FieldNode tree for mapping
 */
export function extractXmlPaths(nodes: FieldNode[], paths: string[] = []): string[] {
  nodes.forEach(node => {
    paths.push(node.path);
    if (node.children) {
      extractXmlPaths(node.children, paths);
    }
  });
  return paths;
}