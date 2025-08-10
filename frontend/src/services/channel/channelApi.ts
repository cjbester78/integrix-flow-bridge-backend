import { api, ApiResponse } from '../api';

export interface Channel {
  id: string;
  name: string;
  description: string;
  status: 'running' | 'idle' | 'stopped';
  load: number;
  throughput: string;
  uptime: string;
  lastActivity: string;
  adapters: Array<{
    id: string;
    name: string;
    category: string;
  }>;
  errorRate: number;
  totalMessages: number;
  avgResponseTime: string;
  businessComponentId?: string;
}

export interface ChannelFilters {
  businessComponentId?: string;
  status?: string;
  category?: string;
  healthStatus?: 'healthy' | 'warning' | 'error';
  limit?: number;
  offset?: number;
}

export interface ChannelAction {
  action: 'start' | 'stop' | 'restart' | 'configure';
  channelId: string;
  parameters?: any;
}

export class ChannelApi {
  // Get all channels with optional filtering
  async getChannels(filters?: ChannelFilters): Promise<ApiResponse<{ channels: Channel[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/channels${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific channel by ID
  async getChannel(id: string): Promise<ApiResponse<Channel>> {
    return api.get<Channel>(`/channels/${id}`);
  }

  // Control channel operations
  async controlChannel(action: ChannelAction): Promise<ApiResponse<Channel>> {
    return api.post<Channel>(`/channels/${action.channelId}/control`, action);
  }

  // Get channels for a specific businessComponent
  async getBusinessComponentChannels(businessComponentId: string, filters?: Omit<ChannelFilters, 'businessComponentId'>): Promise<ApiResponse<{ channels: Channel[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/business-components/${businessComponentId}/channels${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }
}