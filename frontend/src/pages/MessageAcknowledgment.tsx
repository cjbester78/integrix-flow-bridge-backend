// @ts-nocheck
import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { 
  CheckCircle2, 
  XCircle, 
  Clock, 
  RefreshCw, 
  AlertCircle,
  Trash2,
  BarChart3
} from 'lucide-react';
import { apiClient as api } from '@/lib/api-client';
import { useToast } from '@/hooks/use-toast';
import { getStatusIconColor } from '@/lib/icon-colors';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

interface AcknowledgmentStats {
  pending: number;
  acknowledged: number;
  rejected: number;
  requeued: number;
  expired: number;
  failed: number;
  activeChannels: number;
}

interface MessageAcknowledgment {
  id: string;
  messageId: string;
  deliveryTag: number;
  status: string;
  acknowledgedAt?: string;
  rejectedAt?: string;
  requeue: boolean;
  errorMessage?: string;
  retryCount: number;
  nextRetryAt?: string;
  consumerTag?: string;
  channelNumber?: number;
  createdAt: string;
  updatedAt: string;
}

export const MessageAcknowledgmentPage = () => {
  const [stats, setStats] = useState<AcknowledgmentStats | null>(null);
  const [messageHistory, setMessageHistory] = useState<MessageAcknowledgment[]>([]);
  const [selectedMessageId, setSelectedMessageId] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000); // Refresh every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchStats = async () => {
    try {
      const response = await api.get('/api/message-acknowledgments/statistics');
      setStats(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to fetch acknowledgment statistics:', error);
      toast({ title: "Error", description: 'Failed to load acknowledgment statistics', variant: "destructive" });
    }
  };

  const fetchMessageHistory = async (messageId: string) => {
    try {
      const response = await api.get(`/api/message-acknowledgments/message/${messageId}/history`);
      setMessageHistory(response.data);
      setSelectedMessageId(messageId);
    } catch (error) {
      console.error('Failed to fetch message history:', error);
      toast({ title: "Error", description: 'Failed to load message acknowledgment history', variant: "destructive" });
    }
  };

  const handleCleanup = async () => {
    try {
      await api.post('/api/message-acknowledgments/cleanup');
      toast({ title: "Success", description: 'Cleanup triggered successfully' });
      fetchStats();
    } catch (error) {
      console.error('Failed to trigger cleanup:', error);
      toast({ title: "Error", description: 'Failed to trigger cleanup', variant: "destructive" });
    }
  };

  const handleExpired = async () => {
    try {
      await api.post('/api/message-acknowledgments/handle-expired');
      toast({ title: "Success", description: 'Expired acknowledgments handled' });
      fetchStats();
    } catch (error) {
      console.error('Failed to handle expired acknowledgments:', error);
      toast({ title: "Error", description: 'Failed to handle expired acknowledgments', variant: "destructive" });
    }
  };

  const getStatusIcon = (status: string) => {
    const colorClass = getStatusIconColor(status);
    switch (status) {
      case 'ACKNOWLEDGED':
        return <CheckCircle2 className={`h-4 w-4 ${colorClass}`} />;
      case 'REJECTED':
        return <XCircle className={`h-4 w-4 ${colorClass}`} />;
      case 'PENDING':
        return <Clock className={`h-4 w-4 ${colorClass}`} />;
      case 'REQUEUED':
        return <RefreshCw className={`h-4 w-4 ${colorClass}`} />;
      case 'EXPIRED':
        return <AlertCircle className={`h-4 w-4 ${colorClass}`} />;
      case 'FAILED':
        return <XCircle className={`h-4 w-4 ${colorClass}`} />;
      default:
        return null;
    }
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'ACKNOWLEDGED':
        return 'success';
      case 'REJECTED':
      case 'FAILED':
        return 'destructive';
      case 'PENDING':
        return 'warning';
      case 'REQUEUED':
        return 'default';
      case 'EXPIRED':
        return 'secondary';
      default:
        return 'outline';
    }
  };

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }

  const total = stats ? 
    stats.pending + stats.acknowledged + stats.rejected + 
    stats.requeued + stats.expired + stats.failed : 0;

  return (
    <div className="p-8 space-y-8">
      <div>
        <h1 className="text-3xl font-bold mb-2">Message Acknowledgments</h1>
        <p className="text-muted-foreground">
          Monitor and manage message acknowledgment status
        </p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Pending</p>
                <p className="text-2xl font-bold">{stats?.pending || 0}</p>
              </div>
              <Clock className="h-8 w-8 text-yellow-500" />
            </div>
            <Progress 
              value={(stats?.pending || 0) / total * 100} 
              className="mt-3 h-2"
            />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Acknowledged</p>
                <p className="text-2xl font-bold">{stats?.acknowledged || 0}</p>
              </div>
              <CheckCircle2 className="h-8 w-8 text-green-500" />
            </div>
            <Progress 
              value={(stats?.acknowledged || 0) / total * 100} 
              className="mt-3 h-2"
            />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Failed/Rejected</p>
                <p className="text-2xl font-bold">
                  {(stats?.rejected || 0) + (stats?.failed || 0)}
                </p>
              </div>
              <XCircle className="h-8 w-8 text-red-500" />
            </div>
            <Progress 
              value={((stats?.rejected || 0) + (stats?.failed || 0)) / total * 100} 
              className="mt-3 h-2"
            />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Active Channels</p>
                <p className="text-2xl font-bold">{stats?.activeChannels || 0}</p>
              </div>
              <BarChart3 className="h-8 w-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="history">Message History</TabsTrigger>
          <TabsTrigger value="maintenance">Maintenance</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Acknowledgment Status Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {Object.entries(stats || {}).map(([key, value]) => {
                  if (key === 'activeChannels') return null;
                  return (
                    <div key={key} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        {getStatusIcon(key.toUpperCase())}
                        <span className="capitalize font-medium">{key}</span>
                      </div>
                      <Badge variant={getStatusBadgeVariant(key.toUpperCase())}>
                        {value}
                      </Badge>
                    </div>
                  );
                })}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="history" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Message Acknowledgment History</CardTitle>
              <div className="mt-4">
                <input
                  type="text"
                  placeholder="Enter message ID to view history..."
                  className="w-full px-4 py-2 border rounded-lg"
                  value={selectedMessageId}
                  onChange={(e) => setSelectedMessageId(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter' && selectedMessageId) {
                      fetchMessageHistory(selectedMessageId);
                    }
                  }}
                />
              </div>
            </CardHeader>
            <CardContent>
              {messageHistory.length > 0 ? (
                <div className="overflow-x-auto">
                  <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Status</TableHead>
                      <TableHead>Delivery Tag</TableHead>
                      <TableHead>Retry Count</TableHead>
                      <TableHead>Created</TableHead>
                      <TableHead>Processed</TableHead>
                      <TableHead>Error</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {messageHistory.map((ack) => (
                      <TableRow key={ack.id}>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {getStatusIcon(ack.status)}
                            <Badge variant={getStatusBadgeVariant(ack.status)}>
                              {ack.status}
                            </Badge>
                          </div>
                        </TableCell>
                        <TableCell>{ack.deliveryTag}</TableCell>
                        <TableCell>{ack.retryCount}</TableCell>
                        <TableCell>
                          {new Date(ack.createdAt).toLocaleString()}
                        </TableCell>
                        <TableCell>
                          {ack.acknowledgedAt && 
                            new Date(ack.acknowledgedAt).toLocaleString()}
                          {ack.rejectedAt && 
                            new Date(ack.rejectedAt).toLocaleString()}
                        </TableCell>
                        <TableCell className="max-w-xs truncate">
                          {ack.errorMessage}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                  </Table>
                </div>
              ) : (
                <p className="text-center text-muted-foreground py-8">
                  Enter a message ID to view its acknowledgment history
                </p>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="maintenance" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Maintenance Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-4 border rounded-lg">
                <div>
                  <h3 className="font-medium">Handle Expired Acknowledgments</h3>
                  <p className="text-sm text-muted-foreground">
                    Process acknowledgments that have exceeded the timeout period
                  </p>
                </div>
                <Button 
                  onClick={handleExpired}
                  variant="outline"
                  size="sm"
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Handle Expired
                </Button>
              </div>

              <div className="flex items-center justify-between p-4 border rounded-lg">
                <div>
                  <h3 className="font-medium">Cleanup Old Records</h3>
                  <p className="text-sm text-muted-foreground">
                    Remove acknowledged and rejected records older than retention period
                  </p>
                </div>
                <Button 
                  onClick={handleCleanup}
                  variant="outline"
                  size="sm"
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  Cleanup
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};