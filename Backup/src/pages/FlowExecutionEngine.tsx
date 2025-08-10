import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FlowExecutionVisualizer } from '@/components/flow/FlowExecutionVisualizer';
import { FlowExecutionMonitor } from '@/components/flow/FlowExecutionMonitor';
import { FlowScheduler } from '@/components/flow/FlowScheduler';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { FlowDefinition } from '@/types/flow';
import { Play, Activity, Calendar, BarChart3 } from 'lucide-react';

// Mock flow definition for demonstration
const mockFlowDefinition: FlowDefinition = {
  id: 'flow_demo_001',
  name: 'Customer Data Integration Flow',
  description: 'Synchronizes customer data between CRM and ERP systems',
  version: '1.0.0',
  status: 'published',
  steps: [
    {
      id: 'step_1',
      type: 'adapter',
      name: 'CRM Data Extract',
      configuration: {
        adapterType: 'rest',
        url: 'https://crm.example.com/api/customers',
        method: 'GET'
      },
      position: { x: 100, y: 100 },
      connections: ['step_2']
    },
    {
      id: 'step_2',
      type: 'transformation',
      name: 'Data Mapping',
      configuration: {
        transformationType: 'field_mapping',
        mappingRules: []
      },
      position: { x: 300, y: 100 },
      connections: ['step_3']
    },
    {
      id: 'step_3',
      type: 'condition',
      name: 'Data Validation',
      configuration: {
        condition: 'data.email != null && data.customerId != null'
      },
      position: { x: 500, y: 100 },
      connections: ['step_4']
    },
    {
      id: 'step_4',
      type: 'adapter',
      name: 'ERP Data Update',
      configuration: {
        adapterType: 'soap',
        wsdlUrl: 'https://erp.example.com/services/customer?wsdl',
        operation: 'updateCustomer'
      },
      position: { x: 700, y: 100 },
      connections: []
    }
  ],
  triggers: [
    {
      id: 'trigger_manual',
      type: 'manual',
      configuration: {},
      enabled: true
    }
  ],
  settings: {
    retryPolicy: {
      maxRetries: 3,
      retryDelay: 5000,
      backoffStrategy: 'exponential'
    },
    timeout: 300000,
    parallelExecution: false,
    errorHandling: 'stop',
    logging: true
  },
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
};

export const FlowExecutionEngine = () => {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Flow Execution Engine</h2>
        <p className="text-muted-foreground">
          Execute, monitor, and schedule integration flows with real-time visualization
        </p>
      </div>

      <Tabs defaultValue="execution" className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="execution" className="flex items-center gap-2">
            <Play className="h-4 w-4" />
            Execution
          </TabsTrigger>
          <TabsTrigger value="monitor" className="flex items-center gap-2">
            <Activity className="h-4 w-4" />
            Monitor
          </TabsTrigger>
          <TabsTrigger value="scheduler" className="flex items-center gap-2">
            <Calendar className="h-4 w-4" />
            Scheduler
          </TabsTrigger>
          <TabsTrigger value="analytics" className="flex items-center gap-2">
            <BarChart3 className="h-4 w-4" />
            Analytics
          </TabsTrigger>
        </TabsList>

        <TabsContent value="execution" className="space-y-6">
          <FlowExecutionVisualizer
            flowDefinition={mockFlowDefinition}
            onExecutionStart={(executionId) => console.log('Execution started:', executionId)}
            onExecutionComplete={(execution) => console.log('Execution completed:', execution)}
          />
        </TabsContent>

        <TabsContent value="monitor" className="space-y-6">
          <FlowExecutionMonitor />
        </TabsContent>

        <TabsContent value="scheduler" className="space-y-6">
          <FlowScheduler flowDefinition={mockFlowDefinition} />
        </TabsContent>

        <TabsContent value="analytics" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Flow Analytics</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-center py-12 text-muted-foreground">
                Flow analytics and performance metrics will be displayed here.
                <br />
                This includes execution history, success rates, performance trends, and resource usage.
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default FlowExecutionEngine;