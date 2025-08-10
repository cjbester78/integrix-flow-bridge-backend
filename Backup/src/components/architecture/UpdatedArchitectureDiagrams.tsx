import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Copy, Download } from 'lucide-react';

export const UpdatedArchitectureDiagrams: React.FC = () => {
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const updatedGraphTB = `graph TB
    subgraph "Frontend Application"
        A[App.tsx] --> B[AuthProvider]
        A --> C[Router]
        C --> D[ProtectedRoute]
        
        subgraph "Authentication"
            B --> E[AuthContext]
            E --> F[Login Page]
            D --> G[Role-based Access]
        end
        
        subgraph "Main Pages"
            H[Dashboard]
            I[Create Flow]
            J[Data Structures]
            K[Messages]
            L[Channels]
            M[Admin]
            N[Communication Adapter]
        end
        
        subgraph "Core Components"
            O[Field Mapping]
            P[Flow Management]
            Q[Channel Monitoring]
            R[Message Processing]
            S[System Logs]
            T1[Domain Log Viewer]
            T2[System Log Viewer]
        end
        
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
    end
    
    D --> H
    D --> I
    D --> J
    D --> K
    D --> L
    D --> M
    D --> N
    
    H --> O
    I --> P
    K --> R
    L --> Q
    M --> S
    M --> T1
    M --> T2
    
    S --> U1
    T1 --> U2
    T2 --> U1
    
    H --> U3
    I --> U4
    N --> U5
    J --> U6
    L --> U7
    K --> U8
    
    O --> T
    P --> V
    Q --> X
    R --> W
    S --> T
    U1 --> T
    U2 --> Z2
    
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
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style D fill:#fff3e0
    style T fill:#e8f5e8
    style U1 fill:#fff8e1
    style U2 fill:#fff8e1
    style T1 fill:#f1f8e9
    style T2 fill:#f1f8e9
    style EE fill:#fce4ec
    style FF fill:#fce4ec`;

  const updatedMindmap = `mindmap
  root((IntegrixLab Platform))
    Authentication
      Login System
      Role Management
        Administrator
        Integrator  
        Viewer
      Protected Routes
      Session Management
      Error Logging
        Login Failures
        Session Timeouts
        Permission Errors
    
    Core Features
      Flow Creation
        Adapter Configuration
        Transformation Setup
        Field Mapping
        Flow Actions
        Error Tracking
          Creation Failures
          Deployment Issues
          Transformation Errors
      Data Structures
        Schema Management
        WSDL/XSD Support
        JSON Structures
        Custom Fields
        Error Handling
          Schema Validation
          Parsing Failures
          Mapping Issues
      Message Processing
        Real-time Monitoring
        Message Logs
        Error Handling
        Statistics
        Processing Errors
          Transformation Failures
          Routing Errors
          Validation Issues
      Channel Management
        Channel Monitoring
        Log Viewing
        Performance Stats
        Customer Filtering
        Channel Errors
          Start/Stop Failures
          Configuration Issues
    
    Administration
      User Management
        User Creation
        Role Assignment
        Permission Management
        User Error Tracking
      System Logs
        Comprehensive Logging
        Domain-Specific Views
        Real-time Monitoring
        Error Analytics
      Certificate Management
      JAR File Management
      System Statistics
    
    Logging Architecture
      Central System Logs
        Technical Details
        Cross-domain Events
        Performance Metrics
        Error Tracking
      Domain Error Tables
        User Management Errors
        Flow Management Errors
        Adapter Management Errors
        Structure Management Errors
        Channel Management Errors
        Message Processing Errors
      Frontend Components
        SystemLogViewer
        DomainLogViewer
        Tabbed Interface
        Real-time Updates
      React Hooks
        useSystemLogs
        useDomainLogs
        Domain-specific Hooks
        Error Context
      Benefits
        User-friendly Errors
        Technical Debugging
        Contextual Information
        Scalable Architecture
    
    Technical Architecture
      React Frontend
        TypeScript
        Tailwind CSS
        React Router
        Context API
        Custom Hooks
          Logging Hooks
          Domain Hooks
          Error Handling
        Service Layer
          API Service
          Auth Service
          Flow Service
          Message Service
          Channel Service
          Structure Service
          System Error Logger
          Domain Log Services
      Database Design
        Central Logging
          system_logs table
        Domain Tables
          user_management_errors
          flow_management_errors
          adapter_management_errors
          structure_management_errors
          channel_management_errors
          message_processing_errors
        JSON Payload Storage
          Flexible Schema
          Adapter-specific Fields
          Rich Context Data
      Error Flow
        Backend Logging
        Frontend Polling
        Component Updates
        User Display
        Admin Monitoring`;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Updated Architecture Diagrams</h2>
          <p className="text-muted-foreground">
            Enhanced with comprehensive logging architecture and domain-specific error handling
          </p>
        </div>
        <div className="flex gap-2">
          <Badge variant="secondary">Enhanced Logging</Badge>
          <Button variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            Export Updated Diagrams
          </Button>
        </div>
      </div>

      <Tabs defaultValue="graph-tb">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="graph-tb">System Architecture Graph</TabsTrigger>
          <TabsTrigger value="mindmap">Platform Mind Map</TabsTrigger>
        </TabsList>

        <TabsContent value="graph-tb">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Enhanced Frontend Application Architecture</CardTitle>
                <Button variant="outline" size="sm" onClick={() => copyToClipboard(updatedGraphTB)}>
                  <Copy className="h-4 w-4 mr-2" />
                  Copy Mermaid Code
                </Button>
              </div>
              <p className="text-sm text-muted-foreground">
                Top-bottom flowchart showing the complete system with new logging components and domain-specific hooks
              </p>
            </CardHeader>
            <CardContent>
              <div className="w-full overflow-auto">
                <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
                  <code>{updatedGraphTB}</code>
                </pre>
              </div>
              
              <div className="mt-4 grid md:grid-cols-2 gap-4">
                <div className="p-3 bg-green-50 dark:bg-green-950 rounded-md border border-green-200 dark:border-green-800">
                  <h4 className="font-medium text-green-800 dark:text-green-200 mb-2">New Additions:</h4>
                  <ul className="text-sm text-green-700 dark:text-green-300 space-y-1">
                    <li>• Logging Architecture section</li>
                    <li>• Domain-specific hooks (8 total)</li>
                    <li>• DomainLogViewer component</li>
                    <li>• System Error Logger service</li>
                    <li>• Domain Error Tables (6 types)</li>
                  </ul>
                </div>
                
                <div className="p-3 bg-blue-50 dark:bg-blue-950 rounded-md border border-blue-200 dark:border-blue-800">
                  <h4 className="font-medium text-blue-800 dark:text-blue-200 mb-2">Enhanced Features:</h4>
                  <ul className="text-sm text-blue-700 dark:text-blue-300 space-y-1">
                    <li>• Comprehensive error tracking</li>
                    <li>• Domain-specific error context</li>
                    <li>• Real-time log monitoring</li>
                    <li>• User-friendly error messages</li>
                    <li>• Technical debugging support</li>
                  </ul>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="mindmap">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Enhanced IntegrixLab Platform Mind Map</CardTitle>
                <Button variant="outline" size="sm" onClick={() => copyToClipboard(updatedMindmap)}>
                  <Copy className="h-4 w-4 mr-2" />
                  Copy Mermaid Code
                </Button>
              </div>
              <p className="text-sm text-muted-foreground">
                Comprehensive mind map including the new logging architecture and error handling capabilities
              </p>
            </CardHeader>
            <CardContent>
              <div className="w-full overflow-auto">
                <pre className="bg-muted p-4 rounded-md text-sm font-mono overflow-x-auto whitespace-pre-wrap">
                  <code>{updatedMindmap}</code>
                </pre>
              </div>
              
              <div className="mt-4 grid md:grid-cols-3 gap-4">
                <div className="p-3 bg-purple-50 dark:bg-purple-950 rounded-md border border-purple-200 dark:border-purple-800">
                  <h4 className="font-medium text-purple-800 dark:text-purple-200 mb-2">Logging Architecture:</h4>
                  <ul className="text-sm text-purple-700 dark:text-purple-300 space-y-1">
                    <li>• Central System Logs</li>
                    <li>• Domain Error Tables</li>
                    <li>• Frontend Components</li>
                    <li>• React Hooks</li>
                    <li>• Benefits Section</li>
                  </ul>
                </div>
                
                <div className="p-3 bg-orange-50 dark:bg-orange-950 rounded-md border border-orange-200 dark:border-orange-800">
                  <h4 className="font-medium text-orange-800 dark:text-orange-200 mb-2">Error Tracking:</h4>
                  <ul className="text-sm text-orange-700 dark:text-orange-300 space-y-1">
                    <li>• Authentication Errors</li>
                    <li>• Flow Creation Errors</li>
                    <li>• Data Structure Errors</li>
                    <li>• Channel Management Errors</li>
                    <li>• Message Processing Errors</li>
                  </ul>
                </div>
                
                <div className="p-3 bg-teal-50 dark:bg-teal-950 rounded-md border border-teal-200 dark:border-teal-800">
                  <h4 className="font-medium text-teal-800 dark:text-teal-200 mb-2">Technical Enhancements:</h4>
                  <ul className="text-sm text-teal-700 dark:text-teal-300 space-y-1">
                    <li>• Domain-specific Hooks</li>
                    <li>• JSON Payload Storage</li>
                    <li>• Error Flow Management</li>
                    <li>• Real-time Monitoring</li>
                    <li>• Scalable Architecture</li>
                  </ul>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <Card className="bg-gradient-to-r from-amber-50 to-orange-50 dark:from-amber-950 dark:to-orange-950 border-amber-200 dark:border-amber-800">
        <CardContent className="p-6">
          <h3 className="text-lg font-semibold mb-3 text-amber-900 dark:text-amber-100">
            Key Architecture Improvements
          </h3>
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <h4 className="font-medium mb-3 text-amber-800 dark:text-amber-200">System Graph Enhancements:</h4>
              <ul className="space-y-2 text-sm text-amber-700 dark:text-amber-300">
                <li>✅ <strong>Logging Architecture Section:</strong> 8 domain-specific hooks</li>
                <li>✅ <strong>Enhanced Components:</strong> DomainLogViewer + SystemLogViewer</li>
                <li>✅ <strong>Service Layer:</strong> System Error Logger + Domain Log Services</li>
                <li>✅ <strong>Data Layer:</strong> Central logs + 6 domain error tables</li>
                <li>✅ <strong>Visual Styling:</strong> Color-coded sections for clarity</li>
              </ul>
            </div>
            <div>
              <h4 className="font-medium mb-3 text-amber-800 dark:text-amber-200">Mind Map Enhancements:</h4>
              <ul className="space-y-2 text-sm text-amber-700 dark:text-amber-300">
                <li>✅ <strong>Logging Architecture Branch:</strong> Complete logging system overview</li>
                <li>✅ <strong>Error Tracking:</strong> Domain-specific error categories</li>
                <li>✅ <strong>Database Design:</strong> Central + domain table architecture</li>
                <li>✅ <strong>Error Flow:</strong> Backend to frontend error handling</li>
                <li>✅ <strong>Benefits Section:</strong> User experience + technical advantages</li>
              </ul>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};