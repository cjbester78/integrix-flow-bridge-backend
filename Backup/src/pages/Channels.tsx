import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { useCustomerAdapters } from '@/hooks/useCustomerAdapters';
import { Customer } from '@/types/customer';
import { Activity, RefreshCw } from 'lucide-react';
import { Channel } from '@/services/channelService';
import { ChannelStats } from '@/components/channels/ChannelStats';
import { CustomerFilter } from '@/components/channels/CustomerFilter';
import { ChannelList } from '@/components/channels/ChannelList';

export default function Channels() {
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const { customers, loading } = useCustomerAdapters();

  // Mock channels data - replace with actual API call
  const channels: Channel[] = [
    {
      id: '1',
      name: 'SAP-to-Salesforce Integration',
      description: 'Customer data synchronization between SAP ERP and Salesforce CRM',
      status: 'running',
      customerId: selectedCustomer?.id || '1',
      load: 85,
      throughput: '245 msg/min',
      uptime: '99.8%',
      lastActivity: '2 seconds ago',
      errorRate: 0.2,
      totalMessages: 124567,
      healthScore: 98,
      flowMetrics: {
        totalMessages: 124567,
        successfulMessages: 124200,
        failedMessages: 367,
        averageProcessingTime: '120ms',
        lastProcessed: '2 seconds ago'
      },
      adapters: [
        {
          id: 'sap-adapter',
          name: 'SAP ERP',
          status: 'connected',
          category: 'Database',
          messagesProcessed: 50000,
          errorCount: 5,
          avgResponseTime: '120ms',
          icon: 'Activity'
        },
        {
          id: 'salesforce-adapter', 
          name: 'Salesforce CRM',
          status: 'connected',
          category: 'Cloud',
          messagesProcessed: 48000,
          errorCount: 2,
          avgResponseTime: '95ms',
          icon: 'Activity'
        }
      ]
    }
  ];

  const filteredChannels = channels.filter(channel => {
    if (selectedCustomer && channel.customerId !== selectedCustomer.id) return false;
    if (statusFilter && channel.status !== statusFilter) return false;
    return true;
  });

  const handleRefresh = () => {
    // Implement refresh logic
    console.log('Refreshing channels...');
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <RefreshCw className="h-8 w-8 animate-spin mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground">Loading channels...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="animate-slide-up">
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          <Activity className="h-8 w-8" />
          Channel Monitor
        </h1>
        <p className="text-muted-foreground">Monitor and manage integration channel performance</p>
      </div>

      <ChannelStats
        channels={filteredChannels}
        isCustomerSelected={!!selectedCustomer}
        statusFilter={statusFilter}
        onStatusFilter={setStatusFilter}
      />

      <CustomerFilter
        customers={customers}
        selectedCustomer={selectedCustomer}
        onCustomerChange={setSelectedCustomer}
        loading={loading}
      />

      <ChannelList channels={filteredChannels} />
    </div>
  );
}