import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Slider } from '@/components/ui/slider';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

interface SoapSenderAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: any) => void;
}

export function SoapSenderAdapterConfiguration({
  configuration,
  onConfigurationChange
}: SoapSenderAdapterConfigurationProps) {
  const handleAuthTypeChange = (authType: string) => {
    onConfigurationChange('authentication.type', authType);
    // Reset auth-specific fields when type changes
    onConfigurationChange('authentication.credentials', {});
  };

  const handleAuthFieldChange = (field: string, value: string) => {
    const credentials = { ...(configuration.authentication?.credentials || {}) };
    credentials[field] = value;
    onConfigurationChange('authentication.credentials', credentials);
  };

  const renderAuthFields = () => {
    const authType = configuration.authentication?.type;
    const credentials = configuration.authentication?.credentials || {};

    switch (authType) {
      case 'basic':
        return (
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="auth-username">Username</Label>
              <Input
                id="auth-username"
                value={credentials.username || ''}
                onChange={(e) => handleAuthFieldChange('username', e.target.value)}
                placeholder="Enter username"
              />
            </div>
            <div className="space-y-2">
              <PasswordConfirmation
                name="auth-password"
                label="Password"
                placeholder="Enter password"
                value={credentials.password || ''}
                onValueChange={(value) => handleAuthFieldChange('password', value)}
                showConfirmation={false}
              />
            </div>
          </div>
        );
      case 'ws-security':
        return (
          <div className="space-y-6">
            {/* Authentication Credentials */}
            <div className="space-y-4">
              <h4 className="text-md font-medium text-muted-foreground">Authentication Credentials</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="auth-username">Username</Label>
                  <Input
                    id="auth-username"
                    value={credentials.username || ''}
                    onChange={(e) => handleAuthFieldChange('username', e.target.value)}
                    placeholder="Enter username"
                  />
                </div>
                <div className="space-y-2">
                  <PasswordConfirmation
                    name="auth-password"
                    label="Password"
                    placeholder="Enter password"
                    value={credentials.password || ''}
                    onValueChange={(value) => handleAuthFieldChange('password', value)}
                    showConfirmation={false}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password-type">Password Type</Label>
                  <Select
                    value={credentials.passwordType || 'digest'}
                    onValueChange={(value) => handleAuthFieldChange('passwordType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select password type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="digest">Password Digest</SelectItem>
                      <SelectItem value="plaintext">Plain Text</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* WS-Security Policies */}
            <div className="space-y-4">
              <h4 className="text-md font-medium text-muted-foreground">WS-Security Policies</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="security-policy">Security Policy</Label>
                  <Select
                    value={credentials.securityPolicy || ''}
                    onValueChange={(value) => handleAuthFieldChange('securityPolicy', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select security policy" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="usernametoken">Username Token Only</SelectItem>
                      <SelectItem value="timestamp">Timestamp</SelectItem>
                      <SelectItem value="usernametoken-timestamp">Username Token + Timestamp</SelectItem>
                      <SelectItem value="sign">Digital Sign</SelectItem>
                      <SelectItem value="encrypt">Encrypt</SelectItem>
                      <SelectItem value="sign-encrypt">Sign + Encrypt</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="ws-addressing">WS-Addressing</Label>
                  <Select
                    value={credentials.wsAddressing || 'disabled'}
                    onValueChange={(value) => handleAuthFieldChange('wsAddressing', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select WS-Addressing" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="disabled">Disabled</SelectItem>
                      <SelectItem value="optional">Optional</SelectItem>
                      <SelectItem value="required">Required</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="timestamp-ttl">Timestamp TTL (seconds)</Label>
                  <Input
                    id="timestamp-ttl"
                    type="number"
                    value={credentials.timestampTTL || ''}
                    onChange={(e) => handleAuthFieldChange('timestampTTL', e.target.value)}
                    placeholder="300"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="security-algorithm">Security Algorithm</Label>
                  <Select
                    value={credentials.securityAlgorithm || ''}
                    onValueChange={(value) => handleAuthFieldChange('securityAlgorithm', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select algorithm" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="aes128">AES-128</SelectItem>
                      <SelectItem value="aes256">AES-256</SelectItem>
                      <SelectItem value="3des">Triple DES</SelectItem>
                      <SelectItem value="rsa15">RSA 1.5</SelectItem>
                      <SelectItem value="rsa-oaep">RSA-OAEP</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* Certificate and Key Management */}
            <div className="space-y-4">
              <h4 className="text-md font-medium text-muted-foreground">Certificate & Key Management</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="keystore-alias">Private Key Alias</Label>
                  <Input
                    id="keystore-alias"
                    value={credentials.keystoreAlias || ''}
                    onChange={(e) => handleAuthFieldChange('keystoreAlias', e.target.value)}
                    placeholder="mykey"
                  />
                </div>
                <div className="space-y-2">
                  <PasswordConfirmation
                    name="keystore-password"
                    label="Private Key Password"
                    placeholder="Enter private key password"
                    value={credentials.keystorePassword || ''}
                    onValueChange={(value) => handleAuthFieldChange('keystorePassword', value)}
                    showConfirmation={false}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="certificate-alias">Certificate Alias</Label>
                  <Input
                    id="certificate-alias"
                    value={credentials.certificateAlias || ''}
                    onChange={(e) => handleAuthFieldChange('certificateAlias', e.target.value)}
                    placeholder="receiver-cert"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="verify-server-certificate">Verify Server Certificate</Label>
                  <Select
                    value={credentials.verifyServerCertificate || 'true'}
                    onValueChange={(value) => handleAuthFieldChange('verifyServerCertificate', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select verification" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="true">Yes</SelectItem>
                      <SelectItem value="false">No</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* Advanced WS-Security Settings */}
            <div className="space-y-4">
              <h4 className="text-md font-medium text-muted-foreground">Advanced Settings</h4>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="custom-policy">Custom Security Policy</Label>
                  <Textarea
                    id="custom-policy"
                    value={credentials.customPolicy || ''}
                    onChange={(e) => handleAuthFieldChange('customPolicy', e.target.value)}
                    placeholder="Enter custom WS-Security policy XML if needed"
                    rows={3}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="security-token-reference">Security Token Reference</Label>
                  <Select
                    value={credentials.securityTokenReference || 'binarySecurityToken'}
                    onValueChange={(value) => handleAuthFieldChange('securityTokenReference', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select token reference" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="binarySecurityToken">Binary Security Token</SelectItem>
                      <SelectItem value="keyIdentifier">Key Identifier</SelectItem>
                      <SelectItem value="x509Data">X509 Data</SelectItem>
                      <SelectItem value="thumbprint">Thumbprint Reference</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>
          </div>
        );
      case 'oauth':
        return (
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="auth-client-id">Client ID</Label>
              <Input
                id="auth-client-id"
                value={credentials.clientId || ''}
                onChange={(e) => handleAuthFieldChange('clientId', e.target.value)}
                placeholder="Enter client ID"
              />
            </div>
            <div className="space-y-2">
              <PasswordConfirmation
                name="auth-client-secret"
                label="Client Secret"
                placeholder="Enter client secret"
                value={credentials.clientSecret || ''}
                onValueChange={(value) => handleAuthFieldChange('clientSecret', value)}
                showConfirmation={false}
              />
            </div>
          </div>
        );
      default:
        return null;
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
                  <Label htmlFor="service-endpoint">Service Endpoint URL</Label>
                  <Input
                    id="service-endpoint"
                    value={configuration.serviceEndpointUrl || ''}
                    onChange={(e) => onConfigurationChange('serviceEndpointUrl', e.target.value)}
                    placeholder="https://api.yourdomain.com/soap/service"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="wsdl-url">WSDL URL</Label>
                  <Input
                    id="wsdl-url"
                    value={configuration.wsdlUrl || ''}
                    onChange={(e) => onConfigurationChange('wsdlUrl', e.target.value)}
                    placeholder="https://api.yourdomain.com/soap/service?wsdl"
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

            {/* Request Format Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Request Format</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="request-message">Request Message</Label>
                  <Textarea
                    id="request-message"
                    value={configuration.requestMessage || ''}
                    onChange={(e) => onConfigurationChange('requestMessage', e.target.value)}
                    placeholder="XML schema / structure expected in SOAP request"
                    rows={4}
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

            {/* Authentication & Security Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Authentication & Security</h3>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="auth-type">Authentication Type</Label>
                  <Select
                    value={configuration.authentication?.type || 'none'}
                    onValueChange={handleAuthTypeChange}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select authentication type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">None</SelectItem>
                      <SelectItem value="basic">Basic Auth</SelectItem>
                      <SelectItem value="ws-security">WS-Security UsernameToken</SelectItem>
                      <SelectItem value="oauth">OAuth</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="ws-security-policies">WS-Security Policies:</Label>
                  <Select 
                    value={configuration.wsSecurityPolicyType || ''} 
                    onValueChange={(value) => onConfigurationChange('wsSecurityPolicyType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Define Parameter" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">None</SelectItem>
                      <SelectItem value="manual">Via Manual Configuration in Channel</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-sm text-muted-foreground">Security policies applied</p>
                </div>
                {renderAuthFields()}
              </div>
            </div>

            {/* Headers and Metadata Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Headers and Metadata</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="custom-headers">Custom SOAP Headers</Label>
                  <Textarea
                    id="custom-headers"
                    value={configuration.customHeaders || ''}
                    onChange={(e) => onConfigurationChange('customHeaders', e.target.value)}
                    placeholder="Additional headers required by service (WS-Addressing, Custom headers)"
                    rows={3}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="fault-handling">SOAP Fault Handling</Label>
                  <Textarea
                    id="fault-handling"
                    value={configuration.faultHandling || ''}
                    onChange={(e) => onConfigurationChange('faultHandling', e.target.value)}
                    placeholder="Expected fault codes and handling logic"
                    rows={3}
                  />
                </div>
              </div>
            </div>

            {/* Response Format and Error Handling Section */}
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
                    onChange={(e) => onConfigurationChange('timeout', parseInt(e.target.value) || 0)}
                    placeholder="30"
                  />
                </div>
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}