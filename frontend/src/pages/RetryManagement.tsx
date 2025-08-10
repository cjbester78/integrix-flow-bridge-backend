// @ts-nocheck
import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { 
  RefreshCw, 
  AlertTriangle, 
  Clock, 
  CheckCircle, 
  XCircle,
  Play,
  FileWarning,
  Settings,
  TrendingUp
} from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

interface RetryPolicy {
  id: string;
  adapterId: string;
  adapterName: string;
  name: string;
  description: string;
  maxAttempts: number;
  initialDelayMs: number;
  maxDelayMs: number;
  multiplier: number;
  retryOnErrors: string[];
  isActive: boolean;
  createdAt: string;
}

interface DeadLetterMessage {
  id: string;
  originalMessageId: string;
  adapterId: string;
  adapterName: string;
  businessComponentName: string;
  errorType: string;
  errorMessage: string;
  errorCount: number;
  totalRetryAttempts: number;
  lastErrorAt: string;
  createdAt: string;
  reprocessed: boolean;
}

export const RetryManagement = () => {
  const [retryPolicies, setRetryPolicies] = useState<RetryPolicy[]>([]);
  const [deadLetterMessages, setDeadLetterMessages] = useState<DeadLetterMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('policies');
  const { toast } = useToast();

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      if (activeTab === 'policies') {
        const response = await apiClient.get<RetryPolicy[]>('/retry-management/policies');
        setRetryPolicies(response.data);
      } else if (activeTab === 'dlq') {
        const response = await apiClient.get<DeadLetterMessage[]>('/retry-management/dead-letter-queue');
        setDeadLetterMessages(response.data);
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch data',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleReprocess = async (messageId: string) => {
    try {
      await apiClient.post(`/retry-management/dead-letter-queue/${messageId}/reprocess`);
      toast({
        title: 'Success',
        description: 'Message queued for reprocessing',
      });
      fetchData();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to reprocess message',
        variant: 'destructive',
      });
    }
  };

  const getDelayDisplay = (policy: RetryPolicy) => {
    const delays = [];
    let delay = policy.initialDelayMs;
    for (let i = 1; i <= policy.maxAttempts; i++) {
      delays.push(`${delay}ms`);
      delay = Math.min(delay * policy.multiplier, policy.maxDelayMs);
    }
    return delays.join(' → ');
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <RefreshCw className="h-8 w-8" />
            Retry Management
          </h1>
          <p className="text-muted-foreground">
            Configure retry policies and manage failed messages
          </p>
        </div>
        <Button onClick={fetchData} variant="outline">
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Active Policies</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{retryPolicies.filter(p => p.isActive).length}</div>
            <p className="text-xs text-muted-foreground">Total configured</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Messages in DLQ</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-destructive">
              {deadLetterMessages.filter(m => !m.reprocessed).length}
            </div>
            <p className="text-xs text-muted-foreground">Awaiting resolution</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Retry Success Rate</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">85%</div>
            <p className="text-xs text-muted-foreground">Last 24 hours</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Avg Retry Delay</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">2.5s</div>
            <p className="text-xs text-muted-foreground">Across all policies</p>
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="policies" className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            Retry Policies
          </TabsTrigger>
          <TabsTrigger value="dlq" className="flex items-center gap-2">
            <FileWarning className="h-4 w-4" />
            Dead Letter Queue
          </TabsTrigger>
          <TabsTrigger value="statistics" className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4" />
            Statistics
          </TabsTrigger>
        </TabsList>

        <TabsContent value="policies" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Retry Policies</CardTitle>
              <CardDescription>
                Configure how failed messages are retried for each adapter
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="text-center py-4">Loading...</div>
              ) : retryPolicies.length === 0 ? (
                <Alert>
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    No retry policies configured. Default policy will be used.
                  </AlertDescription>
                </Alert>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Adapter</TableHead>
                      <TableHead>Policy Name</TableHead>
                      <TableHead>Max Attempts</TableHead>
                      <TableHead>Delay Pattern</TableHead>
                      <TableHead>Retry On</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {retryPolicies.map((policy) => (
                      <TableRow key={policy.id}>
                        <TableCell className="font-medium">
                          {policy.adapterName || 'Unknown'}
                        </TableCell>
                        <TableCell>{policy.name}</TableCell>
                        <TableCell>{policy.maxAttempts}</TableCell>
                        <TableCell className="text-xs font-mono">
                          {getDelayDisplay(policy)}
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-wrap gap-1">
                            {policy.retryOnErrors.map((error) => (
                              <Badge key={error} variant="secondary" className="text-xs">
                                {error}
                              </Badge>
                            ))}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant={policy.isActive ? 'default' : 'secondary'}>
                            {policy.isActive ? 'Active' : 'Inactive'}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="dlq" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Dead Letter Queue</CardTitle>
              <CardDescription>
                Messages that failed after all retry attempts
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="text-center py-4">Loading...</div>
              ) : deadLetterMessages.length === 0 ? (
                <Alert>
                  <CheckCircle className="h-4 w-4 text-green-600" />
                  <AlertDescription>
                    No messages in the dead letter queue. Great job!
                  </AlertDescription>
                </Alert>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Message ID</TableHead>
                      <TableHead>Adapter</TableHead>
                      <TableHead>Business Component</TableHead>
                      <TableHead>Error Type</TableHead>
                      <TableHead>Attempts</TableHead>
                      <TableHead>Last Error</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {deadLetterMessages.map((message) => (
                      <TableRow key={message.id}>
                        <TableCell className="font-mono text-xs">
                          {message.originalMessageId.substring(0, 8)}...
                        </TableCell>
                        <TableCell>{message.adapterName}</TableCell>
                        <TableCell>{message.businessComponentName}</TableCell>
                        <TableCell>
                          <Badge variant="destructive">{message.errorType}</Badge>
                        </TableCell>
                        <TableCell>{message.totalRetryAttempts}</TableCell>
                        <TableCell className="text-xs">
                          {formatDistanceToNow(new Date(message.lastErrorAt), { addSuffix: true })}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleReprocess(message.id)}
                            disabled={message.reprocessed}
                          >
                            <Play className="h-3 w-3 mr-1" />
                            Reprocess
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="statistics" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Retry Statistics</CardTitle>
              <CardDescription>
                Performance metrics and trends for retry operations
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8 text-muted-foreground">
                Statistics visualization coming soon...
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};