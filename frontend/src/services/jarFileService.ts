// @ts-nocheck
import { api, ApiResponse } from './api';
import { JarFile } from '@/types/admin';

export interface CreateJarFileRequest {
  name: string;
  version: string;
  description: string;
  fileName: string;
  driverType: string;
  vendor?: string;
  licenseInfo?: string;
  dependencies?: any;
}

export interface UpdateJarFileRequest extends Partial<CreateJarFileRequest> {
  id: string;
  isActive?: boolean;
}

export interface JarFileListResponse {
  jarFiles: JarFile[];
  total: number;
}

class JarFileService {
  // Get all JAR files
  async getAllJarFiles(filters?: {
    driverType?: string;
    isActive?: boolean;
  }): Promise<ApiResponse<JarFile[]>> {
    const params = new URLSearchParams();
    if (filters?.driverType) params.append('driverType', filters.driverType);
    if (filters?.isActive !== undefined) params.append('isActive', filters.isActive.toString());
    
    const queryString = params.toString();
    return api.get<JarFile[]>(`/jar-files${queryString ? `?${queryString}` : ''}`);
  }

  // Get JAR file by ID
  async getJarFileById(jarFileId: string): Promise<ApiResponse<JarFile>> {
    return api.get<JarFile>(`/jar-files/${jarFileId}`);
  }

  // Create new JAR file
  async createJarFile(jarFileData: CreateJarFileRequest): Promise<ApiResponse<JarFile>> {
    return api.post<JarFile>('/jar-files', jarFileData);
  }

  // Update JAR file
  async updateJarFile(jarFileId: string, updates: Partial<UpdateJarFileRequest>): Promise<ApiResponse<JarFile>> {
    return api.put<JarFile>(`/jar-files/${jarFileId}`, updates);
  }

  // Delete JAR file
  async deleteJarFile(jarFileId: string): Promise<ApiResponse<void>> {
    return api.delete(`/jar-files/${jarFileId}`);
  }

  // Upload JAR file
  async uploadJarFile(file: File, metadata: {
    name: string;
    version: string;
    description: string;
    driverType: string;
    vendor?: string;
  }): Promise<ApiResponse<JarFile>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', metadata.name);
    formData.append('version', metadata.version);
    formData.append('description', metadata.description);
    formData.append('driverType', metadata.driverType);
    if (metadata.vendor) formData.append('vendor', metadata.vendor);

    return api.post<JarFile>('/jar-files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }

  // Download JAR file
  async downloadJarFile(jarFileId: string): Promise<Blob> {
    const response = await fetch(`${api.baseURL}/jar-files/${jarFileId}/download`, {
      method: 'GET',
      headers: api.getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to download JAR file');
    }
    
    return response.blob();
  }

  // Activate/Deactivate JAR file
  async toggleJarFileStatus(jarFileId: string, isActive: boolean): Promise<ApiResponse<JarFile>> {
    return api.put<JarFile>(`/jar-files/${jarFileId}/status`, { isActive });
  }

  // Get JAR files by driver type
  async getJarFilesByDriverType(driverType: string): Promise<ApiResponse<JarFile[]>> {
    return api.get<JarFile[]>(`/jar-files/driver-type/${driverType}`);
  }

  // Validate JAR file
  async validateJarFile(jarFileId: string): Promise<ApiResponse<{
    valid: boolean;
    errors: string[];
    warnings: string[];
    metadata?: {
      mainClass?: string;
      packages?: string[];
      requiredDependencies?: string[];
    };
  }>> {
    return api.get(`/jar-files/${jarFileId}/validate`);
  }

  // Get driver types
  async getDriverTypes(): Promise<ApiResponse<string[]>> {
    return api.get<string[]>('/jar-files/driver-types');
  }
}

export const jarFileService = new JarFileService();