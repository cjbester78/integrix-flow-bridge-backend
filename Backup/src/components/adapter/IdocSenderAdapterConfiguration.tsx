import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';

interface IdocSenderAdapterConfigurationProps {
  configuration: any;
  onConfigurationChange: (field: string, value: string | number) => void;
}

const connectionTypeOptions = [
  'TCP/IP',
  'Gateway'
];

export function IdocSenderAdapterConfiguration({
  configuration,
  onConfigurationChange
}: IdocSenderAdapterConfigurationProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>IDOC Sender Configuration</CardTitle>
        <CardDescription>Configure your IDOC sender adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="source" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="source">Source</TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
          </TabsList>

          <TabsContent value="source" className="space-y-6">
            {/* SAP System Identification Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">SAP System Identification</h3>
              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapSystemId">SAP System ID (SID)</Label>
                  <Input
                    id="sapSystemId"
                    type="text"
                    placeholder="e.g., PRD, DEV"
                    value={configuration.sapSystemId || ''}
                    onChange={(e) => onConfigurationChange('sapSystemId', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sapClientNumber">SAP Client Number</Label>
                  <Input
                    id="sapClientNumber"
                    type="text"
                    placeholder="Client number within SAP system"
                    value={configuration.sapClientNumber || ''}
                    onChange={(e) => onConfigurationChange('sapClientNumber', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sapSystemNumber">SAP System Number</Label>
                  <Input
                    id="sapSystemNumber"
                    type="text"
                    placeholder="Two-digit SAP system number"
                    value={configuration.sapSystemNumber || ''}
                    onChange={(e) => onConfigurationChange('sapSystemNumber', e.target.value)}
                  />
                </div>
              </div>
            </div>

            {/* Connection Details Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">Connection Details</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapApplicationServerHost">SAP Application Server Host</Label>
                  <Input
                    id="sapApplicationServerHost"
                    type="text"
                    placeholder="Hostname or IP of SAP application server"
                    value={configuration.sapApplicationServerHost || ''}
                    onChange={(e) => onConfigurationChange('sapApplicationServerHost', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sapGatewayHost">SAP Gateway Host</Label>
                  <Input
                    id="sapGatewayHost"
                    type="text"
                    placeholder="Hostname/IP of SAP gateway (optional)"
                    value={configuration.sapGatewayHost || ''}
                    onChange={(e) => onConfigurationChange('sapGatewayHost', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sapGatewayService">SAP Gateway Service</Label>
                  <Input
                    id="sapGatewayService"
                    type="text"
                    placeholder="Gateway service/port (optional)"
                    value={configuration.sapGatewayService || ''}
                    onChange={(e) => onConfigurationChange('sapGatewayService', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="portNumber">Port Number</Label>
                  <Input
                    id="portNumber"
                    type="number"
                    placeholder="Network port if applicable"
                    value={configuration.portNumber || ''}
                    onChange={(e) => onConfigurationChange('portNumber', parseInt(e.target.value) || 0)}
                  />
                </div>
              </div>
              <div className="grid grid-cols-1 gap-4">
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
                      {connectionTypeOptions.map((option) => (
                        <SelectItem key={option} value={option}>
                          {option}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            {/* Authentication Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">Authentication</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sapUser">SAP User</Label>
                  <Input
                    id="sapUser"
                    type="text"
                    placeholder="User ID for RFC connection"
                    value={configuration.sapUser || ''}
                    onChange={(e) => onConfigurationChange('sapUser', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sapPassword">SAP Password</Label>
                  <Input
                    id="sapPassword"
                    type="password"
                    placeholder="Password for RFC user"
                    value={configuration.sapPassword || ''}
                    onChange={(e) => onConfigurationChange('sapPassword', e.target.value)}
                  />
                </div>
              </div>
            </div>

            {/* RFC Destination Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">RFC Destination</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="rfcDestinationName">RFC Destination Name</Label>
                  <Input
                    id="rfcDestinationName"
                    type="text"
                    placeholder="SAP RFC destination (optional)"
                    value={configuration.rfcDestinationName || ''}
                    onChange={(e) => onConfigurationChange('rfcDestinationName', e.target.value)}
                  />
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="processing" className="space-y-6">
            {/* IDoc Transmission Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">IDoc Transmission</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="idocPort">IDoc Port</Label>
                  <Input
                    id="idocPort"
                    type="text"
                    placeholder="Logical port configured in SAP for receiving IDocs from middleware"
                    value={configuration.idocPort || ''}
                    onChange={(e) => onConfigurationChange('idocPort', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="listenerServiceName">Listener Service Name</Label>
                  <Input
                    id="listenerServiceName"
                    type="text"
                    placeholder="Middleware service or process name sending the IDoc"
                    value={configuration.listenerServiceName || ''}
                    onChange={(e) => onConfigurationChange('listenerServiceName', e.target.value)}
                  />
                </div>
              </div>
            </div>

            {/* IDoc Identification Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">IDoc Identification</h3>
              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="idocType">IDoc Type</Label>
                  <Input
                    id="idocType"
                    type="text"
                    placeholder="e.g., ORDERS05"
                    value={configuration.idocType || ''}
                    onChange={(e) => onConfigurationChange('idocType', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="messageType">Message Type</Label>
                  <Input
                    id="messageType"
                    type="text"
                    placeholder="e.g., ORDERS"
                    value={configuration.messageType || ''}
                    onChange={(e) => onConfigurationChange('messageType', e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="processCode">Process Code</Label>
                  <Input
                    id="processCode"
                    type="text"
                    placeholder="SAP process code used for sending the IDoc"
                    value={configuration.processCode || ''}
                    onChange={(e) => onConfigurationChange('processCode', e.target.value)}
                  />
                </div>
              </div>
            </div>

            {/* Security / Encryption Section */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium border-b pb-2">Security / Encryption</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="sncSecuritySettings">SNC / Security Settings</Label>
                  <Textarea
                    id="sncSecuritySettings"
                    placeholder="Secure Network Communications parameters (optional)"
                    value={configuration.sncSecuritySettings || ''}
                    onChange={(e) => onConfigurationChange('sncSecuritySettings', e.target.value)}
                    rows={4}
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