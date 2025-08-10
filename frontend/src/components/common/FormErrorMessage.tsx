import { AlertCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

interface FormErrorMessageProps {
  error?: string | null;
  className?: string;
}

export const FormErrorMessage = ({ error, className }: FormErrorMessageProps) => {
  if (!error) return null;

  return (
    <div className={cn(
      "flex items-center gap-2 text-sm text-destructive mt-1",
      className
    )}>
      <AlertCircle className="h-4 w-4" />
      <span>{error}</span>
    </div>
  );
};