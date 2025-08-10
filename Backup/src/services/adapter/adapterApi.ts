import { api, ApiResponse } from '../api';
import { 
  CommunicationAdapter, 
  AdapterTestResult, 
  AdapterStats, 
  AdapterValidationResult, 
  AdapterType,
  AdapterFilters,
  AdapterLogParams 
} from '@/types/adapter';

export class AdapterApi {
  // Create a new communication adapter
  async createAdapter(adapter: Omit<CommunicationAdapter, 'id' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<CommunicationAdapter>> {
    return api.post<CommunicationAdapter>('/adapters', adapter);
  }

  // Get all adapters with optional filtering
  async getAdapters(params?: AdapterFilters): Promise<ApiResponse<{ adapters: CommunicationAdapter[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/adapters${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific adapter by ID
  async getAdapter(id: string): Promise<ApiResponse<CommunicationAdapter>> {
    return api.get<CommunicationAdapter>(`/adapters/${id}`);
  }

  // Update an existing adapter
  async updateAdapter(id: string, updates: Partial<CommunicationAdapter>): Promise<ApiResponse<CommunicationAdapter>> {
    return api.put<CommunicationAdapter>(`/adapters/${id}`, updates);
  }

  // Delete an adapter
  async deleteAdapter(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/adapters/${id}`);
  }

  // Clone an existing adapter
  async cloneAdapter(id: string, newName: string): Promise<ApiResponse<CommunicationAdapter>> {
    return api.post<CommunicationAdapter>(`/adapters/${id}/clone`, { name: newName });
  }
}