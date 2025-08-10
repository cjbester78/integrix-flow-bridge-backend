import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const LoggingArchitectureGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const loggingGraph = `graph TB
    H[Dashboard]
    I[Create Flow]
    J[Data Structures]
    K[Messages]
    L[Channels]
    N[Communication Adapter]
    S[System Logs]
    T1[Domain Log Viewer]
    T2[System Log Viewer]
    
    subgraph "Logging Architecture"
        U1[useSystemLogs]
        U2[useDomainLogs]
        U3[useUserManagementLogs]
        U4[useFlowManagementLogs]
        U5[useAdapterManagementLogs]
        U6[useStructureManagementLogs]
        U7[useChannelManagementLogs]
        U8[useMessageProcessingLogs]
    end
    
    S --> U1
    T1 --> U2
    T2 --> U1
    
    H --> U3
    I --> U4
    N --> U5
    J --> U6
    L --> U7
    K --> U8
    
    style U1 fill:#fff8e1
    style U2 fill:#fff8e1`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Logging Architecture</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(loggingGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Domain-specific logging hooks and their connections to UI components
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{loggingGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};