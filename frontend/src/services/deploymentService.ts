import { api } from './api';
import { DeploymentInfo } from '@/types/deployment';

export interface DeploymentResponse {
  success: boolean;
  data?: DeploymentInfo;
  error?: string;
}

class DeploymentService {
  async deployFlow(flowId: string): Promise<DeploymentResponse> {
    try {
      const response = await api.post<DeploymentInfo>(`/flows/${flowId}/deployment/deploy`);
      return response;
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to deploy flow'
      };
    }
  }

  async undeployFlow(flowId: string): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await api.post(`/flows/${flowId}/deployment/undeploy`);
      return { success: response.success };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to undeploy flow'
      };
    }
  }

  async getDeploymentInfo(flowId: string): Promise<DeploymentResponse> {
    try {
      const response = await api.get<DeploymentInfo>(`/flows/${flowId}/deployment`);
      return response;
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch deployment info'
      };
    }
  }
}

export const deploymentService = new DeploymentService();