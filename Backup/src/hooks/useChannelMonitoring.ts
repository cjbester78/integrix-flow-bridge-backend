import { useState, useEffect, useCallback } from 'react';
import { channelService, Channel } from '@/services/channelService';
import { useToast } from '@/hooks/use-toast';

export const useChannelMonitoring = (customerId?: string) => {
  const [channels, setChannels] = useState<Channel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadChannels();
  }, [customerId]);

  const loadChannels = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await channelService.getChannels(customerId);
      if (response.success && response.data) {
        setChannels(response.data);
      } else {
        setError(response.error || 'Failed to load channels');
        setChannels([]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
      setChannels([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    channels,
    loading,
    error,
    refreshChannels: loadChannels,
  };
};