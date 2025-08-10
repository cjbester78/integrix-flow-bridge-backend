// @ts-nocheck - Temporary suppression for unused imports/variables
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdapterMonitoring } from '@/services/adapterMonitoringService';

interface AdapterStatsProps {
  adapters: AdapterMonitoring[];
  isBusinessComponentSelected: boolean;
  statusFilter: string | null;
  onStatusFilter: (status: string | null) => void;
}

export const AdapterStats = ({ 
  adapters, 
  isBusinessComponentSelected, 
  statusFilter, 
  onStatusFilter 
}: AdapterStatsProps) => {
  const totalAdapters = adapters.length;
  const activeAdapters = adapters.filter(a => a.status === 'running').length;
  const inactiveAdapters = adapters.filter(a => a.status === 'idle').length;
  const stoppedAdapters = adapters.filter(a => a.status === 'stopped').length;
  const errorAdapters = adapters.filter(a => a.status === 'error').length;

  const handleStatusClick = (status: string) => {
    onStatusFilter(statusFilter === status ? null : status);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'running' ? 'ring-2 ring-success' : ''
        }`}
        onClick={() => handleStatusClick('running')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Active Adapters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-success">{activeAdapters}</div>
          <p className="text-xs text-muted-foreground">
            {isBusinessComponentSelected ? 'for selected component' : 'across all adapters'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'idle' ? 'ring-2 ring-warning' : ''
        }`}
        onClick={() => handleStatusClick('idle')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Inactive Adapters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-warning">{inactiveAdapters}</div>
          <p className="text-xs text-muted-foreground">
            {isBusinessComponentSelected ? 'for selected component' : 'across all adapters'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'stopped' ? 'ring-2 ring-muted-foreground' : ''
        }`}
        onClick={() => handleStatusClick('stopped')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Stopped Adapters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-muted-foreground">{stoppedAdapters}</div>
          <p className="text-xs text-muted-foreground">
            {isBusinessComponentSelected ? 'for selected component' : 'across all adapters'}
          </p>
        </CardContent>
      </Card>

      <Card 
        className={`cursor-pointer transition-all hover:shadow-md ${
          statusFilter === 'error' ? 'ring-2 ring-destructive' : ''
        }`}
        onClick={() => handleStatusClick('error')}
      >
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Error Adapters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-destructive">{errorAdapters}</div>
          <p className="text-xs text-muted-foreground">
            {isBusinessComponentSelected ? 'for selected component' : 'across all adapters'}
          </p>
        </CardContent>
      </Card>
    </div>
  );
};