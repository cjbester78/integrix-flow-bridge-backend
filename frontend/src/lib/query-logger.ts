// @ts-nocheck
import { QueryCache, MutationCache, Query, Mutation } from '@tanstack/react-query';
import { logger, LogCategory } from '@/lib/logger';

/**
 * Create a query cache with logging
 */
export const createQueryCacheWithLogging = () => {
  return new QueryCache({
    onError: (error: any, query: Query) => {
      logger.error(LogCategory.API, 'Query error', error, {
        queryKey: query.queryKey,
        queryHash: query.queryHash,
        state: query.state.status
      });
    },
    onSuccess: (data: unknown, query: Query) => {
      logger.debug(LogCategory.API, 'Query success', {
        queryKey: query.queryKey,
        queryHash: query.queryHash,
        dataType: Array.isArray(data) ? 'array' : typeof data,
        recordCount: Array.isArray(data) ? data.length : undefined
      });
    },
    onSettled: (data: unknown, error: any, query: Query) => {
      const duration = Date.now() - (query.state.dataUpdatedAt || 0);
      
      logger.debug(LogCategory.PERFORMANCE, 'Query completed', {
        queryKey: query.queryKey,
        duration,
        success: !error,
        fromCache: query.state.dataUpdateCount === 0
      });
    }
  });
};

/**
 * Create a mutation cache with logging
 */
export const createMutationCacheWithLogging = () => {
  return new MutationCache({
    onError: (error: any, variables: unknown, context: unknown, mutation: Mutation) => {
      logger.error(LogCategory.API, 'Mutation error', error, {
        mutationKey: mutation.options.mutationKey,
        variables,
        mutationId: mutation.mutationId
      });
    },
    onSuccess: (data: unknown, variables: unknown, context: unknown, mutation: Mutation) => {
      logger.info(LogCategory.API, 'Mutation success', {
        mutationKey: mutation.options.mutationKey,
        variables,
        mutationId: mutation.mutationId,
        dataType: typeof data
      });
    },
    onSettled: (data: unknown, error: any, variables: unknown, context: unknown, mutation: Mutation) => {
      const duration = Date.now() - mutation.state.submittedAt;
      
      logger.debug(LogCategory.PERFORMANCE, 'Mutation completed', {
        mutationKey: mutation.options.mutationKey,
        duration,
        success: !error,
        mutationId: mutation.mutationId
      });
    }
  });
};

/**
 * Log query client events
 */
export const setupQueryClientLogging = (queryClient: any) => {
  // Log when queries are invalidated
  const originalInvalidateQueries = queryClient.invalidateQueries.bind(queryClient);
  queryClient.invalidateQueries = (filters?: any) => {
    logger.debug(LogCategory.API, 'Invalidating queries', {
      queryKey: filters?.queryKey,
      exact: filters?.exact,
      refetchType: filters?.refetchType
    });
    return originalInvalidateQueries(filters);
  };

  // Log when queries are removed
  const originalRemoveQueries = queryClient.removeQueries.bind(queryClient);
  queryClient.removeQueries = (filters?: any) => {
    logger.debug(LogCategory.API, 'Removing queries', {
      queryKey: filters?.queryKey,
      exact: filters?.exact
    });
    return originalRemoveQueries(filters);
  };

  // Log when data is set in cache
  const originalSetQueryData = queryClient.setQueryData.bind(queryClient);
  queryClient.setQueryData = (queryKey: any, updater: any) => {
    logger.debug(LogCategory.API, 'Setting query data', {
      queryKey,
      updaterType: typeof updater
    });
    return originalSetQueryData(queryKey, updater);
  };

  return queryClient;
};