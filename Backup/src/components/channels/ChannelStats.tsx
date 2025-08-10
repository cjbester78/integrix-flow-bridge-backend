import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Channel, FlowMetrics } from '@/services/channelService';

interface ChannelStatsProps {
  channels: Channel[];
  isCustomerSelected: boolean;
  statusFilter: string | null;
  onStatusFilter: (status: string | null) => void;
}

export const ChannelStats = ({ 
  channels, 
  isCustomerSelected, 
  statusFilter, 
  onStatusFilter 
}: ChannelStatsProps) => {
  const totalChannels = channels.length;
  const activeChannels = channels.filter(c => c.status === 'running').length;
  const inactiveChannels = channels.filter(c => c.status === 'idle').length;
  const stoppedChannels = channels.filter(c => c.status === 'stopped').length;
  const errorChannels = channels.filter(c => c.status === 'error').length;

  const handleStatusClick = (status: string) => {
    onStatusFilter(statusFilter === status ? null : status);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'running' ? 'ring-2 ring-success' : ''
        }`}
        onClick={() => handleStatusClick('running')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Active Channels</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-success">{activeChannels}</div>
          <p className="text-xs text-muted-foreground">
            {isCustomerSelected ? 'across selected customer' : 'across all channels'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'idle' ? 'ring-2 ring-warning' : ''
        }`}
        onClick={() => handleStatusClick('idle')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Inactive Channels</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-warning">{inactiveChannels}</div>
          <p className="text-xs text-muted-foreground">
            {isCustomerSelected ? 'across selected customer' : 'across all channels'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'stopped' ? 'ring-2 ring-muted-foreground' : ''
        }`}
        onClick={() => handleStatusClick('stopped')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Stopped Channels</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-muted-foreground">{stoppedChannels}</div>
          <p className="text-xs text-muted-foreground">
            {isCustomerSelected ? 'across selected customer' : 'across all channels'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'error' ? 'ring-2 ring-destructive' : ''
        }`}
        onClick={() => handleStatusClick('error')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Error Channels</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-destructive">{errorChannels}</div>
          <p className="text-xs text-muted-foreground">
            {isCustomerSelected ? 'across selected customer' : 'across all channels'}
          </p>
        </CardContent>
      </Card>
    </div>
  );
};