// @ts-nocheck
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { queryClient, queryKeys } from '@/lib/query-client-simple';
import { useNotify } from '@/stores/notification-store';

/**
 * Field Mapping DTOs
 */
export interface FieldMappingDTO {
  id: string;
  integrationFlowId: string;
  sourceField: string;
  targetField: string;
  transformationType: 'DIRECT' | 'CUSTOM' | 'LOOKUP' | 'CONCATENATE' | 'SPLIT';
  transformationExpression?: string;
  defaultValue?: string;
  isRequired: boolean;
  sequence: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateFieldMappingDTO {
  integrationFlowId: string;
  sourceField: string;
  targetField: string;
  transformationType: 'DIRECT' | 'CUSTOM' | 'LOOKUP' | 'CONCATENATE' | 'SPLIT';
  transformationExpression?: string;
  defaultValue?: string;
  isRequired?: boolean;
  sequence?: number;
}

export interface UpdateFieldMappingDTO {
  sourceField?: string;
  targetField?: string;
  transformationType?: 'DIRECT' | 'CUSTOM' | 'LOOKUP' | 'CONCATENATE' | 'SPLIT';
  transformationExpression?: string;
  defaultValue?: string;
  isRequired?: boolean;
  sequence?: number;
}

/**
 * Hook to fetch field mappings for a flow
 */
export const useFieldMappings = (flowId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.mappings.byFlow(flowId),
    queryFn: () => apiClient.get<FieldMappingDTO[]>(`/field-mappings/flow/${flowId}`),
    enabled: enabled && !!flowId,
  });
};

/**
 * Hook to fetch a single field mapping
 */
export const useFieldMapping = (mappingId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.mappings.detail(mappingId),
    queryFn: () => apiClient.get<FieldMappingDTO>(`/field-mappings/${mappingId}`),
    enabled: enabled && !!mappingId,
  });
};

/**
 * Hook to create a field mapping with optimistic update
 */
export const useCreateFieldMapping = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateFieldMappingDTO) =>
      apiClient.post<FieldMappingDTO>('/field-mappings', data),

    onMutate: async (newMapping) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ 
        queryKey: queryKeys.mappings.byFlow(newMapping.integrationFlowId) 
      });

      // Snapshot previous value
      const previousMappings = queryClient.getQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(newMapping.integrationFlowId)
      );

      // Optimistically update with temporary data
      const tempMapping: FieldMappingDTO = {
        id: `temp-${Date.now()}`,
        ...newMapping,
        isRequired: newMapping.isRequired ?? false,
        sequence: newMapping.sequence ?? 999,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      queryClient.setQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(newMapping.integrationFlowId),
        (old) => [...(old || []), tempMapping]
      );

      return { previousMappings, tempMapping };
    },

    onError: (err, newMapping, context) => {
      // Rollback on error
      if (context?.previousMappings) {
        queryClient.setQueryData(
          queryKeys.mappings.byFlow(newMapping.integrationFlowId),
          context.previousMappings
        );
      }
      notify.error('Failed to create field mapping', err.message);
    },

    onSuccess: (data, variables, context) => {
      // Replace temp mapping with real one
      queryClient.setQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(variables.integrationFlowId),
        (old) => old?.map(mapping => 
          mapping.id === context?.tempMapping.id ? data : mapping
        ) || [data]
      );
      
      notify.success('Field mapping created', 'The field mapping has been added successfully');
    },

    onSettled: (_, __, variables) => {
      // Always refetch to ensure consistency
      queryClient.invalidateQueries({ 
        queryKey: queryKeys.mappings.byFlow(variables.integrationFlowId) 
      });
    },
  });
};

/**
 * Hook to update a field mapping with optimistic update
 */
