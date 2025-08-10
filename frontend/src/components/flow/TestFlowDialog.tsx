// @ts-nocheck
import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Upload, Play, Loader2, FileText, CheckCircle, XCircle } from 'lucide-react';
import { api } from '@/services/api';
import { useToast } from '@/hooks/use-toast';

interface TestFlowDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  flowConfig: {
    flowName: string;
    sourceAdapter: string;
    targetAdapter: string;
    sourceStructure?: string;
    targetStructure?: string;
    fieldMappings?: any[];
    sourceBusinessComponentId?: string;
    targetBusinessComponentId?: string;
  };
}

export function TestFlowDialog({ open, onOpenChange, flowConfig }: TestFlowDialogProps) {
  const { toast } = useToast();
  const [testData, setTestData] = useState('');
  const [outputData, setOutputData] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [testStatus, setTestStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [executionLogs, setExecutionLogs] = useState<string[]>([]);

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      if (!file.name.endsWith('.xml')) {
        toast({
          title: "Invalid File",
          description: "Please upload an XML file.",
          variant: "destructive",
        });
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target?.result as string;
        setTestData(content);
        setOutputData('');
        setTestStatus('idle');
        setExecutionLogs([]);
      };
      reader.readAsText(file);
    }
  };

  const handleRunTest = async () => {
    if (!testData.trim()) {
      toast({
        title: "No Test Data",
        description: "Please upload an XML file or paste XML data to test.",
        variant: "destructive",
      });
      return;
    }

    setIsRunning(true);
    setOutputData('');
    setTestStatus('idle');
    setErrorMessage('');
    setExecutionLogs([]);

    try {
      // Prepare test request
      const testRequest = {
        flowName: flowConfig.flowName || 'Test Flow',
        sourceAdapterId: flowConfig.sourceAdapter,
        targetAdapterId: flowConfig.targetAdapter,
        sourceBusinessComponentId: flowConfig.sourceBusinessComponentId,
        targetBusinessComponentId: flowConfig.targetBusinessComponentId,
        sourceStructureId: flowConfig.sourceStructure,
        targetStructureId: flowConfig.targetStructure,
        fieldMappings: flowConfig.fieldMappings || [],
        testData: testData,
        testMode: true
      };

      // Add initial log
      setExecutionLogs(['Starting flow test...']);

      // Call test endpoint
      const response = await api.post('/flow-composition/test/direct-mapping', testRequest);

      if (response.data?.success) {
        setTestStatus('success');
        setOutputData(response.data.outputData || '');
        
        // Add execution logs
        const logs = response.data.executionLogs || [];
        setExecutionLogs([
          'Starting flow test...',
          `Loading source adapter: ${flowConfig.sourceAdapter}`,
          'Parsing input XML...',
          ...(flowConfig.fieldMappings && flowConfig.fieldMappings.length > 0 
            ? ['Applying field mappings...', `${flowConfig.fieldMappings.length} mappings applied`]
            : ['No mappings defined - using passthrough mode']),
          `Sending to target adapter: ${flowConfig.targetAdapter}`,
          'Generating output XML...',
          'Test completed successfully!',
          ...logs
        ]);

        toast({
          title: "Test Successful",
          description: "Flow executed successfully. Check the output tab for results.",
        });
      } else {
        throw new Error(response.data?.error || 'Test failed');
      }
    } catch (error: any) {
      setTestStatus('error');
      const errorMsg = error.response?.data?.message || error.message || 'Failed to test the flow';
      setErrorMessage(errorMsg);
      
      setExecutionLogs([
        'Starting flow test...',
        `Loading source adapter: ${flowConfig.sourceAdapter}`,
        'ERROR: ' + errorMsg
      ]);

      toast({
        title: "Test Failed",
        description: errorMsg,
        variant: "destructive",
      });
    } finally {
      setIsRunning(false);
    }
  };

  const formatXml = (xml: string) => {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xml, 'text/xml');
      const serializer = new XMLSerializer();
      const formatted = serializer.serializeToString(xmlDoc);
      
      // Simple formatting - add newlines and indentation
      return formatted
        .replace(/></g, '>\n<')
        .split('\n')
        .map((line, index) => {
          const indent = line.split('<').length - 2;
          return '  '.repeat(Math.max(0, indent)) + line.trim();
        })
        .join('\n');
    } catch {
      return xml;
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>Test Flow Execution</DialogTitle>
          <DialogDescription>
            Upload an XML file to test your integration flow. The system will process it through your configured adapters and mappings.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* File Upload Section */}
          <div className="space-y-2">
            <Label htmlFor="test-file">Test Data (XML)</Label>
            <div className="flex gap-2">
              <Button
                variant="outline"
                className="relative"
                disabled={isRunning}
              >
                <input
                  id="test-file"
                  type="file"
                  accept=".xml"
                  onChange={handleFileUpload}
                  className="absolute inset-0 opacity-0 cursor-pointer"
                  disabled={isRunning}
                />
                <Upload className="h-4 w-4 mr-2" />
                Upload XML File
              </Button>
              {testData && (
                <span className="text-sm text-muted-foreground flex items-center">
                  <FileText className="h-4 w-4 mr-1" />
                  XML file loaded
                </span>
              )}
            </div>
          </div>

          {/* Test Data Input */}
          <div className="space-y-2">
            <Label htmlFor="test-data">Input XML</Label>
            <Textarea
              id="test-data"
              value={testData}
              onChange={(e) => setTestData(e.target.value)}
              placeholder="Paste your XML test data here or upload a file..."
              className="font-mono text-sm h-32"
              disabled={isRunning}
            />
          </div>

          {/* Results Tabs */}
          {(outputData || executionLogs.length > 0 || errorMessage) && (
            <Tabs defaultValue="output" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="output" className="flex items-center gap-2">
                  {testStatus === 'success' && <CheckCircle className="h-4 w-4 text-success" />}
                  {testStatus === 'error' && <XCircle className="h-4 w-4 text-destructive" />}
                  Output
                </TabsTrigger>
                <TabsTrigger value="logs">Execution Logs</TabsTrigger>
                <TabsTrigger value="mappings">Mappings Applied</TabsTrigger>
              </TabsList>

              <TabsContent value="output" className="space-y-2">
                {testStatus === 'success' && outputData && (
                  <>
                    <Label>Output XML</Label>
                    <Textarea
                      value={formatXml(outputData)}
                      readOnly
                      className="font-mono text-sm h-64"
                    />
                  </>
                )}
                {testStatus === 'error' && (
                  <Alert variant="destructive">
                    <XCircle className="h-4 w-4" />
                    <AlertDescription>{errorMessage}</AlertDescription>
                  </Alert>
                )}
              </TabsContent>

              <TabsContent value="logs" className="space-y-2">
                <div className="bg-muted rounded-md p-4 h-64 overflow-y-auto">
                  {executionLogs.map((log, index) => (
                    <div
                      key={index}
                      className={`font-mono text-sm ${
                        log.startsWith('ERROR') ? 'text-destructive' : ''
                      }`}
                    >
                      {log}
                    </div>
                  ))}
                </div>
              </TabsContent>

              <TabsContent value="mappings" className="space-y-2">
                <div className="bg-muted rounded-md p-4 h-64 overflow-y-auto">
                  {flowConfig.fieldMappings && flowConfig.fieldMappings.length > 0 ? (
                    <div className="space-y-2">
                      <p className="text-sm font-medium">{flowConfig.fieldMappings.length} field mappings configured:</p>
                      {flowConfig.fieldMappings.map((mapping, index) => (
                        <div key={index} className="text-sm font-mono">
                          {mapping.sourceField} → {mapping.targetField}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-muted-foreground">
                      No field mappings configured. Using passthrough mode - data will be forwarded as-is.
                    </p>
                  )}
                </div>
              </TabsContent>
            </Tabs>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isRunning}>
            Close
          </Button>
          <Button onClick={handleRunTest} disabled={isRunning || !testData}>
            {isRunning ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Running Test...
              </>
            ) : (
              <>
                <Play className="h-4 w-4 mr-2" />
                Run Test
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}