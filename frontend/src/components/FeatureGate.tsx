import { ReactNode } from 'react';
import { useTenant } from '@/contexts/TenantContext';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Lock } from 'lucide-react';

interface FeatureGateProps {
  feature: string;
  children: ReactNode;
  fallback?: ReactNode;
  showUpgradePrompt?: boolean;
}

export function FeatureGate({ 
  feature, 
  children, 
  fallback,
  showUpgradePrompt = true 
}: FeatureGateProps) {
  const { hasFeature, subscription } = useTenant();

  if (hasFeature(feature)) {
    return <>{children}</>;
  }

  if (fallback) {
    return <>{fallback}</>;
  }

  if (showUpgradePrompt) {
    return (
      <Alert>
        <Lock className="h-4 w-4" />
        <AlertTitle>Feature Unavailable</AlertTitle>
        <AlertDescription>
          This feature is not available in your current plan ({subscription?.planName || 'Unknown'}).
          Please upgrade to access this functionality.
        </AlertDescription>
      </Alert>
    );
  }

  return null;
}

/**
 * Hook to check feature availability
 */
export function useFeature(feature: string): boolean {
  const { hasFeature } = useTenant();
  return hasFeature(feature);
}