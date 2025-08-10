import { ChevronRight, Home } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useNavigationHistory } from '@/hooks/useNavigationHistory';
import { cn } from '@/lib/utils';

export const Breadcrumb = () => {
  const { getBreadcrumbs, currentPath } = useNavigationHistory();
  const breadcrumbs = getBreadcrumbs();

  // Don't show breadcrumbs on login or root
  if (currentPath === '/login' || currentPath === '/') {
    return null;
  }

  return (
    <nav className="flex items-center space-x-1 text-sm text-muted-foreground px-6 py-2 border-b">
      {breadcrumbs.map((crumb, index) => {
        const isLast = index === breadcrumbs.length - 1;
        const isFirst = index === 0;

        return (
          <div key={crumb.path} className="flex items-center">
            {!isFirst && <ChevronRight className="h-4 w-4 mx-1" />}
            {isLast ? (
              <span className="font-medium text-foreground flex items-center">
                {isFirst && <Home className="h-4 w-4 mr-1" />}
                {crumb.label}
              </span>
            ) : (
              <Link
                to={crumb.path}
                className={cn(
                  "hover:text-foreground transition-colors flex items-center",
                  "hover:underline"
                )}
              >
                {isFirst && <Home className="h-4 w-4 mr-1" />}
                {crumb.label}
              </Link>
            )}
          </div>
        );
      })}
    </nav>
  );
};