// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { useToast } from '@/hooks/use-toast';
import { flowService } from '@/services/flowService';
import { 
  Loader2, 
  Copy, 
  Download, 
  Globe, 
  Clock, 
  User,
  FileText,
  Link,
  CheckCircle
} from 'lucide-react';
import { DeploymentInfo } from '@/types/deployment';
import { IntegrationFlow } from '@/types/flow';

export default function InterfaceDetails() {
  const { flowId } = useParams<{ flowId: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [loading, setLoading] = useState(true);
  const [flow, setFlow] = useState<IntegrationFlow | null>(null);
  const [deploymentInfo, setDeploymentInfo] = useState<DeploymentInfo | null>(null);

  useEffect(() => {
    if (flowId) {
      fetchFlowAndDeploymentInfo();
    }
  }, [flowId]);

  const fetchFlowAndDeploymentInfo = async () => {
    try {
      setLoading(true);
      
      // Fetch flow details - this contains all the data we need
      const flowResponse = await flowService.getFlow(flowId!);
      if (flowResponse.success && flowResponse.data) {
        setFlow(flowResponse.data);
        
        // Create deployment info from flow data
        const mockDeploymentInfo = {
          flowId: flowResponse.data.id,
          status: flowResponse.data.status,
          endpoints: [],
          metadata: {}
        };
        setDeploymentInfo(mockDeploymentInfo);
      }
    } catch (error) {
      toast({
        variant: 'destructive',
        title: 'Error',
        description: 'Failed to fetch interface details',
      });
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast({
      title: 'Copied!',
      description: `${label} copied to clipboard`,
    });
  };

  const downloadWsdl = () => {
    if (deploymentInfo?.metadata?.wsdlUrl) {
      window.open(deploymentInfo.metadata.wsdlUrl, '_blank');
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex justify-center items-center p-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      </div>
    );
  }

  if (!flow || !deploymentInfo) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="p-12 text-center">
            <p className="text-muted-foreground">Interface not found or not deployed</p>
            <Button 
              variant="outline" 
              className="mt-4"
              onClick={() => navigate('/interfaces')}
            >
              Back to Interfaces
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const adapterType = deploymentInfo.metadata?.adapterType || 'Unknown';
  const isHttpBased = ['HTTP', 'REST', 'SOAP'].includes(adapterType);
  const isSoap = adapterType === 'SOAP';

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-3xl font-bold">{flow.name}</h1>
          <Badge variant="success" className="text-lg px-4 py-2">
            <CheckCircle className="h-4 w-4 mr-2" />
            Deployed
          </Badge>
        </div>
        <p className="text-gray-600">{flow.description}</p>
      </div>

      <div className="grid gap-6">
        {/* Deployment Information */}
        <Card>
          <CardHeader>
            <CardTitle>Deployment Information</CardTitle>
            <CardDescription>
              Details about the deployed interface
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-sm text-muted-foreground">Deployed At</Label>
                <div className="flex items-center gap-2 mt-1">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <span>{new Date(deploymentInfo.deployedAt).toLocaleString()}</span>
                </div>
              </div>
              <div>
                <Label className="text-sm text-muted-foreground">Deployed By</Label>
                <div className="flex items-center gap-2 mt-1">
                  <User className="h-4 w-4 text-muted-foreground" />
                  <span>{deploymentInfo.deployedBy || 'System'}</span>
                </div>
              </div>
            </div>
            
            <Separator />
            
            <div className="space-y-2">
              <Label className="text-sm text-muted-foreground">Adapter Type</Label>
              <Badge variant="outline">{adapterType}</Badge>
            </div>
          </CardContent>
        </Card>

        {/* Endpoint Information */}
        {isHttpBased && (
          <Card>
            <CardHeader>
              <CardTitle>Endpoint Information</CardTitle>
              <CardDescription>
                Connection details for external systems
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Main Endpoint */}
              <div className="space-y-2">
                <Label htmlFor="endpoint">API Endpoint</Label>
                <div className="flex gap-2">
                  <Input
                    id="endpoint"
                    value={deploymentInfo.endpoint}
                    readOnly
                    className="font-mono text-sm"
                  />
                  <Button
                    size="icon"
                    variant="outline"
                    onClick={() => copyToClipboard(deploymentInfo.endpoint, 'Endpoint')}
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              {/* SOAP WSDL */}
              {isSoap && deploymentInfo.metadata?.wsdlUrl && (
                <div className="space-y-2">
                  <Label htmlFor="wsdl">WSDL URL</Label>
                  <div className="flex gap-2">
                    <Input
                      id="wsdl"
                      value={deploymentInfo.metadata.wsdlUrl}
                      readOnly
                      className="font-mono text-sm"
                    />
                    <Button
                      size="icon"
                      variant="outline"
                      onClick={() => copyToClipboard(deploymentInfo.metadata.wsdlUrl, 'WSDL URL')}
                    >
                      <Copy className="h-4 w-4" />
                    </Button>
                    <Button
                      size="icon"
                      variant="outline"
                      onClick={downloadWsdl}
                    >
                      <Download className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              )}

              {/* REST API Docs */}
              {adapterType === 'REST' && deploymentInfo.metadata?.apiDocsUrl && (
                <div className="space-y-2">
                  <Label htmlFor="apidocs">API Documentation</Label>
                  <div className="flex gap-2">
                    <Input
                      id="apidocs"
                      value={deploymentInfo.metadata.apiDocsUrl}
                      readOnly
                      className="font-mono text-sm"
                    />
                    <Button
                      size="icon"
                      variant="outline"
                      onClick={() => window.open(deploymentInfo.metadata.apiDocsUrl, '_blank')}
                    >
                      <Globe className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              )}

              {/* HTTP Methods */}
              {deploymentInfo.metadata?.httpMethods && (
                <div className="space-y-2">
                  <Label>Supported Methods</Label>
                  <p className="text-sm text-muted-foreground">
                    {deploymentInfo.metadata.httpMethods}
                  </p>
                </div>
              )}

              {/* Content Types */}
              {deploymentInfo.metadata?.contentType && (
                <div className="space-y-2">
                  <Label>Content Types</Label>
                  <p className="text-sm text-muted-foreground">
                    {deploymentInfo.metadata.contentType}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* File-based Configuration */}
        {['FILE', 'FTP', 'SFTP'].includes(adapterType) && (
          <Card>
            <CardHeader>
              <CardTitle>File Configuration</CardTitle>
              <CardDescription>
                File handling settings for this interface
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm text-muted-foreground">Polling</Label>
                  <p className="mt-1">
                    {deploymentInfo.metadata?.pollingEnabled ? 'Enabled' : 'Disabled'}
                  </p>
                </div>
                <div>
                  <Label className="text-sm text-muted-foreground">File Pattern</Label>
                  <p className="mt-1 font-mono text-sm">
                    {deploymentInfo.metadata?.filePattern || '*.*'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Integration Details */}
        <Card>
          <CardHeader>
            <CardTitle>Integration Details</CardTitle>
            <CardDescription>
              Configuration and mapping information
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-sm text-muted-foreground">Mapping Mode</Label>
                <Badge variant={flow.mappingMode === 'PASS_THROUGH' ? 'outline' : 'default'}>
                  {flow.mappingMode === 'PASS_THROUGH' ? 'Pass-through' : 'With Mapping'}
                </Badge>
              </div>
              <div>
                <Label className="text-sm text-muted-foreground">Status</Label>
                <Badge variant="success">{flow.status}</Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex justify-end gap-2">
          <Button 
            variant="outline"
            onClick={() => navigate('/interfaces')}
          >
            Back to Interfaces
          </Button>
          <Button 
            variant="outline"
            onClick={() => navigate(`/flows/${flowId}/edit`)}
          >
            <FileText className="h-4 w-4 mr-2" />
            View Configuration
          </Button>
        </div>
      </div>
    </div>
  );
}