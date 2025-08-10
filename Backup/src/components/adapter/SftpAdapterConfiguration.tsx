import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';

interface SftpAdapterConfigurationProps {
  mode: 'sender' | 'receiver';
  onConfigChange?: (config: SftpAdapterConfig) => void;
}

interface FileAccessAdvancedEntry {
  directory: string;
  fileName: string;
  exclusionMask: string;
}

interface SftpAdapterConfig {
  // Common Connection Parameters
  serverAddress: string;
  port: string;
  timeout: string;
  authenticationType: string;
  userName: string;
  password: string;
  publicKey: string;
  privateKey: string;
  passphrase: string;
  maxConcurrentConnections: string;
  retryConnection: string;
  retryConnectionDelay: string;
  connectionMode: string;
  
  // Sender-specific fields
  sourceDirectory: string;
  fileName: string;
  advancedSelection: boolean;
  exclusionMask: string;
  advancedEntries: FileAccessAdvancedEntry[];
  pollingInterval: string;
  processingMode: string;
  emptyFileHandling: string;
  enableDuplicateHandling: boolean;
  duplicateMessageAlertThreshold: string;
  disableChannelOnExceed: boolean;
  
  // Receiver-specific fields
  targetDirectory: string;
  targetFileName: string;
  fileConstructionMode: string;
  overwriteExistingFile: boolean;
  createFileDirectory: boolean;
  filePlacement: string;
  emptyMessageHandling_receiver: string;
}

