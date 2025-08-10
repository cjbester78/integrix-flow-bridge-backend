import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Slider } from '@/components/ui/slider';
import { CertificateSelection } from '@/components/ui/certificate-selection';

interface HttpReceiverAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: string | number | boolean) => void;
  businessComponentId?: string;
}

export function HttpReceiverAdapterConfiguration({
  configuration,
  onConfigurationChange,
  businessComponentId
}: HttpReceiverAdapterConfigurationProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>HTTP Receiver Configuration</CardTitle>
        <CardDescription>Configure your HTTP receiver adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="target" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="target">Target</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
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
              <div className="space-y-4">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="processingMode">Processing Type</Label>
                    <span className="text-sm text-muted-foreground">
                      {configuration.processingMode === 0 ? 'Synchronous' : 'Asynchronous'}
                    </span>
                  </div>
                  <div className="px-2">
                    <Slider
                      id="processingMode"
                      min={0}
                      max={1}
                      step={1}
                      value={[configuration.processingMode || 0]}
                      onValueChange={(value) => onConfigurationChange('processingMode', value[0])}
                      className="w-full"
                    />
                  </div>
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Synchronous</span>
                    <span>Asynchronous</span>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {configuration.processingMode === 0 
                      ? 'Wait for response before continuing (real-time)' 
                      : 'Continue without waiting for response (fire-and-forget)'}
                  </p>
                </div>
              </div>
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
        </Tabs>
      </CardContent>
    </Card>
  );
}