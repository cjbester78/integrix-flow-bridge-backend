import React from 'react';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Lock, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface RestrictedPageProps {
  children: React.ReactNode;
  requireDevelopment?: boolean;
  customMessage?: string;
}

/**
 * Wrapper component for pages that should be restricted in non-development environments
 */
export const RestrictedPage: React.FC<RestrictedPageProps> = ({
  children,
  requireDevelopment = true,
  customMessage
}) => {
  const { isDevelopment, environmentInfo } = useEnvironmentPermissions();
  const navigate = useNavigate();

  if (requireDevelopment && !isDevelopment) {
    return (
      <div className="p-6 max-w-2xl mx-auto">
        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col items-center text-center space-y-4">
              <div className="rounded-full bg-destructive/10 p-4">
                <Lock className="h-12 w-12 text-destructive" />
              </div>
              
              <div className="space-y-2">
                <h2 className="text-2xl font-bold">Page Restricted</h2>
                <p className="text-muted-foreground">
                  {customMessage || 
                    `This page is only available in development environment. 
                     The system is currently configured as ${environmentInfo?.displayName || 'non-development'}.`}
                </p>
              </div>

              <Alert className="mt-4">
                <AlertTriangle className="h-4 w-4" />
                <AlertTitle>Why is this restricted?</AlertTitle>
                <AlertDescription>
                  In {environmentInfo?.displayName || 'this'} environment, creation and modification 
                  of flows, adapters, and data structures is disabled to prevent accidental changes. 
                  You can still import flows, modify adapter configurations, and manage deployments.
                </AlertDescription>
              </Alert>

              <div className="flex gap-3 mt-6">
                <Button onClick={() => navigate(-1)} variant="outline">
                  Go Back
                </Button>
                <Button onClick={() => navigate('/interfaces')}>
                  Interface Management
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return <>{children}</>;
};