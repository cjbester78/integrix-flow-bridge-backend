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

interface MailReceiverAdapterConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (key: string, value: any) => void;
}

export const MailReceiverAdapterConfiguration: React.FC<MailReceiverAdapterConfigurationProps> = ({
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
              <CardTitle>Mail Server Connection</CardTitle>
              <CardDescription>Configure connection to the incoming mail server</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="mailServerHost">Mail Server Host</Label>
                <Input
                  id="mailServerHost"
                  value={configuration.mailServerHost || ''}
                  onChange={(e) => onConfigurationChange('mailServerHost', e.target.value)}
                  placeholder="imap.mailserver.com, pop3.mail.com"
                />
                <p className="text-sm text-muted-foreground">Hostname or IP of the incoming mail server</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="mailServerPort">Mail Server Port</Label>
                  <Input
                    id="mailServerPort"
                    type="number"
                    value={configuration.mailServerPort || ''}
                    onChange={(e) => onConfigurationChange('mailServerPort', e.target.value)}
                    placeholder="993 for IMAP SSL, 995 for POP3 SSL"
                  />
                  <p className="text-xs text-muted-foreground">Port number for IMAP/POP3 service</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="mailProtocol">Protocol</Label>
                  <Select 
                    value={configuration.mailProtocol || ''} 
                    onValueChange={(value) => onConfigurationChange('mailProtocol', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select mail protocol" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="IMAP">IMAP</SelectItem>
                      <SelectItem value="POP3">POP3</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-muted-foreground">Mail protocol used</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="mailUsername">Username</Label>
                  <Input
                    id="mailUsername"
                    value={configuration.mailUsername || ''}
                    onChange={(e) => onConfigurationChange('mailUsername', e.target.value)}
                    placeholder="user@example.com"
                  />
                  <p className="text-sm text-muted-foreground">Mail account username</p>
                </div>

                <div className="space-y-2">
                  <PasswordConfirmation
                    name="mailPassword"
                    label="Password"
                    placeholder="password"
                    value={configuration.mailPassword || ''}
                    onValueChange={(value) => onConfigurationChange('mailPassword', value)}
                    showConfirmation={false}
                  />
                  <p className="text-sm text-muted-foreground">Mail account password</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="useSSLTLS"
                    checked={configuration.useSSLTLS || false}
                    onCheckedChange={(checked) => onConfigurationChange('useSSLTLS', checked)}
                  />
                  <Label htmlFor="useSSLTLS" className="text-sm font-normal">
                    Use SSL/TLS
                  </Label>
                  <p className="text-xs text-muted-foreground">Enable secure connection</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="folderName">Folder Name</Label>
                  <Input
                    id="folderName"
                    value={configuration.folderName || 'INBOX'}
                    onChange={(e) => onConfigurationChange('folderName', e.target.value)}
                    placeholder="INBOX"
                  />
                  <p className="text-xs text-muted-foreground">Mail folder to poll</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="pollingInterval">Polling Interval (ms)</Label>
                  <Input
                    id="pollingInterval"
                    type="number"
                    value={configuration.pollingInterval || ''}
                    onChange={(e) => onConfigurationChange('pollingInterval', e.target.value)}
                    placeholder="60000"
                  />
                  <p className="text-xs text-muted-foreground">Frequency of checking for new emails</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Message Filtering and Fetching</CardTitle>
              <CardDescription>Configure email search criteria and limits</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="searchCriteria">Search Criteria</Label>
                <Input
                  id="searchCriteria"
                  value={configuration.searchCriteria || ''}
                  onChange={(e) => onConfigurationChange('searchCriteria', e.target.value)}
                  placeholder="UNSEEN or FROM:support@example.com"
                />
                <p className="text-sm text-muted-foreground">Email search/filter criteria (subject, sender, flags)</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="maxMessages">Max Messages</Label>
                <Input
                  id="maxMessages"
                  type="number"
                  value={configuration.maxMessages || ''}
                  onChange={(e) => onConfigurationChange('maxMessages', e.target.value)}
                  placeholder="100"
                />
                <p className="text-sm text-muted-foreground">Maximum number of emails to fetch per poll</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="processing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Message Processing</CardTitle>
              <CardDescription>Configure how emails are processed after retrieval</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="contentHandling">Content Handling</Label>
                <Textarea
                  id="contentHandling"
                  value={configuration.contentHandling || ''}
                  onChange={(e) => onConfigurationChange('contentHandling', e.target.value)}
                  placeholder="Save attachments, parse body text"
                  rows={3}
                />
                <p className="text-sm text-muted-foreground">How to handle email content (plain text, HTML, attachments)</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="mailEncoding">Encoding</Label>
                  <Select 
                    value={configuration.mailEncoding || ''} 
                    onValueChange={(value) => onConfigurationChange('mailEncoding', value)}
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
                  <p className="text-sm text-muted-foreground">Character encoding used</p>
                </div>

                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="deleteAfterFetch"
                    checked={configuration.deleteAfterFetch || false}
                    onCheckedChange={(checked) => onConfigurationChange('deleteAfterFetch', checked)}
                  />
                  <Label htmlFor="deleteAfterFetch" className="text-sm font-normal">
                    Delete After Fetch
                  </Label>
                  <p className="text-xs text-muted-foreground">Delete emails after processing</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};