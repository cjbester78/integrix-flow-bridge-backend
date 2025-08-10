import { api, ApiResponse } from '../api';
import { ChannelFilters } from './channelApi';

export interface ChannelStats {
  total: number;
  running: number;
  idle: number;
  stopped: number;
  errorChannels: number;
  avgUptime: number;
  totalThroughput: number;
}

export class ChannelStatsService {
  // Get channel statistics
  async getChannelStats(filters?: Omit<ChannelFilters, 'limit' | 'offset'>): Promise<ApiResponse<ChannelStats>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/channels/stats${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }
}