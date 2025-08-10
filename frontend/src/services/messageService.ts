import { api, ApiResponse } from './api';

export type MessageStatus = 'success' | 'failed' | 'processing';

export interface MessageLog {
  timestamp: string;
  level: string;
  message: string;
}

export interface Message {
  id: string;
  timestamp: string;
  source: string;
  target: string;
  type: string;
  status: MessageStatus;
  processingTime: string;
  size: string;
  businessComponentId: string;
  logs: MessageLog[];
}

export interface MessageFilters {
  status?: MessageStatus[];
  source?: string;
  target?: string;
  type?: string;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
}

export interface MessageStats {
  total: number;
  successful: number;
  processing: number;
  failed: number;
  successRate: number;
  avgProcessingTime: number;
}

class MessageService {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private messageListeners: ((message: Message) => void)[] = [];
  private statsListeners: ((stats: MessageStats) => void)[] = [];

  // Get all messages with optional filtering
  async getMessages(filters?: MessageFilters): Promise<ApiResponse<{ messages: Message[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }
    
    const endpoint = `/messages${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific message by ID
  async getMessage(id: string): Promise<ApiResponse<Message>> {
    return api.get<Message>(`/messages/${id}`);
  }

  // Get message statistics
  async getMessageStats(filters?: Omit<MessageFilters, 'limit' | 'offset'>): Promise<ApiResponse<MessageStats>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }
    
    const endpoint = `/messages/stats${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Reprocess a failed message
  async reprocessMessage(id: string): Promise<ApiResponse<Message>> {
    return api.post<Message>(`/messages/${id}/reprocess`);
  }

  // Get messages for a specific businessComponent
  async getBusinessComponentMessages(businessComponentId: string, filters?: Omit<MessageFilters, 'businessComponentId'>): Promise<ApiResponse<{ messages: Message[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            queryParams.append(key, value.join(','));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }
    
    const endpoint = `/api/business-components/${businessComponentId}/messages${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // WebSocket Real-time Updates
  connectWebSocket(businessComponentId?: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      return; // Already connected
    }

    // Use dynamic host detection for WebSocket
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.hostname;
    const port = window.location.port || '8080';
    const baseUrl = import.meta.env.VITE_WS_URL || `${protocol}//${host}:${port}`;
    const queryParams = businessComponentId ? `?businessComponentId=${businessComponentId}` : '';
    const wsUrl = `${baseUrl}/ws/messages${queryParams}`;
    
    try {
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket connected for message monitoring');
        this.reconnectAttempts = 0;
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'message_update') {
            this.messageListeners.forEach(listener => listener(data.message));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
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
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.messageListeners = [];
    this.statsListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners for real-time updates
  onMessageUpdate(callback: (message: Message) => void): () => void {
    this.messageListeners.push(callback);
    
    // Return unsubscribe function
    return () => {
      const index = this.messageListeners.indexOf(callback);
      if (index > -1) {
        this.messageListeners.splice(index, 1);
      }
    };
  }

  onStatsUpdate(callback: (stats: MessageStats) => void): () => void {
    this.statsListeners.push(callback);
    
    // Return unsubscribe function
    return () => {
      const index = this.statsListeners.indexOf(callback);
      if (index > -1) {
        this.statsListeners.splice(index, 1);
      }
    };
  }

  // Send WebSocket command (e.g., subscribe to specific message types)
  sendCommand(command: string, data?: any): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({ command, data }));
    }
  }
}

export const messageService = new MessageService();