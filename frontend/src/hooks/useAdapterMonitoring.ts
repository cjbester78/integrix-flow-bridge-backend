// @ts-nocheck
import { useState, useEffect, useCallback } from 'react';
import { adapterMonitoringService, AdapterMonitoring } from '@/services/adapterMonitoringService';
import { useToast } from '@/hooks/use-toast';

export const useAdapterMonitoring = (businessComponentId?: string) => {
  const [adapters, setAdapters] = useState<AdapterMonitoring[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAdapters();
  }, [businessComponentId]);

  const loadAdapters = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await adapterMonitoringService.getAdapters(businessComponentId);
      if (response.success && response.data) {
        setAdapters(response.data);
      } else {
        setError(response.error || 'Failed to load adapters');
        setAdapters([]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
      setChannels([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    adapters,
    loading,
    error,
    refreshAdapters: loadAdapters,
  };
};