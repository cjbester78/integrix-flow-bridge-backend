import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Loader2 } from 'lucide-react';

interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * Component that protects routes by checking authentication status
 * Redirects to login if user is not authenticated or session expired
 */
export const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  const { user, isAuthenticated, checkSession, isLoading } = useAuth();
  const location = useLocation();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const verifySession = async () => {
      try {
        // Check if we have a stored token
        const token = localStorage.getItem('token');
        
        if (!token) {
          setIsChecking(false);
          return;
        }

        // Verify the session is still valid
        await checkSession();
      } catch (error) {
        console.error('Session verification failed:', error);
      } finally {
        setIsChecking(false);
      }
    };

    verifySession();
  }, [checkSession]);

  // Show loading while checking authentication
  if (isLoading || isChecking) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p className="text-muted-foreground">Verifying session...</p>
        </div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated || !user) {
    // Save the attempted location for redirect after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Render children if authenticated
  return <>{children}</>;
};

/**
 * Higher-order component for protecting individual routes
 */
export const withAuth = <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P> => {
  return (props: P) => (
    <AuthGuard>
      <Component {...props} />
    </AuthGuard>
  );
};