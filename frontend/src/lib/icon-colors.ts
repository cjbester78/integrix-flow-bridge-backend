// Standardized icon colors using theme tokens
export const iconColors = {
  // Status colors
  success: 'text-success',
  error: 'text-destructive',
  warning: 'text-warning',
  info: 'text-info',
  
  // State colors
  active: 'text-primary',
  inactive: 'text-muted-foreground',
  pending: 'text-warning',
  processing: 'text-info',
  
  // Severity colors for logs
  debug: 'text-muted-foreground',
  warn: 'text-warning',
  critical: 'text-destructive',
  
  // Default
  default: 'text-foreground',
  muted: 'text-muted-foreground'
} as const;

// Helper function to get icon color class
export const getIconColor = (type: keyof typeof iconColors): string => {
  return iconColors[type] || iconColors.default;
};

// Status-specific icon color mapping
export const getStatusIconColor = (status: string): string => {
  const statusMap: Record<string, keyof typeof iconColors> = {
    // General statuses
    'success': 'success',
    'completed': 'success',
    'active': 'success',
    'acknowledged': 'success',
    
    'error': 'error',
    'failed': 'error',
    'rejected': 'error',
    
    'warning': 'warning',
    'pending': 'pending',
    'expired': 'warning',
    
    'processing': 'processing',
    'running': 'processing',
    'requeued': 'processing',
    
    'info': 'info',
    'paused': 'info',
    'stopped': 'muted',
    'inactive': 'muted',
    
    // Default
    'default': 'default'
  };
  
  return getIconColor(statusMap[status.toLowerCase()] || 'default');
};