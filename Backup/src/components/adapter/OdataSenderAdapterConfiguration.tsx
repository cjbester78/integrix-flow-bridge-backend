import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Separator } from '@/components/ui/separator';
import { AdapterConfiguration } from '@/types/adapter';

interface OdataSenderAdapterConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (key: string, value: any) => void;
}

export const OdataSenderAdapterConfiguration: React.FC<OdataSenderAdapterConfigurationProps> = ({
  configuration,
  onConfigurationChange,
}) => {
  const renderAuthFields = () => {
    const authType = configuration.authentication?.type || 'none';

    switch (authType) {
      case 'basic':
        return (
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                value={configuration.authentication?.credentials?.username || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    username: e.target.value
                  }
                })}
                placeholder="Enter username"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={configuration.authentication?.credentials?.password || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    password: e.target.value
                  }
                })}
                placeholder="Enter password"
              />
            </div>
          </div>
        );
      case 'oauth2':
        return (
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="clientId">Client ID</Label>
              <Input
                id="clientId"
                value={configuration.authentication?.credentials?.clientId || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    clientId: e.target.value
                  }
                })}
                placeholder="Enter client ID"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="clientSecret">Client Secret</Label>
              <Input
                id="clientSecret"
                type="password"
                value={configuration.authentication?.credentials?.clientSecret || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    clientSecret: e.target.value
                  }
                })}
                placeholder="Enter client secret"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="tokenUrl">Token URL</Label>
              <Input
                id="tokenUrl"
                value={configuration.authentication?.credentials?.tokenUrl || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    tokenUrl: e.target.value
                  }
                })}
                placeholder="https://oauth.example.com/token"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="scope">Scope</Label>
              <Input
                id="scope"
                value={configuration.authentication?.credentials?.scope || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    scope: e.target.value
                  }
                })}
                placeholder="read write"
              />
            </div>
          </div>
        );
      case 'api-key':
        return (
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="apiKey">API Key</Label>
              <Input
                id="apiKey"
                type="password"
                value={configuration.authentication?.credentials?.apiKey || ''}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    apiKey: e.target.value
                  }
                })}
                placeholder="Enter API key"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="apiKeyHeader">API Key Header</Label>
              <Input
                id="apiKeyHeader"
                value={configuration.authentication?.credentials?.apiKeyHeader || 'X-API-Key'}
                onChange={(e) => onConfigurationChange('authentication', {
                  ...configuration.authentication,
                  credentials: {
                    ...configuration.authentication?.credentials,
                    apiKeyHeader: e.target.value
                  }
                })}
                placeholder="X-API-Key"
              />
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      <Tabs defaultValue="source" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="source">Source</TabsTrigger>
          <TabsTrigger value="processing">Processing</TabsTrigger>
        </TabsList>

        <TabsContent value="source" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Service Connection</CardTitle>
              <CardDescription>Configure OData service connection parameters</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="serviceBaseUrl">Service Base URL</Label>
                <Input
                  id="serviceBaseUrl"
                  value={configuration.baseUrl || ''}
                  onChange={(e) => onConfigurationChange('baseUrl', e.target.value)}
                  placeholder="https://odata.example.com/service"
                />
                <p className="text-sm text-muted-foreground">Base URL of the OData service</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="entitySetName">Entity Set Name</Label>
                <Input
                  id="entitySetName"
                  value={configuration.entitySetName || ''}
                  onChange={(e) => onConfigurationChange('entitySetName', e.target.value)}
                  placeholder="Orders, Customers"
                />
                <p className="text-sm text-muted-foreground">OData entity set to update/create</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="apiVersion">API Version</Label>
                <Select 
                  value={configuration.apiVersion || ''} 
                  onValueChange={(value) => onConfigurationChange('apiVersion', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select OData version" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="v2">v2</SelectItem>
                    <SelectItem value="v4">v4</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">OData protocol version</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="authenticationType">Authentication Type</Label>
                <Select 
                  value={configuration.authentication?.type || 'none'} 
                  onValueChange={(value) => onConfigurationChange('authentication', { type: value, credentials: {} })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">None</SelectItem>
                    <SelectItem value="basic">Basic Auth</SelectItem>
                    <SelectItem value="oauth2">OAuth2</SelectItem>
                    <SelectItem value="api-key">API Key</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">Authentication method</p>
              </div>

              {renderAuthFields()}

              <div className="space-y-2">
                <Label htmlFor="timeout">Timeout Settings (seconds)</Label>
                <Input
                  id="timeout"
                  type="number"
                  value={configuration.timeout || ''}
                  onChange={(e) => onConfigurationChange('timeout', parseInt(e.target.value) || 30)}
                  placeholder="30"
                />
                <p className="text-sm text-muted-foreground">Connection and read timeout limits</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Data Preparation</CardTitle>
              <CardDescription>Configure data processing and validation before sending</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="dataMappingRules">Data Mapping Rules</Label>
                <Textarea
                  id="dataMappingRules"
                  value={configuration.dataMappingRules || ''}
                  onChange={(e) => onConfigurationChange('dataMappingRules', e.target.value)}
                  placeholder="Map OrderID to OrderId"
                  rows={4}
                />
                <p className="text-sm text-muted-foreground">Mapping middleware objects to OData fields</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="dataValidationRules">Data Validation Rules</Label>
                <Textarea
                  id="dataValidationRules"
                  value={configuration.dataValidationRules || ''}
                  onChange={(e) => onConfigurationChange('dataValidationRules', e.target.value)}
                  placeholder="Required fields, format checks"
                  rows={4}
                />
                <p className="text-sm text-muted-foreground">Validate data before sending</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};