export const useUpdateFieldMapping = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ mappingId, data }: { mappingId: string; data: UpdateFieldMappingDTO }) =>
      apiClient.put<FieldMappingDTO>(`/field-mappings/${mappingId}`, data),

    onMutate: async ({ mappingId, data }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: queryKeys.mappings.detail(mappingId) });

      // Get current mapping to find flow ID
      const currentMapping = queryClient.getQueryData<FieldMappingDTO>(
        queryKeys.mappings.detail(mappingId)
      );

      if (!currentMapping) return;

      // Cancel flow mappings query too
      await queryClient.cancelQueries({ 
        queryKey: queryKeys.mappings.byFlow(currentMapping.integrationFlowId) 
      });

      // Snapshot previous values
      const previousMapping = currentMapping;
      const previousMappings = queryClient.getQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(currentMapping.integrationFlowId)
      );

      // Optimistically update single mapping
      const updatedMapping: FieldMappingDTO = {
        ...currentMapping,
        ...data,
        updatedAt: new Date().toISOString(),
      };

      queryClient.setQueryData<FieldMappingDTO>(
        queryKeys.mappings.detail(mappingId),
        updatedMapping
      );

      // Optimistically update list
      queryClient.setQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(currentMapping.integrationFlowId),
        (old) => old?.map(mapping => 
          mapping.id === mappingId ? updatedMapping : mapping
        ) || []
      );

      return { previousMapping, previousMappings, flowId: currentMapping.integrationFlowId };
    },

    onError: (err, { mappingId }, context) => {
      // Rollback on error
      if (context) {
        queryClient.setQueryData(
          queryKeys.mappings.detail(mappingId),
          context.previousMapping
        );
        queryClient.setQueryData(
          queryKeys.mappings.byFlow(context.flowId),
          context.previousMappings
        );
      }
      notify.error('Failed to update field mapping', err.message);
    },

    onSuccess: () => {
      notify.success('Field mapping updated', 'The field mapping has been updated successfully');
    },

    onSettled: (_, __, { mappingId }, context) => {
      // Always refetch to ensure consistency
      queryClient.invalidateQueries({ queryKey: queryKeys.mappings.detail(mappingId) });
      if (context?.flowId) {
        queryClient.invalidateQueries({ 
          queryKey: queryKeys.mappings.byFlow(context.flowId) 
        });
      }
    },
  });
};

/**
 * Hook to delete a field mapping with optimistic update
 */
export const useDeleteFieldMapping = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (mappingId: string) =>
      apiClient.delete(`/field-mappings/${mappingId}`),

    onMutate: async (mappingId) => {
      // Get mapping to find flow ID
      const mapping = queryClient.getQueryData<FieldMappingDTO>(
        queryKeys.mappings.detail(mappingId)
      );

      if (!mapping) return;

      // Cancel outgoing refetches
      await queryClient.cancelQueries({ 
        queryKey: queryKeys.mappings.byFlow(mapping.integrationFlowId) 
      });

      // Snapshot previous value
      const previousMappings = queryClient.getQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(mapping.integrationFlowId)
      );

      // Optimistically remove from list
      queryClient.setQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(mapping.integrationFlowId),
        (old) => old?.filter(m => m.id !== mappingId) || []
      );

      return { previousMappings, flowId: mapping.integrationFlowId };
    },

    onError: (err, mappingId, context) => {
      // Rollback on error
      if (context?.previousMappings && context.flowId) {
        queryClient.setQueryData(
          queryKeys.mappings.byFlow(context.flowId),
          context.previousMappings
        );
      }
      notify.error('Failed to delete field mapping', err.message);
    },

    onSuccess: () => {
      notify.success('Field mapping deleted', 'The field mapping has been removed successfully');
    },

    onSettled: (_, __, mappingId, context) => {
      // Always refetch to ensure consistency
      if (context?.flowId) {
        queryClient.invalidateQueries({ 
          queryKey: queryKeys.mappings.byFlow(context.flowId) 
        });
      }
      // Remove from cache
      queryClient.removeQueries({ queryKey: queryKeys.mappings.detail(mappingId) });
    },
  });
};

/**
 * Hook to reorder field mappings with optimistic update
 */
export const useReorderFieldMappings = () => {
  const notify = useNotify();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ flowId, mappingOrders }: { 
      flowId: string; 
      mappingOrders: { id: string; sequence: number }[] 
    }) =>
      apiClient.put<FieldMappingDTO[]>(`/field-mappings/flow/${flowId}/reorder`, { 
        mappingOrders 
      }),

    onMutate: async ({ flowId, mappingOrders }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ 
        queryKey: queryKeys.mappings.byFlow(flowId) 
      });

      // Snapshot previous value
      const previousMappings = queryClient.getQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(flowId)
      );

      // Optimistically update order
      queryClient.setQueryData<FieldMappingDTO[]>(
        queryKeys.mappings.byFlow(flowId),
        (old) => {
          if (!old) return [];
          
          // Create a map of new sequences
          const sequenceMap = new Map(
            mappingOrders.map(order => [order.id, order.sequence])
          );
          
          // Update sequences and sort
          return [...old]
            .map(mapping => ({
              ...mapping,
              sequence: sequenceMap.get(mapping.id) ?? mapping.sequence,
            }))
            .sort((a, b) => a.sequence - b.sequence);
        }
      );

      return { previousMappings, flowId };
    },

    onError: (err, { flowId }, context) => {
      // Rollback on error
      if (context?.previousMappings) {
        queryClient.setQueryData(
          queryKeys.mappings.byFlow(flowId),
          context.previousMappings
        );
      }
      notify.error('Failed to reorder field mappings', err.message);
    },

    onSuccess: () => {
      notify.success('Field mappings reordered', 'The field mapping order has been updated');
    },

    onSettled: (_, __, { flowId }) => {
      // Always refetch to ensure consistency
      queryClient.invalidateQueries({ 
        queryKey: queryKeys.mappings.byFlow(flowId) 
      });
    },
  });
};