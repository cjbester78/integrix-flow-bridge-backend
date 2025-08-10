import { CheckCircle, XCircle, Clock, Info, AlertTriangle } from 'lucide-react';

export const getStatusIcon = (status: string) => {
  switch (status) {
    case 'success':
      return <CheckCircle className="h-4 w-4 text-success" />;
    case 'failed':
      return <XCircle className="h-4 w-4 text-destructive" />;
    case 'processing':
      return <Clock className="h-4 w-4 text-warning animate-spin" />;
    default:
      return <Info className="h-4 w-4 text-muted-foreground" />;
  }
};

export const getLogLevelIcon = (level: string) => {
  switch (level.toUpperCase()) {
    case 'SUCCESS':
      return <CheckCircle className="h-3 w-3 text-success" />;
    case 'ERROR':
      return <XCircle className="h-3 w-3 text-destructive" />;
    case 'WARN':
      return <AlertTriangle className="h-3 w-3 text-warning" />;
    default:
      return <Info className="h-3 w-3 text-info" />;
  }
};