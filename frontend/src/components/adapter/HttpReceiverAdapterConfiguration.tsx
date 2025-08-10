import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Switch } from '@/components/ui/switch';
import { CertificateSelection } from '@/components/ui/certificate-selection';
import { XmlConversionTab } from '../adapters/XmlConversionTab';
import { TransformationToggle, TransformationConfig } from '../adapters/TransformationToggle';
import { PayloadStructureDefinition, PayloadStructureConfig } from '../adapters/PayloadStructureDefinition';

interface HttpReceiverAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: string | number | boolean | TransformationConfig | PayloadStructureConfig | any) => void;
  businessComponentId?: string;
}

export function HttpReceiverAdapterConfiguration({
  configuration,
  onConfigurationChange,
  businessComponentId
}: HttpReceiverAdapterConfigurationProps) {
  // Initialize transformation and payload configs
  const [transformationConfig, setTransformationConfig] = useState<TransformationConfig>(
    configuration.transformationConfig || { mode: 'transform', requiresStructure: true }
  );
  const [payloadConfig, setPayloadConfig] = useState<PayloadStructureConfig>(
    configuration.payloadStructureConfig || { format: 'JSON' }
  );
  
  // Handle transformation config changes
  const handleTransformationChange = (config: TransformationConfig) => {
    setTransformationConfig(config);
    onConfigurationChange('transformationConfig', config);
  };
  
  // Handle payload config changes
  const handlePayloadChange = (config: PayloadStructureConfig) => {
    setPayloadConfig(config);
    onConfigurationChange('payloadStructureConfig', config);
  };
  
  // Set default processing mode to asynchronous
  useEffect(() => {
    if (!configuration.processingMode) {
      onConfigurationChange('processingMode', 'ASYNCHRONOUS');
    }
  }, []);

  return (
    <Card>
      <CardHeader>
        <CardTitle>HTTP Receiver Configuration</CardTitle>
        <CardDescription>Configure your HTTP receiver adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="target" className="w-full">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="target">Target</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
            <TabsTrigger value="transformation">Transformation</TabsTrigger>
            <TabsTrigger value="payload" disabled={transformationConfig.mode !== 'transform'}>
              Payload Structure
            </TabsTrigger>
            <TabsTrigger value="xmlConversion" disabled={transformationConfig.mode !== 'transform'}>
              XML Conversion
            </TabsTrigger>
          </TabsList>

          <TabsContent value="target" className="space-y-6">
            {/* Connection Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Connection</h3>
              
              <div className="space-y-2">
                <Label htmlFor="targetEndpointUrl">Target Endpoint URL</Label>
                <Input
                  id="targetEndpointUrl"
                  type="text"
                  placeholder="Full URL of the 3rd party API endpoint"
                  value={configuration.targetEndpointUrl || ''}
                  onChange={(e) => onConfigurationChange('targetEndpointUrl', e.target.value)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="httpMethod">HTTP Method</Label>
                <Select
                  value={configuration.httpMethod || ''}
                  onValueChange={(value) => onConfigurationChange('httpMethod', value)}
                >
                  <SelectTrigger className="bg-background">
                    <SelectValue placeholder="Select HTTP method" />
                  </SelectTrigger>
                  <SelectContent className="bg-background border z-50">
                    <SelectItem value="GET">GET</SelectItem>
                    <SelectItem value="POST">POST</SelectItem>
                    <SelectItem value="PUT">PUT</SelectItem>
                    <SelectItem value="DELETE">DELETE</SelectItem>
                    <SelectItem value="PATCH">PATCH</SelectItem>
                    <SelectItem value="HEAD">HEAD</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="connectionTimeout">Connection Timeout</Label>
                <Input
                  id="connectionTimeout"
                  type="number"
                  placeholder="Max time to wait to establish connection (seconds)"
                  value={configuration.connectionTimeout || ''}
                  onChange={(e) => onConfigurationChange('connectionTimeout', parseInt(e.target.value) || 0)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="readTimeout">Read Timeout</Label>
                <Input
                  id="readTimeout"
                  type="number"
                  placeholder="Max time to wait for response (seconds)"
                  value={configuration.readTimeout || ''}
                  onChange={(e) => onConfigurationChange('readTimeout', parseInt(e.target.value) || 0)}
                />
              </div>

              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="useProxy"
                    checked={configuration.useProxy || false}
                    onCheckedChange={(checked) => onConfigurationChange('useProxy', checked)}
                  />
                  <Label htmlFor="useProxy">Use Proxy Settings</Label>
                </div>

                {configuration.useProxy && (
                  <div className="grid grid-cols-2 gap-4 ml-6">
                    <div className="space-y-2">
                      <Label htmlFor="proxyServer">Server</Label>
                      <Input
                        id="proxyServer"
                        type="text"
                        placeholder="Proxy server address"
                        value={configuration.proxyServer || ''}
                        onChange={(e) => onConfigurationChange('proxyServer', e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="proxyPort">Port</Label>
                      <Input
                        id="proxyPort"
                        type="number"
                        placeholder="Proxy port"
                        value={configuration.proxyPort || ''}
                        onChange={(e) => onConfigurationChange('proxyPort', parseInt(e.target.value) || 0)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="proxyUsername">User Name</Label>
                      <Input
                        id="proxyUsername"
                        type="text"
                        placeholder="Proxy username"
                        value={configuration.proxyUsername || ''}
                        onChange={(e) => onConfigurationChange('proxyUsername', e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="proxyPassword">Password</Label>
                      <Input
                        id="proxyPassword"
                        type="password"
                        placeholder="Proxy password"
                        value={configuration.proxyPassword || ''}
                        onChange={(e) => onConfigurationChange('proxyPassword', e.target.value)}
                      />
                    </div>
                  </div>
                )}
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
                      ? 'Fire-and-forget: don\'t wait for response from target system'
                      : 'Wait for complete response from target system before continuing flow'
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

              {(configuration.processingMode || 'ASYNCHRONOUS') === 'SYNCHRONOUS' && (
                <div className="space-y-4 pl-4 border-l-2 border-muted">
                  <div className="space-y-2">
                    <Label htmlFor="responseTimeout">Response Timeout (ms)</Label>
                    <Input
                      id="responseTimeout"
                      type="number"
                      placeholder="30000"
                      value={configuration.responseTimeout || ''}
                      onChange={(e) => onConfigurationChange('responseTimeout', parseInt(e.target.value) || 30000)}
                    />
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="waitForResponse"
                      checked={configuration.waitForResponse !== false}
                      onCheckedChange={(checked) => onConfigurationChange('waitForResponse', checked)}
                    />
                    <Label htmlFor="waitForResponse" className="text-sm">
                      Wait for response in synchronous mode
                    </Label>
                  </div>
                </div>
              )}

              {configuration.processingMode === 'ASYNCHRONOUS' && (
                <div className="space-y-4 pl-4 border-l-2 border-muted">
                  <div className="space-y-2">
                    <Label htmlFor="asyncCorrelationId">Correlation ID (Optional)</Label>
                    <Input
                      id="asyncCorrelationId"
                      type="text"
                      placeholder="Unique identifier for async processing"
                      value={configuration.asyncCorrelationId || ''}
                      onChange={(e) => onConfigurationChange('asyncCorrelationId', e.target.value)}
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Security Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Security</h3>
              
              <div className="space-y-2">
                <Label htmlFor="authenticationType">Authentication Type</Label>
                <Select
                  value={configuration.authenticationType || ''}
                  onValueChange={(value) => onConfigurationChange('authenticationType', value)}
                >
                  <SelectTrigger className="bg-background">
                    <SelectValue placeholder="Select authentication method" />
                  </SelectTrigger>
                  <SelectContent className="bg-background border z-50">
                    <SelectItem value="none">None</SelectItem>
                    <SelectItem value="basic">Basic</SelectItem>
                    <SelectItem value="oauth2-client-credentials">OAuth2 Client Credentials</SelectItem>
                    <SelectItem value="client-certificate">Client Certificate</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {configuration.authenticationType === 'basic' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="basicUsername">Username</Label>
                    <Input
                      id="basicUsername"
                      type="text"
                      placeholder="Basic auth username"
                      value={configuration.basicUsername || ''}
                      onChange={(e) => onConfigurationChange('basicUsername', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="basicPassword">Password</Label>
                    <Input
                      id="basicPassword"
                      type="password"
                      placeholder="Basic auth password"
                      value={configuration.basicPassword || ''}
                      onChange={(e) => onConfigurationChange('basicPassword', e.target.value)}
                    />
                  </div>
                </>
              )}

              {configuration.authenticationType === 'oauth2-client-credentials' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="clientId">Client ID</Label>
                    <Input
                      id="clientId"
                      type="text"
                      placeholder="OAuth2 client ID"
                      value={configuration.clientId || ''}
                      onChange={(e) => onConfigurationChange('clientId', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="clientSecret">Client Secret</Label>
                    <Input
                      id="clientSecret"
                      type="password"
                      placeholder="OAuth2 client secret"
                      value={configuration.clientSecret || ''}
                      onChange={(e) => onConfigurationChange('clientSecret', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="tokenUrl">Token URL</Label>
                    <Input
                      id="tokenUrl"
                      type="text"
                      placeholder="OAuth2 token endpoint URL"
                      value={configuration.tokenUrl || ''}
                      onChange={(e) => onConfigurationChange('tokenUrl', e.target.value)}
                    />
                  </div>
                </>
              )}

              {configuration.authenticationType === 'client-certificate' && (
                <div className="space-y-4">
                  <CertificateSelection
                    id="certificate"
                    label="Client Certificate"
                    value={configuration.certificateId || ''}
                    onChange={(value) => onConfigurationChange('certificateId', value)}
                    businessComponentId={businessComponentId}
                    placeholder="Select client certificate"
                    required
                  />
                </div>
              )}
            </div>

            {/* Request Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Request</h3>
              
              <div className="space-y-2">
                <Label htmlFor="requestHeaders">Request Headers</Label>
                <Textarea
                  id="requestHeaders"
                  placeholder="Headers to send with the HTTP request"
                  value={configuration.requestHeaders || ''}
                  onChange={(e) => onConfigurationChange('requestHeaders', e.target.value)}
                  className="min-h-[80px]"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="requestPayloadFormat">Request Payload Format</Label>
                <Textarea
                  id="requestPayloadFormat"
                  placeholder="Expected format/schema for request body"
                  value={configuration.requestPayloadFormat || ''}
                  onChange={(e) => onConfigurationChange('requestPayloadFormat', e.target.value)}
                  className="min-h-[80px]"
                />
              </div>
            </div>

            {/* Response Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Response</h3>
              
              <div className="space-y-2">
                <Label htmlFor="responseHandling">Response Handling</Label>
                <Textarea
                  id="responseHandling"
                  placeholder="Expected response format and handling logic"
                  value={configuration.responseHandling || ''}
                  onChange={(e) => onConfigurationChange('responseHandling', e.target.value)}
                  className="min-h-[80px]"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="retryPolicy">Retry Policy</Label>
                <Textarea
                  id="retryPolicy"
                  placeholder="Rules for retrying failed requests"
                  value={configuration.retryPolicy || ''}
                  onChange={(e) => onConfigurationChange('retryPolicy', e.target.value)}
                  className="min-h-[80px]"
                />
              </div>
            </div>
          </TabsContent>
          
          {/* Transformation Tab */}
          <TabsContent value="transformation" className="mt-6">
            <TransformationToggle
              config={transformationConfig}
              onChange={handleTransformationChange}
              adapterType="HTTP"
              disabled={false}
            />
          </TabsContent>

          {/* Payload Structure Tab - Only show when transformation is enabled */}
          {transformationConfig.mode === 'transform' && (
            <TabsContent value="payload" className="mt-6">
              <PayloadStructureDefinition
                config={payloadConfig}
                onChange={handlePayloadChange}
                adapterType="HTTP"
                mode="receiver"
                disabled={false}
              />
            </TabsContent>
          )}

          {/* XML Conversion Tab - Only show when transformation is enabled */}
          {transformationConfig.mode === 'transform' && (
            <TabsContent value="xmlConversion" className="mt-6">
              <XmlConversionTab
                mode="RECEIVER"
                config={configuration.xmlConversion || {
                  rootElementName: 'Message',
                  encoding: 'UTF-8',
                  includeXmlDeclaration: true,
                  prettyPrint: true,
                  removeRootElement: true,
                  handleNamespaces: true
                }}
                onChange={(xmlConversion) => onConfigurationChange('xmlConversion', xmlConversion)}
              />
            </TabsContent>
          )}
        </Tabs>
      </CardContent>
    </Card>
  );
}