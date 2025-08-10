import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const MainPagesGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const pagesGraph = `graph TB
    D[ProtectedRoute]
    
    subgraph "Main Pages"
        H[Dashboard]
        I[Create Flow]
        J[Data Structures]
        K[Messages]
        L[Channels]
        M[Admin]
        N[Communication Adapter]
    end
    
    D --> H
    D --> I
    D --> J
    D --> K
    D --> L
    D --> M
    D --> N
    
    style D fill:#fff3e0`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Main Application Pages</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(pagesGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Primary application pages accessible through protected routes
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{pagesGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};