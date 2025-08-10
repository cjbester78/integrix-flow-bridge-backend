import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { CheckCircle, XCircle, Clock, AlertCircle, PlayCircle, PauseCircle } from 'lucide-react';

type StatusType = 'success' | 'error' | 'warning' | 'info' | 'default' | 'running' | 'paused' | 'pending';

interface StatusBadgeProps {
  status: string;
  type?: StatusType;
  showIcon?: boolean;
  className?: string;
}

const statusConfig: Record<StatusType, { variant: any; icon: any; className: string }> = {
  success: {
    variant: 'success',
    icon: CheckCircle,
    className: 'text-success'
  },
  error: {
    variant: 'destructive',
    icon: XCircle,
    className: 'text-destructive'
  },
  warning: {
    variant: 'warning',
    icon: AlertCircle,
    className: 'text-warning'
  },
  info: {
    variant: 'secondary',
    icon: AlertCircle,
    className: 'text-info'
  },
  running: {
    variant: 'default',
    icon: PlayCircle,
    className: 'text-success'
  },
  paused: {
    variant: 'secondary',
    icon: PauseCircle,
    className: 'text-muted-foreground'
  },
  pending: {
    variant: 'secondary',
    icon: Clock,
    className: 'text-warning'
  },
  default: {
    variant: 'secondary',
    icon: null,
    className: ''
  }
};

export const StatusBadge = ({ 
  status, 
  type = 'default', 
  showIcon = true,
  className 
}: StatusBadgeProps) => {
  const config = statusConfig[type] || statusConfig.default;
  const Icon = config.icon;

  return (
    <Badge 
      variant={config.variant}
      className={cn("flex items-center gap-1.5", className)}
    >
      {showIcon && Icon && (
        <Icon className={cn("h-3 w-3", config.className)} />
      )}
      {status}
    </Badge>
  );
};