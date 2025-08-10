import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Message } from '@/services/messageService';
import { MessageStats as MessageStatsType } from '@/services/messageService';

interface MessageStatsProps {
  messages: Message[];
  stats?: MessageStatsType | null;
  isBusinessComponentSelected: boolean;
  onStatusFilter?: (status: string) => void;
  statusFilter?: string | null;
  loading?: boolean;
}

export const MessageStats = ({ 
  messages, 
  stats,
  isBusinessComponentSelected, 
  onStatusFilter, 
  statusFilter,
  loading = false
}: MessageStatsProps) => {
  // Ensure messages is an array
  const safeMessages = messages || [];
  
  // Use API stats if available, otherwise calculate from messages
  const successfulMessages = stats?.successful ?? safeMessages.filter(msg => msg.status === 'success').length;
  const processingMessages = stats?.processing ?? safeMessages.filter(msg => msg.status === 'processing').length;
  const failedMessages = stats?.failed ?? safeMessages.filter(msg => msg.status === 'failed').length;
  const totalMessages = stats?.total ?? safeMessages.length;
  const successRate = stats?.successRate ?? (totalMessages > 0 ? ((successfulMessages / totalMessages) * 100) : 0);
  const avgProcessingTime = stats?.avgProcessingTime ?? (() => {
    const completedMessages = safeMessages.filter(msg => msg.status !== 'processing' && msg.processingTime !== '-');
    return completedMessages.length > 0 
      ? Math.round(completedMessages.reduce((sum, msg) => {
          const time = parseFloat(msg.processingTime.replace(/[^\d.]/g, ''));
          return sum + (isNaN(time) ? 0 : time);
        }, 0) / completedMessages.length)
      : 0;
  })();

  const getSubtext = () => 
    isBusinessComponentSelected 
      ? `of ${totalMessages} business component messages` 
      : 'across all messages';

  const handleCardClick = (status: string) => {
    if (onStatusFilter) {
      onStatusFilter(status);
    }
  };

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <Card key={i} className="bg-gradient-secondary border-border/50">
            <CardHeader className="pb-2">
              <Skeleton className="h-4 w-32" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16 mb-2" />
              <Skeleton className="h-3 w-24" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
      <Card 
        className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
          statusFilter === 'success' ? 'ring-2 ring-success' : ''
        }`}
        onClick={() => handleCardClick('success')}
      >
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Successful Messages</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-success">{successfulMessages}</div>
          <p className="text-xs text-muted-foreground">{getSubtext()}</p>
        </CardContent>
      </Card>
      
      <Card 
        className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
          statusFilter === 'processing' ? 'ring-2 ring-warning' : ''
        }`}
        onClick={() => handleCardClick('processing')}
      >
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Processing Messages</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-warning">{processingMessages}</div>
          <p className="text-xs text-muted-foreground">{getSubtext()}</p>
        </CardContent>
      </Card>
      
      <Card 
        className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
          statusFilter === 'failed' ? 'ring-2 ring-destructive' : ''
        }`}
        onClick={() => handleCardClick('failed')}
      >
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Failed Messages</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-destructive">{failedMessages}</div>
          <p className="text-xs text-muted-foreground">{getSubtext()}</p>
        </CardContent>
      </Card>
      
      <Card className="bg-gradient-secondary border-border/50 animate-scale-in">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Success Rate</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-success">{successRate.toFixed(1)}%</div>
          <p className="text-xs text-muted-foreground">{getSubtext()}</p>
        </CardContent>
      </Card>
      
      <Card className="bg-gradient-secondary border-border/50 animate-scale-in">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Avg Processing</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-info">{avgProcessingTime}ms</div>
          <p className="text-xs text-muted-foreground">{getSubtext()}</p>
        </CardContent>
      </Card>
    </div>
  );
};