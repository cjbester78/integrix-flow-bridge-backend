import { api } from './api';

export type ChannelStatus = 'active' | 'inactive' | 'error' | 'running' | 'idle' | 'stopped';
export type AdapterStatus = 'connected' | 'disconnected' | 'error';
export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'SUCCESS' | 'error' | 'warn' | 'info' | 'debug';

export interface ChannelAdapter {
  id: string;
  name: string;
  icon: string;
  category: string;
  status: AdapterStatus;
  lastSync?: string;
  errorMessage?: string;
  messagesProcessed?: number;
  errorCount?: number;
  avgResponseTime?: string;
}

export interface ChannelLog {
  id: string;
  timestamp: string;
  level: LogLevel;
  message: string;
  channelId: string;
  details?: any;
  adapterName?: string;
  correlationId?: string;
  duration?: number;
}

export interface FlowMetrics {
  totalMessages: number;
  successfulMessages: number;
  failedMessages: number;
  averageProcessingTime: string;
  lastProcessed?: string;
}

export interface Channel {
  id: string;
  name: string;
  description: string;
  status: ChannelStatus;
  adapters: ChannelAdapter[];
  flowMetrics: FlowMetrics;
  lastActivity: string;
  customerId: string;
  load?: number;
  throughput?: string;
  uptime?: string;
  errorRate?: number;
  healthScore?: number;
  totalMessages?: number;
}

export interface ChannelLogsFilters {
  level?: LogLevel[];
  dateFrom?: string;
  dateTo?: string;
  search?: string;
  limit?: number;
}

class ChannelService {
  async getChannels(customerId?: string): Promise<{ success: boolean; data?: Channel[]; error?: string }> {
    try {
      const endpoint = customerId ? `/channels?customerId=${customerId}` : '/channels';
      return await api.get<Channel[]>(endpoint);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch channels'
      };
    }
  }

  async getChannelById(channelId: string): Promise<{ success: boolean; data?: Channel; error?: string }> {
    try {
      return await api.get<Channel>(`/channels/${channelId}`);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch channel'
      };
    }
  }

  async createChannel(channel: Omit<Channel, 'id'>): Promise<{ success: boolean; data?: Channel; error?: string }> {
    try {
      return await api.post<Channel>('/channels', channel);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to create channel'
      };
    }
  }

  async updateChannel(channelId: string, updates: Partial<Channel>): Promise<{ success: boolean; data?: Channel; error?: string }> {
    try {
      return await api.put<Channel>(`/channels/${channelId}`, updates);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to update channel'
      };
    }
  }

  async deleteChannel(channelId: string): Promise<{ success: boolean; error?: string }> {
    try {
      await api.delete(`/channels/${channelId}`);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to delete channel'
      };
    }
  }

  async getChannelLogs(channelId: string, filters?: ChannelLogsFilters): Promise<{ success: boolean; data?: ChannelLog[]; error?: string }> {
    try {
      const params = new URLSearchParams();
      if (filters?.level?.length) params.append('level', filters.level.join(','));
      if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom);
      if (filters?.dateTo) params.append('dateTo', filters.dateTo);
      if (filters?.search) params.append('search', filters.search);

      const queryString = params.toString() ? `?${params.toString()}` : '';
      return await api.get<ChannelLog[]>(`/channels/${channelId}/logs${queryString}`);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch channel logs'
      };
    }
  }

  async getAvailableAdapters(): Promise<{ success: boolean; data?: ChannelAdapter[]; error?: string }> {
    try {
      return await api.get<ChannelAdapter[]>('/adapters');
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch available adapters'
      };
    }
  }

  async testChannelConnection(channelId: string): Promise<{ success: boolean; error?: string }> {
    try {
      await api.post(`/channels/${channelId}/test`);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to test channel connection'
      };
    }
  }
}

export const channelService = new ChannelService();