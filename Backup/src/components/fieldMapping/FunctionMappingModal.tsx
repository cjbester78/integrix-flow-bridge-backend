import React, { useState, useRef, useCallback } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FieldNode, FieldMapping, FunctionNodeData } from './types';
import { functionsByCategory, TransformationFunction } from '@/services/transformationFunctions';
import { X, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

interface FunctionMappingModalProps {
  open: boolean;
  onClose: () => void;
  selectedFunction: string;
  sourceFields: FieldNode[];
  targetField: FieldNode | null;
  onApplyMapping: (mapping: FieldMapping) => void;
}

interface Connection {
  id: string;
  sourceId: string;
  targetId: string;
  sourceX: number;
  sourceY: number;
  targetX: number;
  targetY: number;
}

interface DragState {
  isDragging: boolean;
  draggedItem: FieldNode | null;
  startPosition: { x: number; y: number };
  currentPosition: { x: number; y: number };
}

export const FunctionMappingModal: React.FC<FunctionMappingModalProps> = ({
  open,
  onClose,
  selectedFunction,
  sourceFields,
  targetField,
  onApplyMapping
}) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const svgRef = useRef<SVGSVGElement>(null);

  const [functionNode, setFunctionNode] = useState<FunctionNodeData>({
    id: 'function_modal',
    functionName: selectedFunction,
    parameters: {},
    sourceConnections: {},
    position: { x: 0, y: 0 }
  });

  const [connections, setConnections] = useState<Connection[]>([]);
  const [dragState, setDragState] = useState<DragState>({
    isDragging: false,
    draggedItem: null,
    startPosition: { x: 0, y: 0 },
    currentPosition: { x: 0, y: 0 }
  });
  const [dropTargets, setDropTargets] = useState<Set<string>>(new Set());
  const [outputConnected, setOutputConnected] = useState(false);

  const getAllFunctions = () => Object.values(functionsByCategory).flat();
  const func = getAllFunctions().find(f => f.name === selectedFunction);

  // Mouse-based drag implementation
  const handleMouseDown = useCallback((field: FieldNode, event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    
    // Prevent text selection during drag
    document.body.style.userSelect = 'none';
    document.body.style.webkitUserSelect = 'none';
    
    console.log('🎯 DRAG START - Field:', field.name, 'Position:', { x: event.clientX, y: event.clientY });
    console.log('🎯 DRAG START - Event target:', event.target);
    console.log('🎯 DRAG START - Event currentTarget:', event.currentTarget);
    
    setDragState({
      isDragging: true,
      draggedItem: field,
      startPosition: { x: event.clientX, y: event.clientY },
      currentPosition: { x: event.clientX, y: event.clientY }
    });

    const targets = new Set<string>();
    func?.parameters.forEach(param => {
      if (!param.name.toLowerCase().includes('delimiter') && !param.name.toLowerCase().includes('separator')) {
        targets.add(`param-${functionNode.id}-${param.name}`);
        console.log('🎯 DRAG START - Added drop target:', `param-${functionNode.id}-${param.name}`);
      }
    });
    setDropTargets(targets);
    console.log('🎯 DRAG START - Total drop targets:', targets.size);
  }, [func, functionNode.id]);

  const handleMouseMove = useCallback((event: MouseEvent) => {
    if (dragState.isDragging) {
      console.log('🎯 DRAG MOVE - Position:', { x: event.clientX, y: event.clientY });
      console.log('🎯 DRAG MOVE - Element under mouse:', document.elementFromPoint(event.clientX, event.clientY));
      setDragState(prev => ({
        ...prev,
        currentPosition: { x: event.clientX, y: event.clientY }
      }));
    }
  }, [dragState.isDragging]);

  const handleMouseUp = useCallback((event: MouseEvent) => {
    console.log('🎯 DRAG END - Starting mouse up handler');
    console.log('🎯 DRAG END - isDragging:', dragState.isDragging);
    console.log('🎯 DRAG END - draggedItem:', dragState.draggedItem);
    
    if (!dragState.isDragging || !dragState.draggedItem) {
      console.log('🎯 DRAG END - Early return - no drag in progress');
      return;
    }

    console.log('🎯 DRAG END - Position:', { x: event.clientX, y: event.clientY });
    
    // Check if we're over a drop zone
    const elementUnderMouse = document.elementFromPoint(event.clientX, event.clientY);
    console.log('🎯 DRAG END - Element under mouse:', elementUnderMouse);
    console.log('🎯 DRAG END - Element tag:', elementUnderMouse?.tagName);
    console.log('🎯 DRAG END - Element classes:', elementUnderMouse?.className);
    console.log('🎯 DRAG END - Element data-param:', elementUnderMouse?.getAttribute('data-param'));
    
    const dropZone = elementUnderMouse?.closest('[data-param]');
    console.log('🎯 DRAG END - Drop zone found:', dropZone);
    console.log('🎯 DRAG END - Drop zone data-param:', dropZone?.getAttribute('data-param'));
    
    if (dropZone) {
      const paramName = dropZone.getAttribute('data-param');
      if (paramName) {
        console.log('🎯 DRAG END - ✅ SUCCESS! Dropped on parameter:', paramName);
        const sourceField = dragState.draggedItem;
        console.log('🎯 DRAG END - Source field:', sourceField);

        setFunctionNode(prev => {
          const newNode = {
            ...prev,
            sourceConnections: {
              ...prev.sourceConnections,
              [paramName]: [sourceField.path]
            }
          };
          console.log('🎯 DRAG END - Updated function node:', newNode);
          return newNode;
        });

        // Create visual connection
        setTimeout(() => {
          console.log('🎯 DRAG END - Creating visual connection');
          const sourceElement = document.querySelector(`[data-field-id="${sourceField.id}"]`);
          const targetElement = dropZone;
          
          console.log('🎯 DRAG END - Source element found:', sourceElement);
          console.log('🎯 DRAG END - Target element found:', targetElement);
          console.log('🎯 DRAG END - Canvas ref:', canvasRef.current);
          
          if (sourceElement && targetElement && canvasRef.current) {
            const canvasRect = canvasRef.current.getBoundingClientRect();
            const sourceRect = sourceElement.getBoundingClientRect();
            const targetRect = targetElement.getBoundingClientRect();
            
            console.log('🎯 DRAG END - Canvas rect:', canvasRect);
            console.log('🎯 DRAG END - Source rect:', sourceRect);
            console.log('🎯 DRAG END - Target rect:', targetRect);
            
            const connection: Connection = {
              id: `connection_${Date.now()}`,
              sourceId: sourceField.id,
              targetId: `${functionNode.id}-${paramName}`,
              sourceX: sourceRect.right - canvasRect.left,
              sourceY: sourceRect.top + sourceRect.height / 2 - canvasRect.top,
              targetX: targetRect.left - canvasRect.left,
              targetY: targetRect.top + targetRect.height / 2 - canvasRect.top
            };
            console.log('🎯 DRAG END - Connection created:', connection);
            setConnections(prev => {
              const newConnections = [...prev, connection];
              console.log('🎯 DRAG END - All connections:', newConnections);
              return newConnections;
            });
          } else {
            console.log('🎯 DRAG END - ❌ Failed to create connection - missing elements');
          }
        }, 100);
      } else {
        console.log('🎯 DRAG END - ❌ No param name found on drop zone');
      }
    } else {
      console.log('🎯 DRAG END - ❌ No drop zone found');
    }

    // Reset user selection styles
    document.body.style.userSelect = '';
    document.body.style.webkitUserSelect = '';
    
    console.log('🎯 DRAG END - Resetting drag state');
    setDragState({
      isDragging: false,
      draggedItem: null,
      startPosition: { x: 0, y: 0 },
      currentPosition: { x: 0, y: 0 }
    });
    setDropTargets(new Set());
    console.log('🎯 DRAG END - Complete!');
  }, [dragState, functionNode.id]);

  // Add global mouse event listeners
  React.useEffect(() => {
    console.log('🎯 DRAG STATE CHANGE - isDragging:', dragState.isDragging);
    console.log('🎯 DRAG STATE CHANGE - draggedItem:', dragState.draggedItem?.name);
    
    if (dragState.isDragging) {
      console.log('🎯 DRAG STATE CHANGE - Adding global mouse listeners');
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      return () => {
        console.log('🎯 DRAG STATE CHANGE - Removing global mouse listeners');
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [dragState.isDragging, handleMouseMove, handleMouseUp]);

  const handleFunctionOutputDragStart = useCallback((event: React.DragEvent) => {
    if (!targetField) return;
    event.dataTransfer.setData('text/plain', 'function-output');
    event.dataTransfer.effectAllowed = 'move';

    const targets = new Set<string>();
    targets.add(`target-${targetField.id}`);
    setDropTargets(targets);
  }, [targetField]);

  const handleDragEnd = useCallback(() => {
    setDragState({ 
      isDragging: false, 
      draggedItem: null, 
      startPosition: { x: 0, y: 0 },
      currentPosition: { x: 0, y: 0 }
    });
    setDropTargets(new Set());
  }, []);

  const handleDropOnFunctionParameter = useCallback((paramName: string, event: React.DragEvent) => {
    console.log('=== DROP EVENT ===');
    console.log('Parameter:', paramName);
    console.log('Event type:', event.type);
    
    event.preventDefault();
    event.stopPropagation();
    
    // Try multiple data formats
    let fieldData = event.dataTransfer.getData('application/json');
    const fieldName = event.dataTransfer.getData('text/plain');
    const fieldId = event.dataTransfer.getData('text/field-id');
    
    console.log('Drag data - JSON:', fieldData);
    console.log('Drag data - Name:', fieldName);
    console.log('Drag data - ID:', fieldId);
    
    if (!fieldData && fieldName) {
      // Fallback: find field by name
      const sourceField = sourceFields.find(f => f.name === fieldName);
      if (sourceField) {
        fieldData = JSON.stringify(sourceField);
      }
    }
    
    if (!fieldData) {
      console.log('ERROR: No field data found in drag transfer');
      return;
    }
    
    const sourceField = JSON.parse(fieldData) as FieldNode;
    console.log('Parsed source field:', sourceField);

    setFunctionNode(prev => ({
      ...prev,
      sourceConnections: {
        ...prev.sourceConnections,
        [paramName]: [sourceField.path] // Replace instead of append
      }
    }));

    // Create visual connection
    setTimeout(() => {
      const sourceElement = document.querySelector(`[data-field-id="${sourceField.id}"]`);
      const targetElement = document.querySelector(`[data-param="${paramName}"]`);
      
      if (sourceElement && targetElement && canvasRef.current) {
        const canvasRect = canvasRef.current.getBoundingClientRect();
        const sourceRect = sourceElement.getBoundingClientRect();
        const targetRect = targetElement.getBoundingClientRect();
        
        const connection: Connection = {
          id: `connection_${Date.now()}`,
          sourceId: sourceField.id,
          targetId: `${functionNode.id}-${paramName}`,
          sourceX: sourceRect.right - canvasRect.left,
          sourceY: sourceRect.top + sourceRect.height / 2 - canvasRect.top,
          targetX: targetRect.left - canvasRect.left,
          targetY: targetRect.top + targetRect.height / 2 - canvasRect.top
        };
        setConnections(prev => [...prev, connection]);
        console.log('Connection created:', connection);
      }
    }, 100);

    handleDragEnd();
    console.log('=== DROP COMPLETE ===');
  }, [functionNode.id, handleDragEnd, sourceFields]);

  const handleDropOnTarget = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const data = event.dataTransfer.getData('text/plain');
    if (data !== 'function-output' || !targetField) return;
    
    setOutputConnected(true);

    // Create visual connection with better positioning
    const outputElement = document.querySelector('[data-function-output="true"]');
    const targetElement = document.querySelector(`[data-target-field="${targetField.id}"]`);
    
    if (outputElement && targetElement && canvasRef.current) {
      const canvasRect = canvasRef.current.getBoundingClientRect();
      const outputRect = outputElement.getBoundingClientRect();
      const targetRect = targetElement.getBoundingClientRect();
      
      const connection: Connection = {
        id: `connection_output_${Date.now()}`,
        sourceId: `${functionNode.id}-output`,
        targetId: targetField.id,
        sourceX: outputRect.right - canvasRect.left,
        sourceY: outputRect.top + outputRect.height / 2 - canvasRect.top,
        targetX: targetRect.left - canvasRect.left,
        targetY: targetRect.top + targetRect.height / 2 - canvasRect.top
      };
      setConnections(prev => [...prev, connection]);
    }
  }, [functionNode.id, targetField]);

  const handleApplyFunction = useCallback(() => {
    if (!func || !outputConnected || !targetField) return;

    const connectedSourceFields: string[] = [];
    const connectedSourcePaths: string[] = [];

    Object.values(functionNode.sourceConnections).forEach(paths => {
      paths.forEach(path => {
        connectedSourcePaths.push(path);
        connectedSourceFields.push(path.split('.').pop() || path);
      });
    });

    const newMapping: FieldMapping = {
      id: `mapping_${Date.now()}`,
      name: `${selectedFunction}_to_${targetField.name}`,
      sourceFields: connectedSourceFields,
      targetField: targetField.name,
      sourcePaths: connectedSourcePaths,
      targetPath: targetField.path,
      functionNode: {
        ...functionNode,
        id: `function_${Date.now()}`
      },
      requiresTransformation: true
    };

    onApplyMapping(newMapping);
    onClose();
  }, [func, outputConnected, functionNode, selectedFunction, targetField, onApplyMapping, onClose]);

  if (!func) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-6xl h-[80vh] p-0">
        <DialogHeader className="p-6 pb-0">
          <DialogTitle className="flex items-center justify-between">
            <span>Function Mapping: {func.name}</span>
            <div className="flex items-center gap-2">
              <Button 
                onClick={handleApplyFunction}
                disabled={!outputConnected || Object.keys(functionNode.sourceConnections).length === 0}
                className="bg-primary text-primary-foreground"
              >
                <Check className="h-4 w-4 mr-2" />
                Apply Function
              </Button>
              <Button variant="outline" onClick={onClose} className="text-muted-foreground hover:text-foreground">
                <X className="h-4 w-4 mr-2" />
                Cancel
              </Button>
            </div>
          </DialogTitle>
        </DialogHeader>

        <div ref={canvasRef} className="relative w-full flex-1 bg-background/50 overflow-hidden">
          <svg ref={svgRef} className="absolute inset-0 w-full h-full pointer-events-none z-10" style={{ overflow: 'visible' }}>
            <defs>
              <marker id="arrowhead-modal" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto" markerUnits="strokeWidth">
                <polygon points="0 0, 10 3.5, 0 7" fill="hsl(var(--primary))" />
              </marker>
            </defs>
            {connections.map(connection => (
              <path
                key={connection.id}
                d={`M ${connection.sourceX} ${connection.sourceY} C ${connection.sourceX + 100} ${connection.sourceY}, ${connection.targetX - 100} ${connection.targetY}, ${connection.targetX} ${connection.targetY}`}
                stroke="hsl(var(--primary))"
                strokeWidth="2"
                fill="none"
                markerEnd="url(#arrowhead-modal)"
              />
            ))}
          </svg>

          <div className="flex justify-between items-start px-4 pt-4 space-x-4">
            {/* Source Fields */}
            <div className="w-72 h-60 overflow-y-auto">
              <div className="bg-card border rounded-lg p-2 shadow-sm h-full">
                <h3 className="font-semibold text-sm mb-2 text-primary">Source Fields</h3>
                <div className="space-y-2">
                  {sourceFields.map(field => (
                    <div 
                      key={field.id} 
                      data-field-id={field.id}
                      className={cn(
                        "bg-background border-2 rounded p-3 transition-all hover:shadow-lg hover:border-primary/50 select-none",
                        dragState.isDragging && dragState.draggedItem?.id === field.id && "opacity-70 scale-95"
                      )}
                      style={{ 
                        cursor: dragState.isDragging && dragState.draggedItem?.id === field.id ? 'grabbing' : 'grab'
                      }}
                      onMouseDown={(e) => {
                        e.preventDefault();
                        handleMouseDown(field, e);
                      }}
                    >
                      <div className="font-medium text-sm pointer-events-none">{field.name}</div>
                      <div className="text-xs text-muted-foreground pointer-events-none">{field.type}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Function Node */}
            <div className="bg-card border-2 border-primary/20 rounded-lg shadow-lg overflow-visible w-60" style={{ zIndex: 5 }}>
              <div className="bg-primary text-primary-foreground p-2 rounded-t-lg">
                <div className="font-medium text-sm">{func.name}</div>
                <div className="text-xs opacity-80">{func.description}</div>
              </div>
              <div className="p-2 space-y-2">
                {func.parameters.map(param => (
                  <div key={param.name}>
                    <div className="font-medium text-xs">{param.name}</div>
                    <div className="text-xs text-muted-foreground mb-1">{param.type}</div>
                    
                    {/* For delimiter or similar text parameters, show input field */}
                    {(param.name.toLowerCase().includes('delimiter') || param.name.toLowerCase().includes('separator')) ? (
                      <Input
                        placeholder={`Enter ${param.name}`}
                        value={functionNode.parameters[param.name] || ''}
                        onChange={(e) => setFunctionNode(prev => ({
                          ...prev,
                          parameters: { ...prev.parameters, [param.name]: e.target.value }
                        }))}
                        className="h-8 text-xs"
                      />
                    ) : (
                      <div 
                        data-param={param.name}
                        className={cn(
                          "border-2 border-dashed border-muted-foreground/30 rounded p-2 text-sm transition-colors min-h-10", 
                          dropTargets.has(`param-${functionNode.id}-${param.name}`) && "border-primary bg-primary/10",
                          "hover:border-primary/50"
                        )}
                      >
                        {functionNode.sourceConnections[param.name]?.map((path, idx) => (
                          <div key={idx} className="text-xs bg-primary/10 text-primary rounded px-1 py-0.5 mt-1 inline-block mr-1">
                            {path.split('.').pop()}
                          </div>
                        ))}
                        {!functionNode.sourceConnections[param.name]?.length && (
                          <div className="text-xs text-muted-foreground italic">Drop here</div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
              <div 
                data-function-output="true"
                className="bg-success/10 border-t border-success/20 p-2 text-center cursor-grab hover:bg-success/20 transition-colors"
                draggable="true"
                onDragStart={handleFunctionOutputDragStart}
                onDragEnd={handleDragEnd}
              >
                <div className="text-xs font-medium text-success">Output</div>
                <div className="text-xs text-success/80">Drag to target</div>
              </div>
            </div>

            {/* Target Field */}
            {targetField && (
              <div className="w-72">
                <div className="bg-card border rounded-lg p-2 shadow-sm">
                  <h3 className="font-semibold text-sm mb-2 text-primary">Target Field</h3>
                  <div 
                    data-target-field={targetField.id}
                    className={cn("bg-background border rounded p-3 transition-all", dropTargets.has(`target-${targetField.id}`) && "border-primary bg-primary/10", outputConnected && "border-success bg-success/10")}
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={handleDropOnTarget}
                  >
                    <div className="font-medium text-sm">{targetField.name}</div>
                    <div className="text-sm text-muted-foreground">{targetField.type}</div>
                    {outputConnected && <div className="text-xs text-success mt-2 font-medium">✓ Function output connected</div>}
                  </div>
                </div>
              </div>
            )}
          </div>


        </div>

        <div className="border-t bg-muted/30 py-1 px-2 text-xs">
          <div className="flex justify-between items-center">
            <div className="text-muted-foreground">Connect source → function → target</div>
            <div className="flex items-center gap-2">
              <span className={cn("px-1.5 py-0.5 rounded", Object.keys(functionNode.sourceConnections).length > 0 ? "bg-success/20 text-success" : "bg-muted text-muted-foreground")}>Params: {Object.keys(functionNode.sourceConnections).length}/{func.parameters.length}</span>
              <span className={cn("px-1.5 py-0.5 rounded", outputConnected ? "bg-success/20 text-success" : "bg-muted text-muted-foreground")}>Output: {outputConnected ? 'Connected' : 'Not connected'}</span>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
