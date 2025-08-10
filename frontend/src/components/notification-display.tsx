// @ts-nocheck
import React from 'react';
import { X, CheckCircle, XCircle, AlertTriangle, Info } from 'lucide-react';
import { useNotificationStore, type Notification } from '@/stores/notification-store';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { motion, AnimatePresence } from 'framer-motion';
import { useAnimations } from '@/stores/ui-store';

/**
 * Individual notification component
 */
const NotificationItem: React.FC<{ notification: Notification }> = ({ notification }) => {
  const removeNotification = useNotificationStore((state) => state.removeNotification);
  const animations = useAnimations();

  const icons = {
    success: <CheckCircle className="h-5 w-5" />,
    error: <XCircle className="h-5 w-5" />,
    warning: <AlertTriangle className="h-5 w-5" />,
    info: <Info className="h-5 w-5" />,
  };

  const colors = {
    success: 'bg-green-50 text-green-900 border-green-200 dark:bg-green-900/20 dark:text-green-100 dark:border-green-800',
    error: 'bg-red-50 text-red-900 border-red-200 dark:bg-red-900/20 dark:text-red-100 dark:border-red-800',
    warning: 'bg-yellow-50 text-yellow-900 border-yellow-200 dark:bg-yellow-900/20 dark:text-yellow-100 dark:border-yellow-800',
    info: 'bg-blue-50 text-blue-900 border-blue-200 dark:bg-blue-900/20 dark:text-blue-100 dark:border-blue-800',
  };

  const iconColors = {
    success: 'text-green-600 dark:text-green-400',
    error: 'text-red-600 dark:text-red-400',
    warning: 'text-yellow-600 dark:text-yellow-400',
    info: 'text-blue-600 dark:text-blue-400',
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: -20, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, scale: 0.95, transition: { duration: 0.2 } }}
      transition={{ duration: animations.enabled ? 0.3 : 0 }}
      className={cn(
        'flex items-start gap-3 p-4 rounded-lg border shadow-lg backdrop-blur-sm',
        colors[notification.type],
        'min-w-[320px] max-w-[420px]'
      )}
    >
      <div className={cn('flex-shrink-0', iconColors[notification.type])}>
        {icons[notification.type]}
      </div>
      
      <div className="flex-1 space-y-1">
        <h4 className="text-sm font-semibold">{notification.title}</h4>
        {notification.message && (
          <p className="text-sm opacity-90">{notification.message}</p>
        )}
        {notification.action && (
          <Button
            variant="link"
            size="sm"
            onClick={notification.action.onClick}
            className="p-0 h-auto font-medium"
          >
            {notification.action.label}
          </Button>
        )}
      </div>

      {notification.dismissible && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => removeNotification(notification.id)}
          className="flex-shrink-0 h-auto p-1 hover:bg-black/10 dark:hover:bg-white/10"
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </motion.div>
  );
};

/**
 * Notification container that displays all active notifications
 */
export const NotificationDisplay: React.FC = () => {
  const notifications = useNotificationStore((state) => state.notifications);
  const animations = useAnimations();

  return (
    <div
      className="fixed top-4 right-4 z-[100] space-y-2"
      aria-live="polite"
      aria-atomic="false"
    >
      <AnimatePresence mode="sync">
        {notifications.map((notification) => (
          <NotificationItem
            key={notification.id}
            notification={notification}
          />
        ))}
      </AnimatePresence>
    </div>
  );
};