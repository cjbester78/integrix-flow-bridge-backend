export class ChannelWebSocket {
  private websocket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private channelListeners: ((channel: any) => void)[] = [];
  private statsListeners: ((stats: any) => void)[] = [];
  private alertListeners: ((alert: any) => void)[] = [];

  // WebSocket Real-time Updates
  connectWebSocket(customerId?: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/channels${customerId ? `?customerId=${customerId}` : ''}`;
    
    try {
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket connected for channel monitoring');
        this.reconnectAttempts = 0;
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'channel_update') {
            this.channelListeners.forEach(listener => listener(data.channel));
          } else if (data.type === 'stats_update') {
            this.statsListeners.forEach(listener => listener(data.stats));
          } else if (data.type === 'channel_alert') {
            this.alertListeners.forEach(listener => listener(data.alert));
          } else if (data.type === 'channel_log') {
            this.channelListeners.forEach(listener => listener(data));
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };
      
      this.websocket.onclose = () => {
        console.log('WebSocket connection closed');
        this.attemptReconnect(customerId);
      };
      
      this.websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
    }
  }

  private attemptReconnect(customerId?: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connectWebSocket(customerId);
      }, this.reconnectInterval * this.reconnectAttempts);
    }
  }

  disconnectWebSocket(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = null;
    }
    this.channelListeners = [];
    this.statsListeners = [];
    this.alertListeners = [];
    this.reconnectAttempts = 0;
  }

  // Event listeners
  onChannelUpdate(callback: (channel: any) => void): () => void {
    this.channelListeners.push(callback);
    return () => {
      const index = this.channelListeners.indexOf(callback);
      if (index > -1) this.channelListeners.splice(index, 1);
    };
  }

  onStatsUpdate(callback: (stats: any) => void): () => void {
    this.statsListeners.push(callback);
    return () => {
      const index = this.statsListeners.indexOf(callback);
      if (index > -1) this.statsListeners.splice(index, 1);
    };
  }

  onAlert(callback: (alert: any) => void): () => void {
    this.alertListeners.push(callback);
    return () => {
      const index = this.alertListeners.indexOf(callback);
      if (index > -1) this.alertListeners.splice(index, 1);
    };
  }

  sendCommand(command: string, data?: any): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.websocket.send(JSON.stringify({ command, data }));
    }
  }

  // Real-time log streaming
  connectLogStream(channelId: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.sendCommand('subscribe_channel_logs', { channelId });
    }
  }

  disconnectLogStream(channelId: string): void {
    if (this.websocket?.readyState === WebSocket.OPEN) {
      this.sendCommand('unsubscribe_channel_logs', { channelId });
    }
  }
}