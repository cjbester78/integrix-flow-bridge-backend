import { api, ApiResponse } from './api';
import { Role } from '@/types/admin';

export interface CreateRoleRequest {
  name: string;
  description: string;
  permissions: string[];
}

export interface UpdateRoleRequest extends Partial<CreateRoleRequest> {
  id: string;
}

export interface RoleListResponse {
  roles: Role[];
  total: number;
  page: number;
  limit: number;
}

class RoleService {
  // Get all roles
  async getAllRoles(page: number = 0, limit: number = 50): Promise<ApiResponse<RoleListResponse>> {
    return api.get<RoleListResponse>(`/roles?page=${page}&limit=${limit}`);
  }

  // Get role by ID
  async getRoleById(roleId: string): Promise<ApiResponse<Role>> {
    return api.get<Role>(`/roles/${roleId}`);
  }

  // Create new role
  async createRole(roleData: CreateRoleRequest): Promise<ApiResponse<Role>> {
    return api.post<Role>('/roles', roleData);
  }

  // Update role
  async updateRole(roleId: string, updates: Partial<CreateRoleRequest>): Promise<ApiResponse<Role>> {
    return api.put<Role>(`/roles/${roleId}`, updates);
  }

  // Delete role
  async deleteRole(roleId: string): Promise<ApiResponse<void>> {
    return api.delete(`/roles/${roleId}`);
  }

  // Add permission to role
  async addPermission(roleId: string, permission: string): Promise<ApiResponse<Role>> {
    return api.post<Role>(`/roles/${roleId}/permissions`, { permission });
  }

  // Remove permission from role
  async removePermission(roleId: string, permission: string): Promise<ApiResponse<Role>> {
    return api.delete(`/roles/${roleId}/permissions/${permission}`);
  }

  // Update role permissions
  async updatePermissions(roleId: string, permissions: string[]): Promise<ApiResponse<Role>> {
    return api.put<Role>(`/roles/${roleId}/permissions`, { permissions });
  }

  // Get role permissions
  async getRolePermissions(roleId: string): Promise<ApiResponse<{ permissions: string[] }>> {
    return api.get<{ permissions: string[] }>(`/roles/${roleId}/permissions`);
  }

  // Search roles
  async searchRoles(query: string, filters?: {
    hasPermission?: string;
    page?: number;
    limit?: number;
  }): Promise<ApiResponse<RoleListResponse>> {
    const params = new URLSearchParams({
      q: query,
      ...(filters?.hasPermission && { hasPermission: filters.hasPermission }),
      page: (filters?.page || 1).toString(),
      limit: (filters?.limit || 50).toString()
    });

    return api.get<RoleListResponse>(`/roles/search?${params}`);
  }

  // Get users assigned to role
  async getRoleUsers(roleId: string): Promise<ApiResponse<{ users: any[]; total: number }>> {
    return api.get(`/roles/${roleId}/users`);
  }

  // Clone an existing role
  async cloneRole(roleId: string, newName: string): Promise<ApiResponse<Role>> {
    return api.post<Role>(`/roles/${roleId}/clone`, { name: newName });
  }

  // Get available permissions
  async getAvailablePermissions(): Promise<ApiResponse<{ 
    permissions: Array<{
      name: string;
      description: string;
      category: string;
    }>;
  }>> {
    return api.get('/roles/available-permissions');
  }

  // Validate role permissions
  async validatePermissions(permissions: string[]): Promise<ApiResponse<{
    valid: boolean;
    invalidPermissions: string[];
    suggestions: string[];
  }>> {
    return api.post('/roles/validate-permissions', { permissions });
  }
}

export const roleService = new RoleService();