import axios, { AxiosError } from 'axios';
import { authService } from '@/services/authService';

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: AxiosError | null, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  
  failedQueue = [];
};

// Flag to prevent redirect loops
let redirectInProgress = false;

/**
 * Setup axios interceptors for authentication
 */
export const setupAxiosInterceptors = () => {
  // Set default headers
  axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
  
  // Request interceptor to add auth token
  axios.interceptors.request.use(
    (config) => {
      const token = authService.getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      // Ensure X-Requested-With header is always present
      config.headers['X-Requested-With'] = 'XMLHttpRequest';
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Response interceptor to handle 401 errors
  axios.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as any;
      
      // If there's no response or config, just reject
      if (!error.response || !originalRequest) {
        return Promise.reject(error);
      }

      // Skip interceptor for auth endpoints
      const isAuthEndpoint = originalRequest.url?.includes('/auth/');
      
      // If we're already redirecting, don't do anything
      if (redirectInProgress) {
        return Promise.reject(error);
      }
      
      // Handle 401 errors for non-auth endpoints
      if (error.response?.status === 401 && 
          !originalRequest._retry && 
          !isAuthEndpoint) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axios(originalRequest);
          }).catch(err => {
            return Promise.reject(err);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        return new Promise((resolve, reject) => {
          authService.refreshToken()
            .then((response) => {
              if (response.success && response.data) {
                authService.setToken(response.data.token);
                axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
                originalRequest.headers.Authorization = `Bearer ${response.data.token}`;
                processQueue(null, response.data.token);
                resolve(axios(originalRequest));
              } else {
                throw new Error('Token refresh failed');
              }
            })
            .catch((err) => {
              processQueue(err, null);
              
              // Set redirect flag to prevent loops
              redirectInProgress = true;
              
              // Clear auth data without API call
              localStorage.removeItem('integration_platform_token');
              localStorage.removeItem('integration_platform_refresh_token');
              localStorage.removeItem('integration_platform_user');
              
              // Only redirect if not already on login page
              if (window.location.pathname !== '/login') {
                // Use replace to avoid history issues
                window.location.replace('/login');
              } else {
                // Reset flag if already on login page
                redirectInProgress = false;
              }
              
              reject(err);
            })
            .finally(() => {
              isRefreshing = false;
            });
        });
      }

      return Promise.reject(error);
    }
  );
};

/**
 * Add a response interceptor specifically for session validation
 */
export const addSessionInterceptor = (onSessionExpired: () => void) => {
  const interceptorId = axios.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      if (error.response?.status === 401) {
        // Session expired or invalid
        onSessionExpired();
      }
      return Promise.reject(error);
    }
  );

  // Return function to remove interceptor
  return () => axios.interceptors.response.eject(interceptorId);
};