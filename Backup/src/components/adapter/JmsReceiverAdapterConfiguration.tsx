import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Button } from '@/components/ui/button';
import { CommunicationAdapter } from '@/types/adapter';
import { CertificateSelection } from '@/components/ui/certificate-selection';

interface JmsReceiverAdapterConfigurationProps {
  adapter: CommunicationAdapter;
  onUpdate: (adapter: CommunicationAdapter) => void;
  businessComponentId?: string;
}

export function JmsReceiverAdapterConfiguration({
  adapter,
  onUpdate,
  businessComponentId
}: JmsReceiverAdapterConfigurationProps) {
  const handleInputChange = (field: string, value: string | number | boolean) => {
    onUpdate({
      ...adapter,
      configuration: {
        ...adapter.configuration,
        [field]: value
      }
    });
  };

  const handleTestConnection = () => {
    // TODO: Implement test connection functionality
    alert('Testing connection... (not implemented)');
  };

  return (
    <div className="space-y-6">
      <Tabs defaultValue="target" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="target">Target</TabsTrigger>
          <TabsTrigger value="processing">Processing Details</TabsTrigger>
        </TabsList>

        <TabsContent value="target" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Connection Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="connectionFactoryClass">Connection Factory class</Label>
                  <Input
                    id="connectionFactoryClass"
                    placeholder="com.ibm.mq.jms.MQConnectionFactory"
                    value={adapter.configuration.connectionFactoryClass || ''}
                    onChange={(e) => handleInputChange('connectionFactoryClass', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="queueClass">Queue class</Label>
                  <Input
                    id="queueClass"
                    placeholder="com.ibm.mq.jms.MQQueue"
                    value={adapter.configuration.queueClass || ''}
                    onChange={(e) => handleInputChange('queueClass', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="queueManager">Queue Manager Name *</Label>
                  <Input
                    id="queueManager"
                    placeholder="QM1"
                    value={adapter.configuration.queueManager || ''}
                    onChange={(e) => handleInputChange('queueManager', e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="host">Host Name / IP *</Label>
                  <Input
                    id="host"
                    placeholder="mqserver.example.com"
                    value={adapter.configuration.host || ''}
                    onChange={(e) => handleInputChange('host', e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="port">Port *</Label>
                  <Input
                    id="port"
                    type="number"
                    placeholder="1414"
                    value={adapter.configuration.port || ''}
                    onChange={(e) => handleInputChange('port', parseInt(e.target.value))}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="channel">Channel Name *</Label>
                  <Input
                    id="channel"
                    placeholder="CHANNEL.SVRCONN"
                    value={adapter.configuration.channel || ''}
                    onChange={(e) => handleInputChange('channel', e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="connectionFactory">Connection Factory JNDI Name</Label>
                  <Input
                    id="connectionFactory"
                    placeholder="jms/ConnectionFactory"
                    value={adapter.configuration.connectionFactory || ''}
                    onChange={(e) => handleInputChange('connectionFactory', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="transportType">Transport Type *</Label>
                  <Select
                    value={adapter.configuration.transportType || 'CLIENT'}
                    onValueChange={(value) => handleInputChange('transportType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select transport type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="CLIENT">Remote TCP/IP</SelectItem>
                      <SelectItem value="BINDINGS">Local (in-memory)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="username">User Name</Label>
                  <Input
                    id="username"
                    placeholder="MQ Username"
                    value={adapter.configuration.username || ''}
                    onChange={(e) => handleInputChange('username', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <PasswordConfirmation
                    name="password"
                    label="Password"
                    placeholder="MQ Password"
                    required={false}
                    value={adapter.configuration.password || ''}
                    onValueChange={(value) => handleInputChange('password', value)}
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <Checkbox
                  id="useSSL"
                  checked={adapter.configuration.useSSL || false}
                  onCheckedChange={(checked) => handleInputChange('useSSL', checked)}
                />
                <Label htmlFor="useSSL">Use SSL/TLS</Label>
              </div>

              {adapter.configuration.useSSL && (
                <div className="space-y-4">
                  <CertificateSelection
                    id="sslCertificate"
                    label="SSL Certificate"
                    value={adapter.configuration.sslCertificateId || ''}
                    onChange={(value) => handleInputChange('sslCertificateId', value)}
                    businessComponentId={businessComponentId}
                    placeholder="Select SSL certificate"
                    required
                  />
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Processing Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="queueName">Queue Name *</Label>
                  <Input
                    id="queueName"
                    placeholder="INBOUND.FILE.QUEUE"
                    value={adapter.configuration.queueName || ''}
                    onChange={(e) => handleInputChange('queueName', e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="destinationType">JMS Destination Type *</Label>
                  <Select
                    value={adapter.configuration.destinationType || ''}
                    onValueChange={(value) => handleInputChange('destinationType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select destination type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Queue">Queue</SelectItem>
                      <SelectItem value="Topic">Topic</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="messageSelector">Message Selector</Label>
                  <Input
                    id="messageSelector"
                    placeholder="JMSType='FileMessage'"
                    value={adapter.configuration.messageSelector || ''}
                    onChange={(e) => handleInputChange('messageSelector', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="clientId">Client ID</Label>
                  <Input
                    id="clientId"
                    placeholder="Optional Client ID"
                    value={adapter.configuration.clientId || ''}
                    onChange={(e) => handleInputChange('clientId', e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="ackMode">Acknowledgement Mode *</Label>
                  <Select
                    value={adapter.configuration.ackMode || 'AUTO_ACKNOWLEDGE'}
                    onValueChange={(value) => handleInputChange('ackMode', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select acknowledgement mode" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="AUTO_ACKNOWLEDGE">AUTO_ACKNOWLEDGE</SelectItem>
                      <SelectItem value="CLIENT_ACKNOWLEDGE">CLIENT_ACKNOWLEDGE</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}