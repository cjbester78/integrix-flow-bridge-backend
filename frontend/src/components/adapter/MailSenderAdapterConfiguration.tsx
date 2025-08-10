import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { AdapterConfiguration } from '@/types/adapter';

interface MailSenderAdapterConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (key: string, value: any) => void;
}

export const MailSenderAdapterConfiguration: React.FC<MailSenderAdapterConfigurationProps> = ({
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
              <CardTitle>SMTP Server Connection</CardTitle>
              <CardDescription>Configure connection to the SMTP server for sending emails</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="smtpServerHost">SMTP Server Host</Label>
                <Input
                  id="smtpServerHost"
                  value={configuration.smtpServerHost || ''}
                  onChange={(e) => onConfigurationChange('smtpServerHost', e.target.value)}
                  placeholder="smtp.mailserver.com"
                />
                <p className="text-sm text-muted-foreground">Hostname or IP of the SMTP server</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smtpServerPort">SMTP Server Port</Label>
                  <Input
                    id="smtpServerPort"
                    type="number"
                    value={configuration.smtpServerPort || ''}
                    onChange={(e) => onConfigurationChange('smtpServerPort', e.target.value)}
                    placeholder="587 (TLS), 465 (SSL)"
                  />
                  <p className="text-xs text-muted-foreground">Port number for SMTP service</p>
                </div>

                <div className="flex items-center space-x-2 pt-7">
                  <Checkbox
                    id="smtpUseSSLTLS"
                    checked={configuration.smtpUseSSLTLS || false}
                    onCheckedChange={(checked) => onConfigurationChange('smtpUseSSLTLS', checked)}
                  />
                  <Label htmlFor="smtpUseSSLTLS" className="text-sm font-normal">
                    Use SSL/TLS
                  </Label>
                  <p className="text-xs text-muted-foreground">Enable secure connection</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smtpUsername">Username</Label>
                  <Input
                    id="smtpUsername"
                    value={configuration.smtpUsername || ''}
                    onChange={(e) => onConfigurationChange('smtpUsername', e.target.value)}
                    placeholder="user@example.com"
                  />
                  <p className="text-sm text-muted-foreground">SMTP authentication username</p>
                </div>

                <div className="space-y-2">
                  <PasswordConfirmation
                    name="smtpPassword"
                    label="Password"
                    placeholder="password"
                    value={configuration.smtpPassword || ''}
                    onValueChange={(value) => onConfigurationChange('smtpPassword', value)}
                    showConfirmation={false}
                  />
                  <p className="text-sm text-muted-foreground">SMTP authentication password</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="connectionTimeout">Connection Timeout (ms)</Label>
                  <Input
                    id="connectionTimeout"
                    type="number"
                    value={configuration.connectionTimeout || ''}
                    onChange={(e) => onConfigurationChange('connectionTimeout', e.target.value)}
                    placeholder="30000"
                  />
                  <p className="text-sm text-muted-foreground">Connection timeout in milliseconds</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="readTimeout">Read Timeout (ms)</Label>
                  <Input
                    id="readTimeout"
                    type="number"
                    value={configuration.readTimeout || ''}
                    onChange={(e) => onConfigurationChange('readTimeout', e.target.value)}
                    placeholder="30000"
                  />
                  <p className="text-sm text-muted-foreground">Read timeout in milliseconds</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Message Preparation</CardTitle>
              <CardDescription>Configure email content and recipients</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="fromAddress">From Address</Label>
                <Input
                  id="fromAddress"
                  value={configuration.fromAddress || ''}
                  onChange={(e) => onConfigurationChange('fromAddress', e.target.value)}
                  placeholder="sender@example.com"
                />
                <p className="text-sm text-muted-foreground">Sender's email address</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="toAddresses">To Addresses</Label>
                <Textarea
                  id="toAddresses"
                  value={configuration.toAddresses || ''}
                  onChange={(e) => onConfigurationChange('toAddresses', e.target.value)}
                  placeholder="recipient1@example.com, recipient2@example.com"
                  rows={2}
                />
                <p className="text-sm text-muted-foreground">Recipient email addresses (comma-separated)</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="ccAddresses">CC Addresses</Label>
                  <Textarea
                    id="ccAddresses"
                    value={configuration.ccAddresses || ''}
                    onChange={(e) => onConfigurationChange('ccAddresses', e.target.value)}
                    placeholder="cc1@example.com, cc2@example.com"
                    rows={2}
                  />
                  <p className="text-xs text-muted-foreground">CC recipients (optional)</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="bccAddresses">BCC Addresses</Label>
                  <Textarea
                    id="bccAddresses"
                    value={configuration.bccAddresses || ''}
                    onChange={(e) => onConfigurationChange('bccAddresses', e.target.value)}
                    placeholder="bcc1@example.com, bcc2@example.com"
                    rows={2}
                  />
                  <p className="text-xs text-muted-foreground">BCC recipients (optional)</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="emailSubject">Subject</Label>
                <Input
                  id="emailSubject"
                  value={configuration.emailSubject || ''}
                  onChange={(e) => onConfigurationChange('emailSubject', e.target.value)}
                  placeholder="Order Confirmation"
                />
                <p className="text-sm text-muted-foreground">Email subject line</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="emailBody">Body</Label>
                <Textarea
                  id="emailBody"
                  value={configuration.emailBody || ''}
                  onChange={(e) => onConfigurationChange('emailBody', e.target.value)}
                  placeholder="Email content - plain text or HTML"
                  rows={6}
                />
                <p className="text-sm text-muted-foreground">Email body (plain text or HTML content)</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="emailAttachments">Attachments</Label>
                  <Textarea
                    id="emailAttachments"
                    value={configuration.emailAttachments || ''}
                    onChange={(e) => onConfigurationChange('emailAttachments', e.target.value)}
                    placeholder="PDF, images, etc."
                    rows={2}
                  />
                  <p className="text-xs text-muted-foreground">Files to attach</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="emailEncoding">Encoding</Label>
                  <Select 
                    value={configuration.emailEncoding || ''} 
                    onValueChange={(value) => onConfigurationChange('emailEncoding', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select encoding" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="UTF-8">UTF-8</SelectItem>
                      <SelectItem value="ISO-8859-1">ISO-8859-1</SelectItem>
                      <SelectItem value="ASCII">ASCII</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-muted-foreground">Character encoding used</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};