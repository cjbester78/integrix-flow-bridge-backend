import { useState, useEffect, useCallback } from 'react';
import { systemMonitoringService, SystemHealth, SystemStats, SystemAlert } from '@/services/systemMonitoringService';
import { useToast } from '@/hooks/use-toast';

export const useSystemMonitoring = () => {
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [stats, setStats] = useState<SystemStats | null>(null);
  const [alerts, setAlerts] = useState<SystemAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const { toast } = useToast();

  const loadHealth = useCallback(async () => {
    try {
      const response = await systemMonitoringService.getSystemHealth();
      if (response.success && response.data) {
        setHealth(response.data);
      }
    } catch (error) {
      console.error('Failed to load system health:', error);
    }
  }, []);

  const loadStats = useCallback(async () => {
    try {
      const response = await systemMonitoringService.getSystemStats();
      if (response.success && response.data) {
        setStats(response.data);
      }
    } catch (error) {
      console.error('Failed to load system stats:', error);
    }
  }, []);

  const loadAlerts = useCallback(async () => {
    try {
      const response = await systemMonitoringService.getSystemAlerts({ acknowledged: false });
      if (response.success && response.data) {
        setAlerts(response.data.alerts);
      }
    } catch (error) {
      console.error('Failed to load system alerts:', error);
    }
  }, []);

  useEffect(() => {
    systemMonitoringService.connectWebSocket();
    setConnected(true);

    const unsubscribeHealth = systemMonitoringService.onHealthUpdate((newHealth) => {
      setHealth(newHealth);
    });

    const unsubscribeStats = systemMonitoringService.onStatsUpdate((newStats) => {
      setStats(newStats);
    });

    const unsubscribeAlerts = systemMonitoringService.onAlert((alert) => {
      setAlerts(prev => [alert, ...prev]);
      
      // Show toast for critical alerts
      if (alert.type === 'critical' || alert.type === 'error') {
        toast({
          title: alert.title,
          description: alert.message,
          variant: "destructive",
        });
      }
    });

    const loadInitialData = async () => {
      setLoading(true);
      await Promise.all([loadHealth(), loadStats(), loadAlerts()]);
      setLoading(false);
    };

    loadInitialData();

    return () => {
      unsubscribeHealth();
      unsubscribeStats();
      unsubscribeAlerts();
      systemMonitoringService.disconnectWebSocket();
      setConnected(false);
    };
  }, [loadHealth, loadStats, loadAlerts, toast]);

  const acknowledgeAlert = useCallback(async (alertId: string) => {
    try {
      const response = await systemMonitoringService.acknowledgeAlert(alertId);
      if (response.success) {
        setAlerts(prev => prev.map(alert => 
          alert.id === alertId ? { ...alert, acknowledged: true } : alert
        ));
        toast({
          title: "Success",
          description: "Alert acknowledged",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to acknowledge alert",
        variant: "destructive",
      });
    }
  }, [toast]);

  const resolveAlert = useCallback(async (alertId: string) => {
    try {
      const response = await systemMonitoringService.resolveAlert(alertId);
      if (response.success) {
        setAlerts(prev => prev.filter(alert => alert.id !== alertId));
        toast({
          title: "Success",
          description: "Alert resolved",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to resolve alert",
        variant: "destructive",
      });
    }
  }, [toast]);

  const refreshData = useCallback(() => {
    loadHealth();
    loadStats();
    loadAlerts();
  }, [loadHealth, loadStats, loadAlerts]);

  return {
    health,
    stats,
    alerts,
    loading,
    connected,
    acknowledgeAlert,
    resolveAlert,
    refreshData,
  };
};