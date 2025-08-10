export interface IntegrationFlow {
  id: string;
  name: string;
  description: string;
  status: 'active' | 'inactive' | 'error';
  sourceAdapter: string;
  targetAdapter: string;
  transformationConfig?: any;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  customerId?: string;
}

export interface FlowStep {
  id: string;
  type: 'adapter' | 'transformation' | 'condition' | 'loop' | 'delay';
  name: string;
  configuration: Record<string, any>;
  inputSchema?: any;
  outputSchema?: any;
  position?: { x: number; y: number };
  connections?: string[];
}

export interface FlowDefinition {
  id: string;
  name: string;
  description?: string;
  version: string;
  steps: FlowStep[];
  triggers: FlowTrigger[];
  variables?: Record<string, any>;
  settings: FlowSettings;
  createdAt: string;
  updatedAt: string;
  status: 'draft' | 'published' | 'deprecated';
}

export interface FlowTrigger {
  id: string;
  type: 'manual' | 'schedule' | 'webhook' | 'event';
  configuration: Record<string, any>;
  enabled: boolean;
}

export interface FlowSettings {
  retryPolicy: {
    maxRetries: number;
    retryDelay: number;
    backoffStrategy: 'fixed' | 'exponential';
  };
  timeout: number;
  parallelExecution: boolean;
  errorHandling: 'stop' | 'continue' | 'retry';
  logging: boolean;
}

export interface FlowExecution {
  id: string;
  flowId: string;
  flowVersion: string;
  status: 'pending' | 'running' | 'completed' | 'failed' | 'cancelled' | 'paused';
  startTime: string;
  endTime?: string;
  duration?: number;
  triggeredBy: string;
  triggerType: string;
  context: Record<string, any>;
  steps: StepExecution[];
  error?: ExecutionError;
  metrics: ExecutionMetrics;
}

export interface StepExecution {
  stepId: string;
  stepName: string;
  status: 'pending' | 'running' | 'completed' | 'failed' | 'skipped';
  startTime?: string;
  endTime?: string;
  duration?: number;
  input?: any;
  output?: any;
  error?: ExecutionError;
  retryCount: number;
  logs: ExecutionLog[];
}

export interface ExecutionError {
  code: string;
  message: string;
  details?: any;
  stackTrace?: string;
  timestamp: string;
}

export interface ExecutionLog {
  timestamp: string;
  level: 'debug' | 'info' | 'warn' | 'error';
  message: string;
  data?: any;
}

export interface ExecutionMetrics {
  totalSteps: number;
  completedSteps: number;
  failedSteps: number;
  skippedSteps: number;
  dataProcessed: number;
  memoryUsage?: number;
  cpuUsage?: number;
}

export interface FlowSchedule {
  id: string;
  flowId: string;
  cronExpression: string;
  timezone: string;
  enabled: boolean;
  nextRun?: string;
  lastRun?: string;
  parameters?: Record<string, any>;
}

export interface FlowWebhook {
  id: string;
  flowId: string;
  url: string;
  method: 'POST' | 'PUT' | 'PATCH';
  headers?: Record<string, string>;
  authentication?: {
    type: 'none' | 'basic' | 'bearer' | 'api-key';
    credentials?: Record<string, string>;
  };
  enabled: boolean;
}