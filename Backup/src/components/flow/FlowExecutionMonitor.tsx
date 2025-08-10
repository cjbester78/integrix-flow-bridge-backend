import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { FlowExecution, StepExecution } from '@/types/flow';
import { flowExecutionEngine } from '@/services/flowExecutionEngine';
import { 
  Play, 
  Pause, 
  Square, 
  RefreshCw, 
  CheckCircle, 
  XCircle, 
  Clock, 
  AlertTriangle,
  Activity,
  FileText,
  BarChart3
} from 'lucide-react';

interface FlowExecutionMonitorProps {
  executionId?: string;
  showControls?: boolean;
}

export const FlowExecutionMonitor: React.FC<FlowExecutionMonitorProps> = ({
  executionId,
  showControls = true
}) => {
  const [activeExecutions, setActiveExecutions] = useState<FlowExecution[]>([]);
  const [selectedExecution, setSelectedExecution] = useState<FlowExecution | null>(null);
  const [refreshInterval, setRefreshInterval] = useState<NodeJS.Timeout | null>(null);

  useEffect(() => {
    // Load active executions
    const loadActiveExecutions = () => {
      const executions = flowExecutionEngine.getActiveExecutions();
      setActiveExecutions(executions);
      
      if (executionId) {
        const targetExecution = executions.find(e => e.id === executionId);
        if (targetExecution) {
          setSelectedExecution(targetExecution);
        }
      }
    };

    loadActiveExecutions();
    
    // Set up auto-refresh
    const interval = setInterval(loadActiveExecutions, 1000);
    setRefreshInterval(interval);

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [executionId]);

  const handlePauseExecution = (execution: FlowExecution) => {
    flowExecutionEngine.pauseExecution(execution.id);
  };

  const handleResumeExecution = (execution: FlowExecution) => {
    flowExecutionEngine.resumeExecution(execution.id);
  };

  const handleCancelExecution = (execution: FlowExecution) => {
    flowExecutionEngine.cancelExecution(execution.id);
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

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  const getProgressPercentage = (execution: FlowExecution) => {
    return execution.metrics.totalSteps > 0 
      ? (execution.metrics.completedSteps / execution.metrics.totalSteps) * 100 
      : 0;
  };

  return (
    <div className="space-y-6">
      {/* Active Executions Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="h-5 w-5" />
            Active Flow Executions
          </CardTitle>
        </CardHeader>
        <CardContent>
          {activeExecutions.length === 0 ? (
            <Alert>
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>No active flow executions found.</AlertDescription>
            </Alert>
          ) : (
            <div className="space-y-4">
              {activeExecutions.map(execution => (
                <Card 
                  key={execution.id} 
                  className={`cursor-pointer transition-colors ${
                    selectedExecution?.id === execution.id ? 'border-primary' : ''
                  }`}
                  onClick={() => setSelectedExecution(execution)}
                >
                  <CardContent className="pt-4">
                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          {getStatusIcon(execution.status)}
                          <span className="font-medium">Flow ID: {execution.flowId}</span>
                          <Badge variant={getStatusColor(execution.status)}>
                            {execution.status}
                          </Badge>
                        </div>
                        <div className="text-sm text-muted-foreground">
                          Started: {new Date(execution.startTime).toLocaleString()}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          Triggered by: {execution.triggeredBy} ({execution.triggerType})
                        </div>
                      </div>

                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <div className="text-sm font-medium">
                            Progress: {execution.metrics.completedSteps}/{execution.metrics.totalSteps}
                          </div>
                          <Progress 
                            value={getProgressPercentage(execution)} 
                            className="h-2 w-20" 
                          />
                        </div>

                        {showControls && (
                          <div className="flex gap-1">
                            {execution.status === 'running' && (
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handlePauseExecution(execution);
                                }}
                              >
                                <Pause className="h-3 w-3" />
                              </Button>
                            )}
                            {execution.status === 'paused' && (
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleResumeExecution(execution);
                                }}
                              >
                                <Play className="h-3 w-3" />
                              </Button>
                            )}
                            {['running', 'paused'].includes(execution.status) && (
                              <Button
                                variant="destructive"
                                size="sm"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleCancelExecution(execution);
                                }}
                              >
                                <Square className="h-3 w-3" />
                              </Button>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Execution Details */}
      {selectedExecution && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Execution Details: {selectedExecution.id}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="overview" className="w-full">
              <TabsList>
                <TabsTrigger value="overview">Overview</TabsTrigger>
                <TabsTrigger value="steps">Steps</TabsTrigger>
                <TabsTrigger value="logs">Logs</TabsTrigger>
                <TabsTrigger value="metrics">Metrics</TabsTrigger>
              </TabsList>

              <TabsContent value="overview" className="space-y-4">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div>
                      <h4 className="font-medium mb-2">Execution Information</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Flow ID:</span>
                          <span>{selectedExecution.flowId}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Version:</span>
                          <span>{selectedExecution.flowVersion}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Status:</span>
                          <Badge variant={getStatusColor(selectedExecution.status)}>
                            {selectedExecution.status}
                          </Badge>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Triggered By:</span>
                          <span>{selectedExecution.triggeredBy}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Trigger Type:</span>
                          <span>{selectedExecution.triggerType}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <h4 className="font-medium mb-2">Timing Information</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Start Time:</span>
                          <span>{new Date(selectedExecution.startTime).toLocaleString()}</span>
                        </div>
                        {selectedExecution.endTime && (
                          <div className="flex justify-between">
                            <span className="text-muted-foreground">End Time:</span>
                            <span>{new Date(selectedExecution.endTime).toLocaleString()}</span>
                          </div>
                        )}
                        {selectedExecution.duration && (
                          <div className="flex justify-between">
                            <span className="text-muted-foreground">Duration:</span>
                            <span>{formatDuration(selectedExecution.duration)}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {selectedExecution.error && (
                  <Alert variant="destructive">
                    <AlertTriangle className="h-4 w-4" />
                    <AlertDescription>
                      <strong>{selectedExecution.error.code}:</strong> {selectedExecution.error.message}
                    </AlertDescription>
                  </Alert>
                )}
              </TabsContent>

              <TabsContent value="steps" className="space-y-4">
                <ScrollArea className="h-96">
                  <div className="space-y-4">
                    {selectedExecution.steps.map((step, index) => (
                      <Card key={step.stepId}>
                        <CardContent className="pt-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                              {getStatusIcon(step.status)}
                              <div>
                                <h4 className="font-medium">{step.stepName}</h4>
                                <p className="text-sm text-muted-foreground">
                                  Step ID: {step.stepId}
                                </p>
                              </div>
                            </div>
                            <div className="text-right">
                              <Badge variant={getStatusColor(step.status)}>
                                {step.status}
                              </Badge>
                              {step.duration && (
                                <p className="text-sm text-muted-foreground mt-1">
                                  {formatDuration(step.duration)}
                                </p>
                              )}
                            </div>
                          </div>
                          
                          {step.error && (
                            <Alert variant="destructive" className="mt-3">
                              <AlertTriangle className="h-4 w-4" />
                              <AlertDescription>
                                {step.error.message}
                              </AlertDescription>
                            </Alert>
                          )}
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </ScrollArea>
              </TabsContent>

              <TabsContent value="logs" className="space-y-4">
                <ScrollArea className="h-96">
                  <div className="space-y-2">
                    {selectedExecution.steps.flatMap(step => 
                      step.logs.map((log, logIndex) => (
                        <div key={`${step.stepId}-${logIndex}`} className="text-sm font-mono bg-muted p-2 rounded">
                          <span className="text-muted-foreground">
                            [{new Date(log.timestamp).toLocaleTimeString()}]
                          </span>
                          <span className={`ml-2 font-medium ${
                            log.level === 'error' ? 'text-red-500' :
                            log.level === 'warn' ? 'text-yellow-500' :
                            log.level === 'info' ? 'text-blue-500' : 'text-gray-500'
                          }`}>
                            [{log.level.toUpperCase()}]
                          </span>
                          <span className="ml-2">[{step.stepName}]</span>
                          <span className="ml-2">{log.message}</span>
                        </div>
                      ))
                    )}
                  </div>
                </ScrollArea>
              </TabsContent>

              <TabsContent value="metrics" className="space-y-4">
                <div className="grid grid-cols-2 gap-6">
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg">Step Statistics</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="flex justify-between">
                        <span>Total Steps:</span>
                        <span className="font-medium">{selectedExecution.metrics.totalSteps}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Completed:</span>
                        <span className="font-medium text-green-600">{selectedExecution.metrics.completedSteps}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Failed:</span>
                        <span className="font-medium text-red-600">{selectedExecution.metrics.failedSteps}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Skipped:</span>
                        <span className="font-medium text-yellow-600">{selectedExecution.metrics.skippedSteps}</span>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg">Performance</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="flex justify-between">
                        <span>Data Processed:</span>
                        <span className="font-medium">{selectedExecution.metrics.dataProcessed} records</span>
                      </div>
                      {selectedExecution.metrics.memoryUsage && (
                        <div className="flex justify-between">
                          <span>Memory Usage:</span>
                          <span className="font-medium">{selectedExecution.metrics.memoryUsage} MB</span>
                        </div>
                      )}
                      {selectedExecution.metrics.cpuUsage && (
                        <div className="flex justify-between">
                          <span>CPU Usage:</span>
                          <span className="font-medium">{selectedExecution.metrics.cpuUsage}%</span>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </div>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      )}
    </div>
  );
};