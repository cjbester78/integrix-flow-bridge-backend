import { ReactNode } from 'react';
import { cn } from '@/lib/utils';

interface PageHeaderProps {
  title: string;
  description?: string;
  icon?: ReactNode;
  actions?: ReactNode;
  className?: string;
}

export const PageHeader = ({ 
  title, 
  description, 
  icon, 
  actions, 
  className 
}: PageHeaderProps) => {
  return (
    <div className={cn(
      "flex items-center justify-between mb-6 animate-slide-up",
      className
    )}>
      <div>
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          {icon && <span className="text-primary [&>svg]:h-5 [&>svg]:w-5">{icon}</span>}
          {title}
        </h1>
        {description && (
          <p className="text-muted-foreground mt-1">{description}</p>
        )}
      </div>
      {actions && (
        <div className="flex items-center gap-3">
          {actions}
        </div>
      )}
    </div>
  );
};