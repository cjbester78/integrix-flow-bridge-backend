import React, { useState, useCallback, useMemo } from 'react';
import {
  ReactFlow,
  addEdge,
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  Connection,
  Edge,
  Node,
  MarkerType,
  BackgroundVariant,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Button } from '@/components/ui/button';
import { Trash2 } from 'lucide-react';

// Import our custom node types and components
import { AdapterNode } from './nodes/AdapterNode';
import { StartProcessNode } from './nodes/StartProcessNode';
import { TransformationNode } from './nodes/TransformationNode';
import { RoutingNode } from './nodes/RoutingNode';
import { OrchestrationNodePalette } from './OrchestrationNodePalette';
import { OrchestrationPropertiesPanel } from './OrchestrationPropertiesPanel';

// Define comprehensive node types for all BPMN 2.0 orchestration components
const nodeTypes = {
  // BPMN Events
  'start-process': StartProcessNode,
  'end-process': AdapterNode,
  'start-event': AdapterNode,
  'end-event': AdapterNode,
  'intermediate-event': AdapterNode,
  'timer-event': AdapterNode,
  'message-event': AdapterNode,
  'error-event': AdapterNode,
  
  // BPMN Activities
  'task': TransformationNode,
  'service-task': TransformationNode,
  'user-task': TransformationNode,
  'script-task': TransformationNode,
  'manual-task': TransformationNode,
  'business-rule-task': TransformationNode,
  'receive-task': TransformationNode,
  'send-task': TransformationNode,
  
  // BPMN Gateways
  'exclusive-gateway': RoutingNode,
  'parallel-gateway': RoutingNode,
  'inclusive-gateway': RoutingNode,
  'event-gateway': RoutingNode,
  'complex-gateway': RoutingNode,
  
  // Mapping & Transformation
  'field-mapping': TransformationNode,
  'data-transformation': TransformationNode,
  'format-conversion': TransformationNode,
  'value-mapping': TransformationNode,
  'structure-mapping': TransformationNode,
  'conditional-mapping': TransformationNode,
  'aggregation': TransformationNode,
  'split-mapping': TransformationNode,
  
  // File Transformations
  'csv-to-xml': TransformationNode,
  'json-to-xml': TransformationNode,
  'xml-to-json': TransformationNode,
  'excel-to-csv': TransformationNode,
  'pdf-extractor': TransformationNode,
  'file-splitter': TransformationNode,
  'file-merger': TransformationNode,
  'file-validator': TransformationNode,
  
  // Scripts & Functions
  'javascript-script': TransformationNode,
  'groovy-script': TransformationNode,
  'xslt-transformation': TransformationNode,
  'custom-function': TransformationNode,
  'lookup-function': TransformationNode,
  'calculation-function': TransformationNode,
  'validation-function': TransformationNode,
  'enrichment-function': TransformationNode,
  
  // Flow Routing
  'content-router': RoutingNode,
  'recipient-list': RoutingNode,
  'message-filter': RoutingNode,
  'dynamic-router': RoutingNode,
  'load-balancer': RoutingNode,
  'failover-router': RoutingNode,
  'multicast': RoutingNode,
  'wire-tap': RoutingNode,
  
  // Security
  'encryption': TransformationNode,
  'decryption': TransformationNode,
  'digital-signature': TransformationNode,
  'authentication': TransformationNode,
  'authorization': TransformationNode,
  'certificate-validation': TransformationNode,
  'secure-transport': TransformationNode,
  'access-control': TransformationNode,
  
  // Adapters & Connectors
  'http-adapter': AdapterNode,
  'ftp-adapter': AdapterNode,
  'database-adapter': AdapterNode,
  'mail-adapter': AdapterNode,
  'file-adapter': AdapterNode,
  'jms-adapter': AdapterNode,
  'soap-adapter': AdapterNode,
  'rest-adapter': AdapterNode,
};
// Initial empty state for new orchestration flow
const initialNodes: Node[] = [];
const initialEdges: Edge[] = [];

