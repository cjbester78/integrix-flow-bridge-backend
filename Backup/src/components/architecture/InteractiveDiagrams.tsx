import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Copy } from 'lucide-react';

export const InteractiveDiagrams: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const erDiagramCode = `erDiagram
    SYSTEM_LOGS {
        string id PK
        timestamp timestamp
        string level
        text message
        json details
        string source
        string domain_type
        string domain_reference_id
        string user_id FK
    }
    
    USER_MANAGEMENT_ERRORS {
        string id PK
        string action
        text description
        text payload
        timestamp created_at
        string system_log_id FK
        string user_id FK
    }
    
    FLOW_MANAGEMENT_ERRORS {
        string id PK
        string action
        text description
        text payload
        string flow_id
        timestamp created_at
        string system_log_id FK
        string user_id FK
    }
    
    ADAPTER_MANAGEMENT_ERRORS {
        string id PK
        string action
        text description
        text payload
        string adapter_id
        string adapter_type
        timestamp created_at
        string system_log_id FK
        string user_id FK
    }
    
    STRUCTURE_MANAGEMENT_ERRORS {
        string id PK
        string action
        text description
        text payload
        string structure_id
        string structure_type
        timestamp created_at
        string system_log_id FK
        string user_id FK
    }
    
    CHANNEL_MANAGEMENT_ERRORS {
        string id PK
        string action
        text description
        text payload
        string channel_id
        timestamp created_at
        string system_log_id FK
        string user_id FK
    }
    
    MESSAGE_PROCESSING_ERRORS {
        string id PK
        string action
        text description
        text payload
        string message_id
        string flow_id
        string channel_id
        timestamp created_at
        string system_log_id FK
    }
    
    SYSTEM_LOGS ||--o{ USER_MANAGEMENT_ERRORS : "references"
    SYSTEM_LOGS ||--o{ FLOW_MANAGEMENT_ERRORS : "references"
    SYSTEM_LOGS ||--o{ ADAPTER_MANAGEMENT_ERRORS : "references"
    SYSTEM_LOGS ||--o{ STRUCTURE_MANAGEMENT_ERRORS : "references"
    SYSTEM_LOGS ||--o{ CHANNEL_MANAGEMENT_ERRORS : "references"
    SYSTEM_LOGS ||--o{ MESSAGE_PROCESSING_ERRORS : "references"`;

  const flowchartCode = `flowchart TD
    A[User Action] --> B{Error Occurred?}
    B -->|Yes| C[Backend API Error]
    B -->|No| D[Success Response]
    
    C --> E[Log to system_logs Table]
    E --> F[Log to Domain Error Table]
    
    F --> G[Frontend Polling/Fetch]
    G --> H[useSystemLogs Hook]
    G --> I[useDomainLogs Hook]
    
    H --> J[SystemLogViewer Component]
    I --> K[DomainLogViewer Component]
    
    J --> L[Technical Details View]
    K --> M[User-Friendly Error View]
    
    L --> N[Admin System Dashboard]
    M --> O[Domain-Specific Screens]
    
    N --> P[All Logs Monitoring]
    O --> Q[Contextual Error Display]
    
    style C fill:#ffdddd
    style E fill:#ddffdd
    style F fill:#ddffdd
    style J fill:#ddddff
    style K fill:#ddddff`;

  const sequenceCode = `sequenceDiagram
    participant U as User
    participant F as Frontend
    participant A as API Backend
    participant DB as Database
    participant H as React Hooks
    participant C as Components
    
    U->>F: Performs Action (Create Flow, Test Adapter, etc.)
    F->>A: API Request with Payload
    
    alt Error Occurs
        A->>DB: INSERT into system_logs
        A->>DB: INSERT into domain_error_table
        A->>F: Error Response
        
        F->>H: useSystemLogs() / useDomainLogs()
        H->>A: GET /api/logs/system
        H->>A: GET /api/logs/domain-errors
        A->>DB: Query logs with filters
        DB->>A: Return filtered logs
        A->>H: Logs Response
        
        H->>C: Update Component State
        C->>U: Display Error Details + Context
    else Success
        A->>DB: INSERT success log (optional)
        A->>F: Success Response
        F->>U: Display Success
    end
    
    Note over H,C: Real-time log updates
    H->>A: Periodic polling (if enabled)
    A->>H: Latest logs
    H->>C: Update displays`;

  const mindmapCode = `mindmap
  root((Logging Architecture))
    Database Design
      Central Logging
        system_logs table
        Technical details
        Cross-domain events
        Performance metrics
      Domain Tables
        user_management_errors
          Login failures
          Registration issues
          Permission errors
        flow_management_errors
          Creation failures
          Deployment issues
          Transformation errors
        adapter_management_errors
          Connection failures
          Configuration issues
          Testing problems
        structure_management_errors
          Schema validation
          Parsing failures
          Mapping issues
        channel_management_errors
          Start/stop failures
          Configuration issues
        message_processing_errors
          Transformation failures
          Routing errors
          Validation issues
    Frontend Architecture
      Hook Layer
        useSystemLogs
          All sources
          Real-time updates
          Advanced filtering
          Export capabilities
        useDomainLogs
          Domain-specific
          User-friendly errors
          Contextual information
        Specialized Hooks
          useUserManagementLogs
          useFlowManagementLogs
          useAdapterManagementLogs
          useStructureManagementLogs
          useChannelManagementLogs
          useMessageProcessingLogs
      Component Layer
        SystemLogViewer
          Technical details
          All log levels
          Search and filter
          Export functionality
        DomainLogViewer
          User-friendly display
          Action context
          Payload details
          Error descriptions
        Admin Interface
          Comprehensive dashboard
          Tabbed navigation
          Real-time monitoring
          Domain separation
      Screen Integration
        Domain Screens
          Flow management
          Adapter configuration
          User management
          Channel monitoring
        Contextual Logs
          Screen-specific errors
          Related system logs
          Action history
    Benefits
      User Experience
        Friendly error messages
        Contextual information
        Clear action guidance
      Developer Experience
        Technical debugging
        Comprehensive logging
        Easy integration
      System Management
        Centralized monitoring
        Domain organization
        Scalable architecture
        Real-time visibility`;

  const DiagramCodeBlock = ({ title, code, description }: { title: string; code: string; description: string }) => (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>{title}</CardTitle>
          <Button variant="outline" size="sm" onClick={() => copyToClipboard(code)}>
            <Copy className="h-4 w-4 mr-2" />
            Copy Mermaid Code
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">{description}</p>
      </CardHeader>
      <CardContent>
        <div className="w-full overflow-auto">
          <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto">
            <code>{code}</code>
          </pre>
        </div>
        <div className="mt-4 p-3 bg-blue-50 dark:bg-blue-950 rounded-md border border-blue-200 dark:border-blue-800">
          <p className="text-sm text-blue-800 dark:text-blue-200">
            <strong>Usage:</strong> Copy the code above and paste it into any Mermaid-compatible tool (GitHub, GitLab, Notion, mermaid.live, etc.) to render the interactive diagram.
          </p>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Interactive Architecture Diagrams</h2>
          <p className="text-muted-foreground">Mermaid diagram code for comprehensive logging system visualization</p>
        </div>
        <Badge variant="secondary">Mermaid Compatible</Badge>
      </div>

      <Tabs defaultValue="er-diagram">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="er-diagram">Database ER</TabsTrigger>
          <TabsTrigger value="component-flow">Component Flow</TabsTrigger>
          <TabsTrigger value="data-flow">Data Flow</TabsTrigger>
          <TabsTrigger value="mind-map">System Mind Map</TabsTrigger>
        </TabsList>

        <TabsContent value="er-diagram">
          <DiagramCodeBlock
            title="Database Entity Relationship Diagram"
            code={erDiagramCode}
            description="Shows the relationship between the central system_logs table and domain-specific error tables."
          />
        </TabsContent>

        <TabsContent value="component-flow">
          <DiagramCodeBlock
            title="Frontend Component Architecture Flow"
            code={flowchartCode}
            description="Illustrates how errors flow from backend to frontend components through React hooks."
          />
        </TabsContent>

        <TabsContent value="data-flow">
          <DiagramCodeBlock
            title="End-to-End Data Flow Architecture"
            code={sequenceCode}
            description="Sequence diagram showing the complete flow from user action to error display."
          />
        </TabsContent>

        <TabsContent value="mind-map">
          <DiagramCodeBlock
            title="Logging System Mind Map"
            code={mindmapCode}
            description="Comprehensive overview of the entire logging architecture, from database to frontend benefits."
          />
        </TabsContent>
      </Tabs>

      <Card className="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-950 dark:to-indigo-950 border-blue-200 dark:border-blue-800">
        <CardContent className="p-6">
          <h3 className="text-lg font-semibold mb-3 text-blue-900 dark:text-blue-100">
            How to Use These Diagrams
          </h3>
          <div className="grid md:grid-cols-2 gap-4 text-sm text-blue-800 dark:text-blue-200">
            <div>
              <h4 className="font-medium mb-2">Online Tools:</h4>
              <ul className="space-y-1">
                <li>• mermaid.live (official editor)</li>
                <li>• GitHub/GitLab markdown</li>
                <li>• Notion, Obsidian</li>
                <li>• VS Code Mermaid extension</li>
              </ul>
            </div>
            <div>
              <h4 className="font-medium mb-2">Integration:</h4>
              <ul className="space-y-1">
                <li>• Documentation websites</li>
                <li>• Technical specifications</li>
                <li>• Architecture reviews</li>
                <li>• Developer onboarding</li>
              </ul>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};