/**
 * Get the base URL for API and WebSocket connections
 * Uses the provided backend IP address
 */
export function getBaseUrl() {
  // Use environment variables if available
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }
  
  // Use localhost for development
  return 'http://localhost:8080/api';
}

/**
 * Get WebSocket URL based on backend IP
 */
export function getWebSocketUrl() {
  // Use environment variable if available
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }
  
  // Use localhost for development
  return 'ws://localhost:8080';
}

/**
 * Get API base URL without the /api suffix
 */
export function getApiBaseUrl() {
  const baseUrl = getBaseUrl();
  return baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl;
}