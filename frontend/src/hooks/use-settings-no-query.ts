import { useState, useEffect } from 'react';
import { apiClient } from '@/lib/api-client';
import { useNotify } from '@/stores/notification-store';
import { SystemSettings } from '@/types/settings';

/**
 * Hook to fetch system settings without React Query
 */
export const useSystemSettings = () => {
  const [data, setData] = useState<SystemSettings | undefined>(undefined);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const refetch = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get<SystemSettings>('/system/settings');
      setData(response);
      setError(null);
    } catch (err) {
      setError(err as Error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    refetch();
  }, []);

  return { data, isLoading, error, refetch };
};

/**
 * Hook to update system settings without React Query
 */
export const useUpdateSystemSettings = () => {
  const notify = useNotify();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  
  const mutate = async (settings: Partial<SystemSettings>) => {
    try {
      setIsLoading(true);
      setError(null);
      await apiClient.put<SystemSettings>('/system/settings', settings);
      notify.success('Settings updated', 'System settings have been updated successfully');
    } catch (err) {
      setError(err as Error);
      notify.error('Failed to update settings', (err as Error).message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return { mutate, isLoading, error };
};

/**
 * Hook to test email settings without React Query
 */
export const useTestEmailSettings = () => {
  const notify = useNotify();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  
  const mutate = async (testEmail: string) => {
    try {
      setIsLoading(true);
      setError(null);
      await apiClient.post('/system/settings/email/test', { email: testEmail });
      notify.success('Test email sent', `A test email has been sent to ${testEmail}`);
    } catch (err) {
      setError(err as Error);
      notify.error('Failed to send test email', (err as Error).message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return { mutate, isLoading, error };
};

/**
 * Hook to export system settings without React Query
 */
export const useExportSettings = () => {
  const notify = useNotify();
  const [isLoading, setIsLoading] = useState(false);
  
  const mutate = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get<Blob>('/system/settings/export', {
        responseType: 'blob'
      });
      
      // Create download link
      const url = window.URL.createObjectURL(response);
      const a = document.createElement('a');
      a.href = url;
      a.download = `integrix-settings-${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      notify.success('Settings exported', 'System settings have been exported successfully');
    } catch (err) {
      notify.error('Failed to export settings', (err as Error).message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return { mutate, isLoading };
};

/**
 * Hook to import system settings without React Query
 */
export const useImportSettings = () => {
  const notify = useNotify();
  const [isLoading, setIsLoading] = useState(false);
  
  const mutate = async (file: File) => {
    try {
      setIsLoading(true);
      const formData = new FormData();
      formData.append('file', file);
      
      await apiClient.post('/system/settings/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      notify.success('Settings imported', 'System settings have been imported successfully');
    } catch (err) {
      notify.error('Failed to import settings', (err as Error).message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return { mutate, isLoading };
};