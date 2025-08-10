import React, { useState, useCallback, useEffect } from 'react';
import {
  ReactFlow,
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
  Connection,
  Edge,
  Node,
  MarkerType,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { FlowExecution, FlowDefinition } from '@/types/flow';
import { flowExecutionEngine } from '@/services/flowExecutionEngine';
import { Play, Pause, Square, RefreshCw, CheckCircle, XCircle, Clock, AlertTriangle } from 'lucide-react';

interface FlowExecutionVisualizerProps {
  flowDefinition: FlowDefinition;
  onExecutionStart?: (executionId: string) => void;
  onExecutionComplete?: (execution: FlowExecution) => void;
}

const nodeTypes = {
  adapter: AdapterNode,
  transformation: TransformationNode,
  condition: ConditionNode,
  loop: LoopNode,
  delay: DelayNode,
};

export const FlowExecutionVisualizer: React.FC<FlowExecutionVisualizerProps> = ({
  flowDefinition,
  onExecutionStart,
  onExecutionComplete
}) => {
  const [currentExecution, setCurrentExecution] = useState<FlowExecution | null>(null);
  const [isExecuting, setIsExecuting] = useState(false);
  const [executionProgress, setExecutionProgress] = useState(0);

  // Convert flow steps to React Flow nodes
  const initialNodes: Node[] = flowDefinition.steps.map((step, index) => ({
    id: step.id,
    type: step.type,
    position: step.position || { x: index * 200, y: 100 },
    data: {
      ...step,
      status: 'pending',
      execution: null
    }
  }));

  // Create edges based on step connections
  const initialEdges: Edge[] = flowDefinition.steps.flatMap(step =>
    (step.connections || []).map(targetId => ({
      id: `${step.id}-${targetId}`,
      source: step.id,
      target: targetId,
      type: 'smoothstep',
      markerEnd: {
        type: MarkerType.ArrowClosed,
      },
    }))
  );

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  // Execute flow
  const handleExecuteFlow = async () => {
    if (isExecuting) return;

    setIsExecuting(true);
    setExecutionProgress(0);

    try {
      const execution = await flowExecutionEngine.executeFlow(
        flowDefinition,
        'manual',
        {},
        'user'
      );

      setCurrentExecution(execution);
      onExecutionStart?.(execution.id);

      // Subscribe to execution updates
      flowExecutionEngine.subscribeToExecution(execution.id, (updatedExecution) => {
        setCurrentExecution(updatedExecution);
        updateNodesWithExecution(updatedExecution);
        
        const progress = (updatedExecution.metrics.completedSteps / updatedExecution.metrics.totalSteps) * 100;
        setExecutionProgress(progress);

        if (['completed', 'failed', 'cancelled'].includes(updatedExecution.status)) {
          setIsExecuting(false);
          onExecutionComplete?.(updatedExecution);
        }
      });

    } catch (error) {
      setIsExecuting(false);
      console.error('Failed to execute flow:', error);
    }
  };

  // Update nodes with execution status
  const updateNodesWithExecution = (execution: FlowExecution) => {
    setNodes(currentNodes =>
      currentNodes.map(node => {
        const stepExecution = execution.steps.find(s => s.stepId === node.id);
        return {
          ...node,
          data: {
            ...node.data,
            status: stepExecution?.status || 'pending',
            execution: stepExecution
          }
        };
      })
    );
  };

  // Pause execution
  const handlePauseExecution = () => {
    if (currentExecution) {
      flowExecutionEngine.pauseExecution(currentExecution.id);
    }
  };

  // Resume execution
  const handleResumeExecution = () => {
    if (currentExecution) {
      flowExecutionEngine.resumeExecution(currentExecution.id);
    }
  };

  // Cancel execution
  const handleCancelExecution = () => {
    if (currentExecution) {
      flowExecutionEngine.cancelExecution(currentExecution.id);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'running':
        return <RefreshCw className="h-4 w-4 text-blue-500 animate-spin" />;
      case 'paused':
        return <Pause className="h-4 w-4 text-yellow-500" />;
      case 'cancelled':
        return <Square className="h-4 w-4 text-gray-500" />;
      default:
        return <Clock className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'default';
      case 'failed':
        return 'destructive';
      case 'running':
        return 'default';
      case 'paused':
        return 'secondary';
      default:
        return 'outline';
    }
  };

  return (
    <div className="space-y-4">
      {/* Execution Controls */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              Flow Execution: {flowDefinition.name}
              {currentExecution && (
                <Badge variant={getStatusColor(currentExecution.status)}>
                  {getStatusIcon(currentExecution.status)}
                  {currentExecution.status}
                </Badge>
              )}
            </CardTitle>
            <div className="flex gap-2">
              <Button
                onClick={handleExecuteFlow}
                disabled={isExecuting}
                className="flex items-center gap-2"
              >
                <Play className="h-4 w-4" />
                Execute
              </Button>
              {currentExecution?.status === 'running' && (
                <Button
                  variant="outline"
                  onClick={handlePauseExecution}
                  className="flex items-center gap-2"
                >
                  <Pause className="h-4 w-4" />
                  Pause
                </Button>
              )}
              {currentExecution?.status === 'paused' && (
                <Button
                  variant="outline"
                  onClick={handleResumeExecution}
                  className="flex items-center gap-2"
                >
                  <Play className="h-4 w-4" />
                  Resume
                </Button>
              )}
              {['running', 'paused'].includes(currentExecution?.status || '') && (
                <Button
                  variant="destructive"
                  onClick={handleCancelExecution}
                  className="flex items-center gap-2"
                >
                  <Square className="h-4 w-4" />
                  Cancel
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isExecuting && (
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Execution Progress</span>
                <span>{Math.round(executionProgress)}%</span>
              </div>
              <Progress value={executionProgress} className="h-2" />
            </div>
          )}

          {currentExecution && (
            <div className="mt-4 grid grid-cols-4 gap-4 text-sm">
              <div>
                <span className="text-muted-foreground">Total Steps:</span>
                <div className="font-medium">{currentExecution.metrics.totalSteps}</div>
              </div>
              <div>
                <span className="text-muted-foreground">Completed:</span>
                <div className="font-medium text-green-600">{currentExecution.metrics.completedSteps}</div>
              </div>
              <div>
                <span className="text-muted-foreground">Failed:</span>
                <div className="font-medium text-red-600">{currentExecution.metrics.failedSteps}</div>
              </div>
              <div>
                <span className="text-muted-foreground">Duration:</span>
                <div className="font-medium">
                  {currentExecution.duration ? `${currentExecution.duration}ms` : 'Running...'}
                </div>
              </div>
            </div>
          )}

          {currentExecution?.error && (
            <Alert variant="destructive" className="mt-4">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>
                <strong>{currentExecution.error.code}:</strong> {currentExecution.error.message}
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>

      {/* Flow Visualization */}
      <Card>
        <CardContent className="p-0">
          <div style={{ width: '100%', height: '600px' }}>
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              onConnect={onConnect}
              nodeTypes={nodeTypes}
              fitView
              attributionPosition="top-right"
              style={{ backgroundColor: "#F7F9FB" }}
            >
              <MiniMap zoomable pannable />
              <Controls />
              <Background />
            </ReactFlow>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

// Custom Node Components
function AdapterNode({ data }: { data: any }) {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-white border-2 border-stone-400 min-w-[150px]">
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${getNodeStatusColor(data.status)}`} />
        <div className="font-bold">{data.name}</div>
      </div>
      <div className="text-xs text-gray-500">{data.type}</div>
      {data.execution && (
        <div className="text-xs mt-1">
          {data.execution.duration && `${data.execution.duration}ms`}
        </div>
      )}
    </div>
  );
}

function TransformationNode({ data }: { data: any }) {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-blue-50 border-2 border-blue-400 min-w-[150px]">
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${getNodeStatusColor(data.status)}`} />
        <div className="font-bold">{data.name}</div>
      </div>
      <div className="text-xs text-blue-600">{data.type}</div>
      {data.execution && (
        <div className="text-xs mt-1">
          {data.execution.duration && `${data.execution.duration}ms`}
        </div>
      )}
    </div>
  );
}

function ConditionNode({ data }: { data: any }) {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-yellow-50 border-2 border-yellow-400 min-w-[150px]">
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${getNodeStatusColor(data.status)}`} />
        <div className="font-bold">{data.name}</div>
      </div>
      <div className="text-xs text-yellow-600">{data.type}</div>
      {data.execution && (
        <div className="text-xs mt-1">
          {data.execution.duration && `${data.execution.duration}ms`}
        </div>
      )}
    </div>
  );
}

function LoopNode({ data }: { data: any }) {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-purple-50 border-2 border-purple-400 min-w-[150px]">
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${getNodeStatusColor(data.status)}`} />
        <div className="font-bold">{data.name}</div>
      </div>
      <div className="text-xs text-purple-600">{data.type}</div>
      {data.execution && (
        <div className="text-xs mt-1">
          {data.execution.duration && `${data.execution.duration}ms`}
        </div>
      )}
    </div>
  );
}

function DelayNode({ data }: { data: any }) {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-gray-50 border-2 border-gray-400 min-w-[150px]">
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${getNodeStatusColor(data.status)}`} />
        <div className="font-bold">{data.name}</div>
      </div>
      <div className="text-xs text-gray-600">{data.type}</div>
      {data.execution && (
        <div className="text-xs mt-1">
          {data.execution.duration && `${data.execution.duration}ms`}
        </div>
      )}
    </div>
  );
}

function getNodeStatusColor(status: string): string {
  switch (status) {
    case 'completed':
      return 'bg-green-500';
    case 'failed':
      return 'bg-red-500';
    case 'running':
      return 'bg-blue-500 animate-pulse';
    case 'paused':
      return 'bg-yellow-500';
    case 'cancelled':
      return 'bg-gray-500';
    default:
      return 'bg-gray-300';
  }
}