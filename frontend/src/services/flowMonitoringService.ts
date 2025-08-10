import { api, ApiResponse } from './api';
import { IntegrationFlow } from '@/types/flow';

export interface FlowExecution {
  id: string;
  flowId: string;
  status: 'running' | 'completed' | 'failed' | 'queued';
  startTime: string;
  endTime?: string;
  duration?: number;
  inputSize: number;
  outputSize?: number;
  processedRecords: number;
  errorCount: number;
  warnings: string[];
  businessComponentId?: string;
}

export interface FlowMonitoringStats {
  totalExecutions: number;
  runningExecutions: number;
  completedExecutions: number;
  failedExecutions: number;
  queuedExecutions: number;
  avgExecutionTime: number;
  successRate: number;
  totalProcessedRecords: number;
}

export interface FlowFilters {
  businessComponentId?: string;
  status?: string;
  flowId?: string;
  startDate?: string;
  endDate?: string;
  limit?: number;
  offset?: number;
}

class FlowMonitoringService {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private executionListeners: ((execution: FlowExecution) => void)[] = [];
  private statsListeners: ((stats: FlowMonitoringStats) => void)[] = [];
  private flowListeners: ((flow: IntegrationFlow) => void)[] = [];

  // Get flow executions
  async getFlowExecutions(filters?: FlowFilters): Promise<ApiResponse<{ executions: FlowExecution[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/flows/executions${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get execution statistics
  async getExecutionStats(filters?: Omit<FlowFilters, 'limit' | 'offset'>): Promise<ApiResponse<FlowMonitoringStats>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/flows/executions/stats${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Stop/cancel execution
  async stopExecution(executionId: string): Promise<ApiResponse<FlowExecution>> {
    return api.post<FlowExecution>(`/flows/executions/${executionId}/stop`);
  }

  // Retry failed execution
  async retryExecution(executionId: string): Promise<ApiResponse<FlowExecution>> {
    return api.post<FlowExecution>(`/flows/executions/${executionId}/retry`);
  }

  // Get real-time flow performance
  async getFlowPerformance(flowId: string, timeRange?: string): Promise<ApiResponse<any>> {
    const endpoint = `/flows/${flowId}/performance${timeRange ? `?timeRange=${timeRange}` : ''}`;
    return api.get(endpoint);
  }

  // WebSocket Real-time Updates
  connectWebSocket(businessComponentId?: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/flows${businessComponentId ? `?businessComponentId=${businessComponentId}` : ''}`;
    
    try {
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket connected for flow monitoring');
        this.reconnectAttempts = 0;
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'execution_update') {
            this.executionListeners.forEach(listener => listener(data.execution));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
          } else if (data.type === 'flow_update') {
            this.flowListeners.forEach(listener => listener(data.flow));
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };
      
      this.websocket.onclose = () => {
        console.log('WebSocket connection closed');
        this.attemptReconnect(businessComponentId);
      };
      
      this.websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
    }
  }

  private attemptReconnect(businessComponentId?: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connectWebSocket(businessComponentId);
      }, this.reconnectInterval * this.reconnectAttempts);
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.executionListeners = [];
    this.statsListeners = [];
    this.flowListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners
  onExecutionUpdate(callback: (execution: FlowExecution) => void): () => void {
    this.executionListeners.push(callback);
    return () => {
      const index = this.executionListeners.indexOf(callback);
      if (index > -1) this.executionListeners.splice(index, 1);
    };
  }

  onStatsUpdate(callback: (stats: FlowMonitoringStats) => void): () => void {
    this.statsListeners.push(callback);
    return () => {
      const index = this.statsListeners.indexOf(callback);
      if (index > -1) this.statsListeners.splice(index, 1);
    };
  }

  onFlowUpdate(callback: (flow: IntegrationFlow) => void): () => void {
    this.flowListeners.push(callback);
    return () => {
      const index = this.flowListeners.indexOf(callback);
      if (index > -1) this.flowListeners.splice(index, 1);
    };
  }

  sendCommand(command: string, data?: any): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({ command, data }));
    }
  }
}

export const flowMonitoringService = new FlowMonitoringService();