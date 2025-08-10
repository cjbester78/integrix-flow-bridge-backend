import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const FrontendApplicationGraph: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const frontendGraph = `graph TB
    subgraph "Frontend Application"
        A[App.tsx] --> B[AuthProvider]
        A --> C[Router]
        C --> D[ProtectedRoute]
        
        subgraph "Authentication"
            B --> E[AuthContext]
            E --> F[Login Page]
            D --> G[Role-based Access]
        end
    end
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style D fill:#fff3e0`;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Frontend Application Core</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(frontendGraph)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          Core app structure with authentication and routing
        </p>
      </CardHeader>
      <CardContent>
        <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
          <code>{frontendGraph}</code>
        </pre>
      </CardContent>
    </Card>
  );
};