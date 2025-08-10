// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FlowExecutionVisualizer } from '@/components/flow/FlowExecutionVisualizer';
import { FlowExecutionMonitor } from '@/components/flow/FlowExecutionMonitor';
import { FlowScheduler } from '@/components/flow/FlowScheduler';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { FlowDefinition } from '@/types/flow';
import { Play, Activity, Calendar, BarChart3, Loader2 } from 'lucide-react';
import { flowService } from '@/services/flowService';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';

export default function FlowExecutionEngine() {
  const [selectedFlow, setSelectedFlow] = useState<FlowDefinition | null>(null);
  const [flows, setFlows] = useState<FlowDefinition[]>([]);
  const [loadingFlows, setLoadingFlows] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { toast } = useToast();

  // Fetch available flows
  useEffect(() => {
    const fetchFlows = async () => {
      try {
        setError(null);
        setLoadingFlows(true);
        
        const response = await flowService.getFlows();
        
        if (response.success && response.data) {
          setFlows(response.data);
          // Select the first flow by default if available
          if (response.data.length > 0 && !selectedFlow) {
            setSelectedFlow(response.data[0]);
          }
        } else {
          setError(response.error || 'Failed to fetch flows');
          toast({
            variant: "destructive",
            title: "Error",
            description: response.error || 'Failed to fetch flows',
          });
        }
      } catch (err) {
        console.error('Error fetching flows:', err);
        setError('Failed to load flows');
        toast({
          variant: "destructive",
          title: "Error",
          description: 'Failed to load flows',
        });
      } finally {
        setLoadingFlows(false);
      }
    };

    fetchFlows();
  }, [toast]);

  // Handle flow selection
  const handleFlowSelection = (flowId: string) => {
    const flow = flows.find(f => f.id === flowId);
    if (flow) {
      setSelectedFlow(flow);
    }
  };

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Flow Execution Engine</h1>
        <p className="text-gray-600">Monitor, execute, and schedule your integration flows</p>
      </div>

      {/* Flow Selection */}
      <div className="mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Select Flow</CardTitle>
          </CardHeader>
          <CardContent>
            {loadingFlows ? (
              <div className="flex items-center justify-center p-4">
                <Loader2 className="h-6 w-6 animate-spin" />
              </div>
            ) : error ? (
              <div className="text-destructive p-4">
                {error}
              </div>
            ) : flows.length === 0 ? (
              <div className="text-muted-foreground p-4">
                No flows available. Please create a flow first.
              </div>
            ) : (
              <Select
                value={selectedFlow?.id || ''}
                onValueChange={handleFlowSelection}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Select a flow to monitor" />
                </SelectTrigger>
                <SelectContent>
                  {flows.map((flow) => (
                    <SelectItem key={flow.id} value={flow.id}>
                      {flow.name} - {flow.description}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Flow Execution Tabs */}
      {selectedFlow && (
        <Tabs defaultValue="visualizer" className="space-y-4">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="visualizer">
              <Play className="w-4 h-4 mr-2" />
              Visualizer
            </TabsTrigger>
            <TabsTrigger value="monitor">
              <Activity className="w-4 h-4 mr-2" />
              Monitor
            </TabsTrigger>
            <TabsTrigger value="scheduler">
              <Calendar className="w-4 h-4 mr-2" />
              Scheduler
            </TabsTrigger>
            <TabsTrigger value="analytics">
              <BarChart3 className="w-4 h-4 mr-2" />
              Analytics
            </TabsTrigger>
          </TabsList>

          <TabsContent value="visualizer" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Flow Visualizer</CardTitle>
              </CardHeader>
              <CardContent>
                <FlowExecutionVisualizer flow={selectedFlow} />
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="monitor" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Execution Monitor</CardTitle>
              </CardHeader>
              <CardContent>
                <FlowExecutionMonitor flowId={selectedFlow.id} />
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="scheduler" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Flow Scheduler</CardTitle>
              </CardHeader>
              <CardContent>
                <FlowScheduler flowId={selectedFlow.id} />
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="analytics" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Flow Analytics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm font-medium">Total Executions</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">0</div>
                      <p className="text-xs text-muted-foreground">Last 24 hours</p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm font-medium">Success Rate</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">0%</div>
                      <p className="text-xs text-muted-foreground">Last 24 hours</p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm font-medium">Avg. Duration</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">0ms</div>
                      <p className="text-xs text-muted-foreground">Last 24 hours</p>
                    </CardContent>
                  </Card>
                </div>

                <div className="mt-6">
                  <p className="text-muted-foreground text-center py-8">
                    Analytics data will be available once flows are executed.
                  </p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      )}

      {/* No Flow Selected */}
      {!selectedFlow && !loadingFlows && flows.length > 0 && (
        <Card className="p-8 text-center">
          <p className="text-muted-foreground">
            Please select a flow from the dropdown above to view execution details.
          </p>
        </Card>
      )}
    </div>
  );
}