// @ts-nocheck
// Base API configuration and utilities
import { systemErrorLogger } from './systemErrorLogger';

// Use the backend IP address
const API_BASE_URL = 'http://localhost:8080/api';

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public response?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// Token management functions
function getToken(): string | null {
  return localStorage.getItem('integration_platform_token');
}

function getRefreshToken(): string | null {
  return localStorage.getItem('integration_platform_refresh_token');
}

function removeTokens(): void {
  localStorage.removeItem('integration_platform_token');
  localStorage.removeItem('integration_platform_refresh_token');
  localStorage.removeItem('integration_platform_user');
}

// Token refresh function
async function refreshAuthToken(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return false;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('integration_platform_token', data.token);
      return true;
    }
  } catch (error) {
    console.warn('Token refresh failed:', error);
  }

  removeTokens();
  return false;
}

// Generic API request function
export async function apiRequest<T = any>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  // Prepare headers with authorization if token exists
  const token = getToken();
  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  
  if (token && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/register') && !endpoint.includes('/auth/refresh')) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  const requestOptions: RequestInit = {
    headers: { ...defaultHeaders, ...options.headers },
    ...options,
  };

  try {
    console.log('[API] Making request:', {
      method: requestOptions.method || 'GET',
      url,
      body: requestOptions.body ? JSON.parse(requestOptions.body as string) : undefined
    });
    
    let response = await fetch(url, requestOptions);

    // Handle token expiration and retry with refreshed token
    if (response.status === 401 && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/register') && !endpoint.includes('/auth/refresh')) {
      const refreshed = await refreshAuthToken();
      if (refreshed) {
        const newToken = getToken();
        if (newToken) {
          requestOptions.headers = {
            ...requestOptions.headers,
            'Authorization': `Bearer ${newToken}`,
          };
          response = await fetch(url, requestOptions);
        }
      } else {
        // Redirect to login if refresh fails
        window.location.href = '/login';
        throw new ApiError('Authentication failed', 401);
      }
    }

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      const errorDetails = {
        status: response.status,
        endpoint,
        data
      };
      
      console.log('[API] Error response from backend:', errorDetails);
      
      // Also log to system logs
      systemErrorLogger.logError(
        `API Error: ${endpoint} - ${response.status}`,
        {
          ...errorDetails,
          method: options.method || 'GET',
          url,
          timestamp: new Date().toISOString()
        }
      );
      
      throw new ApiError(
        data.message || data || `HTTP ${response.status}: ${response.statusText}`,
        response.status,
        data
      );
    }

    // If we got HTML when expecting JSON, it's likely an error page
    if (typeof data === 'string' && data.includes('<!DOCTYPE html>')) {
      console.warn(`API endpoint ${endpoint} returned HTML instead of JSON`);
      return {
        success: false,
        error: 'API endpoint returned HTML instead of JSON',
        data: null
      };
    }

    return {
      success: true,
      data: data.data || data,
      message: data.message,
    };
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    return {
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error occurred',
    };
  }
}

// HTTP method helpers
export const api = {
  get: <T = any>(endpoint: string, options?: RequestInit) =>
    apiRequest<T>(endpoint, { method: 'GET', ...options }),

  post: <T = any>(endpoint: string, data?: any, options?: RequestInit) =>
    apiRequest<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
      ...options,
    }),

  put: <T = any>(endpoint: string, data?: any, options?: RequestInit) =>
    apiRequest<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
      ...options,
    }),

  patch: <T = any>(endpoint: string, data?: any, options?: RequestInit) =>
    apiRequest<T>(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(data),
      ...options,
    }),

  delete: <T = any>(endpoint: string, options?: RequestInit) =>
    apiRequest<T>(endpoint, { method: 'DELETE', ...options }),
};