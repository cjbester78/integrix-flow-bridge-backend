import { useState, useCallback, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';


import { Trash2, X, CheckCircle, Zap, Play } from 'lucide-react';
import { FieldNode, FieldMapping } from './fieldMapping/types';
import { useDataStructures } from '@/hooks/useDataStructures';
import { SourcePanel } from './fieldMapping/SourcePanel';
import { TargetPanel } from './fieldMapping/TargetPanel';
import { MappingArea } from './fieldMapping/MappingArea';
import { TestMappingDialog } from './fieldMapping/TestMappingDialog';
import { convertStructureToXml, parseXmlToFieldNodes } from '@/utils/xmlStructureConverter';
import { useToast } from '@/hooks/use-toast';

import { DataStructure } from '@/types/dataStructures';

interface MappingScreenProps {
  onClose?: () => void;
  onSave?: (mappings: FieldMapping[], mappingName?: string) => void;
  initialMappingName?: string;
  initialMappings?: FieldMapping[];
  sampleStructures?: DataStructure[];
  sourceAdapterType?: string;
  targetAdapterType?: string;
  sourceXml?: string;
  targetXml?: string;
  preConverted?: boolean;
  mappingType?: 'request' | 'response' | 'fault';
}

export function FieldMappingScreen({ 
  onClose, 
  onSave, 
  initialMappingName = '',
  initialMappings = [],
  sampleStructures: _sampleStructures = [],
  sourceAdapterType = '',
  targetAdapterType = '',
  sourceXml = '',
  targetXml = '',
  preConverted = false,
  mappingType = 'request'
}: MappingScreenProps) {
  const [sourceFields, setSourceFields] = useState<FieldNode[]>([]);
  const [targetFields, setTargetFields] = useState<FieldNode[]>([]);
  const [mappings, setMappings] = useState<FieldMapping[]>(initialMappings);
  const [draggedField, setDraggedField] = useState<FieldNode | null>(null);
  const [searchSource, setSearchSource] = useState('');
  const [searchTarget, setSearchTarget] = useState('');
  const [selectedSource, setSelectedSource] = useState<string>('');
  const [selectedTarget, setSelectedTarget] = useState<string>('');
  const [showSourceSelector, setShowSourceSelector] = useState(false);
  const [showTargetSelector, setShowTargetSelector] = useState(false);
  const [mappingName, setMappingName] = useState(initialMappingName);
  const [loadingSource, setLoadingSource] = useState(false);
  const [loadingTarget, setLoadingTarget] = useState(false);
  const [selectedSourceField, setSelectedSourceField] = useState<FieldNode | null>(null);
  const [selectedTargetField, setSelectedTargetField] = useState<FieldNode | null>(null);
  const [showTestDialog, setShowTestDialog] = useState(false);
  // Remove viewMode - always use traditional view
  
  const { structures } = useDataStructures();
  const { toast } = useToast();

  // Helper function to filter XML based on mapping type
  const filterXmlByMessageType = (xml: string, messageType: 'request' | 'response' | 'fault'): string => {
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(xml, 'text/xml');
      
      // Find the root element
      const root = doc.documentElement;
      
      // Create a new document with the same root
      const newDoc = document.implementation.createDocument(null, root.tagName, null);
      const newRoot = newDoc.documentElement;
      
      // Copy attributes from original root
      for (let i = 0; i < root.attributes.length; i++) {
        const attr = root.attributes[i];
        newRoot.setAttribute(attr.name, attr.value);
      }
      
      // Identify patterns for different message types
      const messagePatterns = {
        'request': ['_Req_', 'Request', 'Input', 'input'],
        'response': ['_Resp_', 'Response', 'Output', 'output'],
        'fault': ['Fault', 'fault', 'Error', 'error', 'Exception']
      };
      
      const patterns = messagePatterns[messageType];
      
      // Filter child elements based on patterns
      for (let i = 0; i < root.children.length; i++) {
        const child = root.children[i];
        const tagName = child.tagName;
        
        // Check if the tag name matches any of the patterns for the message type
        const shouldInclude = patterns.some(pattern => 
          tagName.includes(pattern) || 
          tagName.toLowerCase().includes(pattern.toLowerCase())
        );
        
        if (shouldInclude) {
          const importedNode = newDoc.importNode(child, true);
          newRoot.appendChild(importedNode);
        }
      }
      
      // If we filtered out all children, return the original XML
      if (newRoot.children.length === 0) {
        console.warn(`No matching elements found for ${messageType} mapping, returning original XML`);
        return xml;
      }
      
      // If we have exactly one child element after filtering, and the root element
      // appears to be a generic wrapper (like SourceMessage/TargetMessage),
      // return just that child element as the new root
      if (newRoot.children.length === 1) {
        const rootName = root.tagName.toLowerCase();
        if (rootName === 'sourcemessage' || rootName === 'targetmessage' || 
            rootName === 'message' || rootName === 'root') {
          // Return the child element as the new root
          const childElement = newRoot.children[0];
          const childDoc = document.implementation.createDocument(null, childElement.tagName, null);
          const newChildRoot = childDoc.importNode(childElement, true);
          childDoc.replaceChild(newChildRoot, childDoc.documentElement);
          const serializer = new XMLSerializer();
          return serializer.serializeToString(childDoc);
        }
      }
      
      // Serialize back to string
      const serializer = new XMLSerializer();
      return serializer.serializeToString(newDoc);
    } catch (error) {
      console.error('Error filtering XML by message type:', error);
      return xml;
    }
  };

  // Parse pre-converted XML on mount
  useEffect(() => {
    if (preConverted && sourceXml && targetXml) {
      try {
        // Filter XML based on mapping type
        const filteredSourceXml = filterXmlByMessageType(sourceXml, mappingType);
        const filteredTargetXml = filterXmlByMessageType(targetXml, mappingType);
        
        // Parse XML directly
        const sourceFieldNodes = parseXmlToFieldNodes(filteredSourceXml);
        const targetFieldNodes = parseXmlToFieldNodes(filteredTargetXml);
        
        setSourceFields(sourceFieldNodes);
        setTargetFields(targetFieldNodes);
        setSelectedSource(`Pre-converted XML (${mappingType})`);
        setSelectedTarget(`Pre-converted XML (${mappingType})`);
        
        console.log(`Filtering XML for ${mappingType} mapping:`, {
          originalSource: sourceXml,
          filteredSource: filteredSourceXml,
          originalTarget: targetXml,
          filteredTarget: filteredTargetXml
        });
        
      } catch (error: any) {
        console.error('Error parsing pre-converted XML:', error);
        toast({
          title: "XML Parse Error",
          description: error.message || "Failed to parse XML structures",
          variant: "destructive",
        });
      }
    } else if (!preConverted) {
      // Auto-select data structures based on adapter types
      const autoSelectStructures = async () => {
        if (sourceAdapterType && targetAdapterType && structures.length > 0 && !selectedSource && !selectedTarget) {
          // Find compatible data structures for the selected adapters
          const sourceStructures = structures.filter(s => s.usage === 'source' || !s.usage);
          const targetStructures = structures.filter(s => s.usage === 'target' || !s.usage);
          
          // Auto-select the first available structures
          if (sourceStructures.length > 0) {
            await selectDataStructure(sourceStructures[0].name, true);
          }
          if (targetStructures.length > 0) {
            await selectDataStructure(targetStructures[0].name, false);
          }
        }
      };
      
      autoSelectStructures();
    }
  }, [preConverted, sourceXml, targetXml, sourceAdapterType, targetAdapterType, structures, mappingType]);

  const toggleExpanded = useCallback((nodeId: string, isSource: boolean) => {
    const updateNode = (nodes: FieldNode[]): FieldNode[] => {
      return nodes.map(node => {
        if (node.id === nodeId) {
          return { ...node, expanded: !node.expanded };
        }
        if (node.children) {
          return { ...node, children: updateNode(node.children) };
        }
        return node;
      });
    };

    if (isSource) {
      setSourceFields(updateNode);
    } else {
      setTargetFields(updateNode);
    }
  }, []);

  const handleDragStart = (field: FieldNode) => {
    setDraggedField(field);
  };

  const handleDragEnd = () => {
    setDraggedField(null);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (targetField: FieldNode) => {
    if (!draggedField) return;
    
    // Check if this is a node-to-node mapping (both source and target are arrays or objects)
    const isSourceNode = draggedField.type === 'array' || draggedField.type === 'object';
    const isTargetNode = targetField.type === 'array' || targetField.type === 'object';
    const isNodeMapping = isSourceNode && isTargetNode;
    
    // Find existing mapping for target field
    const existingMappingIndex = mappings.findIndex(m => m.targetPath === targetField.path);
    
    if (existingMappingIndex >= 0) {
      // Add to existing mapping (multi-source)
      const existingMapping = mappings[existingMappingIndex];
      const updatedMapping: FieldMapping = {
        ...existingMapping,
        sourceFields: [...existingMapping.sourceFields, draggedField.name],
        sourcePaths: [...existingMapping.sourcePaths, draggedField.path]
      };
      
      const updatedMappings = [...mappings];
      updatedMappings[existingMappingIndex] = updatedMapping;
      setMappings(updatedMappings);
    } else {
      // Create new mapping
      const newMapping: FieldMapping = {
        id: `mapping_${Date.now()}`,
        name: `${draggedField.name}_to_${targetField.name}`,
        sourceFields: [draggedField.name],
        targetField: targetField.name,
        sourcePaths: [draggedField.path],
        targetPath: targetField.path,
        // Mark as array mapping if it's a node-to-node mapping
        ...(isNodeMapping && {
          javaFunction: 'nodeMapping',
          functionNode: {
            id: `node_${Date.now()}`,
            functionName: 'nodeMapping',
            parameters: { 
              sourceType: draggedField.type,
              targetType: targetField.type,
              isArrayMapping: draggedField.type === 'array' || targetField.type === 'array' ? 'true' : 'false'
            },
            sourceConnections: {},
            position: { x: 0, y: 0 }
          }
        })
      };
      
      setMappings(prev => [...prev, newMapping]);
      
      // Show a toast for node mapping
      if (isNodeMapping) {
        toast({
          title: "Node Mapping Created",
          description: `Created ${draggedField.type} to ${targetField.type} mapping. This will map all child fields.`,
        });
      }
    }
    
    setDraggedField(null);
  };

  const removeMapping = (mappingId: string) => {
    setMappings(prev => prev.filter(m => m.id !== mappingId));
  };

  const clearAllMappings = () => {
    setMappings([]);
  };

  // Helper function to get field name without path
  const getFieldName = (field: FieldNode): string => {
    // Remove array notation and get the last part of the name
    return field.name.replace(/\[\]$/, '').split('.').pop() || field.name;
  };

  // Helper function to collect all fields from a node tree
  const collectFields = (nodes: FieldNode[], collected: FieldNode[] = []): FieldNode[] => {
    nodes.forEach(node => {
      // Collect all nodes (leaf nodes, arrays, and objects)
      collected.push(node);
      
      if (node.children) {
        collectFields(node.children, collected);
      }
    });
    return collected;
  };

  // Auto-map function to create mappings for matching field names
  const autoMapFields = () => {
    if (!sourceFields.length || !targetFields.length) {
      toast({
        title: "Cannot auto-map",
        description: "Please select both source and target structures first",
        variant: "destructive"
      });
      return;
    }

    const newMappings: FieldMapping[] = [];
    const existingTargetPaths = new Set(mappings.map(m => m.targetPath));

    // Check if specific fields are selected
    if (selectedSourceField && selectedTargetField) {
      const isSourceNode = (selectedSourceField.type === 'array' || selectedSourceField.type === 'object') && 
                          selectedSourceField.children && selectedSourceField.children.length > 0;
      const isTargetNode = (selectedTargetField.type === 'array' || selectedTargetField.type === 'object') &&
                          selectedTargetField.children && selectedTargetField.children.length > 0;
      
      // If both are nodes, map all matching fields within them
      if (isSourceNode && isTargetNode) {
        let mappedCount = 0;
        
        // ALWAYS map the selected nodes themselves, regardless of name matching
        if (!existingTargetPaths.has(selectedTargetField.path)) {
          const nodeMapping: FieldMapping = {
            id: `mapping_${Date.now()}_${Math.random()}`,
            name: `${selectedSourceField.name}_to_${selectedTargetField.name}`,
            sourceFields: [selectedSourceField.name],
            targetField: selectedTargetField.name,
            sourcePaths: [selectedSourceField.path],
            targetPath: selectedTargetField.path,
            javaFunction: 'nodeMapping',
            functionNode: {
              id: `node_${Date.now()}_${Math.random()}`,
              functionName: 'nodeMapping',
              parameters: { 
                sourceType: selectedSourceField.type,
                targetType: selectedTargetField.type,
                isArrayMapping: (selectedSourceField.type === 'array' || selectedTargetField.type === 'array') ? 'true' : 'false'
              },
              sourceConnections: {},
              position: { x: 0, y: 0 }
            }
          };
          
          newMappings.push(nodeMapping);
          existingTargetPaths.add(selectedTargetField.path);
          mappedCount++;
        }
        
        // Now collect children only (not including the parent nodes)
        const sourceChildren = collectFields(selectedSourceField.children || []);
        const targetChildren = collectFields(selectedTargetField.children || []);
        
        // Create a map of target fields by name for quick lookup
        const targetChildMap = new Map<string, FieldNode[]>();
        targetChildren.forEach(field => {
          const fieldName = getFieldName(field);
          if (!targetChildMap.has(fieldName)) {
            targetChildMap.set(fieldName, []);
          }
          targetChildMap.get(fieldName)!.push(field);
        });
        
        // Map all matching child fields
        sourceChildren.forEach(sourceField => {
          const sourceFieldName = getFieldName(sourceField);
          const matchingTargetFields = targetChildMap.get(sourceFieldName) || [];
          
          // Find the best matching target field (prefer same type)
          const targetField = matchingTargetFields.find(tf => 
            !existingTargetPaths.has(tf.path) && tf.type === sourceField.type
          ) || matchingTargetFields.find(tf => 
            !existingTargetPaths.has(tf.path)
          );
          
          if (targetField) {
            const isNodeMapping = (sourceField.type === 'array' || sourceField.type === 'object') &&
                                (targetField.type === 'array' || targetField.type === 'object');
            
            const newMapping: FieldMapping = {
              id: `mapping_${Date.now()}_${Math.random()}`,
              name: `${sourceField.name}_to_${targetField.name}`,
              sourceFields: [sourceField.name],
              targetField: targetField.name,
              sourcePaths: [sourceField.path],
              targetPath: targetField.path,
              ...(isNodeMapping && {
                javaFunction: 'nodeMapping',
                functionNode: {
                  id: `node_${Date.now()}_${Math.random()}`,
                  functionName: 'nodeMapping',
                  parameters: { 
                    sourceType: sourceField.type,
                    targetType: targetField.type,
                    isArrayMapping: (sourceField.type === 'array' || targetField.type === 'array') ? 'true' : 'false'
                  },
                  sourceConnections: {},
                  position: { x: 0, y: 0 }
                }
              })
            };
            
            newMappings.push(newMapping);
            existingTargetPaths.add(targetField.path);
            mappedCount++;
          }
        });
        
        if (mappedCount > 0) {
          setMappings(prev => [...prev, ...newMappings]);
          toast({
            title: "Node mapping complete",
            description: `Created ${mappedCount} mappings for matching fields within selected nodes`,
          });
        } else {
          toast({
            title: "No matching fields found",
            description: "Could not find any matching field names within the selected nodes",
            variant: "destructive"
          });
        }
        
        // Clear selections after mapping
        setSelectedSourceField(null);
        setSelectedTargetField(null);
      } else {
        // Element-to-element mapping - allow any to any
        if (!existingTargetPaths.has(selectedTargetField.path)) {
          const newMapping: FieldMapping = {
            id: `mapping_${Date.now()}_${Math.random()}`,
            name: `${selectedSourceField.name}_to_${selectedTargetField.name}`,
            sourceFields: [selectedSourceField.name],
            targetField: selectedTargetField.name,
            sourcePaths: [selectedSourceField.path],
            targetPath: selectedTargetField.path,
          };
          
          setMappings(prev => [...prev, newMapping]);
          toast({
            title: "Mapping created",
            description: `Mapped ${selectedSourceField.name} to ${selectedTargetField.name}`,
          });
          
          // Clear selections after mapping
          setSelectedSourceField(null);
          setSelectedTargetField(null);
        } else {
          toast({
            title: "Target already mapped",
            description: "The selected target field is already mapped",
            variant: "destructive"
          });
        }
      }
      return;
    }
    

    // Default behavior - map all fields (when nothing is selected)

    // Collect all source and target fields
    const allSourceFields = collectFields(sourceFields);
    const allTargetFields = collectFields(targetFields);

    // Create a map of target fields by name for quick lookup
    const targetFieldMap = new Map<string, FieldNode[]>();
    allTargetFields.forEach(field => {
      const fieldName = getFieldName(field);
      if (!targetFieldMap.has(fieldName)) {
        targetFieldMap.set(fieldName, []);
      }
      targetFieldMap.get(fieldName)!.push(field);
    });

    // Match source fields to target fields
    allSourceFields.forEach(sourceField => {
      const sourceFieldName = getFieldName(sourceField);
      const matchingTargetFields = targetFieldMap.get(sourceFieldName) || [];

      // Find the best matching target field (prefer same type)
      const targetField = matchingTargetFields.find(tf => 
        !existingTargetPaths.has(tf.path) && tf.type === sourceField.type
      ) || matchingTargetFields.find(tf => 
        !existingTargetPaths.has(tf.path)
      );

      if (targetField) {
        const isNodeMapping = (sourceField.type === 'array' || sourceField.type === 'object') &&
                            (targetField.type === 'array' || targetField.type === 'object');

        const newMapping: FieldMapping = {
          id: `mapping_${Date.now()}_${Math.random()}`,
          name: `${sourceField.name}_to_${targetField.name}`,
          sourceFields: [sourceField.name],
          targetField: targetField.name,
          sourcePaths: [sourceField.path],
          targetPath: targetField.path,
          // Mark as array mapping if it's a node-to-node mapping
          ...(isNodeMapping && {
            javaFunction: 'nodeMapping',
            functionNode: {
              id: `node_${Date.now()}_${Math.random()}`,
              functionName: 'nodeMapping',
              parameters: { 
                sourceType: sourceField.type,
                targetType: targetField.type,
                isArrayMapping: (sourceField.type === 'array' || targetField.type === 'array') ? 'true' : 'false'
              },
              sourceConnections: {},
              position: { x: 0, y: 0 }
            }
          })
        };

        newMappings.push(newMapping);
        existingTargetPaths.add(targetField.path);
      }
    });

    if (newMappings.length > 0) {
      setMappings(prev => [...prev, ...newMappings]);
      toast({
        title: "Auto-mapping complete",
        description: `Created ${newMappings.length} automatic mappings`,
      });
    } else {
      toast({
        title: "No matching fields found",
        description: "Could not find any matching field names between source and target",
        variant: "destructive"
      });
    }
  };

  const selectDataStructure = async (structureName: string, isSource: boolean) => {
    const selectedStructure = structures.find(s => s.name === structureName);
    if (!selectedStructure || !selectedStructure.id) return;
    
    try {
      // Set loading state
      if (isSource) {
        setLoadingSource(true);
      } else {
        setLoadingTarget(true);
      }
      
      // Convert the structure to XML
      const xmlResult = await convertStructureToXml(selectedStructure.id, {
        rootElementName: selectedStructure.name.replace(/\s+/g, ''),
        includeXmlDeclaration: true,
        prettyPrint: true,
        convertPropertyNames: true,
        preserveNullValues: false
      });
      const xmlContent = xmlResult.xmlContent;
      
      // Parse XML to field nodes
      let fieldNodes;
      try {
        fieldNodes = parseXmlToFieldNodes(xmlContent);
      } catch (parseError) {
        console.error('Error parsing XML:', parseError);
        console.error('XML content that failed to parse:', xmlContent);
        throw parseError;
      }
      
      if (isSource) {
        setSelectedSource(structureName);
        setSourceFields(fieldNodes);
        setShowSourceSelector(false);
      } else {
        setSelectedTarget(structureName);
        setTargetFields(fieldNodes);
        setShowTargetSelector(false);
      }
      
      toast({
        title: "Structure converted to XML",
        description: `${structureName} has been converted to XML format for mapping`,
      });
      
    } catch (error: any) {
      console.error('Error converting structure to XML:', error);
      toast({
        title: "Conversion failed",
        description: error.message || "Failed to convert structure to XML",
        variant: "destructive",
      });
    } finally {
      if (isSource) {
        setLoadingSource(false);
      } else {
        setLoadingTarget(false);
      }
    }
  };

  const updateMapping = (mappingId: string, updates: Partial<FieldMapping>) => {
    setMappings(prev => prev.map(m => 
      m.id === mappingId ? { ...m, ...updates } : m
    ));
  };

  const createMapping = (mapping: FieldMapping) => {
    setMappings(prev => [...prev, mapping]);
  };

  return (
    <div className="fixed inset-0 bg-background z-50 overflow-hidden animate-fade-in">
      {/* Header */}
      <div className="border-b">
        {/* Mapping Name and Actions */}
        <div className="h-16 flex items-center justify-between px-6">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <Label htmlFor="mappingName" className="text-sm font-medium">Mapping Name:</Label>
              <Input
                id="mappingName"
                placeholder="Enter mapping name"
                value={mappingName}
                onChange={(e) => setMappingName(e.target.value)}
                className="w-64"
              />
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <Button 
              variant="outline" 
              onClick={autoMapFields} 
              className="hover-scale"
              disabled={!sourceFields.length || !targetFields.length}
            >
              <Zap className="h-4 w-4 mr-2" />
              {selectedSourceField && selectedTargetField ? 'Map Selected' : 'Map All'}
            </Button>
            <Button 
              variant="outline" 
              onClick={clearAllMappings} 
              className="text-destructive hover:text-destructive hover-scale"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete All Mappings
            </Button>
            {mappings.length > 0 && (
              <>
                <Button 
                  variant="outline"
                  onClick={() => setShowTestDialog(true)} 
                  className="hover-scale"
                >
                  <Play className="h-4 w-4 mr-2" />
                  Test Mapping
                </Button>
                <Button 
                  onClick={() => onSave?.(mappings, mappingName)} 
                  className="hover-scale"
                  disabled={!mappingName.trim()}
                >
                  <CheckCircle className="h-4 w-4 mr-2" />
                  Save Mappings
                </Button>
              </>
            )}
            <Button 
              variant="outline" 
              onClick={onClose} 
              className="hover-scale"
            >
              <X className="h-4 w-4 mr-2" />
              Close
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content - Always Traditional View */}
      <div className="flex h-[calc(100vh-8rem)]">
        <SourcePanel
          fields={sourceFields}
          mappings={mappings}
          selectedService={selectedSource}
          searchValue={searchSource}
          showSelector={!preConverted && showSourceSelector}
          selectedField={selectedSourceField}
          onSearchChange={setSearchSource}
          onShowSelectorChange={setShowSourceSelector}
          onSelectService={(service) => selectDataStructure(service, true)}
          onToggleExpanded={toggleExpanded}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onSelectField={setSelectedSourceField}
          isLoading={loadingSource}
          hideSelector={preConverted}
        />

        <MappingArea
          mappings={mappings}
          sourceFields={sourceFields}
          targetFields={targetFields}
          onRemoveMapping={removeMapping}
          onUpdateMapping={updateMapping}
          onCreateMapping={createMapping}
        />

        <TargetPanel
          fields={targetFields}
          mappings={mappings}
          selectedService={selectedTarget}
          searchValue={searchTarget}
          showSelector={!preConverted && showTargetSelector}
          selectedField={selectedTargetField}
          onSearchChange={setSearchTarget}
          onShowSelectorChange={setShowTargetSelector}
          onSelectService={(service) => selectDataStructure(service, false)}
          onToggleExpanded={toggleExpanded}
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          onSelectField={setSelectedTargetField}
          isLoading={loadingTarget}
          hideSelector={preConverted}
        />
      </div>

      <TestMappingDialog
        open={showTestDialog}
        onOpenChange={setShowTestDialog}
        mappings={mappings}
        mappingName={mappingName}
        mappingType={mappingType}
        sourceXml={sourceXml}
        targetXml={targetXml}
      />
    </div>
  );
}