import React, { useState, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { FunctionPicker } from './FunctionPicker';
import { VisualFlowEditor } from './VisualFlowEditor';
import { FieldNode, FieldMapping } from './types';
import { Plus, Settings } from 'lucide-react';

interface VisualMappingCanvasProps {
  sourceFields: FieldNode[];
  targetFields: FieldNode[];
  mappings: FieldMapping[];
  draggedField: FieldNode | null;
  onUpdateMapping: (mappingId: string, updates: Partial<FieldMapping>) => void;
  onCreateMapping: (mapping: FieldMapping) => void;
  onRemoveMapping: (mappingId: string) => void;
  onDragEnd: () => void;
  currentTargetField?: FieldNode;
  selectedSourceStructure?: string;
}

export const VisualMappingCanvas: React.FC<VisualMappingCanvasProps> = ({
  sourceFields,
  targetFields,
  mappings,
  onCreateMapping,
  onRemoveMapping,
  currentTargetField,
  selectedSourceStructure
}) => {
  const [visualFlowEditor, setVisualFlowEditor] = useState<{
    open: boolean;
    targetField: FieldNode | null;
  }>({
    open: false,
    targetField: null
  });

  // Filter fields based on current context
  const filteredSourceFields = currentTargetField && selectedSourceStructure 
    ? sourceFields 
    : sourceFields;

  // Handle visual flow mapping
  const handleApplyFlowMapping = useCallback((mapping: FieldMapping) => {
    // Remove any existing mapping for this target field
    mappings.forEach(existingMapping => {
      if (existingMapping.targetField === mapping.targetField) {
        onRemoveMapping(existingMapping.id);
      }
    });
    
    // Create the new flow-based mapping
    onCreateMapping(mapping);
    
    // Close the editor
    setVisualFlowEditor({
      open: false,
      targetField: null
    });
  }, [mappings, onCreateMapping, onRemoveMapping]);

  return (
    <div className="relative w-full h-full bg-background overflow-hidden border rounded-lg">
      {/* Toolbar */}
      <div className="flex items-center justify-between p-3 border-b bg-muted/30">
        <div className="flex items-center gap-2">
          <Button 
            variant="outline" 
            size="sm" 
            className="h-8"
            disabled={!currentTargetField}
            onClick={() => {
              if (currentTargetField) {
                setVisualFlowEditor({
                  open: true,
                  targetField: currentTargetField
                });
              }
            }}
          >
            <Plus className="h-4 w-4 mr-1" />
            Open Flow Editor
          </Button>
          <Button variant="outline" size="sm" className="h-8">
            <Settings className="h-4 w-4 mr-1" />
            Settings
          </Button>
        </div>
        <div className="text-sm text-muted-foreground">
          {mappings.length} mapping(s) • Visual flow mapping
        </div>
      </div>

      {/* Main content area */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="text-center max-w-md">
          <div className="text-lg font-semibold text-primary mb-2">
            Visual Flow Field Mapping
          </div>
           <div className="text-muted-foreground mb-6">
             {currentTargetField 
               ? `Open the visual flow editor to create a mapping to "${currentTargetField.name}"`
               : "Select a target field first, then open the flow editor to create complex mappings"
             }
           </div>
          
          {!currentTargetField && (
            <div className="bg-muted/50 rounded-lg p-4 text-sm text-muted-foreground">
              <div className="font-medium mb-2">To create a mapping:</div>
              <ol className="text-left space-y-1">
                <li>1. Select a target field from the right panel</li>
                <li>2. Click "Open Flow Editor" to start building</li>
                <li>3. Add source fields, functions, constants as nodes</li>
                <li>4. Connect nodes to create your transformation flow</li>
                <li>5. Connect the final output to the target field</li>
                <li>6. Save the flow</li>
              </ol>
            </div>
          )}
          
          {currentTargetField && (
            <div className="bg-primary/10 rounded-lg p-4 border border-primary/20">
              <div className="text-sm font-medium text-primary mb-2">
                Target Field Selected
              </div>
              <div className="text-sm">
                <span className="font-medium">{currentTargetField.name}</span>
                <span className="text-muted-foreground ml-2">({currentTargetField.type})</span>
              </div>
              <div className="mt-3">
                <Button 
                  size="sm" 
                  className="bg-primary text-primary-foreground"
                  onClick={() => {
                    setVisualFlowEditor({
                      open: true,
                      targetField: currentTargetField
                    });
                  }}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Open Flow Editor
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Status bar */}
      <div className="absolute bottom-0 left-0 right-0 border-t bg-muted/30 p-2">
        <div className="flex justify-between items-center text-xs text-muted-foreground">
          <div>
            Visual flow mapping • Select target field and open editor to create complex mappings
          </div>
          <div>
            {selectedSourceStructure && `Source: ${selectedSourceStructure}`}
          </div>
        </div>
      </div>

      {/* Visual Flow Editor */}
      {visualFlowEditor.open && visualFlowEditor.targetField && (
        <VisualFlowEditor
          open={visualFlowEditor.open}
          onClose={() => setVisualFlowEditor({
            open: false,
            targetField: null
          })}
          sourceFields={filteredSourceFields}
          targetField={visualFlowEditor.targetField}
          onApplyMapping={handleApplyFlowMapping}
          initialMapping={mappings.find(m => m.targetField === visualFlowEditor.targetField?.name)}
        />
      )}
    </div>
  );
};