// Base API configuration and utilities
const API_BASE_URL = 'http://localhost:8080/api';

// Mock data for dashboard endpoints and auth
const mockData = {
  '/auth/login': {
    success: true,
    data: {
      user: {
        id: 'mock-admin-1',
        username: 'admin',
        email: 'admin@integrixlab.com',
        firstName: 'Admin',
        lastName: 'User',
        role: 'administrator',
        status: 'active',
        permissions: ['flows:read', 'flows:write', 'users:read', 'users:write', 'admin:read', 'admin:write'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
        lastLoginAt: new Date().toISOString()
      },
      token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb2NrLWFkbWluLTEiLCJyb2xlIjoiYWRtaW5pc3RyYXRvciIsImV4cCI6OTk5OTk5OTk5OX0.mock',
      refreshToken: 'mock-refresh-token-admin',
      expiresIn: 3600
    }
  },
  '/auth/profile': {
    success: true,
    data: {
      id: 'mock-admin-1',
      username: 'admin',
      email: 'admin@integrixlab.com',
      firstName: 'Admin',
      lastName: 'User',
      role: 'administrator',
      status: 'active',
      permissions: ['flows:read', 'flows:write', 'users:read', 'users:write', 'admin:read', 'admin:write'],
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
      lastLoginAt: new Date().toISOString()
    }
  },
  '/business-components': {
    success: true,
    data: [
      {
        id: "1",
        name: "ACME Corporation",
        description: "Large enterprise client",
        contactEmail: "contact@acme.com",
        contactPhone: "+1-555-0123",
        createdAt: "2024-01-15T10:00:00Z",
        updatedAt: "2024-01-15T10:00:00Z"
      },
      {
        id: "2",
        name: "TechStart Inc",
        description: "Growing technology startup",
        contactEmail: "hello@techstart.com",
        contactPhone: "+1-555-0456",
        createdAt: "2024-01-16T14:30:00Z",
        updatedAt: "2024-01-16T14:30:00Z"
      }
    ]
  },
  '/adapters': {
    success: true,
    data: []
  },
  '/flows': {
    success: true,
    data: []
  },
  '/channels': {
    success: true,
    data: []
  },
  '/messages': {
    success: true,
    data: []
  },
  '/users': {
    success: true,
    data: {
      users: [
        {
          id: '1',
          username: 'admin',
          email: 'admin@integrixlab.com',
          first_name: 'System',
          last_name: 'Administrator',
          role: 'administrator',
          status: 'active',
          permissions: {
            flows: ['create', 'read', 'update', 'delete', 'execute'],
            adapters: ['create', 'read', 'update', 'delete', 'test'],
            structures: ['create', 'read', 'update', 'delete'],
            users: ['create', 'read', 'update', 'delete'],
            system: ['create', 'read', 'update', 'delete']
          },
          email_verified: true,
          created_at: '2024-01-01 09:00:00',
          updated_at: '2024-01-01 09:00:00',
          last_login_at: '2024-01-15 14:30:25'
        },
        {
          id: '2',
          username: 'integrator1',
          email: 'integrator1@company.com',
          first_name: 'John',
          last_name: 'Integrator',
          role: 'integrator',
          status: 'active',
          permissions: {
            flows: ['create', 'read', 'update', 'execute'],
            adapters: ['create', 'read', 'update', 'test'],
            structures: ['create', 'read', 'update']
          },
          email_verified: true,
          created_at: '2024-01-05 10:30:00',
          updated_at: '2024-01-05 10:30:00',
          last_login_at: '2024-01-15 12:15:30'
        },
        {
          id: '3',
          username: 'viewer1',
          email: 'viewer1@company.com',
          first_name: 'Jane',
          last_name: 'Viewer',
          role: 'viewer',
          status: 'inactive',
          permissions: {
            flows: ['read'],
            adapters: ['read'],
            structures: ['read']
          },
          email_verified: true,
          created_at: '2024-01-08 14:20:00',
          updated_at: '2024-01-08 14:20:00',
          last_login_at: '2024-01-10 16:45:12'
        }
      ],
      total: 3,
      page: 0,
      limit: 50
    }
  },
  '/system/stats': {
    success: true,
    data: {
      totalUsers: 5,
      activeFlows: 12,
      totalMessages: 1523,
      systemHealth: 'healthy'
    }
  }
};

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
      throw new ApiError(
        data.message || data || `HTTP ${response.status}: ${response.statusText}`,
        response.status,
        data
      );
    }

    return {
      success: true,
      data: data.data || data,
      message: data.message,
    };
  } catch (error) {
    // Fall back to mock data if API is not available
    // Check for exact endpoint match first, then check if endpoint starts with a mock path
    const mockKey = Object.keys(mockData).find(key => {
      if (endpoint === key) return true;
      if (endpoint.startsWith(key) && endpoint.includes('?')) return true;
      return false;
    });
    
    if (mockKey) {
      console.log(`API not available, using mock data for ${endpoint}`);
      return mockData[mockKey as keyof typeof mockData] as ApiResponse<T>;
    }

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
