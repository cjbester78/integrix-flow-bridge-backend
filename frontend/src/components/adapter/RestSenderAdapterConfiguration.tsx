import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { useDataStructures } from '@/hooks/useDataStructures';
import { XmlConversionTab } from '../adapters/XmlConversionTab';
import { TransformationToggle, TransformationConfig } from '../adapters/TransformationToggle';
import { PayloadStructureDefinition, PayloadStructureConfig } from '../adapters/PayloadStructureDefinition';

interface RestSenderAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: string | number | TransformationConfig | PayloadStructureConfig | any) => void;
}

export function RestSenderAdapterConfiguration({
  configuration,
  onConfigurationChange
}: RestSenderAdapterConfigurationProps) {
  const { businessComponents, loading } = useBusinessComponentAdapters();
  const { structures } = useDataStructures();
  const [selectedBusinessComponentId, setSelectedBusinessComponentId] = useState<string>('');
  const [sourceStructures, setSourceStructures] = useState<any[]>([]);
  
  // Initialize transformation and payload configs
  const [transformationConfig, setTransformationConfig] = useState<TransformationConfig>(
    configuration.transformationConfig || { mode: 'transform', requiresStructure: true }
  );
  const [payloadConfig, setPayloadConfig] = useState<PayloadStructureConfig>(
    configuration.payloadStructureConfig || { format: 'JSON' }
  );

  // Set default processing mode to asynchronous
  useEffect(() => {
    if (!configuration.processingMode) {
      onConfigurationChange('processingMode', 'ASYNCHRONOUS');
    }
  }, []);

  // Filter structures based on selected business component
  useEffect(() => {
    if (selectedBusinessComponentId) {
      const filteredStructures = structures.filter(
        structure => structure.usage === 'source'
      );
      setSourceStructures(filteredStructures);
    } else {
      setSourceStructures([]);
    }
  }, [selectedBusinessComponentId, structures]);
  
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

  return (
    <Card>
      <CardHeader>
        <CardTitle>REST Sender Configuration</CardTitle>
        <CardDescription>Configure your REST sender adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="source" className="w-full">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="source">Source</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
            <TabsTrigger value="transformation">Transformation</TabsTrigger>
            <TabsTrigger value="payload" disabled={transformationConfig.mode !== 'transform'}>
              Payload Structure
            </TabsTrigger>
            <TabsTrigger value="xmlConversion" disabled={transformationConfig.mode !== 'transform'}>
              XML Conversion
            </TabsTrigger>
          </TabsList>

          <TabsContent value="source" className="space-y-6">
            {/* Endpoint Information Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Endpoint Information</h3>
              
              <div className="space-y-2">
                <Label htmlFor="baseEndpointUrl">Base Endpoint URL</Label>
                <Input
                  id="baseEndpointUrl"
                  type="text"
                  placeholder="https://api.yourdomain.com/integration"
                  value={configuration.baseEndpointUrl || ''}
                  onChange={(e) => onConfigurationChange('baseEndpointUrl', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">Base URL of your REST API</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="resourcePath">Resource Path</Label>
                <Input
                  id="resourcePath"
                  type="text"
                  placeholder="/orders, /status, /submit"
                  value={configuration.resourcePath || ''}
                  onChange={(e) => onConfigurationChange('resourcePath', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">Specific REST resource or path</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="apiVersion">API Version</Label>
                <Input
                  id="apiVersion"
                  type="text"
                  placeholder="v1, v2"
                  value={configuration.apiVersion || ''}
                  onChange={(e) => onConfigurationChange('apiVersion', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">Versioning info for API</p>
              </div>
            </div>

            {/* Request Method and Format Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Request Method and Format</h3>
              
              <div className="space-y-2">
                <Label htmlFor="httpMethod">HTTP Method</Label>
                <Select
                  value={configuration.httpMethod || ''}
                  onValueChange={(value) => onConfigurationChange('httpMethod', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select HTTP method" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="GET">GET</SelectItem>
                    <SelectItem value="POST">POST</SelectItem>
                    <SelectItem value="PUT">PUT</SelectItem>
                    <SelectItem value="DELETE">DELETE</SelectItem>
                    <SelectItem value="PATCH">PATCH</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">HTTP verb to call</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="contentType">Content-Type</Label>
                <Select
                  value={configuration.contentType || ''}
                  onValueChange={(value) => onConfigurationChange('contentType', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select media type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="application/json">application/json</SelectItem>
                    <SelectItem value="application/xml">application/xml</SelectItem>
                    <SelectItem value="application/x-www-form-urlencoded">application/x-www-form-urlencoded</SelectItem>
                    <SelectItem value="text/plain">text/plain</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Expected media type for request</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="acceptType">Accept</Label>
                <Select
                  value={configuration.acceptType || ''}
                  onValueChange={(value) => onConfigurationChange('acceptType', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select expected response type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="application/json">application/json</SelectItem>
                    <SelectItem value="application/xml">application/xml</SelectItem>
                    <SelectItem value="text/plain">text/plain</SelectItem>
                    <SelectItem value="text/html">text/html</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Expected media type of response</p>
              </div>
            </div>

            {/* Request Parameters Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Request Parameters</h3>
              
              <div className="space-y-2">
                <Label htmlFor="queryParameters">Query Parameters</Label>
                <Textarea
                  id="queryParameters"
                  placeholder="?page=1&size=10"
                  value={configuration.queryParameters || ''}
                  onChange={(e) => onConfigurationChange('queryParameters', e.target.value)}
                  className="min-h-[80px]"
                />
                <p className="text-sm text-muted-foreground">Expected query params for filtering/pagination</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="timeoutSettings">Timeout Settings (seconds)</Label>
                <Input
                  id="timeoutSettings"
                  type="number"
                  placeholder="30"
                  value={configuration.timeoutSettings || ''}
                  onChange={(e) => onConfigurationChange('timeoutSettings', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">Connection and read timeout limits</p>
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

            {/* Authentication Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Authentication</h3>
              
              <div className="space-y-2">
                <Label htmlFor="authenticationType">Authentication Type</Label>
                <Select
                  value={configuration.authenticationType || ''}
                  onValueChange={(value) => onConfigurationChange('authenticationType', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select authentication method" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">None</SelectItem>
                    <SelectItem value="basic">Basic Auth</SelectItem>
                    <SelectItem value="oauth2-bearer">OAuth2 Bearer Token</SelectItem>
                    <SelectItem value="api-key">API Key</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Security method required</p>
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

              {configuration.authenticationType === 'oauth2-bearer' && (
                <div className="space-y-2">
                  <Label htmlFor="bearerToken">Bearer Token</Label>
                  <Input
                    id="bearerToken"
                    type="password"
                    placeholder="OAuth2 bearer token"
                    value={configuration.bearerToken || ''}
                    onChange={(e) => onConfigurationChange('bearerToken', e.target.value)}
                  />
                </div>
              )}

              {configuration.authenticationType === 'api-key' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="apiKeyName">API Key Name</Label>
                    <Input
                      id="apiKeyName"
                      type="text"
                      placeholder="X-API-Key"
                      value={configuration.apiKeyName || ''}
                      onChange={(e) => onConfigurationChange('apiKeyName', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="apiKeyValue">API Key Value</Label>
                    <Input
                      id="apiKeyValue"
                      type="password"
                      placeholder="API key value"
                      value={configuration.apiKeyValue || ''}
                      onChange={(e) => onConfigurationChange('apiKeyValue', e.target.value)}
                    />
                  </div>
                </>
              )}
            </div>

            {/* Payload and Headers Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Payload and Headers</h3>
              
              <div className="space-y-2">
                <Label htmlFor="businessComponent">Business Component</Label>
                <Select
                  value={selectedBusinessComponentId}
                  onValueChange={(value) => {
                    setSelectedBusinessComponentId(value);
                    onConfigurationChange('businessComponentId', value);
                  }}
                  disabled={loading}
                >
                  <SelectTrigger className="bg-background">
                    <SelectValue placeholder="Select a business component" />
                  </SelectTrigger>
                  <SelectContent className="bg-background border z-50">
                    {businessComponents.map((businessComponent) => (
                      <SelectItem key={businessComponent.id} value={businessComponent.id}>
                        {businessComponent.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="requestPayload">Request Payload</Label>
                <Select
                  value={configuration.requestPayload || ''}
                  onValueChange={(value) => onConfigurationChange('requestPayload', value)}
                  disabled={!selectedBusinessComponentId || sourceStructures.length === 0}
                >
                  <SelectTrigger className="bg-background">
                    <SelectValue placeholder="Select request structure" />
                  </SelectTrigger>
                  <SelectContent className="bg-background border z-50">
                    {sourceStructures.map((structure) => (
                      <SelectItem key={structure.id} value={structure.id}>
                        {structure.name} ({structure.type})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Format JSON or XML schema expected in request body</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="responseFormat">Response Format</Label>
                <Select
                  value={configuration.responseFormat || ''}
                  onValueChange={(value) => onConfigurationChange('responseFormat', value)}
                  disabled={!selectedBusinessComponentId || sourceStructures.length === 0}
                >
                  <SelectTrigger className="bg-background">
                    <SelectValue placeholder="Select response structure" />
                  </SelectTrigger>
                  <SelectContent className="bg-background border z-50">
                    {sourceStructures.map((structure) => (
                      <SelectItem key={structure.id} value={structure.id}>
                        {structure.name} ({structure.type})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Format of API response</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="customHeaders">Headers</Label>
                <Textarea
                  id="customHeaders"
                  placeholder="X-Correlation-ID: <UUID>"
                  value={configuration.customHeaders || ''}
                  onChange={(e) => onConfigurationChange('customHeaders', e.target.value)}
                  className="min-h-[80px]"
                />
                <p className="text-sm text-muted-foreground">Required custom HTTP headers</p>
              </div>
            </div>

            {/* Error Handling and Throttling Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Error Handling and Throttling</h3>
              
              <div className="space-y-2">
                <Label htmlFor="errorHandling">Error Handling</Label>
                <Textarea
                  id="errorHandling"
                  placeholder="4xx/5xx codes, JSON error payload"
                  value={configuration.errorHandling || ''}
                  onChange={(e) => onConfigurationChange('errorHandling', e.target.value)}
                  className="min-h-[80px]"
                />
                <p className="text-sm text-muted-foreground">Expected HTTP status codes and error message formats</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="rateLimiting">Rate Limiting / Throttling</Label>
                <Input
                  id="rateLimiting"
                  type="text"
                  placeholder="1000 requests/minute"
                  value={configuration.rateLimiting || ''}
                  onChange={(e) => onConfigurationChange('rateLimiting', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">Limits on API calls per time interval</p>
              </div>
            </div>
          </TabsContent>

          {/* XML Conversion Tab */}
          {/* Transformation Tab */}
          <TabsContent value="transformation" className="mt-6">
            <TransformationToggle
              config={transformationConfig}
              onChange={handleTransformationChange}
              adapterType="REST"
              dataStructures={sourceStructures}
              disabled={false}
            />
          </TabsContent>

          {/* Payload Structure Tab - Only show when transformation is enabled */}
          {transformationConfig.mode === 'transform' && (
            <TabsContent value="payload" className="mt-6">
              <PayloadStructureDefinition
                config={payloadConfig}
                onChange={handlePayloadChange}
                adapterType="REST"
                mode="sender"
                disabled={false}
              />
            </TabsContent>
          )}

          {/* XML Conversion Tab - Only show when transformation is enabled */}
          {transformationConfig.mode === 'transform' && (
            <TabsContent value="xmlConversion" className="mt-6">
              <XmlConversionTab
                mode="SENDER"
                config={configuration.xmlConversion || {
                  rootElementName: 'Message',
                  encoding: 'UTF-8',
                  includeXmlDeclaration: true,
                  prettyPrint: true
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