import { BusinessComponent, CreateBusinessComponentRequest, UpdateBusinessComponentRequest } from '@/types/businessComponent';
import { apiClient } from '@/lib/api-client';

class BusinessComponentService {
  async getAllBusinessComponents(): Promise<{ success: boolean; data?: BusinessComponent[]; error?: string }> {
    try {
      const data = await apiClient.get<BusinessComponent[]>('/business-components');
      return { success: true, data };
    } catch (error) {
      console.error('Failed to fetch business components:', error);
      return { success: false, error: 'Failed to fetch business components' };
    }
  }

  async getBusinessComponentById(id: string): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      const data = await apiClient.get<BusinessComponent>(`/business-components/${id}`);
      return { success: true, data };
    } catch (error) {
      console.error('Failed to fetch business component:', error);
      return { success: false, error: 'Failed to fetch business component' };
    }
  }

  async createBusinessComponent(data: CreateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      console.log('Frontend: Creating business component with data:', data);
      
      const result = await apiClient.post<BusinessComponent>('/business-components', data);
      console.log('Frontend: Created business component:', result);
      return { success: true, data: result };
    } catch (error) {
      console.error('Frontend: Failed to create business component:', error);
      return { success: false, error: 'Failed to create business component' };
    }
  }

  async updateBusinessComponent(data: UpdateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      const result = await apiClient.put<BusinessComponent>(`/business-components/${data.id}`, data);
      
      return { success: true, data: result };
    } catch (error) {
      console.error('Failed to update business component:', error);
      return { success: false, error: 'Failed to update business component' };
    }
  }

  async deleteBusinessComponent(id: string): Promise<{ success: boolean; error?: string }> {
    try {
      await apiClient.delete(`/business-components/${id}`);
      return { success: true };
    } catch (error) {
      console.error('Failed to delete business component:', error);
      return { success: false, error: 'Failed to delete business component' };
    }
  }
}

export const businessComponentService = new BusinessComponentService();