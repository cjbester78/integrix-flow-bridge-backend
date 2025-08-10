import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { LucideIcon } from 'lucide-react';

interface FormActionsProps {
  primaryLabel?: string;
  primaryIcon?: LucideIcon;
  primaryLoading?: boolean;
  primaryDisabled?: boolean;
  onPrimaryClick?: () => void;
  primaryType?: 'button' | 'submit' | 'reset';
  
  secondaryLabel?: string;
  secondaryIcon?: LucideIcon;
  secondaryDisabled?: boolean;
  onSecondaryClick?: () => void;
  
  className?: string;
  align?: 'left' | 'right' | 'center' | 'between';
}

export function FormActions({
  primaryLabel = 'Save',
  primaryIcon: PrimaryIcon,
  primaryLoading = false,
  primaryDisabled = false,
  onPrimaryClick,
  primaryType = 'submit',
  
  secondaryLabel = 'Cancel',
  secondaryIcon: SecondaryIcon,
  secondaryDisabled = false,
  onSecondaryClick,
  
  className,
  align = 'right'
}: FormActionsProps) {
  const alignmentClasses = {
    left: 'justify-start',
    right: 'justify-end',
    center: 'justify-center',
    between: 'justify-between'
  };

  return (
    <div className={cn(
      "flex items-center gap-3",
      alignmentClasses[align],
      className
    )}>
      {onSecondaryClick && (
        <Button
          type="button"
          variant="outline"
          onClick={onSecondaryClick}
          disabled={secondaryDisabled}
        >
          {SecondaryIcon && <SecondaryIcon className="h-4 w-4 mr-2" />}
          {secondaryLabel}
        </Button>
      )}
      
      <Button
        type={primaryType}
        onClick={onPrimaryClick}
        disabled={primaryDisabled || primaryLoading}
      >
        {primaryLoading ? (
          <>
            <span className="h-4 w-4 mr-2 animate-spin rounded-full border-2 border-current border-t-transparent" />
            {primaryLabel}ing...
          </>
        ) : (
          <>
            {PrimaryIcon && <PrimaryIcon className="h-4 w-4 mr-2" />}
            {primaryLabel}
          </>
        )}
      </Button>
    </div>
  );
}