import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Clock } from 'lucide-react';

/**
 * Component that warns users before their session expires
 */
export const SessionTimeoutWarning: React.FC = () => {
  const { tokenExpiry, checkSession, logout } = useAuth();
  const [showWarning, setShowWarning] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState(0);

  useEffect(() => {
    if (!tokenExpiry) return;

    const checkTimeout = () => {
      const now = Date.now();
      const remaining = tokenExpiry - now;
      
      // Show warning 5 minutes before expiry
      const warningThreshold = 5 * 60 * 1000; // 5 minutes
      
      if (remaining <= 0) {
        // Token already expired
        logout();
      } else if (remaining <= warningThreshold && !showWarning) {
        setShowWarning(true);
        setTimeRemaining(Math.floor(remaining / 1000)); // Convert to seconds
      }
    };

    // Check immediately
    checkTimeout();

    // Check every 30 seconds
    const interval = setInterval(checkTimeout, 30000);

    return () => clearInterval(interval);
  }, [tokenExpiry, logout, showWarning]);

  useEffect(() => {
    if (!showWarning || timeRemaining <= 0) return;

    // Update countdown every second
    const countdown = setInterval(() => {
      setTimeRemaining((prev) => {
        if (prev <= 1) {
          logout();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(countdown);
  }, [showWarning, logout]);

  const handleExtendSession = async () => {
    try {
      const isValid = await checkSession();
      if (isValid) {
        setShowWarning(false);
        setTimeRemaining(0);
      }
    } catch (error) {
      console.error('Failed to extend session:', error);
      logout();
    }
  };

  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <AlertDialog open={showWarning} onOpenChange={setShowWarning}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            Session Expiring Soon
          </AlertDialogTitle>
          <AlertDialogDescription className="space-y-2">
            <p>Your session will expire in:</p>
            <p className="text-2xl font-mono font-bold text-foreground">
              {formatTime(timeRemaining)}
            </p>
            <p className="text-sm">Would you like to extend your session or logout?</p>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel onClick={logout}>
            Logout
          </AlertDialogCancel>
          <AlertDialogAction onClick={handleExtendSession}>
            Extend Session
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};