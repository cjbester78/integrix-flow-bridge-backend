import { useState, useEffect, useCallback } from 'react';
import { flowMonitoringService, FlowExecution, FlowMonitoringStats, FlowFilters } from '@/services/flowMonitoringService';
import { useToast } from '@/hooks/use-toast';

export const useFlowMonitoring = (customerId?: string) => {
  const [executions, setExecutions] = useState<FlowExecution[]>([]);
  const [stats, setStats] = useState<FlowMonitoringStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const { toast } = useToast();

  const loadExecutions = useCallback(async (filters?: FlowFilters) => {
    setLoading(true);
    try {
      const response = await flowMonitoringService.getFlowExecutions(filters);
      if (response.success && response.data) {
        setExecutions(response.data.executions);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load flow executions",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  }, [toast]);

  const loadStats = useCallback(async (filters?: Omit<FlowFilters, 'limit' | 'offset'>) => {
    try {
      const response = await flowMonitoringService.getExecutionStats(filters);
      if (response.success && response.data) {
        setStats(response.data);
      }
    } catch (error) {
      console.error('Failed to load execution stats:', error);
    }
  }, []);

  useEffect(() => {
    flowMonitoringService.connectWebSocket(customerId);
    setConnected(true);

    const unsubscribeExecutions = flowMonitoringService.onExecutionUpdate((execution) => {
      setExecutions(prev => {
        const existingIndex = prev.findIndex(e => e.id === execution.id);
        if (existingIndex >= 0) {
          const updated = [...prev];
          updated[existingIndex] = execution;
          return updated;
        } else {
          return [execution, ...prev];
        }
      });

      if (execution.status === 'failed') {
        toast({
          title: "Flow Execution Failed",
          description: `Flow execution ${execution.id} has failed`,
          variant: "destructive",
        });
      }
    });

    const unsubscribeStats = flowMonitoringService.onStatsUpdate((newStats) => {
      setStats(newStats);
    });

    loadExecutions();
    loadStats();

    return () => {
      unsubscribeExecutions();
      unsubscribeStats();
      flowMonitoringService.disconnectWebSocket();
      setConnected(false);
    };
  }, [customerId, loadExecutions, loadStats, toast]);

  const stopExecution = useCallback(async (executionId: string) => {
    try {
      const response = await flowMonitoringService.stopExecution(executionId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Execution stopped successfully",
        });
      } else {
        throw new Error(response.error || 'Failed to stop execution');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to stop execution",
        variant: "destructive",
      });
    }
  }, [toast]);

  const retryExecution = useCallback(async (executionId: string) => {
    try {
      const response = await flowMonitoringService.retryExecution(executionId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Execution queued for retry",
        });
      } else {
        throw new Error(response.error || 'Failed to retry execution');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to retry execution",
        variant: "destructive",
      });
    }
  }, [toast]);

  const refreshData = useCallback((filters?: FlowFilters) => {
    loadExecutions(filters);
    loadStats(filters);
  }, [loadExecutions, loadStats]);

  return {
    executions,
    stats,
    loading,
    connected,
    loadExecutions,
    loadStats,
    stopExecution,
    retryExecution,
    refreshData,
  };
};