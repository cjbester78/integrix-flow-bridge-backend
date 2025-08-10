import { useState, useEffect } from 'react';
import { adapterMonitoringService, AdapterLog, AdapterLogsFilters } from '@/services/adapterMonitoringService';

export const useAdapterLogs = (adapterId: string, filters?: AdapterLogsFilters, autoLoad: boolean = false) => {
  const [logs, setLogs] = useState<AdapterLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (adapterId && autoLoad) {
      loadLogs();
    }
  }, [adapterId, filters, autoLoad]);

  const loadLogs = async () => {
    if (!adapterId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await adapterMonitoringService.getAdapterLogs(adapterId, filters);
      if (response.success && response.data) {
        setLogs(response.data);
      } else {
        setError(response.error || 'Failed to load adapter logs');
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