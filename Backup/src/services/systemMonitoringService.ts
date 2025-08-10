import { api, ApiResponse } from './api';

export interface SystemHealth {
  status: 'healthy' | 'warning' | 'critical';
  uptime: string;
  version: string;
  timestamp: string;
  components: ComponentHealth[];
  metrics: SystemMetrics;
}

export interface ComponentHealth {
  name: string;
  status: 'healthy' | 'warning' | 'error';
  message?: string;
  lastCheck: string;
  responseTime?: number;
}

export interface SystemMetrics {
  cpu: number;
  memory: number;
  disk: number;
  activeConnections: number;
  requestsPerMinute: number;
  errorRate: number;
  avgResponseTime: number;
}

export interface SystemAlert {
  id: string;
  type: 'info' | 'warning' | 'error' | 'critical';
  title: string;
  message: string;
  timestamp: string;
  source: string;
  acknowledged: boolean;
  resolvedAt?: string;
}

export interface SystemStats {
  totalUsers: number;
  activeUsers: number;
  totalFlows: number;
  activeFlows: number;
  totalMessages: number;
  messagesPerHour: number;
  totalChannels: number;
  activeChannels: number;
  storageUsed: string;
  storageLimit: string;
}

class SystemMonitoringService {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private healthListeners: ((health: SystemHealth) => void)[] = [];
  private alertListeners: ((alert: SystemAlert) => void)[] = [];
  private statsListeners: ((stats: SystemStats) => void)[] = [];

  // Get system health status
  async getSystemHealth(): Promise<ApiResponse<SystemHealth>> {
    return api.get<SystemHealth>('/system/health');
  }

  // Get system statistics
  async getSystemStats(): Promise<ApiResponse<SystemStats>> {
    return api.get<SystemStats>('/system/stats');
  }

  // Get system alerts
  async getSystemAlerts(filters?: {
    type?: string;
    acknowledged?: boolean;
    limit?: number;
    offset?: number;
  }): Promise<ApiResponse<{ alerts: SystemAlert[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/system/alerts${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Acknowledge alert
  async acknowledgeAlert(alertId: string): Promise<ApiResponse<SystemAlert>> {
    return api.post<SystemAlert>(`/system/alerts/${alertId}/acknowledge`);
  }

  // Resolve alert
  async resolveAlert(alertId: string): Promise<ApiResponse<SystemAlert>> {
    return api.post<SystemAlert>(`/system/alerts/${alertId}/resolve`);
  }

  // Get audit logs
  async getAuditLogs(filters?: {
    userId?: string;
    action?: string;
    startDate?: string;
    endDate?: string;
    limit?: number;
    offset?: number;
  }): Promise<ApiResponse<any[]>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/system/audit${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // System maintenance operations
  async performMaintenance(operation: string, parameters?: any): Promise<ApiResponse<any>> {
    return api.post('/system/maintenance', { operation, parameters });
  }

  // WebSocket Real-time Updates
  connectWebSocket(): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/system`;
    
    try {
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket connected for system monitoring');
        this.reconnectAttempts = 0;
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'health_update') {
            this.healthListeners.forEach(listener => listener(data.health));
          } else if (data.type === 'new_alert') {
            this.alertListeners.forEach(listener => listener(data.alert));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };
      
      this.websocket.onclose = () => {
        console.log('WebSocket connection closed');
        this.attemptReconnect();
      };
      
      this.websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
    }
  }

  private attemptReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connectWebSocket();
      }, this.reconnectInterval * this.reconnectAttempts);
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.healthListeners = [];
    this.alertListeners = [];
    this.statsListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners
  onHealthUpdate(callback: (health: SystemHealth) => void): () => void {
    this.healthListeners.push(callback);
    return () => {
      const index = this.healthListeners.indexOf(callback);
      if (index > -1) this.healthListeners.splice(index, 1);
    };
  }

  onAlert(callback: (alert: SystemAlert) => void): () => void {
    this.alertListeners.push(callback);
    return () => {
      const index = this.alertListeners.indexOf(callback);
      if (index > -1) this.alertListeners.splice(index, 1);
    };
  }

  onStatsUpdate(callback: (stats: SystemStats) => void): () => void {
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

export const systemMonitoringService = new SystemMonitoringService();