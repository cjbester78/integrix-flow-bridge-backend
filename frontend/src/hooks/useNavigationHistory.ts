import { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

interface NavigationHistoryItem {
  pathname: string;
  state?: any;
  timestamp: number;
}

// Global navigation history (max 10 items)
const navigationHistory: NavigationHistoryItem[] = [];
const MAX_HISTORY_SIZE = 10;

/**
 * Hook to track navigation history and provide smart back navigation
 */
export const useNavigationHistory = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const previousPathRef = useRef<string | null>(null);

  useEffect(() => {
    // Only track if it's a different path
    if (location.pathname !== previousPathRef.current) {
      // Add to history
      navigationHistory.push({
        pathname: location.pathname,
        state: location.state,
        timestamp: Date.now()
      });

      // Keep history size limited
      if (navigationHistory.length > MAX_HISTORY_SIZE) {
        navigationHistory.shift();
      }

      previousPathRef.current = location.pathname;
    }
  }, [location]);

  /**
   * Navigate back with smart behavior
   * - If there's a previous page in history, go there
   * - If coming from menu/direct navigation, go to default page
   * - Reset form state when appropriate
   */
  const navigateBack = (defaultPath: string = '/dashboard') => {
    // Check if we have history (excluding current page)
    const historyWithoutCurrent = navigationHistory.filter(
      item => item.pathname !== location.pathname
    );

    if (historyWithoutCurrent.length > 0) {
      // Go to the most recent different page
      const previousPage = historyWithoutCurrent[historyWithoutCurrent.length - 1];
      navigate(previousPage.pathname, { state: previousPage.state });
    } else {
      // No history or direct navigation - go to default
      navigate(defaultPath);
    }
  };

  /**
   * Check if the current navigation was from menu (direct)
   */
  const isDirectNavigation = (): boolean => {
    // If history has less than 2 items, it's likely direct navigation
    if (navigationHistory.length < 2) return true;

    // Check if previous page was from a different section
    const currentPath = location.pathname;
    const previousPath = navigationHistory[navigationHistory.length - 2]?.pathname;

    // If no previous path or significant path change, consider it direct
    if (!previousPath) return true;

    // Check if paths are in same section
    const currentSection = currentPath.split('/')[1];
    const previousSection = previousPath?.split('/')[1];

    return currentSection !== previousSection;
  };

  /**
   * Get breadcrumb trail
   */
  const getBreadcrumbs = () => {
    const paths = location.pathname.split('/').filter(Boolean);
    const breadcrumbs = [{ label: 'Home', path: '/' }];

    let currentPath = '';
    paths.forEach((segment) => {
      currentPath += `/${segment}`;
      breadcrumbs.push({
        label: formatSegmentLabel(segment),
        path: currentPath
      });
    });

    return breadcrumbs;
  };

  return {
    navigateBack,
    isDirectNavigation,
    getBreadcrumbs,
    currentPath: location.pathname,
    hasHistory: navigationHistory.length > 1
  };
};

/**
 * Format URL segment to readable label
 */
function formatSegmentLabel(segment: string): string {
  // Special case for settings page
  if (segment === 'settings') {
    return 'User Settings';
  }
  
  // Special case for development-functions
  if (segment === 'development-functions') {
    return 'Development Functions';
  }
  
  return segment
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}