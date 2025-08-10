import React from 'react';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Lock } from 'lucide-react';

interface PermissionGuardProps {
  action: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
  showMessage?: boolean;
}

/**
 * Component that conditionally renders children based on environment permissions
 */
export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  action,
  children,
  fallback,
  showMessage = true
}) => {
  const { permissions, environmentInfo } = useEnvironmentPermissions();
  
  // Check permission based on action
  const hasPermission = React.useMemo(() => {
    switch (action) {
      case 'flow.create':
        return permissions?.canCreateFlows ?? false;
      case 'adapter.create':
        return permissions?.canCreateAdapters ?? false;
      case 'adapter.updateConfig':
        return permissions?.canModifyAdapterConfig ?? true;
      case 'flow.import':
        return permissions?.canImportFlows ?? true;
      case 'flow.deploy':
        return permissions?.canDeployFlows ?? true;
      case 'businessComponent.create':
        return permissions?.canCreateBusinessComponents ?? false;
      case 'dataStructure.create':
        return permissions?.canCreateDataStructures ?? false;
      case 'admin.access':
        return permissions?.isAdmin ?? false;
      default:
        return true;
    }
  }, [permissions, action]);

  if (!hasPermission) {
    if (fallback) {
      return <>{fallback}</>;
    }

    if (showMessage) {
      return (
        <Alert variant="destructive">
          <Lock className="h-4 w-4" />
          <AlertDescription>
            This action is not available in {environmentInfo?.displayName || 'this'} environment.
            {environmentInfo?.type !== 'DEVELOPMENT' && (
              <span className="block mt-1 text-sm">
                Only available in development environment.
              </span>
            )}
          </AlertDescription>
        </Alert>
      );
    }

    return null;
  }

  return <>{children}</>;
};

/**
 * Hook for checking if a UI element should be visible
 */
export const usePermissionVisibility = (element: string): boolean => {
  const { permissions, isDevelopment } = useEnvironmentPermissions();

  return React.useMemo(() => {
    switch (element) {
      case 'createFlowButton':
      case 'createAdapterButton':
      case 'createBusinessComponentButton':
      case 'createDataStructureButton':
        return isDevelopment ?? false;
      case 'importFlowButton':
      case 'deployButton':
      case 'exportButton':
      case 'adapterConfigButton':
        return true;
      case 'adminPanel':
        return permissions?.isAdmin ?? false;
      default:
        return true;
    }
  }, [element, permissions, isDevelopment]);
};