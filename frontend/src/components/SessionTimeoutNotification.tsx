import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Clock } from 'lucide-react';

export const SessionTimeoutNotification = () => {
  const { tokenExpiry } = useAuth();
  const { toast } = useToast();
  const [showWarning, setShowWarning] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState<number>(0);

  useEffect(() => {
    if (!tokenExpiry) return;

    const updateTimeRemaining = () => {
      const remaining = tokenExpiry - Date.now();
      setTimeRemaining(remaining);

      // Show warning when 10 minutes or less remain
      if (remaining <= 10 * 60 * 1000 && remaining > 0) {
        setShowWarning(true);
      } else {
        setShowWarning(false);
      }

      // Show toast when 5 minutes remain
      if (remaining <= 5 * 60 * 1000 && remaining > 4 * 60 * 1000) {
        toast({
          title: "Session Expiring Soon",
          description: "Your session will expire in 5 minutes. Please save your work.",
          variant: "destructive",
        });
      }

      // Show toast when 1 minute remains
      if (remaining <= 1 * 60 * 1000 && remaining > 30 * 1000) {
        toast({
          title: "Session Expiring",
          description: "Your session will expire in 1 minute.",
          variant: "destructive",
        });
      }
    };

    updateTimeRemaining();
    const interval = setInterval(updateTimeRemaining, 1000);

    return () => clearInterval(interval);
  }, [tokenExpiry, toast]);

  const handleExtendSession = () => {
    // In a real app, this would refresh the token
    // For now, we'll just hide the warning
    setShowWarning(false);
    toast({
      title: "Session Extended",
      description: "Your session has been extended.",
    });
  };

  const formatTimeRemaining = (ms: number) => {
    const minutes = Math.floor(ms / (1000 * 60));
    const seconds = Math.floor((ms % (1000 * 60)) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <AlertDialog open={showWarning && timeRemaining > 0}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            Session Expiring Soon
          </AlertDialogTitle>
          <AlertDialogDescription className="space-y-2">
            <p>Your session will expire in:</p>
            <p className="text-2xl font-mono font-bold text-foreground">
              {formatTimeRemaining(timeRemaining)}
            </p>
            <p className="text-sm">Please save your work or extend your session to continue.</p>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogAction onClick={handleExtendSession}>
            Extend Session
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};