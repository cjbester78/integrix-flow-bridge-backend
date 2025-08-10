import { QueryClient } from '@tanstack/react-query';
// Temporarily disabled query logging due to circular dependencies
// import { createQueryCacheWithLogging, createMutationCacheWithLogging, setupQueryClientLogging } from '@/lib/query-logger';

/**
 * Create and configure QueryClient instance
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      gcTime: 10 * 60 * 1000,
      retry: 3,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
});

/**
 * Query key factory for consistent query key generation
 */
export const queryKeys = {
  // Auth
  auth: {
    all: ['auth'] as const,
    me: () => [...queryKeys.auth.all, 'me'] as const,
  },
  
  // Users
  users: {
    all: ['users'] as const,
    lists: () => [...queryKeys.users.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.users.lists(), filters] as const,
    details: () => [...queryKeys.users.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.users.details(), id] as const,
  },
  
  // Business Components
  businessComponents: {
    all: ['businessComponents'] as const,
    lists: () => [...queryKeys.businessComponents.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.businessComponents.lists(), filters] as const,
    details: () => [...queryKeys.businessComponents.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.businessComponents.details(), id] as const,
  },
  
  // Adapters
  adapters: {
    all: ['adapters'] as const,
    lists: () => [...queryKeys.adapters.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.adapters.lists(), filters] as const,
    details: () => [...queryKeys.adapters.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.adapters.details(), id] as const,
    types: () => [...queryKeys.adapters.all, 'types'] as const,
    config: (type: string) => [...queryKeys.adapters.all, 'config', type] as const,
  },
  
  // Integration Flows
  flows: {
    all: ['flows'] as const,
    lists: () => [...queryKeys.flows.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.flows.lists(), filters] as const,
    details: () => [...queryKeys.flows.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.flows.details(), id] as const,
    executions: (flowId: string) => [...queryKeys.flows.all, 'executions', flowId] as const,
    metrics: (flowId: string) => [...queryKeys.flows.all, 'metrics', flowId] as const,
  },
  
  // Field Mappings
  mappings: {
    all: ['mappings'] as const,
    byFlow: (flowId: string) => [...queryKeys.mappings.all, 'flow', flowId] as const,
    detail: (id: string) => [...queryKeys.mappings.all, 'detail', id] as const,
  },
  
  // System
  system: {
    all: ['system'] as const,
    health: () => [...queryKeys.system.all, 'health'] as const,
    metrics: () => [...queryKeys.system.all, 'metrics'] as const,
    logs: (filters: any) => [...queryKeys.system.all, 'logs', filters] as const,
    certificates: () => [...queryKeys.system.all, 'certificates'] as const,
    settings: () => [...queryKeys.system.all, 'settings'] as const,
  },
};

/**
 * Helper to invalidate related queries
 */
export const invalidateRelatedQueries = async (entity: string, id?: string) => {
  const invalidations: Promise<void>[] = [];
  
  switch (entity) {
    case 'user':
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.users.all }));
      break;
      
    case 'businessComponent':
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.businessComponents.all }));
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.adapters.all }));
      break;
      
    case 'adapter':
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.adapters.all }));
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.flows.all }));
      break;
      
    case 'flow':
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.flows.all }));
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.mappings.all }));
      if (id) {
        invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.flows.executions(id) }));
        invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.flows.metrics(id) }));
      }
      break;
      
    case 'mapping':
      invalidations.push(queryClient.invalidateQueries({ queryKey: queryKeys.mappings.all }));
      break;
  }
  
  await Promise.all(invalidations);
};