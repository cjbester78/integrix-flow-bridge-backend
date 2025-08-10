import { useState, useEffect } from 'react';
import { channelService, ChannelLog, ChannelLogsFilters } from '@/services/channelService';

export const useChannelLogs = (channelId: string, filters?: ChannelLogsFilters) => {
  const [logs, setLogs] = useState<ChannelLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (channelId) {
      loadLogs();
    }
  }, [channelId, filters]);

  const loadLogs = async () => {
    if (!channelId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await channelService.getChannelLogs(channelId, filters);
      if (response.success && response.data) {
        setLogs(response.data);
      } else {
        setError(response.error || 'Failed to load channel logs');
        setLogs([]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
      setLogs([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    logs,
    loading,
    error,
    refreshLogs: loadLogs,
    connected: false,
    exportLogs: async () => {
      console.log('Export logs functionality will be implemented');
    },
  };
};