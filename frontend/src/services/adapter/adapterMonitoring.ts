import { api, ApiResponse } from '../api';
import { AdapterStats, AdapterLogParams } from '@/types/adapter';

export class AdapterMonitoring {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private logListeners: ((log: any) => void)[] = [];
  private statsListeners: ((stats: AdapterStats) => void)[] = [];

  // Get adapter statistics and health
  async getAdapterStats(id: string, timeRange?: string): Promise<ApiResponse<AdapterStats>> {
    const endpoint = `/adapters/${id}/stats${timeRange ? `?timeRange=${timeRange}` : ''}`;
    return api.get(endpoint);
  }

  // Get adapter execution logs
  async getAdapterLogs(id: string, params?: AdapterLogParams): Promise<ApiResponse<any[]>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/adapters/${id}/logs${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // WebSocket Real-time Updates for Adapter Logs
  connectWebSocket(adapterId?: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/adapters${adapterId ? `/${adapterId}/logs` : '/logs'}`;
    
    try {
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket connected for adapter monitoring');
        this.reconnectAttempts = 0;
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'new_log') {
            this.logListeners.forEach(listener => listener(data.log));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };
      
      this.websocket.onclose = () => {
        console.log('WebSocket connection closed');
        this.attemptReconnect(adapterId);
      };
      
      this.websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
    }
  }

  private attemptReconnect(adapterId?: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connectWebSocket(adapterId);
      }, this.reconnectInterval * this.reconnectAttempts);
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.logListeners = [];
    this.statsListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners
  onLogUpdate(callback: (log: any) => void): () => void {
    this.logListeners.push(callback);
    return () => {
      const index = this.logListeners.indexOf(callback);
      if (index > -1) this.logListeners.splice(index, 1);
    };
  }

  onStatsUpdate(callback: (stats: AdapterStats) => void): () => void {
    this.statsListeners.push(callback);
    return () => {
      const index = this.statsListeners.indexOf(callback);
      if (index > -1) this.statsListeners.splice(index, 1);
    };
  }

  sendCommand(command: string, data?: any): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({ command, data }));
    }
  }
}