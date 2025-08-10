import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const ServicesLayerGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const servicesGraph = `graph TB
    O[Field Mapping]
    P[Flow Management]
    Q[Channel Monitoring]
    R[Message Processing]
    S[System Logs]
    U1[useSystemLogs]
    U2[useDomainLogs]
    
    subgraph "Services Layer"
        T[API Service]
        U[Auth Service]
        V[Flow Service]
        W[Message Service]
        X[Channel Service]
        Y[Structure Service]
        Z1[System Error Logger]
        Z2[Domain Log Services]
    end
    
    O --> T
    P --> V
    Q --> X
    R --> W
    S --> T
    U1 --> T
    U2 --> Z2
    
    style T fill:#e8f5e8`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Services Layer</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(servicesGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Service layer connecting components to backend APIs and logging
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{servicesGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};