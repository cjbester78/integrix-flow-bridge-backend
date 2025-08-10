import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { api } from '@/services/api';

interface SoapSenderAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: any) => void;
  businessComponentId?: string;
}

export function SoapSenderAdapterConfiguration({
  configuration,
  onConfigurationChange,
  businessComponentId
}: SoapSenderAdapterConfigurationProps) {
  const [wsdls, setWsdls] = useState<any[]>([]);
  const [loadingWsdls, setLoadingWsdls] = useState(false);
  const [wsdlStructureDetails, setWsdlStructureDetails] = useState<{
    request?: any;
    response?: any;
    fault?: any;
  }>({});

  // Set default processing mode to asynchronous
  useEffect(() => {
    if (!configuration.processingMode) {
      onConfigurationChange('processingMode', 'ASYNCHRONOUS');
    }
  }, []);

  // Fetch WSDLs when business component changes
  useEffect(() => {
    if (businessComponentId) {
      fetchWsdls();
    }
  }, [businessComponentId]);

  // Check WSDL structure and set processing mode
  useEffect(() => {
    if (configuration.selectedWsdl) {
      checkWsdlStructure(configuration.selectedWsdl);
    }
  }, [configuration.selectedWsdl]);

  const fetchWsdls = async () => {
    try {
      setLoadingWsdls(true);
      // Fetch WSDLs as DataStructures filtered by type, usage and business component
      const response = await api.get('/structures', {
        params: {
          type: 'wsdl',
          usage: 'source',
          businessComponentId: businessComponentId,
          limit: 100
        }
      } as any);
      
      if (response.data && response.data.structures) {
        console.log('SOAP Sender - Raw structures from API:', response.data.structures);
        
        // Transform the structures to match our expected format
        // Filter to ensure we only get WSDL type structures with source usage
        const wsdlList = response.data.structures
          .filter((structure: any) => {
            // Must be WSDL type
            const isWsdl = structure.type?.toLowerCase() === 'wsdl';
            
            // Check usage - source for sender adapter
            const isSource = structure.usage === 'source' || 
                           structure.metadata?.usage === 'source' ||
                           !structure.usage; // Include if usage is not set
            
            const included = isWsdl && isSource;
            console.log(`SOAP Sender - Structure ${structure.name}: type=${structure.type}, usage=${structure.usage}, metadata.usage=${structure.metadata?.usage}, included=${included}`);
            return included;
          })
          .map((structure: any) => ({
            id: structure.id,
            name: structure.name,
            endpointUrl: structure.metadata?.endpointUrl || ''
          }));
        
        console.log('SOAP Sender - Filtered WSDL list:', wsdlList);
        setWsdls(wsdlList);
      } else {
        setWsdls([]);
      }
    } catch (error) {
      console.error('Error fetching WSDLs:', error);
      setWsdls([]);
    } finally {
      setLoadingWsdls(false);
    }
  };

  const checkWsdlStructure = async (wsdlId: string) => {
    try {
      // Get the WSDL structure details
      const response = await api.get(`/structures/${wsdlId}`);
      
      if (response.data) {
        const wsdlStructure = response.data;
        console.log('SOAP Sender - WSDL Structure:', wsdlStructure);
        console.log('SOAP Sender - WSDL Metadata:', wsdlStructure.metadata);
        
        // Extract structure details from metadata
        const structureDetails: any = {};
        
        // Extract request structure
        if (wsdlStructure.metadata?.requestStructure || wsdlStructure.metadata?.operationInfo?.request) {
          structureDetails.request = wsdlStructure.metadata?.requestStructure || wsdlStructure.metadata?.operationInfo?.request;
        }
        
        // Extract response structure
        if (wsdlStructure.metadata?.responseStructure || wsdlStructure.metadata?.operationInfo?.response) {
          structureDetails.response = wsdlStructure.metadata?.responseStructure || wsdlStructure.metadata?.operationInfo?.response;
        }
        
        // Extract fault structure
        if (wsdlStructure.metadata?.faultStructure || wsdlStructure.metadata?.operationInfo?.fault) {
          structureDetails.fault = wsdlStructure.metadata?.faultStructure || wsdlStructure.metadata?.operationInfo?.fault;
        }
        
        setWsdlStructureDetails(structureDetails);
        console.log('SOAP Sender - Extracted structures:', structureDetails);
        
        // Check if metadata contains sync/async information
        const hasInput = wsdlStructure.metadata?.hasInput || wsdlStructure.metadata?.operationInfo?.hasInput;
        const hasOutput = wsdlStructure.metadata?.hasOutput || wsdlStructure.metadata?.operationInfo?.hasOutput;
        const hasFault = wsdlStructure.metadata?.hasFault || wsdlStructure.metadata?.operationInfo?.hasFault;
        
        console.log(`SOAP Sender - WSDL Analysis: hasInput=${hasInput}, hasOutput=${hasOutput}, hasFault=${hasFault}`);
        
        if (hasInput && hasOutput && hasFault) {
          // Has Input, Output, and Fault - set to synchronous
          console.log('SOAP Sender - Setting mode to SYNCHRONOUS');
          onConfigurationChange('processingMode', 'SYNCHRONOUS');
        } else {
          // Missing one or more - set to asynchronous
          console.log('SOAP Sender - Setting mode to ASYNCHRONOUS');
          onConfigurationChange('processingMode', 'ASYNCHRONOUS');
        }
      }
    } catch (error) {
      console.error('Error checking WSDL structure:', error);
      // Default to async on error
      onConfigurationChange('processingMode', 'ASYNCHRONOUS');
      setWsdlStructureDetails({});
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>SOAP Sender Configuration</CardTitle>
        <CardDescription>Configure your SOAP sender adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="source" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="source">Source</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
          </TabsList>

          <TabsContent value="source" className="space-y-6">
            {/* Endpoint Information Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Endpoint Information</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="service-endpoint">Endpoint URL</Label>
                  <Input
                    id="service-endpoint"
                    value={configuration.serviceEndpointUrl || ''}
                    onChange={(e) => onConfigurationChange('serviceEndpointUrl', e.target.value)}
                    placeholder="https://api.yourdomain.com/soap/service"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="soap-action">SOAP Action</Label>
                  <Input
                    id="soap-action"
                    value={configuration.soapAction || ''}
                    onChange={(e) => onConfigurationChange('soapAction', e.target.value)}
                    placeholder="urn:getOrderDetails"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="content-type">Content-Type</Label>
                  <Select
                    value={configuration.contentType || ''}
                    onValueChange={(value) => onConfigurationChange('contentType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select content type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="text/xml">text/xml</SelectItem>
                      <SelectItem value="application/soap+xml">application/soap+xml</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="soap-version">SOAP Version</Label>
                  <Select
                    value={configuration.soapVersion || ''}
                    onValueChange={(value) => onConfigurationChange('soapVersion', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select SOAP version" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="1.1">SOAP 1.1</SelectItem>
                      <SelectItem value="1.2">SOAP 1.2</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* Source Message Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Source Message</h3>
              <div className="space-y-2">
                <Label htmlFor="source-wsdl">Select WSDL</Label>
                <Select
                  value={configuration.selectedWsdl || ''}
                  onValueChange={(value) => onConfigurationChange('selectedWsdl', value)}
                  disabled={loadingWsdls || !businessComponentId}
                >
                  <SelectTrigger>
                    <SelectValue placeholder={
                      !businessComponentId 
                        ? "Select a business component first" 
                        : loadingWsdls 
                        ? "Loading WSDLs..." 
                        : "Select a WSDL"
                    } />
                  </SelectTrigger>
                  <SelectContent>
                    {wsdls.map((wsdl) => (
                      <SelectItem key={wsdl.id} value={wsdl.id}>
                        {wsdl.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="processing" className="space-y-6">
            {/* Processing Mode Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Processing Mode</h3>
              
              <div className="flex items-center justify-between space-x-4 p-4 border rounded-lg">
                <div className="space-y-1">
                  <Label htmlFor="processingMode" className="text-sm font-medium">
                    {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' 
                      ? 'Asynchronous Processing' 
                      : 'Synchronous Processing'
                    }
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' 
                      ? 'Return immediate acknowledgment (HTTP 202), process in background'
                      : 'Wait for complete processing before sending response back to caller'
                    }
                  </p>
                </div>
                <Switch
                  id="processingMode"
                  checked={(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS'}
                  onCheckedChange={(checked) => 
                    onConfigurationChange('processingMode', checked ? 'ASYNCHRONOUS' : 'SYNCHRONOUS')
                  }
                />
              </div>

              {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' && (
                <div className="space-y-4 pl-4 border-l-2 border-muted">
                  <div className="space-y-2">
                    <Label htmlFor="asyncResponseTimeout">Async Response Timeout (ms)</Label>
                    <Input
                      id="asyncResponseTimeout"
                      type="number"
                      placeholder="30000"
                      value={configuration.asyncResponseTimeout || ''}
                      onChange={(e) => onConfigurationChange('asyncResponseTimeout', parseInt(e.target.value) || 30000)}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="asyncResponseFormat">Async Response Format</Label>
                    <Select
                      value={configuration.asyncResponseFormat || 'HTTP_202'}
                      onValueChange={(value) => onConfigurationChange('asyncResponseFormat', value)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select response format" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="HTTP_202">HTTP 202 Accepted</SelectItem>
                        <SelectItem value="CUSTOM_RESPONSE">Custom Response</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="asyncCallbackUrl">Callback URL (Optional)</Label>
                    <Input
                      id="asyncCallbackUrl"
                      type="url"
                      placeholder="https://callback-endpoint.example.com/webhook"
                      value={configuration.asyncCallbackUrl || ''}
                      onChange={(e) => onConfigurationChange('asyncCallbackUrl', e.target.value)}
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Response Format and Error Handling Section - Only show in ASYNC mode */}
            {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' && (
              <div className="space-y-4">
                <h3 className="text-lg font-medium">Response Format and Error Handling</h3>
                <div className="grid grid-cols-1 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="response-message">Response Message</Label>
                    <Textarea
                      id="response-message"
                      value={configuration.responseMessage || ''}
                      onChange={(e) => onConfigurationChange('responseMessage', e.target.value)}
                      placeholder="Expected SOAP response XML schema"
                      rows={4}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="error-handling">Error Handling</Label>
                    <Textarea
                      id="error-handling"
                      value={configuration.errorHandling || ''}
                      onChange={(e) => onConfigurationChange('errorHandling', e.target.value)}
                      placeholder="Expected SOAP fault codes and error handling"
                      rows={3}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="timeout">Timeout Settings (seconds)</Label>
                    <Input
                      id="timeout"
                      type="number"
                      value={configuration.timeout || ''}
                      onChange={(e) => onConfigurationChange('timeout', e.target.value)}
                      placeholder="60"
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Custom Headers Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Custom Headers</h3>
              <div className="space-y-2">
                <Label htmlFor="custom-headers">Custom SOAP Headers</Label>
                <Textarea
                  id="custom-headers"
                  value={configuration.customHeaders || ''}
                  onChange={(e) => onConfigurationChange('customHeaders', e.target.value)}
                  placeholder="Additional SOAP headers in XML format (e.g., WS-Addressing, Custom security headers)"
                  rows={4}
                />
                <p className="text-sm text-muted-foreground">
                  Enter custom SOAP headers that will be included in the request envelope
                </p>
              </div>
            </div>

            {/* Additional Settings Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Additional Settings</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="retry-count">Retry Count</Label>
                  <Input
                    id="retry-count"
                    type="number"
                    value={configuration.retryCount || ''}
                    onChange={(e) => onConfigurationChange('retryCount', e.target.value)}
                    placeholder="3"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="retry-interval">Retry Interval (seconds)</Label>
                  <Input
                    id="retry-interval"
                    type="number"
                    value={configuration.retryInterval || ''}
                    onChange={(e) => onConfigurationChange('retryInterval', e.target.value)}
                    placeholder="5"
                  />
                </div>
              </div>
            </div>

            {/* WSDL Structure Details Section */}
            {configuration.selectedWsdl && (
              <div className="space-y-4">
                <h3 className="text-lg font-medium">WSDL Structure Details</h3>
                
                {/* Request Structure - Always shown */}
                {wsdlStructureDetails.request && (
                  <div className="space-y-2">
                    <Label>Request Structure</Label>
                    <div className="bg-muted p-4 rounded-md">
                      <pre className="text-sm overflow-auto max-h-40">
                        {typeof wsdlStructureDetails.request === 'string' 
                          ? wsdlStructureDetails.request 
                          : JSON.stringify(wsdlStructureDetails.request, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
                
                {/* Response Structure - Only in SYNCHRONOUS mode */}
                {(configuration.processingMode || 'ASYNCHRONOUS') === 'SYNCHRONOUS' && wsdlStructureDetails.response && (
                  <div className="space-y-2">
                    <Label>Response Structure</Label>
                    <div className="bg-muted p-4 rounded-md">
                      <pre className="text-sm overflow-auto max-h-40">
                        {typeof wsdlStructureDetails.response === 'string' 
                          ? wsdlStructureDetails.response 
                          : JSON.stringify(wsdlStructureDetails.response, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
                
                {/* Fault Structure - Always shown */}
                {wsdlStructureDetails.fault && (
                  <div className="space-y-2">
                    <Label>Fault Structure</Label>
                    <div className="bg-muted p-4 rounded-md">
                      <pre className="text-sm overflow-auto max-h-40">
                        {typeof wsdlStructureDetails.fault === 'string' 
                          ? wsdlStructureDetails.fault 
                          : JSON.stringify(wsdlStructureDetails.fault, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
                
                {/* If no structures found */}
                {!wsdlStructureDetails.request && !wsdlStructureDetails.response && !wsdlStructureDetails.fault && (
                  <p className="text-sm text-muted-foreground">No structure details available in WSDL metadata</p>
                )}
              </div>
            )}
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}