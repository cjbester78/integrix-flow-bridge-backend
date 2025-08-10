// @ts-nocheck
import { useState, useEffect } from 'react';
import { systemConfigService, EnvironmentInfo, EnvironmentPermissions } from '@/services/systemConfigService';

/**
 * Hook for managing environment-based permissions without React Query
 */
export const useEnvironmentPermissions = () => {
  const [environmentInfo, setEnvironmentInfo] = useState<EnvironmentInfo | undefined>(undefined);
  const [permissions, setPermissions] = useState<EnvironmentPermissions | undefined>(undefined);
  const [isLoading, setIsLoading] = useState(true);

  const fetchData = async () => {
    try {
      const [envInfo, perms] = await Promise.all([
        systemConfigService.getEnvironmentConfig(),
        systemConfigService.getPermissions()
      ]);
      setEnvironmentInfo(envInfo);
      setPermissions(perms);
    } catch (error) {
      console.error('Error fetching environment data:', error);
      // Set default values on error
      setEnvironmentInfo({ type: 'DEVELOPMENT', name: 'Development' });
      setPermissions({ isAdmin: false, canWrite: true, canRead: true });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const checkPermission = async (action: string): Promise<boolean> => {
    try {
      return await systemConfigService.checkPermission(action);
    } catch {
      return false;
    }
  };

  const checkUIVisibility = async (element: string): Promise<boolean> => {
    try {
      return await systemConfigService.checkUIVisibility(element);
    } catch {
      return true; // Default to visible on error
    }
  };

  const updateEnvironmentType = async (type: string) => {
    const result = await systemConfigService.updateEnvironmentType(type);
    // Refetch data after update
    await fetchData();
    return result;
  };

  return {
    environmentInfo,
    permissions,
    isLoading,
    checkPermission,
    checkUIVisibility,
    updateEnvironmentType,
    isDevelopment: environmentInfo?.type === 'DEVELOPMENT',
    isQA: environmentInfo?.type === 'QUALITY_ASSURANCE',
    isProduction: environmentInfo?.type === 'PRODUCTION',
  };
};

/**
 * Hook for checking specific UI element visibility
 */
export const useUIVisibility = (element: string) => {
  const [visible, setVisible] = useState(true);
  const { permissions, isDevelopment } = useEnvironmentPermissions();

  useEffect(() => {
    const checkVisibility = () => {
      switch (element) {
        case 'createFlowButton':
        case 'createAdapterButton':
        case 'createBusinessComponentButton':
        case 'createDataStructureButton':
          setVisible(isDevelopment);
          break;
        case 'adminPanel':
          setVisible(permissions?.isAdmin || false);
          break;
        default:
          setVisible(true);
      }
    };

    checkVisibility();
  }, [element, permissions, isDevelopment]);

  return visible;
};