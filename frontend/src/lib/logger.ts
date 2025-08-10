// @ts-nocheck
// Removed apiClient and useAuthStore imports to avoid circular dependencies
// Using native fetch and localStorage instead

/**
 * Log levels matching backend
 */
export enum LogLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
  FATAL = 'FATAL'
}

/**
 * Log categories for better organization
 */
export enum LogCategory {
  AUTH = 'AUTH',
  API = 'API',
  VALIDATION = 'VALIDATION',
  USER_ACTION = 'USER_ACTION',
  NAVIGATION = 'NAVIGATION',
  PERFORMANCE = 'PERFORMANCE',
  ERROR = 'ERROR',
  SECURITY = 'SECURITY',
  BUSINESS_LOGIC = 'BUSINESS_LOGIC',
  UI = 'UI',
  SYSTEM = 'SYSTEM'
}

/**
 * Log entry structure
 */
export interface LogEntry {
  level: LogLevel;
  category: LogCategory;
  message: string;
  details?: Record<string, any>;
  error?: Error | any;
  stackTrace?: string;
  userAgent?: string;
  url?: string;
  timestamp?: string;
  userId?: string;
  sessionId?: string;
  correlationId?: string;
}

/**
 * Batch configuration
 */
interface BatchConfig {
  maxBatchSize: number;
  flushInterval: number;
  retryAttempts: number;
  retryDelay: number;
}

/**
 * Frontend Logger Class
 */
class FrontendLogger {
  private static instance: FrontendLogger;
  private logQueue: LogEntry[] = [];
  private flushTimer: NodeJS.Timeout | null = null;
  private isOnline: boolean = navigator.onLine;
  private sessionId: string;
  private config: BatchConfig = {
    maxBatchSize: 50,
    flushInterval: 5000, // 5 seconds
    retryAttempts: 3,
    retryDelay: 1000
  };

  private constructor() {
    this.sessionId = this.generateSessionId();
    this.setupEventListeners();
    this.startFlushTimer();
    this.setupGlobalErrorHandlers();
  }

  /**
   * Get singleton instance
   */
  public static getInstance(): FrontendLogger {
    if (!FrontendLogger.instance) {
      FrontendLogger.instance = new FrontendLogger();
    }
    return FrontendLogger.instance;
  }

