import React, { useState, useCallback, useMemo } from 'react';
import {
  ReactFlow,
  Node,
  Edge,
  addEdge,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
  Connection,
  ConnectionMode,
  MiniMap,
  Panel,
  MarkerType,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { FieldNode, FieldMapping } from './types';
import { Plus, Save, X, Zap, Hash, GitBranch, Code } from 'lucide-react';
import { SourceFieldNode } from './nodes/SourceFieldNode';
import { FunctionNode } from './nodes/FunctionNode';
import { ConstantNode } from './nodes/ConstantNode';
import { TargetFieldNode } from './nodes/TargetFieldNode';
import { ConditionalNode } from './nodes/ConditionalNode';
import { FieldSelectorDialog } from './FieldSelectorDialog';
import { FunctionSelectorDialog } from './FunctionSelectorDialog';
import { DeletableEdge } from './DeletableEdge';
import { functionsByCategory, TransformationFunction } from '@/services/transformationFunctions';

interface VisualFlowEditorProps {
  open: boolean;
  onClose: () => void;
  sourceFields: FieldNode[];
  targetField: FieldNode | null;
  onApplyMapping: (mapping: FieldMapping) => void;
  initialMapping?: FieldMapping;
}

const nodeTypes = {
  sourceField: SourceFieldNode,
  function: FunctionNode,
  constant: ConstantNode,
  targetField: TargetFieldNode,
  conditional: ConditionalNode,
};

const edgeTypes = {
  deletable: DeletableEdge,
};

export const VisualFlowEditor: React.FC<VisualFlowEditorProps> = ({
  open,
  onClose,
  sourceFields,
  targetField,
  onApplyMapping,
  initialMapping
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [nodeIdCounter, setNodeIdCounter] = useState(1);
  const [showFieldSelector, setShowFieldSelector] = useState(false);
  const [showFunctionSelector, setShowFunctionSelector] = useState(false);

  // Initialize nodes when dialog opens or when initialMapping changes
  React.useEffect(() => {
    if (open && targetField) {
      let initialNodes: Node[] = [];
      let initialEdges: Edge[] = [];
      let initialNodeCounter = 1;
      
      // Check if we have saved visual flow data to restore
      if (initialMapping?.visualFlowData) {
        // Restore the complete flow state
        initialNodes = initialMapping.visualFlowData.nodes;
        initialEdges = initialMapping.visualFlowData.edges;
        initialNodeCounter = initialMapping.visualFlowData.nodeIdCounter;
      } else {
        // Create new flow
        // Add target field node
        initialNodes.push({
          id: 'target',
          type: 'targetField',
          position: { x: 800, y: 200 },
          data: { field: targetField },
        });

        // If there's an initial mapping, add its source fields to the canvas
        if (initialMapping && initialMapping.sourcePaths && initialMapping.sourcePaths.length > 0) {
          initialMapping.sourcePaths.forEach((sourceFieldPath, index) => {
            // Find the corresponding FieldNode for this source field path
            const sourceField = sourceFields.find(field => 
              field.path === sourceFieldPath || field.name === sourceFieldPath
            );
            
            if (sourceField) {
              const sourceNodeId = `source-${sourceField.id}`;
              initialNodes.push({
                id: sourceNodeId,
                type: 'sourceField',
                position: { x: 50, y: 50 + index * 100 },
                data: { field: sourceField },
              });

              // Create edge connecting source to target
              initialEdges.push({
                id: `edge-${sourceNodeId}-target`,
                source: sourceNodeId,
                target: 'target',
                type: 'deletable',
                animated: false,
                style: { 
                  stroke: 'hsl(var(--primary))', 
                  strokeWidth: 2 
                },
                markerEnd: {
                  type: MarkerType.ArrowClosed,
                  color: 'hsl(var(--primary))'
                }
              });
            }
          });
        }
        // Fallback: try using sourceFields if sourcePaths is not available
        else if (initialMapping && initialMapping.sourceFields && initialMapping.sourceFields.length > 0) {
          initialMapping.sourceFields.forEach((sourceFieldName, index) => {
            const sourceField = sourceFields.find(field => 
              field.name === sourceFieldName || field.path === sourceFieldName
            );
            
            if (sourceField) {
              const sourceNodeId = `source-${sourceField.id}`;
              initialNodes.push({
                id: sourceNodeId,
                type: 'sourceField',
                position: { x: 50, y: 50 + index * 100 },
                data: { field: sourceField },
              });

              // Create edge connecting source to target
              initialEdges.push({
                id: `edge-${sourceNodeId}-target`,
                source: sourceNodeId,
                target: 'target',
                type: 'deletable',
                animated: false,
                style: { 
                  stroke: 'hsl(var(--primary))', 
                  strokeWidth: 2 
                },
                markerEnd: {
                  type: MarkerType.ArrowClosed,
                  color: 'hsl(var(--primary))'
                }
              });
            }
          });
        }
        
        // Set node counter based on how many nodes we're starting with  
        const sourceNodeCount = (initialMapping?.sourcePaths?.length || initialMapping?.sourceFields?.length || 0);
        initialNodeCounter = 2 + sourceNodeCount;
      }

      setNodes(initialNodes);
      setEdges(initialEdges);
      setNodeIdCounter(initialNodeCounter);
    }
  }, [open, targetField, sourceFields, initialMapping, setNodes, setEdges]);

  const onConnect = useCallback(
    (params: Connection) => {
      // If connecting to target field, remove any existing connections to target
      if (params.target === 'target') {
        setEdges((eds) => {
          // Remove existing edges to target
          const filteredEdges = eds.filter(edge => edge.target !== 'target');
          
          // Add the new edge
          const edge = {
            ...params,
            type: 'deletable',
            animated: false,
            style: { 
              stroke: 'hsl(var(--primary))', 
              strokeWidth: 2 
            },
            markerEnd: {
              type: MarkerType.ArrowClosed,
              color: 'hsl(var(--primary))'
            }
          };
          
          return addEdge(edge, filteredEdges);
        });
      } else {
        // For non-target connections, just add normally
        const edge = {
          ...params,
          type: 'deletable',
          animated: false,
          style: { 
            stroke: 'hsl(var(--primary))', 
            strokeWidth: 2 
          },
          markerEnd: {
            type: MarkerType.ArrowClosed,
            color: 'hsl(var(--primary))'
          }
        };
        setEdges((eds) => addEdge(edge, eds));
      }
    },
    [setEdges]
  );

  const addSourceField = useCallback(() => {
    setShowFieldSelector(true);
  }, []);

  const handleSelectSourceField = useCallback((field: FieldNode) => {
    const existingSourceNodes = nodes.filter(node => node.type === 'sourceField');
    const newNode: Node = {
      id: `source-${field.id}`,
      type: 'sourceField',
      position: { x: 50, y: 50 + existingSourceNodes.length * 100 }, // Consistent spacing
      data: { field },
    };

    setNodes((nds) => [...nds, newNode]);
  }, [nodes, setNodes]);

  const addFunction = useCallback(() => {
    setShowFunctionSelector(true);
  }, []);

  const handleSelectFunction = useCallback((func: TransformationFunction) => {
    const existingFunctionNodes = nodes.filter(node => node.type === 'function');
    const newNode: Node = {
      id: `function-${nodeIdCounter}`,
      type: 'function',
      position: { 
        x: 350, 
        y: 50 + existingFunctionNodes.length * 120 // Start at top, space functions vertically
      },
      data: { 
        function: func,
        parameters: {},
        showSelector: false
      },
    };

    setNodes((nds) => [...nds, newNode]);
    setNodeIdCounter(prev => prev + 1);
  }, [nodeIdCounter, nodes, setNodes]);

  const addCustomFunction = useCallback(() => {
    const existingCustomNodes = nodes.filter(node => node.type === 'function' && node.data.function.name === 'custom');
    const newNode: Node = {
      id: `custom-function-${nodeIdCounter}`,
      type: 'function',
      position: { 
        x: 350, 
        y: 50 + existingCustomNodes.length * 120
      },
      data: { 
        function: {
          name: 'custom',
          description: 'Custom Java function',
          category: 'custom',
          parameters: [],
          javaCode: ''
        },
        parameters: {},
        showSelector: false,
        isCustom: true
      },
    };

    setNodes((nds) => [...nds, newNode]);
    setNodeIdCounter(prev => prev + 1);
  }, [nodeIdCounter, nodes, setNodes]);

  const handleSave = useCallback(() => {
    if (!targetField) return;

    console.log('🔥 VisualFlowEditor handleSave - saving with nodes:', nodes.length, 'edges:', edges.length);

    // Find the target node and trace back all connections
    const targetNode = nodes.find(n => n.type === 'targetField');
    if (!targetNode) return;

    // Get all source field paths connected to the flow
    const connectedSourcePaths: string[] = [];
    const connectedSourceFields: string[] = [];

    nodes.forEach(node => {
      if (node.type === 'sourceField') {
        const hasConnections = edges.some(edge => edge.source === node.id);
        if (hasConnections) {
          connectedSourcePaths.push(node.data.field.path);
          connectedSourceFields.push(node.data.field.name);
        }
      }
    });

    console.log('🔥 VisualFlowEditor handleSave - connectedSourcePaths:', connectedSourcePaths);
    console.log('🔥 VisualFlowEditor handleSave - connectedSourceFields:', connectedSourceFields);

    // Create mapping object with complete flow state
    const mapping: FieldMapping = {
      id: initialMapping?.id || `mapping_${Date.now()}`,
      name: initialMapping?.name || `visual_flow_to_${targetField.name}`,
      sourceFields: connectedSourceFields,
      targetField: targetField.name,
      sourcePaths: connectedSourcePaths,
      targetPath: targetField.path,
      requiresTransformation: true,
      // Store the complete flow configuration for future editing
      visualFlowData: {
        nodes: nodes,
        edges: edges,
        nodeIdCounter: nodeIdCounter
      },
      functionNode: {
        id: 'visual_flow',
        functionName: 'visual_flow',
        parameters: {},
        sourceConnections: {},
        position: { x: 0, y: 0 }
      }
    };

    console.log('🔥 VisualFlowEditor handleSave - final mapping:', mapping);

    onApplyMapping(mapping);
    onClose();
  }, [nodes, edges, nodeIdCounter, targetField, initialMapping, onApplyMapping, onClose]);

  const isFlowValid = useMemo(() => {
    const targetNode = nodes.find(n => n.type === 'targetField');
    if (!targetNode) return false;
    
    // Check if target node has incoming connections
    const hasTargetConnection = edges.some(edge => edge.target === targetNode.id);
    return hasTargetConnection;
  }, [nodes, edges]);

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-[98vw] h-[95vh] p-0 max-h-[95vh]">
        <DialogHeader className="p-4 pb-2 border-b bg-muted/30">
          <DialogTitle className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-lg font-semibold">Visual Flow Editor</span>
              <div className="text-sm text-muted-foreground">
                Target: <span className="font-medium text-primary">{targetField.name}</span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Button 
                onClick={handleSave}
                disabled={!isFlowValid}
                className="bg-primary text-primary-foreground"
              >
                <Save className="h-4 w-4 mr-2" />
                Save Flow
              </Button>
              <Button variant="outline" onClick={onClose}>
                <X className="h-4 w-4 mr-2" />
                Cancel
              </Button>
            </div>
          </DialogTitle>
        </DialogHeader>

        <div className="flex-1 relative h-[calc(95vh-80px)]">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            connectionMode={ConnectionMode.Loose}
            fitView
            fitViewOptions={{
              padding: 0.1,
              minZoom: 0.5,
              maxZoom: 2
            }}
            defaultEdgeOptions={{
              type: 'deletable',
              animated: false,
              style: { 
                stroke: 'hsl(var(--primary))', 
                strokeWidth: 2 
              },
              markerEnd: {
                type: MarkerType.ArrowClosed,
                color: 'hsl(var(--primary))'
              }
            }}
            className="bg-background"
          >
            <Background gap={20} size={1} />
            <Controls />
            <MiniMap 
              className="bg-background border border-border" 
              pannable 
              zoomable 
              nodeColor="hsl(var(--primary))"
            />
            
            <Panel position="top-left" className="bg-card border rounded-lg p-3 shadow-lg min-w-[200px]">
              <div className="flex flex-col gap-2">
                <h3 className="text-sm font-semibold text-primary mb-2 border-b border-border pb-2">Add Nodes</h3>
                <Button
                  onClick={addSourceField}
                  size="sm"
                  variant="outline"
                  className="justify-start h-9"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Source Field
                </Button>
                <Button
                  onClick={addFunction}
                  size="sm"
                  variant="outline"
                  className="justify-start h-9"
                >
                  <Zap className="h-4 w-4 mr-2" />
                  Function
                </Button>
                <Button
                  onClick={addCustomFunction}
                  size="sm"
                  variant="outline"
                  className="justify-start h-9"
                >
                  <Code className="h-4 w-4 mr-2" />
                  Custom Function
                </Button>
              </div>
            </Panel>

            <Panel position="bottom-right" className="bg-card border rounded-lg p-3 shadow-lg">
              <div className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${isFlowValid ? 'bg-green-500' : 'bg-red-500'}`} />
                <div className="text-sm font-medium">
                  {isFlowValid ? 'Flow is valid - ready to save' : 'Connect nodes to target field'}
                </div>
              </div>
            </Panel>
          </ReactFlow>
        </div>

        <FieldSelectorDialog
          open={showFieldSelector}
          onClose={() => setShowFieldSelector(false)}
          sourceFields={sourceFields}
          onSelectField={handleSelectSourceField}
          excludeFields={nodes
            .filter(node => node.type === 'sourceField')
            .map(node => node.data.field.id)
          }
        />

        <FunctionSelectorDialog
          open={showFunctionSelector}
          onClose={() => setShowFunctionSelector(false)}
          onSelectFunction={handleSelectFunction}
        />
      </DialogContent>
    </Dialog>
  );
};