export function VisualOrchestrationEditor() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [selectedNode, setSelectedNode] = useState<Node | null>(null);
  const [nodeWithDeleteButton, setNodeWithDeleteButton] = useState<string | null>(null);
  
  // Track selected nodes for deletion
  const selectedNodes = useMemo(() => {
    return nodes.filter(node => node.selected).map(node => node.id);
  }, [nodes]);

  const onConnect = useCallback(
    (params: Connection) => {
      const edge = {
        ...params,
        type: 'smoothstep',
        animated: true,
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
    },
    [setEdges]
  );

  const onNodeClick = useCallback((event: React.MouseEvent, node: Node) => {
    console.log('[VisualOrchestrationEditor] Node clicked:', { nodeId: node.id, nodeType: node.type });
    setSelectedNode(node);
    setNodeWithDeleteButton(node.id);
    // Hide delete button after 3 seconds
    setTimeout(() => setNodeWithDeleteButton(null), 3000);
  }, []);

  // Function to add a new node based on type and category
  const addNode = useCallback((type: string, category: string) => {
    console.log('[VisualOrchestrationEditor] Adding new node:', { type, category });
    
    const nodeId = `${type}-${Date.now()}`;
    const newNode: Node = {
      id: nodeId,
      type: type,
      position: { 
        x: Math.random() * 400 + 200, 
        y: Math.random() * 300 + 150 
      },
      data: { 
        adapterType: type,
        transformationType: type,
        routingType: type,
        configured: false,
        showDeleteButton: false,
        onConfigChange: (config: any) => {
          console.log('[VisualOrchestrationEditor] Node config changed:', { nodeId, config });
          setNodes((nds) =>
            nds.map((node) => (node.id === nodeId ? { ...node, data: { ...node.data, ...config, configured: true } } : node))
          );
        }
      },
    };
    
    console.log('[VisualOrchestrationEditor] Created new node:', newNode);
    setNodes((nds) => {
      const updatedNodes = nds.concat(newNode);
      console.log('[VisualOrchestrationEditor] Updated nodes list:', updatedNodes);
      return updatedNodes;
    });
  }, [setNodes]);

  // Function to delete selected nodes
  const deleteSelectedNodes = useCallback(() => {
    setNodes((nds) => nds.filter((node) => !node.selected));
    setEdges((eds) => eds.filter((edge) => 
      !selectedNodes.includes(edge.source) && !selectedNodes.includes(edge.target)
    ));
  }, [selectedNodes, setNodes, setEdges]);

  // Calculate flow statistics
  const flowStats = useMemo(() => {
    const totalNodes = nodes.length;
    const totalConnections = edges.length;
    
    // Simple complexity calculation based on node count and connections
    let complexity: 'Low' | 'Medium' | 'High' = 'Low';
    if (totalNodes > 10 || totalConnections > 15) {
      complexity = 'High';
    } else if (totalNodes > 5 || totalConnections > 8) {
      complexity = 'Medium';
    }
    
    // Estimate execution time based on node types and connections
    const estimatedMs = totalNodes * 100 + totalConnections * 50; // rough estimate
    const estimatedExecutionTime = estimatedMs < 1000 ? `${estimatedMs}ms` : `${(estimatedMs / 1000).toFixed(1)}s`;
    
    return {
      totalNodes,
      totalConnections,
      estimatedExecutionTime,
      complexity
    };
  }, [nodes, edges]);

  return (
    <div className="h-full w-full flex">
      {/* Left Sidebar - Node Palette */}
      <div className="flex-shrink-0">
        <OrchestrationNodePalette onAddNode={addNode} />
      </div>

      {/* Main Canvas Area */}
      <div className="flex-1 relative">
        <ReactFlow
          nodes={nodes.map(node => ({
            ...node,
            data: {
              ...node.data,
              showDeleteButton: nodeWithDeleteButton === node.id
            }
          }))}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onNodeClick={onNodeClick}
          nodeTypes={nodeTypes}
          fitView
          deleteKeyCode="Delete"
          multiSelectionKeyCode="Control"
          className="bg-background"
        >
          <Controls showInteractive={false} />
          <MiniMap />
          <Background variant={BackgroundVariant.Dots} gap={12} size={1} />
        </ReactFlow>
      </div>

      {/* Right Sidebar - Properties and Status */}
      <div className="flex-shrink-0">
        <OrchestrationPropertiesPanel 
          selectedNode={selectedNode} 
          flowStats={flowStats}
        />
      </div>
    </div>
  );
}