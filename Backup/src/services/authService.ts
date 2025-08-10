import { api, ApiResponse } from './api';

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'administrator' | 'integrator' | 'viewer';
  status: 'active' | 'inactive' | 'pending';
  permissions: string[] | string;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role?: string;
}

class AuthService {
  private readonly TOKEN_KEY = 'integration_platform_token';
  private readonly REFRESH_TOKEN_KEY = 'integration_platform_refresh_token';
  private readonly USER_KEY = 'integration_platform_user';

  // Login user
  async login(credentials: LoginCredentials): Promise<ApiResponse<LoginResponse>> {
    const response = await api.post<LoginResponse>('/auth/login', credentials);
    
    if (response.success && response.data) {
      this.setAuthData(response.data);
    }
    
    return response;
  }

  // Register new user
  async register(userData: RegisterData): Promise<ApiResponse<User>> {
    return api.post<User>('/auth/register', userData);
  }

  // Logout user
  async logout(): Promise<ApiResponse<void>> {
    try {
      const refreshToken = this.getRefreshToken();
      if (refreshToken) {
        await api.post('/auth/logout', { refreshToken });
      }
    } catch (error) {
      // Continue with logout even if API call fails
      console.warn('Logout API call failed:', error);
    } finally {
      this.clearAuthData();
    }
    
    return { success: true };
  }

  // Refresh authentication token
  async refreshToken(): Promise<ApiResponse<{ token: string; expiresIn: number }>> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return { success: false, error: 'No refresh token available' };
    }

    const response = await api.post<{ token: string; expiresIn: number }>('/auth/refresh', {
      refreshToken
    });

    if (response.success && response.data) {
      localStorage.setItem(this.TOKEN_KEY, response.data.token);
    }

    return response;
  }

  // Get current user profile
  async getProfile(): Promise<ApiResponse<User>> {
    return api.get<User>('/auth/profile');
  }

  // Update user profile
  async updateProfile(updates: Partial<Pick<User, 'firstName' | 'lastName' | 'email'>>): Promise<ApiResponse<User>> {
    return api.put<User>('/auth/profile', updates);
  }

  // Change password
  async changePassword(currentPassword: string, newPassword: string): Promise<ApiResponse<void>> {
    return api.post('/auth/change-password', {
      currentPassword,
      newPassword
    });
  }

  // Request password reset
  async requestPasswordReset(email: string): Promise<ApiResponse<void>> {
    return api.post('/auth/forgot-password', { email });
  }

  // Reset password with token
  async resetPassword(token: string, newPassword: string): Promise<ApiResponse<void>> {
    return api.post('/auth/reset-password', { token, newPassword });
  }

  // Verify email
  async verifyEmail(token: string): Promise<ApiResponse<void>> {
    return api.post('/auth/verify-email', { token });
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp > Date.now() / 1000;
    } catch {
      return false;
    }
  }

  // Get current user from local storage
  getCurrentUser(): User | null {
    try {
      const userStr = localStorage.getItem(this.USER_KEY);
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  }

  // Get authentication token
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  // Get refresh token
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  // Set authentication data
  private setAuthData(loginResponse: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, loginResponse.token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, loginResponse.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(loginResponse.user));
  }

  // Clear authentication data
  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  // Check user permissions
  hasPermission(permission: string): boolean {
    const user = this.getCurrentUser();
    if (!user?.permissions) return false;
    
    // Handle permissions as either string (JSON) or array
    let permissionsArray: string[] = [];
    if (typeof user.permissions === 'string') {
      try {
        const parsed = JSON.parse(user.permissions);
        permissionsArray = Array.isArray(parsed) ? parsed : Object.keys(parsed);
      } catch {
        permissionsArray = [];
      }
    } else if (Array.isArray(user.permissions)) {
      permissionsArray = user.permissions;
    }
    
    return permissionsArray.includes(permission);
  }

  // Check user role
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  // Check if user is admin
  isAdmin(): boolean {
    return this.hasRole('administrator');
  }
}

export const authService = new AuthService();