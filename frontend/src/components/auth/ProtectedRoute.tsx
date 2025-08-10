import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Loader2 } from 'lucide-react';

/**
 * Component that wraps routes requiring authentication
 * Uses React Router's Outlet to render child routes
 */
export const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  // Don't show loading state too long - prevent infinite loading
  const [showLoading, setShowLoading] = React.useState(true);
  
  React.useEffect(() => {
    // Hide loading after 500ms to prevent infinite loading states
    const timer = setTimeout(() => {
      setShowLoading(false);
    }, 500);
    
    return () => clearTimeout(timer);
  }, []);

  // Log authentication state for debugging
  React.useEffect(() => {
    console.log('ProtectedRoute: isAuthenticated =', isAuthenticated, 'isLoading =', isLoading);
  }, [isAuthenticated, isLoading]);

  if (isLoading && showLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  // Always redirect to login if not authenticated
  if (!isAuthenticated) {
    console.log('ProtectedRoute: Redirecting to login - not authenticated');
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};