import { SystemLogEntry } from '@/hooks/useSystemLogs';

class SystemErrorLogger {
  private logs: SystemLogEntry[] = [];
  private listeners: ((log: SystemLogEntry) => void)[] = [];

  constructor() {
    // Listen for unhandled errors
    window.addEventListener('error', (event) => {
      this.logError('System Error', {
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        stack: event.error?.stack,
      });
    });

    // Listen for unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      this.logError('Unhandled Promise Rejection', {
        reason: event.reason,
        stack: event.reason?.stack,
      });
    });

    // Add some sample logs for demo
    this.initializeSampleLogs();
  }

  private initializeSampleLogs() {
    const sampleLogs: SystemLogEntry[] = [
      {
        id: 'sys-001',
        timestamp: new Date(Date.now() - 30000).toISOString(),
        level: 'error',
        message: 'Failed to connect to WebSocket server',
        details: { url: 'ws://localhost:8080', error: 'Connection refused' },
        source: 'system',
        sourceId: 'websocket',
        sourceName: 'WebSocket Service',
      },
      {
        id: 'sys-002',
        timestamp: new Date(Date.now() - 60000).toISOString(),
        level: 'warn',
        message: 'API request timeout',
        details: { endpoint: '/api/adapters', timeout: 5000 },
        source: 'api',
        sourceId: 'api-service',
        sourceName: 'API Service',
      },
      {
        id: 'adapter-001',
        timestamp: new Date(Date.now() - 90000).toISOString(),
        level: 'info',
        message: 'Adapter successfully processed message',
        details: { messageId: 'msg-123', processingTime: 450 },
        source: 'adapter',
        sourceId: 'adapter-001',
        sourceName: 'SAP R/3 Adapter',
      },
      {
        id: 'channel-001',
        timestamp: new Date(Date.now() - 120000).toISOString(),
        level: 'error',
        message: 'Channel connection lost',
        details: { channelId: 'ch-001', error: 'Network timeout' },
        source: 'channel',
        sourceId: 'ch-001',
        sourceName: 'ERP Channel',
      },
      {
        id: 'sys-003',
        timestamp: new Date(Date.now() - 150000).toISOString(),
        level: 'debug',
        message: 'System health check completed',
        details: { 
          memory: { used: '245MB', total: '512MB' },
          cpu: '12%',
          uptime: '2h 30m'
        },
        source: 'system',
        sourceId: 'health-monitor',
        sourceName: 'Health Monitor',
      },
    ];

    this.logs = sampleLogs;
  }

  logError(message: string, details?: any) {
    const logEntry: SystemLogEntry = {
      id: `sys-${Date.now()}`,
      timestamp: new Date().toISOString(),
      level: 'error',
      message,
      details,
      source: 'system',
      sourceId: 'error-logger',
      sourceName: 'System Error Logger',
    };

    this.logs.unshift(logEntry);
    this.notifyListeners(logEntry);
  }

  logInfo(message: string, details?: any, source: SystemLogEntry['source'] = 'system', sourceId?: string, sourceName?: string) {
    const logEntry: SystemLogEntry = {
      id: `log-${Date.now()}`,
      timestamp: new Date().toISOString(),
      level: 'info',
      message,
      details,
      source,
      sourceId: sourceId || 'general',
      sourceName: sourceName || 'System',
    };

    this.logs.unshift(logEntry);
    this.notifyListeners(logEntry);
  }

  logWarning(message: string, details?: any, source: SystemLogEntry['source'] = 'system', sourceId?: string, sourceName?: string) {
    const logEntry: SystemLogEntry = {
      id: `warn-${Date.now()}`,
      timestamp: new Date().toISOString(),
      level: 'warn',
      message,
      details,
      source,
      sourceId: sourceId || 'general',
      sourceName: sourceName || 'System',
    };

    this.logs.unshift(logEntry);
    this.notifyListeners(logEntry);
  }

  getAllLogs(): SystemLogEntry[] {
    return [...this.logs];
  }

  getFilteredLogs(filters: {
    source?: SystemLogEntry['source'];
    sourceId?: string;
    level?: SystemLogEntry['level'];
    search?: string;
  }): SystemLogEntry[] {
    let filtered = [...this.logs];

    if (filters.source) {
      filtered = filtered.filter(log => log.source === filters.source);
    }

    if (filters.sourceId) {
      filtered = filtered.filter(log => log.sourceId === filters.sourceId);
    }

    if (filters.level) {
      filtered = filtered.filter(log => log.level === filters.level);
    }

    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(log =>
        log.message.toLowerCase().includes(searchLower) ||
        log.sourceName?.toLowerCase().includes(searchLower) ||
        (log.details && JSON.stringify(log.details).toLowerCase().includes(searchLower))
      );
    }

    return filtered;
  }

  onLogUpdate(callback: (log: SystemLogEntry) => void): () => void {
    this.listeners.push(callback);
    return () => {
      const index = this.listeners.indexOf(callback);
      if (index > -1) this.listeners.splice(index, 1);
    };
  }

  private notifyListeners(log: SystemLogEntry) {
    this.listeners.forEach(listener => listener(log));
  }
}

export const systemErrorLogger = new SystemErrorLogger();