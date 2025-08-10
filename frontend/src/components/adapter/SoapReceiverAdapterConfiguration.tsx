import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { CertificateSelection } from '@/components/ui/certificate-selection';
import { api } from '@/services/api';

interface SoapReceiverAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: any) => void;
  businessComponentId?: string;
}

export function SoapReceiverAdapterConfiguration({
  configuration,
  onConfigurationChange,
  businessComponentId
}: SoapReceiverAdapterConfigurationProps) {
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

  // Fetch WSDL details and check structure when WSDL is selected
  useEffect(() => {
    if (configuration.selectedWsdl) {
      fetchWsdlDetails(configuration.selectedWsdl);
    }
  }, [configuration.selectedWsdl]);

  const fetchWsdls = async () => {
    try {
      setLoadingWsdls(true);
      // Fetch WSDLs as DataStructures filtered by type, usage and business component
      const response = await api.get('/structures', {
        params: {
          type: 'wsdl',
          usage: 'target',
          businessComponentId: businessComponentId,
          limit: 100
        }
      } as any);
      
      if (response.data && response.data.structures) {
        // Transform the structures to match our expected format
        // Filter to ensure we only get WSDL type structures with target usage
        const wsdlList = response.data.structures
          .filter((structure: any) => {
            // Must be WSDL type
            const isWsdl = structure.type?.toLowerCase() === 'wsdl';
            
            // Check usage - target for receiver adapter
            const isTarget = structure.usage === 'target' || 
                           structure.metadata?.usage === 'target' ||
                           !structure.usage; // Include if usage is not set
            
            const included = isWsdl && isTarget;
            console.log(`SOAP Receiver - Structure ${structure.name}: type=${structure.type}, usage=${structure.usage}, metadata.usage=${structure.metadata?.usage}, included=${included}`);
            return included;
          })
          .map((structure: any) => ({
            id: structure.id,
            name: structure.name,
            endpointUrl: structure.metadata?.endpointUrl || ''
          }));
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

  const fetchWsdlDetails = async (wsdlId: string) => {
    try {
      // Get the WSDL structure details
      const response = await api.get(`/structures/${wsdlId}`);
      
      if (response.data) {
        console.log('SOAP Receiver - WSDL Structure:', response.data);
        console.log('SOAP Receiver - WSDL Structure:', response.data);
        console.log('SOAP Receiver - WSDL Metadata:', response.data.metadata);
        console.log('SOAP Receiver - WSDL Namespace:', response.data.namespace);
        console.log('SOAP Receiver - Full response data:', JSON.stringify(response.data, null, 2));
        
        // Extract structure details from metadata
        const structureDetails: any = {};
        
        // Extract request structure
        if (response.data.metadata?.requestStructure || response.data.metadata?.operationInfo?.request) {
          structureDetails.request = response.data.metadata?.requestStructure || response.data.metadata?.operationInfo?.request;
        }
        
        // Extract response structure
        if (response.data.metadata?.responseStructure || response.data.metadata?.operationInfo?.response) {
          structureDetails.response = response.data.metadata?.responseStructure || response.data.metadata?.operationInfo?.response;
        }
        
        // Extract fault structure
        if (response.data.metadata?.faultStructure || response.data.metadata?.operationInfo?.fault) {
          structureDetails.fault = response.data.metadata?.faultStructure || response.data.metadata?.operationInfo?.fault;
        }
        
        setWsdlStructureDetails(structureDetails);
        console.log('SOAP Receiver - Extracted structures:', structureDetails);
        
        // Auto-populate endpoint URL from WSDL metadata or namespace
        if (response.data.metadata?.endpointUrl) {
          onConfigurationChange('targetEndpointUrl', response.data.metadata.endpointUrl);
          console.log('SOAP Receiver - Using endpoint URL from metadata:', response.data.metadata.endpointUrl);
        } else if (response.data.namespace?.schemaLocation) {
          // Use schemaLocation from namespace as endpoint URL
          onConfigurationChange('targetEndpointUrl', response.data.namespace.schemaLocation);
          console.log('SOAP Receiver - Using endpoint URL from namespace.schemaLocation:', response.data.namespace.schemaLocation);
        } else if (response.data.namespace && typeof response.data.namespace === 'string') {
          // Try to parse namespace if it's a JSON string
          try {
            const namespaceObj = JSON.parse(response.data.namespace);
            if (namespaceObj.schemaLocation) {
              onConfigurationChange('targetEndpointUrl', namespaceObj.schemaLocation);
              console.log('SOAP Receiver - Using endpoint URL from parsed namespace:', namespaceObj.schemaLocation);
            }
          } catch (e) {
            console.log('SOAP Receiver - Could not parse namespace string');
          }
        } else if (response.data.originalContent) {
          // Parse the WSDL content to extract the endpoint URL from soap:address
          try {
            const parser = new DOMParser();
            const doc = parser.parseFromString(response.data.originalContent, 'text/xml');
            const soapAddresses = doc.querySelectorAll('address, soap\\:address, soap12\\:address, wsdl\\:address, *[name()="soap:address"], *[name()="soap12:address"]');
            if (soapAddresses.length > 0) {
              const endpointUrl = soapAddresses[0].getAttribute('location');
              if (endpointUrl) {
                onConfigurationChange('targetEndpointUrl', endpointUrl);
                console.log('SOAP Receiver - Extracted endpoint URL from WSDL:', endpointUrl);
              }
            }
          } catch (error) {
            console.error('Error parsing WSDL for endpoint URL:', error);
          }
        }
        
        // Check if metadata contains sync/async information
        const hasInput = response.data.metadata?.hasInput || response.data.metadata?.operationInfo?.hasInput;
        const hasOutput = response.data.metadata?.hasOutput || response.data.metadata?.operationInfo?.hasOutput;
        const hasFault = response.data.metadata?.hasFault || response.data.metadata?.operationInfo?.hasFault;
        
        console.log(`SOAP Receiver - WSDL Analysis: hasInput=${hasInput}, hasOutput=${hasOutput}, hasFault=${hasFault}`);
        
        if (hasInput && hasOutput && hasFault) {
          // Has Input, Output, and Fault - set to synchronous
          console.log('SOAP Receiver - Setting mode to SYNCHRONOUS');
          onConfigurationChange('processingMode', 'SYNCHRONOUS');
        } else {
          // Missing one or more - set to asynchronous
          console.log('SOAP Receiver - Setting mode to ASYNCHRONOUS');
          onConfigurationChange('processingMode', 'ASYNCHRONOUS');
        }
      }
    } catch (error) {
      console.error('Error fetching WSDL details:', error);
      // Default to async on error
      onConfigurationChange('processingMode', 'ASYNCHRONOUS');
      setWsdlStructureDetails({});
    }
  };

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
              <Label htmlFor="auth-password">Password</Label>
              <Input
                id="auth-password"
                type="password"
                value={credentials.password || ''}
                onChange={(e) => handleAuthFieldChange('password', e.target.value)}
                placeholder="Enter password"
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
                  <Label htmlFor="auth-password">Password</Label>
                  <Input
                    id="auth-password"
                    type="password"
                    value={credentials.password || ''}
                    onChange={(e) => handleAuthFieldChange('password', e.target.value)}
                    placeholder="Enter password"
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
              <div className="grid grid-cols-1 gap-4">
                <CertificateSelection
                  id="soapCertificate"
                  label="SOAP Certificate"
                  value={configuration.certificateId || ''}
                  onChange={(value) => onConfigurationChange('certificateId', value)}
                  businessComponentId={businessComponentId}
                  placeholder="Select SOAP certificate"
                  required
                />
                
                <div className="space-y-2">
                  <Label htmlFor="verify-server-certificate">Verify Server Certificate</Label>
                  <Select
                    value={configuration.verifyServerCertificate || 'true'}
                    onValueChange={(value) => onConfigurationChange('verifyServerCertificate', value)}
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
              <Label htmlFor="auth-client-secret">Client Secret</Label>
              <Input
                id="auth-client-secret"
                type="password"
                value={credentials.clientSecret || ''}
                onChange={(e) => handleAuthFieldChange('clientSecret', e.target.value)}
                placeholder="Enter client secret"
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
        <CardTitle>SOAP Receiver Configuration</CardTitle>
        <CardDescription>Configure your SOAP receiver adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="target" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="target">Target</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
          </TabsList>

          <TabsContent value="target" className="space-y-6">
            {/* Endpoint Information Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Endpoint Information</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="target-endpoint">Target Endpoint URL</Label>
                  <Input
                    id="target-endpoint"
                    value={configuration.targetEndpointUrl || ''}
                    onChange={(e) => onConfigurationChange('targetEndpointUrl', e.target.value)}
                    placeholder="https://api.thirdparty.com/soap/service"
                  />
                  {configuration.selectedWsdl && (
                    <p className="text-xs text-muted-foreground">Auto-populated from selected WSDL</p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="soap-action">SOAP Action</Label>
                  <Input
                    id="soap-action"
                    value={configuration.soapAction || ''}
                    onChange={(e) => onConfigurationChange('soapAction', e.target.value)}
                    placeholder="urn:submitOrder"
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

            {/* Target Message Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Target Message</h3>
              <div className="space-y-2">
                <Label htmlFor="target-wsdl">Select WSDL</Label>
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

            {/* Connection Parameters Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Connection Parameters</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="connection-timeout">Connection Timeout (seconds)</Label>
                  <Input
                    id="connection-timeout"
                    type="number"
                    value={configuration.connectionTimeout || ''}
                    onChange={(e) => onConfigurationChange('connectionTimeout', parseInt(e.target.value) || 0)}
                    placeholder="30"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="read-timeout">Read Timeout (seconds)</Label>
                  <Input
                    id="read-timeout"
                    type="number"
                    value={configuration.readTimeout || ''}
                    onChange={(e) => onConfigurationChange('readTimeout', parseInt(e.target.value) || 0)}
                    placeholder="30"
                  />
                </div>
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

              {(configuration.processingMode || 'ASYNCHRONOUS') === 'ASYNCHRONOUS' && (
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
                    <Label htmlFor="retry-policy">Retry Policy</Label>
                    <Textarea
                      id="retry-policy"
                      value={configuration.retryPolicy || ''}
                      onChange={(e) => onConfigurationChange('retryPolicy', e.target.value)}
                      placeholder="Rules for retrying failed SOAP calls (Number of retries, backoff strategy)"
                      rows={3}
                    />
                  </div>
                </div>
              </div>
            )}

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