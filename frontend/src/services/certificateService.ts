// @ts-nocheck
import { api, ApiResponse } from './api';
import { Certificate } from '@/types/admin';

export interface CreateCertificateRequest {
  name: string;
  type: string;
  issuer: string;
  validFrom: string;
  validTo: string;
  usage: string;
  content?: string;
  privateKey?: string;
  passwordHint?: string;
}

export interface UpdateCertificateRequest extends Partial<CreateCertificateRequest> {
  id: string;
}

export interface CertificateListResponse {
  certificates: Certificate[];
  total: number;
}

class CertificateService {
  // Get all certificates
  async getAllCertificates(businessComponentId?: string): Promise<ApiResponse<Certificate[]>> {
    const params = businessComponentId ? `?businessComponentId=${businessComponentId}` : '';
    return api.get<Certificate[]>(`/certificates${params}`);
  }

  // Get certificate by ID
  async getCertificateById(certificateId: string): Promise<ApiResponse<Certificate>> {
    return api.get<Certificate>(`/certificates/${certificateId}`);
  }

  // Create new certificate
  async createCertificate(certificateData: CreateCertificateRequest): Promise<ApiResponse<Certificate>> {
    return api.post<Certificate>('/certificates', certificateData);
  }

  // Update certificate
  async updateCertificate(certificateId: string, updates: Partial<CreateCertificateRequest>): Promise<ApiResponse<Certificate>> {
    return api.put<Certificate>(`/certificates/${certificateId}`, updates);
  }

  // Delete certificate
  async deleteCertificate(certificateId: string): Promise<ApiResponse<void>> {
    return api.delete(`/certificates/${certificateId}`);
  }

  // Upload certificate file
  async uploadCertificate(file: File, metadata: {
    name: string;
    type: string;
    usage: string;
  }): Promise<ApiResponse<Certificate>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', metadata.name);
    formData.append('type', metadata.type);
    formData.append('usage', metadata.usage);

    return api.post<Certificate>('/certificates/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }

  // Download certificate
  async downloadCertificate(certificateId: string): Promise<Blob> {
    const response = await fetch(`${api.baseURL}/certificates/${certificateId}/download`, {
      method: 'GET',
      headers: api.getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to download certificate');
    }
    
    return response.blob();
  }

  // Get certificates by business component
  async getCertificatesByBusinessComponent(businessComponentId: string): Promise<ApiResponse<Certificate[]>> {
    return api.get<Certificate[]>(`/business-components/${businessComponentId}/certificates`);
  }

  // Validate certificate
  async validateCertificate(certificateId: string): Promise<ApiResponse<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }>> {
    return api.get(`/certificates/${certificateId}/validate`);
  }

  // Get expiring certificates
  async getExpiringCertificates(days: number = 30): Promise<ApiResponse<Certificate[]>> {
    return api.get<Certificate[]>(`/certificates/expiring?days=${days}`);
  }
}

export const certificateService = new CertificateService();