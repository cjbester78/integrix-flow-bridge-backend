// @ts-nocheck
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { queryClient, queryKeys, invalidateRelatedQueries } from '@/lib/query-client-simple';
import { useNotify } from '@/stores/notification-store';

/**
 * Integration Flow DTOs
 */
export interface IntegrationFlowDTO {
  id: string;
  name: string;
  description?: string;
  sourceAdapterId: string;
  targetAdapterId: string;
  businessComponentId: string;
  isActive: boolean;
  flowType: 'DIRECT_MAPPING' | 'ORCHESTRATION';
  createdAt: string;
  updatedAt: string;
  lastExecutionTime?: string;
  executionCount: number;
  errorCount: number;
  averageExecutionTime?: number;
}

export interface CreateFlowDTO {
  name: string;
  description?: string;
  sourceAdapterId: string;
  targetAdapterId: string;
  businessComponentId: string;
  flowType: 'DIRECT_MAPPING' | 'ORCHESTRATION';
}

export interface UpdateFlowDTO {
  name?: string;
  description?: string;
  sourceAdapterId?: string;
  targetAdapterId?: string;
  isActive?: boolean;
}

export interface FlowExecutionDTO {
  id: string;
  flowId: string;
  startTime: string;
  endTime?: string;
  status: 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';
  errorMessage?: string;
  recordsProcessed: number;
  executionTime?: number;
}

export interface FlowMetricsDTO {
  flowId: string;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  averageExecutionTime: number;
  lastExecutionTime?: string;
  throughput: number;
  errorRate: number;
}

interface FlowListParams {
  page?: number;
  size?: number;
  search?: string;
  businessComponentId?: string;
  flowType?: 'DIRECT_MAPPING' | 'ORCHESTRATION';
  isActive?: boolean;
}

/**
 * Hook to fetch all integration flows
 */
export const useFlows = (params?: FlowListParams) => {
  return useQuery({
    queryKey: queryKeys.flows.list(params || {}),
    queryFn: () => apiClient.get<IntegrationFlowDTO[]>('/integration-flows', { params }),
  });
};

/**
 * Hook to fetch a single flow by ID
 */
export const useFlow = (flowId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.flows.detail(flowId),
    queryFn: () => apiClient.get<IntegrationFlowDTO>(`/integration-flows/${flowId}`),
    enabled: enabled && !!flowId,
  });
};

/**
 * Hook to fetch flow executions
 */
export const useFlowExecutions = (flowId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.flows.executions(flowId),
    queryFn: () => apiClient.get<FlowExecutionDTO[]>(`/integration-flows/${flowId}/executions`),
    enabled: enabled && !!flowId,
    refetchInterval: 5000, // Poll every 5 seconds for live updates
  });
};

/**
 * Hook to fetch flow metrics
 */
export const useFlowMetrics = (flowId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.flows.metrics(flowId),
    queryFn: () => apiClient.get<FlowMetricsDTO>(`/integration-flows/${flowId}/metrics`),
    enabled: enabled && !!flowId,
    refetchInterval: 30000, // Refresh every 30 seconds
  });
};

/**
 * Hook to create a new flow
 */
export const useCreateFlow = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (data: CreateFlowDTO) =>
      apiClient.post<IntegrationFlowDTO>('/integration-flows', data),
    
    onSuccess: (flow) => {
      notify.success('Flow created', `Integration flow "${flow.name}" has been created successfully`);
      invalidateRelatedQueries('flow');
    },
    
    onError: (error: any) => {
      notify.error('Failed to create flow', error.message);
    },
  });
};

/**
 * Hook to update a flow
 */
export const useUpdateFlow = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: ({ flowId, data }: { flowId: string; data: UpdateFlowDTO }) =>
      apiClient.put<IntegrationFlowDTO>(`/integration-flows/${flowId}`, data),
    
    onSuccess: (flow) => {
      notify.success('Flow updated', `Integration flow "${flow.name}" has been updated successfully`);
      invalidateRelatedQueries('flow', flow.id);
    },
    
    onError: (error: any) => {
      notify.error('Failed to update flow', error.message);
    },
  });
};

/**
 * Hook to delete a flow
 */
export const useDeleteFlow = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (flowId: string) =>
      apiClient.delete(`/integration-flows/${flowId}`),
    
    onSuccess: (_, flowId) => {
      notify.success('Flow deleted', 'Integration flow has been deleted successfully');
      invalidateRelatedQueries('flow', flowId);
    },
    
    onError: (error: any) => {
      notify.error('Failed to delete flow', error.message);
    },
  });
};

/**
 * Hook to execute a flow
 */
export const useExecuteFlow = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (flowId: string) =>
      apiClient.post<FlowExecutionDTO>(`/integration-flows/${flowId}/execute`),
    
    onMutate: async (flowId) => {
      // Show loading notification
      notify.info('Flow execution started', 'The integration flow is now running...');
    },
    
    onSuccess: (execution, flowId) => {
      notify.success('Flow executed', 'Integration flow has been triggered successfully');
      
      // Immediately refetch executions and metrics
      queryClient.invalidateQueries({ queryKey: queryKeys.flows.executions(flowId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.flows.metrics(flowId) });
    },
    
    onError: (error: any) => {
      notify.error('Failed to execute flow', error.message);
    },
  });
};

/**
 * Hook for optimistic flow activation toggle
 */
export const useToggleFlowStatus = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ flowId, isActive }: { flowId: string; isActive: boolean }) =>
      apiClient.patch<IntegrationFlowDTO>(`/integration-flows/${flowId}/status`, { isActive }),
    
    onMutate: async ({ flowId, isActive }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: queryKeys.flows.detail(flowId) });
      
      // Snapshot previous value
      const previousFlow = queryClient.getQueryData<IntegrationFlowDTO>(
        queryKeys.flows.detail(flowId)
      );
      
      // Optimistically update
      if (previousFlow) {
        queryClient.setQueryData<IntegrationFlowDTO>(
          queryKeys.flows.detail(flowId),
          {
            ...previousFlow,
            isActive,
          }
        );
      }
      
      // Update in list view too
      queryClient.setQueriesData<IntegrationFlowDTO[]>(
        { queryKey: queryKeys.flows.lists() },
        (old) => {
          if (!old) return old;
          return old.map((flow) =>
            flow.id === flowId ? { ...flow, isActive } : flow
          );
        }
      );
      
      return { previousFlow };
    },
    
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousFlow) {
        queryClient.setQueryData(
          queryKeys.flows.detail(variables.flowId),
          context.previousFlow
        );
      }
      notify.error('Failed to update flow status', err.message);
    },
    
    onSettled: (_, __, { flowId }) => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: queryKeys.flows.detail(flowId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.flows.lists() });
    },
    
    onSuccess: (_, { isActive }) => {
      notify.success(
        'Flow status updated',
        `Integration flow has been ${isActive ? 'activated' : 'deactivated'}`
      );
    },
  });
};

/**
 * Hook to deploy a flow (saga pattern)
 */
export const useDeployFlow = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (flowId: string) =>
      apiClient.post<{ sagaId: string; status: string }>(`/integration-flows/${flowId}/deploy`),
    
    onSuccess: (result, flowId) => {
      notify.success('Flow deployment started', `Deployment saga ${result.sagaId} initiated`);
      invalidateRelatedQueries('flow', flowId);
    },
    
    onError: (error: any) => {
      notify.error('Failed to deploy flow', error.message);
    },
  });
};