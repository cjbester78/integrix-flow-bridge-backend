import * as React from 'react';
import { cn } from '@/lib/utils';

export interface SegmentedControlOption {
  value: string;
  label: string;
  icon?: React.ReactNode;
}

interface SegmentedControlProps {
  value: string;
  onValueChange: (value: string) => void;
  options: SegmentedControlOption[];
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
}

export const SegmentedControl = React.forwardRef<
  HTMLDivElement,
  SegmentedControlProps
>(({ value, onValueChange, options, className, size = 'md', disabled = false }, ref) => {
  const sizeClasses = {
    sm: 'h-8 text-xs',
    md: 'h-10 text-sm',
    lg: 'h-12 text-base'
  };

  const handleClick = (option: SegmentedControlOption) => {
    if (!disabled) {
      onValueChange(option.value);
    }
  };

  return (
    <div
      ref={ref}
      className={cn(
        'inline-flex rounded-lg bg-muted p-1 text-muted-foreground',
        sizeClasses[size],
        disabled && 'opacity-50 cursor-not-allowed',
        className
      )}
    >
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => handleClick(option)}
          disabled={disabled}
          className={cn(
            'relative inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
            value === option.value ? 'bg-background text-foreground shadow-sm' : 'hover:text-foreground',
            'flex-1'
          )}
        >
          {option.icon && (
            <span className="mr-2">{option.icon}</span>
          )}
          {option.label}
        </button>
      ))}
    </div>
  );
});

SegmentedControl.displayName = 'SegmentedControl';