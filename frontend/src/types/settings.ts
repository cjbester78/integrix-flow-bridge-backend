/**
 * System Settings Types
 */

export interface SystemSettings {
  general: GeneralSettings;
  security: SecuritySettings;
  integration: IntegrationSettings;
  performance: PerformanceSettings;
  notifications: NotificationSettings;
  monitoring: MonitoringSettings;
  backup: BackupSettings;
  certificates: CertificateSettings;
}

export interface GeneralSettings {
  systemName: string;
  systemDescription: string;
  timezone: string;
  dateFormat: string;
  timeFormat: string;
  language: string;
  defaultCurrency: string;
  maintenanceMode: boolean;
  maintenanceMessage?: string;
  debugMode: boolean;
  logLevel: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
}

export interface SecuritySettings {
  sessionTimeout: number; // minutes
  maxLoginAttempts: number;
  lockoutDuration: number; // minutes
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireLowercase: boolean;
  passwordRequireNumbers: boolean;
  passwordRequireSpecialChars: boolean;
  passwordExpiryDays: number;
  mfaEnabled: boolean;
  mfaProvider: 'totp' | 'sms' | 'email';
  ipWhitelist: string[];
  corsEnabled: boolean;
  corsAllowedOrigins: string[];
  jwtExpiryMinutes: number;
  refreshTokenExpiryDays: number;
  apiRateLimitPerMinute: number;
  encryptionAlgorithm: string;
}

export interface IntegrationSettings {
  maxConcurrentFlows: number;
  defaultTimeout: number; // seconds
  retryAttempts: number;
  retryDelay: number; // milliseconds
  maxMessageSize: number; // MB
  enableMessageCompression: boolean;
  compressionLevel: number;
  deadLetterQueueEnabled: boolean;
  messageRetentionDays: number;
  enableTransactionLog: boolean;
  transactionLogRetentionDays: number;
  defaultBatchSize: number;
  enableCircuitBreaker: boolean;
  circuitBreakerThreshold: number;
  circuitBreakerTimeout: number;
}

export interface PerformanceSettings {
  connectionPoolSize: number;
  connectionTimeout: number; // seconds
  queryTimeout: number; // seconds
  cacheEnabled: boolean;
  cacheSize: number; // MB
  cacheTTL: number; // minutes
  enableQueryOptimization: boolean;
  threadPoolSize: number;
  maxHeapSize: number; // MB
  gcStrategy: 'G1GC' | 'ZGC' | 'ParallelGC';
  enableJMX: boolean;
  jmxPort: number;
  enableMetrics: boolean;
  metricsInterval: number; // seconds
}

export interface NotificationSettings {
  emailEnabled: boolean;
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword: string;
  smtpTls: boolean;
  emailFrom: string;
  emailReplyTo: string;
  notifyOnFlowFailure: boolean;
  notifyOnSystemError: boolean;
  notifyOnHighLoad: boolean;
  notifyOnCertificateExpiry: boolean;
  certificateExpiryWarningDays: number;
  dailyReportEnabled: boolean;
  dailyReportTime: string;
  weeklyReportEnabled: boolean;
  weeklyReportDay: string;
  monthlyReportEnabled: boolean;
  monthlyReportDay: number;
  slackEnabled: boolean;
  slackWebhookUrl: string;
  teamsEnabled: boolean;
  teamsWebhookUrl: string;
}

export interface MonitoringSettings {
  metricsEnabled: boolean;
  metricsRetentionDays: number;
  tracingEnabled: boolean;
  traceSampleRate: number;
  loggingEnabled: boolean;
  logRetentionDays: number;
  auditLogEnabled: boolean;
  auditLogRetentionDays: number;
  healthCheckInterval: number; // seconds
  healthCheckTimeout: number; // seconds
  alertingEnabled: boolean;
  cpuAlertThreshold: number; // percentage
  memoryAlertThreshold: number; // percentage
  diskAlertThreshold: number; // percentage
  errorRateThreshold: number; // percentage
  latencyThreshold: number; // milliseconds
  prometheusEnabled: boolean;
  prometheusPort: number;
  grafanaEnabled: boolean;
  grafanaUrl: string;
}

export interface BackupSettings {
  autoBackupEnabled: boolean;
  backupSchedule: string; // cron expression
  backupRetentionDays: number;
  backupLocation: 'local' | 's3' | 'azure' | 'gcs';
  backupPath: string;
  backupCompression: boolean;
  backupEncryption: boolean;
  backupNotification: boolean;
  includeFlows: boolean;
  includeAdapters: boolean;
  includeMessages: boolean;
  includeLogs: boolean;
  includeCertificates: boolean;
  s3BucketName?: string;
  s3Region?: string;
  azureContainerName?: string;
  gcsContainerName?: string;
}

export interface CertificateSettings {
  certificateStorePath: string;
  autoRenewalEnabled: boolean;
  renewalThresholdDays: number;
  certificateValidationEnabled: boolean;
  trustedCAs: string[];
  clientCertificateRequired: boolean;
  certificateRevocationCheckEnabled: boolean;
  ocspEnabled: boolean;
  ocspResponderUrl: string;
}