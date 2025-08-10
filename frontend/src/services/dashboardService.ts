import { api } from './api';

export interface DashboardStats {
  activeIntegrations: number;
  messagesToday: number;
  successRate: number;
  avgResponseTime: number;
}

export interface DashboardMetric {
  title: string;
  value: string;
  change: string;
  icon: string;
  color: string;
}

export interface RecentMessage {
  id: string;
  source: string;
  target: string;
  status: 'success' | 'failed' | 'processing';
  time: string;
  businessComponentId?: string;
}

export interface AdapterStatus {
  id: string;
  name: string;
  type: string;
  mode: string;
  status: 'running' | 'stopped' | 'error';
  load: number;
  businessComponentId?: string;
  businessComponentName?: string;
  messagesProcessed?: number;
  errorsCount?: number;
  lastActivityTime?: string;
}

export interface DashboardData {
  stats: DashboardStats;
  metrics: DashboardMetric[];
  recentMessages: RecentMessage[];
  adapterStatuses: AdapterStatus[];
}

class DashboardService {
  async getDashboardStats(businessComponentId?: string): Promise<{ success: boolean; data?: DashboardStats; error?: string }> {
    try {
      const endpoint = businessComponentId 
        ? `/dashboard/stats?businessComponentId=${businessComponentId}` 
        : '/dashboard/stats';
      return await api.get<DashboardStats>(endpoint);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch dashboard stats'
      };
    }
  }

  async getDashboardMetrics(businessComponentId?: string): Promise<{ success: boolean; data?: DashboardMetric[]; error?: string }> {
    try {
      const endpoint = businessComponentId 
        ? `/dashboard/metrics?businessComponentId=${businessComponentId}` 
        : '/dashboard/metrics';
      const response = await api.get<DashboardStats>(endpoint);
      
      if (response.success && response.data) {
        // Transform stats into metrics format
        const stats = response.data;
        const metrics: DashboardMetric[] = [
          {
            title: "Active Integrations",
            value: stats.activeIntegrations?.toString() || '0',
            change: "+0%", // This should come from backend
            icon: "Activity",
            color: "text-success"
          },
          {
            title: "Messages Today",
            value: stats.messagesToday?.toLocaleString() || '0',
            change: "+0%", // This should come from backend
            icon: "MessageSquare",
            color: "text-info"
          },
          {
            title: "Success Rate",
            value: stats.successRate ? `${stats.successRate.toFixed(1)}%` : '0.0%',
            change: "+0%", // This should come from backend
            icon: "CheckCircle",
            color: "text-success"
          },
          {
            title: "Avg Response Time",
            value: stats.avgResponseTime ? `${stats.avgResponseTime}ms` : '0ms',
            change: "0ms", // This should come from backend
            icon: "Zap",
            color: "text-warning"
          }
        ];
        
        return { success: true, data: metrics };
      }
      
      // Return empty metrics if the API doesn't return the expected format
      return { success: false, data: [], error: 'Invalid response format' };
    } catch (error) {
      console.error('Dashboard metrics error:', error);
      return {
        success: false,
        data: [], // Return empty array to prevent map errors
        error: error instanceof Error ? error.message : 'Failed to fetch dashboard metrics'
      };
    }
  }

  async getRecentMessages(businessComponentId?: string, limit: number = 10): Promise<{ success: boolean; data?: RecentMessage[]; error?: string }> {
    try {
      const endpoint = businessComponentId 
        ? `/messages/recent?businessComponentId=${businessComponentId}&limit=${limit}` 
        : `/messages/recent?limit=${limit}`;
      return await api.get<RecentMessage[]>(endpoint);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch recent messages'
      };
    }
  }

  async getAdapterStatuses(businessComponentId?: string): Promise<{ success: boolean; data?: AdapterStatus[]; error?: string }> {
    try {
      const endpoint = businessComponentId 
        ? `/adapter-monitoring/status?businessComponentId=${businessComponentId}` 
        : '/adapter-monitoring/status';
      return await api.get<AdapterStatus[]>(endpoint);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch adapter statuses'
      };
    }
  }

  async getDashboardData(businessComponentId?: string): Promise<{ success: boolean; data?: DashboardData; error?: string }> {
    try {
      // Fetch all dashboard data in parallel
      const [statsResponse, messagesResponse, adaptersResponse] = await Promise.all([
        this.getDashboardStats(businessComponentId),
        this.getRecentMessages(businessComponentId),
        this.getAdapterStatuses(businessComponentId)
      ]);

      if (!statsResponse.success || !messagesResponse.success || !adaptersResponse.success) {
        return {
          success: false,
          error: 'Failed to fetch some dashboard data'
        };
      }

      // Transform stats to metrics
      const metrics = await this.getDashboardMetrics(businessComponentId);

      return {
        success: true,
        data: {
          stats: statsResponse.data!,
          metrics: metrics.data || [],
          recentMessages: messagesResponse.data || [],
          adapterStatuses: adaptersResponse.data || []
        }
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch dashboard data'
      };
    }
  }
}

export const dashboardService = new DashboardService();