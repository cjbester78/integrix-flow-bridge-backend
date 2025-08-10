import { api } from './api';
import { FieldNode } from '@/components/fieldMapping/types';

export interface WebserviceStructure {
  id: string;
  name: string;
  type: string;
  path: string;
  expanded?: boolean;
  children?: WebserviceStructure[];
}

export interface WebserviceFile {
  id: string;
  name: string;
  uploadDate: string;
  size: string;
  businessComponentId: string;
}

class WebserviceService {
  async getWebserviceFiles(businessComponentId?: string): Promise<{ success: boolean; data?: WebserviceFile[]; error?: string }> {
    try {
      const endpoint = businessComponentId ? `/webservices?businessComponentId=${businessComponentId}` : '/webservices';
      return await api.get<WebserviceFile[]>(endpoint);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch webservice files'
      };
    }
  }

  async getWebserviceStructure(filename: string): Promise<{ success: boolean; data?: FieldNode[]; error?: string }> {
    try {
      return await api.get<FieldNode[]>(`/webservices/${encodeURIComponent(filename)}/structure`);
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to fetch webservice structure'
      };
    }
  }

  async uploadWebservice(file: File, businessComponentId: string): Promise<{ success: boolean; data?: WebserviceFile; error?: string }> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('businessComponentId', businessComponentId);

      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/webservices/upload`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      return {
        success: true,
        data: data.data || data,
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to upload webservice'
      };
    }
  }

  async deleteWebservice(filename: string): Promise<{ success: boolean; error?: string }> {
    try {
      await api.delete(`/webservices/${encodeURIComponent(filename)}`);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Failed to delete webservice'
      };
    }
  }
}

export const webserviceService = new WebserviceService();