// @ts-nocheck
import { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Play, Upload, Download, Copy, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import { FieldMapping } from './types';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/services/api';

interface TestMappingDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  mappings: FieldMapping[];
  mappingName: string;
  mappingType: 'request' | 'response' | 'fault';
  sourceXml?: string;
  targetXml?: string;
}

export function TestMappingDialog({
  open,
  onOpenChange,
  mappings,
  mappingName,
  mappingType,
  sourceXml = '',
  targetXml = ''
}: TestMappingDialogProps) {
  const { toast } = useToast();
  const [inputXml, setInputXml] = useState('');
  const [outputXml, setOutputXml] = useState('');
  const [testStatus, setTestStatus] = useState<'idle' | 'testing' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target?.result as string;
        setInputXml(content);
      };
      reader.readAsText(file);
    }
  };

  const handleTestMapping = async () => {
    if (!inputXml.trim()) {
      toast({
        title: "Input Required",
        description: "Please provide input XML to test the mapping",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);
    setTestStatus('testing');
    setErrorMessage('');
    setOutputXml('');

    try {
      const response = await api.post('/test/field-mappings', {
        inputXml: inputXml,
        mappings: mappings.map(m => ({
          sourceFields: m.sourceFields,
          targetField: m.targetField,
          sourcePaths: m.sourcePaths,
          targetPath: m.targetPath,
          javaFunction: m.functionNode?.functionName === 'visual_flow' ? 'visual_flow' : m.javaFunction,
          functionNode: m.functionNode,
          visualFlowData: m.visualFlowData,
          requiresTransformation: m.requiresTransformation !== false
        })),
        mappingType: mappingType,
        sourceStructureXml: sourceXml,
        targetStructureXml: targetXml
      });

      if (response.data.success) {
        setOutputXml(response.data.outputXml);
        setTestStatus('success');
        toast({
          title: "Test Successful",
          description: "Field mappings were applied successfully",
        });
      } else {
        throw new Error(response.data.error || 'Test failed');
      }
    } catch (error: any) {
      setTestStatus('error');
      const errorMsg = error.response?.data?.message || error.message || 'Failed to test mappings';
      setErrorMessage(errorMsg);
      toast({
        title: "Test Failed",
        description: errorMsg,
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopyOutput = () => {
    navigator.clipboard.writeText(outputXml);
    toast({
      title: "Copied",
      description: "Output XML copied to clipboard",
    });
  };

  const handleDownloadOutput = () => {
    const blob = new Blob([outputXml], { type: 'text/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${mappingName}_${mappingType}_output.xml`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const loadSampleXml = () => {
    // Load sample XML based on the mapping type
    const sampleXml = `<?xml version="1.0" encoding="UTF-8"?>
<${mappingType === 'request' ? 'Request' : mappingType === 'response' ? 'Response' : 'Fault'}>
    <!-- Add your test data here -->
    <Token>sample-token-123</Token>
    <amount>100.00</amount>
    <subClass>A</subClass>
    <tariffIndex>1</tariffIndex>
    <meterBaseDate>2024-01-01</meterBaseDate>
    <keyExpiryNumber>12345</keyExpiryNumber>
    <supplyGroupCode>SG001</supplyGroupCode>
    <keyRevisionNumber>1</keyRevisionNumber>
    <meterSerialNumber>MSN123456</meterSerialNumber>
    <encryptionAlgorithm>AES256</encryptionAlgorithm>
</${mappingType === 'request' ? 'Request' : mappingType === 'response' ? 'Response' : 'Fault'}>`;
    
    setInputXml(sampleXml);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>Test Field Mappings - {mappingName}</DialogTitle>
          <DialogDescription>
            Test your {mappingType} field mappings with sample XML data. Upload or paste your input XML to see the transformation results.
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 overflow-hidden">
          <Tabs defaultValue="test" className="h-full flex flex-col">
            <TabsList>
              <TabsTrigger value="test">Test Mappings</TabsTrigger>
              <TabsTrigger value="mappings">View Mappings ({mappings.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="test" className="flex-1 overflow-auto mt-4">
              <div className="grid grid-cols-2 gap-4 h-full">
                {/* Input Section */}
                <Card>
                  <CardHeader>
                    <CardTitle>Input XML</CardTitle>
                    <CardDescription>
                      Provide the source XML to test the mappings
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={loadSampleXml}
                      >
                        Load Sample
                      </Button>
                      <Label
                        htmlFor="file-upload"
                        className="cursor-pointer"
                      >
                        <Button
                          variant="outline"
                          size="sm"
                          asChild
                        >
                          <span>
                            <Upload className="h-4 w-4 mr-2" />
                            Upload XML
                          </span>
                        </Button>
                      </Label>
                      <input
                        id="file-upload"
                        type="file"
                        accept=".xml"
                        onChange={handleFileUpload}
                        className="hidden"
                      />
                    </div>
                    <Textarea
                      value={inputXml}
                      onChange={(e) => setInputXml(e.target.value)}
                      placeholder="Paste your input XML here..."
                      className="font-mono text-sm h-[400px]"
                    />
                  </CardContent>
                </Card>

                {/* Output Section */}
                <Card>
                  <CardHeader>
                    <CardTitle>Output XML</CardTitle>
                    <CardDescription>
                      Transformed XML after applying field mappings
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {testStatus === 'success' && (
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={handleCopyOutput}
                        >
                          <Copy className="h-4 w-4 mr-2" />
                          Copy
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={handleDownloadOutput}
                        >
                          <Download className="h-4 w-4 mr-2" />
                          Download
                        </Button>
                      </div>
                    )}
                    {testStatus === 'idle' && (
                      <Alert>
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription>
                          Click "Test Mapping" to see the transformation results
                        </AlertDescription>
                      </Alert>
                    )}
                    {testStatus === 'testing' && (
                      <div className="flex items-center justify-center h-[400px]">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                      </div>
                    )}
                    {testStatus === 'error' && (
                      <Alert variant="destructive">
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription>{errorMessage}</AlertDescription>
                      </Alert>
                    )}
                    {testStatus === 'success' && (
                      <Textarea
                        value={outputXml}
                        readOnly
                        className="font-mono text-sm h-[400px]"
                      />
                    )}
                  </CardContent>
                </Card>
              </div>

              <div className="flex justify-center mt-6">
                <Button
                  onClick={handleTestMapping}
                  disabled={isLoading || !inputXml.trim()}
                  size="lg"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Testing...
                    </>
                  ) : (
                    <>
                      <Play className="h-4 w-4 mr-2" />
                      Test Mapping
                    </>
                  )}
                </Button>
              </div>
            </TabsContent>

            <TabsContent value="mappings" className="flex-1 overflow-auto mt-4">
              <div className="space-y-4">
                {mappings.map((mapping, index) => (
                  <Card key={mapping.id}>
                    <CardHeader className="pb-3">
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-sm">{mapping.name}</CardTitle>
                        <Badge variant="outline">Mapping {index + 1}</Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-2">
                      <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                          <Label className="text-muted-foreground">Source Fields</Label>
                          <p className="font-mono">{mapping.sourceFields.join(', ')}</p>
                        </div>
                        <div>
                          <Label className="text-muted-foreground">Target Field</Label>
                          <p className="font-mono">{mapping.targetField}</p>
                        </div>
                      </div>
                      {mapping.functionNode && (
                        <div>
                          <Label className="text-muted-foreground">Transformation</Label>
                          <p className="font-mono text-sm">
                            {mapping.functionNode.functionName}
                          </p>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
}