import { api, ApiResponse } from '../api';
import { AdapterTestResult, AdapterConfiguration, AdapterValidationResult } from '@/types/adapter';

export class AdapterTesting {
  // Test adapter connection and configuration
  async testAdapter(adapterId: string, testData?: any): Promise<ApiResponse<AdapterTestResult>> {
    return api.post<AdapterTestResult>(`/adapters/${adapterId}/test`, { testData });
  }

  // Test adapter configuration without saving
  async testAdapterConfiguration(config: AdapterConfiguration, type: string): Promise<ApiResponse<AdapterTestResult>> {
    return api.post<AdapterTestResult>('/adapters/test-config', { configuration: config, type });
  }

  // Validate adapter configuration
  async validateAdapterConfig(type: string, configuration: AdapterConfiguration): Promise<ApiResponse<AdapterValidationResult>> {
    return api.post('/adapters/validate-config', { type, configuration });
  }
}