export const SftpAdapterConfiguration = ({ mode, onConfigChange }: SftpAdapterConfigurationProps) => {
  const [config, setConfig] = useState<SftpAdapterConfig>({
    // Common connection parameters
    serverAddress: '',
    port: '22',
    timeout: '',
    authenticationType: '',
    userName: '',
    password: '',
    publicKey: '',
    privateKey: '',
    passphrase: '',
    maxConcurrentConnections: '',
    retryConnection: '',
    retryConnectionDelay: '',
    connectionMode: '',
    
    // Sender-specific fields
    sourceDirectory: '',
    fileName: '',
    advancedSelection: false,
    exclusionMask: '',
    advancedEntries: [{ directory: '', fileName: '', exclusionMask: '' }],
    pollingInterval: '',
    processingMode: '',
    emptyFileHandling: '',
    enableDuplicateHandling: false,
    duplicateMessageAlertThreshold: '',
    disableChannelOnExceed: false,
    
    // Receiver-specific fields
    targetDirectory: '',
    targetFileName: '',
    fileConstructionMode: '',
    overwriteExistingFile: false,
    createFileDirectory: false,
    filePlacement: '',
    emptyMessageHandling_receiver: '',
  });

  const updateConfig = (updates: Partial<SftpAdapterConfig>) => {
    const newConfig = { ...config, ...updates };
    setConfig(newConfig);
    onConfigChange?.(newConfig);
  };

  const addAdvancedEntry = () => {
    const newEntries = [...config.advancedEntries, { directory: '', fileName: '', exclusionMask: '' }];
    updateConfig({ advancedEntries: newEntries });
  };

  const updateAdvancedEntry = (index: number, field: keyof FileAccessAdvancedEntry, value: string) => {
    const newEntries = [...config.advancedEntries];
    newEntries[index] = { ...newEntries[index], [field]: value };
    updateConfig({ advancedEntries: newEntries });
  };

  const removeAdvancedEntry = (index: number) => {
    if (config.advancedEntries.length > 1) {
      const newEntries = config.advancedEntries.filter((_, i) => i !== index);
      updateConfig({ advancedEntries: newEntries });
    }
  };

  const renderAuthenticationFields = () => {
    switch (config.authenticationType) {
      case 'public-key':
        return (
          <>
            <div className="space-y-2">
              <Label htmlFor="keyPairSelection">Key Pair Selection *</Label>
              <Select value={config.publicKey} onValueChange={(value) => updateConfig({ publicKey: value })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a key pair from system" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="rsa-key-prod-01">RSA Production Key 01</SelectItem>
                  <SelectItem value="rsa-key-dev-01">RSA Development Key 01</SelectItem>
                  <SelectItem value="ecdsa-key-prod-01">ECDSA Production Key 01</SelectItem>
                  <SelectItem value="ed25519-key-prod-01">Ed25519 Production Key 01</SelectItem>
                  <SelectItem value="rsa-key-staging-01">RSA Staging Key 01</SelectItem>
                  <SelectItem value="custom-key-pair-01">Custom Key Pair 01</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="md:col-span-2">
              <PasswordConfirmation
                name="passphrase"
                label="Passphrase (optional)"
                placeholder="Enter passphrase for selected key pair"
                required={false}
                value={config.passphrase}
                onValueChange={(value) => updateConfig({ passphrase: value })}
              />
            </div>
          </>
        );
      case 'username-password':
        return (
          <>
            <div className="space-y-2">
              <Label htmlFor="userName">User Name *</Label>
              <Input
                id="userName"
                value={config.userName}
                onChange={(e) => updateConfig({ userName: e.target.value })}
                placeholder="Enter username"
              />
            </div>
            <div className="md:col-span-2">
              <PasswordConfirmation
                name="password"
                label="Password"
                placeholder="Enter password"
                required={true}
                value={config.password}
                onValueChange={(value) => updateConfig({ password: value })}
              />
            </div>
          </>
        );
      case 'dual':
        return (
          <>
            <div className="space-y-2">
              <Label htmlFor="userName">User Name *</Label>
              <Input
                id="userName"
                value={config.userName}
                onChange={(e) => updateConfig({ userName: e.target.value })}
                placeholder="Enter username"
              />
            </div>
            <div className="md:col-span-2">
              <PasswordConfirmation
                name="password"
                label="Password"
                placeholder="Enter password"
                required={true}
                value={config.password}
                onValueChange={(value) => updateConfig({ password: value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="keyPairSelection">Key Pair Selection *</Label>
              <Select value={config.publicKey} onValueChange={(value) => updateConfig({ publicKey: value })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a key pair from system" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="rsa-key-prod-01">RSA Production Key 01</SelectItem>
                  <SelectItem value="rsa-key-dev-01">RSA Development Key 01</SelectItem>
                  <SelectItem value="ecdsa-key-prod-01">ECDSA Production Key 01</SelectItem>
                  <SelectItem value="ed25519-key-prod-01">Ed25519 Production Key 01</SelectItem>
                  <SelectItem value="rsa-key-staging-01">RSA Staging Key 01</SelectItem>
                  <SelectItem value="custom-key-pair-01">Custom Key Pair 01</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="md:col-span-2">
              <PasswordConfirmation
                name="passphrase"
                label="Passphrase (optional)"
                placeholder="Enter passphrase for selected key pair"
                required={false}
                value={config.passphrase}
                onValueChange={(value) => updateConfig({ passphrase: value })}
              />
            </div>
          </>
        );
      case 'dynamic':
        return (
          <div className="md:col-span-2 p-4 border rounded-lg bg-muted/20">
            <p className="text-sm text-muted-foreground">
              Dynamic authentication will be configured at runtime based on message content.
            </p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>SFTP {mode === 'sender' ? 'Sender' : 'Receiver'} Adapter Configuration</CardTitle>
        <CardDescription>Configure your SFTP {mode} adapter settings</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue={mode === 'sender' ? 'source' : 'target'} className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value={mode === 'sender' ? 'source' : 'target'}>
              {mode === 'sender' ? 'Source' : 'Target'}
            </TabsTrigger>
            <TabsTrigger value="processing">Processing</TabsTrigger>
          </TabsList>

          {/* Sender Source Tab */}
          {mode === 'sender' && (
            <TabsContent value="source" className="space-y-6 mt-6">
              {/* File Access Parameters Section */}
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold mb-4">File Access Parameters</h3>
                  <Separator className="mb-4" />
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="sourceDirectory">Source Directory *</Label>
                    <Input
                      id="sourceDirectory"
                      value={config.sourceDirectory}
                      onChange={(e) => updateConfig({ sourceDirectory: e.target.value })}
                      placeholder="Enter source directory path"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="fileName">File Name *</Label>
                    <Input
                      id="fileName"
                      value={config.fileName}
                      onChange={(e) => updateConfig({ fileName: e.target.value })}
                      placeholder="Enter file name pattern"
                    />
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="advancedSelection"
                      checked={config.advancedSelection}
                      onCheckedChange={(checked) => updateConfig({ advancedSelection: checked === true })}
                    />
                    <Label htmlFor="advancedSelection">Advanced selection for Source files</Label>
                  </div>

                  {config.advancedSelection && (
                    <div className="space-y-4 p-4 border rounded-lg bg-muted/20">
                      <div className="space-y-2">
                        <Label htmlFor="exclusionMask">Exclusion Mask</Label>
                        <Input
                          id="exclusionMask"
                          value={config.exclusionMask}
                          onChange={(e) => updateConfig({ exclusionMask: e.target.value })}
                          placeholder="Enter exclusion pattern (e.g., *.tmp, backup_*)"
                        />
                      </div>
                      
                      <div className="flex justify-between items-center">
                        <Label className="text-sm font-medium">Multiple File Selection</Label>
                        <button
                          type="button"
                          onClick={addAdvancedEntry}
                          className="text-sm text-primary hover:underline"
                        >
                          + Add Entry
                        </button>
                      </div>
                      
                      {config.advancedEntries.map((entry, index) => (
                        <div key={index} className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
                          <div className="space-y-1">
                            <Label className="text-xs">Directory</Label>
                            <Input
                              value={entry.directory}
                              onChange={(e) => updateAdvancedEntry(index, 'directory', e.target.value)}
                              placeholder="Directory path"
                              className="text-sm"
                            />
                          </div>
                          <div className="space-y-1">
                            <Label className="text-xs">File Name</Label>
                            <Input
                              value={entry.fileName}
                              onChange={(e) => updateAdvancedEntry(index, 'fileName', e.target.value)}
                              placeholder="File name pattern"
                              className="text-sm"
                            />
                          </div>
                          <div className="space-y-1">
                            <Label className="text-xs">Exclusion Mask</Label>
                            <Input
                              value={entry.exclusionMask}
                              onChange={(e) => updateAdvancedEntry(index, 'exclusionMask', e.target.value)}
                              placeholder="Exclusion pattern"
                              className="text-sm"
                            />
                          </div>
                          {config.advancedEntries.length > 1 && (
                            <button
                              type="button"
                              onClick={() => removeAdvancedEntry(index)}
                              className="text-xs text-destructive hover:underline self-end pb-2"
                            >
                              Remove
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Connection Parameters Section */}
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Connection Parameters</h3>
                  <Separator className="mb-4" />
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="serverAddress">Server Address *</Label>
                    <Input
                      id="serverAddress"
                      value={config.serverAddress}
                      onChange={(e) => updateConfig({ serverAddress: e.target.value })}
                      placeholder="Enter server address"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="port">Port *</Label>
                    <Input
                      id="port"
                      value={config.port}
                      onChange={(e) => updateConfig({ port: e.target.value })}
                      placeholder="Enter port number (default: 22)"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="timeout">Timeout</Label>
                    <Input
                      id="timeout"
                      value={config.timeout}
                      onChange={(e) => updateConfig({ timeout: e.target.value })}
                      placeholder="Timeout in seconds"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="authenticationType">Authentication *</Label>
                    <Select value={config.authenticationType} onValueChange={(value) => updateConfig({ authenticationType: value })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select authentication type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="public-key">Public Key</SelectItem>
                        <SelectItem value="username-password">User Name/Password</SelectItem>
                        <SelectItem value="dual">Dual</SelectItem>
                        <SelectItem value="dynamic">Dynamic</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {renderAuthenticationFields()}
                  
                  <div className="space-y-2">
                    <Label htmlFor="maxConcurrentConnections">Maximum Concurrent Connections</Label>
                    <Input
                      id="maxConcurrentConnections"
                      value={config.maxConcurrentConnections}
                      onChange={(e) => updateConfig({ maxConcurrentConnections: e.target.value })}
                      placeholder="Enter max concurrent connections"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="retryConnection">Retry Connection</Label>
                    <Input
                      id="retryConnection"
                      value={config.retryConnection}
                      onChange={(e) => updateConfig({ retryConnection: e.target.value })}
                      placeholder="Number of retry attempts"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="retryConnectionDelay">Retry Connection Delay "in sec"</Label>
                    <Input
                      id="retryConnectionDelay"
                      value={config.retryConnectionDelay}
                      onChange={(e) => updateConfig({ retryConnectionDelay: e.target.value })}
                      placeholder="Delay between retries in seconds"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="connectionMode">Connection Mode *</Label>
                    <Select value={config.connectionMode} onValueChange={(value) => updateConfig({ connectionMode: value })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select connection mode" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="permanently">Permanently</SelectItem>
                        <SelectItem value="per-file-transfer">Per File Transfer</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </TabsContent>
          )}

          {/* Receiver Target Tab */}
          {mode === 'receiver' && (
            <TabsContent value="target" className="space-y-6 mt-6">
              {/* File Access Parameters Section */}
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold mb-4">File Access Parameters</h3>
                  <Separator className="mb-4" />
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="targetDirectory">Target Directory *</Label>
                    <Input
                      id="targetDirectory"
                      value={config.targetDirectory}
                      onChange={(e) => updateConfig({ targetDirectory: e.target.value })}
                      placeholder="Enter target directory path"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="targetFileName">File Name *</Label>
                    <Input
                      id="targetFileName"
                      value={config.targetFileName}
                      onChange={(e) => updateConfig({ targetFileName: e.target.value })}
                      placeholder="Enter file name"
                    />
                  </div>
                </div>
              </div>

              {/* Connection Parameters Section */}
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Connection Parameters</h3>
                  <Separator className="mb-4" />
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="serverAddress">Server Address *</Label>
                    <Input
                      id="serverAddress"
                      value={config.serverAddress}
                      onChange={(e) => updateConfig({ serverAddress: e.target.value })}
                      placeholder="Enter server address"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="port">Port *</Label>
                    <Input
                      id="port"
                      value={config.port}
                      onChange={(e) => updateConfig({ port: e.target.value })}
                      placeholder="Enter port number (default: 22)"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="timeout">Timeout</Label>
                    <Input
                      id="timeout"
                      value={config.timeout}
                      onChange={(e) => updateConfig({ timeout: e.target.value })}
                      placeholder="Timeout in seconds"
                      type="number"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="authenticationType">Authentication *</Label>
                    <Select value={config.authenticationType} onValueChange={(value) => updateConfig({ authenticationType: value })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select authentication type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="public-key">Public Key</SelectItem>
                        <SelectItem value="username-password">User Name/Password</SelectItem>
                        <SelectItem value="dual">Dual</SelectItem>
                        <SelectItem value="dynamic">Dynamic</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {renderAuthenticationFields()}
                  
                  <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="connectionMode">Connection Mode *</Label>
                    <Select value={config.connectionMode} onValueChange={(value) => updateConfig({ connectionMode: value })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select connection mode" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="permanently">Permanently</SelectItem>
                        <SelectItem value="per-file-transfer">Per File Transfer</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </TabsContent>
          )}

          {/* Processing Tab */}
          <TabsContent value="processing" className="space-y-6 mt-6">
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold mb-4">Processing Parameters</h3>
                <Separator className="mb-4" />
              </div>
              
              {mode === 'sender' ? (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="pollingInterval">Polling Interval "Secs" *</Label>
                      <Input
                        id="pollingInterval"
                        value={config.pollingInterval}
                        onChange={(e) => updateConfig({ pollingInterval: e.target.value })}
                        placeholder="Enter polling interval in seconds"
                        type="number"
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="processingMode">Processing Mode *</Label>
                      <Select value={config.processingMode} onValueChange={(value) => updateConfig({ processingMode: value })}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select processing mode" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="archive">Archive</SelectItem>
                          <SelectItem value="delete">Delete</SelectItem>
                          <SelectItem value="keep-file">Keep File</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    
                    <div className="space-y-2 md:col-span-2">
                      <Label htmlFor="emptyFileHandling">Empty File Handling *</Label>
                      <Select value={config.emptyFileHandling} onValueChange={(value) => updateConfig({ emptyFileHandling: value })}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select empty file handling" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="do-not-create-message">Do not create message</SelectItem>
                          <SelectItem value="process-empty-files">Process empty files</SelectItem>
                          <SelectItem value="skip-empty-files">Skip empty files</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  {/* Duplicate Handling Section */}
                  <div className="space-y-4">
                    <div>
                      <h3 className="text-lg font-semibold mb-4">Duplicate Handling</h3>
                      <Separator className="mb-4" />
                    </div>
                    
                    <div className="space-y-4">
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id="enableDuplicateHandling"
                          checked={config.enableDuplicateHandling}
                          onCheckedChange={(checked) => updateConfig({ enableDuplicateHandling: checked === true })}
                        />
                        <Label htmlFor="enableDuplicateHandling">Enable Duplicate Handling</Label>
                      </div>

                      {config.enableDuplicateHandling && (
                        <div className="space-y-4 p-4 border rounded-lg bg-muted/20">
                          <div className="space-y-2">
                            <Label htmlFor="duplicateMessageAlertThreshold">Duplicate message alert threshold</Label>
                            <Input
                              id="duplicateMessageAlertThreshold"
                              value={config.duplicateMessageAlertThreshold}
                              onChange={(e) => updateConfig({ duplicateMessageAlertThreshold: e.target.value })}
                              placeholder="Enter threshold value"
                              type="number"
                            />
                          </div>
                          
                          <div className="flex items-center space-x-2">
                            <Checkbox
                              id="disableChannelOnExceed"
                              checked={config.disableChannelOnExceed}
                              onCheckedChange={(checked) => updateConfig({ disableChannelOnExceed: checked === true })}
                            />
                            <Label htmlFor="disableChannelOnExceed">Disable Channel if Duplicate threshold has been exceeded</Label>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="createFileDirectory"
                        checked={config.createFileDirectory}
                        onCheckedChange={(checked) => updateConfig({ createFileDirectory: checked === true })}
                      />
                      <Label htmlFor="createFileDirectory">Create File Directory</Label>
                    </div>
                    
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="overwriteExistingFile"
                        checked={config.overwriteExistingFile}
                        onCheckedChange={(checked) => updateConfig({ overwriteExistingFile: checked === true })}
                      />
                      <Label htmlFor="overwriteExistingFile">Overwrite Existing File</Label>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="fileConstructionMode">File Construction Mode *</Label>
                      <Select value={config.fileConstructionMode} onValueChange={(value) => updateConfig({ fileConstructionMode: value })}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select file construction mode" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="create">Create</SelectItem>
                          <SelectItem value="append">Append</SelectItem>
                          <SelectItem value="add-time-stamp">Add Time Stamp</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="filePlacement">File Placement *</Label>
                      <Select value={config.filePlacement} onValueChange={(value) => updateConfig({ filePlacement: value })}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select file placement" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="use-temporary-file">Use Temporary File</SelectItem>
                          <SelectItem value="write-out-directly">Write Out Directly</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="emptyMessageHandling_receiver">Empty-Message Handling *</Label>
                      <Select value={config.emptyMessageHandling_receiver} onValueChange={(value) => updateConfig({ emptyMessageHandling_receiver: value })}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select empty message handling" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="write-out-empty-file">Write Out Empty File</SelectItem>
                          <SelectItem value="ignore-empty-file">Ignore Empty File</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="maxConcurrentConnections">Maximum Concurrent Connections</Label>
                      <Input
                        id="maxConcurrentConnections"
                        value={config.maxConcurrentConnections}
                        onChange={(e) => updateConfig({ maxConcurrentConnections: e.target.value })}
                        placeholder="Enter max concurrent connections"
                        type="number"
                      />
                    </div>
                  </div>
                  
                </div>
              )}
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
};