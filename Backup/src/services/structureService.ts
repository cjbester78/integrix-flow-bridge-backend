import { api, ApiResponse } from './api';
import { DataStructure } from '@/types/dataStructures';

export interface DataStructureCreate {
  name: string;
  type: 'json' | 'xsd' | 'wsdl' | 'custom';
  description?: string;
  usage: 'source' | 'target' | 'both';
  structure: any;
  namespace?: {
    uri: string;
    prefix?: string;
    schemaLocation: string;
  };
  tags?: string[];
}

export interface StructureValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
  suggestions?: string[];
}

export interface StructureUsageInfo {
  totalFlows: number;
  activeFlows: number;
  flowIds: string[];
}

class StructureService {
  // Create a new data structure
  async createStructure(structure: DataStructureCreate): Promise<ApiResponse<DataStructure>> {
    return api.post<DataStructure>('/structures', structure);
  }

  // Get all data structures with optional filtering
  async getStructures(params?: {
    type?: string;
    usage?: string;
    search?: string;
    tags?: string[];
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<{ structures: DataStructure[]; total: number }>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          if (Array.isArray(value)) {
            value.forEach(v => queryParams.append(key, v.toString()));
          } else {
            queryParams.append(key, value.toString());
          }
        }
      });
    }
    
    const endpoint = `/structures${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return api.get(endpoint);
  }

  // Get a specific structure by ID
  async getStructure(id: string): Promise<ApiResponse<DataStructure>> {
    return api.get<DataStructure>(`/structures/${id}`);
  }

  // Update an existing structure
  async updateStructure(id: string, updates: Partial<DataStructureCreate>): Promise<ApiResponse<DataStructure>> {
    return api.put<DataStructure>(`/structures/${id}`, updates);
  }

  // Delete a structure
  async deleteStructure(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/structures/${id}`);
  }

  // Validate structure definition
  async validateStructure(structure: any, type: string): Promise<ApiResponse<StructureValidationResult>> {
    return api.post<StructureValidationResult>('/structures/validate', { structure, type });
  }

  // Parse and import structure from various formats
  async parseStructure(content: string, type: 'json' | 'xsd' | 'wsdl'): Promise<ApiResponse<{
    structure: any;
    metadata: {
      fields: number;
      complexity: 'simple' | 'medium' | 'complex';
      namespace?: string;
    };
  }>> {
    return api.post('/structures/parse', { content, type });
  }

  // Generate sample data from structure
  async generateSampleData(id: string, format?: 'json' | 'xml'): Promise<ApiResponse<any>> {
    const endpoint = `/structures/${id}/sample${format ? `?format=${format}` : ''}`;
    return api.get(endpoint);
  }

  // Get structure usage information
  async getStructureUsage(id: string): Promise<ApiResponse<StructureUsageInfo>> {
    return api.get<StructureUsageInfo>(`/structures/${id}/usage`);
  }

  // Clone an existing structure
  async cloneStructure(id: string, newName: string): Promise<ApiResponse<DataStructure>> {
    return api.post<DataStructure>(`/structures/${id}/clone`, { name: newName });
  }

  // Compare two structures
  async compareStructures(sourceId: string, targetId: string): Promise<ApiResponse<{
    compatibility: 'compatible' | 'partially_compatible' | 'incompatible';
    score: number;
    differences: Array<{
      field: string;
      sourceType?: string;
      targetType?: string;
      issue: string;
    }>;
    suggestions: Array<{
      field: string;
      suggestion: string;
      action: 'map' | 'transform' | 'ignore';
    }>;
  }>> {
    return api.post('/structures/compare', { sourceId, targetId });
  }

  // Get structure transformations/mappings
  async getStructureMappings(sourceId: string, targetId: string): Promise<ApiResponse<Array<{
    sourceField: string;
    targetField: string;
    confidence: number;
    transformationSuggestion?: string;
  }>>> {
    return api.get(`/structures/mappings?source=${sourceId}&target=${targetId}`);
  }

  // Export structure in different formats
  async exportStructure(id: string, format: 'json' | 'xsd' | 'yaml' | 'typescript'): Promise<ApiResponse<{
    content: string;
    filename: string;
    mimeType: string;
  }>> {
    return api.get(`/structures/${id}/export?format=${format}`);
  }

  // Search structures by content/fields
  async searchStructures(query: string, filters?: {
    type?: string;
    usage?: string;
    hasField?: string;
  }): Promise<ApiResponse<DataStructure[]>> {
    const queryParams = new URLSearchParams({ q: query });
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    return api.get(`/structures/search?${queryParams.toString()}`);
  }

  // Get structure version history
  async getStructureVersions(id: string): Promise<ApiResponse<Array<{
    version: number;
    createdAt: string;
    createdBy: string;
    changes: string;
    structure: any;
  }>>> {
    return api.get(`/structures/${id}/versions`);
  }
}

export const structureService = new StructureService();