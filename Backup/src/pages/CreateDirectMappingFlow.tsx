import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ArrowLeft, Settings as SettingsIcon } from 'lucide-react';
import { FlowActionsCard } from '@/components/createFlow/FlowActionsCard';
import { FlowSummaryCard } from '@/components/createFlow/FlowSummaryCard';
import { QuickTipsCard } from '@/components/createFlow/QuickTipsCard';
import { FieldMappingScreen } from '@/components/FieldMappingScreen';
import { Database, Server, Globe, Mail, FileText, Code } from 'lucide-react';

// Mock adapter data
const mockAdapters = [
  { id: 'jdbc-receiver', name: 'JDBC Receiver', icon: Database, category: 'Database' },
  { id: 'jdbc-sender', name: 'JDBC Sender', icon: Database, category: 'Database' },
  { id: 'http-receiver', name: 'HTTP Receiver', icon: Globe, category: 'Web Service' },
  { id: 'http-sender', name: 'HTTP Sender', icon: Globe, category: 'Web Service' },
  { id: 'rest-receiver', name: 'REST Receiver', icon: Server, category: 'Web Service' },
  { id: 'rest-sender', name: 'REST Sender', icon: Server, category: 'Web Service' },
  { id: 'soap-receiver', name: 'SOAP Receiver', icon: Code, category: 'Web Service' },
  { id: 'soap-sender', name: 'SOAP Sender', icon: Code, category: 'Web Service' },
  { id: 'mail-receiver', name: 'Mail Receiver', icon: Mail, category: 'Communication' },
  { id: 'mail-sender', name: 'Mail Sender', icon: Mail, category: 'Communication' },
  { id: 'file-adapter', name: 'File Adapter', icon: FileText, category: 'File' },
  { id: 'ftp-adapter', name: 'FTP Adapter', icon: SettingsIcon, category: 'File' },
  { id: 'sftp-adapter', name: 'SFTP Adapter', icon: SettingsIcon, category: 'File' },
];

// Mock business components
const mockBusinessComponents = [
  { id: 'customer-mgmt', name: 'Customer Management' },
  { id: 'order-processing', name: 'Order Processing' },
  { id: 'inventory-mgmt', name: 'Inventory Management' },
  { id: 'financial-sys', name: 'Financial System' },
  { id: 'hr-system', name: 'HR System' },
];

// Mock data structures
const mockDataStructures = [
  { 
    id: 'customer-schema', 
    name: 'Customer Schema',
    type: 'xsd' as const,
    structure: '',
    createdAt: new Date().toISOString(),
    usage: 'source' as const
  },
  { 
    id: 'order-schema', 
    name: 'Order Schema',
    type: 'json' as const,
    structure: '',
    createdAt: new Date().toISOString(),
    usage: 'target' as const
  },
  { 
    id: 'product-schema', 
    name: 'Product Schema',
    type: 'xsd' as const,
    structure: '',
    createdAt: new Date().toISOString(),
    usage: 'source' as const
  },
  { 
    id: 'invoice-schema', 
    name: 'Invoice Schema',
    type: 'json' as const,
    structure: '',
    createdAt: new Date().toISOString(),
    usage: 'target' as const
  },
];

