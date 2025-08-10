// @ts-nocheck - Temporary suppression for unused imports/variables
import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { 
  Play, 
  Pause, 
  Settings, 
  Zap, 
  TrendingUp, 
  Clock, 
  Server,
  ChevronDown,
  ChevronRight,
  Activity,
  AlertCircle,
  BarChart3,
  Eye
} from 'lucide-react';
import { AdapterMonitoring, AdapterMonitoringStatus } from '@/services/adapterMonitoringService';
import { AdapterLogViewer } from './AdapterLogViewer';
import { useAdapterLogs } from '@/hooks/useAdapterLogs';

interface AdapterCardProps {
  adapter: AdapterMonitoring;
  onUpdate?: (updates: Partial<AdapterMonitoring>) => void;
  onDelete?: () => void;
}

export const AdapterCard = ({ adapter, onUpdate, onDelete }: AdapterCardProps) => {
  const [isLogsExpanded, setIsLogsExpanded] = useState(false);
  const [isMetricsExpanded, setIsMetricsExpanded] = useState(false);
  
  const { logs, loading: logsLoading, connected, refreshLogs, exportLogs } = useAdapterLogs(
    adapter.id, 
    { limit: 50 },
    isLogsExpanded // Only load logs when the section is expanded
  );

  // Load logs when the logs section is expanded
  useEffect(() => {
    if (isLogsExpanded) {
      refreshLogs();
    }
  }, [isLogsExpanded]);

  const getStatusColor = (status: AdapterMonitoringStatus) => {
    switch (status) {
      case 'running':
        return 'bg-success';
      case 'idle':
        return 'bg-warning';
      case 'stopped':
        return 'bg-destructive';
      default:
        return 'bg-muted-foreground';
    }
  };

  const getStatusVariant = (status: AdapterMonitoringStatus) => {
    switch (status) {
      case 'running':
        return 'default' as const;
      case 'idle':
        return 'secondary' as const;
      case 'stopped':
        return 'destructive' as const;
      default:
        return 'outline' as const;
    }
  };

  const getHealthScoreColor = (score: number) => {
    if (score >= 95) return 'text-success';
    if (score >= 85) return 'text-warning';
    return 'text-destructive';
  };

  const getAdapterStatusVariant = (status: string) => {
    switch (status) {
      case 'active':
        return 'default' as const;
      case 'inactive':
        return 'secondary' as const;
      case 'error':
        return 'destructive' as const;
      case 'testing':
        return 'outline' as const;
      default:
        return 'outline' as const;
    }
  };

  return (
    <Card className="bg-gradient-secondary border-border/50 animate-fade-in">
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          {/* Left Section - Channel Info */}
          <div className="flex items-center space-x-4">
            <div className={`h-3 w-3 rounded-full ${getStatusColor(adapter.status)}`} />
            <div>
              <div className="font-semibold text-lg">{adapter.name}</div>
              <div className="text-sm text-muted-foreground">{adapter.type} - {adapter.mode}</div>
            </div>
          </div>

          {/* Right Section - Status */}
          <div className="flex items-center space-x-2">
            <Badge 
              variant={getStatusVariant(adapter.status)}
              className="text-xs"
            >
              {adapter.status}
            </Badge>
          </div>
        </div>

        {/* Metrics Row */}
        <div className="flex items-center justify-between mt-6 pt-4 border-t border-border/10">
          {/* Load Progress */}
          <div className="flex-1 max-w-xs">
            <div className="flex items-center justify-between text-sm mb-2">
              <span className="text-muted-foreground">Load</span>
              <span className="font-medium">{adapter.load}%</span>
            </div>
            <Progress value={adapter.load} className="h-2" />
          </div>

          {/* Metrics */}
          <div className="flex items-center gap-8 ml-8">
            <div className="flex items-center gap-2">
              <Zap className="h-4 w-4 text-warning" />
              <div className="text-center">
                <div className="text-sm font-medium">{adapter.messagesProcessed || 0}</div>
                <div className="text-xs text-muted-foreground">messages</div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <div className="text-center">
                <div className="text-sm font-medium">{adapter.lastActivityTime || 'Never'}</div>
                <div className="text-xs text-muted-foreground">Last activity</div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-success" />
              <div className="text-center">
                <div className="text-sm font-medium">100%</div>
                <div className="text-xs text-muted-foreground">Uptime</div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <AlertCircle className="h-4 w-4 text-destructive" />
              <div className="text-center">
                <div className="text-sm font-medium">{adapter.errorsCount || 0}</div>
                <div className="text-xs text-muted-foreground">Errors</div>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2 mt-4">
          {/* Play/Pause Button */}
          <Button 
            variant="outline" 
            size="sm" 
            className="hover-scale"
            onClick={() => {
              // Handle start/stop adapter action
              console.log(`${adapter.status === 'running' ? 'Stopping' : 'Starting'} adapter ${adapter.id}`);
              if (onUpdate) {
                onUpdate({ status: adapter.status === 'running' ? 'stopped' : 'running' });
              }
            }}
          >
            {adapter.status === 'running' ? (
              <Pause className="h-4 w-4 mr-2" />
            ) : (
              <Play className="h-4 w-4 mr-2" />
            )}
            {adapter.status === 'running' ? 'Stop' : 'Start'}
          </Button>

          {/* Settings Button */}
          <Button 
            variant="outline" 
            size="sm" 
            className="hover-scale"
            onClick={() => {
              // Handle adapter settings
              console.log(`Opening settings for adapter ${adapter.id}`);
            }}
          >
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </Button>

          <Collapsible open={isLogsExpanded} onOpenChange={setIsLogsExpanded}>
            <CollapsibleTrigger asChild>
              <Button variant="outline" size="sm" className="hover-scale">
                <Eye className="h-4 w-4 mr-2" />
                View Logs
                {isLogsExpanded ? (
                  <ChevronDown className="h-4 w-4 ml-2" />
                ) : (
                  <ChevronRight className="h-4 w-4 ml-2" />
                )}
              </Button>
            </CollapsibleTrigger>
          </Collapsible>
          
          <Collapsible open={isMetricsExpanded} onOpenChange={setIsMetricsExpanded}>
            <CollapsibleTrigger asChild>
              <Button variant="outline" size="sm" className="hover-scale">
                <BarChart3 className="h-4 w-4 mr-2" />
                Detailed Metrics
                {isMetricsExpanded ? (
                  <ChevronDown className="h-4 w-4 ml-2" />
                ) : (
                  <ChevronRight className="h-4 w-4 ml-2" />
                )}
              </Button>
            </CollapsibleTrigger>
          </Collapsible>

          <Button variant="outline" size="sm" className="hover-scale">
            <Activity className="h-4 w-4 mr-2" />
            Live
          </Button>

          {connected && (
            <Badge variant="secondary" className="animate-pulse">
              Live
            </Badge>
          )}
        </div>

        {/* Expandable Sections */}
        <Collapsible open={isLogsExpanded} onOpenChange={setIsLogsExpanded}>
          <CollapsibleContent className="mt-4">
            <AdapterLogViewer
              adapterId={adapter.id}
              adapterName={adapter.name}
              logs={logs}
              loading={logsLoading}
              onRefresh={refreshLogs}
              onExport={exportLogs}
              realTimeEnabled={connected}
            />
          </CollapsibleContent>
        </Collapsible>

        <Collapsible open={isMetricsExpanded} onOpenChange={setIsMetricsExpanded}>
          <CollapsibleContent className="mt-4">
            <Card className="bg-gradient-secondary border-border/50">
              <CardHeader>
                <CardTitle className="text-lg">Detailed Metrics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-3 border border-border/50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <Server className="h-5 w-5" />
                      <div>
                        <div className="font-medium">{adapter.name}</div>
                        <div className="text-sm text-muted-foreground">{adapter.type} - {adapter.mode}</div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 text-sm">
                      <div className="text-center">
                        <div className="font-medium">{(adapter.messagesProcessed || 0).toLocaleString()}</div>
                        <div className="text-xs text-muted-foreground">Messages</div>
                      </div>
                      <div className="text-center">
                        <div className="font-medium">{adapter.errorsCount || 0}</div>
                        <div className="text-xs text-muted-foreground">Errors</div>
                      </div>
                      <div className="text-center">
                        <div className="font-medium">{adapter.businessComponentName || 'N/A'}</div>
                        <div className="text-xs text-muted-foreground">Component</div>
                      </div>
                      <Badge variant={getStatusVariant(adapter.status)}>
                        {adapter.status}
                      </Badge>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </CollapsibleContent>
        </Collapsible>
      </CardContent>
    </Card>
  );
};