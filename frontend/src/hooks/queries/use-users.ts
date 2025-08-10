import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { queryClient, queryKeys, invalidateRelatedQueries } from '@/lib/query-client-simple';
import { useNotify } from '@/stores/notification-store';
import { User } from '@/stores/auth-store';

/**
 * User creation/update DTOs
 */
interface CreateUserDTO {
  username: string;
  email: string;
  password: string;
  fullName: string;
  roleId: string;
}

interface UpdateUserDTO {
  email?: string;
  fullName?: string;
  roleId?: string;
  isActive?: boolean;
}

interface UserListParams {
  page?: number;
  size?: number;
  search?: string;
  roleId?: string;
  isActive?: boolean;
}

/**
 * Hook to fetch all users with pagination and filtering
 */
export const useUsers = (params?: UserListParams) => {
  return useQuery({
    queryKey: queryKeys.users.list(params || {}),
    queryFn: () => apiClient.get<User[]>('/users', { params }),
  });
};

/**
 * Hook to fetch a single user by ID
 */
export const useUser = (userId: string, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.users.detail(userId),
    queryFn: () => apiClient.get<User>(`/users/${userId}`),
    enabled: enabled && !!userId,
  });
};

/**
 * Hook to create a new user
 */
export const useCreateUser = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (data: CreateUserDTO) => 
      apiClient.post<User>('/users', data),
    
    onSuccess: (user) => {
      notify.success('User created', `User ${user.username} has been created successfully`);
      invalidateRelatedQueries('user');
    },
    
    onError: (error: any) => {
      notify.error('Failed to create user', error.message);
    },
  });
};

/**
 * Hook to update a user
 */
export const useUpdateUser = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: ({ userId, data }: { userId: string; data: UpdateUserDTO }) =>
      apiClient.put<User>(`/users/${userId}`, data),
    
    onSuccess: (user) => {
      notify.success('User updated', `User ${user.username} has been updated successfully`);
      invalidateRelatedQueries('user', user.id);
    },
    
    onError: (error: any) => {
      notify.error('Failed to update user', error.message);
    },
  });
};

/**
 * Hook to delete a user
 */
export const useDeleteUser = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: (userId: string) =>
      apiClient.delete(`/users/${userId}`),
    
    onSuccess: (_, userId) => {
      notify.success('User deleted', 'User has been deleted successfully');
      invalidateRelatedQueries('user', userId);
    },
    
    onError: (error: any) => {
      notify.error('Failed to delete user', error.message);
    },
  });
};

/**
 * Hook to reset user password
 */
export const useResetUserPassword = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: ({ userId, newPassword }: { userId: string; newPassword: string }) =>
      apiClient.post(`/users/${userId}/reset-password`, { newPassword }),
    
    onSuccess: () => {
      notify.success('Password reset', 'User password has been reset successfully');
    },
    
    onError: (error: any) => {
      notify.error('Failed to reset password', error.message);
    },
  });
};

/**
 * Hook for optimistic user status toggle
 */
export const useToggleUserStatus = () => {
  const notify = useNotify();
  
  return useMutation({
    mutationFn: ({ userId, isActive }: { userId: string; isActive: boolean }) =>
      apiClient.patch<User>(`/users/${userId}/status`, { isActive }),
    
    onMutate: async ({ userId, isActive }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: queryKeys.users.detail(userId) });
      
      // Snapshot previous value
      const previousUser = queryClient.getQueryData<User>(queryKeys.users.detail(userId));
      
      // Optimistically update
      if (previousUser) {
        queryClient.setQueryData<User>(queryKeys.users.detail(userId), {
          ...previousUser,
          isActive,
        });
      }
      
      return { previousUser };
    },
    
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousUser) {
        queryClient.setQueryData(
          queryKeys.users.detail(variables.userId),
          context.previousUser
        );
      }
      notify.error('Failed to update user status', err.message);
    },
    
    onSettled: (_, __, { userId }) => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() });
    },
    
    onSuccess: (_, { isActive }) => {
      notify.success(
        'User status updated',
        `User has been ${isActive ? 'activated' : 'deactivated'}`
      );
    },
  });
};