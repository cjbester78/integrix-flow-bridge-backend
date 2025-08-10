import { AdapterApi } from './adapterApi';
import { AdapterTesting } from './adapterTesting';
import { AdapterMonitoring } from './adapterMonitoring';
import { AdapterTypes } from './adapterTypes';
import { adapterValidationService } from './adapterValidation';

class AdapterService {
  private api: AdapterApi;
  private testing: AdapterTesting;
  private monitoring: AdapterMonitoring;
  private types: AdapterTypes;

  constructor() {
    this.api = new AdapterApi();
    this.testing = new AdapterTesting();
    this.monitoring = new AdapterMonitoring();
    this.types = new AdapterTypes();
  }

  // API methods
  async createAdapter(...args: Parameters<AdapterApi['createAdapter']>) {
    return this.api.createAdapter(...args);
  }

  async getAdapters(...args: Parameters<AdapterApi['getAdapters']>) {
    return this.api.getAdapters(...args);
  }

  async getAdapter(...args: Parameters<AdapterApi['getAdapter']>) {
    return this.api.getAdapter(...args);
  }

  async updateAdapter(...args: Parameters<AdapterApi['updateAdapter']>) {
    return this.api.updateAdapter(...args);
  }

  async deleteAdapter(...args: Parameters<AdapterApi['deleteAdapter']>) {
    return this.api.deleteAdapter(...args);
  }

  async cloneAdapter(...args: Parameters<AdapterApi['cloneAdapter']>) {
    return this.api.cloneAdapter(...args);
  }

  // Testing methods
  async testAdapter(...args: Parameters<AdapterTesting['testAdapter']>) {
    return this.testing.testAdapter(...args);
  }

  async testAdapterConfiguration(...args: Parameters<AdapterTesting['testAdapterConfiguration']>) {
    return this.testing.testAdapterConfiguration(...args);
  }

  async validateAdapterConfig(...args: Parameters<AdapterTesting['validateAdapterConfig']>) {
    return this.testing.validateAdapterConfig(...args);
  }

  // Monitoring methods
  async getAdapterStats(...args: Parameters<AdapterMonitoring['getAdapterStats']>) {
    return this.monitoring.getAdapterStats(...args);
  }

  async getAdapterLogs(...args: Parameters<AdapterMonitoring['getAdapterLogs']>) {
    return this.monitoring.getAdapterLogs(...args);
  }

  // Types methods
  async getAdapterTypes(...args: Parameters<AdapterTypes['getAdapterTypes']>) {
    return this.types.getAdapterTypes(...args);
  }

  // Validation methods
  async validateAdapter(config: any) {
    return adapterValidationService.validateAdapter(config);
  }
}

export const adapterService = new AdapterService();

// Re-export types for convenience
export * from '@/types/adapter';