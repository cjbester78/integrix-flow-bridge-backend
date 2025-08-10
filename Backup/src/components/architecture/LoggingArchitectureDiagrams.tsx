import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { 
  Database, 
  Workflow, 
  Network, 
  User, 
  Settings, 
  MessageSquare, 
  GitBranch,
  Eye,
  Download,
  Layers
} from 'lucide-react';

export const LoggingArchitectureDiagrams: React.FC = () => {
  const [activeTab, setActiveTab] = useState('database');

  const DatabaseSchemaDigram = () => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Database className="h-5 w-5" />
          Database Schema Architecture
        </h3>
        <Badge variant="secondary">Central + Domain Tables</Badge>
      </div>
      
      <div className="bg-muted p-4 rounded-lg">
        <pre className="text-sm overflow-auto">
{`
┌─────────────────┐       ┌──────────────────────────┐
│   system_logs   │◄──────┤ user_management_errors   │
│                 │       │                          │
│ • id (PK)       │       │ • id (PK)                │
│ • timestamp     │       │ • action                 │
│ • level         │       │ • description            │
│ • message       │       │ • payload                │
│ • details       │       │ • system_log_id (FK)     │
│ • source        │       │ • user_id (FK)           │
│ • domain_type   │       └──────────────────────────┘
│ • domain_ref_id │       
│ • user_id       │       ┌──────────────────────────┐
└─────────────────┘◄──────┤ flow_management_errors   │
          │                │                          │
          │                │ • id (PK)                │
          │                │ • action                 │
          │                │ • description            │
          │                │ • payload                │
          │                │ • flow_id                │
          │                │ • system_log_id (FK)     │
          │                └──────────────────────────┘
          │                
          │                ┌──────────────────────────┐
          └────────────────┤adapter_management_errors │
          │                │                          │
          │                │ • id (PK)                │
          │                │ • action                 │
          │                │ • adapter_id             │
          │                │ • adapter_type           │
          │                │ • system_log_id (FK)     │
          │                └──────────────────────────┘
          │                
          │                ┌──────────────────────────┐
          └────────────────┤structure_management_errors│
          │                │                          │
          │                │ • id (PK)                │
          │                │ • structure_type         │
          │                │ • system_log_id (FK)     │
          │                └──────────────────────────┘
          │                
          └─── (Additional Domain Tables) ───┘
`}
        </pre>
      </div>
    </div>
  );

  const ComponentArchitectureDiagram = () => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Layers className="h-5 w-5" />
          Frontend Component Architecture
        </h3>
        <Badge variant="secondary">React Hooks + Components</Badge>
      </div>
      
      <div className="bg-muted p-4 rounded-lg">
        <pre className="text-sm overflow-auto">
{`
┌─────────────────────────────────────────────────────────────┐
│                     Admin Dashboard                         │
│  ┌─────────────────────────────────────────────────────────┤
│  │                SystemLogs Component                     │
│  │  ┌─────────────┬─────────────┬─────────────┬──────────┐ │
│  │  │   System    │    Users    │    Flows    │    ...   │ │
│  │  │     Tab     │     Tab     │     Tab     │    Tabs  │ │
│  │  └─────────────┴─────────────┴─────────────┴──────────┘ │
│  └─────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────────┤
│  │                Domain-Specific Screens                  │
│  │                                                         │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  │ Flow Screen  │  │Adapter Screen│  │ User Screen  │  │
│  │  │              │  │              │  │              │  │
│  │  │ useFlow      │  │ useAdapter   │  │ useUser      │  │
│  │  │ ManagementLogs│  │ManagementLogs│  │ManagementLogs│  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  │
│  └─────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      Hook Layer                             │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  useSystemLogs  │    │       useDomainLogs             │ │
│  │                 │    │                                 │ │
│  │ • System-wide   │    │ • Domain-specific               │ │
│  │ • All sources   │    │ • User-friendly errors          │ │
│  │ • Technical     │    │ • Contextual filtering          │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│           │                           │                     │
│           └───────────┬───────────────┘                     │
└─────────────────────────────────────────────────────────────┘
                        │
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                             │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   API Service   │    │    SystemErrorLogger            │ │
│  │                 │    │                                 │ │
│  │ • HTTP calls    │    │ • Mock data                     │ │
│  │ • Domain APIs   │    │ • Sample logs                   │ │
│  │ • Error handling│    │ • Local storage                 │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
`}
        </pre>
      </div>
    </div>
  );

  const DataFlowDiagram = () => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <GitBranch className="h-5 w-5" />
          Data Flow Architecture
        </h3>
        <Badge variant="secondary">API ↔ Frontend Flow</Badge>
      </div>
      
      <div className="bg-muted p-4 rounded-lg">
        <pre className="text-sm overflow-auto">
{`
Backend API                     Frontend React App
┌──────────────┐               ┌─────────────────────┐
│   System     │               │                     │
│   Logs API   │◄──────────────┤  useSystemLogs()    │
│              │               │                     │
│ GET /logs/   │               │ • Filters           │
│ system       │               │ • Search            │
└──────────────┘               │ • Real-time         │
                               └─────────────────────┘
┌──────────────┐                         │
│   Domain     │               ┌─────────────────────┐
│   Error APIs │◄──────────────┤  useDomainLogs()    │
│              │               │                     │
│ GET /logs/   │               │ • Domain filtering  │
│ user-errors  │               │ • Reference IDs     │
│ flow-errors  │               │ • Combined view     │
│ adapter-errors│               └─────────────────────┘
│ ...          │                         │
└──────────────┘               ┌─────────────────────┐
                               │                     │
┌──────────────┐               │  Domain Components  │
│  Application │◄──────────────┤                     │
│  Events      │               │ • FlowScreen        │
│              │               │ • AdapterScreen     │
│ • User login │               │ • UserScreen        │
│ • Flow create│               │ • ChannelScreen     │
│ • API errors │               └─────────────────────┘
│ • Adapter    │                         │
│   failures   │               ┌─────────────────────┐
└──────────────┘               │                     │
        │                      │  SystemLogViewer    │
        │                      │  DomainLogViewer    │
        │                      │                     │
        ▼                      │ • Expandable logs   │
┌──────────────┐               │ • Level filtering   │
│   Database   │               │ • Search            │
│              │               │ • Export            │
│ system_logs  │               └─────────────────────┘
│ domain_errors│
└──────────────┘

Error Flow:
1. User Action → API Error
2. API logs to system_logs + domain_error table
3. Frontend polls/fetches logs
4. Display in appropriate component
5. User sees both technical + friendly error
`}
        </pre>
      </div>
    </div>
  );

  const LoggingMindMap = () => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Eye className="h-5 w-5" />
          Logging System Mind Map
        </h3>
        <Badge variant="secondary">Conceptual Overview</Badge>
      </div>
      
      <div className="bg-muted p-4 rounded-lg">
        <pre className="text-sm overflow-auto">
{`
                         LOGGING ARCHITECTURE
                                │
                ┌───────────────┼───────────────┐
                │               │               │
           DATABASE        FRONTEND         BACKEND
               │               │               │
       ┌───────┼───────┐      │      ┌───────┼───────┐
       │       │       │      │      │       │       │
  CENTRAL   DOMAIN   USERS    │   LOGGING  APIS   MONITORING
   LOGS    TABLES           │                  
       │       │             │      
 system_logs   │             │      
       │   ┌───┴────┐        │
       │   │        │        │
 - timestamp │   user_mgmt    │
 - level     │   flow_mgmt    │
 - message   │   adapter_mgmt │
 - details   │   structure_mgmt
 - source    │   channel_mgmt │
 - domain    │   message_proc │
              │                │
              └─ payload       │
                 description   │
                 action        │
                              │
                    ┌─────────┼─────────┐
                    │         │         │
                 HOOKS    COMPONENTS  SERVICES
                    │         │         │
              ┌─────┴─────┐   │    ┌────┴────┐
              │           │   │    │         │
        useSystemLogs  useDomain  │   api.ts  systemErrorLogger
              │        Logs   │   │         │
              │           │   │   │
        - All sources    │   │   │- HTTP calls
        - Filtering      │   │   │- Error handling  
        - Real-time      │   │   │- Mock fallback
                        │   │
               ┌────────┴───┴──────────┐
               │                       │
          SystemLogViewer      DomainLogViewer
               │                       │
        - Technical details    - User-friendly
        - All log levels       - Domain context
        - Search/filter        - Action tracking
        - Export               - Payload display

Domain-Specific Integration:
┌─────────────┬─────────────┬─────────────┬─────────────┐
│    USERS    │    FLOWS    │  ADAPTERS   │  CHANNELS   │
│             │             │             │             │
│ Login logs  │ Flow create │ Connection  │ Start/Stop  │
│ Register    │ Deploy      │ Test        │ Config      │
│ Profile     │ Transform   │ Configure   │ Monitor     │
│ Permissions │ Monitor     │ Deploy      │ Logs        │
└─────────────┴─────────────┴─────────────┴─────────────┘
       │             │             │             │
       └─────────────┼─────────────┼─────────────┘
                     │             │
            Central system_logs table
                 (technical details)
                     │
                Domain error tables
              (user-friendly context)

Benefits:
• Centralized system logging
• Domain-specific error context  
• User-friendly error messages
• Technical debugging capability
• Contextual log viewing
• Scalable architecture
• Real-time monitoring
`}
        </pre>
      </div>
    </div>
  );

  const MermaidDiagrams = () => (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Workflow className="h-5 w-5" />
          Interactive Diagrams
        </h3>
        <Badge variant="secondary">Mermaid Diagrams</Badge>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Database Entity Relationship</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mermaid-container">
            <div className="bg-white p-4 rounded border">
              <p className="text-sm text-muted-foreground mb-2">Mermaid ER Diagram:</p>
              <code className="text-xs block bg-gray-100 p-2 rounded">
{`erDiagram
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
    
    SYSTEM_LOGS ||--o{ USER_MANAGEMENT_ERRORS : "has"
    SYSTEM_LOGS ||--o{ FLOW_MANAGEMENT_ERRORS : "has"
    SYSTEM_LOGS ||--o{ ADAPTER_MANAGEMENT_ERRORS : "has"`}
              </code>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Component Architecture Flow</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mermaid-container">
            <div className="bg-white p-4 rounded border">
              <p className="text-sm text-muted-foreground mb-2">Mermaid Flowchart:</p>
              <code className="text-xs block bg-gray-100 p-2 rounded">
{`flowchart TD
    A[User Action] --> B{Error Occurred?}
    B -->|Yes| C[Log to system_logs]
    B -->|No| D[Success Response]
    C --> E[Log to domain_error table]
    E --> F[Frontend Hook Fetch]
    F --> G[useSystemLogs]
    F --> H[useDomainLogs]
    G --> I[SystemLogViewer]
    H --> J[DomainLogViewer]
    I --> K[Technical Details]
    J --> L[User-Friendly Error]
    K --> M[Admin Debug View]
    L --> N[User Context View]`}
              </code>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Domain Integration Map</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mermaid-container">
            <div className="bg-white p-4 rounded border">
              <p className="text-sm text-muted-foreground mb-2">Mermaid Mind Map:</p>
              <code className="text-xs block bg-gray-100 p-2 rounded">
{`mindmap
  root((Logging System))
    Database Layer
      Central Logs
        system_logs
        Technical Details
        All Domains
      Domain Tables
        user_management_errors
        flow_management_errors
        adapter_management_errors
        structure_management_errors
        channel_management_errors
        message_processing_errors
    Frontend Layer
      Hooks
        useSystemLogs
        useDomainLogs
        Domain-specific hooks
      Components
        SystemLogViewer
        DomainLogViewer
        Tabbed Interface
      Screens
        Admin Dashboard
        Domain Screens
        Contextual Views
    Architecture Benefits
      Centralized Logging
      Domain Context
      User-Friendly Errors
      Technical Debugging
      Real-time Monitoring
      Scalable Design`}
              </code>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold flex items-center gap-3">
            <Database className="h-7 w-7" />
            Logging Architecture Diagrams
          </h2>
          <p className="text-muted-foreground">
            Comprehensive visual documentation of the logging system architecture
          </p>
        </div>
        <Button variant="outline" size="sm">
          <Download className="h-4 w-4 mr-2" />
          Export Diagrams
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="database">Database Schema</TabsTrigger>
          <TabsTrigger value="components">Components</TabsTrigger>
          <TabsTrigger value="dataflow">Data Flow</TabsTrigger>
          <TabsTrigger value="mindmap">Mind Map</TabsTrigger>
          <TabsTrigger value="mermaid">Mermaid Diagrams</TabsTrigger>
        </TabsList>

        <TabsContent value="database">
          <Card>
            <CardContent className="p-6">
              <DatabaseSchemaDigram />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="components">
          <Card>
            <CardContent className="p-6">
              <ComponentArchitectureDiagram />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="dataflow">
          <Card>
            <CardContent className="p-6">
              <DataFlowDiagram />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="mindmap">
          <Card>
            <CardContent className="p-6">
              <LoggingMindMap />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="mermaid">
          <MermaidDiagrams />
        </TabsContent>
      </Tabs>
    </div>
  );
};