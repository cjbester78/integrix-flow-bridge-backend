import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { AdapterConfiguration } from '@/types/adapter';

interface RfcSenderAdapterConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (key: string, value: any) => void;
}

export const RfcSenderAdapterConfiguration: React.FC<RfcSenderAdapterConfigurationProps> = ({
  configuration,
  onConfigurationChange,
}) => {
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
              <CardTitle>SAP System Identification</CardTitle>
              <CardDescription>SAP system connection details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapSystemId">SAP System ID (SID)</Label>
                  <Input
                    id="sapSystemId"
                    value={configuration.sapSystemId || ''}
                    onChange={(e) => onConfigurationChange('sapSystemId', e.target.value)}
                    placeholder="PRD, DEV"
                  />
                  <p className="text-xs text-muted-foreground">SAP system identifier</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="sapClientNumber">SAP Client Number</Label>
                  <Input
                    id="sapClientNumber"
                    value={configuration.sapClientNumber || ''}
                    onChange={(e) => onConfigurationChange('sapClientNumber', e.target.value)}
                    placeholder="100"
                  />
                  <p className="text-xs text-muted-foreground">SAP client number</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="sapSystemNumber">SAP System Number</Label>
                  <Input
                    id="sapSystemNumber"
                    value={configuration.sapSystemNumber || ''}
                    onChange={(e) => onConfigurationChange('sapSystemNumber', e.target.value)}
                    placeholder="00"
                  />
                  <p className="text-xs text-muted-foreground">Two-digit system number</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Connection Details</CardTitle>
              <CardDescription>SAP application server and gateway configuration</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="sapApplicationServerHost">SAP Application Server Host</Label>
                <Input
                  id="sapApplicationServerHost"
                  value={configuration.sapApplicationServerHost || ''}
                  onChange={(e) => onConfigurationChange('sapApplicationServerHost', e.target.value)}
                  placeholder="sapserver.example.com"
                />
                <p className="text-sm text-muted-foreground">Hostname or IP of SAP application server</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapGatewayHost">SAP Gateway Host</Label>
                  <Input
                    id="sapGatewayHost"
                    value={configuration.sapGatewayHost || ''}
                    onChange={(e) => onConfigurationChange('sapGatewayHost', e.target.value)}
                    placeholder="sapgw.example.com"
                  />
                  <p className="text-xs text-muted-foreground">Gateway host (optional)</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="sapGatewayService">SAP Gateway Service</Label>
                  <Input
                    id="sapGatewayService"
                    value={configuration.sapGatewayService || ''}
                    onChange={(e) => onConfigurationChange('sapGatewayService', e.target.value)}
                    placeholder="sapgw00"
                  />
                  <p className="text-xs text-muted-foreground">Gateway service/port (optional)</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="portNumber">Port Number</Label>
                  <Input
                    id="portNumber"
                    type="number"
                    value={configuration.portNumber || ''}
                    onChange={(e) => onConfigurationChange('portNumber', e.target.value)}
                    placeholder="3300"
                  />
                  <p className="text-xs text-muted-foreground">Port number (optional)</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="connectionType">Connection Type</Label>
                  <Select 
                    value={configuration.connectionType || ''} 
                    onValueChange={(value) => onConfigurationChange('connectionType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select connection type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="TCP">TCP/IP</SelectItem>
                      <SelectItem value="Gateway">Gateway</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-muted-foreground">TCP/IP or Gateway</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Authentication</CardTitle>
              <CardDescription>SAP user credentials</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapUser">SAP User</Label>
                  <Input
                    id="sapUser"
                    value={configuration.sapUser || ''}
                    onChange={(e) => onConfigurationChange('sapUser', e.target.value)}
                    placeholder="sapuser"
                  />
                  <p className="text-sm text-muted-foreground">User for RFC connection</p>
                </div>

                <div className="space-y-2">
                  <PasswordConfirmation
                    name="sapPassword"
                    label="SAP Password"
                    placeholder="secret"
                    value={configuration.sapPassword || ''}
                    onValueChange={(value) => onConfigurationChange('sapPassword', value)}
                    showConfirmation={false}
                  />
                  <p className="text-sm text-muted-foreground">Password for RFC user</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>RFC Configuration</CardTitle>
              <CardDescription>RFC-specific settings</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="rfcDestinationName">RFC Destination Name</Label>
                  <Input
                    id="rfcDestinationName"
                    value={configuration.rfcDestinationName || ''}
                    onChange={(e) => onConfigurationChange('rfcDestinationName', e.target.value)}
                    placeholder="MIDDLEWARE_DEST"
                  />
                  <p className="text-sm text-muted-foreground">RFC destination configured in SAP</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="callerServiceName">Caller Service Name</Label>
                  <Input
                    id="callerServiceName"
                    value={configuration.callerServiceName || ''}
                    onChange={(e) => onConfigurationChange('callerServiceName', e.target.value)}
                    placeholder="RfcSenderService"
                  />
                  <p className="text-sm text-muted-foreground">Middleware service calling the RFC</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Data Preparation</CardTitle>
              <CardDescription>Configuration for preparing data before sending to SAP</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="dataMappingRules">Data Mapping Rules</Label>
                <Textarea
                  id="dataMappingRules"
                  value={configuration.dataMappingRules || ''}
                  onChange={(e) => onConfigurationChange('dataMappingRules', e.target.value)}
                  placeholder="Map OrderID to VBELN"
                  rows={4}
                />
                <p className="text-sm text-muted-foreground">Map middleware data to SAP RFC parameters</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="validationRules">Validation Rules</Label>
                <Textarea
                  id="validationRules"
                  value={configuration.validationRules || ''}
                  onChange={(e) => onConfigurationChange('validationRules', e.target.value)}
                  placeholder="Mandatory fields, formats"
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