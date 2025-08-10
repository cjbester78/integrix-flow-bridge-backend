import { api, ApiResponse } from '../api';
import { AdapterType } from '@/types/adapter';

export interface CreateAdapterTypeRequest {
  name: string;
  category: 'sender' | 'receiver';
  description?: string;
  configuration: any;
  version?: string;
  isActive?: boolean;
}

export interface UpdateAdapterTypeRequest extends Partial<CreateAdapterTypeRequest> {
  id: string;
}

export interface AdapterTypeListResponse {
  adapterTypes: AdapterType[];
  total: number;
  page: number;
  limit: number;
}

export class AdapterTypes {
  // Get available adapter types and their configurations
  async getAdapterTypes(params?: {
    category?: string;
    isActive?: boolean;
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<AdapterTypeListResponse>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/adapters/types${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific adapter type by ID
  async getAdapterType(id: string): Promise<ApiResponse<AdapterType>> {
    return api.get<AdapterType>(`/adapters/types/${id}`);
  }

  // Create new adapter type
  async createAdapterType(adapterTypeData: CreateAdapterTypeRequest): Promise<ApiResponse<AdapterType>> {
    return api.post<AdapterType>('/adapters/types', adapterTypeData);
  }

  // Update adapter type
  async updateAdapterType(id: string, updates: Partial<CreateAdapterTypeRequest>): Promise<ApiResponse<AdapterType>> {
    return api.put<AdapterType>(`/adapters/types/${id}`, updates);
  }

  // Delete adapter type
  async deleteAdapterType(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/adapters/types/${id}`);
  }

  // Clone an existing adapter type
  async cloneAdapterType(id: string, newName: string): Promise<ApiResponse<AdapterType>> {
    return api.post<AdapterType>(`/adapters/types/${id}/clone`, { name: newName });
  }

  // Get adapter types by category
  async getAdapterTypesByCategory(category: 'sender' | 'receiver'): Promise<ApiResponse<AdapterType[]>> {
    return api.get<AdapterType[]>(`/adapters/types?category=${category}`);
  }

  // Update adapter type status
  async updateAdapterTypeStatus(id: string, isActive: boolean): Promise<ApiResponse<AdapterType>> {
    return api.patch<AdapterType>(`/adapters/types/${id}/status`, { isActive });
  }

  // Validate adapter type configuration
  async validateAdapterTypeConfiguration(configuration: any, category: string): Promise<ApiResponse<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }>> {
    return api.post('/adapters/types/validate', { configuration, category });
  }

  // Search adapter types
  async searchAdapterTypes(query: string, filters?: {
    category?: string;
    isActive?: boolean;
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<AdapterTypeListResponse>> {
    const params = new URLSearchParams({
      q: query,
      ...(filters?.category && { category: filters.category }),
      ...(filters?.isActive !== undefined && { isActive: filters.isActive.toString() }),
      page: (filters?.page || 1).toString(),
      limit: (filters?.limit || 50).toString()
    });

    return api.get<AdapterTypeListResponse>(`/adapters/types/search?${params}`);
  }

  // Get adapter type usage statistics
  async getAdapterTypeUsage(id: string): Promise<ApiResponse<{
    totalAdapters: number;
    activeAdapters: number;
    adapterIds: string[];
  }>> {
    return api.get(`/adapters/types/${id}/usage`);
  }
}