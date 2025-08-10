import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { useMessageMonitoring } from '@/hooks/useMessageMonitoring';
import { BusinessComponent } from '@/types/businessComponent';
import { MessageSquare, RefreshCw, Wifi, WifiOff } from 'lucide-react';
import { MessageStats } from '@/components/messages/MessageStats';
import { MessageList } from '@/components/messages/MessageList';
import { BusinessComponentFilter } from '@/components/channels/BusinessComponentFilter';

export const Messages = () => {
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const { businessComponents, loading } = useBusinessComponentAdapters();
  const { 
    messages, 
    stats, 
    loading: messagesLoading, 
    connected, 
    refreshData 
  } = useMessageMonitoring(selectedBusinessComponent?.id);

  // Apply status filter to messages
  const displayMessages = statusFilter 
    ? messages.filter(msg => msg.status === statusFilter)
    : messages;

  const handleRefresh = () => {
    refreshData();
  };

  const handleStatusFilter = (status: string) => {
    setStatusFilter(statusFilter === status ? null : status);
  };

  const handleBusinessComponentChange = (businessComponent: BusinessComponent | null) => {
    setSelectedBusinessComponent(businessComponent);
    setStatusFilter(null); // Reset status filter when changing business component
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <MessageSquare className="h-8 w-8" />
            Message Monitor
          </h1>
          <div className="flex items-center gap-3">
            <p className="text-muted-foreground">Track and analyze integration message flows</p>
            <Badge variant={connected ? "default" : "destructive"} className="flex items-center gap-1">
              {connected ? <Wifi className="h-3 w-3" /> : <WifiOff className="h-3 w-3" />}
              {connected ? 'Live' : 'Disconnected'}
            </Badge>
          </div>
        </div>
        <Button 
          variant="outline" 
          size="sm" 
          onClick={handleRefresh} 
          className="hover-scale"
          disabled={messagesLoading}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${messagesLoading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Overview Stats */}
      <MessageStats 
        messages={displayMessages} 
        stats={stats}
        isBusinessComponentSelected={!!selectedBusinessComponent}
        onStatusFilter={handleStatusFilter}
        statusFilter={statusFilter}
        loading={messagesLoading}
      />

      {/* Business Component Selection */}
      <BusinessComponentFilter
        selectedBusinessComponent={selectedBusinessComponent}
        businessComponents={businessComponents}
        loading={loading}
        onBusinessComponentChange={handleBusinessComponentChange}
      />

      {/* Message List */}
      <MessageList 
        messages={displayMessages} 
        isBusinessComponentSelected={!!selectedBusinessComponent}
        statusFilter={statusFilter}
        loading={messagesLoading}
      />
    </div>
  );
};