export function CreateDirectMappingFlow() {
  const navigate = useNavigate();
  const [showFieldMapping, setShowFieldMapping] = useState(false);
  
  // Flow state
  const [flowName, setFlowName] = useState('');
  const [description, setDescription] = useState('');
  const [sourceBusinessComponent, setSourceBusinessComponent] = useState('');
  const [targetBusinessComponent, setTargetBusinessComponent] = useState('');
  const [sourceAdapter, setSourceAdapter] = useState('');
  const [targetAdapter, setTargetAdapter] = useState('');
  const [sourceStructure, setSourceStructure] = useState('');
  const [targetStructure, setTargetStructure] = useState('');

  const handleClose = () => {
    navigate('/dashboard');
  };

  const handleSave = (mappings?: any[], mappingName?: string) => {
    console.log('Saving direct mapping flow:', { 
      flowName, 
      description, 
      sourceBusinessComponent, 
      targetBusinessComponent, 
      sourceAdapter, 
      targetAdapter,
      sourceStructure,
      targetStructure,
      mappings, 
      mappingName 
    });
    navigate('/dashboard');
  };

  const handleTestFlow = () => {
    console.log('Testing flow...');
  };

  const handleCreateMapping = () => {
    setShowFieldMapping(true);
  };

  if (showFieldMapping) {
    return (
      <div className="min-h-screen bg-background">
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container mx-auto px-6 py-4">
            <div className="flex items-center gap-4">
              <Button 
                variant="ghost" 
                size="sm" 
                onClick={() => setShowFieldMapping(false)}
                className="gap-2"
              >
                <ArrowLeft className="h-4 w-4" />
                Back to Flow Configuration
              </Button>
              <div>
                <h1 className="text-2xl font-bold tracking-tight">Field Mapping - {flowName}</h1>
                <p className="text-muted-foreground">
                  Map fields between {sourceAdapter} and {targetAdapter}
                </p>
              </div>
            </div>
          </div>
        </div>
        
        <FieldMappingScreen
          onClose={handleClose}
          onSave={handleSave}
          initialMappingName={flowName}
          sourceAdapterType={sourceAdapter}
          targetAdapterType={targetAdapter}
        />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="px-6 py-4">
          <div className="flex items-center gap-4">
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={handleClose}
              className="gap-2"
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </Button>
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Create Integration Flow</h1>
              <p className="text-muted-foreground">
                Design and configure a new message integration flow
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            {/* Flow Details */}
            <Card className="animate-scale-in">
              <CardHeader>
                <CardTitle>Flow Details</CardTitle>
                <CardDescription>Configure the basic information for your integration flow</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="flowName">Flow Name *</Label>
                  <Input
                    id="flowName"
                    placeholder="e.g., Customer Data Sync"
                    value={flowName}
                    onChange={(e) => setFlowName(e.target.value)}
                    className="transition-all duration-300 focus:scale-[1.01]"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    placeholder="Describe what this integration flow does..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="transition-all duration-300 focus:scale-[1.01]"
                    rows={3}
                  />
                </div>
              </CardContent>
            </Card>

            {/* Source & Target Configuration */}
            <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
              <CardHeader>
                <CardTitle>Source & Target Configuration</CardTitle>
                <CardDescription>Select the source and target systems for your integration</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Source Configuration */}
                  <div className="space-y-4">
                    <h4 className="font-medium text-sm text-muted-foreground uppercase tracking-wide">Source</h4>
                    <div className="space-y-3">
                      <div className="space-y-2">
                        <Label>Source Business Component *</Label>
                        <Select value={sourceBusinessComponent} onValueChange={setSourceBusinessComponent}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select source business component" />
                          </SelectTrigger>
                          <SelectContent>
                            {mockBusinessComponents.map((component) => (
                              <SelectItem key={component.id} value={component.id}>
                                {component.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Source Adapter *</Label>
                        <Select value={sourceAdapter} onValueChange={setSourceAdapter}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select source system" />
                          </SelectTrigger>
                          <SelectContent>
                            {mockAdapters.map((adapter) => (
                              <SelectItem key={adapter.id} value={adapter.id}>
                                {adapter.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </div>

                  {/* Target Configuration */}
                  <div className="space-y-4">
                    <h4 className="font-medium text-sm text-muted-foreground uppercase tracking-wide">Target</h4>
                    <div className="space-y-3">
                      <div className="space-y-2">
                        <Label>Target Business Component *</Label>
                        <Select value={targetBusinessComponent} onValueChange={setTargetBusinessComponent}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select target business component" />
                          </SelectTrigger>
                          <SelectContent>
                            {mockBusinessComponents.map((component) => (
                              <SelectItem key={component.id} value={component.id}>
                                {component.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Target Adapter *</Label>
                        <Select value={targetAdapter} onValueChange={setTargetAdapter}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select target system" />
                          </SelectTrigger>
                          <SelectContent>
                            {mockAdapters.map((adapter) => (
                              <SelectItem key={adapter.id} value={adapter.id}>
                                {adapter.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Data Transformations */}
            <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
              <CardHeader>
                <CardTitle>Data Transformations</CardTitle>
                <CardDescription>Configure how data should be transformed during the flow</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex flex-col items-center justify-center py-12 text-center space-y-4">
                  <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center">
                    <SettingsIcon className="h-8 w-8 text-muted-foreground" />
                  </div>
                  <div className="space-y-2">
                    <p className="font-medium">Configure data transformations with field mapping</p>
                    <p className="text-sm text-muted-foreground">
                      Field mapping allows you to connect source and target fields with custom logic.
                    </p>
                    <p className="text-sm text-orange-600">
                      Please select both source and target business components AND their corresponding adapters above to enable mapping.
                    </p>
                  </div>
                  <Button 
                    onClick={handleCreateMapping}
                    disabled={!sourceBusinessComponent || !targetBusinessComponent || !sourceAdapter || !targetAdapter}
                    className="mt-4"
                  >
                    Create Mapping
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Right Sidebar */}
          <div className="lg:col-span-1 space-y-6">
            <FlowActionsCard
              onTestFlow={handleTestFlow}
              onSaveFlow={() => handleSave()}
            />
            
            <FlowSummaryCard
              sourceAdapter={sourceAdapter}
              targetAdapter={targetAdapter}
              sourceStructure={sourceStructure}
              targetStructure={targetStructure}
              selectedTransformations={[]}
              adapters={mockAdapters}
              sampleStructures={mockDataStructures}
            />
            
            <QuickTipsCard />
          </div>
        </div>
      </div>
    </div>
  );
}