  /**
   * Generate unique session ID
   */
  private generateSessionId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Setup event listeners
   */
  private setupEventListeners(): void {
    // Online/Offline events
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.info('System', 'Application is online');
      this.flush(); // Flush queued logs
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.warn('System', 'Application is offline');
    });

    // Page visibility
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.flush(); // Flush logs when page becomes hidden
      }
    });

    // Before unload
    window.addEventListener('beforeunload', () => {
      this.flush(true); // Force sync flush
    });
  }

  /**
   * Setup global error handlers
   */
  private setupGlobalErrorHandlers(): void {
    // Global error handler
    window.addEventListener('error', (event) => {
      this.error('System', 'Uncaught error', {
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        error: event.error
      });
    });

    // Unhandled promise rejection
    window.addEventListener('unhandledrejection', (event) => {
      this.error('System', 'Unhandled promise rejection', {
        reason: event.reason,
        promise: event.promise
      });
    });

    // Console error override
    const originalConsoleError = console.error;
    console.error = (...args) => {
      this.error('Console', 'Console error', { arguments: args });
      originalConsoleError.apply(console, args);
    };

    // Console warn override
    const originalConsoleWarn = console.warn;
    console.warn = (...args) => {
      this.warn('Console', 'Console warning', { arguments: args });
      originalConsoleWarn.apply(console, args);
    };
  }

  /**
   * Start flush timer
   */
  private startFlushTimer(): void {
    this.flushTimer = setInterval(() => {
      // Skip flushing if on login page to prevent reload issues
      if (window.location.pathname === '/login') {
        return;
      }
      
      if (this.logQueue.length > 0) {
        this.flush();
      }
    }, this.config.flushInterval);
  }

  /**
   * Add log entry to queue
   */
  private addToQueue(entry: LogEntry): void {
    // Get user ID from localStorage to avoid circular dependencies
    let userId: string | undefined;
    try {
      const userStr = localStorage.getItem('integration_platform_user');
      if (userStr) {
        const user = JSON.parse(userStr);
        userId = user.id;
      }
    } catch (error) {
      // Ignore JSON parse errors
    }

    const enrichedEntry: LogEntry = {
      ...entry,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      sessionId: this.sessionId,
      userId,
      correlationId: this.getCurrentCorrelationId()
    };

    this.logQueue.push(enrichedEntry);

    // Flush if batch size reached
    if (this.logQueue.length >= this.config.maxBatchSize) {
      this.flush();
    }

    // Also log to console in development
    if (import.meta.env.DEV) {
      const consoleMethod = this.getConsoleMethod(entry.level);
      consoleMethod(`[${entry.category}]`, entry.message, entry.details || '');
    }
  }

  /**
   * Get current correlation ID from API client
   */
  private getCurrentCorrelationId(): string | undefined {
    // This would be set by the API client interceptor
    return (window as any).__currentCorrelationId;
  }

  /**
   * Get appropriate console method
   */
  private getConsoleMethod(level: LogLevel): (...args: any[]) => void {
    switch (level) {
      case LogLevel.DEBUG:
        return console.log;
      case LogLevel.INFO:
        return console.info;
      case LogLevel.WARN:
        return console.warn;
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        return console.error;
      default:
        return console.log;
    }
  }

  /**
   * Flush logs to backend
   */
  private async flush(sync: boolean = false): Promise<void> {
    if (this.logQueue.length === 0) return;

    // Check if user is authenticated before trying to send logs
    const isAuthenticated = !!localStorage.getItem('integration_platform_token');
    if (!isAuthenticated && !sync) {
      // Don't try to send logs if not authenticated
      return;
    }

    const logsToSend = [...this.logQueue];
    this.logQueue = [];

    if (!this.isOnline && !sync) {
      // Re-queue logs if offline
      this.logQueue.unshift(...logsToSend);
      return;
    }

    try {
      if (sync) {
        // Synchronous request for beforeunload
        const blob = new Blob([JSON.stringify({ logs: logsToSend })], {
          type: 'application/json'
        });
        navigator.sendBeacon('/api/system/logs/batch', blob);
      } else {
        // Async request
        await this.sendLogs(logsToSend);
      }
    } catch (error) {
      // Re-queue failed logs
      this.logQueue.unshift(...logsToSend);
      console.error('Failed to send logs:', error);
    }
  }

  /**
   * Send logs with retry
   */
  private async sendLogs(logs: LogEntry[], attempt: number = 1): Promise<void> {
    try {
      const token = localStorage.getItem('integration_platform_token');
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      const response = await fetch('/api/system/logs/batch', {
        method: 'POST',
        headers,
        body: JSON.stringify({ logs }),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
    } catch (error) {
      if (attempt < this.config.retryAttempts) {
        await new Promise(resolve => setTimeout(resolve, this.config.retryDelay * attempt));
        return this.sendLogs(logs, attempt + 1);
      }
      throw error;
    }
  }

  /**
   * Log methods
   */
  public debug(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.addToQueue({
      level: LogLevel.DEBUG,
      category: category as LogCategory,
      message,
      details
    });
  }

  public info(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.addToQueue({
      level: LogLevel.INFO,
      category: category as LogCategory,
      message,
      details
    });
  }

  public warn(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.addToQueue({
      level: LogLevel.WARN,
      category: category as LogCategory,
      message,
      details
    });
  }

  public error(category: LogCategory | string, message: string, error?: Error | any, details?: Record<string, any>): void {
    const errorDetails = error instanceof Error ? {
      name: error.name,
      message: error.message,
      stack: error.stack
    } : error;

    this.addToQueue({
      level: LogLevel.ERROR,
      category: category as LogCategory,
      message,
      details: { ...details, ...errorDetails },
      error,
      stackTrace: error?.stack
    });
  }

  public fatal(category: LogCategory | string, message: string, error?: Error | any, details?: Record<string, any>): void {
    const errorDetails = error instanceof Error ? {
      name: error.name,
      message: error.message,
      stack: error.stack
    } : error;

    this.addToQueue({
      level: LogLevel.FATAL,
      category: category as LogCategory,
      message,
      details: { ...details, ...errorDetails },
      error,
      stackTrace: error?.stack
    });

    // Force immediate flush for fatal errors
    this.flush();
  }

  /**
   * Log user action
   */
  public logUserAction(action: string, details?: Record<string, any>): void {
    this.info(LogCategory.USER_ACTION, action, details);
  }

  /**
   * Log API call
   */
  public logApiCall(method: string, url: string, details?: Record<string, any>): void {
    this.debug(LogCategory.API, `${method} ${url}`, details);
  }

  /**
   * Log API error
   */
  public logApiError(method: string, url: string, error: any, details?: Record<string, any>): void {
    this.error(LogCategory.API, `API Error: ${method} ${url}`, error, details);
  }

  /**
   * Log validation error
   */
  public logValidationError(form: string, errors: Record<string, any>): void {
    this.warn(LogCategory.VALIDATION, `Validation failed: ${form}`, { errors });
  }

  /**
   * Log navigation
   */
  public logNavigation(from: string, to: string, details?: Record<string, any>): void {
    this.info(LogCategory.NAVIGATION, `Navigate: ${from} -> ${to}`, details);
  }

  /**
   * Log performance metric
   */
  public logPerformance(metric: string, value: number, details?: Record<string, any>): void {
    this.debug(LogCategory.PERFORMANCE, `Performance: ${metric}`, {
      value,
      ...details
    });
  }

  /**
   * Log security event
   */
  public logSecurityEvent(event: string, details?: Record<string, any>): void {
    this.warn(LogCategory.SECURITY, `Security: ${event}`, details);
  }

  /**
   * Measure and log execution time
   */
  public async measureExecutionTime<T>(
    name: string,
    fn: () => Promise<T>,
    category: LogCategory = LogCategory.PERFORMANCE
  ): Promise<T> {
    const start = performance.now();
    try {
      const result = await fn();
      const duration = performance.now() - start;
      this.logPerformance(name, duration, { status: 'success' });
      return result;
    } catch (error) {
      const duration = performance.now() - start;
      this.logPerformance(name, duration, { status: 'error', error });
      throw error;
    }
  }

  /**
   * Create a child logger with context
   */
  public createContextLogger(context: Record<string, any>): ContextLogger {
    return new ContextLogger(this, context);
  }

  /**
   * Force flush all logs
   */
  public forceFlush(): Promise<void> {
    return this.flush();
  }

  /**
   * Clear log queue
   */
  public clearQueue(): void {
    this.logQueue = [];
  }

  /**
   * Get queue size
   */
  public getQueueSize(): number {
    return this.logQueue.length;
  }

  /**
   * Destroy logger instance
   */
  public destroy(): void {
    if (this.flushTimer) {
      clearInterval(this.flushTimer);
    }
    this.flush(true);
  }
}

/**
 * Context logger for adding persistent context
 */
class ContextLogger {
  constructor(
    private logger: FrontendLogger,
    private context: Record<string, any>
  ) {}

  private enrichDetails(details?: Record<string, any>): Record<string, any> {
    return { ...this.context, ...details };
  }

  debug(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.logger.debug(category, message, this.enrichDetails(details));
  }

  info(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.logger.info(category, message, this.enrichDetails(details));
  }

  warn(category: LogCategory | string, message: string, details?: Record<string, any>): void {
    this.logger.warn(category, message, this.enrichDetails(details));
  }

  error(category: LogCategory | string, message: string, error?: Error | any, details?: Record<string, any>): void {
    this.logger.error(category, message, error, this.enrichDetails(details));
  }
}

// Export singleton instance
export const logger = FrontendLogger.getInstance();

// Export types
export type { LogEntry, ContextLogger };