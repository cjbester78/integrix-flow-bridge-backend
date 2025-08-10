import { cn } from '@/lib/utils';
import { ReactNode } from 'react';

interface PageContainerProps {
  children: ReactNode;
  className?: string;
}

export function PageContainer({ children, className }: PageContainerProps) {
  return (
    <div 
      className={cn(
        "mx-auto max-w-7xl px-6 md:px-8 py-6 space-y-6 animate-fade-in",
        className
      )}
    >
      {children}
    </div>
  );
}