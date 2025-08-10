import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { useSystemLogs } from '@/hooks/useSystemLogs';
import { useDomainLogs, DomainType } from '@/hooks/useDomainLogs';
import { SystemLogViewer } from '@/components/SystemLogViewer';
import { DomainLogViewer } from '@/components/DomainLogViewer';
import { LogFilters } from '@/components/adapter/LogFilters';
import { LogExport } from '@/components/adapter/LogExport';
import { ScrollText, Search, Filter, Download, RefreshCw, Database, Workflow, Network, User, Settings, MessageSquare } from 'lucide-react';

export const SystemLogs = () => {
  const [selectedSource, setSelectedSource] = useState<'adapter' | 'system' | 'channel' | 'flow' | 'api' | ''>('');
  const [selectedSourceId, setSelectedSourceId] = useState<string>('');
  const [logLevel, setLogLevel] = useState<'info' | 'warn' | 'error' | 'debug' | ''>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [dateRange, setDateRange] = useState<{ start?: string; end?: string }>({});
  const [activeTab, setActiveTab] = useState('system');

  const { logs, loading, error, refetch, sources } = useSystemLogs({
    source: selectedSource || undefined,
    sourceId: selectedSourceId || undefined,
    level: logLevel || undefined,
    search: searchQuery,
    startDate: dateRange.start,
    endDate: dateRange.end,
  });

  // Domain-specific log hooks
  const userLogs = useDomainLogs({ domainType: 'UserManagement', includeSystemLogs: false });
  const flowLogs = useDomainLogs({ domainType: 'FlowEngine', includeSystemLogs: false });
  const adapterLogs = useDomainLogs({ domainType: 'AdapterManagement', includeSystemLogs: false });
  const structureLogs = useDomainLogs({ domainType: 'DataStructures', includeSystemLogs: false });
  const channelLogs = useDomainLogs({ domainType: 'ChannelManagement', includeSystemLogs: false });
  const messageLogs = useDomainLogs({ domainType: 'MessageProcessing', includeSystemLogs: false });

  const handleRefresh = () => {
    refetch();
    // Refetch domain logs as well
    userLogs.refetch();
    flowLogs.refetch();
    adapterLogs.refetch();
    structureLogs.refetch();
    channelLogs.refetch();
    messageLogs.refetch();
  };

  const handleFilterChange = (filters: any) => {
    setLogLevel(filters.level);
    setDateRange(filters.dateRange);
  };

  const getDomainTabIcon = (domainType: DomainType) => {
    const iconMap = {
      UserManagement: User,
      FlowEngine: Workflow,
      AdapterManagement: Network,
      DataStructures: Database,
      ChannelManagement: Settings,
      MessageProcessing: MessageSquare,
    };
    return iconMap[domainType];
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-foreground flex items-center gap-3">
            <ScrollText className="h-7 w-7" />
            System Logs
          </h2>
          <p className="text-muted-foreground">Monitor and analyze all system logs including errors, adapters, channels, and flows</p>
        </div>
        <div className="flex gap-2">
          <LogExport adapterId={selectedSourceId} />
          <Button variant="outline" size="sm" onClick={handleRefresh} className="hover-scale">
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Filters Section */}
      <Card className="bg-gradient-secondary border-border/50">
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Source Type</label>
              <Select value={selectedSource} onValueChange={(value: any) => setSelectedSource(value)}>
                <SelectTrigger>
                  <SelectValue placeholder="All sources" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="system">System</SelectItem>
                  <SelectItem value="adapter">Adapters</SelectItem>
                  <SelectItem value="channel">Channels</SelectItem>
                  <SelectItem value="flow">Flows</SelectItem>
                  <SelectItem value="api">API</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {selectedSource && selectedSource !== 'system' && selectedSource !== 'api' && (
              <div className="space-y-2">
                <label className="text-sm font-medium">Specific Source</label>
                <Select value={selectedSourceId} onValueChange={setSelectedSourceId}>
                  <SelectTrigger>
                    <SelectValue placeholder={`Select ${selectedSource}`} />
                  </SelectTrigger>
                  <SelectContent>
                    {selectedSource === 'adapter' && sources.adapters.map((adapter) => (
                      <SelectItem key={adapter.id} value={adapter.id!}>
                        {adapter.name}
                      </SelectItem>
                    ))}
                    {selectedSource === 'channel' && sources.channels.map((channel) => (
                      <SelectItem key={channel.id} value={channel.id!}>
                        {channel.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            <div className="space-y-2">
              <label className="text-sm font-medium">Log Level</label>
              <Select value={logLevel} onValueChange={(value: any) => setLogLevel(value)}>
                <SelectTrigger>
                  <SelectValue placeholder="All levels" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="debug">Debug</SelectItem>
                  <SelectItem value="info">Info</SelectItem>
                  <SelectItem value="warn">Warning</SelectItem>
                  <SelectItem value="error">Error</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Search</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search logs..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
          </div>

          <LogFilters onFilterChange={handleFilterChange} />
        </CardContent>
      </Card>

      {/* Log Stats */}
      {logs.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="bg-gradient-secondary border-border/50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Logs</p>
                  <p className="text-2xl font-bold">{logs.length}</p>
                </div>
                <Badge variant="outline">24h</Badge>
              </div>
            </CardContent>
          </Card>
          <Card className="bg-gradient-secondary border-border/50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Errors</p>
                  <p className="text-2xl font-bold text-destructive">
                    {logs.filter(log => log.level === 'error').length}
                  </p>
                </div>
                <Badge variant="destructive">Error</Badge>
              </div>
            </CardContent>
          </Card>
          <Card className="bg-gradient-secondary border-border/50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Warnings</p>
                  <p className="text-2xl font-bold text-warning">
                    {logs.filter(log => log.level === 'warn').length}
                  </p>
                </div>
                <Badge variant="secondary">Warning</Badge>
              </div>
            </CardContent>
          </Card>
          <Card className="bg-gradient-secondary border-border/50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Info</p>
                  <p className="text-2xl font-bold text-success">
                    {logs.filter(log => log.level === 'info').length}
                  </p>
                </div>
                <Badge variant="outline">Info</Badge>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Comprehensive Log Viewer with Domain Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-7">
          <TabsTrigger value="system" className="flex items-center gap-2">
            <ScrollText className="h-4 w-4" />
            System Logs
          </TabsTrigger>
          <TabsTrigger value="user" className="flex items-center gap-2">
            <User className="h-4 w-4" />
            Users
          </TabsTrigger>
          <TabsTrigger value="flow" className="flex items-center gap-2">
            <Workflow className="h-4 w-4" />
            Flows
          </TabsTrigger>
          <TabsTrigger value="adapter" className="flex items-center gap-2">
            <Network className="h-4 w-4" />
            Adapters
          </TabsTrigger>
          <TabsTrigger value="structure" className="flex items-center gap-2">
            <Database className="h-4 w-4" />
            Structures
          </TabsTrigger>
          <TabsTrigger value="channel" className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            Channels
          </TabsTrigger>
          <TabsTrigger value="message" className="flex items-center gap-2">
            <MessageSquare className="h-4 w-4" />
            Messages
          </TabsTrigger>
        </TabsList>

        <TabsContent value="system">
          <SystemLogViewer 
            logs={logs} 
            loading={loading} 
            error={error}
            selectedSource={selectedSource}
          />
        </TabsContent>

        <TabsContent value="user">
          <DomainLogViewer
            domainType="UserManagement"
            domainErrors={userLogs.domainErrors}
            systemLogs={userLogs.systemLogs}
            loading={userLogs.loading}
            error={userLogs.error}
          />
        </TabsContent>

        <TabsContent value="flow">
          <DomainLogViewer
            domainType="FlowEngine"
            domainErrors={flowLogs.domainErrors}
            systemLogs={flowLogs.systemLogs}
            loading={flowLogs.loading}
            error={flowLogs.error}
          />
        </TabsContent>

        <TabsContent value="adapter">
          <DomainLogViewer
            domainType="AdapterManagement"
            domainErrors={adapterLogs.domainErrors}
            systemLogs={adapterLogs.systemLogs}
            loading={adapterLogs.loading}
            error={adapterLogs.error}
          />
        </TabsContent>

        <TabsContent value="structure">
          <DomainLogViewer
            domainType="DataStructures"
            domainErrors={structureLogs.domainErrors}
            systemLogs={structureLogs.systemLogs}
            loading={structureLogs.loading}
            error={structureLogs.error}
          />
        </TabsContent>

        <TabsContent value="channel">
          <DomainLogViewer
            domainType="ChannelManagement"
            domainErrors={channelLogs.domainErrors}
            systemLogs={channelLogs.systemLogs}
            loading={channelLogs.loading}
            error={channelLogs.error}
          />
        </TabsContent>

        <TabsContent value="message">
          <DomainLogViewer
            domainType="MessageProcessing"
            domainErrors={messageLogs.domainErrors}
            systemLogs={messageLogs.systemLogs}
            loading={messageLogs.loading}
            error={messageLogs.error}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};