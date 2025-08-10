import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const DataLayerGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const dataGraph = `graph TB
    T[API Service]
    V[Flow Service]
    W[Message Service]
    X[Channel Service]
    Z1[System Error Logger]
    Z2[Domain Log Services]
    
    subgraph "Data Layer"
        Z[Customer Data]
        AA[Flow Definitions]
        BB[Message Logs]
        CC[Channel Status]
        DD[System Metrics]
        EE[System Logs Table]
        FF[Domain Error Tables]
            FF1[User Management Errors]
            FF2[Flow Management Errors]
            FF3[Adapter Management Errors]
            FF4[Structure Management Errors]
            FF5[Channel Management Errors]
            FF6[Message Processing Errors]
    end
    
    T --> Z
    V --> AA
    W --> BB
    X --> CC
    T --> DD
    Z1 --> EE
    Z2 --> FF
    
    FF --> FF1
    FF --> FF2
    FF --> FF3
    FF --> FF4
    FF --> FF5
    FF --> FF6
    
    style EE fill:#fce4ec
    style FF fill:#fce4ec`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Data Layer</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(dataGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Database tables and data storage for application and logging
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{dataGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};