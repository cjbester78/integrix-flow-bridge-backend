export interface DeploymentInfo {
  flowId: string;
  endpoint: string;
  deployedAt: string;
  deployedBy?: string;
  metadata?: {
    flowName?: string;
    adapterType?: string;
    adapterMode?: string;
    wsdlUrl?: string;
    apiDocsUrl?: string;
    openApiUrl?: string;
    httpMethods?: string;
    contentType?: string;
    pollingEnabled?: boolean;
    filePattern?: string;
    [key: string]: any;
  };
}