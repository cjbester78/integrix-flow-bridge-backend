// @ts-nocheck
import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';
import { useAuthStore } from '@/stores/auth-store';
// Removed logger import to avoid circular dependency

/**
 * API error type with consistent structure
 */
export interface ApiError {
  message: string;
  code?: string;
  details?: Record<string, unknown>;
  timestamp: string;
}

/**
 * Type-safe API response wrapper
 */
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
}

/**
 * Paginated response type
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Request configuration with additional options
 */
export interface ApiRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
  retryCount?: number;
}

class ApiClient {
  private client: AxiosInstance;
  private baseURL: string;

  constructor() {
    // Use localhost for development
    this.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
    
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        // Get token from localStorage (where authService stores it)
        const token = localStorage.getItem('integration_platform_token');
        
        console.log('API Request - Token present:', !!token);
        
        if (token && !(config as ApiRequestConfig).skipAuth) {
          config.headers.Authorization = `Bearer ${token}`;
          console.log('API Request - Adding Authorization header');
        } else {
          console.log('API Request - No token or skipAuth=true');
        }
        
        // Add correlation ID for tracking
        const correlationId = this.generateCorrelationId();
        config.headers['X-Correlation-ID'] = correlationId;
        
        // Store correlation ID globally for logger
        (window as any).__currentCorrelationId = correlationId;
        
        // Logger removed to avoid circular dependency
        console.log(`API Call: ${config.method?.toUpperCase() || 'GET'} ${config.url || ''}`);
        console.log('Request headers:', config.headers);
        
        return config;
      },
      (error) => {
        return Promise.reject(this.handleError(error));
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => {
        // Logger removed to avoid circular dependency
        console.log(`API Response: ${response.config.method?.toUpperCase()} ${response.config.url} - ${response.status}`);
        return response;
      },
      async (error: AxiosError<ApiError>) => {
        const originalRequest = error.config as ApiRequestConfig;
        
        // Handle 401 Unauthorized
        if (error.response?.status === 401 && !originalRequest.skipAuth) {
          const refreshToken = localStorage.getItem('integration_platform_refresh_token');
          
          // Try to refresh token
          if (refreshToken && !originalRequest.url?.includes('/refresh')) {
            try {
              // Call refresh endpoint
              const refreshResponse = await this.client.post('/auth/refresh', { refreshToken }, {
                skipAuth: true
              } as ApiRequestConfig);
              
              // Store new token
              const newToken = refreshResponse.data.token;
              localStorage.setItem('integration_platform_token', newToken);
              
              // Retry original request with new token
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
              }
              
              return this.client(originalRequest);
            } catch (refreshError) {
              // Refresh failed, logout user
              console.error('Token refresh failed:', refreshError);
              localStorage.removeItem('integration_platform_token');
              localStorage.removeItem('integration_platform_refresh_token');
              localStorage.removeItem('integration_platform_user');
              window.location.href = '/login';
            }
          } else {
            // No refresh token available, logout
            localStorage.removeItem('integration_platform_token');
            localStorage.removeItem('integration_platform_refresh_token');
            localStorage.removeItem('integration_platform_user');
            window.location.href = '/login';
          }
        }
        
        return Promise.reject(this.handleError(error));
      }
    );
  }

  private generateCorrelationId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  private handleError(error: AxiosError<ApiError>): ApiError {
    // Logger removed to avoid circular dependency
    console.error(`API Error: ${error.config?.method?.toUpperCase() || 'UNKNOWN'} ${error.config?.url || 'unknown'}`, error);
    
    if (error.response?.data) {
      return {
        message: error.response.data.message || 'An error occurred',
        code: error.response.data.code || error.response.status?.toString(),
        details: error.response.data.details,
        timestamp: error.response.data.timestamp || new Date().toISOString(),
      };
    }
    
    if (error.request) {
      return {
        message: 'No response from server. Please check your connection.',
        code: 'NETWORK_ERROR',
        timestamp: new Date().toISOString(),
      };
    }
    
    return {
      message: error.message || 'An unexpected error occurred',
      code: 'UNKNOWN_ERROR',
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * GET request
   */
  async get<T>(url: string, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.get<T>(url, config);
    return response.data;
  }

  /**
   * POST request
   */
  async post<T, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.post<T>(url, data, config);
    return response.data;
  }

  /**
   * PUT request
   */
  async put<T, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.put<T>(url, data, config);
    return response.data;
  }

  /**
   * PATCH request
   */
  async patch<T, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  /**
   * DELETE request
   */
  async delete<T>(url: string, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }

  /**
   * Upload file
   */
  async upload<T>(url: string, formData: FormData, config?: ApiRequestConfig): Promise<T> {
    const response = await this.client.post<T>(url, formData, {
      ...config,
      headers: {
        ...config?.headers,
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  /**
   * Download file
   */
  async download(url: string, filename: string, config?: ApiRequestConfig): Promise<void> {
    const response = await this.client.get(url, {
      ...config,
      responseType: 'blob',
    });
    
    const blob = new Blob([response.data]);
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(link.href);
  }
}

// Export singleton instance
export const apiClient = new ApiClient();

// Export types
export type { AxiosError as ApiClientError };