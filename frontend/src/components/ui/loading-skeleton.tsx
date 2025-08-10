import { cn } from '@/lib/utils';
import { Skeleton } from '@/components/ui/skeleton';

interface LoadingSkeletonProps {
  variant?: 'card' | 'table' | 'list' | 'text' | 'button';
  count?: number;
  className?: string;
}

export function LoadingSkeleton({ 
  variant = 'card', 
  count = 1, 
  className 
}: LoadingSkeletonProps) {
  const renderSkeleton = () => {
    switch (variant) {
      case 'card':
        return (
          <div className={cn("rounded-lg border p-6 space-y-4", className)}>
            <div className="space-y-2">
              <Skeleton className="h-4 w-[250px]" />
              <Skeleton className="h-4 w-[200px]" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-3/4" />
            </div>
          </div>
        );
      
      case 'table':
        return (
          <div className={cn("space-y-4", className)}>
            <div className="rounded-md border">
              <div className="border-b px-6 py-4">
                <Skeleton className="h-4 w-[150px]" />
              </div>
              <div className="p-0">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="border-b px-6 py-4 flex items-center space-x-4">
                    <Skeleton className="h-4 w-[100px]" />
                    <Skeleton className="h-4 w-[200px]" />
                    <Skeleton className="h-4 w-[150px]" />
                    <Skeleton className="h-4 w-[80px] ml-auto" />
                  </div>
                ))}
              </div>
            </div>
          </div>
        );
      
      case 'list':
        return (
          <div className={cn("space-y-3", className)}>
            {[...Array(3)].map((_, i) => (
              <div key={i} className="flex items-center space-x-4 p-4 rounded-lg border">
                <Skeleton className="h-12 w-12 rounded-full" />
                <div className="space-y-2 flex-1">
                  <Skeleton className="h-4 w-[200px]" />
                  <Skeleton className="h-3 w-[150px]" />
                </div>
                <Skeleton className="h-8 w-20" />
              </div>
            ))}
          </div>
        );
      
      case 'text':
        return (
          <div className={cn("space-y-2", className)}>
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
          </div>
        );
      
      case 'button':
        return <Skeleton className={cn("h-10 w-24", className)} />;
      
      default:
        return null;
    }
  };

  return (
    <>
      {[...Array(count)].map((_, i) => (
        <div key={i}>
          {renderSkeleton()}
        </div>
      ))}
    </>
  );
}