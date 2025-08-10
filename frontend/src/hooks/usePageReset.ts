import { useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { useNavigationHistory } from './useNavigationHistory';

/**
 * Hook to reset page state when navigating directly from menu
 * @param resetCallback Function to call when page should be reset
 */
export const usePageReset = (resetCallback: () => void) => {
  const location = useLocation();
  const { isDirectNavigation } = useNavigationHistory();
  const hasResetRef = useRef(false);
  const previousPathRef = useRef(location.pathname);

  useEffect(() => {
    // Check if this is a new navigation to this page
    const isNewNavigation = previousPathRef.current !== location.pathname;
    
    if (isNewNavigation) {
      previousPathRef.current = location.pathname;
      
      // Reset if it's direct navigation (from menu)
      if (isDirectNavigation()) {
        resetCallback();
        hasResetRef.current = true;
      } else {
        hasResetRef.current = false;
      }
    }
  }, [location.pathname, isDirectNavigation, resetCallback]);

  return {
    isDirectNavigation: isDirectNavigation(),
    hasReset: hasResetRef.current
  };
};