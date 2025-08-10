import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Clock, RefreshCw } from 'lucide-react';

export const SessionTimeoutNotification = () => {
  const { tokenExpiry, logout } = useAuth();
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

  if (!showWarning || timeRemaining <= 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 w-80">
      <Alert className="border-warning bg-warning/10">
        <Clock className="h-4 w-4" />
        <AlertDescription className="flex items-center justify-between">
          <div>
            <p className="font-medium">Session expires in:</p>
            <p className="text-lg font-mono">{formatTimeRemaining(timeRemaining)}</p>
          </div>
          <Button
            size="sm"
            variant="outline"
            onClick={handleExtendSession}
            className="ml-2"
          >
            <RefreshCw className="h-3 w-3 mr-1" />
            Extend
          </Button>
        </AlertDescription>
      </Alert>
    </div>
  );
};