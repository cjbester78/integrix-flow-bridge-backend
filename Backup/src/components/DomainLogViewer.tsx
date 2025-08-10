import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { 
  ChevronDown, 
  ChevronRight, 
  AlertTriangle, 
  Info, 
  XCircle, 
  Clock,
  User,
  Settings,
  Database,
  Workflow,
  MessageSquare,
  Network
} from 'lucide-react';
import { SystemLogEntry } from '@/hooks/useSystemLogs';
import { DomainErrorEntry, DomainType } from '@/hooks/useDomainLogs';

interface DomainLogViewerProps {
  domainType: DomainType;
  domainErrors: DomainErrorEntry[];
  systemLogs: SystemLogEntry[];
  loading: boolean;
  error?: string;
  referenceId?: string;
  referenceName?: string;
}

export const DomainLogViewer: React.FC<DomainLogViewerProps> = ({
  domainType,
  domainErrors,
  systemLogs,
  loading,
  error,
  referenceId,
  referenceName,
}) => {
  const [expandedErrors, setExpandedErrors] = useState<Set<string>>(new Set());
  const [expandedLogs, setExpandedLogs] = useState<Set<string>>(new Set());

  const toggleErrorExpansion = (errorId: string) => {
    const newExpanded = new Set(expandedErrors);
    if (newExpanded.has(errorId)) {
      newExpanded.delete(errorId);
    } else {
      newExpanded.add(errorId);
    }
    setExpandedErrors(newExpanded);
  };

  const toggleLogExpansion = (logId: string) => {
    const newExpanded = new Set(expandedLogs);
    if (newExpanded.has(logId)) {
      newExpanded.delete(logId);
    } else {
      newExpanded.add(logId);
    }
    setExpandedLogs(newExpanded);
  };

  const getDomainIcon = (type: DomainType) => {
    const iconMap = {
      UserManagement: User,
      FlowEngine: Workflow,
      AdapterManagement: Network,
      DataStructures: Database,
      ChannelManagement: Settings,
      MessageProcessing: MessageSquare,
    };
    const IconComponent = iconMap[type];
    return <IconComponent className="h-4 w-4" />;
  };

  const getActionBadge = (action: string) => {
    const actionColors: Record<string, string> = {
      Create: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      Update: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      Delete: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
      Test: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
      Deploy: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    };

    const actionType = Object.keys(actionColors).find(type => action.includes(type)) || 'default';
    const className = actionColors[actionType] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';

    return (
      <Badge variant="secondary" className={className}>
        {action}
      </Badge>
    );
  };

  const getLevelIcon = (level: string) => {
    switch (level) {
      case 'error':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'warn':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      case 'info':
        return <Info className="h-4 w-4 text-blue-500" />;
      case 'debug':
        return <Settings className="h-4 w-4 text-gray-500" />;
      default:
        return <Info className="h-4 w-4 text-gray-500" />;
    }
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  const formatPayload = (payload: string) => {
    try {
      const parsed = JSON.parse(payload);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return payload;
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center text-muted-foreground">Loading logs...</div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertTriangle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          {getDomainIcon(domainType)}
          {domainType} Logs
          {referenceName && (
            <Badge variant="outline">{referenceName}</Badge>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="errors" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="errors">
              User-Friendly Errors ({domainErrors.length})
            </TabsTrigger>
            <TabsTrigger value="system">
              System Logs ({systemLogs.length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="errors">
            <ScrollArea className="h-[600px] w-full">
              {domainErrors.length === 0 ? (
                <div className="text-center text-muted-foreground py-8">
                  No domain errors found.
                </div>
              ) : (
                <div className="space-y-4">
                  {domainErrors.map((error) => (
                    <Card key={error.id} className="border-l-4 border-l-red-500">
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                          <div className="flex-1 space-y-2">
                            <div className="flex items-center gap-2">
                              {getActionBadge(error.action)}
                              <span className="text-sm text-muted-foreground">
                                <Clock className="h-3 w-3 inline mr-1" />
                                {formatTimestamp(error.createdAt)}
                              </span>
                            </div>
                            
                            {error.description && (
                              <p className="text-sm font-medium">{error.description}</p>
                            )}

                            <div className="flex items-center gap-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => toggleErrorExpansion(error.id)}
                                className="h-6 px-2"
                              >
                                {expandedErrors.has(error.id) ? (
                                  <ChevronDown className="h-3 w-3" />
                                ) : (
                                  <ChevronRight className="h-3 w-3" />
                                )}
                                Show Details
                              </Button>
                            </div>

                            {expandedErrors.has(error.id) && (
                              <div className="mt-3 space-y-3 border-t pt-3">
                                <div>
                                  <h4 className="font-semibold text-sm mb-1">Request Payload:</h4>
                                  <pre className="text-xs bg-muted p-2 rounded overflow-auto max-h-40">
                                    {formatPayload(error.payload)}
                                  </pre>
                                </div>
                                
                                {error.systemLogId && (
                                  <div>
                                    <h4 className="font-semibold text-sm mb-1">System Log ID:</h4>
                                    <Badge variant="outline" className="text-xs">
                                      {error.systemLogId}
                                    </Badge>
                                  </div>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </ScrollArea>
          </TabsContent>

          <TabsContent value="system">
            <ScrollArea className="h-[600px] w-full">
              {systemLogs.length === 0 ? (
                <div className="text-center text-muted-foreground py-8">
                  No system logs found.
                </div>
              ) : (
                <div className="space-y-4">
                  {systemLogs.map((log) => (
                    <Card key={log.id}>
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                          <div className="flex-1 space-y-2">
                            <div className="flex items-center gap-2">
                              {getLevelIcon(log.level)}
                              <Badge variant={log.level === 'error' ? 'destructive' : 'secondary'}>
                                {log.level.toUpperCase()}
                              </Badge>
                              {log.source && (
                                <Badge variant="outline">{log.source}</Badge>
                              )}
                              <span className="text-sm text-muted-foreground">
                                <Clock className="h-3 w-3 inline mr-1" />
                                {formatTimestamp(log.timestamp)}
                              </span>
                            </div>
                            
                            <p className="text-sm font-medium">{log.message}</p>

                            <div className="flex items-center gap-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => toggleLogExpansion(log.id)}
                                className="h-6 px-2"
                              >
                                {expandedLogs.has(log.id) ? (
                                  <ChevronDown className="h-3 w-3" />
                                ) : (
                                  <ChevronRight className="h-3 w-3" />
                                )}
                                Show Details
                              </Button>
                            </div>

                            {expandedLogs.has(log.id) && (
                              <div className="mt-3 space-y-3 border-t pt-3">
                                {log.details && (
                                  <div>
                                    <h4 className="font-semibold text-sm mb-1">Details:</h4>
                                    <pre className="text-xs bg-muted p-2 rounded overflow-auto max-h-40">
                                      {typeof log.details === 'string' 
                                        ? log.details 
                                        : JSON.stringify(log.details, null, 2)
                                      }
                                    </pre>
                                  </div>
                                )}
                                
                                <div className="grid grid-cols-2 gap-4 text-xs">
                                  {log.sourceName && (
                                    <div>
                                      <span className="font-semibold">Source Name:</span>
                                      <p className="text-muted-foreground">{log.sourceName}</p>
                                    </div>
                                  )}
                                  {log.sourceId && (
                                    <div>
                                      <span className="font-semibold">Source ID:</span>
                                      <p className="text-muted-foreground">{log.sourceId}</p>
                                    </div>
                                  )}
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </ScrollArea>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
};