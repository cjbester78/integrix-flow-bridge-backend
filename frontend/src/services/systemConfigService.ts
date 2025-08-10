// @ts-nocheck
import { api } from './api';

export interface EnvironmentInfo {
  type: 'DEVELOPMENT' | 'QUALITY_ASSURANCE' | 'PRODUCTION';
  displayName: string;
  description: string;
  enforceRestrictions: boolean;
  permissions: EnvironmentPermissions;
}

export interface EnvironmentPermissions {
  canCreateFlows: boolean;
  canCreateAdapters: boolean;
  canModifyAdapterConfig: boolean;
  canImportFlows: boolean;
  canDeployFlows: boolean;
  canCreateBusinessComponents: boolean;
  canCreateDataStructures: boolean;
  isAdmin: boolean;
  canAccessAdmin: boolean;
}

export interface UIVisibility {
  element: string;
  visible: boolean;
}

export const systemConfigService = {
  /**
   * Get current environment configuration
   */
  getEnvironmentConfig: async (): Promise<EnvironmentInfo> => {
    try {
      const response = await api.get<EnvironmentInfo>('/system/config/environment');
      // Validate the response has the expected structure
      if (response.data && typeof response.data === 'object' && 'type' in response.data) {
        return response.data;
      }
      // Return default if response is invalid
      console.warn('Invalid environment config response, using defaults');
      return {
        type: 'DEVELOPMENT',
        displayName: 'Development',
        description: 'Development Environment',
        enforceRestrictions: false,
        permissions: {
          canCreateFlows: true,
          canCreateAdapters: true,
          canModifyAdapterConfig: true,
          canImportFlows: true,
          canDeployFlows: true,
          canCreateBusinessComponents: true,
          canCreateDataStructures: true,
          isAdmin: false,
          canAccessAdmin: false
        }
      };
    } catch (error) {
      console.error('Error fetching environment config:', error);
      // Return default on error
      return {
        type: 'DEVELOPMENT',
        displayName: 'Development',
        description: 'Development Environment',
        enforceRestrictions: false,
        permissions: {
          canCreateFlows: true,
          canCreateAdapters: true,
          canModifyAdapterConfig: true,
          canImportFlows: true,
          canDeployFlows: true,
          canCreateBusinessComponents: true,
          canCreateDataStructures: true,
          isAdmin: false,
          canAccessAdmin: false
        }
      };
    }
  },

  /**
   * Get current user permissions
   */
  getPermissions: async (): Promise<EnvironmentPermissions> => {
    try {
      const response = await api.get<EnvironmentPermissions>('/system/config/permissions');
      // Validate the response has the expected structure
      if (response.data && typeof response.data === 'object') {
        return response.data;
      }
      // Return default if response is invalid
      console.warn('Invalid permissions response, using defaults');
      return {
        canCreateFlows: true,
        canCreateAdapters: true,
        canModifyAdapterConfig: true,
        canImportFlows: true,
        canDeployFlows: true,
        canCreateBusinessComponents: true,
        canCreateDataStructures: true,
        isAdmin: false,
        canAccessAdmin: false
      };
    } catch (error) {
      console.error('Error fetching permissions:', error);
      // Return default on error
      return {
        canCreateFlows: true,
        canCreateAdapters: true,
        canModifyAdapterConfig: true,
        canImportFlows: true,
        canDeployFlows: true,
        canCreateBusinessComponents: true,
        canCreateDataStructures: true,
        isAdmin: false,
        canAccessAdmin: false
      };
    }
  },

  /**
   * Update environment type (admin only)
   */
  updateEnvironmentType: async (environmentType: string): Promise<EnvironmentInfo> => {
    const response = await api.put<EnvironmentInfo>('/system/config/environment', {
      environmentType
    });
    return response.data;
  },

  /**
   * Check if a specific action is allowed
   */
  checkPermission: async (action: string): Promise<boolean> => {
    const response = await api.get<{ allowed: boolean }>('/system/config/check-permission', {
      params: { action }
    });
    return response.data.allowed;
  },

  /**
   * Check if UI element should be visible
   */
  checkUIVisibility: async (element: string): Promise<boolean> => {
    const response = await api.get<UIVisibility>('/system/config/ui-visibility', {
      params: { element }
    });
    return response.data.visible;
  }
};