import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const CoreComponentsGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const componentsGraph = `graph TB
    H[Dashboard]
    I[Create Flow]
    J[Data Structures]
    K[Messages]
    L[Channels]
    M[Admin]
    N[Communication Adapter]
    
    subgraph "Core Components"
        O[Field Mapping]
        P[Flow Management]
        Q[Channel Monitoring]
        R[Message Processing]
        S[System Logs]
        T1[Domain Log Viewer]
        T2[System Log Viewer]
    end
    
    H --> O
    I --> P
    K --> R
    L --> Q
    M --> S
    M --> T1
    M --> T2
    
    style T1 fill:#f1f8e9
    style T2 fill:#f1f8e9`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Core Application Components</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(componentsGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Core business logic components used across the application
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{componentsGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};