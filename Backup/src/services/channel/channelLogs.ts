import { api, ApiResponse } from '../api';

export class ChannelLogsService {
  // Get channel logs
  async getChannelLogs(channelId: string, filters?: any): Promise<ApiResponse<any[]>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/channels/${channelId}/logs${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Export channel logs
  async exportChannelLogs(channelId: string, filters?: any): Promise<ApiResponse<string>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/channels/${channelId}/logs/export${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }
}