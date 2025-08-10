import { useState, useEffect } from 'react';
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
import { Input } from '@/components/ui/input';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, Download, Eye } from 'lucide-react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { toast } from '@/hooks/use-toast';

interface WSDLGeneratorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface DataStructure {
  id: string;
  name: string;
  description?: string;
  format: string;
  createdAt: string;
}

interface WsdlGenerationRequest {
  mode: 'SOURCE' | 'TARGET';
  type: 'SYNCHRONOUS' | 'ASYNCHRONOUS';
  requestStructureId: string;
  responseStructureId?: string;
  faultStructureId?: string;
  serviceName: string;
  namespace: string;
  operationName: string;
}

interface WsdlGenerationResponse {
  wsdlId: string;
  wsdlContent: string;
  createdStructures: Array<{
    id: string;
    name: string;
    type: string;
  }>;
  downloadUrl: string;
}

export function WSDLGeneratorModal({ isOpen, onClose }: WSDLGeneratorModalProps) {
  const [mode, setMode] = useState<'SOURCE' | 'TARGET'>('SOURCE');
  const [type, setType] = useState<'SYNCHRONOUS' | 'ASYNCHRONOUS'>('SYNCHRONOUS');
  const [requestStructureId, setRequestStructureId] = useState('');
  const [responseStructureId, setResponseStructureId] = useState('');
  const [faultStructureId, setFaultStructureId] = useState('');
  const [serviceName, setServiceName] = useState('');
  const [namespace, setNamespace] = useState('');
  const [operationName, setOperationName] = useState('');
  const [generatedWsdl, setGeneratedWsdl] = useState<WsdlGenerationResponse | null>(null);
  const [previewContent, setPreviewContent] = useState('');
  const [showPreview, setShowPreview] = useState(false);

  // Fetch XML/XSD data structures
  const { data: structures = [], isLoading: structuresLoading } = useQuery({
    queryKey: ['xml-data-structures'],
    queryFn: async () => {
      const response = await apiClient.get<DataStructure[]>('/wsdl/structures/xml');
      return response;
    },
    enabled: isOpen,
  });

  // Generate WSDL mutation
  const generateMutation = useMutation({
    mutationFn: async (request: WsdlGenerationRequest) => {
      const response = await apiClient.post<WsdlGenerationResponse>('/wsdl/generate', request);
      return response;
    },
    onSuccess: (data) => {
      setGeneratedWsdl(data);
      toast({ title: "Success", description: 'WSDL generated successfully' });
    },
    onError: (error: any) => {
      toast({ title: "Error", description: error.response?.data?.message || 'Failed to generate WSDL', variant: "destructive" });
    },
  });

  // Note: validateMutation removed as it was unused

  // Extract namespace from selected request structure
  useEffect(() => {
    if (requestStructureId) {
      const structure = structures.find((s: any) => s.id === requestStructureId);
      if (structure) {
        // Try to extract namespace from structure content
        // This would be done server-side in real implementation
        // For now, we'll leave it to the user to fill
      }
    }
  }, [requestStructureId, structures]);

  const handleGenerate = async () => {
    // Validate required fields
    if (!serviceName || !namespace || !operationName || !requestStructureId) {
      toast({ title: "Error", description: 'Please fill all required fields', variant: "destructive" });
      return;
    }

    if (type === 'SYNCHRONOUS' && !responseStructureId) {
      toast({ title: "Error", description: 'Response structure is required for synchronous WSDL', variant: "destructive" });
      return;
    }

    const request: WsdlGenerationRequest = {
      mode,
      type,
      requestStructureId,
      responseStructureId: type === 'SYNCHRONOUS' ? responseStructureId : undefined,
      faultStructureId: faultStructureId || undefined,
      serviceName,
      namespace,
      operationName,
    };

    generateMutation.mutate(request);
  };

  const handlePreview = async () => {
    if (!generatedWsdl) return;
    
    try {
      const response = await apiClient.get(`/wsdl/preview/${generatedWsdl.wsdlId}`, {
        responseType: 'text',
      });
      setPreviewContent((response as any).data || '');
      setShowPreview(true);
    } catch (error) {
      toast({ title: "Error", description: 'Failed to preview WSDL', variant: "destructive" });
    }
  };

  const handleDownload = () => {
    if (!generatedWsdl) return;
    
    // Create a download link
    const link = document.createElement('a');
    link.href = `/api${generatedWsdl.downloadUrl}`;
    link.download = `${serviceName}.wsdl`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleClose = () => {
    // Reset form
    setMode('SOURCE');
    setType('SYNCHRONOUS');
    setRequestStructureId('');
    setResponseStructureId('');
    setFaultStructureId('');
    setServiceName('');
    setNamespace('');
    setOperationName('');
    setGeneratedWsdl(null);
    setPreviewContent('');
    setShowPreview(false);
    onClose();
  };

  return (
    <>
      <Dialog open={isOpen && !showPreview} onOpenChange={handleClose}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Generate WSDL</DialogTitle>
            <DialogDescription>
              Generate a WSDL 1.1 file from existing XML/XSD data structures
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            {/* Mode Selection */}
            <div className="space-y-2">
              <Label>WSDL Mode</Label>
              <RadioGroup value={mode} onValueChange={(value) => setMode(value as any)}>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="SOURCE" id="source" />
                  <Label htmlFor="source">Source (Outbound service)</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="TARGET" id="target" />
                  <Label htmlFor="target">Target (Inbound service)</Label>
                </div>
              </RadioGroup>
            </div>

            {/* Type Selection */}
            <div className="space-y-2">
              <Label>WSDL Type</Label>
              <RadioGroup value={type} onValueChange={(value) => setType(value as any)}>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="SYNCHRONOUS" id="sync" />
                  <Label htmlFor="sync">Synchronous (Request/Response)</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="ASYNCHRONOUS" id="async" />
                  <Label htmlFor="async">Asynchronous (Request only)</Label>
                </div>
              </RadioGroup>
            </div>

            {/* Data Structure Selection */}
            <div className="space-y-2">
              <Label htmlFor="request">Request Structure *</Label>
              <Select value={requestStructureId} onValueChange={setRequestStructureId}>
                <SelectTrigger id="request">
                  <SelectValue placeholder="Select request structure" />
                </SelectTrigger>
                <SelectContent>
                  {structures.map((structure: any) => (
                    <SelectItem key={structure.id} value={structure.id}>
                      {structure.name} {structure.description && `- ${structure.description}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {type === 'SYNCHRONOUS' && (
              <div className="space-y-2">
                <Label htmlFor="response">Response Structure *</Label>
                <Select value={responseStructureId} onValueChange={setResponseStructureId}>
                  <SelectTrigger id="response">
                    <SelectValue placeholder="Select response structure" />
                  </SelectTrigger>
                  <SelectContent>
                    {structures.map((structure: any) => (
                      <SelectItem key={structure.id} value={structure.id}>
                        {structure.name} {structure.description && `- ${structure.description}`}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="fault">Fault Structure (Optional)</Label>
              <Select value={faultStructureId} onValueChange={setFaultStructureId}>
                <SelectTrigger id="fault">
                  <SelectValue placeholder="Select fault structure (optional)" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">None</SelectItem>
                  {structures.map((structure: any) => (
                    <SelectItem key={structure.id} value={structure.id}>
                      {structure.name} {structure.description && `- ${structure.description}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Service Details */}
            <div className="space-y-2">
              <Label htmlFor="service-name">Service Name *</Label>
              <Input
                id="service-name"
                value={serviceName}
                onChange={(e) => setServiceName(e.target.value)}
                placeholder="e.g., CustomerService"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="namespace">Namespace *</Label>
              <Input
                id="namespace"
                value={namespace}
                onChange={(e) => setNamespace(e.target.value)}
                placeholder="e.g., http://example.com/services/customer"
              />
              {!namespace && requestStructureId && (
                <p className="text-sm text-muted-foreground">
                  Enter namespace or it will be extracted from request structure if available
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="operation">Operation Name *</Label>
              <Input
                id="operation"
                value={operationName}
                onChange={(e) => setOperationName(e.target.value)}
                placeholder="e.g., getCustomer"
              />
            </div>

            {generatedWsdl && (
              <Alert>
                <AlertDescription>
                  WSDL generated successfully! {generatedWsdl.createdStructures.length} data structures were created.
                </AlertDescription>
              </Alert>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            {generatedWsdl ? (
              <>
                <Button variant="outline" onClick={handlePreview}>
                  <Eye className="h-4 w-4 mr-2" />
                  Preview
                </Button>
                <Button onClick={handleDownload}>
                  <Download className="h-4 w-4 mr-2" />
                  Download
                </Button>
              </>
            ) : (
              <Button 
                onClick={handleGenerate} 
                disabled={generateMutation.isPending || structuresLoading}
              >
                {generateMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                Generate WSDL
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Preview Dialog */}
      <Dialog open={showPreview} onOpenChange={setShowPreview}>
        <DialogContent className="max-w-4xl max-h-[80vh]">
          <DialogHeader>
            <DialogTitle>WSDL Preview</DialogTitle>
          </DialogHeader>
          <div className="overflow-auto">
            <pre className="text-sm bg-gray-100 p-4 rounded">{previewContent}</pre>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowPreview(false)}>
              Close
            </Button>
            <Button onClick={handleDownload}>
              <Download className="h-4 w-4 mr-2" />
              Download
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}