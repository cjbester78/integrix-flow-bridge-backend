import { api, ApiResponse } from './api';

export interface IntegrationFlow {
  id?: string;
  name: string;
  description?: string;
  sourceAdapterId: string;
  targetAdapterId: string;
  sourceStructureId?: string;
  targetStructureId?: string;
  transformations: FlowTransformation[];
  status: 'draft' | 'active' | 'inactive' | 'error';
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface FlowTransformation {
  id?: string;
  flowId?: string;
  type: 'field-mapping' | 'custom-function' | 'filter' | 'enrichment';
  configuration: any;
  order: number;
}

export interface FieldMapping {
  id?: string;
  transformationId?: string;
  sourceFields: string[];
  targetField: string;
  javaFunction?: string;
  mappingRule?: string;
}

export interface FlowTestResult {
  success: boolean;
  executionTime: number;
  inputData: any;
  outputData?: any;
  errors?: string[];
  warnings?: string[];
}

class FlowService {
  // Create a new integration flow
  async createFlow(flow: Omit<IntegrationFlow, 'id' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<IntegrationFlow>> {
    return api.post<IntegrationFlow>('/flows', flow);
  }

  // Get all flows with optional filtering
  async getFlows(params?: {
    status?: string;
    sourceAdapter?: string;
    targetAdapter?: string;
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<{ flows: IntegrationFlow[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/flows${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific flow by ID
  async getFlow(id: string): Promise<ApiResponse<IntegrationFlow>> {
    return api.get<IntegrationFlow>(`/flows/${id}`);
  }

  // Update an existing flow
  async updateFlow(id: string, updates: Partial<IntegrationFlow>): Promise<ApiResponse<IntegrationFlow>> {
    return api.put<IntegrationFlow>(`/flows/${id}`, updates);
  }

  // Delete a flow
  async deleteFlow(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/flows/${id}`);
  }

  // Test a flow configuration
  async testFlow(flowId: string, testData?: any): Promise<ApiResponse<FlowTestResult>> {
    return api.post<FlowTestResult>(`/flows/${flowId}/test`, { testData });
  }

  // Start/Stop flow execution
  async updateFlowStatus(id: string, status: 'active' | 'inactive'): Promise<ApiResponse<IntegrationFlow>> {
    return api.patch<IntegrationFlow>(`/flows/${id}/status`, { status });
  }

  // Get flow execution history
  async getFlowExecutions(flowId: string, params?: {
    startDate?: string;
    endDate?: string;
    status?: 'success' | 'error' | 'warning';
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<any[]>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/flows/${flowId}/executions${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Save field mappings for a flow
  async saveFieldMappings(flowId: string, mappings: FieldMapping[]): Promise<ApiResponse<FieldMapping[]>> {
    return api.post<FieldMapping[]>(`/flows/${flowId}/field-mappings`, { mappings });
  }

  // Clone an existing flow
  async cloneFlow(id: string, newName: string): Promise<ApiResponse<IntegrationFlow>> {
    return api.post<IntegrationFlow>(`/flows/${id}/clone`, { name: newName });
  }

  // Validate flow configuration
  async validateFlow(flow: Partial<IntegrationFlow>): Promise<ApiResponse<{ valid: boolean; errors: string[] }>> {
    return api.post('/flows/validate', flow);
  }
}

export const flowService = new FlowService();