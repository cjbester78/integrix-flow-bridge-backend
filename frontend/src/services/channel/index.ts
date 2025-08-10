import { ChannelApi } from './channelApi';
import { ChannelWebSocket } from './channelWebSocket';
import { ChannelStatsService } from './channelStats';
import { ChannelLogsService } from './channelLogs';

class ChannelService extends ChannelApi {
  private webSocket: ChannelWebSocket;
  private statsService: ChannelStatsService;
  private logsService: ChannelLogsService;

  constructor() {
    super();
    this.webSocket = new ChannelWebSocket();
    this.statsService = new ChannelStatsService();
    this.logsService = new ChannelLogsService();
  }

  // WebSocket methods
  connectWebSocket(businessComponentId?: string): void {
    return this.webSocket.connectWebSocket(businessComponentId);
  }

  disconnectWebSocket(): void {
    return this.webSocket.disconnectWebSocket();
  }

  onChannelUpdate(callback: (channel: any) => void): () => void {
    return this.webSocket.onChannelUpdate(callback);
  }

  onStatsUpdate(callback: (stats: any) => void): () => void {
    return this.webSocket.onStatsUpdate(callback);
  }

  onAlert(callback: (alert: any) => void): () => void {
    return this.webSocket.onAlert(callback);
  }

  sendCommand(command: string, data?: any): void {
    return this.webSocket.sendCommand(command, data);
  }

  connectLogStream(channelId: string): void {
    return this.webSocket.connectLogStream(channelId);
  }

  disconnectLogStream(channelId: string): void {
    return this.webSocket.disconnectLogStream(channelId);
  }

  onLogUpdate(callback: (log: any) => void): () => void {
    return this.webSocket.onChannelUpdate(callback);
  }

  // Stats methods
  async getChannelStats(filters?: any) {
    return this.statsService.getChannelStats(filters);
  }

  // Logs methods
  async getChannelLogs(channelId: string, filters?: any) {
    return this.logsService.getChannelLogs(channelId, filters);
  }

  async exportChannelLogs(channelId: string, filters?: any) {
    return this.logsService.exportChannelLogs(channelId, filters);
  }
}

export const channelService = new ChannelService();
export * from './channelApi';
export * from './channelStats';