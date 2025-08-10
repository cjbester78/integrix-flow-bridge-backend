// @ts-nocheck
import { api } from './api';
import { 
  FlowExportDTO, 
  FlowExportRequestDTO, 
  FlowImportRequestDTO, 
  FlowImportResultDTO,
  FlowImportValidationDTO 
} from '@/types/export-import';

export const flowExportImportService = {
  /**
   * Export a flow to JSON file (downloads file)
   */
  exportFlowFile: async (request: FlowExportRequestDTO): Promise<Blob> => {
    const response = await api.post('/flows/export-import/export', request, {
      responseType: 'blob'
    });
    return response.data;
  },

  /**
   * Export a flow as JSON data
   */
  exportFlowJson: async (flowId: string): Promise<FlowExportDTO> => {
    const response = await api.get<FlowExportDTO>(`/flows/export-import/export/${flowId}`);
    return response.data;
  },

  /**
   * Validate if a flow can be exported
   */
  validateExport: async (flowId: string): Promise<Record<string, any>> => {
    const response = await api.get<Record<string, any>>(`/flows/export-import/export/${flowId}/validate`);
    return response.data;
  },

  /**
   * Import a flow from JSON data
   */
  importFlow: async (request: FlowImportRequestDTO): Promise<FlowImportResultDTO> => {
    const response = await api.post<FlowImportResultDTO>('/flows/export-import/import', request);
    return response.data;
  },

  /**
   * Import a flow from file
   */
  importFlowFromFile: async (
    file: File, 
    options: {
      conflictStrategy?: string;
      activateAfterImport?: boolean;
      namePrefix?: string;
      nameSuffix?: string;
    } = {}
  ): Promise<FlowImportResultDTO> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const params = new URLSearchParams();
    if (options.conflictStrategy) params.append('conflictStrategy', options.conflictStrategy);
    if (options.activateAfterImport !== undefined) params.append('activateAfterImport', String(options.activateAfterImport));
    if (options.namePrefix) params.append('namePrefix', options.namePrefix);
    if (options.nameSuffix) params.append('nameSuffix', options.nameSuffix);

    const response = await api.post<FlowImportResultDTO>(
      `/flows/export-import/import/file?${params.toString()}`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    return response.data;
  },

  /**
   * Validate an import
   */
  validateImport: async (request: FlowImportRequestDTO): Promise<FlowImportValidationDTO> => {
    const response = await api.post<FlowImportValidationDTO>('/flows/export-import/import/validate', request);
    return response.data;
  },

  /**
   * Validate import from file
   */
  validateImportFromFile: async (file: File): Promise<FlowImportValidationDTO> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<FlowImportValidationDTO>(
      '/flows/export-import/import/validate/file',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    return response.data;
  }
};

/**
 * Download a flow export file
 */
export const downloadFlowExport = async (flowId: string, flowName: string, options?: any) => {
  const request: FlowExportRequestDTO = {
    flowId,
    options: options || {}
  };

  const blob = await flowExportImportService.exportFlowFile(request);
  
  // Create download link
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  
  // Generate filename
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
  link.download = `flow_${flowName.replace(/[^a-zA-Z0-9]/g, '_')}_${timestamp}.json`;
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  // Cleanup
  window.URL.revokeObjectURL(url);
};