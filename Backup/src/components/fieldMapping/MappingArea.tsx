
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ArrowRight, Code, X, Zap } from 'lucide-react';
import { FieldMapping, FieldNode } from './types';
import { FunctionPicker } from './FunctionPicker';
import { FunctionMappingModal } from './FunctionMappingModal';
import { VisualFlowEditor } from './VisualFlowEditor';

interface MappingAreaProps {
  mappings: FieldMapping[];
  sourceFields?: FieldNode[];
  targetFields?: FieldNode[];
  onRemoveMapping: (mappingId: string) => void;
  
  onUpdateMapping?: (mappingId: string, updates: Partial<FieldMapping>) => void;
  onCreateMapping?: (mapping: FieldMapping) => void;
}

export function MappingArea({ 
  mappings, 
  sourceFields = [], 
  targetFields = [], 
  onRemoveMapping,
  onUpdateMapping,
  onCreateMapping
}: MappingAreaProps) {
  const [functionMappingModal, setFunctionMappingModal] = useState<{
    open: boolean;
    selectedFunction: string;
    targetField: FieldNode | null;
    existingMappingId?: string;
    filteredSourceFields?: FieldNode[];
  }>({
    open: false,
    selectedFunction: '',
    targetField: null,
    filteredSourceFields: []
  });

  const [visualFlowEditor, setVisualFlowEditor] = useState<{
    open: boolean;
    targetField: FieldNode | null;
    existingMapping?: FieldMapping;
  }>({
    open: false,
    targetField: null
  });

  useEffect(() => {
    console.log('🔍 Modal state changed:', functionMappingModal);
  }, [functionMappingModal]);

  useEffect(() => {
    console.log('🔥 Visual flow editor state changed:', visualFlowEditor);
  }, [visualFlowEditor]);

  const handleQuickFunction = (mappingId: string, functionName: string, javaCode: string) => {
    console.log('🔍 handleQuickFunction called with:', { mappingId, functionName, javaCode });
    
    // Find the existing mapping to get the target field
    const existingMapping = mappings.find(m => m.id === mappingId);
    console.log('🔍 Found existing mapping:', existingMapping);
    
    if (!existingMapping) {
      console.log('❌ No existing mapping found for ID:', mappingId);
      return;
    }

    // Find the target field node
    const targetField = targetFields.find(field => field.name === existingMapping.targetField);
    console.log('🔍 Looking for target field:', existingMapping.targetField);
    console.log('🔍 Available target fields:', targetFields.map(f => f.name));
    console.log('🔍 Found target field:', targetField);
    
    if (!targetField) {
      console.log('❌ Target field not found, falling back to simple function update');
      // Fallback to simple function update if target field not found
      if (onUpdateMapping) {
        
      }
      return;
    }

    // Filter source fields to only show the ones already mapped in this mapping
    const mappedSourceFields = sourceFields.filter(field => 
      existingMapping.sourceFields.includes(field.name) || 
      existingMapping.sourcePaths.includes(field.path)
    );
    
    console.log('🔍 Existing mapping source fields:', existingMapping.sourceFields);
    console.log('🔍 Existing mapping source paths:', existingMapping.sourcePaths);
    console.log('🔍 Filtered source fields for visual mapper:', mappedSourceFields.map(f => f.name));

    console.log('✅ Opening visual mapping modal with:', {
      functionName,
      targetField: targetField.name,
      mappingId,
      sourceFieldCount: mappedSourceFields.length
    });
    
    // Open the visual mapping modal with filtered source fields
    setFunctionMappingModal({
      open: true,
      selectedFunction: functionName,
      targetField,
      existingMappingId: mappingId,
      filteredSourceFields: mappedSourceFields
    });
  };

  const handleOpenVisualFlowEditor = (mappingId: string) => {
    console.log('🔥 handleOpenVisualFlowEditor called with mappingId:', mappingId);
    console.log('🔥 Available mappings:', mappings);
    console.log('🔥 Available targetFields:', targetFields);
    console.log('🔥 Target fields names:', targetFields.map(f => f?.name));
    
    const existingMapping = mappings.find(m => m.id === mappingId);
    console.log('🔥 Found existing mapping:', existingMapping);
    
    if (!existingMapping) {
      console.log('❌ No existing mapping found for ID:', mappingId);
      return;
    }

    const targetField = targetFields.find(field => field?.name === existingMapping.targetField);
    console.log('🔥 Looking for target field with name:', existingMapping.targetField);
    console.log('🔥 Found target field:', targetField);
    
    if (!targetField) {
      console.log('❌ Target field not found, attempting to create a mock target field');
      
      // Create a mock target field if not found in the array
      const mockTargetField: FieldNode = {
        id: `target_${existingMapping.targetField}`,
        name: existingMapping.targetField,
        type: 'string', // Default type
        path: existingMapping.targetPath || existingMapping.targetField,
        children: [],
        expanded: false
      };
      
      console.log('🔥 Created mock target field:', mockTargetField);
      
      setVisualFlowEditor({
        open: true,
        targetField: mockTargetField,
        existingMapping
      });
      return;
    }

    console.log('✅ Opening visual flow editor with:', {
      targetField: targetField.name,
      existingMapping: existingMapping.id
    });

    setVisualFlowEditor({
      open: true,
      targetField,
      existingMapping
    });
  };

  const handleApplyVisualFlow = (newMapping: FieldMapping) => {
    console.log('🔥 handleApplyVisualFlow called with:', newMapping);
    
    if (visualFlowEditor.existingMapping) {
      // Update existing mapping - include ALL fields, especially visualFlowData
      if (onUpdateMapping) {
        onUpdateMapping(visualFlowEditor.existingMapping.id, {
          functionNode: newMapping.functionNode,
          visualFlowData: newMapping.visualFlowData, // This was missing!
          requiresTransformation: true,
          sourceFields: newMapping.sourceFields,
          sourcePaths: newMapping.sourcePaths,
          name: newMapping.name
        });
      }
    } else {
      // Create new mapping
      if (onCreateMapping) {
        onCreateMapping(newMapping);
      }
    }

    // Close the editor
    setVisualFlowEditor({
      open: false,
      targetField: null
    });
  };

  const handleApplyFunctionMapping = (newMapping: FieldMapping) => {
    if (functionMappingModal.existingMappingId) {
      // Update existing mapping
      if (onUpdateMapping) {
        onUpdateMapping(functionMappingModal.existingMappingId, {
          functionNode: newMapping.functionNode,
          requiresTransformation: true,
          sourceFields: newMapping.sourceFields,
          sourcePaths: newMapping.sourcePaths
        });
      }
    } else {
      // Create new mapping
      if (onCreateMapping) {
        onCreateMapping(newMapping);
      }
    }

    // Close the modal
    setFunctionMappingModal({
      open: false,
      selectedFunction: '',
      targetField: null,
      filteredSourceFields: []
    });
  };

  // Debug logging for render
  console.log('🔍 MappingArea render - mappings count:', mappings.length);
  console.log('🔍 MappingArea render - mappings:', mappings);

  return (
    <div className="w-1/3 relative bg-background animate-fade-in">
      <div className="p-4 border-b">
        <h3 className="font-semibold flex items-center gap-2">
          <ArrowRight className="h-4 w-4" />
          Field Mappings ({mappings.length})
        </h3>
      </div>

      <div className="p-4 h-[calc(100%-60px)] overflow-y-auto">
        {mappings.length === 0 ? (
          <Alert className="mt-8">
            <AlertDescription>
              Drag fields from source to target to create mappings
            </AlertDescription>
          </Alert>
        ) : (
          <div className="space-y-3">
            {mappings.map(mapping => (
              <div key={mapping.id} className="p-3 border rounded-lg bg-muted/30 animate-scale-in">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Badge variant="outline" className="text-xs">
                      {mapping.sourceFields.length > 1 ? 'Multi-Source Mapping' : 'Mapping'}
                    </Badge>
                    {mapping.requiresTransformation === false && (
                      <Badge variant="secondary" className="text-xs">
                        Pass-through
                      </Badge>
                    )}
                    {mapping.functionNode && (
                      <Badge variant="default" className="text-xs">
                        Function: {mapping.functionNode.functionName}
                      </Badge>
                    )}
                  </div>
                  <div className="flex gap-1">
                    {mapping.requiresTransformation !== false && (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            console.log('🔥 Lightning bolt clicked for mapping:', mapping.id);
                            handleOpenVisualFlowEditor(mapping.id);
                          }}
                          className="h-6 w-6 p-0 hover-scale"
                          title="Open Visual Flow Editor"
                        >
                          <Zap className="h-3 w-3" />
                        </Button>
                      </>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onRemoveMapping(mapping.id)}
                      className="h-6 w-6 p-0 text-destructive hover:text-destructive hover-scale"
                      title="Remove Mapping"
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                
                <div className="text-xs space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-primary">{mapping.name}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Source:</span>
                    <span className="text-muted-foreground">
                      {mapping.sourceFields.join(', ')}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <ArrowRight className="h-3 w-3 text-primary" />
                    <span className="font-medium">Target:</span>
                    <span className="text-muted-foreground">{mapping.targetField}</span>
                  </div>
                  {mapping.requiresTransformation !== false && (
                    <>
                      {mapping.functionNode && (
                        <div className="mt-2 p-2 bg-primary/10 rounded text-xs">
                          <div className="flex items-center gap-2 mb-1">
                            <Zap className="h-3 w-3 text-primary" />
                            <span className="font-medium text-primary">Function Mapping:</span>
                          </div>
                          <div className="text-muted-foreground">
                            {mapping.functionNode.functionName === 'visual_flow' ? (
                              mapping.visualFlowData ? (
                                `Visual Flow - ${mapping.sourceFields.length} source field(s): ${mapping.sourceFields.join(', ')}`
                              ) : (
                                `Visual Flow - ${mapping.sourceFields.length} source field(s) connected`
                              )
                            ) : (
                              `${mapping.functionNode.functionName} with ${Object.keys(mapping.functionNode.sourceConnections).length} parameter(s) connected`
                            )}
                          </div>
                        </div>
                      )}
                       {!mapping.functionNode && (
                        <div className="mt-2 text-xs text-muted-foreground italic">
                          Click the lightning icon to configure transformations using the visual flow editor
                        </div>
                      )}
                    </>
                  )}
                  {mapping.requiresTransformation === false && (
                    <div className="mt-2 text-xs text-muted-foreground italic">
                      Direct field mapping - no transformation required
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Function Mapping Modal */}
      {functionMappingModal.open && functionMappingModal.targetField && (
        <FunctionMappingModal
          open={functionMappingModal.open}
          onClose={() => setFunctionMappingModal({
            open: false,
            selectedFunction: '',
            targetField: null,
            filteredSourceFields: []
          })}
          selectedFunction={functionMappingModal.selectedFunction}
          sourceFields={functionMappingModal.filteredSourceFields || sourceFields}
          targetField={functionMappingModal.targetField}
          onApplyMapping={handleApplyFunctionMapping}
        />
      )}

      {/* Visual Flow Editor */}
      {visualFlowEditor.open && visualFlowEditor.targetField && (
        <VisualFlowEditor
          open={visualFlowEditor.open}
          onClose={() => setVisualFlowEditor({
            open: false,
            targetField: null
          })}
          sourceFields={sourceFields}
          targetField={visualFlowEditor.targetField}
          onApplyMapping={handleApplyVisualFlow}
          initialMapping={visualFlowEditor.existingMapping}
        />
      )}
    </div>
  );
}
