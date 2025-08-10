import { create } from 'zustand';

/**
 * Notification types
 */
export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * Notification interface
 */
export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message?: string;
  duration?: number;
  dismissible?: boolean;
  action?: {
    label: string;
    onClick: () => void;
  };
}

/**
 * Notification state interface
 */
interface NotificationState {
  notifications: Notification[];
  addNotification: (notification: Omit<Notification, 'id'>) => void;
  removeNotification: (id: string) => void;
  clearNotifications: () => void;
}

/**
 * Default notification duration (ms)
 */
const DEFAULT_DURATION = 5000;

/**
 * Notification store
 */
export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],

  addNotification: (notification) => {
    const id = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const newNotification: Notification = {
      ...notification,
      id,
      duration: notification.duration ?? DEFAULT_DURATION,
      dismissible: notification.dismissible ?? true,
    };

    set((state) => ({
      notifications: [...state.notifications, newNotification],
    }));

    // Auto-remove after duration
    if (newNotification.duration && newNotification.duration > 0) {
      setTimeout(() => {
        get().removeNotification(id);
      }, newNotification.duration);
    }
  },

  removeNotification: (id) => {
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },

  clearNotifications: () => {
    set({ notifications: [] });
  },
}));

/**
 * Convenience hooks for different notification types
 */
export const useNotify = () => {
  const addNotification = useNotificationStore((state) => state.addNotification);

  return {
    success: (title: string, message?: string, options?: Partial<Notification>) =>
      addNotification({ ...options, type: 'success', title, message }),
    
    error: (title: string, message?: string, options?: Partial<Notification>) =>
      addNotification({ ...options, type: 'error', title, message, duration: 0 }), // Errors don't auto-dismiss
    
    warning: (title: string, message?: string, options?: Partial<Notification>) =>
      addNotification({ ...options, type: 'warning', title, message }),
    
    info: (title: string, message?: string, options?: Partial<Notification>) =>
      addNotification({ ...options, type: 'info', title, message }),
  };
};