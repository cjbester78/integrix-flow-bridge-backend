import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

interface ErrorAlertProps {
  title?: string;
  error: string | Error | unknown;
  className?: string;
  variant?: 'default' | 'destructive';
}

export function ErrorAlert({ 
  title = 'Error', 
  error, 
  className,
  variant = 'destructive'
}: ErrorAlertProps) {
  const errorMessage = error instanceof Error 
    ? error.message 
    : typeof error === 'string' 
    ? error 
    : 'An unexpected error occurred';

  const Icon = variant === 'destructive' ? XCircle : AlertCircle;

  return (
    <Alert variant={variant} className={cn("mb-4", className)}>
      <Icon className="h-4 w-4" />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>{errorMessage}</AlertDescription>
    </Alert>
  );
}