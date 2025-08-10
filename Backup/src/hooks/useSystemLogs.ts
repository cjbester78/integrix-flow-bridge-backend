import { useState, useEffect } from 'react';
import { adapterService, CommunicationAdapter } from '@/services/adapter';
import { api } from '@/services/api';
import { systemErrorLogger } from '@/services/systemErrorLogger';

export interface SystemLogEntry {
  id: string;
  timestamp: string;
  level: 'info' | 'warn' | 'error' | 'debug';
  message: string;
  details?: any;
  source: 'adapter' | 'system' | 'channel' | 'flow' | 'api';
  sourceId?: string;
  sourceName?: string;
}

interface UseSystemLogsParams {
  source?: 'adapter' | 'system' | 'channel' | 'flow' | 'api';
  sourceId?: string;
  level?: 'info' | 'warn' | 'error' | 'debug';
  search?: string;
  startDate?: string;
  endDate?: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
  // Domain-specific filtering
  domainType?: 'UserManagement' | 'FlowEngine' | 'AdapterManagement' | 'DataStructures' | 'ChannelManagement' | 'MessageProcessing';
  domainReferenceId?: string;
  includeUserFriendlyErrors?: boolean;
}

export const useSystemLogs = (params: UseSystemLogsParams = {}) => {
  const [logs, setLogs] = useState<SystemLogEntry[]>([]);
  const [sources, setSources] = useState<{adapters: CommunicationAdapter[], channels: any[], flows: any[]}>({
    adapters: [],
    channels: [],
    flows: []
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>();

  const fetchSources = async () => {
    try {
      const [adaptersRes, channelsRes] = await Promise.all([
        adapterService.getAdapters(),
        api.get('/channels')
      ]);
      
      setSources({
        adapters: adaptersRes.success ? adaptersRes.data?.adapters || [] : [],
        channels: channelsRes.success ? channelsRes.data || [] : [],
        flows: [] // Add flows API when available
      });
    } catch (err) {
      console.error('Failed to fetch sources:', err);
    }
  };

  const fetchLogs = async () => {
    setLoading(true);
    setError(undefined);

    try {
      // Build query parameters for API call
      const queryParams = new URLSearchParams();
      if (params.source) queryParams.append('source', params.source);
      if (params.sourceId) queryParams.append('sourceId', params.sourceId);
      if (params.level) queryParams.append('level', params.level);
      if (params.search) queryParams.append('search', params.search);
      if (params.startDate) queryParams.append('startDate', params.startDate);
      if (params.endDate) queryParams.append('endDate', params.endDate);
      if (params.domainType) queryParams.append('domainType', params.domainType);
      if (params.domainReferenceId) queryParams.append('domainReferenceId', params.domainReferenceId);

      // Try to fetch from API first
      const endpoint = `/api/logs/system${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
      const response = await api.get(endpoint);
      
      if (response.success && response.data) {
        setLogs(response.data);
      } else {
        // Fallback to mock system logger
        const systemLogs = systemErrorLogger.getFilteredLogs({
          source: params.source,
          sourceId: params.sourceId,
          level: params.level,
          search: params.search,
        });
        setLogs(systemLogs);
      }
    } catch (err) {
      // Fallback to mock system logger on API error
      try {
        const systemLogs = systemErrorLogger.getFilteredLogs({
          source: params.source,
          sourceId: params.sourceId,
          level: params.level,
          search: params.search,
        });
        setLogs(systemLogs);
      } catch (fallbackErr) {
        setError(fallbackErr instanceof Error ? fallbackErr.message : 'Failed to fetch logs');
        setLogs([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const refetch = () => {
    fetchLogs();
  };

  // Fetch sources on mount
  useEffect(() => {
    fetchSources();
  }, []);

  // Fetch logs when parameters change
  useEffect(() => {
    fetchLogs();
  }, [params.source, params.sourceId, params.level, params.search, params.startDate, params.endDate]);

  // Auto refresh setup
  useEffect(() => {
    if (params.autoRefresh) {
      const interval = setInterval(fetchLogs, params.refreshInterval || 30000);
      return () => clearInterval(interval);
    }
  }, [params.autoRefresh, params.refreshInterval]);

  // Listen for new logs from the system logger
  useEffect(() => {
    const unsubscribe = systemErrorLogger.onLogUpdate(() => {
      fetchLogs(); // Refresh when new logs are added
    });
    return unsubscribe;
  }, []);

  return {
    logs,
    sources,
    loading,
    error,
    refetch,
  };
};