// @ts-nocheck
import { useEffect, useRef, useCallback } from 'react';
import { useLocation, useNavigationType } from 'react-router-dom';
import { logger, LogCategory, ContextLogger } from '@/lib/logger';
import { useAuthStore } from '@/stores/auth-store';

/**
 * Hook to log component lifecycle
 */
export const useComponentLogger = (componentName: string, props?: Record<string, any>) => {
  const mountTime = useRef<number>(Date.now());
  const renderCount = useRef<number>(0);

  useEffect(() => {
    renderCount.current++;
    
    // Log component mount
    logger.debug(LogCategory.UI, `Component mounted: ${componentName}`, {
      props,
      renderCount: renderCount.current
    });

    return () => {
      // Log component unmount with lifetime
      const lifetime = Date.now() - mountTime.current;
      logger.debug(LogCategory.UI, `Component unmounted: ${componentName}`, {
        lifetime,
        renderCount: renderCount.current
      });
    };
  }, []);

  // Log renders in development
  if (import.meta.env.DEV) {
    useEffect(() => {
      if (renderCount.current > 1) {
        logger.debug(LogCategory.PERFORMANCE, `Component re-rendered: ${componentName}`, {
          renderCount: renderCount.current,
          props
        });
      }
    });
  }
};

/**
 * Hook to log navigation events
 */
export const useNavigationLogger = () => {
  const location = useLocation();
  const navigationType = useNavigationType();
  const previousPath = useRef<string>(location.pathname);

  useEffect(() => {
    if (previousPath.current !== location.pathname) {
      logger.logNavigation(previousPath.current, location.pathname, {
        navigationType,
        search: location.search,
        hash: location.hash
      });
      previousPath.current = location.pathname;
    }
  }, [location, navigationType]);
};

/**
 * Hook to create user action logger
 */
export const useActionLogger = (context?: string) => {
  const user = useAuthStore(state => state.user);
  
  const logAction = useCallback((action: string, details?: Record<string, any>) => {
    logger.logUserAction(action, {
      context,
      userId: user?.id,
      username: user?.username,
      ...details
    });
  }, [context, user]);

  const logClick = useCallback((elementName: string, details?: Record<string, any>) => {
    logAction(`Clicked: ${elementName}`, details);
  }, [logAction]);

  const logFormSubmit = useCallback((formName: string, data?: Record<string, any>) => {
    logAction(`Form submitted: ${formName}`, { formData: data });
  }, [logAction]);

  const logValidationError = useCallback((formName: string, errors: Record<string, any>) => {
    logger.logValidationError(formName, errors);
  }, []);

  return {
    logAction,
    logClick,
    logFormSubmit,
    logValidationError
  };
};

/**
 * Hook to measure and log performance
 */
export const usePerformanceLogger = (metricName: string) => {
  const startTime = useRef<number>();

  const startMeasure = useCallback(() => {
    startTime.current = performance.now();
  }, []);

  const endMeasure = useCallback((details?: Record<string, any>) => {
    if (startTime.current) {
      const duration = performance.now() - startTime.current;
      logger.logPerformance(metricName, duration, details);
      startTime.current = undefined;
    }
  }, [metricName]);

  const measure = useCallback(async <T,>(
    fn: () => Promise<T>,
    details?: Record<string, any>
  ): Promise<T> => {
    return logger.measureExecutionTime(metricName, fn, LogCategory.PERFORMANCE);
  }, [metricName]);

  return {
    startMeasure,
    endMeasure,
    measure
  };
};

/**
 * Hook to create context logger
 */
export const useContextLogger = (context: Record<string, any>): ContextLogger => {
  const contextLogger = useRef<ContextLogger>();

  if (!contextLogger.current) {
    contextLogger.current = logger.createContextLogger(context);
  }

  return contextLogger.current;
};

/**
 * Hook to log errors with context
 */
export const useErrorLogger = (context?: string) => {
  const logError = useCallback((message: string, error: Error | any, details?: Record<string, any>) => {
    logger.error(LogCategory.ERROR, message, error, {
      context,
      ...details
    });
  }, [context]);

  const logWarning = useCallback((message: string, details?: Record<string, any>) => {
    logger.warn(LogCategory.ERROR, message, {
      context,
      ...details
    });
  }, [context]);

  return {
    logError,
    logWarning
  };
};

/**
 * Hook to track form field changes
 */
export const useFormLogger = (formName: string) => {
  const fieldChangeCount = useRef<Record<string, number>>({});
  const formStartTime = useRef<number>(Date.now());

  const logFieldChange = useCallback((fieldName: string, value: any, previousValue?: any) => {
    fieldChangeCount.current[fieldName] = (fieldChangeCount.current[fieldName] || 0) + 1;
    
    logger.debug(LogCategory.USER_ACTION, `Form field changed: ${formName}.${fieldName}`, {
      fieldName,
      changeCount: fieldChangeCount.current[fieldName],
      hasValue: !!value,
      valueType: typeof value
    });
  }, [formName]);

  const logFormComplete = useCallback((success: boolean, data?: any) => {
    const duration = Date.now() - formStartTime.current;
    
    logger.info(LogCategory.USER_ACTION, `Form completed: ${formName}`, {
      success,
      duration,
      fieldChangeCount: fieldChangeCount.current,
      totalFieldChanges: Object.values(fieldChangeCount.current).reduce((a, b) => a + b, 0),
      data
    });
  }, [formName]);

  return {
    logFieldChange,
    logFormComplete
  };
};

/**
 * Hook to automatically log fetch operations
 */
export const useFetchLogger = (resourceName: string) => {
  const logFetchStart = useCallback((params?: Record<string, any>) => {
    logger.debug(LogCategory.API, `Fetching ${resourceName}`, params);
  }, [resourceName]);

  const logFetchSuccess = useCallback((data: any, duration?: number) => {
    logger.debug(LogCategory.API, `Fetched ${resourceName} successfully`, {
      recordCount: Array.isArray(data) ? data.length : 1,
      duration
    });
  }, [resourceName]);

  const logFetchError = useCallback((error: Error | any) => {
    logger.error(LogCategory.API, `Failed to fetch ${resourceName}`, error);
  }, [resourceName]);

  return {
    logFetchStart,
    logFetchSuccess,
    logFetchError
  };
};