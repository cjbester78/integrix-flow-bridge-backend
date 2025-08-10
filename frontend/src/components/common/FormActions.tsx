import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

interface FormActionsProps {
  onCancel?: () => void;
  onSubmit?: () => void;
  onSecondary?: () => void;
  cancelLabel?: string;
  submitLabel?: string;
  secondaryLabel?: string;
  isLoading?: boolean;
  isDisabled?: boolean;
  className?: string;
  alignment?: 'left' | 'right' | 'center' | 'between';
}

export const FormActions = ({
  onCancel,
  onSubmit,
  onSecondary,
  cancelLabel = 'Cancel',
  submitLabel = 'Save',
  secondaryLabel,
  isLoading = false,
  isDisabled = false,
  className,
  alignment = 'right'
}: FormActionsProps) => {
  const alignmentClasses = {
    left: 'justify-start',
    right: 'justify-end',
    center: 'justify-center',
    between: 'justify-between'
  };

  return (
    <div className={cn(
      "flex gap-3 mt-6",
      alignmentClasses[alignment],
      className
    )}>
      {onSecondary && (
        <Button
          type="button"
          variant="outline"
          onClick={onSecondary}
          disabled={isLoading || isDisabled}
        >
          {secondaryLabel}
        </Button>
      )}
      
      {onCancel && (
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading}
        >
          {cancelLabel}
        </Button>
      )}
      
      {onSubmit && (
        <Button
          type="submit"
          onClick={onSubmit}
          disabled={isLoading || isDisabled}
        >
          {isLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
          {submitLabel}
        </Button>
      )}
    </div>
  );
};