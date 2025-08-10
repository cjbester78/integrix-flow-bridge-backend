import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { queryClient, queryKeys } from '@/lib/query-client-simple';
import { useNotify } from '@/stores/notification-store';
import { SystemSettings } from '@/types/settings';

/**
 * Hook to fetch system settings
 */
export const useSystemSettings = () => {
  return useQuery({
    queryKey: queryKeys.system.all,
    queryFn: () => apiClient.get<SystemSettings>('/system/settings'),
    staleTime: 5 * 60 * 1000, // Consider settings fresh for 5 minutes
  });
};

/**
 * Hook to update system settings
 */
export const useUpdateSystemSettings = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (settings: Partial<SystemSettings>) =>
      apiClient.put<SystemSettings>('/system/settings', settings),
    
    onSuccess: () => {
      notify.success('Settings updated', 'System settings have been updated successfully');
      queryClient.invalidateQueries({ queryKey: queryKeys.system.all });
    },
    
    onError: (error: any) => {
      notify.error('Failed to update settings', error.message);
    },
  });
};

/**
 * Hook to test email settings
 */
export const useTestEmailSettings = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (emailSettings: {
      smtpHost: string;
      smtpPort: number;
      smtpUsername: string;
      smtpPassword: string;
      smtpTls: boolean;
      testEmail: string;
    }) => apiClient.post('/system/settings/test-email', emailSettings),
    
    onSuccess: () => {
      notify.success('Email test successful', 'Test email has been sent successfully');
    },
    
    onError: (error: any) => {
      notify.error('Email test failed', error.message);
    },
  });
};

/**
 * Hook to test backup configuration
 */
export const useTestBackupConfig = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (backupConfig: any) =>
      apiClient.post('/system/settings/test-backup', backupConfig),
    
    onSuccess: () => {
      notify.success('Backup test successful', 'Backup configuration is valid');
    },
    
    onError: (error: any) => {
      notify.error('Backup test failed', error.message);
    },
  });
};

/**
 * Hook to export settings
 */
export const useExportSettings = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: () => apiClient.download('/system/settings/export', 'system-settings.json'),
    
    onSuccess: () => {
      notify.success('Settings exported', 'System settings have been exported successfully');
    },
    
    onError: (error: any) => {
      notify.error('Export failed', error.message);
    },
  });
};

/**
 * Hook to import settings
 */
export const useImportSettings = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return apiClient.upload<SystemSettings>('/system/settings/import', formData);
    },
    
    onSuccess: () => {
      notify.success('Settings imported', 'System settings have been imported successfully');
      queryClient.invalidateQueries({ queryKey: queryKeys.system.all });
    },
    
    onError: (error: any) => {
      notify.error('Import failed', error.message);
    },
  });
};