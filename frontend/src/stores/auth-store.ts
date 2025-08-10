import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { apiClient } from '@/lib/api-client';
// Removed logger import to avoid circular dependency with logger.ts

/**
 * User type based on backend UserDTO
 */
export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: {
    id: string;
    name: string;
    permissions: string[];
  };
  isActive: boolean;
  lastLogin?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Authentication state interface
 */
interface AuthState {
  // State
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshAccessToken: () => Promise<void>;
  updateUser: (user: Partial<User>) => void;
  checkAuth: () => Promise<void>;
  clearError: () => void;
}

/**
 * Login response from backend
 */
interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

/**
 * Refresh token response
 */
interface RefreshResponse {
  token: string;
  expiresIn: number;
}

/**
 * Auth store with persistence
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Login action
      login: async (username: string, password: string) => {
        set({ isLoading: true, error: null });
        
        // Logger removed to avoid circular dependency
        console.log('Login attempt:', username);
        
        try {
          const response = await apiClient.post<LoginResponse>('/auth/login', {
            username,
            password,
          }, {
            skipAuth: true,
          });

          set({
            user: response.user,
            token: response.token,
            refreshToken: response.refreshToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          // Logger removed to avoid circular dependency
          console.log('Login successful:', response.user.username);

          // Set token expiration reminder
          const expirationTime = response.expiresIn * 1000 * 0.9; // Refresh at 90% of expiration
          setTimeout(() => {
            get().refreshAccessToken();
          }, expirationTime);

        } catch (error: any) {
          // Logger removed to avoid circular dependency
          console.error('Login failed:', error);
          
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: error.message || 'Login failed',
          });
          throw error;
        }
      },

      // Logout action
      logout: () => {
        const currentUser = get().user;
        
        // Logger removed to avoid circular dependency
        console.log('User logout:', currentUser?.username);
        
        set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
          error: null,
        });
      },

      // Refresh access token
      refreshAccessToken: async () => {
        const { refreshToken } = get();
        
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        try {
          const response = await apiClient.post<RefreshResponse>('/auth/refresh', {
            refreshToken,
          }, {
            skipAuth: true,
          });

          set({
            token: response.token,
            error: null,
          });

          // Set new expiration reminder
          const expirationTime = response.expiresIn * 1000 * 0.9;
          setTimeout(() => {
            get().refreshAccessToken();
          }, expirationTime);

        } catch (error: any) {
          // Refresh failed, logout user
          get().logout();
          throw error;
        }
      },

      // Update user information
      updateUser: (updates: Partial<User>) => {
        const { user } = get();
        if (user) {
          set({
            user: { ...user, ...updates },
          });
        }
      },

      // Check authentication status
      checkAuth: async () => {
        const { token } = get();
        
        if (!token) {
          set({ isAuthenticated: false });
          return;
        }

        set({ isLoading: true });

        try {
          const user = await apiClient.get<User>('/auth/me');
          set({
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error) {
          // Token is invalid
          get().logout();
          set({ isLoading: false });
        }
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

/**
 * Hook to check if user has specific permission
 */
export const useHasPermission = (permission: string): boolean => {
  const user = useAuthStore((state) => state.user);
  return user?.role.permissions.includes(permission) || false;
};

/**
 * Hook to check if user has any of the specified permissions
 */
export const useHasAnyPermission = (permissions: string[]): boolean => {
  const user = useAuthStore((state) => state.user);
  if (!user) return false;
  
  return permissions.some(permission => 
    user.role.permissions.includes(permission)
  );
};

/**
 * Hook to check if user has all of the specified permissions
 */
export const useHasAllPermissions = (permissions: string[]): boolean => {
  const user = useAuthStore((state) => state.user);
  if (!user) return false;
  
  return permissions.every(permission => 
    user.role.permissions.includes(permission)
  );
};

/**
 * Hook to check if user has specific role
 */
export const useHasRole = (roleName: string): boolean => {
  const user = useAuthStore((state) => state.user);
  return user?.role.name === roleName || false;
};