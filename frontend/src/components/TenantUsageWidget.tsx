import React from 'react';
import { useTenant } from '@/contexts/TenantContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { 
  Activity, 
  MessageSquare, 
  Globe, 
  HardDrive, 
  Users, 
  Workflow,
  AlertCircle,
  CheckCircle
} from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';

interface UsageMetric {
  key: string;
  label: string;
  icon: React.ReactNode;
  value: number;
  quota: number;
  unit?: string;
}

export function TenantUsageWidget() {
  const { usage, subscription, isOverQuota, getQuotaPercentage, loading } = useTenant();

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Usage & Quotas</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!usage || !subscription) {
    return null;
  }

  const metrics: UsageMetric[] = [
    {
      key: 'flows',
      label: 'Integration Flows',
      icon: <Workflow className="h-4 w-4" />,
      value: usage.flows,
      quota: usage.quotas?.flows || -1
    },
    {
      key: 'executions_per_month',
      label: 'Executions',
      icon: <Activity className="h-4 w-4" />,
      value: usage.executions,
      quota: usage.quotas?.executions_per_month || -1,
      unit: '/month'
    },
    {
      key: 'messages_per_month',
      label: 'Messages',
      icon: <MessageSquare className="h-4 w-4" />,
      value: usage.messages,
      quota: usage.quotas?.messages_per_month || -1,
      unit: '/month'
    },
    {
      key: 'api_calls_per_month',
      label: 'API Calls',
      icon: <Globe className="h-4 w-4" />,
      value: usage.apiCalls,
      quota: usage.quotas?.api_calls_per_month || -1,
      unit: '/month'
    },
    {
      key: 'storage_gb',
      label: 'Storage',
      icon: <HardDrive className="h-4 w-4" />,
      value: usage.storageGb,
      quota: usage.quotas?.storage_gb || -1,
      unit: ' GB'
    },
    {
      key: 'users',
      label: 'Users',
      icon: <Users className="h-4 w-4" />,
      value: usage.users,
      quota: usage.quotas?.users || -1
    }
  ];

  const formatValue = (value: number, quota: number): string => {
    if (quota === -1) {
      return value.toLocaleString();
    }
    return `${value.toLocaleString()} / ${quota.toLocaleString()}`;
  };

  const getStatusIcon = (isOver: boolean) => {
    return isOver ? (
      <AlertCircle className="h-4 w-4 text-destructive" />
    ) : (
      <CheckCircle className="h-4 w-4 text-success" />
    );
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Usage & Quotas</CardTitle>
          <Badge variant="outline">
            {subscription.planName}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {metrics.map((metric) => {
            const percentage = getQuotaPercentage(metric.key);
            const isOver = isOverQuota(metric.key);
            const isUnlimited = metric.quota === -1;

            return (
              <div key={metric.key} className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                    {metric.icon}
                    <span className="font-medium">{metric.label}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {!isUnlimited && getStatusIcon(isOver)}
                    <span className={isOver ? 'text-destructive font-medium' : ''}>
                      {formatValue(metric.value, metric.quota)}
                      {metric.unit}
                    </span>
                  </div>
                </div>
                {!isUnlimited && (
                  <Progress 
                    value={percentage} 
                    className={`h-2 ${isOver ? '[&>div]:bg-destructive' : ''}`}
                  />
                )}
                {isUnlimited && (
                  <div className="text-xs text-muted-foreground">Unlimited</div>
                )}
              </div>
            );
          })}
        </div>
        
        {subscription.daysRemaining !== undefined && subscription.daysRemaining < 7 && (
          <div className="mt-4 p-3 rounded-lg bg-warning/10 border border-warning/20">
            <div className="flex items-center gap-2 text-sm text-warning">
              <AlertCircle className="h-4 w-4" />
              <span>
                Your subscription renews in {subscription.daysRemaining} days
              </span>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}