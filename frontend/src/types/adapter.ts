export interface CommunicationAdapter {
  id?: string;
  name: string;
  type: 'rest' | 'soap' | 'file' | 'database' | 'sap' | 'salesforce' | 'email' | 'sms' | 'jms' | 'odata' | 'rfc' | 'mail';
  mode: 'inbound' | 'outbound' | 'bidirectional';
  description?: string;
  configuration: AdapterConfiguration;
  status: 'active' | 'inactive' | 'error' | 'testing';
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface AdapterConfiguration {
  // REST API Configuration
  baseUrl?: string;
  authentication?: {
    type: 'none' | 'basic' | 'bearer' | 'api-key' | 'oauth2';
    credentials?: any;
  };
  headers?: { [key: string]: string };
  timeout?: number;
  
  // SOAP Configuration
  wsdlUrl?: string;
  soapAction?: string;
  namespace?: string;
  
  // SOAP Sender Configuration
  sender?: string;
  senderAddress?: string;
  senderWsdlUrl?: string;
  senderAuthorization?: string;
  senderUserRole?: string;
  bodySizeMB?: number;
  attachmentsSizeMB?: number;
  
  // SOAP Receiver Configuration
  receiver?: string;
  receiverAddress?: string;
  receiverWsdlUrl?: string;
  service?: string;
  endpoint?: string;
  operationName?: string;
  proxyType?: string;
  receiverAuthentication?: string;
  credentialName?: string;
  receiverTimeout?: number;
  keepAlive?: boolean;
  compressMessage?: boolean;
  allowChunking?: boolean;
  returnHttpResponseCodeAsHeader?: boolean;
  cleanupRequestHeaders?: boolean;
  sapRmMessageIdDetermination?: string;
  
  // SOAP More/Advanced Configuration
  parameterType?: string;
  allowHeader?: string;
  httpSessionReuse?: string;
  returnExceptionToSender?: boolean;
  
  // File Configuration
  directory?: string;
  filePattern?: string;
  encoding?: string;
  
  // Database Configuration
  connectionString?: string;
  driver?: string;
  schema?: string;
  
  // Email Configuration
  smtpHost?: string;
  smtpPort?: number;
  encryption?: 'none' | 'ssl' | 'tls';
  
  // JMS Configuration
  connectionFactoryClass?: string;
  queueClass?: string;
  queueManager?: string;
  host?: string;
  port?: number;
  channel?: string;
  connectionFactory?: string;
  queueName?: string;
  transportType?: 'CLIENT' | 'BINDINGS';
  username?: string;
  password?: string;
  useSSL?: boolean;
  sslKeystore?: string;
  sslPassword?: string;
  sslCertificateId?: string;
  destinationType?: 'Queue' | 'Topic';
  messageSelector?: string;
  clientId?: string;
  ackMode?: 'AUTO_ACKNOWLEDGE' | 'CLIENT_ACKNOWLEDGE';
  
  // OData Configuration
  entitySetName?: string;
  apiVersion?: string;
  queryOptions?: string;
  dataMappingRules?: string;
  dataValidationRules?: string;
  errorHandling?: string;
  loggingLevel?: string;
  wsSecurityPolicies?: string;
  wsSecurityPolicyType?: string;
  
  // RFC Configuration
  sapSystemId?: string;
  sapClientNumber?: string;
  sapSystemNumber?: string;
  sapApplicationServerHost?: string;
  sapGatewayHost?: string;
  sapGatewayService?: string;
  portNumber?: string;
  connectionType?: string;
  sapUser?: string;
  sapPassword?: string;
  rfcDestinationName?: string;
  listenerServiceName?: string;
  callerServiceName?: string;
  validationRules?: string;
  
  // Mail Configuration
  mailServerHost?: string;
  mailServerPort?: string;
  mailProtocol?: string;
  mailUsername?: string;
  mailPassword?: string;
  useSSLTLS?: boolean;
  folderName?: string;
  pollingInterval?: string;
  searchCriteria?: string;
  maxMessages?: string;
  contentHandling?: string;
  mailEncoding?: string;
  deleteAfterFetch?: boolean;
  
  // SMTP Configuration
  smtpServerHost?: string;
  smtpServerPort?: string;
  smtpUseSSLTLS?: boolean;
  smtpUsername?: string;
  smtpPassword?: string;
  connectionTimeout?: string;
  readTimeout?: string;
  fromAddress?: string;
  toAddresses?: string;
  ccAddresses?: string;
  bccAddresses?: string;
  emailSubject?: string;
  emailBody?: string;
  emailAttachments?: string;
  emailEncoding?: string;
  
  // Certificate Configuration
  certificateId?: string;
  keystoreAlias?: string;
  keystorePassword?: string;
  certificateAlias?: string;
  verifyServerCertificate?: string;
  
  // Custom properties
  properties?: { [key: string]: any };
}

export interface AdapterTestResult {
  success: boolean;
  responseTime: number;
  statusCode?: number;
  message: string;
  details?: any;
  errors?: string[];
}

export interface AdapterStats {
  totalMessages: number;
  successfulMessages: number;
  failedMessages: number;
  averageResponseTime: number;
  uptime: number;
  lastExecution?: string;
}

export interface AdapterValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export interface AdapterType {
  type: string;
  name: string;
  description: string;
  configurationSchema: any;
  supportedModes: string[];
}

export interface AdapterFilters {
  type?: string;
  mode?: string;
  status?: string;
  page?: number;
  limit?: number;
}

export interface AdapterLogParams {
  level?: 'info' | 'warn' | 'error';
  startDate?: string;
  endDate?: string;
  page?: number;
  limit?: number;
}